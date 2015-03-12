package com.ufavaloro.android.visu.processing;

import android.os.Handler;

import com.ufavaloro.android.visu.storage.SamplesBuffer;

public class ProcessingOperation {
	
	protected ProcessingBuffer mProcessingBuffer;
	protected double mWaitTime = 1.5; // x seg
	protected boolean mIsProcessing;
	protected OperationType mOperationType;
	protected int mChannel;
	protected Handler mProcessingInterfaceHandler;

	public void nextOperation() {
		mIsProcessing = true;
		
		getRawSample();
		operate();
		increaseProcessingIndex();
		
		mIsProcessing = false;
	}
	
	public void operate() {
	}

	public void increaseProcessingIndex() {
		mProcessingBuffer.increaseProcessingIndex();
	}
	
	public void getRawSample() {
		int processingIndex = mProcessingBuffer.getProcessingIndex();
		mProcessingBuffer.getRawSample(processingIndex);
	}
	
	public ProcessingOperation(OperationType operationType, double fs, int samplesPerPackage
							   , Handler processingInterfaceHandler, int channel) {
		mOperationType = operationType;
		mChannel = channel;
		mProcessingInterfaceHandler = processingInterfaceHandler;
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

	public void setOperationType(OperationType operationType) {
		mOperationType = operationType;
	}
	
	public ProcessingBuffer getProcessingBuffer() {
		return mProcessingBuffer;
	}
}
