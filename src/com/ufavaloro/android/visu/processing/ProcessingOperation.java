package com.ufavaloro.android.visu.processing;

import android.os.Handler;
import android.util.Log;

public class ProcessingOperation {
	
	protected OperationType mOperationType;
	protected int mOperationChannel;
	protected int mOperationIndex;
	
	protected ProcessingBuffer mProcessingBuffer;
	protected boolean mIsProcessing;

	protected Handler mProcessingInterfaceHandler;

	protected double mOperationResult;
	protected double mCorrectedOperationResult;
	
	protected double[] mMinValues;
	protected double[] mMaxValues;
	protected double mMeanValue;
		
	private boolean mLog = false;
	
	public ProcessingOperation(OperationType operationType, int operationChannel, int operationIndex
								, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
	mOperationType = operationType;
	mOperationChannel = operationChannel;
	mOperationIndex = operationIndex;
	mProcessingInterfaceHandler = processingInterfaceHandler;
	mOperationResult = 0;
	
	mMinValues = new double[2];
	mMaxValues = new double[2];
	
	mProcessingBuffer = new ProcessingBuffer(samplesPerPackage*100);
	
	}
	
	public void nextOperation() {
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
			//mMinValues = new double[2];
			//mMaxValues = new double[2];
		}
	}
	
	public void returnSample() {
		
		if(mOperationType == OperationType.TIME_DERIVATIVE) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_DERIVATIVE.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.TIME_SELF_MULTIPLY) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_SELF_MULTIPLY.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.TIME_MAF) {
	 		 Log.d("", "MAF: " + mOperationResult);
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_MAF.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.TIME_LOWPASS) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_LOWPASS.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.TIME_HIGHPASS) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_HIGHPASS.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.FREQUENCY_FFT) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.FREQUENCY_FFT.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.EKG_QRS_ADAPTIVE_THRESHOLD) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.EKG_QRS_ADAPTIVE_THRESHOLD.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
		
		if(mOperationType == OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE) {
			mProcessingInterfaceHandler.obtainMessage(OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE.getValue()
														, mOperationChannel
														, mOperationIndex
														, mCorrectedOperationResult).sendToTarget();
		}
	}

	public void correctSample() {		
		mOperationResult = mOperationResult + Math.abs(mMinValues[0]);
		if((mMaxValues[0] + Math.abs(mMinValues[0])) != 0) {
			mCorrectedOperationResult = mOperationResult / (mMaxValues[0] + Math.abs(mMinValues[0]));
		}
		
		if(mLog) Log.d("", "Min: " + String.valueOf(mMinValues[0]));
		if(mLog) Log.d("", "Max: " + String.valueOf(mMaxValues[0]));
		if(mLog) Log.d("", "Op. Result: " + String.valueOf(mOperationResult));
		if(mLog) Log.d("", "Corrected Op. Result: " + String.valueOf(mCorrectedOperationResult));
	}
	
	public void calculateAverages() {		
		
		if(mOperationResult < mMinValues[0]) {
			mMinValues[1] = mMinValues[0];
			mMinValues[0] = mOperationResult;
			
			if(mMinValues[0] != 0 && mMinValues[1] != 0) {
				double  average = (mMinValues[0] + mMinValues[1])/2;
				mMinValues[0] = average;
			}
			
		} else if(mOperationResult <= mMinValues[1]){
			mMinValues[1] = mOperationResult;
			
			if(mMinValues[0] != 0 && mMinValues[1] != 0) {
				double  average = (mMinValues[0] + mMinValues[1])/2;
				mMinValues[0] = average;
			}
			
		}
		
		if(mOperationResult >= mMaxValues[0]) {
			mMaxValues[1] = mMaxValues[0];
			mMaxValues[0] = mOperationResult;
			
			if(mMaxValues[0] != 0 && mMaxValues[1] != 0) {
				double average = (mMaxValues[0] + mMaxValues[1])/2;
				mMaxValues[0] = average;
			}
			
		} else if(mOperationResult >= mMaxValues[1]){
			mMaxValues[1] = mOperationResult;
			
			if(mMaxValues[0] != 0 && mMaxValues[1] != 0) {
				double average = (mMaxValues[0] + mMaxValues[1])/2;
				mMaxValues[0] = average;
			}
				
		}
	
		mMeanValue = (mMaxValues[0] + Math.abs(mMinValues[0])) / 2;
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
		return mOperationChannel;
	}

	public void setChannel(int channel) {
		mOperationChannel = channel;
	}

	public ProcessingBuffer getProcessingBuffer() {
		return mProcessingBuffer;
	}

	public double getMaxValue() {
		return mMaxValues[0];
	}
	
	public double getMinValue() {
		return mMinValues[0];
	}

	public int getOrder() {
		return mOperationIndex;
	}
}