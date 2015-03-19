package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;
import android.util.Log;

public class SelfMultiply extends ProcessingOperation {

	public SelfMultiply(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
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
