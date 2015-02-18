package com.ufavaloro.android.visu.maininterface;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.widget.Toast;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocol;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocolMessage;
import com.ufavaloro.android.visu.draw.DrawInterface;
import com.ufavaloro.android.visu.storage.SamplesBuffer;
import com.ufavaloro.android.visu.storage.StorageInterface;
import com.ufavaloro.android.visu.storage.StorageInterfaceMessage;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;
import com.ufavaloro.android.visu.storage.datatypes.PatientData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.ufavaloro.android.visu.userinterface.MainActivity;
import com.google.android.gms.drive.DriveId;

public class MainInterface {

	// Storage System Interface (Local and Google Drive)
	private StorageInterface storageInterface;
	
	// Bluetooth Protocol (needed for decoding incoming packages)
	private BluetoothProtocol bluetoothProtocol;
	
	// Drawing System Interface
	private DrawInterface drawInterface;
	
	// On-Line StudyData (data acquired in real-time from a sensor)
	public StudyData[] onlineStudyData;
	
	// Off-Line StudyData (data acquired from a file)
	public ArrayList<StudyData> offlineStudyData = new ArrayList<StudyData>();
	
	// Activity Context (needed for Google Drive API)
	private Activity mainActivity;

	// Online ADC Channels
	private int mTotalAdcChannels;
		
	
	/**
	 * Constructor.
	 * @param mainActivity - Main Activity of the program (needed for Google Drive API).
	 */
	public MainInterface(Activity mainActivity) {
		this.mainActivity = mainActivity;
		drawInterface = (DrawInterface) mainActivity.findViewById(R.id.drawSurface);
		bluetoothProtocol = new BluetoothProtocol(mBluetoothProtocolHandler);
		storageInterface = new StorageInterface(mainActivity, mStorageInterfaceHandler);	
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

	/**
	 * Opens a file from Google Drive.
	 * @param driveId - Google Drive ID of the given file.
	 */
	public void loadFileFromGoogleDrive(DriveId driveId) {
		storageInterface.loadFileFromGoogleDrive(driveId);
	}

	/**
	 * Opens a file from the Local External Storage.
	 * @param filePath - path of the given File.
	 */
	public void loadFileFromLocalStorage(String filePath) {
		storageInterface.loadFileFromLocalStorage(filePath);
	}
	
	/**
	 * Adds a Slave connection to the Bluetooth Connections array.
	 */
	public void addSlaveBluetoothConnection() {
		bluetoothProtocol.addSlaveBluetoothConnection();
	}
	
	/**
	 * Checks the last added Bluetooth connection.
	 * @return True if connected, false otherwise.
	 */
	public boolean isConnectedToRemoteDevice() {
		return bluetoothProtocol.isConnected();
	}
	
	/**
	 * Returns the amount of ADC channels of the last added Bluetooth connection.
	 */
	public int getTotalAdcChannels() {	
		return bluetoothProtocol.getTotalAdcChannels();
	}
	
	/**
	 * Returns the remote device name of the last added Bluetooth connection.
	 */
	public String getRemoteDevice() {
		return bluetoothProtocol.getActualRemoteDevice();
	}

	// Método para crear buffers de graficación online
	public void addChannel(int channel) {
		if(channel >= getTotalAdcChannels()) return;
		drawInterface.addChannel(onlineStudyData[channel], true);
		drawInterface.onlineDrawBuffersOk = true;
	}
	
	public void hideChannel(int channel) {
		drawInterface.hideChannel(channel);
	}
	
	public void removeChannel(int channel) {
		drawInterface.removeChannel(channel);
	}
	
 	public void startDrawing() {
		drawInterface.startDrawing();
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
 	
 	public BluetoothProtocol getBluetoothProtocol() {
 		return bluetoothProtocol;
 	}

 	private void onGoogleDriveFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		offlineStudyData.add(studyData);
 		drawInterface.addChannel(studyData, false); 	
 	}
 	
 	private void onLocalStorageFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		if(studyData.getSamplesBuffer().getSize() == 0) {
 			Toast.makeText(mainActivity, "El archivo no posee muestras", Toast.LENGTH_LONG).show();
 			return;
 		} else {
	 		offlineStudyData.add(studyData);
	 		drawInterface.addChannel(studyData, false);
	 	}
 	}
 	
 	private void onNewSamplesBatch(short[] samples, int channel) {
 		if(drawInterface.onlineDrawBuffersOk == true) drawInterface.draw(samples, channel);
		if(storageInterface.recording == true) storageInterface.saveSamplesBatch(onlineStudyData[channel], samples);
 	}
 	
 	private void onTotalAdcChannels(int totalAdcChannels) {
 		mTotalAdcChannels = totalAdcChannels;
 	}
 	
 	private void onAdcData(AdcData[] adcData) {
 		onlineStudyData = new StudyData[mTotalAdcChannels];
 		AcquisitionData acquisitionData;
 		SamplesBuffer samplesBuffer;
 		
 		for(int i = 0; i < mTotalAdcChannels; i++) {
 			onlineStudyData[i] = new StudyData();
 			
 			acquisitionData = new AcquisitionData(adcData[i]);
 			onlineStudyData[i].setAcquisitionData(acquisitionData);
 			
 			samplesBuffer = new SamplesBuffer(onlineStudyData[i].getAcquisitionData(), "");
 			onlineStudyData[i].setSamplesBuffer(samplesBuffer);
 		}

 		for(int i = 0; i < getTotalAdcChannels(); i++) {
			addChannel(i);
		}
		
		// Empiezo a dibujar
		startDrawing();
 		//mainActivity.onConfigurationOk();
 	}
 	
 	private void onGoogleDriveConnected() {
 	}
 	
 	private void onGoogleDriveSuspended() {
 		
 	}
 	
 	private void onGoogleDriveDisconnected() {
 		
 	}
 	
 	private void onGoogleDriveConnectionFailed(Message msg) {
 		
 	}
 	
	@SuppressLint("HandlerLeak")
	private final Handler mBluetoothProtocolHandler = new Handler() {
		
		// Método para manejar el mensaje
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			BluetoothProtocolMessage bluetoothProtocolMessage = BluetoothProtocolMessage.values(msg.what);
			
			switch (bluetoothProtocolMessage) {
				
				// Succesfully connected to Google Play Services
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
					
				default:
					break;
			
			}
		}
	};
		
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

}//Study.java
