/*****************************************************************************************
 * StorageBuffer.java																	 *
 * Buffer de almacenamiento.															 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage;

import com.ufavaloro.android.visu.storage.data.AcquisitionData;
import com.ufavaloro.android.visu.storage.data.PatientData;
import com.ufavaloro.android.visu.storage.data.StorageData;

public class StorageBuffer {
	
	// Buffer
	private short[] mBuffer;
	
	// Largo total del Buffer
	private int mSize;
	
	// Puntero de almacenamiento
	private int mStoringIndex;
	
	// Tiempo de guardado en segundos
	private double mSaveTime = 5;
	
	public AcquisitionData acquisitionData;
	public PatientData patientData;
	public StorageData storageData;
		
	
/*****************************************************************************************
* Métodos principales								      						         *
*****************************************************************************************/	
	// Constructor
	public StorageBuffer(AcquisitionData acquisitionData, String units) {

		this.acquisitionData = acquisitionData;
		
		double fs = acquisitionData.getFs();
		double ts = 1 / fs;
		int samplesPerPackage = acquisitionData.getSamplesPerPackage();
		
		int i = 0;
		while(i*samplesPerPackage*ts < mSaveTime) i++;
		
		mSize = i * samplesPerPackage;
		mBuffer = new short[mSize];
		
		patientData = new PatientData();
		storageData = new StorageData();
	}
	
	// Método para almacenar muestras
	public void storeSamples(short[] x) {
		
		// Almaceno
		for(int i=0; i<x.length; i++) {
			
			mBuffer[mStoringIndex] = x[i];
			
			// Incremento índices
			mStoringIndex++;
			
			// Si llego al máximo, pongo índices en cero
			if(mStoringIndex == mSize) mStoringIndex = 0;
		
		}
	
	}

	
/*****************************************************************************************
* Getters											      						         *
*****************************************************************************************/
	public int getStoringIndex() {
		return mStoringIndex;
	}
	
	public int getSize() {
		return mSize;
	}
	
	public short[] getBuffer() {
		return mBuffer;
	}

}//StoringBuffer

