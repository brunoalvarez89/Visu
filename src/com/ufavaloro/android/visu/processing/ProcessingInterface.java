package com.ufavaloro.android.visu.processing;

import com.ufavaloro.android.visu.processing.ekg.FirstDerivativeSlope;
import com.ufavaloro.android.visu.processing.ekg.AdaptiveThreshold;
import com.ufavaloro.android.visu.processing.frequencyoperations.FFT;
import com.ufavaloro.android.visu.processing.timeoperations.Derivative;
import com.ufavaloro.android.visu.processing.timeoperations.HighPass;
import com.ufavaloro.android.visu.processing.timeoperations.LowPass;
import com.ufavaloro.android.visu.processing.timeoperations.MAF;
import com.ufavaloro.android.visu.processing.timeoperations.SelfMultiply;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ProcessingInterface {
	
	private ProcessingThread mProcessingThread;
	private ProcessingOperation[][] mProcessingOperation;
 	private Handler mMainInterfaceHandler;
	
	public ProcessingInterface(Handler mainInterfaceHandler) {
		mProcessingOperation = new ProcessingOperation[10][10];
		mMainInterfaceHandler = mainInterfaceHandler;
		mProcessingThread = new ProcessingThread();
		mProcessingThread.start();
	}
	
	public void writeSamples(short[] samples, int channel, int operationIndex) {
		ProcessingBuffer buffer = mProcessingOperation[channel][operationIndex].getProcessingBuffer();
		buffer.writeRawSamples(samples);
	}
	
	public void writeSample(short sample, int channel, int operationIndex) {
		ProcessingBuffer buffer = mProcessingOperation[channel][operationIndex].getProcessingBuffer();
		buffer.writeRawSample(sample);
	}
	
	public synchronized void addProcessingOperation(OperationType operationType, double fs, int samplesPerPackage, int channel) {				
		int operationNumber = 0;
		
		for(int i = 0; i < mProcessingOperation.length ; i++) {
			if(mProcessingOperation[channel][i] == null) {
				operationNumber = i; 
				break;
			}
		}
		
		if(operationType == OperationType.TIME_DERIVATIVE) {
			int operationOrder = findOperationOrder(channel, operationType);
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new Derivative(operationType, fs, samplesPerPackage, operationOrder, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_SELF_MULTIPLY) {
			int operationOrder = findOperationOrder(channel, operationType);
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new SelfMultiply(operationType, fs, samplesPerPackage, operationOrder, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_MAF) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new MAF(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_LOWPASS) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new LowPass(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_HIGHPASS) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new HighPass(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.FREQUENCY_FFT) {			
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new FFT(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.EKG_QRS_ADAPTIVE_THRESHOLD) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new AdaptiveThreshold(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new FirstDerivativeSlope(operationType, fs, samplesPerPackage, 1, mProcessingOperationHandler, channel);
		}
		
	}

	private synchronized int findOperationOrder(int channel, OperationType operationType) {
		// Find derivative order
		int operationOrder = 1;
		for(int i = 0; i < mProcessingOperation[channel].length; i++) {
			if(mProcessingOperation[channel][i] != null) {
				if(mProcessingOperation[channel][i].getOperationType() == OperationType.TIME_DERIVATIVE) {
					operationOrder++;
				}
			}
		}
		return operationOrder;
	}
	
	public synchronized void removeProcessingOperation(OperationType operationType, int channel) {
		mProcessingOperation = null;
	}
	
	public synchronized void resume() {
		mProcessingThread.onResume();
	}
	
	public synchronized void pause() {
		mProcessingThread.onPause();
	}
	
	public ProcessingOperation getOperation(int channel, int operationNumber) {
		return mProcessingOperation[channel][operationNumber];
	}
	
	private class ProcessingThread extends Thread {
		
		private boolean mRun = true;
		private Object mPauseLock = new Object();
		private boolean mPaused = false;
		
		public ProcessingThread() {}
			
		// Thread.run()
		@Override
		public void run() {
			while(mRun) {
				
				snore(50);
				
				if(mProcessingOperation != null) {
				// For each channel
					for(int i = 0; i < mProcessingOperation.length; i++) {
						// For each ProcessingOperation
						for(int j = 0; j < mProcessingOperation[i].length; j++) {
							if(mProcessingOperation[i][j] != null) mProcessingOperation[i][j].nextOperation();	
						}	
					}
				}
			}
		}

		public void onPause() {
			synchronized (mPauseLock) {
				mPaused = true;
			}
		}
		 
		public void onResume() {
			synchronized (mPauseLock) {
				mPaused = false;
				mPauseLock.notifyAll();
			}
		}
		
		private void snore(long time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}//ProcessingThread

	@SuppressLint("HandlerLeak")
	private final Handler mProcessingOperationHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			Object operationResult = msg.obj;
			int operationOrder = msg.arg1;
			int nothing = msg.arg2;
			
			// Tipo de mensaje recibido
			OperationType operationType = OperationType.values(msg.what);
			
			switch (operationType) {
				
				case TIME_DERIVATIVE:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_DERIVATIVE.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();
					break;
					
				case TIME_SELF_MULTIPLY:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_SELF_MULTIPLY.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();					
					break;
				
				case TIME_MAF:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_MAF.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();	
					break;
					
				case TIME_LOWPASS:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_LOWPASS.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();	
					break;
					
				case TIME_HIGHPASS:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_HIGHPASS.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();	
					break;
					
				case FREQUENCY_FFT:
					mMainInterfaceHandler.obtainMessage(OperationType.FREQUENCY_FFT.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();					
					break;
					
				case EKG_QRS_ADAPTIVE_THRESHOLD:
					mMainInterfaceHandler.obtainMessage(OperationType.EKG_QRS_ADAPTIVE_THRESHOLD.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();					
					break;
					
				case EKG_QRS_FIRST_DERIVATIVE_SLOPE:
					mMainInterfaceHandler.obtainMessage(OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE.getValue()
														, operationOrder
														, nothing
														, operationResult).sendToTarget();					
					break;
					
				default:
					break;
			
			}
		}
	};
}
