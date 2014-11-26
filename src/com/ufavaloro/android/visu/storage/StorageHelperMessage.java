package com.ufavaloro.android.visu.storage;

public enum StorageHelperMessage {

	GOOGLE_DRIVE_CONNECTED(1),
	GOOGLE_DRIVE_SUSPENDED(2),
	GOOGLE_DRIVE_DISCONNECTED(3),
	GOOGLE_DRIVE_CONNECTION_FAILED(4),
	GOOGLE_DRIVE_FILE_OPENED(5),
	LOCAL_STORAGE_FILE_OPENED(6);
	
	private final int value;

	private StorageHelperMessage(int value){
		this.value=value;
	}
	
	public static StorageHelperMessage values(int what) {
		switch(what){
		case 1: return GOOGLE_DRIVE_CONNECTED;
		case 2: return GOOGLE_DRIVE_SUSPENDED;
		case 3: return GOOGLE_DRIVE_DISCONNECTED;
		case 4: return GOOGLE_DRIVE_CONNECTION_FAILED;
		case 5: return GOOGLE_DRIVE_FILE_OPENED;
		case 6: return LOCAL_STORAGE_FILE_OPENED;
		
		default: return GOOGLE_DRIVE_FILE_OPENED;
		}	
	}
	
	public int getValue(){return value;}
	

}//StorageHelperMessage
