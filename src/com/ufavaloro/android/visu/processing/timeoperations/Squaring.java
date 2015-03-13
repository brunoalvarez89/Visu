package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;
import android.util.Log;

public class Squaring extends ProcessingOperation {

	public Squaring(OperationType operationType, double fs,
			int samplesPerPackage, Handler processingInterfaceHandler,
			int channel) {
		super(operationType, fs, samplesPerPackage, processingInterfaceHandler, channel);
	}

	public void squaring() {
		int index = mProcessingBuffer.getProcessingIndex();
		
		int y = mProcessingBuffer.getProcessingBufferSample(index);

		mOperationResult = (int) Math.pow(y, 2);
	}
	
	@Override
	public void operate() {
		squaring();
	}
}
