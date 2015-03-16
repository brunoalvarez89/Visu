package com.ufavaloro.android.visu.processing.timeoperations;

import android.os.Handler;
import android.util.Log;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

// IIR 3db, Fcut ~ 11 Hz
public class LowPass extends ProcessingOperation {

	private double y_1;
	private double y_2;
	
	public LowPass(OperationType operationType, double fs, int samplesPerPackage, int operationOrder,
			Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
	}

	private void lowPass() {
		int index = mProcessingBuffer.getProcessingIndex();
		
		double r1 = 2 * y_1;
		double r2 = y_2;
		double r3 = (double) (mProcessingBuffer.getProcessingBufferSample(index));
		double r4 = 2 * (double) (mProcessingBuffer.getProcessingBufferSample(index-6));
		double r5 = (double)mProcessingBuffer.getProcessingBufferSample(index-12);
		
		// r1 - r2 +
		mOperationResult = r1 - r2 + r3 - r4 + r5;
	
		y_2 = y_1;
		Log.d("", "-");
		Log.d("", "Actual Sample: " + mProcessingBuffer.getProcessingBufferSample(index));
		Log.d("", "y_2:" + y_2);
		y_1 = mOperationResult;
		Log.d("", "y_1:" + y_1);
	}
	
	@Override
	public void operate() {
		lowPass();
	}
		
}
