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
* Métodos principales								      						         *
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
	
	// Método para almacenar muestras
	public void storeSamples(short[] x) {
		
		// Almaceno
		for(int i=0; i<x.length; i++) {
			
			mSamplesBuffer[mStoringIndex] = x[i];
			
			// Incremento índices
			mStoringIndex++;
			
			// Si llego al máximo, pongo índices en cero
			if(mStoringIndex == mSamplesBuffer.length) mStoringIndex = 0;
		
		}
	
	}
	
	// Método para almacenar una única muestra
	public void storeSample(short sample) {
		//Almaceno
		mSamplesBuffer[mStoringIndex] = sample;
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

}//StoringBuffer

