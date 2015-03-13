package com.ufavaloro.android.visu.processing;

import com.ufavaloro.android.visu.processing.ekg.MAF;
import com.ufavaloro.android.visu.processing.timeoperations.FirstOrderDerivative;
import com.ufavaloro.android.visu.processing.timeoperations.Squaring;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class ProcessingInterface {
	
	private ProcessingThread mProcessingThread;
	private ProcessingOperation[][] mProcessingOperation;
	private int[] mProcessingOperationsPerChannel;
 	private Handler mMainInterfaceHandler;
	
	public ProcessingInterface(Handler mainInterfaceHandler) {
		mProcessingOperation = new ProcessingOperation[10][10];
		mMainInterfaceHandler = mainInterfaceHandler;
		mProcessingThread = new ProcessingThread();
		mProcessingThread.start();
	}
	
	public synchronized void writeSamples(short[] samples, int channel, int operationIndex) {
		ProcessingBuffer buffer = mProcessingOperation[channel][operationIndex].getProcessingBuffer();
		buffer.writeRawSamples(samples);
	}
	
	public synchronized void addProcessingOperation(OperationType operationType, double fs, int samplesPerPackage, int channel) {				
		int operationNumber = 0;
		
		for(int i = 0; i < mProcessingOperation.length ; i++) {
			if(mProcessingOperation[channel][i] == null) operationNumber = i; break;
		}
		
		if(operationType == OperationType.EKG_QRS_MAF) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new MAF(operationType, fs, samplesPerPackage, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_FIRST_ORDER_DERIVATIVE) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new FirstOrderDerivative(operationType, fs, samplesPerPackage, mProcessingOperationHandler, channel);
		}
		
		if(operationType == OperationType.TIME_SQUARING) {
			mProcessingOperation[channel][operationNumber] = (ProcessingOperation) 
			new Squaring(operationType, fs, samplesPerPackage, mProcessingOperationHandler, channel);
		}
	
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
	
	private class ProcessingThread extends Thread {
		
		private boolean mRun = true;
		private Object mPauseLock = new Object();
		private boolean mPaused = false;
		
		public ProcessingThread() {}
			
		// Thread.run()
		@Override
		public void run() {
			while(mRun) {
				
				// Pause Lock
				synchronized(mPauseLock) {
					while(mPaused) {
						try {
							mPauseLock.wait();
						} catch (InterruptedException e) {}
					}
				}

				if(mProcessingOperation != null) {
					
					try {
						Thread.sleep(1,5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(mProcessingOperation != null) {
						synchronized(mProcessingOperation) {
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
		
	}//ProcessingThread

	@SuppressLint("HandlerLeak")
	private final Handler mProcessingOperationHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			OperationType operationType = OperationType.values(msg.what);
			
			switch (operationType) {
				
				case TIME_FIRST_ORDER_DERIVATIVE:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_FIRST_ORDER_DERIVATIVE.getValue(), msg.obj).sendToTarget();
					break;
					
				case TIME_SQUARING:
					mMainInterfaceHandler.obtainMessage(OperationType.TIME_SQUARING.getValue(), msg.obj).sendToTarget();
					break;
					
				case EKG_QRS_MAF:
					mMainInterfaceHandler.obtainMessage(OperationType.EKG_QRS_MAF.getValue()).sendToTarget();
					break;
					
				default:
					break;
			
			}
		}
	};
}
