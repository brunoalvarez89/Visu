package com.ufavaloro.android.visu.connection;

public enum ConnectionType {

	NULL(-1),
	BLUETOOTH(1);
	
	private final int value;

	private ConnectionType(int value){
		this.value=value;
	}
	
	public static ConnectionType values(int what) {
		switch(what) {
		case 1: return BLUETOOTH;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
