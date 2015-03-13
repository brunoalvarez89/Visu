package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;
import android.util.Log;

public class FirstOrderDerivative extends ProcessingOperation {

	public FirstOrderDerivative(OperationType operationType, double fs,
			int samplesPerPackage, Handler processingInterfaceHandler,
			int channel) {
		super(operationType, fs, samplesPerPackage, processingInterfaceHandler, channel);
	}

	public void firstOrderDerivative() {
		int index = mProcessingBuffer.getProcessingIndex();
		
		// Avoid first sample (reduces noise)
		if(index == 0) return;
		
		int y2 = mProcessingBuffer.getProcessingBufferSample(index);
		//Log.d("", "y2: " + String.valueOf(y2));
		
		int y1 = mProcessingBuffer.getProcessingBufferSample(index-1);
		//Log.d("", "y1: " + String.valueOf(y1));

		mOperationResult = y2 - y1;
	}
	
	@Override
	public void operate() {
		firstOrderDerivative();
	}
}
