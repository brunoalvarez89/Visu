package com.ufavaloro.android.visu.processing.timeoperations;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

// IIR 3dB, Fcut ~ 5 Hz
public class HighPass extends ProcessingOperation {

	private double y_prev;
	
	public HighPass(OperationType operationType, double fs, int samplesPerPackage, int operationOrder,
			Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
	}

	private void highPass() {
		int index = mProcessingBuffer.getProcessingIndex();

		mOperationResult = y_prev
							- (1/32) * mProcessingBuffer.getProcessingBufferSample(index)
							+ mProcessingBuffer.getProcessingBufferSample(index-16)
							- mProcessingBuffer.getProcessingBufferSample(index-17)
							+ (1/32) * mProcessingBuffer.getProcessingBufferSample(index-32);
	
		y_prev = mOperationResult;
							
	}
	
	@Override
	public void operate() {
		highPass();
	}
}
