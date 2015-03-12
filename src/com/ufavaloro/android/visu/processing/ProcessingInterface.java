package com.ufavaloro.android.visu.processing;

import com.ufavaloro.android.visu.processing.ekg.MAF;
import com.ufavaloro.android.visu.storage.SamplesBuffer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class ProcessingInterface {
	
	private ProcessingThread mProcessingThread;
	private ProcessingOperation mProcessingOperation;
	private Handler mMainInterfaceHandler;
	
	public ProcessingInterface(Handler mainInterfaceHandler) {
		mMainInterfaceHandler = mainInterfaceHandler;
		mProcessingThread = new ProcessingThread();
		mProcessingThread.start();
	}
	
	public synchronized void writeSamples(short[] samples, int channel) {
		ProcessingBuffer buffer = mProcessingOperation.getProcessingBuffer();
		buffer.writeRawSamples(samples);
	}
	
	public synchronized void addProcessingOperation(OperationType operationType, double fs, int samplesPerPackage, int channel) {
		if(operationType == OperationType.QRS_DETECTION_MAF) {
			mProcessingOperation = (ProcessingOperation) new MAF(operationType
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
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(!mProcessingOperation.isProcessing()) mProcessingOperation.nextOperation();
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
				
				case QRS_DETECTION_MAF:
					break;
					
				case HEARTBEAT:
					mMainInterfaceHandler.obtainMessage(OperationType.HEARTBEAT.getValue()).sendToTarget();
					break;
					
				case LOWPASS:
					mMainInterfaceHandler.obtainMessage(OperationType.LOWPASS.getValue(), msg.obj).sendToTarget();
					
				default:
					break;
			
			}
		}
	};
}
