/*****************************************************************************************
 * GoogleDriveMessage.java																 *
 * Enum que posee los distintos tipos de mensaje que puede enviar la clase 				 *
 * GoogleDriveManager			 														 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage.googledrive;

public enum GoogleDriveClientMessage {

	CONNECTED(1),
	CONNECTION_FAILED(2),
	DISCONNECTED(3),
	CONNECTION_SUSPENDED(4),
	FOLDER_CREATED(5),
	FOLDER_NOT_CREATED(6),
	FOLDER_ALREADY_EXISTS(7),
	FOLDER_ITERATE(8),
	FILE_CREATED(9),
	FILE_NOT_CREATED(10),
	FILE_ALREADY_EXISTS(11),
	FILE_OPENED(12),
	FILE_NOT_OPENED(13);
	
	private final int value;

	private GoogleDriveClientMessage(int value){
		this.value=value;
	}
	
	public static GoogleDriveClientMessage values(int what) {
		
		switch(what){
		
			case 1: return CONNECTED;
			case 2: return CONNECTION_FAILED;
			case 3: return DISCONNECTED;
			case 4: return CONNECTION_SUSPENDED;
			case 5: return FOLDER_CREATED;
			case 6: return FOLDER_NOT_CREATED;
			case 7: return FOLDER_ALREADY_EXISTS;
			case 8: return FOLDER_ITERATE;
			case 9: return FILE_CREATED;
			case 10: return FILE_NOT_CREATED;
			case 11: return FILE_ALREADY_EXISTS;
			case 12: return FILE_OPENED;
			case 13: return FILE_NOT_OPENED;
		
			default: return DISCONNECTED;
		
		}	
	
	}
	
	public int getValue(){return value;}
	

}//GoogleDriveClientMessage
