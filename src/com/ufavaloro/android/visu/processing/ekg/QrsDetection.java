package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

	private int mCardiacFrecuency;
	
	public QrsDetection(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	protected void estimateQrs() {}
	
	protected void estimateCardiacFrecuency() {}
}
