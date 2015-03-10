package com.ufavaloro.android.visu.processing;

import com.ufavaloro.android.visu.storage.SamplesBuffer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class ProcessingInterface {
	
	private ProcessingThread mProcessingThread;
	private ProcessingOperation mProcessingOperation;
	private Handler mMainInterfaceHandler;
	
	public ProcessingInterface(Handler mainInterfaceHandler) {
		mMainInterfaceHandler = mainInterfaceHandler;
		mProcessingThread = new ProcessingThread();
		mProcessingThread.start();
		mProcessingThread.onPause();
	}
	
	public synchronized void writeSamples(short[] samples, int channel) {
		SamplesBuffer buffer = mProcessingOperation.getProcessingBuffer();
		buffer.writeSamples(samples);
		if(buffer.getStoringIndex() == 0) mProcessingThread.onResume();
		
	}
	
	public synchronized void addProcessingOperation(OperationType operationType, double fs, int samplesPerPackage, int channel) {
		if(operationType == OperationType.QRS_DETECTION) {
			mProcessingOperation = (ProcessingOperation) new QrsDetector(operationType
																		  , fs
																		  , samplesPerPackage
																		  , mProcessingOperationHandler
																		  , channel);
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
						OperationType operationType = mProcessingOperation.getOperationType();
					
						if(operationType == OperationType.QRS_DETECTION) {
							int[] result = mProcessingOperation.operate();
							int channel = mProcessingOperation.getChannel();
							int operation = OperationType.QRS_DETECTION.getValue();
							int success = ProcessingInterfaceMessage.SUCCESS.getValue();
							
							mMainInterfaceHandler.obtainMessage(// What did I do?
																operation
																// Was it succesful?
																, success			
																// What channel?
																, channel
																// Result
																, result).sendToTarget();
						}				
					
					
					this.onPause();
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
				
				case QRS_DETECTION:
					break;
					
				case HEARTBEAT:
					mMainInterfaceHandler.obtainMessage(OperationType.HEARTBEAT.getValue()).sendToTarget();
					break;
					
				default:
					break;
			
			}
		}
	};
}
