package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

	private int mCardiacFrecuency;
	
	public QrsDetection(OperationType operationType, double fs, int samplesPerPackage, int operationOrder,
			Handler processingInterfaceHandler, int channel) {
		super(operationType, fs, samplesPerPackage, operationOrder, processingInterfaceHandler, channel);
	}

	protected void estimateQrs() {}
	
	protected void estimateCardiacFrecuency() {}
}
