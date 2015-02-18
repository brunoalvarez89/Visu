package com.ufavaloro.android.visu.storage.googledrive;

public enum GoogleDriveInterfaceMessage {

	GOOGLE_DRIVE_FILE_OPEN(1),
	GOOGLE_DRIVE_CONNECTED(2),
	GOOGLE_DRIVE_SUSPENDED(3),
	GOOGLE_DRIVE_DISCONNECTED(4),
	GOOGLE_DRIVE_CONNECTION_FAILED(5);
	
	private final int value;

	private GoogleDriveInterfaceMessage(int value){
		this.value=value;
	}
	
	public static GoogleDriveInterfaceMessage values(int what) {
		
		switch(what){
		
			case 1: return GOOGLE_DRIVE_FILE_OPEN;
			case 2: return GOOGLE_DRIVE_CONNECTED;
			case 3: return GOOGLE_DRIVE_SUSPENDED;
			case 4: return GOOGLE_DRIVE_DISCONNECTED;
			case 5: return GOOGLE_DRIVE_CONNECTION_FAILED;
			
			default: return GOOGLE_DRIVE_DISCONNECTED;
		
		}	
	
	}
	
	public int getValue(){return value;}
}