package com.ufavaloro.android.visu.processing;

import com.ufavaloro.android.visu.processing.ekg.FirstDerivativeSlope;
import com.ufavaloro.android.visu.processing.ekg.AdaptiveThreshold;
import com.ufavaloro.android.visu.processing.frequencyoperations.FFT;
import com.ufavaloro.android.visu.processing.timeoperations.Derivative;
import com.ufavaloro.android.visu.processing.timeoperations.HighPass;
import com.ufavaloro.android.visu.processing.timeoperations.LowPass;
import com.ufavaloro.android.visu.processing.timeoperations.MAF;
import com.ufavaloro.android.visu.processing.timeoperations.SelfMultiply;
import com.ufavaloro.android.visu.processing.timeoperations.MovingAverage;

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
		if(mProcessingOperation[channel][operationIndex] == null) return;
		
		ProcessingBuffer buffer = mProcessingOperation[channel][operationIndex].getProcessingBuffer();
		buffer.writeRawSample(sample);
	}
	
	public synchronized void addProcessingOperation(OperationType operationType, double fs
													, int samplesPerPackage, int operationChannel) {				
		int operationIndex = 0;
		for(int i = 0; i < mProcessingOperation.length ; i++) {
			if(mProcessingOperation[operationChannel][i] == null) {
				operationIndex = i; 
				break;
			}
		}
		
		if(operationType == OperationType.TIME_DERIVATIVE) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new Derivative(operationType, operationChannel, operationIndex, fs, samplesPerPackage
							, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.TIME_SELF_MULTIPLY) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new SelfMultiply(operationType, operationChannel, operationIndex, fs, samplesPerPackage
							, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.TIME_MAF) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new MAF(operationType, operationChannel, operationIndex, fs, samplesPerPackage
					, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.TIME_LOWPASS) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new LowPass(operationType, operationChannel, operationIndex, fs, samplesPerPackage
						, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.TIME_HIGHPASS) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new HighPass(operationType, operationChannel, operationIndex, fs, samplesPerPackage
						, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.TIME_MOVING_AVERAGE) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new MovingAverage(operationType, operationChannel, operationIndex, fs, samplesPerPackage
						, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.FREQUENCY_FFT) {			
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new FFT(operationType, operationChannel, operationIndex, fs, samplesPerPackage
					, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.EKG_QRS_ADAPTIVE_THRESHOLD) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new AdaptiveThreshold(operationType, operationChannel, operationIndex, fs, samplesPerPackage
									, mProcessingOperationHandler);
		}
		
		if(operationType == OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE) {
			mProcessingOperation[operationChannel][operationIndex] = (ProcessingOperation) 
			new FirstDerivativeSlope(operationType, operationChannel, operationIndex, fs, samplesPerPackage
									, mProcessingOperationHandler);
		}
		
	}

	public synchronized void removeProcessingOperation(int channel, int operationIndex) {
		mProcessingOperation[channel][operationIndex] = null;
	}
	
	public synchronized void resume() {
		mProcessingThread.onResume();
	}
	
	public synchronized void pause() {
		mProcessingThread.onPause();
	}
	
	public ProcessingOperation getOperation(int channel, int operationIndex) {
		if(mProcessingOperation[channel][operationIndex] != null) {
		return mProcessingOperation[channel][operationIndex];
		} else {
			return null;
		}
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
				
				snore(0);
				
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
			int operationChannel = msg.arg1;
			int operationIndex = msg.arg2;
			
			// Tipo de mensaje recibido
			OperationType operationType = OperationType.values(msg.what);
			
			switch (operationType) {
				
				case TIME_DERIVATIVE:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_DERIVATIVE.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();
					break;
					
				case TIME_SELF_MULTIPLY:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_SELF_MULTIPLY.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();					
					break;
				
				case TIME_MAF:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_MAF.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();	
					break;
					
				case TIME_LOWPASS:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_LOWPASS.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();	
					break;
					
				case TIME_HIGHPASS:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_HIGHPASS.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();	
					break;
					
				case TIME_MOVING_AVERAGE:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_MOVING_AVERAGE.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();	
					break;
					
				case FREQUENCY_FFT:
					mMainInterfaceHandler.obtainMessage(OperationType.FREQUENCY_FFT.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();					
					break;
					
				case EKG_QRS_ADAPTIVE_THRESHOLD:
					mMainInterfaceHandler.obtainMessage(OperationType.EKG_QRS_ADAPTIVE_THRESHOLD.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();	
					break;
					
				case EKG_QRS_FIRST_DERIVATIVE_SLOPE:
					mMainInterfaceHandler.obtainMessage(OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE.getValue()
														, operationChannel
														, operationIndex
														, operationResult).sendToTarget();					
					break;
					
				default:
					break;
			
			}
		}
	};
}
