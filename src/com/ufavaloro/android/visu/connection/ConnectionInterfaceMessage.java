package com.ufavaloro.android.visu.connection;

public enum ConnectionInterfaceMessage {

	NULL(-1),
	CONNECTED(1),
	DISCONNECTED(2),
	ADC_DATA(3),
	NEW_SAMPLES_BATCH(4),
	NEW_SAMPLE(5);
	
	private final int value;

	private ConnectionInterfaceMessage(int value){
		this.value=value;
	}
	
	public static ConnectionInterfaceMessage values(int what) {
		switch(what){
		case 1: return CONNECTED;
		case 2: return DISCONNECTED;
		case 3: return ADC_DATA;
		case 4: return NEW_SAMPLES_BATCH;
		case 5: return NEW_SAMPLE;
		
		default: return DISCONNECTED;
		}	
	}
	
	public int getValue(){return value;}
	

}
