package com.ufavaloro.android.visu.storage.data;

import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.SamplesBuffer;

public class StudyData {

	private AcquisitionData mAcquisitionData;
	private PatientData mPatientData;
	private StorageData mStorageData;
	private SamplesBuffer mSamplesBuffer;
	private boolean mMarkedForStoring;
	
	public StudyData() {
		mAcquisitionData = new AcquisitionData();
		mPatientData = new PatientData();
		mStorageData = new StorageData();
		mSamplesBuffer = new SamplesBuffer();
		mMarkedForStoring = false;
	}
	
	public void setAcquisitionData(AcquisitionData acquisitionData) {
		mAcquisitionData = acquisitionData;
	}
	
	public void setPatientData(PatientData patientData) {
		mPatientData = patientData;
	}
	
	public void setStorageData(StorageData storageData) {
		mStorageData = storageData;
	}
	
	public void setSamplesBuffer(ArrayList<Short> samplesBuffer) {
		mSamplesBuffer.createSamplesBuffer(samplesBuffer);
	}
	
	public void setSamplesBuffer(SamplesBuffer samplesBuffer) {
		mSamplesBuffer = samplesBuffer;
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

	public SamplesBuffer getSamplesBuffer() {
		return mSamplesBuffer;
	}
	
	public boolean isMarkedForStoring() {
		return mMarkedForStoring;
	}
	
	public void setMarkedForStoring(boolean mMarkedForStoring) {
		this.mMarkedForStoring = mMarkedForStoring;
	}
	
}//StudyData