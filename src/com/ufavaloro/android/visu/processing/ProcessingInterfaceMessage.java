package com.ufavaloro.android.visu.processing;

public enum ProcessingInterfaceMessage {

	NULL(-1),
	HEARTBEAT(0);
	
	private final int value;

	private ProcessingInterfaceMessage(int value){
		this.value=value;
	}
	
	public static ProcessingInterfaceMessage values(int what) {
		switch(what){
		case 0: return HEARTBEAT;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
