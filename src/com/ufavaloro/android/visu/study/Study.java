/*****************************************************************************************
 * Study.java																			 *
 * Clase que administra todas las otras clases.											 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.study;

import java.util.ArrayList;

import android.content.IntentSender.SendIntentException;
import android.os.Handler;
import android.os.Message;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocol;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocolMessage;
import com.ufavaloro.android.visu.draw.DrawHelper;
import com.ufavaloro.android.visu.storage.StorageHelper;
import com.ufavaloro.android.visu.storage.StorageHelperMessage;
import com.ufavaloro.android.visu.storage.data.AcquisitionData;
import com.ufavaloro.android.visu.storage.data.AdcData;
import com.ufavaloro.android.visu.storage.data.StudyData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.DriveId;

public class Study {

/*****************************************************************************************
* Inicio de atributos de clase														     *
*****************************************************************************************/
	// Helpers
	public StorageHelper storage;
	public BluetoothProtocol bluetooth;
	public DrawHelper draw;

	private StudyData[] mStudyData;
	// Context de StudyActivity
	public StudyActivity studyActivity;
	
	// Canal Bluetooth asociado al estudio
	private int mBluetoothChannel;
	
	// Cantidad total de canales del estudio
	private int mTotalAdcChannels;
	
	private boolean mAdcDataOk = true;
	
	// Flags principales
	//
	// bluetoothHelper.connected
	// bluetoothHelper.totalAdcChannels
	//
	// drawHelper.onlineDrawBuffersOk
	//
	// storageHelper.driveManager.connected
	// storageHelper.sdManager.rootFoldersOk
	// storageHelper.sdManager.studyFoldersOk
	// storageHelper.sdManager.studyFilesOk
	// storageHelper.buffersOk
	// storageHelper.patientDataOk
	// storageHelper.recordingStarted

/*****************************************************************************************
* Inicio de métodos de clase														     *
*****************************************************************************************/

/*****************************************************************************************
* Métodos principales																     *
*****************************************************************************************/
	// Constructor
	public Study(StudyActivity mStudyActivity) {
		
		this.studyActivity = mStudyActivity;
		
		draw = (DrawHelper) mStudyActivity.findViewById(R.id.drawSurface);
		
		bluetooth = new BluetoothProtocol(mBluetoothProtocolHandler);

		storage = new StorageHelper(mStudyActivity, mStorageHelperHandler);
						
	}

/*****************************************************************************************
* Métodos de almacenamiento															     *
*****************************************************************************************/
 	// Método que genera un nuevo estudio Offline y, si estoy conectado al Drive, lo crea
	// también ahí
	public void newStudy(String patientName, String patientSurname, String studyName) {
 		 		
		// Creo buffers de almacenamiento
		createStorageBuffers();

		// Almaceno los datos del paciente en esos buffers
		storage.createPatientData(patientName, patientSurname, studyName);
		
		// Creo carpetas locales y en google drive
		storage.createStudyFolders();
		
		// Creo archivos .vis de los estudios
		storage.createLocalStudyFiles();
 	
 	}
 	
	public void saveStudyToGoogleDrive() {
		storage.createGoogleDriveStudyFiles();	
	}

	// Método para crear buffers de almacenamiento
	private void createStorageBuffers() {

		for(int i = 0; i < mStudyData.length; i++) {
			storage.createStorageBuffer(mStudyData[i]);
		}
	
		storage.buffersOk = true;

	}

	// Método para saber si estoy conectado a Google Drive
	public boolean googleDriveConnectionOk() {
		
		return storage.googleDrive.isConnected();
	
	}

	
	// Método para abrir un archivo desde Google Drive
	public void loadFileFromGoogleDrive(DriveId driveId) {
		storage.loadFileFromGoogleDrive(driveId);
	}

	public void loadFileFromLocalStorage(String filePath) {
		storage.loadFileFromLocalStorage(filePath);
	}
	
/*****************************************************************************************
* Métodos de Conexión Bluetooth														     *
*****************************************************************************************/
	// Método que agrega una conexión Bluetooth 
	public void newBluetoothConnection() {
		bluetooth.addBluetoothConnection();
	}
	
	// Método que informa el estado de la conexión
	public boolean connectedToRemoteDevice() {
		return bluetooth.getConnected();
	}
	
	// Getter de la cantidad de canales del adc
	public int getTotalAdcChannels() {	
		return bluetooth.getTotalAdcChannels();
	}
	
	// Método para obtener el dispositivo con el cual me conecté
	public String getRemoteDevice() {
		return bluetooth.getActualRemoteDevice();
	}

/*****************************************************************************************
* Métodos de Graficación															     *
*****************************************************************************************/
	
	// Método para crear buffers de graficación online
	public void addChannel(int channel) {
		
		if(channel >= getTotalAdcChannels()) return;
		
		draw.addChannel(mStudyData[channel]);

		draw.onlineDrawBuffersOk = true;
	
	}
	
	// Método para crear un buffer de graficación offline
	public void createOfflineChannel(StudyData studyData, ArrayList<Short> samples) {
		//draw.createOfflineDrawBuffer(studyData, "", samples);
	}

	public void removeChannel(int channel) {
		draw.removeChannel(channel);
	}
	
	public boolean buffersOk() {
		return draw.onlineDrawBuffersOk;
	}
	
	public boolean adcDataOk() {
		return mAdcDataOk;
	}
	
 	public void startDrawing() {
		draw.startDrawing();
	}

 	public void startRecording() { 		
 		draw.currentlyRecording = true;
 		storage.recording = true;
 	}

 	public void stopRecording() {		
 		draw.currentlyRecording = false;
		storage.recording = false;
 	}
 	
 	private void onGoogleDriveFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		createOfflineChannel(studyData, studyData.getDataBuffer());	
 	}
 	
 	private void onLocalStorageFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		createOfflineChannel(studyData, studyData.getDataBuffer());	
 	}
 	
 	private void onNewSamplesBatch(short[] samples, int channel) {
 		if(draw.onlineDrawBuffersOk == true) draw.draw(samples, channel);
		if(storage.recording == true) storage.saveSamplesBatch(samples, channel);
 	}
 	
 	private void onTotalAdcChannels(int totalAdcChannels) {
 		mTotalAdcChannels = totalAdcChannels;
 	}
 	
 	private void onAdcData(AdcData[] adcData) {
 		mStudyData = new StudyData[mTotalAdcChannels];
 		AcquisitionData acquisitionData;
 		
 		for(int i = 0; i < mTotalAdcChannels; i++) {
 			acquisitionData = new AcquisitionData(adcData[i]);
 			mStudyData[i] = new StudyData();
 			mStudyData[i].setAcquisitionData(acquisitionData);
 		}
 		
 		mAdcDataOk = true;
 		
 		studyActivity.onConfigurationOk();
 	}
 	
 	private void onGoogleDriveConnected() {
 		studyActivity.shortToast("Conectado a Google Drive");
 	}
 	
 	private void onGoogleDriveSuspended() {
 		
 	}
 	
 	private void onGoogleDriveDisconnected() {
 		
 	}
 	
 	private void onGoogleDriveConnectionFailed(Message msg) {
 
 		
 	}
 	
	private final Handler mBluetoothProtocolHandler = new Handler() {
		
		// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
		
			// Tipo de mensaje recibido
			BluetoothProtocolMessage bluetoothHelperMessage = BluetoothProtocolMessage.values(msg.what);
			
			switch (bluetoothHelperMessage) {
				
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
	
	private final Handler mStorageHelperHandler = new Handler() {
		
		// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			StorageHelperMessage storageHelperMessage = StorageHelperMessage.values(msg.what);
			
			switch (storageHelperMessage) {
				
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

}
