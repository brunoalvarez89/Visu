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

	protected int mOperationResult;
	protected float mCorrectedOperationResult;
	
	protected int[] mMinValues;
	protected int mMeanMinValue;
	protected int[] mMaxValues;
	protected int mMeanMaxValue;
	
	public ProcessingOperation(OperationType operationType, double fs, int samplesPerPackage
			   , Handler processingInterfaceHandler, int channel) {
	mOperationType = operationType;
	mChannel = channel;
	mProcessingInterfaceHandler = processingInterfaceHandler;
	mOperationResult = 0;
	
	mMinValues = new int[2];
	mMeanMinValue = 0;
	
	mMeanMaxValue = 0;
	mMaxValues = new int[2];
	
	mProcessingBuffer = new ProcessingBuffer(samplesPerPackage*100);
	}
	
	public synchronized void nextOperation() {
		if(getRawSample()) { 
			operate();
			calculateAverages();
			correctSample();
			returnSample();
			increaseProcessingIndex();
		}
	}
	
	public void operate() {}

	public void increaseProcessingIndex() {
		mProcessingBuffer.increaseProcessingIndex();
		if(mProcessingBuffer.getProcessingIndex() == 0) {
			mMeanMaxValue = 0;
			mMeanMinValue = 0;
		}
	}
	
	public void returnSample() {
		if(mOperationType == OperationType.TIME_FIRST_ORDER_DERIVATIVE) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_FIRST_ORDER_DERIVATIVE.getValue()
					  , mCorrectedOperationResult).sendToTarget();
		}
		if(mOperationType == OperationType.TIME_SQUARING) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_SQUARING.getValue()
					  , mCorrectedOperationResult).sendToTarget();
		}
	}
	
	public void correctSample() {		
		mOperationResult = mOperationResult + Math.abs(mMinValues[0]);
		if((mMaxValues[0] + Math.abs(mMinValues[0])) != 0) {
			mCorrectedOperationResult = (float) mOperationResult / (mMaxValues[0] + Math.abs(mMinValues[0]));
		}
		Log.d("", "Corrected Op. Result: " + String.valueOf(mOperationResult));
	}
	
	public void calculateAverages() {		
		
		if(mOperationResult < mMinValues[0]) {
			mMinValues[1] = mMinValues[0];
			mMinValues[0] = mOperationResult;
			
			if(mMinValues[0] != 0 && mMinValues[1] != 0) {
				int average = (mMinValues[0] + mMinValues[1])/2;
				mMinValues[0] = average;
			}
			
		} else if(mOperationResult <= mMinValues[1]){
			mMinValues[1] = mOperationResult;
			
			if(mMinValues[0] != 0 && mMinValues[1] != 0) {
				int average = (mMinValues[0] + mMinValues[1])/2;
				mMinValues[0] = average;
			}
			
		}
		Log.d("", "Min: " + String.valueOf(mMinValues[0]));
		
		if(mOperationResult >= mMaxValues[0]) {
			mMaxValues[1] = mMaxValues[0];
			mMaxValues[0] = mOperationResult;
			
			if(mMaxValues[0] != 0 && mMaxValues[1] != 0) {
				int average = (mMaxValues[0] + mMaxValues[1])/2;
				mMaxValues[0] = average;
			}
			
		} else if(mOperationResult >= mMaxValues[1]){
			mMaxValues[1] = mOperationResult;
			
			if(mMaxValues[0] != 0 && mMaxValues[1] != 0) {
				int average = (mMaxValues[0] + mMaxValues[1])/2;
				mMaxValues[0] = average;
			}
				
		}
		Log.d("", "Max: " + String.valueOf(mMaxValues[0]));
	
	}
	
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
