package com.ufavaloro.android.visu.processing.timeoperations;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

// IIR 3dB, Fcut ~ 5 Hz
public class HighPass extends ProcessingOperation {

	private double y_prev;
	
	public HighPass(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	private void highPass() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();

		mOperationResult = y_prev
							- (1/32) * mProcessingBuffer.getProcessingBufferSample(mProcessingIndex)
							+ mProcessingBuffer.getProcessingBufferSample(mProcessingIndex-16)
							- mProcessingBuffer.getProcessingBufferSample(mProcessingIndex-17)
							+ (1/32) * mProcessingBuffer.getProcessingBufferSample(mProcessingIndex-32);
	
		y_prev = mOperationResult;
							
	}
	
	@Override
	public void operate() {
		highPass();
	}
}
