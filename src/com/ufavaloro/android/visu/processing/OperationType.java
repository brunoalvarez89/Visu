package com.ufavaloro.android.visu.processing;

public enum OperationType {

	NULL(-1),
	QRS_DETECTION_MAF(1),
	QRS_DETECTION_DERIVATIVE(2),
	HEARTBEAT(3),
	LOWPASS(4);
	
	private final int value;

	private OperationType(int value){
		this.value=value;
	}
	
	public static OperationType values(int what) {
		switch(what){
		case 1: return QRS_DETECTION_MAF;
		case 2: return QRS_DETECTION_DERIVATIVE;
		case 3: return HEARTBEAT;
		case 4: return LOWPASS;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
