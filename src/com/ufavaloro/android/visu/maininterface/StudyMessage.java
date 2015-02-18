package com.ufavaloro.android.visu.maininterface;

public enum StudyMessage {

	CONFIGURATION_OK(1);

	private final int value;

	private StudyMessage(int value){
		this.value=value;
	}
	
	public static StudyMessage values(int what) {
		switch(what){
		case 1: return CONFIGURATION_OK;

		default: return CONFIGURATION_OK;
		}	
	}
	
	public int getValue(){return value;}
	
}
