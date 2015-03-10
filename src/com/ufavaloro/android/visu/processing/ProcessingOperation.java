package com.ufavaloro.android.visu.processing;

import com.google.android.gms.common.data.DataBuffer;

public class ProcessingOperation {
	
	protected SamplesBuffer mProcessingBuffer;
	protected boolean mProcessing;
	protected OperationType mOperationType;
	protected int mChannel;

	public int[] operate() {
		return null;
	}

	public ProcessingOperation(short[] samples, OperationType operationType, int channel) {
		mProcessingBuffer = samples;
		mOperationType = operationType;
		mChannel = channel;
	}

	public ProcessingOperation() {
		mProcessingBuffer = new DataBuffer();
		mOperationType = null;
		mChannel = -1;
	}

	public void setSamples(short[] samples) {
		mProcessingBuffer = samples;
	}
	
	public boolean isProcessing() {
		return mProcessing;
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
}
