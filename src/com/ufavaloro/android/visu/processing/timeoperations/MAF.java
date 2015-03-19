package com.ufavaloro.android.visu.processing.timeoperations;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class MAF extends  ProcessingOperation {
	
	private int M = 5;
	private double mConstant = 1/(double)M;
	
	public MAF(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	private void calculateHighPassMAF() {
		int index = mProcessingBuffer.getProcessingIndex();
		
        double x1_sum = 0;
        for(int j = index; j > index - M; j--) {
        	double sample = mProcessingBuffer.getProcessingBufferSample(j);
        	x1_sum = x1_sum + sample;
        }
        double x1 = mConstant * x1_sum;
        
        double x2 = mProcessingBuffer.getProcessingBufferSample(index-((M+1)/2));

       	mOperationResult = x2-x1;       
	}
	
	@Override
	public void operate() {
		calculateHighPassMAF();
	}
}
