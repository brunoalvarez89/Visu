package com.ufavaloro.android.visu.storage.datatypes;

import com.ufavaloro.android.visu.maininterface.StudyType;


public class AcquisitionData {
	
	// Adc Data
	private AdcData mAdcData;
	
	// Tipo de estudio
	private char[] mStudyType;
	private int mStudyTypeSize = 50;
	private int mStudyTypeBytes = mStudyTypeSize*((Character.SIZE)/8);
	
	// Cantidad de muestras almacenadas
	private double mTotalSamples;
	private int mTotalSamplesBytes = (Double.SIZE)/8;
	
	// Total de bytes para los datos de adquisición.
	private int mAcquisitionDataBytes;
	
	
	public AcquisitionData(AdcData mAdcData) {
		
		this.mAdcData = mAdcData;
		
		mStudyType = new char[mStudyTypeSize];
				
		mTotalSamples = 0;

		mAcquisitionDataBytes =  mAdcData.adcBytes + mStudyTypeBytes + mTotalSamplesBytes;
	
	}
	
	public AcquisitionData() {
		// TODO Auto-generated constructor stub
	}

	public void setAdcChannel(int mAdcChannel) {
		mAdcData.adcChannel = mAdcChannel;
	}
	
	public void setBluetoothChannel(int mBluetoothChannel) {
		mAdcData.bluetoothChannel = mBluetoothChannel;
	}
	
	public int getBits() {
		return mAdcData.bits;
	}
	
	public double getFs() {
		return mAdcData.fs;
	}
	
	public int getChannel() {
		return mAdcData.adcChannel;
	}
	
	public int getBluetoothChannel() {
		return mAdcData.bluetoothChannel;
	}
	
	public double getVMin() {
		return mAdcData.vMin;
	}
	
	public double getVMax() {
		return mAdcData.vMax;
	}

	public double getAMin() {
		return mAdcData.aMin;
	}
	
	public double getAMax() {
		return mAdcData.aMax;
	}
	
	public void setAMax(double aMax) {
		mAdcData.aMax = aMax;
	}
	
	public void setAMin(double aMin) {
		mAdcData.aMin = aMin;
	}
	
	public void setStudyType(int study) {
		
		mStudyType = new char[mStudyTypeSize];
		mStudyType[0] = (char) study;
	}
	
	public void setSensor(String sensor_string) {
		
		char[] sensor = sensor_string.toCharArray();
		int size = sensor_string.length();

		for(int i = 0; i < size; i++) {
			mAdcData.sensor[i] = sensor[i];
		}
	}
	
	public char[] getSensor() {
		return mAdcData.sensor;
	}

	public char[] getStudyType() {
		return mStudyType;
	}

	public int getAcquisitionDataBytes() {
		return mAcquisitionDataBytes;
	}

	public double getTotalSamples() {
		return mTotalSamples;
	}
	
	public int getStudyTypeSize() {
		return mStudyTypeSize;
	}
	
	public int getSensorSize() {
		return mAdcData.sensorSize;
	}

	public int getAdcChannel() {
		return mAdcData.adcChannel;
	}

	public int getSamplesPerPackage() {
		return mAdcData.samplesPerPackage;
	}

	public int getAdcChannelBytes() {
		return mAdcData.adcChannelBytes;
	}

	public void setAdcChannelBytes(int mAdcChannelBytes) {
		mAdcData.adcChannelBytes = mAdcChannelBytes;
	}

	public int getBluetoothChannelBytes() {
		return mAdcData.bluetoothChannelBytes;
	}

	public void setBluetoothChannelBytes(int mBluetoothChannelBytes) {
		mAdcData.bluetoothChannelBytes = mBluetoothChannelBytes;
	}

}// AcquisitionData

