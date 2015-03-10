package com.ufavaloro.android.visu.processing;

import android.os.Handler;
import android.util.Log;


public class ProcessingInterface {
	
	private ProcessingThread mProcessingThread;
	private ProcessingOperation mProcessingOperation;
	private Handler mMainInterfaceHandler;
	
	public ProcessingInterface(Handler mainInterfaceHandler) {
		mMainInterfaceHandler = mainInterfaceHandler;
		mProcessingOperation = new ProcessingOperation();
		mProcessingThread = new ProcessingThread();
		mProcessingThread.start();
	}
	
	public void resume() {
		mProcessingThread.onResume();
	}
	
	public void pause() {
		mProcessingThread.onPause();
	}
	
	public void detectQrs(short[] samples, int channel) {
		mProcessingOperation.setSamples(samples);
		mProcessingOperation.setChannel(channel);
		mProcessingOperation.setOperationType(OperationType.QRS_DETECTION);
		mProcessingThread.setOperation(mProcessingOperation);
	}
	
	private class ProcessingThread extends Thread {
		
		private boolean mRun = true;
		private Object mPauseLock = new Object();
		private boolean mPaused = false;
		
		public void setOperation(ProcessingOperation processingOperation) {
			mProcessingOperation = processingOperation;	
		}
		
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

				if(mProcessingOperation.getOperationType() == OperationType.QRS_DETECTION) {

					synchronized(this) {

						OperationType operationType = mProcessingOperation.getOperationType();
						
						switch (operationType) {
						
							case NULL:
								break;
						
							case QRS_DETECTION:
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
								break;
						}
					}				
				}
			}
		}


		@SuppressWarnings("unused")
		public void onPause() {
			synchronized (mPauseLock) {
				mPaused = true;
			}
		}
		 
		@SuppressWarnings("unused")
		public void onResume() {
			synchronized (mPauseLock) {
				mPaused = false;
				mPauseLock.notifyAll();
			}
		}
		
	}//ProcessingThread

}
