package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;

// First Derivative Slope QRS Detection
// Input must be Derivative Operation

public class FirstDerivativeSlope extends QrsDetection {
 	
	double mX2;
	double mX1;
	
	public FirstDerivativeSlope(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void estimateQrs() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();
		mX2 = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex);
		mX1 = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex-1);
		
		if(mX2 < mMeanValue && mX1 > mMeanValue) { 
			mOperationResult = 1;	
		} else {
			mOperationResult = 0;
		}
	}
	
}
