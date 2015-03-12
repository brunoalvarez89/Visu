package com.ufavaloro.android.visu.processing.qrsdetection;

import com.ufavaloro.android.visu.processing.OperationType;
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
		mProcessingBuffer = new SamplesBuffer(samplesPerPackage*i);
		
		mHighPass = new float[samplesPerPackage*i];
		mLowPass = new float[samplesPerPackage*i];
		mMean = new float[samplesPerPackage*i];
		mQrs = new int[samplesPerPackage*i];
		
	}

	// Moving Average Filter (High Pass)
	private void mafHighPass() {
		//float[] highPass = new float[nsamp];
		short[] samples = mProcessingBuffer.getBuffer();
		int nsamp = samples.length;
		
        for(int i = 0; i < nsamp; i++) {
            float y1 = 0;
            float y2 = 0;
 
            int y2_index = i-((M+1)/2);
            if(y2_index < 0) {
                y2_index = nsamp + y2_index;
            }
            y2 = samples[y2_index];
 
            float y1_sum = 0;
            for(int j=i; j>i-M; j--) {
                int x_index = i - (i-j);
                if(x_index < 0) {
                    x_index = nsamp + x_index;
                }
                y1_sum += samples[x_index];
            }
 
            y1 = mConstant * y1_sum;
            mHighPass[i] = y2 - y1;
 
        }        
 
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
		mProcessingInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), mLowPass).sendToTarget();

	}
	
	private void mean() {
		float sum = 0;
		for(int i = 0; i < mMean.length; i++) {
			sum = sum + mLowPass[i];
		}
		
		for(int i = 0; i < mMean.length; i++) {
			mMean[i] = sum / mMean.length;
		}
	 
		//mProcessingInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), mMean).sendToTarget();

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
	public int[] operate() {	
		//for(int i = 0; i < samples.length; i++) samples_int[i] = (int) samples[i]; 
		//for(int i = 0; i < samples.length; i++) System.out.println(samples_int[i]);
		
		mafHighPass();
		//for(int i = 0; i < samples.length; i++) System.out.println(highPass[i]);
        
		lowPass();
		//for(int i = 0; i < samples.length; i++) System.out.println(lowPass[i]);

		mean();
		//qrs();
		//for(int i = 0; i < samples.length; i++) System.out.println(QRS[i]);
		
		return mQrs;
	}
	
}
