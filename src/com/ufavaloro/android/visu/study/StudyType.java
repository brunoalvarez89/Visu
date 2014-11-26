package com.ufavaloro.android.visu.study;

public enum StudyType {

	STUDY_EMPTY(0),
	STUDY_SPO2(1),
	STUDY_EKG(2),
	STUDY_EEG(3),
	STUDY_NIP(4);
	
	private final int value;
	
	private StudyType(int value) {
		this.value=value;
	}
	 
	public int getValue() {
		return value;
	}
	
	public static StudyType values(int what) {
		switch(what){
		case 0: return STUDY_EMPTY;
		case 1: return STUDY_SPO2;
		case 2: return STUDY_EKG;
		case 3: return STUDY_EEG;
		case 4: return STUDY_NIP;
		
		default: return STUDY_EMPTY;
		}	
	}

}//Enum
