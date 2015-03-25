package com.ufavaloro.android.visu.processing.ekg;

import com.ufavaloro.android.visu.processing.OperationType;

import android.os.Handler;
import android.util.Log;
	
// Moving Average based Filtering 
// http://cinc.org/archives/2003/pdf/585.pdf
// Input must be a MAF filtered and then Low Pass Filtered sample

public class AdaptiveThreshold extends QrsDetection {
	
	double mGamma;
	double mAlpha;
	double mLocalPeak;
	double mThreshold;
	double mNewThreshold;
	boolean mAdded;
	int mStep;
	int mWindow;
	int mQrsCandidateIndex;
	// QRS time ~ 80-120 ms
	private int mRefractoryWindowSize = (int) (0.2 / (mTs));
	private int mRefractoryCounter;
	
	public AdaptiveThreshold(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
	}

	@Override
	public void estimateQrs() {
		mProcessingIndex = mProcessingBuffer.getProcessingIndex();
		
		/*
		if(mRefractoryCounter > 0) {
			mRefractoryCounter--;
			mOperationResult = 0;
			return;
		}
		*/
		// Searh for local peak
		if(mProcessingBuffer.getProcessingBufferSample(mProcessingIndex) > mThreshold) {
			mLocalPeak = mProcessingBuffer.getProcessingBufferSample(mProcessingIndex);
			
			mGamma = (Math.random() > 0.5) ? 0.15 : 0.20;
			mAlpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
		    mThreshold = mAlpha*mGamma*mLocalPeak + (1-mAlpha)*mThreshold;
		    
			mRefractoryCounter = mRefractoryWindowSize;
			
			Log.d("", String.valueOf(mThreshold));
			
			if(mAdded == false) {
				mAdded= true;
				mOperationResult = 1;
			} else {
				mOperationResult = 0;
			}
			
		} else {
			mAdded = false;
			mOperationResult = 0;
		}

	}
	
	/*
	private void qrs() {
		mOperationIndex = mProcessingBuffer.getProcessingIndex();	
	   
		if(mProcessingBuffer.getProcessingBufferSample(mOperationIndex) >= mLocalPeak) {
			mLocalPeak = mProcessingBuffer.getProcessingBufferSample(mOperationIndex);
		}
	 
		
	    mStep = 1;
	    mWindow = 100;
	    //for(int i = mOperationIndex; i > mOperationIndex - mWindow; i-=mStep) {
	        
	    	mLocalPeak = 0;
	        for(int j = mOperationIndex; j > mOperationIndex - mWindow; j--) {
	            if(mProcessingBuffer.getProcessingBufferSample(j) > mLocalPeak) {
	            	mLocalPeak = mProcessingBuffer.getProcessingBufferSample(j);
	            }
	        }
	        
	        mAdded = false;
	        for(int j = mOperationIndex; j > mOperationIndex - mWindow; j--) {
	            if(mProcessingBuffer.getProcessingBufferSample(j) > mLocalPeak && !mAdded) {
	                mOperationResult = 1;
	            	
	                mAdded = true;
	                break;
	            }
	            else {
	                mOperationResult = 0;
	            }
	        }
	        
	        mGamma = (Math.random() > 0.5) ? 0.15 : 0.20;
  	        mAlpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
  	        mLocalPeak = mAlpha * mGamma * mLocalPeak + (1 - mAlpha) * mLocalPeak;
		//}

	}
	

	//}
	 */

}
