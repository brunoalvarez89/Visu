package com.ufavaloro.android.visu.connection;

import java.util.ArrayList;

import com.google.android.gms.internal.bl;
import com.ufavaloro.android.visu.connection.bluetooth.BluetoothConnection;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class ConnectionInterface {

 	private Handler mMainInterfaceHandler;
 	private ArrayList<Connection> mConnection;

 	public ConnectionInterface(Handler mainInterfaceHandler) {
 		mMainInterfaceHandler = mainInterfaceHandler;
 		mConnection = new ArrayList<Connection>();
 	}
 	
 	public void addConnection(ConnectionType connectionType) {

 		switch(connectionType) {
 		
 			case BLUETOOTH:
 				Connection bluetoothConnection = 
 				(Connection) new BluetoothConnection(connectionType, mConnectionHandler);
 				bluetoothConnection.slaveConnection();
 				mConnection.add(bluetoothConnection);
 				break;
 			
 			default:
 				break;
 		}
 	}
 	
 	@SuppressLint("HandlerLeak")
	private final Handler mConnectionHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			BluetoothConnectionMessage connectionMessage = BluetoothConnectionMessage.values(msg.what);
			
			switch (connectionMessage) {
				
			// Llegó una muestra nueva 
			case NEW_SAMPLE:
				// Obtengo muestra
				byte sample = (Byte) msg.obj;
				// Obtengo canal
				int bluetoothChannel = msg.arg1;
				
				// Si el visualizador está configurado
				if(mConfigurationOk == true) {
					enqueueSample(sample);
				} else {
					// Si recibí la cantidad de canales
					if(mChannelsOk == false) {
						parseChannelMesssage(sample, bluetoothChannel);
					} else {
						parseAdcMessage(sample, bluetoothChannel);
					}
				}
				
				break;
				
			// Escuchando conexiones entrantes
			case LISTENING_RFCOMM:
				break;
			
			// Me conecté
			case CONNECTED: 
				// Informo
				mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.CONNECTED.getValue()).sendToTarget();
				mConnected = true;
				break;
		
			// Obtengo nombre del dispositivo con el cual me conecté
			case REMOTE_DEVICE: 
				mActualRemoteDevice = (String) msg.obj;
				mRemoteDevice.add(mActualRemoteDevice);
				break;
				
			// Me desconecté
			case DISCONNECTED:	
				// Informo
				mConnectionInterfaceHandler.obtainMessage(ProtocolMessage.DISCONNECTED.getValue()).sendToTarget();
				mConnected = false;
				mTotalBluetoothConnections--;
				mConfigurationOk = false;
				mChannelsOk = false;
				mStatus = WAITING_FOR_CONTROL;
				break;

			default: 
				break;
			
			}
		}
	};
 	
 	@SuppressLint("HandlerLeak")
	private final Handler mProtocolHandler = new Handler() {
		
 		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			ProtocolMessage protocolMessage = ProtocolMessage.values(msg.what);
			
			switch (protocolMessage) {
				
				case NEW_SAMPLES_BATCH:
					short[] samples = (short[]) msg.obj;
					int channel = msg.arg2;
					//onNewSamplesBatch(samples, channel);
					break;
				
				case ADC_DATA:
					AdcData[] adcData = (AdcData[]) msg.obj;
					//onAdcData(adcData);
					break;
					
				case TOTAL_ADC_CHANNELS:
					int totalAdcChannels = (Integer) msg.obj;
					//onTotalAdcChannels(totalAdcChannels);
					break;
					
				case CONNECTED:
					//onBluetoothConnected();
					break;
				
				case DISCONNECTED:
					//onBluetoothDisconnected();
					break;
					
				default:
					break;
			
			}
		}
	};
}
