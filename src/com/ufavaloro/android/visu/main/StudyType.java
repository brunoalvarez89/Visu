package com.ufavaloro.android.visu.main;

public enum StudyType {

	Ninguno(0),
	ECG(1),
	Presion(2),
	CO2(3);
	
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
		case 2: return Presion;
		case 3: return CO2;
		
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
			case Presion:
				return "mmHg";
			case CO2:
				return "mmHg";
			default:
				return "?";
		}	
	}

}//Enum
