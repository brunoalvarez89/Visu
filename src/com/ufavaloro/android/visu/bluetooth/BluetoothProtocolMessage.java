/*****************************************************************************************
 * BluetoothMessage.java																 *
 * Enum que posee los distintos tipos de mensaje que puede enviar la clase				 *
 * BluetoothService a BluetoothHelper													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.bluetooth;

public enum BluetoothProtocolMessage {

	NEW_SAMPLES_BATCH(1),
	ADC_DATA(2),
	TOTAL_ADC_CHANNELS(3);
	
	private final int value;

	private BluetoothProtocolMessage(int value){
		this.value=value;
	}
	
	public static BluetoothProtocolMessage values(int what) {
		switch(what){
		case 1: return NEW_SAMPLES_BATCH;
		case 2: return ADC_DATA;
		case 3: return TOTAL_ADC_CHANNELS;
		
		default: return NEW_SAMPLES_BATCH;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
