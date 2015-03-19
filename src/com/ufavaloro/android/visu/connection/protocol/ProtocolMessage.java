/*****************************************************************************************
 * BluetoothMessage.java																 *
 * Enum que posee los distintos tipos de mensaje que puede enviar la clase				 *
 * BluetoothService a BluetoothHelper													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.connection.protocol;

public enum ProtocolMessage {
	
	NULL (-1),
	NEW_SAMPLES_BATCH(1),
	NEW_SAMPLE(2),
	ADC_DATA(3),
	TOTAL_ADC_CHANNELS(4);
	
	private final int value;

	private ProtocolMessage(int value){
		this.value=value;
	}
	
	public static ProtocolMessage values(int what) {
		switch(what){
		case 1: return NEW_SAMPLES_BATCH;
		case 2: return NEW_SAMPLE;
		case 3: return ADC_DATA;
		case 4: return TOTAL_ADC_CHANNELS;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}
