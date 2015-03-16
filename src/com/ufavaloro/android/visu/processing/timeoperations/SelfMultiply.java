package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;
import android.util.Log;

public class SelfMultiply extends ProcessingOperation {

	public SelfMultiply(OperationType operationType, double fs, int samplesPerPackage, int operationOrder,
			Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
	}

	public void selfMultiply() {
		int index = mProcessingBuffer.getProcessingIndex();
		
		int x = mProcessingBuffer.getProcessingBufferSample(index);

		mOperationResult = x*x;
	}
	
	@Override
	public void operate() {
		selfMultiply();
	}
}
