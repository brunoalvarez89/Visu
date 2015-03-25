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
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();
		
		mOperationResult = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex)
							* mProcessingBuffer.getProcessingBufferSample(mProcessingIndex);;
	}
	
	@Override
	public void operate() {
		selfMultiply();
	}
}
