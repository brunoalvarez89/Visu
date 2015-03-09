package com.ufavaloro.android.visu.processing;

public enum ProcessingInterfaceMessage {

	NULL(-1),
	SUCCESS(1),
	ERROR(2);
	
	private final int value;

	private ProcessingInterfaceMessage(int value){
		this.value=value;
	}
	
	public static ProcessingInterfaceMessage values(int what) {
		switch(what){
		case 1: return SUCCESS;
		case 2: return ERROR;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
