package com.ufavaloro.android.visu.draw.channel;

import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.data.AcquisitionData;
import com.ufavaloro.android.visu.storage.data.StudyData;

public class DrawBuffer {
	
	// Buffer
	private short[] mBuffer;
	
	// Largo total del Buffer
	private int mSize;
	
	// Cantidad de "p�ginas" del buffer
	private int mTotalPages;
	
	// Puntero de almacenamiento
	private int mStoringIndex;
	
	// Puntero de graficaci�n
	private int mGraphingIndex;
	
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
		mBuffer = new short[mSize];
		mBits = bits;
		setZero();
	}

	// Constructor para Offline Draw Buffer
	public DrawBuffer(int mAdcChannelNumber, StudyData studyData, int totalPages) {
		mSize = studyData.getSamplesBuffer().getSize();
		mTotalPages = totalPages;
		mBuffer = new short[mSize];
		mBits = studyData.getAcquisitionData().getBits();
		mBuffer = studyData.getSamplesBuffer().getBuffer();	
	}

	// M�todo para almacenar muestras
	public void storeSamples(short[] x) {
		
		// Almaceno
		for(int i=0; i<x.length; i++) {
			
			this.mBuffer[mStoringIndex] = x[i];
			
			// Incremento �ndices
			mStoringIndex++;
			mGraphingIndex++;
			
			// Si llego al m�ximo, pongo �ndices en cero
			if(mStoringIndex == mSize) mStoringIndex = 0;
			if(mGraphingIndex == mSize) mGraphingIndex = 0;
		
		}
	
	}
	
	// M�todo para recibir muestras
	public int getSample(int index) {
		
		// Resto offset de muestras
		index = index + (mGraphingIndex - mSize/mTotalPages);
		
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
		return mBuffer[newIndex];
	}
	
	// M�todo para incrementar el �ndice de graficaci�n
	public void increaseGraphingIndex(int inc) {
		
		// Incremento
		mGraphingIndex = mGraphingIndex + inc;
		
		// Si me pas� del largo total
		if(mGraphingIndex > mSize - 1) { 
			
			// Le resto el largo total al �ndice
			mGraphingIndex = mGraphingIndex - mSize;
		
		}
	
	}
	
	// M�todo para decrementar el �ndice de graficaci�n
	public void decreaseGraphingIndex(int dec) {
		
		// Incremento
		mGraphingIndex = mGraphingIndex - dec;
		
		// Si el �ndice es negativo
		if(mGraphingIndex < 0) { 
			
			// Le resto el �ndice al largo total
			mGraphingIndex = mGraphingIndex + mSize;
		
		}
	
	}

	// Seteo el valor nulo
	public void setZero() {
		short zero = (short) (Math.pow(2, mBits) / 2);
		for(int i=0; i<mSize; i++) { 
			mBuffer[i] = zero;
		}
		
	}

	// Setter de graphing index, para resetear
	public void setGraphingIndex(int graphingIndex) {
		mGraphingIndex = graphingIndex;
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
		return mGraphingIndex;
	}
	
	public short[] getBuffer() {
		return mBuffer;
	}
	
	public float getHorizontalZoom() {
		return mHorizontalZoom;
	}
	
	public float getVerticalZoom() {
		return mVerticalZoom;
	}

}// DrawBuffer

