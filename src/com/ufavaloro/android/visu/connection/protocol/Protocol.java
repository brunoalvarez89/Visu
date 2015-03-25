package com.ufavaloro.android.visu.connection.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.ufavaloro.android.visu.connection.ConnectionMessage;
import com.ufavaloro.android.visu.connection.bluetooth.BluetoothConnection;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;


public class Protocol extends Thread {

/*****************************************************************************************
* Inicio de atributos de clase											   				 *
*****************************************************************************************/
	// Handler to Connection Interface
	private Handler mConnectionInterfaceHandler;
	private int mProtocolIndex;
	private boolean mDebugMode = false;
	private int mPackageNumberByteCount = 0;
	private double mPackageCount;
	private double mReceivedPackages;
	private byte[] mPackageNumberByteBuffer = new byte[Double.SIZE/8];
	private boolean mLog = true;
	
/*****************************************************************************************
* Variables de control													   				 *
*****************************************************************************************/
	// Estados 
	private final int WAITING_FOR_CONTROL = 0;
	private final int WAITING_FOR_SAMPLES = 1;
	private int mStatus = WAITING_FOR_CONTROL;
	
	// Cantidad de bytes de control (3 por defecto)
	private final int mControlBytes = 3;
	
	// Flags de configuración
	private boolean mChannelsOk = false;
	private boolean mConfigurationOk = false;

	// Contador de '#' recibidos
	private int mHashCount = 0;
	
	// Contador de cantidad de muestras actuales recibidas por buffer
	private int mSamplesBufferByteCount = 0;
	
	// Contador de bytes de la muestra actual
	private int mActualSampleByteCount = 0;
	
	// Cantidad de bytes del mensaje de canal recibidos
	private int mChannelByteCount = 0;

/*****************************************************************************************
* Atributos de conexión											   						 *
*****************************************************************************************/
	// Array de conexiones (sockets) BT
	private SparseArray<BluetoothConnection> mBtConnections = new SparseArray<BluetoothConnection>();

	// Dispositivo remoto actual
	public String mRemoteDevice;
	
	// Cantidad de muestras por paquete
	private int mSamplesPerPackage;
	
	// Cantidad de bytes que voy a utilizar para cada muestra
	private int mBytesPerSample;

	// Flag de conectado
	private boolean mConnected = false;

/*****************************************************************************************
* Mensajes de configuración													   			 *
*****************************************************************************************/
	/*************************************************************************************
	* Mensaje de cantidad de canales							 				 	 *
	*************************************************************************************/
	// Cantidad de bytes para indicar el canal (4 bytes -int- por defecto)
	private final int mChannelBytes = 4;
	
	// Mensaje de 4 Bytes con la cantidad de canales a procesar.
	private byte[] mChannelMessage = new byte[mChannelBytes];
	
	// Buffers en bytes de la cantidad de canales
	private byte[] mChannelByteBuffer = new byte[mChannelBytes];
	
	/*************************************************************************************
	* Mensaje de información del ADC								 				 	 *
	*************************************************************************************/
	// Cantidad de Bytes del mensaje de información del ADC. Se setea luego de recibir 
	// la cantidad de canales.
	private int mAdcMessageTotalBytes;
	
	// Mensaje de información del ADC
	private byte[] mAdcMessage;
	
	// Array de información del adc. Su tamaño es igual a la cantidad de canales de dicho ADC.
	private AdcData[] adcData;

/*****************************************************************************************
* Atributos del paquete de muestras del ADC											   				 	 *
*****************************************************************************************/
	// Cantidad de canales por conexión
	private int mTotalAdcChannels = 0;
	
	// Canal actual de graficación
	private int mActualChannel;

	// Buffer de bytes para almacenar temporalmente las muestras recibidas por canal
	private byte[] mByteBufferedInput;
	
	// Buffer de 2 bytes para almacenar una única muestra en Short
	private byte[] mActualSampleBuffer;
	

/*****************************************************************************************
* Inicio de métodos de clase														   	 *
*****************************************************************************************/
/*****************************************************************************************
* Mètodos principales																	 *
*****************************************************************************************/
	// Constructor
	public Protocol(Handler connectionInterfaceHandler, int protocolIndex) {
		mConnectionInterfaceHandler = connectionInterfaceHandler;
		mProtocolIndex = protocolIndex;
		mRemoteDevice = "Sin Nombre";
		mActualSampleBuffer = new byte[2];
	}
	
	// Connection Sample Input
	public void checkSample(byte sample) {
		// Si el visualizador está configurado
		if(mConfigurationOk == true) {
			enqueueSample(sample);
		} else {
			// Si recibí la cantidad de canales
			if(mChannelsOk == false) {
				parseChannelMesssage(sample);
			} else {
				parseAdcMessage(sample);
			}
		}
	}
	
	// Método que transmite las muestras recibidas en grupo
	private void newBatch(short[] batch) {
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.NEW_SAMPLES_BATCH.getValue()
							   						,-1
							   						, mActualChannel
							   						, batch).sendToTarget();

	}
	
	// Método que transmite las muestras recibidas de a una
	private void newSample(short sample) {
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.NEW_SAMPLE.getValue()
									   				,-1
									   				, mActualChannel
									   				, sample).sendToTarget();
	}
	
	private void addSample(byte sample) {
		mByteBufferedInput[mSamplesBufferByteCount] = sample;
		mSamplesBufferByteCount++;
		mActualSampleByteCount++;
		
		if(mActualSampleByteCount == 2) {
			mActualSampleBuffer[0] = mByteBufferedInput[mSamplesBufferByteCount-2];
			mActualSampleBuffer[1] = mByteBufferedInput[mSamplesBufferByteCount-1];
			mActualSampleByteCount = 0;
			short[] shortSample = byteArrayToShortArray(mActualSampleBuffer.clone());
			newSample(shortSample[0]);
		}
	}
	
	// Método que parsea los datos arrays con los datos obtenidos del ADC
	public void createAdcInfo(double[] voltages, double[] amplitudes, double[] fs, int[] bits) {
			
		double vMax;
		double vMin;
		double aMax;
		double aMin;
				
		char[] remoteSensor = mRemoteDevice.toCharArray();
		
		adcData = new AdcData[mTotalAdcChannels];
		
		for(int i = 0; i < mTotalAdcChannels; i++) {
			
			vMax = voltages[2*i];
			vMin = voltages[(2*i) + 1];
			
			aMax = amplitudes[2*i];
			aMin = amplitudes[(2*i) + 1];
			
			adcData[i] = new AdcData(fs[i], bits[i], vMax, vMin, aMax, aMin, remoteSensor, 
								     mSamplesPerPackage, i);
			
		}
				
	}

/*****************************************************************************************
* Parseo y control de paquetes													         *
*****************************************************************************************/
	/*************************************************************************************
	* Parseo del mensaje con las características del ADC						         *
	*************************************************************************************/
	// Método que configura el mensaje de configuración ADC a recibir
	private void setAdcMessage(byte[] bytes) {
		
		// Contenido del paquete de LSB a MSB
		
		// 1) Vmax y Vmin de cada canal (2 doubles por canal)
		int voltageBlock = 2*mTotalAdcChannels*8;
		
		// 2) Amplitudes máximas y mínimas de cada canal (2 doubles por canal)
		int amplitudeBlock = 2*mTotalAdcChannels*8;
		
		// 3) Frecuencia de muestreo de cada canal (1 double por canal)
		int fsBlock = mTotalAdcChannels*8;
		
		// 4) Resolución de cada canal (1 entero por canal)
		int resolutionBlock = 4*mTotalAdcChannels;
		
		// 5) Cantidad de muestras por paquete (1 entero)
		int sampleQtyBlock = 4;
		
		// 6) Cantidad de bytes por muestra (1 entero)
		int bytesPerSampleBlock = 4;
		
		// Total
		mAdcMessageTotalBytes = voltageBlock +
								amplitudeBlock +
							    fsBlock +
							    resolutionBlock +
							    sampleQtyBlock + 
							    bytesPerSampleBlock;
		
		// Genero paquete
		mAdcMessage = new byte[mAdcMessageTotalBytes];
	}
	
	// Método que parsea el mensaje con las características del ADC
	private void parseAdcMessage(Byte sample) {
		
		if(checkControl(sample) == true) return;
		
		if(mStatus == WAITING_FOR_SAMPLES) {
			mAdcMessage[mSamplesBufferByteCount] = sample;
			mSamplesBufferByteCount++;
			if(mSamplesBufferByteCount == mAdcMessageTotalBytes) {
				configureAdc(mAdcMessage);
				mConfigurationOk = true;
				mStatus = WAITING_FOR_CONTROL;
				mSamplesBufferByteCount = 0;
			}
		}
	}
		
	// Método que setea los parámetros del ADC
	private void configureAdc(byte[] bytes) {
		
		// Contenido del paquete de control de LSB a MSB
		// 1) Vmax y Vmin de cada canal (2 voltajes * mCantCanales * 8 bytes = 16*mCantCanales)
		// 2) Amax y Amin de cada canal (2 valores * mCantCanales * 8 bytes = 16*mCantCanales)
		// 3) Frecuencia de muestreo de cada canal (8 bytes)
		// 4) Resolución de cada canal (4 bytes)
		// 5) Cantidad de muestras por paquete (4 bytes)
		// 6) Cantidad de bytes por muestra (4 bytes)
		//
		// TAMAÑO TOTAL = (32*mCantCanales + 20) bytes
		
		ByteBuffer auxBuffer = ByteBuffer.wrap(bytes);
		
		// 1)
		double [] voltages = new double[2*mTotalAdcChannels];
		for(int i=0; i<2*mTotalAdcChannels; i++) {
			voltages[i] = auxBuffer.getDouble();
		}
		
		// 2)
		double[] amplitudes = new double[2*mTotalAdcChannels];
		for(int i=0; i<2*mTotalAdcChannels; i++) {
			amplitudes[i] = auxBuffer.getDouble();
		}
		
		// 3)
		double[] fs = new double[mTotalAdcChannels];
		for(int i=0; i<mTotalAdcChannels; i++) {
			fs[i] = auxBuffer.getDouble();
		}
		
		// 4)
		int[] bits = new int[mTotalAdcChannels];
		for(int i=0; i<mTotalAdcChannels; i++) {
			bits[i] = auxBuffer.getInt();
		}
		
		// 5) Cantidad de muestras por paquete (4 bytes)
		mSamplesPerPackage = auxBuffer.getInt();
		
		// 6) Cantidad de bytes por muestra (4 bytes)
		mBytesPerSample = auxBuffer.getInt();
		
		// Creo buffers de recepción de datos
		mByteBufferedInput = new byte[mSamplesPerPackage*mBytesPerSample];

		// Creo buffers de almacenamiento y graficación
		createAdcInfo(voltages, amplitudes, fs, bits);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.ADC_DATA.getValue()
				   									,-1
				   									, mProtocolIndex
				   									, adcData).sendToTarget();

		byte[] mensajeAdcOk = new byte[1];
		mensajeAdcOk[0] = '$';
		//mConexionBT.get(canalBT).pausarRecepcion();
		//SystemClock.sleep(10);
		//mConexionBT.get(canalBT).Escribir(mensajeAdcOk);
		//SystemClock.sleep(10);
		//mConexionBT.get(canalBT).resumirRecepcion();
		
	}
	
	/*************************************************************************************
	* Parseo del mensaje con la cantidad de canales								         *
	*************************************************************************************/
	// Método que parsea el mensaje con la cantidad de canales
	private void parseChannelMesssage(Byte sample) {
	
		if(checkControl(sample) == true) return;
	
		if(mStatus == WAITING_FOR_SAMPLES) {
			mChannelMessage[mSamplesBufferByteCount] = sample;
			mSamplesBufferByteCount++;
			if(mSamplesBufferByteCount == mChannelBytes) {
				configureChannelQuantity(mChannelMessage);
				mChannelsOk = true;
				mStatus = WAITING_FOR_CONTROL;
				mSamplesBufferByteCount = 0;
			}
		}
	}
	
	// Método que recibe y setea la cantidad de canales
	private void configureChannelQuantity(byte[] bytes) {
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		mTotalAdcChannels = byteArrayToInt(byteBuffer);
		setAdcMessage(bytes);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.TOTAL_ADC_CHANNELS.getValue()
							   						,-1
							   						, mProtocolIndex
							   						, mTotalAdcChannels).sendToTarget();
		
		//byte[] mensajeCanalesOk = new byte[1];
		//mensajeCanalesOk[0] = '&';
		//mConexionBT.get(canalBT).Escribir(mensajeCanalesOk);
	}
	
	/*************************************************************************************
	* Control de muestras											         *
	*************************************************************************************/
	// Método que chequea si estoy recibiendo una # (byte de control)
	private boolean checkControl(Byte sample) {
		if(mStatus == WAITING_FOR_CONTROL && sample == '#') {
			mHashCount++;
			if(mHashCount == mControlBytes) {
				mHashCount = 0;
				mStatus = WAITING_FOR_SAMPLES;
				return true;
			}
			return true;
		} 
		return false;
	}
	
	// Método que arma el paquete de muestras y lo envía al Drawing Surface
	private void enqueueSample(Byte sample) {

		if(checkControl(sample) == true) return;
		
		if(mStatus == WAITING_FOR_SAMPLES) {
			
			if(mChannelByteCount < mChannelBytes) {
				mChannelByteBuffer[mChannelByteCount] = sample;
				mChannelByteCount++;
				if(mChannelByteCount == mChannelBytes) {
					ByteBuffer auxBuffer = ByteBuffer.wrap(mChannelByteBuffer);
					mActualChannel = byteArrayToInt(auxBuffer);
					return;
				}
				return;
			}
			
			if(mSamplesBufferByteCount < mSamplesPerPackage*mBytesPerSample) {
				addSample(sample);
					if(mSamplesBufferByteCount == mSamplesPerPackage*mBytesPerSample) {
						//short[] shortBufferedInput = byteArrayToShortArray(mByteBufferedInput.clone());	
						//newBatch(shortBufferedInput);
						
						if(!mDebugMode) {
							mStatus = WAITING_FOR_CONTROL;
							mSamplesBufferByteCount = 0;
							mChannelByteCount = 0;
						}
						return;
					}
				return;
			}

			if(mDebugMode) {
				if(mPackageNumberByteCount < Double.SIZE/8) {
					mPackageNumberByteBuffer[mPackageNumberByteCount] = sample;
					mPackageNumberByteCount++;
					if(mPackageNumberByteCount == Double.SIZE/8) {
						ByteBuffer auxBuffer = ByteBuffer.wrap(mPackageNumberByteBuffer);
						mPackageCount = auxBuffer.getDouble();
						//if(mLog) Log.d("Bluetooth Reception", "Paquete: " + mPackageCount);
						
						mReceivedPackages++;
						//if(mLog) Log.d("Bluetooth Reception", "Contador: " + mReceivedPackages);
						
						mStatus = WAITING_FOR_CONTROL;
						mSamplesBufferByteCount = 0;
						mChannelByteCount = 0;
						mPackageNumberByteCount = 0;						
						return;
					}
				}
				
			}
		}
	}

	
/*****************************************************************************************
* Envío de información															         *
*****************************************************************************************/	
	// Getter de estado de conexión
	public boolean isConnected() {
		return mConnected;
	}

	// Getter de dispositivo remoto actual
	public String getActualRemoteDevice() {
		return mRemoteDevice;
	}
	
	// Getter de cantidad de canales del Adc 
	public int getTotalAdcChannels() {
		
		return mTotalAdcChannels;
	
	}
	
	// Getter de configuración
	public boolean getConfigurationOk() {
		return mConfigurationOk;
	}

/*****************************************************************************************
* Otros	métodos																	         *
*****************************************************************************************/	
	// Método para pasar las muestras de byte a short
	private short[] byteArrayToShortArray(byte[] byteBuffer) {
		short muestra = 0;
		short mascara = 0xFF;
		// Esta linea me genera un nuevo vector para cada paquete
		byte[] aux = new byte[byteBuffer.length];
		for(int i = 0; i < byteBuffer.length; i++) aux[i] = byteBuffer[i];
		
		short[] shortBufferedInput= new short[byteBuffer.length/2];
		
		for(int i=0; i<byteBuffer.length/2; i++) {
			muestra = (short) (byteBuffer[(2*i)] & mascara);
			muestra = (short) (muestra + ((byteBuffer[(2*i) + 1] & mascara) << 8));
			shortBufferedInput[i] = muestra;
		}
		return shortBufferedInput;
	}

	// Método para pasar de byte a int
	private int byteArrayToInt(ByteBuffer byteBuffer) {
		byte[] intBytes = new byte[4];
		for(int i=0; i<4; i++) {
			intBytes[i] = byteBuffer.get(i);
		}
		return ByteBuffer.wrap(intBytes).getInt();
	}
	
	public void setConnected(boolean value) {
		mConnected = value;
	}
	
	public void setConfigured(boolean value) {
		mConfigurationOk = value;
	}
	
	public void setChannelsInfo(boolean value) {
		mChannelsOk = value;
	}

	public void setStatus(int status) {
		mStatus = status;
	}

	public void setRemoteDevice(String remoteDevice) {
		int a = 1;
		a = 3;
		String v = remoteDevice;	
	}
}
