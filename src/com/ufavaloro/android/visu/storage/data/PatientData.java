package com.ufavaloro.android.visu.storage.data;

public class PatientData {
	
	// Nombre del paciente
	private char[] mPatientName;
	private int mPatientNameSize = 50;
	private int mPatientNameBytes = mPatientNameSize*((Character.SIZE)/8);
	
	// Apellido del paciente
	private char[] mPatientSurname;
	private int mPatientSurnameSize = 50;
	private int mPatientSurnameBytes = mPatientSurnameSize*((Character.SIZE)/8);
	
	// Nombre del estudio
	private char[] mStudyName;
	private int mStudyNameSize = 100;
	private int mStudyNameBytes = mStudyNameSize*((Character.SIZE)/8);
	
	private int mPatientDataBytes;
	
	public PatientData() {
		
		mPatientName = new char[mPatientNameSize];
		mPatientSurname = new char[mPatientSurnameSize];
		mStudyName = new char[mStudyNameSize];
		
		mPatientDataBytes = mPatientNameBytes + mPatientSurnameBytes + mStudyNameBytes;
		
	}

	public PatientData(char[] patientName, char[] patientSurname, char[] studyName) {
		mPatientName = patientName;
		mPatientSurname = patientSurname;
		mStudyName = studyName;
	}

	public void setPatientName(String patientName) {
		mPatientName = patientName.toCharArray();
	}
	
	public void setPatientSurname(String patientSurname) {
		mPatientSurname = patientSurname.toCharArray();
	}

	public char[] getPatientName() {
		return mPatientName;
	}
	
	public char[] getPatientSurname() {
		return mPatientSurname;
	}

	public void setStudyName(String studyName) {
		mStudyName = studyName.toCharArray();
	}

	public char[] getStudyName() {
		return mStudyName;
	}

	public int getPatientDataBytes() {
		return mPatientDataBytes;
	}

	public int getPatientNameSize() {
		return mPatientNameSize;
	}
	
	public int getPatientSurnameSize() {
		return mPatientSurnameSize;
	}
	
	public int getStudyNameSize() {
		return mStudyNameSize;
	}
	
		
}
