package com.ufavaloro.android.visu.processing;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.internal.mp;
import com.google.android.gms.internal.mr;
import com.ufavaloro.android.visu.storage.SamplesBuffer;

public class ProcessingOperation {
	
	protected ProcessingBuffer mProcessingBuffer;
	protected double mWaitTime = 1.5; // x seg
	protected boolean mIsProcessing;
	protected OperationType mOperationType;
	protected int mChannel;
	protected Handler mProcessingInterfaceHandler;

	public ProcessingOperation(OperationType operationType, double fs, int samplesPerPackage
			   , Handler processingInterfaceHandler, int channel) {
	mOperationType = operationType;
	mChannel = channel;
	mProcessingInterfaceHandler = processingInterfaceHandler;

	mProcessingBuffer = new ProcessingBuffer(samplesPerPackage*100);
	}
	
	public synchronized void nextOperation() {
		mIsProcessing = true;
		
		boolean result = getRawSample();
		if(result) operate();
		
		mIsProcessing = false;
	}
	
	public void operate() {}

	public boolean getRawSample() {
		int processingIndex = mProcessingBuffer.getProcessingIndex();
		
		int storingIndex = mProcessingBuffer.getStoringIndex();
		
		if(processingIndex == storingIndex) { 
			return false;
		} else {
			short sample = mProcessingBuffer.getRawSample(processingIndex);
			mProcessingBuffer.writeProcessingSample(sample);
			return true;
		}
		
		//return false;
	}
	
	
	public boolean isProcessing() {
		return mIsProcessing;
	}
	
	
	public OperationType getOperationType() {
		return mOperationType;
	}
	
	
	public int getChannel() {
		return mChannel;
	}

	
	public void setChannel(int channel) {
		mChannel = channel;
	}
	
	
	public ProcessingBuffer getProcessingBuffer() {
		return mProcessingBuffer;
	}
}
