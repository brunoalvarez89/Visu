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
import com.ufavaloro.android.visu.connection.ConnectionInterface;
import com.ufavaloro.android.visu.connection.ConnectionInterfaceMessage;
import com.ufavaloro.android.visu.connection.protocol.Protocol;
import com.ufavaloro.android.visu.connection.protocol.ProtocolMessage;
import com.ufavaloro.android.visu.draw.DrawInterface;
import com.ufavaloro.android.visu.processing.OperationType;
import com.ufavaloro.android.visu.processing.ParameterName;
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

	// Storage Interface (Local/Google Drive)
	private StorageInterface mStorageInterface;
	
	// Connection Interface
	private ConnectionInterface mConnectionInterface;
	
	// Draw Interface
	private DrawInterface mDrawInterface;
	
	// Processing Interface
	private ProcessingInterface mProcessingInterface;
	
	// On-Line StudyData (data acquired in real-time from a sensor)
	public StudyData[] onlineStudyData;
	
	// Off-Line StudyData (data acquired from a file)
	public ArrayList<StudyData> offlineStudyData = new ArrayList<StudyData>();
	
	// Activity Context (needed for Google Drive API)
	private MainActivity mMainActivity;
	private Handler mMainActivityHandler;
	
	/**
	 * Constructor.
	 * @param mainActivity - Main Activity of the program (needed for Google Drive API).
	 */
	public MainInterface(MainActivity mainActivity, Handler mainActivityHandler) {
		mMainActivity = mainActivity;
		mMainActivityHandler = mainActivityHandler;
		mDrawInterface = (DrawInterface) mainActivity.findViewById(R.id.drawSurface);
		mConnectionInterface = new ConnectionInterface(ConnectionInterfaceHandler);
		mStorageInterface = new StorageInterface(mainActivity, mStorageInterfaceHandler);
		mProcessingInterface = new ProcessingInterface(mProcessingInterfaceHandler);
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
			mDrawInterface.getChannels().getChannelAtIndex(i).getStudyData().setPatientData(patientData);
		}
		
		mDrawInterface.getChannels().update();
		
		// Creo carpetas locales y en google drive
		mStorageInterface.createStudyFolders(onlineStudyData);
		
		// Creo archivos .vis de los estudios
		mStorageInterface.createLocalStudyFiles(onlineStudyData);
 	
 	}
 	
	/**
	 * Saves the current onlineStudyData array to Google Drive.
	 */
	public void saveStudyToGoogleDrive() {
		mStorageInterface.createGoogleDriveStudyFiles(onlineStudyData);	
	}

	/**
	 * Sets the Study Type of a given channel.
	 */
	public void setStudyType(int studyType, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setStudyType(studyType);
		// Guardo valor en los buffers de dibujo
		mDrawInterface.getChannels().getChannelAtIndex(channel).setStudyType(studyType);
		mDrawInterface.getChannels().update();
	}
	
	/**
	 * Sets the Maximum Amplitude of a given channel.
	 */
	public void setAMax(double aMax, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setAMax(aMax);
		// Guardo valor en los buffers de dibujo
		mDrawInterface.getChannels().getChannelAtIndex(channel).setAMax(aMax);
		mDrawInterface.getChannels().update();
	}
	
	/**
	 * Sets the Minimum Amplitude of a given channel.
	 */
	public void setAMin(double aMin, int channel) {
		// Guardo valor en los buffers de almacenamiento
		onlineStudyData[channel].getAcquisitionData().setAMin(aMin);
		// Guardo valor en los buffers de dibujo
		mDrawInterface.getChannels().getChannelAtIndex(channel).setAMin(aMin);
		mDrawInterface.getChannels().update();
	}
	
	/**
	 * Checks the Google Drive connection.
	 * @return True if connected, false otherwise.
	 */
	public boolean isGoogleDriveConnected() {
		return mStorageInterface.googleDrive.isConnected();
	}
		
 	public void startRecording() { 		
 		mDrawInterface.currentlyRecording = true;
 		mStorageInterface.recording = true;
 	}

 	public void stopRecording() {		
 		mDrawInterface.currentlyRecording = false;
		mStorageInterface.recording = false;
 	}
 	
 	public DrawInterface getDrawInterface() {
 		return mDrawInterface;
 	}
 	
 	public StorageInterface getStorageInterface() {
 		return mStorageInterface;
 	}
 	
 	public ConnectionInterface getConnectionInterface() {
 		return mConnectionInterface;
 	}

 	public ProcessingInterface getProcessingInterface() {
 		return mProcessingInterface;
 	}

/*****************************************************************************************
Connection Interface Event Handling
*****************************************************************************************/	
	@SuppressLint("HandlerLeak")
	private final Handler ConnectionInterfaceHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			ConnectionInterfaceMessage connectionInterfaceMessage = ConnectionInterfaceMessage.values(msg.what);
			
			switch (connectionInterfaceMessage) {
				
				case CONNECTED:
					onBluetoothConnected();
					break;
			
				case DISCONNECTED:
					onBluetoothDisconnected();
					break;
					
				case ADC_DATA:
					AdcData[] adcData = (AdcData[]) msg.obj;
					onAdcData(adcData);
					break;
				
				case NEW_SAMPLES_BATCH:
					short[] samples = (short[]) msg.obj;
					int channel1 = msg.arg2;
					onNewSamplesBatch(samples, channel1);
					break;
				
				case NEW_SAMPLE:
					short sample = (short) msg.obj;
					int channel2 = msg.arg2;
					onNewSample(sample, channel2);
					break;
					
				default:
					break;
			
			}
		}
	};
	
 	private void onNewSamplesBatch(short[] samples, int channel) {
 		if(mDrawInterface.onlineDrawBuffersOk == true) mDrawInterface.drawSamples(samples, channel);
		if(mStorageInterface.recording == true) mStorageInterface.setSamples(onlineStudyData[channel], samples);
		
		//mProcessingInterface.writeSamples(samples, channel, 0);
 	}

 	private void onNewSample(short sample, int channel) {
 		if(mDrawInterface.onlineDrawBuffersOk == true) mDrawInterface.drawSample(sample, channel);
		if(mStorageInterface.recording == true) mStorageInterface.setSample(onlineStudyData[channel], sample);
		
		addProcessingSample(sample, channel); 	
 	}
 	
 	private void onBluetoothConnected() {
 		mMainActivityHandler.obtainMessage(MainInterfaceMessage.BLUETOOTH_CONNECTED.getValue()).sendToTarget();
 	}
 	
 	private void onBluetoothDisconnected() {
 		mMainActivityHandler.obtainMessage(MainInterfaceMessage.BLUETOOTH_DISCONNECTED.getValue()).sendToTarget();
 	}
 	
 	private void onAdcData(AdcData[] adcData) {
 		int totalAdcChannels = adcData.length;
 		onlineStudyData = new StudyData[totalAdcChannels];
 		AcquisitionData acquisitionData = null;
 		SamplesBuffer samplesBuffer;
 		
 		for(int i = 0; i < totalAdcChannels; i++) {
 			onlineStudyData[i] = new StudyData();
 			
 			acquisitionData = new AcquisitionData(adcData[i]);
 			onlineStudyData[i].setAcquisitionData(acquisitionData);
 			
 			samplesBuffer = new SamplesBuffer(onlineStudyData[i].getAcquisitionData(), "");
 			onlineStudyData[i].setSamplesBuffer(samplesBuffer);
 		}

 		
 		for(int i = 0; i < mConnectionInterface.getProtocol(0).getTotalAdcChannels(); i++) {
 			mDrawInterface.addChannel(onlineStudyData[i], true);
		}
 		
 		addProcessingOperations();
 		
		mDrawInterface.onlineDrawBuffersOk = true;
		mDrawInterface.startDrawing();
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
 		mDrawInterface.addChannel(studyData, false); 	
 	}
 	
 	private void onLocalStorageFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		if(studyData.getSamplesBuffer().getSize() == 0) {
 			Toast.makeText(mMainActivity, "El archivo no posee muestras", Toast.LENGTH_LONG).show();
 			return;
 		} else {
	 		offlineStudyData.add(studyData);
	 		mDrawInterface.addChannel(studyData, false);
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
			int operationChannel = msg.arg1;
			int operationIndex = msg.arg2;
			
			// Tipo de mensaje recibido
			OperationType operationType = OperationType.values(msg.what);
			
			switch (operationType) {
						
				case TIME_DERIVATIVE:
					onTimeDerivative(operationResult, operationChannel, operationIndex);
					break;
					
				case TIME_SELF_MULTIPLY:
					onSelfMultiply(operationResult, operationChannel, operationIndex);
					break;
					
				case TIME_MAF:
					onTimeMAF(operationResult, operationChannel, operationIndex);
					break;
					
				case TIME_LOWPASS:
					onTimeLowPass(operationResult, operationChannel, operationIndex);
					break;
					
				case TIME_HIGHPASS:
					onTimeHighPass(operationResult, operationChannel, operationIndex);
					break;
					
				case TIME_MOVING_AVERAGE:
					onTimeMovingAverage(operationResult, operationChannel, operationIndex);
					break;
					
				case FREQUENCY_FFT:
					onFFT(operationResult, operationChannel, operationIndex);
					break;
					
				case EKG_QRS_ADAPTIVE_THRESHOLD:
					onQrsAdaptiveThreshold(operationResult, operationChannel, operationIndex);
					break;
				
				case EKG_QRS_FIRST_DERIVATIVE_SLOPE:
					onQrsFirstDerivativeSlope(operationResult, operationChannel, operationIndex);
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

	private void onTimeDerivative(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult); 
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
 	}
 	
 	private void onSelfMultiply(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult); 
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
 	}
 	
 	private void onFFT(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult); 
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
 	}
 	
 	private void onQrsAdaptiveThreshold(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult);
 		if(operationResult == 1)  {
 			mDrawInterface.heartBeat(operationChannel);
 			double bpm = mProcessingInterface.getOperation(operationChannel, operationIndex)
 												.getParameter(ParameterName.BPM);
 		}
 	}
 	
 	private void onQrsFirstDerivativeSlope(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult); 
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
 	}
 	
 	private void onTimeMAF(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult); 
 		mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 	}
	
	private void onTimeLowPass(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult);
 		mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
	}
	
	private void onTimeHighPass(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult);
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
	}
	
	private void onTimeMovingAverage(double operationResult, int operationChannel, int operationIndex) {
 		short adaptedResult = adaptResult(operationResult);
 		//mProcessingInterface.writeSample(adaptedResult, operationChannel, operationIndex+1);
 		//mDrawInterface.drawSample(adaptedResult, operationIndex+1);
	}

	private void addProcessingOperations() {
		int channel = 0;
		double fs = onlineStudyData[channel].getAcquisitionData().getFs();
		int samplesPerPackage = onlineStudyData[channel].getAcquisitionData().getSamplesPerPackage();
		
		mProcessingInterface.addProcessingOperation(OperationType.TIME_MAF, fs, samplesPerPackage, channel);
		mProcessingInterface.addProcessingOperation(OperationType.TIME_LOWPASS, fs, samplesPerPackage, channel);
		mProcessingInterface.addProcessingOperation(OperationType.EKG_QRS_ADAPTIVE_THRESHOLD, fs, samplesPerPackage, channel);
	}
	
	private void addProcessingSample(short sample, int channel) {
		if(mProcessingInterface.getOperation(channel, 0) == null) return;
		
		switch(channel) {
			case 0:
				mProcessingInterface.writeSample(sample, channel, 0);
				break;
			
			default:
				break;
		}
	}

}//MainInterface
