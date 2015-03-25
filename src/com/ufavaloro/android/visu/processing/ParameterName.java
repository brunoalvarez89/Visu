package com.ufavaloro.android.visu.processing;

public enum ParameterName {

	NULL(-1),
	BPM(1);
	
	private final int value;

	private ParameterName(int value){
		this.value=value;
	}
	
	public static ParameterName values(int what) {
		switch(what){
		case 1: return BPM;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	
}
