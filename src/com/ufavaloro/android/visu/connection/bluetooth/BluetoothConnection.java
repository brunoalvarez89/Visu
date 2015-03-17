/*****************************************************************************************
 * BluetoothConnection.java																 *
 * Clase que administra la conexión Bluetooth entre dos dispositivos.					 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.connection.bluetooth;

import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;

import com.ufavaloro.android.visu.connection.Connection;
import com.ufavaloro.android.visu.connection.ConnectionMessage;
import com.ufavaloro.android.visu.connection.ConnectionMode;
import com.ufavaloro.android.visu.connection.ConnectionType;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


public class BluetoothConnection extends Connection {
/*****************************************************************************************
* Inicio de atributos de clase			        									     *
*****************************************************************************************/
	// Debugging
    private static final String TAG = "Bluetooth";
    private static final boolean mLog = false;
    
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
	private BluetoothAdapter mBluetoothAdapter;
	
	// Threads
	private ClientThread mSlaveThread = null;
	private MasterThread mMasterThread = null;
	private ConnectedThread mConnectedThread = null;

	// Nombre del dispositivo con el cual me conecté
	private String mRemoteDevice;
	
/*****************************************************************************************
* Inicio de métodos de clase			        									     *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales                                  								 	 *
*****************************************************************************************/
	// Constructor
	public BluetoothConnection(ConnectionType connectionType, ConnectionMode connectionMode
								, Handler connectionInterfaceHandler, int connectionIndex) {
		super(connectionType, connectionMode, connectionInterfaceHandler, connectionIndex);
		
		// Log
		if (mLog) Log.d(TAG, "Creando servicio Bluetooth...");
	}
	
	@Override
	// Si el dispositivo se conecta como Servidor...
	public synchronized void slaveConnection() {
		
		// Obtengo antena
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Estado por defecto
		mStatus = STATUS_DISCONNECTED;
		
		// Log
		if (mLog) Log.d(TAG, "Iniciando Servicio Bluetooth como servidor...");
		
		// Cancelo cualquier Thread de Establecer Conexion
		stopMasterThread();
		
		// Cancelo cualquier Thread de Conexion Establecida
	    stopConnectedThread();
		
	    // Inicializo el Thread de Dialogar Conexion para escuchar en un BluetoothServerSocket
		if (mSlaveThread == null) {
			mSlaveThread = new ClientThread(); 
			mSlaveThread.start();
		}
		
		// Actualizo estado
		setStatus(STATUS_LISTENING);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.LISTENING_RFCOMM.getValue()).sendToTarget();
	}

	// Si el dispositivo se conecta como Cliente...
	public synchronized void masterConnection(BluetoothDevice device) {
		
		// Log
		if (mLog) Log.d(TAG, "Iniciando Servicio BT como cliente...");	
		
		// Cancelo cualquier Thread de Establecer Conexion
		if (mStatus == STATUS_SEARCHING) stopMasterThread();
		
		// Cancelo cualquier Thread de Conexion Establecida
		stopConnectedThread();
		
		// Inicializo el Thread de Establecer Conexion
		mMasterThread = new MasterThread(device);
		mMasterThread.start();
		
		// Actualizo estado
		setStatus(STATUS_SEARCHING);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.LOOKING_FOR_DEVICES.getValue()).sendToTarget();

	}
	
	// Si los dispositivos ya se conectaron...
	private synchronized void connected(BluetoothSocket connectedSocket) {
		
		// Log
		if (mLog) Log.d(TAG, "Intentando conectar dispositivos...");
		
		// Cierro cualquier Thread de Escuchar Conexion
		stopSlaveThread();
		
		// Cancelo cualquier Thread de Establecer Conexion
		stopMasterThread();
		
		// Cancelo cualquier Thread que este en una conexion
	    stopConnectedThread();
		
	    // Empiezo el Thread de Conexion Establecida
		mConnectedThread = new ConnectedThread(connectedSocket);
		mConnectedThread.start();
	    
		// Actualizo estado
		setStatus(STATUS_CONNECTED);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.CONNECTED.getValue()).sendToTarget();
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
* Otros métodos		                                								 	 *
*****************************************************************************************/
	// Metodo que para todos los Threads
	public void stop() {
		
		// Mato todo
		stopMasterThread();
		stopSlaveThread();
		stopConnectedThread();
		
		// Actualizo estado
		setStatus(STATUS_DISCONNECTED);
		
		// Informo
		mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.DISCONNECTED.getValue()).sendToTarget();
	}

	// Método que mata el Thread Cliente
	private void stopMasterThread() {
		// Mato Thread Cliente
		if (mMasterThread != null) mMasterThread.cancel(); 		
	}
	
	// Método que mata el Thread Servidor
	private void stopSlaveThread() {
		// Mato Thread Servidor
		if (mSlaveThread != null) mSlaveThread.cancel(); 
	}
	
	// Método que mata el Thread de Conexión Establecida
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
* THREAD SLAVE                                  								 	 *
* Se utiliza para crear y escuchar un Bluetooth Server Socket. 							 *
*****************************************************************************************/
	private class ClientThread extends Thread {
		
		// Socket Bluetooth Servidor. Se utiliza para escuchar y aceptar conexiones entrantes.
		private final BluetoothServerSocket mmBluetoothServerSocket;

		// Constructor
		public ClientThread() {
			
			// Log
			if (mLog) Log.d(TAG, "Inicializando ThreadServidor()...");
			
			// Inicializo Socket a null 
			BluetoothServerSocket tmp = null;
			
			// Obtengo el Socket Servidor
			try {
				
				// Log
				if (mLog) Log.d(TAG, "Generando canal RFCOMM...");
				
				// Escucho... blocking call!
				tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
				
				// Log
				if (mLog) Log.d(TAG, "Canal RFCOMM generado exitosamente.");
			
			} catch (IOException e) {
				
				// Log
				if (mLog) Log.d(TAG, "Error al generar canal RFCOMM (" + e.getMessage() +").");
			
			}
			
			// Si no puede obtener un Socket Servidor, seguirá siendo nulo
			mmBluetoothServerSocket = tmp;
		}
	
		// Thread.run()
		public void run() {
			
			// Socket Bluetooth. A traves de este socket se realizara la transferencia de informacion.
			BluetoothSocket mmBluetoothSocket = null;
			
			// Escucho hasta conectarme
			while (mStatus != STATUS_CONNECTED) {
				
				try {
					
					if (mLog) Log.d(TAG, "A la espera de conexiones entrantes...");
					
					// Escucho socket servidor... blocking call!
					mmBluetoothSocket = mmBluetoothServerSocket.accept();
					
					// Si se conectó un dispositivo, lo obtengo
					BluetoothDevice remoteDevice = mmBluetoothSocket.getRemoteDevice();
					
					// Obtengo nombre en String
					mRemoteDevice = remoteDevice.getName();
					
					// Informo
					mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.REMOTE_DEVICE.getValue()
										   						,-1
										   						, -1
										   						, mRemoteDevice).sendToTarget();
					
					// Log
					if (mLog) Log.d(TAG, "Conectado con " + mRemoteDevice + ".");
				
				} catch (IOException e) {
					
					// Log
					if (mLog) Log.d(TAG, "Conexión entrante rechazada (" + e.getMessage() + ").");
					
					break;
				
				}
				
			// Conexión aceptada	
				if (mmBluetoothSocket != null) {
                   
					synchronized (BluetoothConnection.this) {
                        
						switch (mStatus) {
                        
                        // Todo normal. Inicializo la conexión.
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
* THREAD MASTER                           										    	 *
* Se utiliza para crear un Bluetooth Socket. 											 *
*****************************************************************************************/
	private class MasterThread extends Thread {
		
		// Socket Bluetooth
		private BluetoothSocket mmBluetoothSocket;
		
		// Dispositivo Bluetooth local
		private final BluetoothDevice mmLocalDevice;
			
		// Constructor
		public MasterThread(BluetoothDevice localDevice) {
			
			// Log
			if (mLog) Log.d(TAG, "Inicializando ThreadCliente()...");
			
			// Dummy socket
			BluetoothSocket tmp = null;
			
			// Obtengo dispositivo bluetooth local
			mmLocalDevice = localDevice;
			
			/*
			Method m = null;
			try {
				m = mmBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			} catch (NoSuchMethodException e) {
				if (D) Log.d(TAG, "Error en la generación del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
			}
			
			try {
				tmp = (BluetoothSocket) m.invoke(mmBluetoothDevice, Integer.valueOf(3));
				if (D) Log.d(TAG, "BluetoothSocket generado exitosamente.");
			} catch (IllegalAccessException e) {
				if (D) Log.d(TAG, "Error en la generación del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
				} 
			  catch (IllegalArgumentException e) {
				if (D) Log.d(TAG, "Error en la generación del BluetoothSocket (" + e.getMessage() + ").");
				e.printStackTrace();
				} 
			  catch (InvocationTargetException e) {
				if (D) Log.d(TAG, "Error en la generación del BluetoothSocket (" + e.getMessage() + ").");
			    e.printStackTrace();
			}
			// Obtengo el BluetoothSocket para conectarme con el BluetoothDevice seleccionado
			*/
			
			  try {
				  
				// Creo canal RFCOMM
				tmp = mmLocalDevice.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
			
			  } catch (IOException e) {}
			
			// Log
			if (mLog) Log.d(TAG, "Socket cliente creado: " + tmp);
			
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
				if (mLog) Log.d(TAG, "Ejecutando BluetoothSocket.connect()...");
				
				// Intento conectarme... blocking call!
				mmBluetoothSocket.connect();
				
				// Log
				if (mLog) Log.d(TAG, "Conectado a " + mmLocalDevice.getName() + " exitosamente." );
			
			} catch(IOException e1) {
				
				// Log
				if (mLog) Log.d(TAG, "mmBluetoothSocket.connect() falló (" + e1.getMessage() +"), cerrando Socket...");
				
				// Intento cerrar el socket
				try {
					
					mmBluetoothSocket.close();
				
				} catch (IOException e2) {
					
					// Log
					if (mLog) Log.d(TAG, "mmBluetoothSocket.close() falló (" + e2.getMessage() +").");
				
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
* THREAD DE CONEXIÓN ESTABLECIDA                  								     	 *
* Permite leer y escribir información en un Socket conectado.						 	 *
*****************************************************************************************/ 
	private class ConnectedThread extends Thread {
		
		// Socket al cual se encuentra anclada la conexión
		private final BluetoothSocket mmBluetoothSocket;
		
		// Stream de entrada
		private final InputStream mmInputStream;
		
		// Stream de salida
		private final OutputStream mmOutputStream;
		
		// Buffer de recepción
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
			 * read(buffer) devuelve error si no pudo leer o hay desconexión			 *
			 ****************************************************************************/
			
			// Log
			if (mLog) Log.d(TAG, "Iniciando bucle de escucha...");
			
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
					mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.NEW_SAMPLE.getValue()
											, -1 
											, mConnectionIndex
											, mmInputBuffer[0]).sendToTarget();
				
				}// Desconexión! 
				 catch (IOException e) { 
					
					 // Log
					 if (mLog) Log.d(TAG, "Conexión perdida (" + e.getMessage() +").");
					
					 // Actualizo estado
					setStatus(STATUS_DISCONNECTED);
				
					// Informo
					mConnectionInterfaceHandler.obtainMessage(ConnectionMessage.DISCONNECTED.getValue()).sendToTarget();
					break; 
				}	
			}//while
		}
		
		// Constructor de clase
		public ConnectedThread(BluetoothSocket Socket) {
			// Log
			if (mLog) Log.d(TAG, "Inicializando ThreadConexion()...");
			
			// Socket conectado
			mmBluetoothSocket = Socket;
			
			// Dummy streams
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Obtengo InputStream y OutputStream del BluetoothSocket
			try {
				
				// Log
				if (mLog) Log.d(TAG, "Generando InputSTream() y OutputStream()...");
				
				// Obtengo inpustream
				tmpIn = mmBluetoothSocket.getInputStream();
				
				// Obtengo outputstream
				tmpOut = mmBluetoothSocket.getOutputStream();
				
				// Log
				if (mLog) Log.d(TAG, "InputSTream() y OutputStream() generados exitosamente.");
			
			} catch (IOException e) {				
				
				// Log
				if (mLog) Log.d(TAG, "Error en la creación de InputSTream() y OutputStream() (" + e.getMessage() +").");
			
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
				mmInputStream.close();
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
