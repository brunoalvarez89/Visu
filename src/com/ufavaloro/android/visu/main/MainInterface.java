package com.ufavaloro.android.visu.main;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.util.SparseArray;
import android.widget.Toast;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.connection.Protocol;
import com.ufavaloro.android.visu.connection.ProtocolMessage;
import com.ufavaloro.android.visu.draw.DrawInterface;
import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ProcessingInterface;
import com.ufavaloro.android.visu.processing.ProcessingOperation;
import com.ufavaloro.android.visu.storage.SamplesBuffer;
import com.ufavaloro.android.visu.storage.StorageInterface;
import com.ufavaloro.android.visu.storage.StorageInterfaceMessage;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;
import com.ufavaloro.android.visu.storage.datatypes.PatientData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.ufavaloro.android.visu.userinterface.MainActivity;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.internal.dr;
import com.google.android.gms.internal.op;

@SuppressLint("HandlerLeak")
public class MainInterface {

	// Storage System Interface (Local and Google Drive)
	private StorageInterface storageInterface;
	
	// Bluetooth Protocol (needed for decoding incoming packages)
	private Protocol bluetoothProtocol;
	
	// Draw Interface
	private DrawInterface drawInterface;
	
	// Processing Interface
	private ProcessingInterface processingInterface;
	
	// On-Line StudyData (data acquired in real-time from a sensor)
	public StudyData[] onlineStudyData;
	
	// Off-Line StudyData (data acquired from a file)
	public ArrayList<StudyData> offlineStudyData = new ArrayList<StudyData>();
	
	// Activity Context (needed for Google Drive API)
	private MainActivity mMainActivity;
	private Handler mMainActivityHandler;

	// Online ADC Channels
	private int mTotalAdcChannels;
		
	
	/**
	 * Constructor.
	 * @param mainActivity - Main Activity of the program (needed for Google Drive API).
	 */
	public MainInterface(MainActivity mainActivity, Handler mainActivityHandler) {
		mMainActivity = mainActivity;
		mMainActivityHandler = mainActivityHandler;
		drawInterface = (DrawInterface) mainActivity.findViewById(R.id.drawSurface);
		bluetoothProtocol = new Protocol(ConnectionInterfaceHandler);
		storageInterface = new StorageInterface(mainActivity, mStorageInterfaceHandler);
		processingInterface = new ProcessingInterface(mProcessingInterfaceHandler);
	}

	/**
	 * Creates a StudyData array of channelsToStore.size() elements.
	 */
	public void newStudy(String patientName, String patientSurname, String studyName
						 , SparseArray<Integer> channelsToStore) {
 		 		
		PatientData patientData = new PatientData(patientName, patientSurname, studyName);

		for(int i = 0; i < channelsToStore.size(); i++) {
			int index = channelsToStore.valueAt(i);
			onlineStudyData[index].setMarkedForStoring(true);
		}
		
		for(int i = 0; i < onlineStudyData.length; i++) {
			onlineStudyData[i].setPatientData(patientData);
			drawInterface.getChannels().getChannelAtIndex(i).getStudyData().setPatientData(patientData);
		}
		
		drawInterface.getChannels().update();
		
		// Creo carpetas locales y en google drive
		storageInterface.createStudyFolders(onlineStudyData);
		
		// Creo archivos .vis de los estudios
		storageInterface.createLocalStudyFiles(onlineStudyData);
 	
 	}
 	
	/**
	 * Saves the current onlineStudyData array to Google Drive.
	 */
	public void saveStudyToGoogleDrive() {
		storageInterface.createGoogleDriveStudyFiles(onlineStudyData);	
	}

	/**
	 * Sets the Study Type of a given channel.
	 */
	public void setStudyType(int studyType, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setStudyType(studyType);
		// Guardo valor en los buffers de dibujo
		drawInterface.getChannels().getChannelAtIndex(channel).setStudyType(studyType);
		drawInterface.getChannels().update();
	}
	
	/**
	 * Sets the Maximum Amplitude of a given channel.
	 */
	public void setAMax(double aMax, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setAMax(aMax);
		// Guardo valor en los buffers de dibujo
		drawInterface.getChannels().getChannelAtIndex(channel).setAMax(aMax);
		drawInterface.getChannels().update();
	}
	
	/**
	 * Sets the Minimum Amplitude of a given channel.
	 */
	public void setAMin(double aMin, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setAMin(aMin);
		// Guardo valor en los buffers de dibujo
		drawInterface.getChannels().getChannelAtIndex(channel).setAMin(aMin);
		drawInterface.getChannels().update();
	}
	
	/**
	 * Checks the Google Drive connection.
	 * @return True if connected, false otherwise.
	 */
	public boolean isGoogleDriveConnected() {
		return storageInterface.googleDrive.isConnected();
	}
		
 	public void startRecording() { 		
 		drawInterface.currentlyRecording = true;
 		storageInterface.recording = true;
 	}

 	public void stopRecording() {		
 		drawInterface.currentlyRecording = false;
		storageInterface.recording = false;
 	}
 	
 	public DrawInterface getDrawInterface() {
 		return drawInterface;
 	}
 	
 	public StorageInterface getStorageInterface() {
 		return storageInterface;
 	}
 	
 	public Protocol getBluetoothProtocol() {
 		return bluetoothProtocol;
 	}

/*****************************************************************************************
Protocol Event Handling
*****************************************************************************************/	
	@SuppressLint("HandlerLeak")
	private final Handler ConnectionInterfaceHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			ProtocolMessage protocolMessage = ProtocolMessage.values(msg.what);
			
			switch (protocolMessage) {
				
				case NEW_SAMPLES_BATCH:
					short[] samples = (short[]) msg.obj;
					int channel = msg.arg2;
					onNewSamplesBatch(samples, channel);
					break;
				
				case ADC_DATA:
					AdcData[] adcData = (AdcData[]) msg.obj;
					onAdcData(adcData);
					break;
					
				case TOTAL_ADC_CHANNELS:
					int totalAdcChannels = (Integer) msg.obj;
					onTotalAdcChannels(totalAdcChannels);
					break;
					
				case CONNECTED:
					onBluetoothConnected();
					break;
				
				case DISCONNECTED:
					onBluetoothDisconnected();
					break;
					
				default:
					break;
			
			}
		}
	};
	
 	private void onNewSamplesBatch(short[] samples, int channel) {
 		if(drawInterface.onlineDrawBuffersOk == true) drawInterface.drawSamples(samples, channel);
		if(storageInterface.recording == true) storageInterface.setSamples(onlineStudyData[channel], samples);
		
		processingInterface.writeSamples(samples, channel, 0);
 	}
 	
 	private void onTotalAdcChannels(int totalAdcChannels) {
 		mTotalAdcChannels = totalAdcChannels;
 	}
 	
 	private void onBluetoothConnected() {
 		mMainActivityHandler.obtainMessage(MainInterfaceMessage.BLUETOOTH_CONNECTED.getValue()).sendToTarget();
 	}
 	
 	private void onBluetoothDisconnected() {
 		mMainActivityHandler.obtainMessage(MainInterfaceMessage.BLUETOOTH_DISCONNECTED.getValue()).sendToTarget();
 	}
 	
 	private void onAdcData(AdcData[] adcData) {
 		onlineStudyData = new StudyData[mTotalAdcChannels];
 		AcquisitionData acquisitionData = null;
 		SamplesBuffer samplesBuffer;
 		
 		for(int i = 0; i < mTotalAdcChannels; i++) {
 			onlineStudyData[i] = new StudyData();
 			
 			acquisitionData = new AcquisitionData(adcData[i]);
 			onlineStudyData[i].setAcquisitionData(acquisitionData);
 			
 			samplesBuffer = new SamplesBuffer(onlineStudyData[i].getAcquisitionData(), "");
 			onlineStudyData[i].setSamplesBuffer(samplesBuffer);
 		}

 		for(int i = 0; i < bluetoothProtocol.getTotalAdcChannels(); i++) {
 			drawInterface.addChannel(onlineStudyData[i], true);
			
 			
			processingInterface.addProcessingOperation(OperationType.TIME_DERIVATIVE
									, acquisitionData.getFs()
									, acquisitionData.getSamplesPerPackage()
									, i);
			
			processingInterface.addProcessingOperation(OperationType.EKG_QRS_FIRST_DERIVATIVE_SLOPE
									, acquisitionData.getFs()
									, acquisitionData.getSamplesPerPackage()
									, i);
			/*
			processingInterface.addProcessingOperation(OperationType.EKG_QRS_ADAPTIVE_THRESHOLD
					, acquisitionData.getFs()
					, acquisitionData.getSamplesPerPackage()
					, i);
			*/
		}
 		
 		drawInterface.addChannel(onlineStudyData[0], true);
 		drawInterface.addChannel(onlineStudyData[0], true);
 		//drawInterface.addChannel(onlineStudyData[0], true);
 		
		drawInterface.onlineDrawBuffersOk = true;
		drawInterface.startDrawing();
 	}
		
/*****************************************************************************************
Storage Interface Event Handling
*****************************************************************************************/
	@SuppressLint("HandlerLeak")
	private final Handler mStorageInterfaceHandler = new Handler() {
		
		// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			StorageInterfaceMessage storageInterfaceMessage = StorageInterfaceMessage.values(msg.what);
			
			switch (storageInterfaceMessage) {
				
				case GOOGLE_DRIVE_CONNECTED:
					onGoogleDriveConnected();
					break;
					
				case GOOGLE_DRIVE_SUSPENDED:
					onGoogleDriveSuspended();
					break;
					
				case GOOGLE_DRIVE_DISCONNECTED:
					onGoogleDriveDisconnected();
					break;
					
				case GOOGLE_DRIVE_CONNECTION_FAILED:
					onGoogleDriveConnectionFailed(msg);
					break;
					
				case GOOGLE_DRIVE_FILE_OPENED:
					onGoogleDriveFileOpened(msg.obj);
					break;
				
				case LOCAL_STORAGE_FILE_OPENED:
					onLocalStorageFileOpened(msg.obj);
				
				default:
					break;
			}
		}
		
	};
	
 	private void onGoogleDriveConnected() {}
 	
 	private void onGoogleDriveSuspended() {}
 	
 	private void onGoogleDriveDisconnected() {}
 	
 	private void onGoogleDriveConnectionFailed(Message msg) {}
 	
 	private void onGoogleDriveFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		offlineStudyData.add(studyData);
 		drawInterface.addChannel(studyData, false); 	
 	}
 	
 	private void onLocalStorageFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		if(studyData.getSamplesBuffer().getSize() == 0) {
 			Toast.makeText(mMainActivity, "El archivo no posee muestras", Toast.LENGTH_LONG).show();
 			return;
 		} else {
	 		offlineStudyData.add(studyData);
	 		drawInterface.addChannel(studyData, false);
	 	}
 	}
 	
/*****************************************************************************************
Processing Operation Interface Event Handling
*****************************************************************************************/
	@SuppressLint("HandlerLeak")
	private final Handler mProcessingInterfaceHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			double operationResult = (double)(msg.obj);
			int operationOrder = msg.arg1;
			int nothing = msg.arg2;
			
			// Tipo de mensaje recibido
			OperationType operationType = OperationType.values(msg.what);
			
			switch (operationType) {
						
				case TIME_DERIVATIVE:
					onTimeDerivative(operationResult, operationOrder);
					break;
					
				case TIME_SELF_MULTIPLY:
					onSelfMultiply(operationResult, operationOrder);
					break;
					
				case TIME_MAF:
					onTimeMAF(operationResult, operationOrder);
					break;
					
				case TIME_LOWPASS:
					onTimeLowPass(operationResult, operationOrder);
					break;
					
				case TIME_HIGHPASS:
					onTimeHighPass(operationResult, operationOrder);
					break;
					
				case FREQUENCY_FFT:
					onFFT(operationResult);
					break;
					
				case EKG_QRS_ADAPTIVE_THRESHOLD:
					onQrsAdaptiveThreshold(operationResult);
					break;
				
				case EKG_QRS_FIRST_DERIVATIVE_SLOPE:
					onQrsFirstDerivativeSlope(operationResult);
					break;
					
				default:
					break;
			
			}
		}
	};

	private short adaptResult(double operationResult) { 
 		int bits = onlineStudyData[0].getAcquisitionData().getBits();
 		int steps = (int) (Math.pow(2, bits) - 1);
 		 		
 		return (short) (operationResult*steps);
	}

	private void onTimeDerivative(double operationResult, int operationOrder) {
 		short adaptedResult = adaptResult(operationResult); 
 		processingInterface.writeSample(adaptedResult, 0, 1);
 		drawInterface.drawSample(adaptedResult, 1);
 	}
 	
 	private void onSelfMultiply(double operationResult, int operationOrder) {
 		short adaptedResult = adaptResult(operationResult); 
 	}
 	
 	private void onFFT(double operationResult) {
 		short adaptedResult = adaptResult(operationResult); 
 	}
 	
 	private void onQrsAdaptiveThreshold(double operationResult) {
 		short adaptedResult = adaptResult(operationResult);
 		drawInterface.drawSample(adaptedResult, 3);
 	}
 	
 	private void onQrsFirstDerivativeSlope(double operationResult) {
 		short adaptedResult = adaptResult(operationResult); 
 		drawInterface.drawSample(adaptedResult, 2);
 		if(operationResult == 1) drawInterface.heartBeat();
 	}
 	
 	private void onTimeMAF(double operationResult, int operationOrder) {
 		short adaptedResult = adaptResult(operationResult); 
 		processingInterface.writeSample(adaptedResult, 0, 1);
 		drawInterface.drawSample(adaptedResult, 1);
 		//Log.d("", "MAF: " + adaptedResult);
 	}
	
	private void onTimeLowPass(double operationResult, int operationOrder) {
 		short adaptedResult = adaptResult(operationResult);
 		//processingInterface.writeSample(adaptedResult, 0, 2);
 		drawInterface.drawSample(adaptedResult, 1);
 		Log.d("", "Low: " + operationResult);
 		Log.d("", "Low Adapted: " + adaptedResult);
	}
	
	private void onTimeHighPass(double operationResult, int operationOrder) {
 		short adaptedResult = adaptResult(operationResult);
 		//processingInterface.writeSample(adaptedResult, 0, 2);
 		drawInterface.drawSample(adaptedResult, 1);
 		//Log.d("", "High: " + adaptedResult);
	}

}//MainInterface
