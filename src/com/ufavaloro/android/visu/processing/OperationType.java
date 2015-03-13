package com.ufavaloro.android.visu.processing;

public enum OperationType {

	NULL(-1),
	TIME_FIRST_ORDER_DERIVATIVE(1),
	EKG_QRS_MAF(2);
	
	private final int value;

	private OperationType(int value){
		this.value=value;
	}
	
	public static OperationType values(int what) {
		switch(what){
		case 1: return TIME_FIRST_ORDER_DERIVATIVE;
		case 2: return EKG_QRS_MAF;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
