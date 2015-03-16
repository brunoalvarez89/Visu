package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;

public class Derivative extends ProcessingOperation {
	
	public Derivative(OperationType operationType, double fs, int samplesPerPackage, int operationOrder,
			Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
	}

	public void derivative() {
		int index = mProcessingBuffer.getProcessingIndex();
		
		int x2 = mProcessingBuffer.getProcessingBufferSample(index);
		//Log.d("", "y2: " + String.valueOf(y2));
		
		int x1 = mProcessingBuffer.getProcessingBufferSample(index-2);
		//Log.d("", "y1: " + String.valueOf(y1));

		mOperationResult = (x2 - x1)/2;
	}
	
	@Override
	public void operate() {
		derivative();
	}
}
