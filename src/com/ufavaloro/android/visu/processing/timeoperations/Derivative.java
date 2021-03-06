package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;

public class Derivative extends ProcessingOperation {
	
	public Derivative(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	public void derivative() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();
		
		int x2 = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex);
		//Log.d("", "y2: " + String.valueOf(y2));
		
		int x1 = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex-2);
		//Log.d("", "y1: " + String.valueOf(y1));

		mOperationResult = (x2 - x1)/2;
	}
	
	@Override
	public void operate() {
		derivative();
	}
}
