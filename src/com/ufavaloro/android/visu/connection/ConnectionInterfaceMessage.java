package com.ufavaloro.android.visu.connection;

public enum ConnectionInterfaceMessage {

	NULL(-1),
	CONNECTED(1),
	DISCONNECTED(2),
	CONFIGURED(3),
	NEW_SAMPLE(4);
	
	private final int value;

	private ConnectionInterfaceMessage(int value){
		this.value=value;
	}
	
	public static ConnectionInterfaceMessage values(int what) {
		switch(what){
		case 1: return CONNECTED;
		case 2: return DISCONNECTED;
		case 3: return CONFIGURED;
		case 4: return NEW_SAMPLE;
		
		default: return DISCONNECTED;
		}	
	}
	
	public int getValue(){return value;}
	

}
