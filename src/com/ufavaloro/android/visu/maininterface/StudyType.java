package com.ufavaloro.android.visu.maininterface;

public enum StudyType {

	Ninguno(0),
	ECG(1),
	EEG(2),
	Bruxismo(3);
	
	private final int value;
	private static int totalStudyTypes = 4;
	
	private StudyType(int value) {
		this.value=value;
	}
	 
	public int getValue() {
		return value;
	}
	
	public static StudyType values(int what) {
		switch(what){
		case 0: return Ninguno;
		case 1: return ECG;
		case 2: return EEG;
		case 3: return Bruxismo;
		
		default: return Ninguno;
		}	
	}
	
	public static int getTotalStudyTypes() {
		return totalStudyTypes;
	}
	
	public static String getUnits(StudyType studyType) {
		switch(studyType) {
			case Ninguno:
				return "?";
			case ECG:
				return "mV";
			case EEG:
				return "mV";
			case Bruxismo:
				return "Pa";
			default:
				return "?";
		}	
	}

}//Enum
