package com.ufavaloro.android.visu.processing.ekg;

import com.ufavaloro.android.visu.processing.OperationType;

import android.os.Handler;
	
// Moving Average based Filtering 
// http://cinc.org/archives/2003/pdf/585.pdf
// Input must be MAF(Low Pass) Operation

public class AdaptiveThreshold extends QrsDetection {
	
	public AdaptiveThreshold(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	private void qrs() {
	    //int[] QRS = new int[nsamp];
		int index = mProcessingBuffer.getProcessingIndex();	
	    double treshold = 0;
	 
	    for(int i = index; i < 200 + index; i++) {
	        if(mProcessingBuffer.getProcessingBufferSample(i) > treshold) {
	            treshold = mProcessingBuffer.getProcessingBufferSample(i);
	        }
	    }
	 
	    int frame = 250;
	    int window = 100;
	    for(int i = index; i < index + window; i+=frame) {
	        float max = 0;
      
	        for(int j = i; j < i+frame; j++) {
	            if(mProcessingBuffer.getProcessingBufferSample(j) > max) {
	            	max = mProcessingBuffer.getProcessingBufferSample(j);
	            }
	        }
	        
	        boolean added = false;
	        
	        for(int j = i; j<index; j++) {
	            if(mProcessingBuffer.getProcessingBufferSample(j) > treshold && !added) {
	                mOperationResult = 1;
	                added = true;
	                break;
	            }
	            else {
	                mOperationResult = 0;
	            }
	        }
	 
	        double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	        double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	 
	        treshold = alpha * gama * max + (1 - alpha) * treshold;
	 
	    }
	}

	@Override
	public void operate() {	
		qrs();
	}
	
}
