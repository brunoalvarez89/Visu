package com.ufavaloro.android.visu.draw.channel;

import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

public class DrawBuffer {
	
	// Buffer
	private short[] mSamplesBuffer;
	
	// Largo total del Buffer
	private int mSize;
	
	// Cantidad de "p�ginas" del buffer
	private int mTotalPages;
	
	// Puntero de almacenamiento
	private int mStoringIndex;
	
	// Puntero de graficaci�n
	private int mDrawingIndex;
	
	private float mHorizontalZoom;
	private float mVerticalZoom;
	
	private int mBits;
	
/*****************************************************************************************
* M�todos principales								      						         *
*****************************************************************************************/	

	// Constructor para Online Draw Buffer
	public DrawBuffer(int channelNumber, int samplesPerPage, int totalPages, int bits) {
		mSize = samplesPerPage*totalPages;
		mTotalPages = totalPages;
		mSamplesBuffer = new short[mSize];
		mBits = bits;
		setZero();
		mHorizontalZoom = 1;
		mVerticalZoom = 1;
	}

	// Constructor para Offline Draw Buffer
	public DrawBuffer(int mAdcChannelNumber, StudyData studyData, int totalPages) {
		mSize = studyData.getSamplesBuffer().getSize();
		mTotalPages = totalPages;
		mSamplesBuffer = new short[mSize];
		mBits = studyData.getAcquisitionData().getBits();
		mSamplesBuffer = studyData.getSamplesBuffer().getBuffer();	
		mHorizontalZoom = 1;
		mVerticalZoom = 1;
	}

	// M�todo para almacenar muestras
	public void writeSamples(short[] samples) {
		// Almaceno
		for(int i=0; i<samples.length; i++) {
			
			this.mSamplesBuffer[mStoringIndex] = samples[i];
			
			// Incremento �ndices
			mStoringIndex++;
			mDrawingIndex++;
			
			// Si llego al m�ximo, pongo �ndices en cero
			if(mStoringIndex == mSize) mStoringIndex = 0;
			if(mDrawingIndex == mSize) mDrawingIndex = 0;
		
		}
	}
	
	public void storeSample(short sample) {
		// Almaceno
		this.mSamplesBuffer[mStoringIndex] = sample;
		
		// Incremento �ndices
		mStoringIndex++;
		mDrawingIndex++;
		
		// Si llego al m�ximo, pongo �ndices en cero
		if(mStoringIndex == mSize) mStoringIndex = 0;
		if(mDrawingIndex == mSize) mDrawingIndex = 0;
	}
	// M�todo para recibir muestras
	public int getSample(int index) {
		
		// Resto offset de muestras
		index = index + (mDrawingIndex - mSize/mTotalPages);
		
		// Inicializo un �ndice dummy
		int newIndex = 0;
		
		// Si el �ndice es negativo
		if(index < 0) {
			// Le sumo el largo del Buffer
			newIndex = index + mSize;
		}
		
		// Si el �ndice est� en el rango permitido
		if(index <= mSize - 1 && index >= 0) { 
			// No hago nada
			newIndex = index;
		}
		
		// Si el �ndice es mayor al largo total
		if(index > mSize - 1) { 
			// Le resto el �ndice al largo total
			newIndex = index - mSize;
		}
		
		// Devuelvo muestra
		return mSamplesBuffer[newIndex];
	}
	
	// M�todo para incrementar el �ndice de graficaci�n
	public void increaseGraphingIndex(int inc) {
		
		// Incremento
		mDrawingIndex = mDrawingIndex + inc;
		
		// Si me pas� del largo total
		if(mDrawingIndex > mSize - 1) { 
			
			// Le resto el largo total al �ndice
			mDrawingIndex = mDrawingIndex - mSize;
		
		}
	
	}
	
	// M�todo para decrementar el �ndice de graficaci�n
	public void decreaseGraphingIndex(int dec) {
		
		// Incremento
		mDrawingIndex = mDrawingIndex - dec;
		
		// Si el �ndice es negativo
		if(mDrawingIndex < 0) { 
			
			// Le resto el �ndice al largo total
			mDrawingIndex = mDrawingIndex + mSize;
		
		}
	
	}

	// Seteo el valor nulo
	public void setZero() {
		short zero = (short) (Math.pow(2, mBits) / 2);
		for(int i=0; i<mSize; i++) { 
			mSamplesBuffer[i] = zero;
		}
		
	}

	// Setter de graphing index, para resetear
	public void setGraphingIndex(int graphingIndex) {
		mDrawingIndex = graphingIndex;
	}

	public void setHorizontalZoom(float horizontalZoom) {
		mHorizontalZoom = horizontalZoom;
	}

	public void setVerticalZoom(float verticalZoom) {
		mVerticalZoom = verticalZoom;
	}
	
/*****************************************************************************************
* Getters											      						         *
*****************************************************************************************/
	public int getStoringIndex() {
		return mStoringIndex;
	}
	
	public int getGraphingIndex() {
		return mDrawingIndex;
	}
	
	public short[] getBuffer() {
		return mSamplesBuffer;
	}
	
	public float getHorizontalZoom() {
		return mHorizontalZoom;
	}
	
	public float getVerticalZoom() {
		return mVerticalZoom;
	}


}// DrawBuffer

