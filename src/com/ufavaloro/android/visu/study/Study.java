/*****************************************************************************************
 * Study.java																			 *
 * Clase que administra todas las otras clases.											 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.study;

import java.util.ArrayList;

import android.content.IntentSender.SendIntentException;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.widget.Toast;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.UI.MainActivity;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocol;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocolMessage;
import com.ufavaloro.android.visu.draw.DrawHelper;
import com.ufavaloro.android.visu.storage.SamplesBuffer;
import com.ufavaloro.android.visu.storage.StorageHelper;
import com.ufavaloro.android.visu.storage.StorageHelperMessage;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;
import com.ufavaloro.android.visu.storage.datatypes.PatientData;
import com.ufavaloro.android.visu.storage.datatypes.StorageData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveId;

public class Study {

/*****************************************************************************************
* Inicio de atributos de clase														     *
*****************************************************************************************/
	// Helpers
	public StorageHelper storage;
	public BluetoothProtocol bluetooth;
	public DrawHelper draw;

	public StudyData[] onlineStudyData;
	public ArrayList<StudyData> offlineStudyData;
	
	// Context de StudyActivity
	private MainActivity mainActivity;
	
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
* Inicio de m�todos de clase														     *
*****************************************************************************************/

/*****************************************************************************************
* M�todos principales																     *
*****************************************************************************************/
	// Constructor
	public Study(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		draw = (DrawHelper) mainActivity.findViewById(R.id.drawSurface);
		bluetooth = new BluetoothProtocol(mBluetoothProtocolHandler);
		storage = new StorageHelper(mainActivity, mStorageHelperHandler);	
		
		offlineStudyData = new ArrayList<StudyData>();
	}

/*****************************************************************************************
* M�todos de almacenamiento															     *
*****************************************************************************************/
 	// M�todo que genera un nuevo estudio Offline y, si estoy conectado al Drive, lo crea
	// tambi�n ah�
	public void newStudy(String patientName, String patientSurname, String studyName
						 , SparseArray<Integer> channelsToStore) {
 		 		
		PatientData patientData = new PatientData(patientName, patientSurname, studyName);

		for(int i = 0; i < channelsToStore.size(); i++) {
			int index = channelsToStore.valueAt(i);
			onlineStudyData[index].setMarkedForStoring(true);
		}
		
		for(int i = 0; i < onlineStudyData.length; i++) {
			onlineStudyData[i].setPatientData(patientData);
			draw.getChannelList().getChannelAtIndex(i).getStudyData().setPatientData(patientData);
		}
		
		
		// Creo carpetas locales y en google drive
		storage.createStudyFolders(onlineStudyData);
		
		// Creo archivos .vis de los estudios
		storage.createLocalStudyFiles(onlineStudyData);
 	
 	}
 	
	public void saveStudyToGoogleDrive() {
		storage.createGoogleDriveStudyFiles(onlineStudyData);	
	}

	public void setStudyType(int studyType, int channel) {
		onlineStudyData[channel].getAcquisitionData().setStudyType(studyType);
	}
	
	// M�todo para saber si estoy conectado a Google Drive
	public boolean googleDriveConnectionOk() {
		
		return storage.googleDrive.isConnected();
	
	}

	// M�todo para abrir un archivo desde Google Drive
	public void loadFileFromGoogleDrive(DriveId driveId) {
		storage.loadFileFromGoogleDrive(driveId);
	}

	public void loadFileFromLocalStorage(String filePath) {
		storage.loadFileFromLocalStorage(filePath);
	}
	
/*****************************************************************************************
* M�todos de Conexi�n Bluetooth														     *
*****************************************************************************************/
	// M�todo que agrega una conexi�n Bluetooth 
	public void newBluetoothConnection() {
		bluetooth.addBluetoothConnection();
	}
	
	// M�todo que informa el estado de la conexi�n
	public boolean connectedToRemoteDevice() {
		return bluetooth.getConnected();
	}
	
	// Getter de la cantidad de canales del adc
	public int getTotalAdcChannels() {	
		return bluetooth.getTotalAdcChannels();
	}
	
	// M�todo para obtener el dispositivo con el cual me conect�
	public String getRemoteDevice() {
		return bluetooth.getActualRemoteDevice();
	}

/*****************************************************************************************
* M�todos de Graficaci�n															     *
*****************************************************************************************/
	// M�todo para crear buffers de graficaci�n online
	public void addChannel(int channel) {
		
		if(channel >= getTotalAdcChannels()) return;
		
		draw.addChannel(onlineStudyData[channel], true);

		draw.onlineDrawBuffersOk = true;
	
	}
	
	public void hideChannel(int channel) {
		draw.hideChannel(channel);
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
 		offlineStudyData.add(studyData);
 		draw.addChannel(studyData, false); 	
 	}
 	
 	private void onLocalStorageFileOpened(Object object) {
 		StudyData studyData = (StudyData) object;
 		if(studyData.getSamplesBuffer().getSize() == 0) {
 			Toast.makeText(mainActivity, "El archivo no posee muestras", Toast.LENGTH_LONG).show();
 			return;
 		} else {
	 		offlineStudyData.add(studyData);
	 		draw.addChannel(studyData, false);
	 	}
 	}
 	
 	private void onNewSamplesBatch(short[] samples, int channel) {
 		if(draw.onlineDrawBuffersOk == true) draw.draw(samples, channel);
		if(storage.recording == true) storage.saveSamplesBatch(onlineStudyData[channel], samples);
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
 		
 		mAdcDataOk = true;
 		
 		mainActivity.onConfigurationOk();
 	}
 	
 	private void onGoogleDriveConnected() {
 		mainActivity.shortToast("Conectado a Google Drive");
 	}
 	
 	private void onGoogleDriveSuspended() {
 		
 	}
 	
 	private void onGoogleDriveDisconnected() {
 		
 	}
 	
 	private void onGoogleDriveConnectionFailed(Message msg) {
 
 		
 	}
 	
	private final Handler mBluetoothProtocolHandler = new Handler() {
		
		// M�todo para manejar el mensaje
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
		
		// M�todo para manejar el mensaje
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
