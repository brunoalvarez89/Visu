package com.ufavaloro.android.visu.processing.timeoperations;

import android.os.Handler;
import android.util.Log;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

// IIR 3db, Fcut ~ 11 Hz

public class LowPass extends ProcessingOperation {

	private double correction = Math.pow(2, 12)/2;
	private int mWindowSize = (int) (0.15 / (mTs));

	public LowPass(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	private void lowPass() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();

		mOperationResult = 0;
		
		for(int i = mProcessingIndex; i > mProcessingIndex - mWindowSize; i--) {
			mOperationResult = mOperationResult 
								+ Math.pow(mProcessingBuffer.getProcessingBufferSample(i) - correction + 1, 2);
		}
	}
	
	@Override
	public void operate() {
		lowPass();
	}
		
}
