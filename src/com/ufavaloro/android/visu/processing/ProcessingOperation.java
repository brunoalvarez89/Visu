package com.ufavaloro.android.visu.processing;

import android.os.Handler;

import com.ufavaloro.android.visu.storage.SamplesBuffer;

public class ProcessingOperation {
	
	protected SamplesBuffer mProcessingBuffer;
	protected double mWaitTime = 1.5; // x seg
	protected boolean mIsProcessing;
	protected OperationType mOperationType;
	protected int mChannel;
	protected Handler mProcessingInterfaceHandler;

	public int[] operate() {
		return null;
	}

	public ProcessingOperation(OperationType operationType, double fs, int samplesPerPackage
							   , Handler processingInterfaceHandler, int channel) {
		mOperationType = operationType;
		mChannel = channel;
		mProcessingInterfaceHandler = processingInterfaceHandler;

		int i = 0;
		while(samplesPerPackage*i < 200) i++;
		mProcessingBuffer = new SamplesBuffer(samplesPerPackage*i);
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
	
	public SamplesBuffer getProcessingBuffer() {
		return mProcessingBuffer;
	}
}
