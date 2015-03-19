package com.ufavaloro.android.visu.processing.frequencyoperations;

import android.os.Handler;

import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingOperation;

public class FFT extends ProcessingOperation {

	public FFT(OperationType operationType, int operationChannel, int operationIndex
			, double fs, int samplesPerPackage, Handler processingInterfaceHandler) {
		super(operationType, operationChannel, operationIndex, fs, samplesPerPackage, processingInterfaceHandler);
		// TODO Auto-generated constructor stub
	}

	public void calculateFFT() {
		int index = mProcessingBuffer.getProcessingIndex();
		int N = 16;
		if(index < N-1) return;
		
		double[] xr = new double[N];
		double[] XR = new double[N];
		double[] xi = new double[N];
		double[] XI = new double[N];
		for(int i = 0; i < N; i++) { 
			xr[N-(i+1)] = mProcessingBuffer.getProcessingBufferSample(index-i);
			XR[i] = 0;
			xi[i] = 0;
			XI[i] = 0;
		}
		
		double N2=N/2;
    	double v=Math.log10(N)/Math.log10(2);
    	double WR[]= new double[(int)N];
    	double WI[]= new double[(int)N];
    	double T1R=0;
    	double T1I=0;
    	double Aux=0;
    	double s=1;
    	int	L=1;
    	int k=0;
    	int p;
    	int I=0;
    	int m=0;
    	boolean inverse = false;
    	
        if(inverse) s=-1;
    	
    	for(k = 0; k < N; k++) {
    		XR[k] = xr[k];
    		XI[k] = xi[k];
    		
    		WR[k]=Math.cos(2*Math.PI/N*k);
    		WI[k]=-s*Math.sin(2*Math.PI/N*k);
    	}
    	
    	k=0;
    	while(L<=v) {	
    		
    		do {
    			I=0;
    			m=k>>((int)v-L);
        		p=invertedBitRoutine(m,(int)v);
    			
        		do {
    				T1R=WR[p]*XR[k+(int)N2] - WI[p]*XI[k+(int)N2];
    				T1I=WI[p]*XR[k+(int)N2]+WR[p]*XI[k+(int)N2];
    				XR[k+(int)N2]=XR[k]-T1R;
    				XI[k+(int)N2]=XI[k]-T1I;
    				XR[k]=XR[k]+T1R;
    				XI[k]=XI[k]+T1I;
    				k++;
    				I++;
    			}while(I<N2);
    			
    			k=k+(int)N2;
    			
    		}while(k<N-1);
    		
    		L++;
    		N2=N2/2;
    		k=0;
    	}
    	
    	k=0;
    	
    	do {
    		m=invertedBitRoutine(k,(int)v);
    		
    		if(m>k) {
    			Aux=XR[k];
    			XR[k]=XR[m];
    			XR[m]=Aux;
    			Aux=XI[k];
    			XI[k]=XI[m];
    			XI[m]=Aux;
    		}
    		
    		k++;
    		
    	}while(k<N-1);
    	
    	mOperationResult = (int) XR[N-1];
    }
	
	private int invertedBitRoutine(int m, int v) {
	   int i=0,p=0,c;
	   
	   for(i = 0; i < v; i++) {
		   c=(m>>i)&1;
		   p=p+(c<<(v-i-1));
	   }
	   
	   return p;
    }
	
	public void operate() {
		calculateFFT();
	}
}
