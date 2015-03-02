package com.ufavaloro.android.visu.maininterface;

public enum MainInterfaceMessage {

	NOTHING(1),
	BLUETOOTH_CONNECTED(2),
	BLUETOOTH_DISCONNECTED(3);
	
	private final int value;

	private MainInterfaceMessage(int value){
		this.value=value;
	}
	
	public static MainInterfaceMessage values(int what) {
		switch(what){
		case 1: return NOTHING;
		case 2: return BLUETOOTH_CONNECTED;
		case 3: return BLUETOOTH_DISCONNECTED;
		
		default: return NOTHING;
		}	
	}
	
	public int getValue(){return value;}
	

}
