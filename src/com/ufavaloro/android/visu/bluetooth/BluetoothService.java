/*****************************************************************************************
 * BluetoothService.java																 *
 * Clase que administra la conexi�n Bluetooth entre dos dispositivos.					 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.bluetooth;

import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;

import com.ufavaloro.android.visu.bluetooth.BluetoothServiceMessage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


public class BluetoothService {
/*****************************************************************************************
* Inicio de atributos de clase			        									     *
*****************************************************************************************/
	//region.start
	
	// Debugging
    private static final String TAG = "Bluetooth";
    private static final boolean D = true;
    
	// UUID del dispositivo
	private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	// Nombre del servicio Bluetooth que se mostrara en el BluetoothSocket
	private static final String SERVICE_NAME = "Bluetooth";
	
	// Estado de la conexion
	private int mStatus;
	public static final int STATUS_DISCONNECTED = 0; // Desconectado
	public static final int STATUS_LISTENING = 1; 	 // Escuchando conexiones entrantes (Servidor)
	public static final int STATUS_SEARCHING = 2;    // Inicializando una conexion saliente (Cliente)
	public static final int STATUS_CONNECTED = 3;    // Conectado
	
	// Adaptador Bluetooth local (antena del dispositivo)
	private final BluetoothAdapter mBluetoothAdapter;
	
	// Handler de la conexi�n 
	private final Handler mHandler;
	
	// Threads
	private ServerThread mServerThread = null;
	private ClientThread mClientThread = null;
	private ConnectedThread mConnectedThread = null;

	// Nombre del dispositivo con el cual me conect�
	private String mRemoteDevice;
	
	// Canal de conexi�n
	private int mBluetoothChannel;

	//region.end
	
/*****************************************************************************************
* Inicio de m�todos de clase			        									     *
*****************************************************************************************/
/*****************************************************************************************
* M�todos principales                                  								 	 *
*****************************************************************************************/
	// Constructor
	public BluetoothService(Handler mHandler, int mBluetoothChannel) {
		
		// Log
		if (D) Log.d(TAG, "Creando servicio Bluetooth...");
		
		// Obtengo antena
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Referencio al Handler
		this.mHandler = mHandler;
		
		// N�mero de canal bluetooth
		this.mBluetoothChannel = mBluetoothChannel;
		
		// Estado por defecto
		mStatus = STATUS_DISCONNECTED;
	}
	
	// Si el dispositivo se conecta como Servidor...
	public synchronized void serverSide() {
		
		// Log
		if (D) Log.d(TAG, "Iniciando Servicio Bluetooth como servidor...");
		
		// Cancelo cualquier Thread de Establecer Conexion
		stopClientThread();
		
		// Cancelo cualquier Thread de Conexion Establecida
	    stopConnectedThread();
		
	    // Inicializo el Thread de Dialogar Conexion para escuchar en un BluetoothServerSocket
		if (mServerThread == null) {
			mServerThread = new ServerThread(); 
			mServerThread.start();
		}
		
		// Actualizo estado
		setStatus(STATUS_LISTENING);
		
		// Informo
		mHandler.obtainMessage(BluetoothServiceMessage.LISTENING_RFCOMM.getValue()).sendToTarget();
	}

	// Si el dispositivo se conecta como Cliente...
	public synchronized void clientSide(BluetoothDevice device) {
		
		// Log
		if (D) Log.d(TAG, "Iniciando Servicio BT como cliente...");	
		
		// Cancelo cualquier Thread de Establecer Conexion
		if (mStatus == STATUS_SEARCHING) stopClientThread();
		
		// Cancelo cualquier Thread de Conexion Establecida
		stopConnectedThread();
		
		// Inicializo el Thread de Establecer Conexion
		mClientThread = new ClientThread(device);
		mClientThread.start();
		
		// Actualizo estado
		setStatus(STATUS_SEARCHING);
		
		// Informo
		mHandler.obtainMessage(BluetoothServiceMessage.LOOKING_FOR_DEVICES.getValue()).sendToTarget();

	}
	
	// Si los dispositivos ya se conectaron...
	private synchronized void connected(BluetoothSocket connectedSocket) {
		
		// Log
		if (D) Log.d(TAG, "Intentando conectar dispositivos...");
		
		// Cierro cualquier Thread de Escuchar Conexion
		stopServerThread();
		
		// Cancelo cualquier Thread de Establecer Conexion
		stopClientThread();
		
		// Cancelo cualquier Thread que este en una conexion
	    stopConnectedThread();
		
	    // Empiezo el Thread de Conexion Establecida
		mConnectedThread = new ConnectedThread(connectedSocket);
		mConnectedThread.start();
	    
		// Actualizo estado
		setStatus(STATUS_CONNECTED);
		
		// Informo
		mHandler.obtainMessage(BluetoothServiceMessage.CONNECTED.getValue()).sendToTarget();
	}

	// Metodo de escritura sobre el Socket
	public synchronized void asyncWrite(byte[] out) {
		ConnectedThread temp;
		synchronized(this) {
			if (mStatus != STATUS_CONNECTED) return;
			temp = mConnectedThread;
		}
		temp.syncWrite(out);
	}
	
	
/*****************************************************************************************
* Otros m�todos		                                								 	 *
*****************************************************************************************/
	// Metodo que para todos los Threads
	public void stop() {
		
		// Mato todo
		stopClientThread();
		stopServerThread();
		stopConnectedThread();
		
		// Actualizo estado
		setStatus(STATUS_DISCONNECTED);
	}

	// M�todo que mata el Thread Cliente
	private void stopClientThread() {
		// Mato Thread Cliente
		if (mClientThread != null) mClientThread.cancel(); 		
	}
	
	// M�todo que mata el Thread Servidor
	private void stopServerThread() {
		// Mato Thread Servidor
		if (mServerThread != null) mServerThread.cancel(); 
	}
	
	// M�todo que mata el Thread de Conexi�n Establecida
	private void stopConnectedThread() {
		// Mato Thread Conexion
		if (mConnectedThread != null) mConnectedThread.cancel();
	}

	// Getter de Estado
	public int getStatus() {
		return mStatus;
	}
	
	// Getter de Dispositivo Remoto
	public String getRemoteDevice() {
		return mRemoteDevice;
	}
	
	// Setter de Estado
	private void setStatus(int mStatus) {
		this.mStatus = mStatus;
	}
	
	
/*****************************************************************************************
* THREAD SERVIDOR                                   								 	 *
* Se utiliza para crear y escuchar un Bluetooth Server Socket. 							 *
*****************************************************************************************/
	private class ServerThread extends Thread {
		
		// Socket Bluetooth Servidor. Se utiliza para escuchar y aceptar conexiones entrantes.
		private final BluetoothServerSocket mmBluetoothServerSocket;
		
		// Dispositivo Bluetooth remoto
		private String mmRemoteDevice = "Sin nombre";
		
		// Constructor
		public ServerThread() {
			
			// Log
			if (D) Log.d(TAG, "Inicializando ThreadServidor()...");
			
			// Inicializo Socket a null 
			BluetoothServerSocket tmp = null;
			
			// Obtengo el Socket Servidor
			try {
				
				// Log
				if (D) Log.d(TAG, "Generando canal RFCOMM...");
				
				// Escucho... blocking call!
				tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
				
				// Log
				if (D) Log.d(TAG, "Canal RFCOMM generado exitosamente.");
			
			} catch (IOException e) {
				
				// Log
				if (D) Log.d(TAG, "Error al generar canal RFCOMM (" + e.getMessage() +").");
			
			}
			
			// Si no puede obtener un Socket Servidor, seguir� siendo nulo
			mmBluetoothServerSocket = tmp;
		}
	
		// Thread.run()
		public void run() {
			
			// Socket Bluetooth. A traves de este socket se realizara la transferencia de informacion.
			BluetoothSocket mmBluetoothSocket = null;
			
			// Escucho hasta conectarme
			while (mStatus != STATUS_CONNECTED) {
				
				try {
					
					if (D) Log.d(TAG, "A la espera de conexiones entrantes...");
					
					// Escucho socket servidor... blocking call!
					mmBluetoothSocket = mmBluetoothServerSocket.accept();
					
					// Si se conect� un dispositivo, lo obtengo
					BluetoothDevice remoteDevice = mmBluetoothSocket.getRemoteDevice();
					
					// Obtengo nombre en String
					mmRemoteDevice = remoteDevice.getName();
					
					// Informo
					mHandler.obtainMessage(BluetoothServiceMessage.REMOTE_DEVICE.getValue()
										   ,-1, -1, mmRemoteDevice).sendToTarget();
					
					// Log
					if (D) Log.d(TAG, "Conectado con " + mRemoteDevice + ".");
				
				} catch (IOException e) {
					
					// Log
					if (D) Log.d(TAG, "Conexi�n entrante rechazada (" + e.getMessage() + ").");
					
					break;
				
				}
				
			// Conexi�n aceptada	
				if (mmBluetoothSocket != null) {
                   
					synchronized (BluetoothService.this) {
                        
						switch (mStatus) {
                        
                        // Todo normal. Inicializo la conexi�n.
						case STATUS_LISTENING:
						case STATUS_SEARCHING:
							
            				connected(mmBluetoothSocket);
                            
            				break;
                        
                        // No preparado o ya conectado. Cierro Socket.
						case STATUS_DISCONNECTED:
						case STATUS_CONNECTED:
                            try {
                            	
                            	mmBluetoothSocket.close();
                            
                            } catch (IOException e) {}
                            break;
                        }
						
                    }
                }	
			}//while
		}
		
		// Mato Thread
		public void cancel() {
			try {
				mmBluetoothServerSocket.close();
				} catch (IOException e) {}
		}
	
	}//ThreadServidor

	
/*****************************************************************************************
* THREAD CLIENTE                           										    	 *
* Se utiliza para crear un Bluetooth Socket. 											 *
*****************************************************************************************/
	private class ClientThread extends Thread {
		
		// Socket Bluetooth
		private BluetoothSocket mmBluetoothSocket;
		
		// Dispositivo Bluetooth local
		private final BluetoothDevice mmLocalDevice;
			
		// Constructor
		public ClientThread(BluetoothDevice localDevice) {
			
			// Log
			if (D) Log.d(TAG, "Inicializando ThreadCliente()...");
			
			// Dummy socket
			BluetoothSocket tmp = null;
			
			// Obtengo dispositivo bluetooth local
			mmLocalDevice = localDevice;
			
			/*
			Method m = null;
			try {
				m = mmBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			} catch (NoSuchMethodException e) {
				if (D) Log.d(TAG, "Error en la generaci�n del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
			}
			
			try {
				tmp = (BluetoothSocket) m.invoke(mmBluetoothDevice, Integer.valueOf(3));
				if (D) Log.d(TAG, "BluetoothSocket generado exitosamente.");
			} catch (IllegalAccessException e) {
				if (D) Log.d(TAG, "Error en la generaci�n del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
				} 
			  catch (IllegalArgumentException e) {
				if (D) Log.d(TAG, "Error en la generaci�n del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
				} 
			  catch (InvocationTargetException e) {
				if (D) Log.d(TAG, "Error en la generaci�n del BluetoothSocket (" + e.getMessage() + ").");
			    e.printStackTrace();
			}
			// Obtengo el BluetoothSocket para conectarme con el BluetoothDevice seleccionado
			*/
			
			  try {
				  
				// Creo canal RFCOMM
				tmp = mmLocalDevice.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
			
			  } catch (IOException e) {}
			
			// Log
			if (D) Log.d(TAG, "Socket cliente creado: " + tmp);
			
			// Obtengo socket generado
			mmBluetoothSocket = tmp;
		}
			
		// Thread.run()
		@Override
		public void run() {
			
			// Cancelo escucha de dispositivos
			mBluetoothAdapter.cancelDiscovery();
			
			// Intento conectarme
			try {
				
				// Log
				if (D) Log.d(TAG, "Ejecutando BluetoothSocket.connect()...");
				
				// Intento conectarme... blocking call!
				mmBluetoothSocket.connect();
				
				// Log
				if (D) Log.d(TAG, "Conectado a " + mmLocalDevice.getName() + " exitosamente." );
			
			} catch(IOException e1) {
				
				// Log
				if (D) Log.d(TAG, "mmBluetoothSocket.connect() fall� (" + e1.getMessage() +"), cerrando Socket...");
				
				// Intento cerrar el socket
				try {
					
					mmBluetoothSocket.close();
				
				} catch (IOException e2) {
					
					// Log
					if (D) Log.d(TAG, "mmBluetoothSocket.close() fall� (" + e2.getMessage() +").");
				
				}
			}
			
			// Empiezo el Thread de conexion establecida
			connected(mmBluetoothSocket);
		}

		// Mato Thread Cliente
		public void cancel() {
			
			try {
				
				mmBluetoothSocket.close();
			
			} catch (IOException e) {}
			
		}
	
	}//ThreadCliente
	
	
/*****************************************************************************************
* THREAD DE CONEXI�N ESTABLECIDA                  								     	 *
* Permite leer y escribir informaci�n en un Socket conectado.						 	 *
*****************************************************************************************/ 
	private class ConnectedThread extends Thread {
		
		// Socket al cual se encuentra anclada la conexi�n
		private final BluetoothSocket mmBluetoothSocket;
		
		// Stream de entrada
		private final InputStream mmInputStream;
		
		// Stream de salida
		private final OutputStream mmOutputStream;
		
		// Buffer de recepci�n
		private byte[] mmInputBuffer = new byte[1];
		
		// Flag de run
		private boolean mRun = true;
		
		// Candado que traba el thread
		private Object mPauseLock = new Object();
		
		// Flag de pausa
		private boolean mPaused = false;
		
		// Byte que recibo
		@SuppressWarnings("unused")
		int mmByte;
		
		// Thread.run()
		public void run() {
			
			/***************************************************************************** 
			 * BUCLE DE ESCUCHA															 *
			 * read(buffer) devuelve -1 si End Of Stream                                 *
			 * read(buffer) devuelve error si no pudo leer o hay desconexi�n			 *
			 ****************************************************************************/
			
			// Log
			if (D) Log.d(TAG, "Iniciando bucle de escucha...");
			
			// 
			while(mRun) {
				try {
					
					// Candado de pausa
					// Deja el Thread en espera utilizando wait() hasta que mPaused == false
					synchronized(mPauseLock) {
						while(mPaused) {
							try {
								mPauseLock.wait();
							} catch (InterruptedException e) {}
						}
					}

					// Leo InputStream
					mmByte = mmInputStream.read(mmInputBuffer);
					
					// Envio los Bytes recibidos a la UI mediante el Handler
					// @param Object = datos
					// @param arg1 = canal
					mHandler.obtainMessage(BluetoothServiceMessage.NEW_SAMPLE.getValue(), mBluetoothChannel, 
										   -1, mmInputBuffer[0]).sendToTarget();
				
				}// Desconexi�n! 
				 catch (IOException e) { 
					
					 // Log
					 if (D) Log.d(TAG, "Conexi�n perdida (" + e.getMessage() +").");
					
					 // Actualizo estado
					setStatus(STATUS_DISCONNECTED);
				
					// Informo
					mHandler.obtainMessage(BluetoothServiceMessage.CONNECTION_LOST.getValue(), 
										   mBluetoothChannel, -1, mmInputBuffer).sendToTarget();
					break; 
				}	
			}//while
		}
		
		// Constructor de clase
		public ConnectedThread(BluetoothSocket Socket) {
			// Log
			if (D) Log.d(TAG, "Inicializando ThreadConexion()...");
			
			// Socket conectado
			mmBluetoothSocket = Socket;
			
			// Dummy streams
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Obtengo InputStream y OutputStream del BluetoothSocket
			try {
				
				// Log
				if (D) Log.d(TAG, "Generando InputSTream() y OutputStream()...");
				
				// Obtengo inpustream
				tmpIn = mmBluetoothSocket.getInputStream();
				
				// Obtengo outputstream
				tmpOut = mmBluetoothSocket.getOutputStream();
				
				// Log
				if (D) Log.d(TAG, "InputSTream() y OutputStream() generados exitosamente.");
			
			} catch (IOException e) {				
				
				// Log
				if (D) Log.d(TAG, "Error en la creaci�n de InputSTream() y OutputStream() (" + e.getMessage() +").");
			
			}
			
			// Guardo los streams
			mmInputStream = tmpIn;
			mmOutputStream = tmpOut;
		}
				
		// Metodo de escritura sobre el Socket
		public void syncWrite(byte[] buffer) {
			
			try {
				
				// Escribo en el buffer y flusheo
				//mmOutputStream.write(buffer);
				mmOutputStream.flush();
				
			} catch (IOException e) {}
		
		}
		
		// Metodo para cerrar el Socket
		public void cancel() {
			
			mRun = false;
			
			try {
			
				mmBluetoothSocket.close();
			
			} catch (IOException e) {}
		}

		// Pauseo el Thread
		@SuppressWarnings("unused")
		public void onPause() {
			
			synchronized (mPauseLock) {
			
				mPaused = true;
			
			}
		
		}
		 
		// Resumo el Thread
		@SuppressWarnings("unused")
		public void onResume() {
		    
			synchronized (mPauseLock) {
		    
				mPaused = false;
		       
				mPauseLock.notifyAll();
		    
			}
		
		}	
	
	}//ThreadConexionEstablecida
	
	
}//BluetoothService
