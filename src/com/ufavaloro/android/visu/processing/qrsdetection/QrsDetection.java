package com.ufavaloro.android.visu.processing.qrsdetection;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

	public QrsDetection(OperationType operationType, double fs,
			int samplesPerPackage, Handler processingInterfaceHandler,
			int channel) {
		super(operationType, fs, samplesPerPackage, processingInterfaceHandler, channel);
	}

}
