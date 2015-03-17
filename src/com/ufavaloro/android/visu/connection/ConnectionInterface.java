package com.ufavaloro.android.visu.connection;

import java.util.ArrayList;

import com.google.android.gms.internal.bl;
import com.ufavaloro.android.visu.connection.bluetooth.BluetoothConnection;
import com.ufavaloro.android.visu.connection.protocol.Protocol;
import com.ufavaloro.android.visu.connection.protocol.ProtocolMessage;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class ConnectionInterface {

 	private Handler mMainInterfaceHandler;
 	private ArrayList<Connection> mConnectionList;
 	private ArrayList<Protocol> mProtocolList;

 	public ConnectionInterface(Handler mainInterfaceHandler) {
 		mMainInterfaceHandler = mainInterfaceHandler;
 		mConnectionList = new ArrayList<Connection>();
 		mProtocolList = new ArrayList<Protocol>();
 	}
 	
 	public void addConnection(ConnectionType connectionType, ConnectionMode connectionMode) {

 		switch(connectionType) {
 		
 			case BLUETOOTH:
 				int connectionIndex = mConnectionList.size();
 				Connection bluetoothConnection = (Connection) 
 				new BluetoothConnection(connectionType, connectionMode, mConnectionHandler, connectionIndex);
 				mConnectionList.add(bluetoothConnection);
 				
 				int protocolIndex = connectionIndex;
 				Protocol protocol = new Protocol(mProtocolHandler, protocolIndex);
 				mProtocolList.add(protocol);
 				
 				break;
 			
 			default:
 				break;
 		}
 	}
 	
 	public void removeConnection(int connectionIndex) {
		//if(mMainInterface != null) mMainInterface.getBluetoothProtocol().stopConnections();
 	}
 	
 	public Connection getConnection(int connectionIndex) {
 		return mConnectionList.get(connectionIndex);
 	}
 	
 	public Protocol getProtocol(int protocolIndex) {
 		return mProtocolList.get(protocolIndex);
 	}
 	
/*****************************************************************************************
* Connection Handler
*****************************************************************************************/
 	@SuppressLint("HandlerLeak")
	private final Handler mConnectionHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			ConnectionMessage connectionMessage = ConnectionMessage.values(msg.what);
			int connectionIndex = msg.arg2;
			
			switch (connectionMessage) {
				
			// Llegó una muestra nueva 
			case NEW_SAMPLE:
				// Obtengo muestra
				byte sample = (Byte) msg.obj;
				mProtocolList.get(connectionIndex).checkSample(sample);
				break;
				
			// Escuchando conexiones entrantes
			case LISTENING_RFCOMM:
				break;
			
			// Me conecté
			case CONNECTED: 
				// Informo
				mMainInterfaceHandler.obtainMessage(ConnectionInterfaceMessage.CONNECTED.getValue()).sendToTarget();
				mProtocolList.get(connectionIndex).setConnected(true);
				break;
		
			// Obtengo nombre del dispositivo con el cual me conecté
			case REMOTE_DEVICE: 
				String remoteDevice = (String) msg.obj;
				//mProtocolList.get(connectionIndex).setRemoteDevice(remoteDevice);
				break;
				
			// Me desconecté
			case DISCONNECTED:	
				// Informo
				mMainInterfaceHandler.obtainMessage(ConnectionInterfaceMessage.DISCONNECTED.getValue()).sendToTarget();
				mProtocolList.get(connectionIndex).setConnected(false);
				mProtocolList.get(connectionIndex).setConfigured(false);
				mProtocolList.get(connectionIndex).setChannelsInfo(false);
				mProtocolList.get(connectionIndex).setStatus(0);
				//mConfigurationOk = false;
				//mChannelsOk = false;
				//mStatus = WAITING_FOR_CONTROL;
				break;

			default: 
				break;
			
			}
		}
	};

/*****************************************************************************************
* Protocol Handler
*****************************************************************************************/
 	@SuppressLint("HandlerLeak")
	private final Handler mProtocolHandler = new Handler() {
		
 		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			ProtocolMessage protocolMessage = ProtocolMessage.values(msg.what);
			int protocolIndex = msg.arg2;
			
			switch (protocolMessage) {
				
				case NEW_SAMPLES_BATCH:
					short[] samples = (short[]) msg.obj;
					mMainInterfaceHandler.obtainMessage(ConnectionInterfaceMessage.NEW_SAMPLE.getValue()
														, -1
														, protocolIndex
														, samples).sendToTarget();
					break;
				
				case ADC_DATA:
					AdcData[] adcData = (AdcData[]) msg.obj;
					mMainInterfaceHandler.obtainMessage(ConnectionInterfaceMessage.CONFIGURED.getValue()
														, -1
														, protocolIndex
														, adcData).sendToTarget();
					break;
		
				default:
					break;
			
			}
		}
	};
}
