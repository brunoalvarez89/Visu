package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;

// First Derivative Slope QRS Detection
// Input must be Derivative Operation

public class FirstDerivativeSlope extends QrsDetection {
 	
	public FirstDerivativeSlope(OperationType operationType, double fs, int samplesPerPackage
			,int operationOrder, Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void estimateQrs() {
		int index = mProcessingBuffer.getProcessingIndex();
		double x2 = mProcessingBuffer.getProcessingBufferSample(index);
		double x1 = mProcessingBuffer.getProcessingBufferSample(index-1);
		
		if(x2 < mMeanValue && x1 > mMeanValue) { 
			mOperationResult = 1;	
		} else {
			mOperationResult = 0;
		}
	}
	
	@Override
	public void operate() {
		estimateQrs();
	}
}
