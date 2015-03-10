package com.ufavaloro.android.visu.processing;

public enum OperationType {

	NULL(-1),
	QRS_DETECTION(1),
	HEARTBEAT(2);
	
	private final int value;

	private OperationType(int value){
		this.value=value;
	}
	
	public static OperationType values(int what) {
		switch(what){
		case 1: return QRS_DETECTION;
		case 2: return HEARTBEAT;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
