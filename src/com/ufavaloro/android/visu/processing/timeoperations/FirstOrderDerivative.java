package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;

public class FirstOrderDerivative extends ProcessingOperation {

	public FirstOrderDerivative(OperationType operationType, double fs,
			int samplesPerPackage, Handler processingInterfaceHandler,
			int channel) {
		super(operationType, fs, samplesPerPackage, processingInterfaceHandler, channel);
	}

	public void firstOrderDerivative() {
		int index = mProcessingBuffer.getProcessingIndex();
		int y2 = mProcessingBuffer.getProcessingBufferSample(index);
		int y1 = mProcessingBuffer.getProcessingBufferSample(index-1);
		 
		int derivative = Math.abs(y2 - y1);
						
		mProcessingInterfaceHandler.obtainMessage(OperationType.TIME_FIRST_ORDER_DERIVATIVE.getValue(), derivative*derivative).sendToTarget();
	}
	
	@Override
	public void operate() {
		firstOrderDerivative();
	}
}
