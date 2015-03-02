/*****************************************************************************************
 * BluetoothServiceMessage.java																 *
 * Enum que posee los distintos tipos de mensaje que puede enviar la clase				 *
 * BluetoothService a BluetoothHelper													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.bluetooth;

public enum BluetoothServiceMessage {

	DISCONNECTED(1),
	LOOKING_FOR_DEVICES(2),
	LISTENING_RFCOMM(3),
	CONNECTED(4),
	NEW_SAMPLE(5),
	REMOTE_DEVICE(6);
	
	private final int value;

	private BluetoothServiceMessage(int value){
		this.value=value;
	}
	
	public static BluetoothServiceMessage values(int what) {
		switch(what){
		case 1: return DISCONNECTED;
		case 2: return LOOKING_FOR_DEVICES;
		case 3: return LISTENING_RFCOMM;
		case 4: return CONNECTED;
		case 5: return NEW_SAMPLE;
		case 6: return REMOTE_DEVICE;
		
		default: return DISCONNECTED;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothServiceMessage
