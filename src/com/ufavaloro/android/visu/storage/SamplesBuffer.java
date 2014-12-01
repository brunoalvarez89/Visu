/*****************************************************************************************
 * StorageBuffer.java																	 *
 * Buffer de almacenamiento.															 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage;

import com.ufavaloro.android.visu.storage.data.AcquisitionData;

public class SamplesBuffer {
	
	// Buffer
	private short[] mSamplesBuffer;

	// Puntero de almacenamiento
	private int mStoringIndex;
	
	// Tiempo de guardado en segundos
	private double mSaveTime = 5;
	
	
/*****************************************************************************************
* M�todos principales								      						         *
*****************************************************************************************/	
	// Constructor
	public SamplesBuffer(AcquisitionData acquisitionData, String units) {
		double fs = acquisitionData.getFs();
		double ts = 1 / fs;
		int samplesPerPackage = acquisitionData.getSamplesPerPackage();
		
		int i = 0;
		while(i*samplesPerPackage*ts < mSaveTime) i++;
		
		mSamplesBuffer = new short[i * samplesPerPackage];
	}
	
	// M�todo para almacenar muestras
	public void storeSamples(short[] x) {
		
		// Almaceno
		for(int i=0; i<x.length; i++) {
			
			mSamplesBuffer[mStoringIndex] = x[i];
			
			// Incremento �ndices
			mStoringIndex++;
			
			// Si llego al m�ximo, pongo �ndices en cero
			if(mStoringIndex == mSamplesBuffer.length) mStoringIndex = 0;
		
		}
	
	}
	
	// M�todo para almacenar una �nica muestra
	public void storeSample(short sample) {
		//Almaceno
		mSamplesBuffer[mStoringIndex] = sample;
		// Incremento �ndices
		mStoringIndex++;
		// Si llego al m�ximo, pongo �ndices en cero
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

}//StoringBuffer

