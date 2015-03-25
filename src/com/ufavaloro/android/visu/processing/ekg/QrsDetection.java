package com.ufavaloro.android.visu.processing.ekg;

import android.os.Handler;
import android.util.Log;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class QrsDetection extends ProcessingOperation {

<<<<<<< HEAD
	protected int mCardiacFrecuency;
	protected int mTotalBeats;
	protected int mCurrentQrsIndex;
	protected int mPreviousQrsIndex;
=======
	private double mCardiacFrecuency;
	private double mBPM;
	private double mPeakToPeakSamples;
	private double mPeakToPeakTime;
	private int mCurrentQrsIndex;
	private int mPreviousQrsIndex;
>>>>>>> 5a418c789175de20bbacbe828f5ef9f57c600d46
	
	public QrsDetection(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	public void estimateQrs() {}
	
	protected void estimateCardiacFrecuency() {
<<<<<<< HEAD
		mTotalBeats++;
		mCurrentQrsIndex = mProcessingBuffer.getProcessingIndex();
		mCardiacFrecuency = (int) ((mTotalBeats / (mCurrentQrsIndex - mPreviousQrsIndex)*mTs))*1000;
		mPreviousQrsIndex = mCurrentQrsIndex;
		if(mCurrentQrsIndex == mProcessingBuffer.size() - 1) {
			mTotalBeats = 0;
		}
		Log.d("","FC: " + mCardiacFrecuency);
	}

=======
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
			Log.d("", String.valueOf(mPeakToPeakTime));
			mCardiacFrecuency = 1 / (60 * mPeakToPeakSamples * mTs);
			mBPM = mCardiacFrecuency;
			
			mPreviousQrsIndex = mCurrentQrsIndex;			
		}
	}
	
>>>>>>> 5a418c789175de20bbacbe828f5ef9f57c600d46
	@Override
	public void operate() {
		estimateQrs();
		estimateCardiacFrecuency();
	}
<<<<<<< HEAD
=======
	
	public double getBpm() {
		return mBPM;
	}
>>>>>>> 5a418c789175de20bbacbe828f5ef9f57c600d46
}
