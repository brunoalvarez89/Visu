package com.ufavaloro.android.visu.processing.ekg;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingBuffer;
import com.ufavaloro.android.visu.storage.SamplesBuffer;

import android.os.Handler;

// Moving Average based Filtering 
// http://cinc.org/archives/2003/pdf/585.pdf
public class MAF extends QrsDetection {

	private int M = 5;
	private float mConstant = 1/M;
	private float[] mHighPass;
	private float[] mLowPass;
	private float[] mMean;
	private int[] mQrs;
	//QRS -> 0.06 - 0.10 seg
	
	public MAF(OperationType operationType, double fs, int samplesPerPackage
			   , Handler processingInterfaceHandler, int channel) {
		
		super(operationType, fs, samplesPerPackage, processingInterfaceHandler, channel);
		
		int i = 0;
		while(samplesPerPackage*i < 50) i++;
		mProcessingBuffer = new ProcessingBuffer(samplesPerPackage*i);
		
		mHighPass = new float[samplesPerPackage*i];
		mLowPass = new float[samplesPerPackage*i];
		mMean = new float[samplesPerPackage*i];
		mQrs = new int[samplesPerPackage*i];
	}

	// Moving Average Filter (High Pass)
	private void mafHighPass() {
		int index = mProcessingBuffer.getProcessingIndex();
		
        float y1_sum = 0;
        for(int j = index; j > index - M; j--) {
        	y1_sum = y1_sum + mProcessingBuffer.getProcessedSample(j);
        }
        float y1 = mConstant * y1_sum;
        
        float y2 = mProcessingBuffer.getProcessedSample(index-((M+1)/2));

        mHighPass[index] = (y2-y1)*(y2-y1);       
		mProcessingInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), mHighPass[index]).sendToTarget();

    }

	// Low Pass Filter
	private void lowPass() {
	    //float[] lowPass = new float[nsamp];
		float[] samples = mHighPass;
		int nsamp = samples.length;
		int filterSamples = 30;
				
	    for(int i=0; i<nsamp; i++) {
	        float sum = 0;
	       
	        if(i+filterSamples < nsamp) {
	            for(int j=i; j<i+filterSamples; j++) {
	                float current = samples[j] * samples[j];
	                sum += current;
	            }
	        }
	        else if(i+filterSamples >= samples.length) {
	            int over = i+filterSamples - samples.length;
	           
	            for(int j=i; j<samples.length; j++) {
	                float current = samples[j] * samples[j];
	                sum += current;
	            }
	            
	            for(int j=0; j<over; j++) {
	                float current = samples[j] * samples[j];
	                sum += current;
	            }
	        }
	 
	        mLowPass[i] = sum;
	    }
		//mProcessingInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), mLowPass).sendToTarget();

	}
	
	private void mean() {
		float sum = 0;
		for(int i = 0; i < mMean.length; i++) {
			sum = sum + mLowPass[i];
		}
		
		for(int i = 0; i < mMean.length; i++) {
			mMean[i] = sum / mMean.length;
		}
		
		mProcessingInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), mMean).sendToTarget();
	}
	
	// QRS Detection
	private int[] qrs() {
	    //int[] QRS = new int[nsamp];
		float[] samples = mLowPass;
		int nsamp = samples.length;
		
	    double treshold = 0;
	 
	    for(int i=0; i<200; i++) {
	        if(samples[i] > treshold) {
	            treshold = samples[i];
	        }
	    }
	 
	    int frame = 100;
	 
	    for(int i=0; i<samples.length; i+=frame) {
	        float max = 0;
	        int index = 0;
	       
	        if(i + frame > samples.length) {
	            index = samples.length;
	        }
	        else {
	            index = i + frame;
	        }
	        
	        for(int j=i; j<index; j++) {
	            if(samples[j] > max) max = samples[j];
	        }
	        
	        boolean added = false;
	        
	        for(int j=i; j<index; j++) {
	            if(samples[j] > treshold && !added) {
	                mQrs[j] = 1;
	                added = true;
	                mProcessingInterfaceHandler.obtainMessage(OperationType.HEARTBEAT.getValue()).sendToTarget();
	                break;
	            }
	            else {
	                mQrs[j] = 0;
	            }
	        }
	 
	        double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	        double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	 
	        treshold = alpha * gama * max + (1 - alpha) * treshold;
	 
	    }
	 
	    return mQrs;
	}

	// Full Procedure
	@Override
	public void operate() {	
		//for(int i = 0; i < samples.length; i++) samples_int[i] = (int) samples[i]; 
		//for(int i = 0; i < samples.length; i++) System.out.println(samples_int[i]);
		
		mafHighPass();
		//for(int i = 0; i < samples.length; i++) System.out.println(highPass[i]);
        
		//lowPass();
		//for(int i = 0; i < samples.length; i++) System.out.println(lowPass[i]);

		//mean();
		//qrs();
		//for(int i = 0; i < samples.length; i++) System.out.println(QRS[i]);
	}
	
}
