package com.ufavaloro.android.visu.processing.timeoperations;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

import android.os.Handler;

public class MovingAverage extends ProcessingOperation {
	
	// QRS time ~ 80-120 ms, utilizo 100
	private int mWindowSize = (int) (0.1 / (mTs));
	//private int mWindowSize = 10;
	
	public MovingAverage(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	public void movingAverage() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();
		mOperationResult = 0;
		
		for(int i = mProcessingIndex; i > mProcessingIndex - mWindowSize; i--) {
			mOperationResult += (mProcessingBuffer.getProcessingBufferSample(i));
		}
		
		mOperationResult = mOperationResult / mWindowSize;
	}
	
	@Override
	public void operate() {
		movingAverage();
	}
}
