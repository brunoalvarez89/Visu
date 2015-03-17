package com.ufavaloro.android.visu.connection;

public enum ConnectionMode {

	NULL(-1),
	SLAVE(1),
	MASTER(2);
	
	private final int value;

	private ConnectionMode(int value){
		this.value=value;
	}
	
	public static ConnectionMode values(int what) {
		switch(what) {
		case 1: return SLAVE;
		case 2: return MASTER;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}
