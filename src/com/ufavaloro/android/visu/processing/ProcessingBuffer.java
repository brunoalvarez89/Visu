package com.ufavaloro.android.visu.processing;

public class ProcessingBuffer {
	
	private short[] mRawSamplesBuffer;
	private short[] mProcessingSamplesBuffer;
	private int mStoringIndex;
	private int mProcessingIndex;

	
	public ProcessingBuffer(int bufferSize) {
		mRawSamplesBuffer = new short[bufferSize];
		mProcessingSamplesBuffer = new short[bufferSize];
		mStoringIndex = 0;
		mProcessingIndex = 0;

	}
	
	public void writeRawSamples(short[] x) {
		// Almaceno
		for(int i=0; i<x.length; i++) {
			mRawSamplesBuffer[mStoringIndex] = x[i];
			
			// Incremento índices
			mStoringIndex++;
			
			// Si llego al máximo, pongo índices en cero
			if(mStoringIndex == mRawSamplesBuffer.length) { 
				mStoringIndex = 0;
			}
		}
	}
	
	public void writeRawSample(short sample) {
		mRawSamplesBuffer[mStoringIndex] = sample;
		
		// Incremento índices
		mStoringIndex++;
		
		// Si llego al máximo, pongo índices en cero
		if(mStoringIndex == mRawSamplesBuffer.length) { 
			mStoringIndex = 0;
		}
		
	}
	
	public void writeProcessingSample(short x) {
		mProcessingSamplesBuffer[mProcessingIndex] = x;
	}
	
	public void increaseProcessingIndex() {
		// Incremento índices
		mProcessingIndex++;
		
		// Si llego al máximo, pongo índices en cero
		if(mProcessingIndex == mProcessingSamplesBuffer.length) { 
			mProcessingIndex = 0;
		}
	}
	
	public int getStoringIndex() {
		return mStoringIndex;
	}
	
	public int getProcessingIndex() {
		return mProcessingIndex;
	}
	
	public int size() {
		return mRawSamplesBuffer.length;
	}
	
	public short[] getRawSamplesBuffer() {
		return mRawSamplesBuffer;
	}
	
	public short getRawSample(int index) {
		int length = mRawSamplesBuffer.length;
		// Inicializo un índice dummy
		int newIndex = 0;
		
		// Si el índice es negativo
		if(index < 0) {
			// Le sumo el largo del Buffer
			newIndex = index + length;
		}
		
		// Si el índice está en el rango permitido
		if(index <= length - 1 && index >= 0) { 
			// No hago nada
			newIndex = index;
		}
		
		// Si el índice es mayor al largo total
		if(index > length - 1) { 
			// Le resto el índice al largo total
			newIndex = index - length;
		}
		
		// Devuelvo muestra
		return mRawSamplesBuffer[newIndex];
	}

	public short getProcessingBufferSample(int index) {
		int length = mProcessingSamplesBuffer.length;
		// Inicializo un índice dummy
		int newIndex = 0;
		
		// Si el índice es negativo
		if(index < 0) {
			// Le sumo el largo del Buffer
			newIndex = index + length;
		}
		
		// Si el índice está en el rango permitido
		if(index <= length - 1 && index >= 0) { 
			// No hago nada
			newIndex = index;
		}
		
		// Si el índice es mayor al largo total
		if(index > length - 1) { 
			// Le resto el índice al largo total
			newIndex = index - length;
		}
		
		// Devuelvo muestra
		return mProcessingSamplesBuffer[newIndex];
	}

	public short[] getProcessingSamplesBuffer() {
		return mProcessingSamplesBuffer;
	}

	
}

