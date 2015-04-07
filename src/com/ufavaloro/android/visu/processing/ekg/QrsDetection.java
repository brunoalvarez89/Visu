package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;
import android.util.Log;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

	private double mCardiacFrecuency;
	private double mBPM;
	private double mPeakToPeakSamples;
	private double mPeakToPeakTime;
	private int mCurrentQrsIndex;
	private int mPreviousQrsIndex;
	
	public QrsDetection(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	public void estimateQrs() {}
	
	protected void estimateCardiacFrecuency() {
		if(mOperationResult == 1)  {
			mCurrentQrsIndex = mProcessingIndex;
			
			// Circular buffer overflowed
			if(mCurrentQrsIndex < mPreviousQrsIndex) {
				int delta = mProcessingBuffer.size() - mPreviousQrsIndex;
				mPeakToPeakSamples = Math.abs(delta + mCurrentQrsIndex);
			} else if(mCurrentQrsIndex > mPreviousQrsIndex)  {
				mPeakToPeakSamples = (mCurrentQrsIndex - mPreviousQrsIndex);
			} else if(mCurrentQrsIndex == mPreviousQrsIndex) {
				mPeakToPeakSamples = 1;
			}

			mPeakToPeakTime = mPeakToPeakSamples*mTs;
			//Log.d("", String.valueOf(mPeakToPeakTime));
			mCardiacFrecuency = 1 / (60 * mPeakToPeakSamples * mTs);
			mBPM = mCardiacFrecuency;
			
			mPreviousQrsIndex = mCurrentQrsIndex;			
		}
	}
	
	@Override
	public void operate() {
		estimateQrs();
		estimateCardiacFrecuency();
	}
	
	public double getBpm() {
		return mBPM;
	}
}
