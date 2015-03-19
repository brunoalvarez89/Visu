package com.ufavaloro.android.visu.storage;

import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;

public class SamplesBuffer {
	
	// Buffer
	private short[] mSamplesBuffer;

	// Puntero de almacenamiento
	private int mStoringIndex;
	
	// Tiempo de guardado en segundos
	private double mSaveTime = 5;
	
	
/*****************************************************************************************
* Métodos principales								      						         *
*****************************************************************************************/	
	// Constructors
	public SamplesBuffer(AcquisitionData acquisitionData, String units) {
		double fs = acquisitionData.getFs();
		double ts = 1 / fs;
		int samplesPerPackage = acquisitionData.getSamplesPerPackage();
		
		int i = 0;
		while(i*samplesPerPackage*ts < mSaveTime) i++;
		
		mSamplesBuffer = new short[i * samplesPerPackage];
	}
	
	public SamplesBuffer(int bufferSize) {
		mSamplesBuffer = new short[bufferSize];
	}
	
	public SamplesBuffer() {}

	public void writeSamples(short[] x) {
		// Almaceno
		for(int i=0; i<x.length; i++) {
			
			mSamplesBuffer[mStoringIndex] = x[i];
			
			// Incremento índices
			mStoringIndex++;
			
			// Si llego al máximo, pongo índices en cero
			if(mStoringIndex == mSamplesBuffer.length) mStoringIndex = 0;
		}
	}
	
	public void writeSample(short x) {
		mSamplesBuffer[mStoringIndex] = x;
		
		// Incremento índices
		mStoringIndex++;
		
		// Si llego al máximo, pongo índices en cero
		if(mStoringIndex == mSamplesBuffer.length) mStoringIndex = 0;
	}

/*****************************************************************************************
* Getters											      						         *
*****************************************************************************************/
	public int getStoringIndex() {
		return mStoringIndex;
	}
	
	public int getSize() {
		return mSamplesBuffer.length;
	}
	
	public short[] getBuffer() {
		return mSamplesBuffer;
	}

	public void createSamplesBuffer(ArrayList<Short> samplesBuffer) {
		mSamplesBuffer = new short[samplesBuffer.size()];
		for(int i = 0; i < samplesBuffer.size(); i++) {
				mSamplesBuffer[i] = samplesBuffer.get(i);
		}
	}

}

