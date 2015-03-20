package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;
import android.util.Log;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

	protected int mCardiacFrecuency;
	protected int mTotalBeats;
	protected int mCurrentQrsIndex;
	protected int mPreviousQrsIndex;
	
	public QrsDetection(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	protected void estimateQrs() {}
	
	protected void estimateCardiacFrecuency() {
		mTotalBeats++;
		mCurrentQrsIndex = mProcessingBuffer.getProcessingIndex();
		mCardiacFrecuency = (int) ((mTotalBeats / (mCurrentQrsIndex - mPreviousQrsIndex)*mTs))*1000;
		mPreviousQrsIndex = mCurrentQrsIndex;
		if(mCurrentQrsIndex == mProcessingBuffer.size() - 1) {
			mTotalBeats = 0;
		}
		Log.d("","FC: " + mCardiacFrecuency);
	}

	@Override
	public void operate() {
		estimateQrs();
		estimateCardiacFrecuency();
	}
}
