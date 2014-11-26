package com.ufavaloro.android.visu.storage.data;

import java.util.ArrayList;

public class StudyData {

	private AcquisitionData mAcquisitionData;
	private PatientData mPatientData;
	private StorageData mStorageData;
	private ArrayList<Short> mDataBuffer;
	
	public void setAcquisitionData(AcquisitionData acquisitionData) {
		mAcquisitionData = acquisitionData;
	}
	
	public void setPatientData(PatientData patientData) {
		mPatientData = patientData;
	}
	
	public void setStorageData(StorageData storageData) {
		mStorageData = storageData;
	}
	
	public void setDataBuffer(ArrayList<Short> dataBuffer) {
		mDataBuffer = dataBuffer;
	}
	
	public AcquisitionData getAcquisitionData() {
		return mAcquisitionData;
	}
	
	public PatientData getPatientData() {
		return mPatientData;
	}
	
	public StorageData getStorageData() {
		return mStorageData;
	}

	public ArrayList<Short> getDataBuffer() {
		return mDataBuffer;
	}
}//StudyData