package com.ufavaloro.android.visu.processing;

public class ProcessingOperation {
	
	protected short[] mSamples;
	protected boolean mProcessing;
	protected OperationType mOperationType;
	protected int mChannel;

	public int[] operate() {
		return null;
	}

	public ProcessingOperation(short[] samples, OperationType operationType, int channel) {
		mSamples = samples;
		mOperationType = operationType;
		mChannel = channel;
	}

	public ProcessingOperation() {
		mOperationType = null;
		mChannel = -1;
	}

	public void setSamples(short[] samples) {
		mSamples = samples;
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
