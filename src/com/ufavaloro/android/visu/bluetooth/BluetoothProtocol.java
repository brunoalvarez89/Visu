/*****************************************************************************************
 * BluetoothHelper.java																	 *
 * Clase que sirve de interfaz entre Study y el servicio de conexi�n Bluetooth.			 *
 * Posee m�todos para control y procesamiento de paquetes recibidos.					 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.bluetooth;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.datatypes.AdcData;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;


public class BluetoothProtocol extends Thread{

/*****************************************************************************************
* Inicio de atributos de clase											   				 *
*****************************************************************************************/
	private AdcData[] adcData;
	private Handler mHandler;
	
/*****************************************************************************************
* Variables de control													   				 *
*****************************************************************************************/
	// Estados 
	private final int WAITING_FOR_CONTROL = 0;
	private final int WAITING_FOR_SAMPLES = 1;
	private int mStatus = WAITING_FOR_CONTROL;
	
	// Cantidad de bytes de control (3 por defecto)
	private int mControlBytes = 3;
	
	// Flags de configuraci�n
	private boolean mChannelsOk = false;
	private boolean mConfigurationOk = false;

	// Contador de '#' recibidos
	private int mHashCount = 0;
	
	// Cantidad de muestras recibidas
	private int mSampleByteCount = 0;
	
	// Cantidad de bytes del mensaje de canal recibidos
	private int mChannelByteCount = 0;

/*****************************************************************************************
* Atributos de conexi�n											   						 *
*****************************************************************************************/
	// Array de conexiones (sockets) BT
	private SparseArray<BluetoothService> mBtConnections = new SparseArray<BluetoothService>();
	
	// Contador de conexiones establecidas
	private int mTotalBluetoothConnections = 0;
	
	// Array de dispositivos remotos
	private ArrayList<String> mRemoteDevice = new ArrayList<String>();
	
	// Dispositivo remoto actual
	public String mActualRemoteDevice;
	
	// Cantidad de muestras por paquete
	private int mSamplesPerPackage;
	
	// Cantidad de bytes que voy a utilizar para cada muestra
	private int mBytesPerSample;

	// Flag de conectado
	private boolean mConnected = false;

/*****************************************************************************************
* Mensajes de configuraci�n													   			 *
*****************************************************************************************/
	/*************************************************************************************
	* Mensaje de informaci�n del ADC								 				 	 *
	*************************************************************************************/
	// Cantidad de bytes para indicar el canal (4 bytes -int- por defecto)
	private int mChannelBytes = 4;
	
	// Mensaje de 4 Bytes con la cantidad de canales a procesar.
	private byte[] mChannelMessage = new byte[mChannelBytes];
	
	// Buffers en bytes de la cantidad de canales
	private byte[] mChannelByteBuffer = new byte[mChannelBytes];
	
	/*************************************************************************************
	* Mensaje de informaci�n del ADC								 				 	 *
	*************************************************************************************/
	// Cantidad de Bytes del mensaje de informaci�n del ADC. Se setea luego de recibir 
	// la cantidad de canales.
	private int mAdcMessageTotalBytes;
	
	// Mensaje de informaci�n del ADC
	private byte[] mAdcMessage;

/*****************************************************************************************
* Variables de graficaci�n 											   				 	 *
*****************************************************************************************/
	// Cantidad de canales por conexi�n
	private int mTotalAdcChannels = 0;
	
	// Canal actual de graficaci�n
	private int mActualChannel;

	// Buffer de bytes para almacenar temporalmente las muestras recibidas por canal
	private byte[] mByteBufferedInput;

/*****************************************************************************************
* Inicio de m�todos de clase														   	 *
*****************************************************************************************/
/*****************************************************************************************
* M�todos principales																	 *
*****************************************************************************************/
	// Constructor
	public BluetoothProtocol(Handler handler) {
		mHandler = handler;
	}
	
	// Handler de BluetoothService
    @SuppressLint("HandlerLeak")
	private final Handler mBluetoothServiceHandler = new Handler() {
		
    	// M�todo para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			BluetoothServiceMessage bluetoothServiceMessage = BluetoothServiceMessage.values(msg.what);
			
			switch (bluetoothServiceMessage) {
				
				// Lleg� una muestra nueva 
				case NEW_SAMPLE:
					// Obtengo muestra
					byte sample = (Byte) msg.obj;
					// Obtengo canal
					int bluetoothChannel = msg.arg1;
					
					// Si el visualizador est� configurado
					if(mConfigurationOk == true) {
						enqueueSample(sample);
					}
					
					// Si el visualizador no est� configurado
					if(mConfigurationOk == false) {
						// Si recib� la cantidad de canales
						if(mChannelsOk == false) {
							parseChannelMesssage(sample, bluetoothChannel);
						}
						// Si no recib� la cantidad de canales
						if(mChannelsOk == true) {
							parseAdcMessage(sample, bluetoothChannel);
						}
					}
					break;
					
				// Escuchando conexiones entrantes
				case LISTENING_RFCOMM:
					break;
				
				// Me conect�
				case CONNECTED: 
					addBluetoothConnection();
					mConnected = true;
					break;
			
				// Obtengo nombre del dispositivo con el cual me conect�
				case REMOTE_DEVICE: 
					mActualRemoteDevice = (String) msg.obj;
					mRemoteDevice.add(mActualRemoteDevice);
					break;
					
				// Me desconect�
				case DISCONNECTED:					
					break;

				// Perd� la conexi�n
				case CONNECTION_LOST:
					// Obtengo canal
					bluetoothChannel = msg.arg1;
					// Lo elimino del array de conexiones
					mBtConnections.remove(bluetoothChannel);
					// Vuelvo a setear los flags de configuraci�n en cero y el estado al
					// estado por defecto.   
					mConfigurationOk = false;
					mChannelsOk = false;
					mStatus = WAITING_FOR_CONTROL;
					break;
			
				default: 
					break;
			}//switch
		}
	};//mHandlerConexion

	// M�todo que agrega una Conexi�n Bluetooth a la lista de conexiones
	public void addBluetoothConnection() {
		
		BluetoothService btConnection= new BluetoothService(mBluetoothServiceHandler, mTotalBluetoothConnections);
		
		mBtConnections.put(mTotalBluetoothConnections, btConnection);
		
		mBtConnections.get(mTotalBluetoothConnections).serverSide();
		
		mTotalBluetoothConnections++;
	
	}
	
	// M�todo que frena todas las conexiones
	public void stopConnections() {
		
		if (mBtConnections != null) {
			
			for(int i=0; i < mTotalBluetoothConnections; i++) {
				mBtConnections.get(i).stop();
			}
			
		}
	}

	// M�todo que transmite las muestras recibidas
	private void newBatch(short[] batch, int channel) {
		
		// Informo
		mHandler.obtainMessage(BluetoothProtocolMessage.NEW_SAMPLES_BATCH.getValue()
							   ,-1, channel, batch).sendToTarget();

	}

	// M�todo que parsea los datos arrays con los datos obtenidos del ADC
	public void createAdcInfo(double[] voltages, double[] amplitudes, double[] fs, int[] bits) {
			
		double vMax;
		double vMin;
		double aMax;
		double aMin;
				
		char[] remoteSensor = mActualRemoteDevice.toCharArray();
		
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
	* Parseo del mensaje con las caracter�sticas del ADC						         *
	*************************************************************************************/
	// M�todo que configura el mensaje de configuraci�n ADC a recibir
	private void setAdcMessage(byte[] bytes) {
		
		// Contenido del paquete de LSB a MSB
		
		// 1) Vmax y Vmin de cada canal (2 doubles por canal)
		int voltageBlock = 2*mTotalAdcChannels*8;
		
		// 2) Amplitudes m�ximas y m�nimas de cada canal (2 doubles por canal)
		int amplitudeBlock = 2*mTotalAdcChannels*8;
		
		// 3) Frecuencia de muestreo de cada canal (1 double por canal)
		int fsBlock = mTotalAdcChannels*8;
		
		// 4) Resoluci�n de cada canal (1 entero por canal)
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
	
	// M�todo que parsea el mensaje con las caracter�sticas del ADC
	private void parseAdcMessage(Byte sample, int btChannel) {
		
		if(checkControl(sample) == true) return;
		
		if(mStatus == WAITING_FOR_SAMPLES) {
			mAdcMessage[mSampleByteCount] = sample;
			mSampleByteCount++;
			if(mSampleByteCount == mAdcMessageTotalBytes) {
				configureAdc(mAdcMessage, btChannel);
				mConfigurationOk = true;
				mStatus = WAITING_FOR_CONTROL;
				mSampleByteCount = 0;
			}
		}
	}
		
	// M�todo que setea los par�metros del ADC
	private void configureAdc(byte[] bytes, int btChannel) {
		
		// Contenido del paquete de control de LSB a MSB
		// 1) Vmax y Vmin de cada canal (2 voltajes * mCantCanales * 8 bytes = 16*mCantCanales)
		// 2) Amax y Amin de cada canal (2 valores * mCantCanales * 8 bytes = 16*mCantCanales)
		// 3) Frecuencia de muestreo de cada canal (8 bytes)
		// 4) Resoluci�n de cada canal (4 bytes)
		// 5) Cantidad de muestras por paquete (4 bytes)
		// 6) Cantidad de bytes por muestra (4 bytes)
		//
		// TAMA�O TOTAL = (32*mCantCanales + 20) bytes
		
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
		
		// Creo buffers de recepci�n de datos
		mByteBufferedInput = new byte[mSamplesPerPackage*mBytesPerSample];

		// Creo buffers de almacenamiento y graficaci�n
		createAdcInfo(voltages, amplitudes, fs, bits);
		
		// Informo
		mHandler.obtainMessage(BluetoothProtocolMessage.ADC_DATA.getValue()
				   ,-1, -1, adcData).sendToTarget();

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
	// M�todo que parsea el mensaje con la cantidad de canales
	private void parseChannelMesssage(Byte sample, int btChannel) {
	
		if(checkControl(sample) == true) return;
	
		if(mStatus == WAITING_FOR_SAMPLES) {
			mChannelMessage[mSampleByteCount] = sample;
			mSampleByteCount++;
			if(mSampleByteCount == mChannelBytes) {
				configureChannelQuantity(mChannelMessage, btChannel);
				mChannelsOk = true;
				mStatus = WAITING_FOR_CONTROL;
				mSampleByteCount = 0;
			}
		}
	}
	
	// M�todo que recibe y setea la cantidad de canales
	private void configureChannelQuantity(byte[] bytes, int btChannel) {
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		mTotalAdcChannels = byteArrayToInt(byteBuffer);
		setAdcMessage(bytes);
		
		// Informo
		mHandler.obtainMessage(BluetoothProtocolMessage.TOTAL_ADC_CHANNELS.getValue()
							   ,-1, -1, mTotalAdcChannels).sendToTarget();
		
		//byte[] mensajeCanalesOk = new byte[1];
		//mensajeCanalesOk[0] = '&';
		//mConexionBT.get(canalBT).Escribir(mensajeCanalesOk);
	}
	
	
	/*************************************************************************************
	* Control y graficaci�n de muestras											         *
	*************************************************************************************/
	// M�todo que chequea si estoy recibiendo una # (byte de control)
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
	
	// M�todo que arma el paquete de muestras y lo env�a al Drawing Surface
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
			
			if(mSampleByteCount < mSamplesPerPackage*mBytesPerSample) {
			mByteBufferedInput[mSampleByteCount] = sample;
			mSampleByteCount++;
				if(mSampleByteCount == mSamplesPerPackage*mBytesPerSample) {
					short[] shortBufferedInput = byteArrayToShortArray(mByteBufferedInput.clone());
					mStatus = WAITING_FOR_CONTROL;
					mSampleByteCount = 0;
					mChannelByteCount = 0;
					
					newBatch(shortBufferedInput, mActualChannel);
					
					return;
				}
			return;
			}
		}
	}

	
/*****************************************************************************************
* M�todos de conversi�n entre tipos de dato										         *
*****************************************************************************************/	
	// M�todo para pasar las muestras de byte a short
	private short[] byteArrayToShortArray(byte[] byteBuffer) {
		short muestra = 0;
		short mascara = 0xFF;
		// Esta linea me genera un nuevo vector para cada paquete
		byte[] aux = new byte[byteBuffer.length];
		for(int i = 0; i < byteBuffer.length; i++) aux[i] = byteBuffer[i];
		
		short[] shortBufferedInput= new short[mSamplesPerPackage];
		
		for(int i=0; i<mSamplesPerPackage; i++) {
			muestra = (short) (byteBuffer[(2*i)] & mascara);
			muestra = (short) (muestra + ((byteBuffer[(2*i) + 1] & mascara) << 8));
			shortBufferedInput[i] = muestra;
		}
		return shortBufferedInput;
	}

	// M�todo para pasar de byte a int
	private int byteArrayToInt(ByteBuffer byteBuffer) {
		byte[] intBytes = new byte[4];
		for(int i=0; i<4; i++) {
			intBytes[i] = byteBuffer.get(i);
		}
		return ByteBuffer.wrap(intBytes).getInt();
	}

	
/*****************************************************************************************
* Env�o de informaci�n															         *
*****************************************************************************************/	
	// Getter de estado de conexi�n
	public boolean getConnected() {
		return mConnected;
	}

	// Getter de dispositivo remoto actual
	public String getActualRemoteDevice() {
	
		return mActualRemoteDevice;
		
	}
	
	// Getter de cantidad de canales del Adc 
	public int getTotalAdcChannels() {
		
		return mTotalAdcChannels;
	
	}
	
	// Getter de configuraci�n
	public boolean getConfigurationOk() {
		return mConfigurationOk;
	}

}//BluetoothHelper
