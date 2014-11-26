/*****************************************************************************************
 * StorageHelper.java																	 *
 * Clase que administra el almacenamiento de datos en el disco local y en Google Drive	 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.ufavaloro.android.visu.bluetooth.BluetoothProtocolMessage;
import com.ufavaloro.android.visu.storage.data.AcquisitionData;
import com.ufavaloro.android.visu.storage.data.AdcData;
import com.ufavaloro.android.visu.storage.data.DataConversion;
import com.ufavaloro.android.visu.storage.data.PatientData;
import com.ufavaloro.android.visu.storage.data.StudyData;
import com.ufavaloro.android.visu.storage.data.StudyDataParser;
import com.ufavaloro.android.visu.storage.googledrive.GoogleDriveManager;
import com.ufavaloro.android.visu.storage.googledrive.GoogleDriveManagerMessage;
import com.ufavaloro.android.visu.storage.local.LocalStorageManager;
import com.ufavaloro.android.visu.study.Study;
import com.ufavaloro.android.visu.study.StudyActivity;

public class StorageHelper {
	
/*****************************************************************************************
* Inicio de atributos de clase		 									   				 *
*****************************************************************************************/
	// Lista de los buffers de almacenamiento
	public ArrayList<StorageBuffer> mStorageBuffers = new ArrayList<StorageBuffer>();
	private int mTotalStorageBuffers;	

	int mGoogleDriveFolderIterator = 0;
	
	// Flag de buffers creados correctamente
	public boolean buffersOk = false;
	
	public boolean recording = false;
	
	// Flag de información del paciente almacenada
	public boolean patientDataOk = false;

	public LocalStorageManager local;
	public GoogleDriveManager googleDrive;
	
	public Study study;
	
	private Handler mHandler;

/*****************************************************************************************
* Inicio de métodos de clase			        									     *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales					        									     *
*****************************************************************************************/
	// Constructor
	public StorageHelper(StudyActivity studyActivity, Handler handler) {
		
		mHandler = handler;
		
		// Creo lista de buffers de almacenamiento
		mStorageBuffers = new ArrayList<StorageBuffer>();	
		
		// Creo managers
		local = new LocalStorageManager();
		googleDrive = new GoogleDriveManager(studyActivity, mGoogleDriveManagerHandler);
		
		// Creo carpetas raíz locales
		local.createRootFolders();
	}
	
	// Método que crea un buffer de almacenamiento
	public void createStorageBuffer(StudyData studyData) {
		
		StorageBuffer storageBuffer = new StorageBuffer(studyData.getAcquisitionData(), "");
		
		mStorageBuffers.add(storageBuffer);
		
		mTotalStorageBuffers++;
		
	}

	// Método que almacena los datos del paciente en cada buffer
	public void createPatientData(String patientName, String patientSurname, String studyName) {
		
		if(mStorageBuffers.size() == 0) return;
		
		// Seteo los parámetros en los buffers
		for(int i = 0; i < mTotalStorageBuffers; i++) {
			
			StorageBuffer buffer = mStorageBuffers.get(i);
			
			buffer.patientData.setPatientName(patientName);
			buffer.patientData.setPatientSurname(patientSurname);
			buffer.patientData.setStudyName(studyName);
			
			buffer.storageData.setNewStudyOk(true);
			
		}

		patientDataOk = true;
		
	}
	
	// Método que crea los archivos correspondientes a los estudios
	public void createLocalStudyFiles() {		
		local.createStudyFiles(mStorageBuffers);
	}

	public void createGoogleDriveStudyFiles() {
		googleDrive.createStudyFiles(mStorageBuffers);
	}

	public void createStudyFolders() {
		local.createStudyFolders(mStorageBuffers);		
		googleDrive.createStudyFolders(mStorageBuffers);
	}
	
/*****************************************************************************************
* Métodos de lectura/escritura sobre estudios existente    							     *
*****************************************************************************************/

	// Método que lee muestras de un archivo
	public void loadFileFromLocalStorage(String filePath) {
				
		byte[] fileInputBuffer = local.loadFile(filePath);
		
		StudyData studyData = StudyDataParser.getStudyData(fileInputBuffer);
		
		// Informo
		mHandler.obtainMessage(StorageHelperMessage.LOCAL_STORAGE_FILE_OPENED.getValue() 
							   ,-1, -1, studyData).sendToTarget();
		
	}

	public void loadFileFromGoogleDrive(DriveId driveId) {
		googleDrive.loadFile(driveId);
	}
	
	private void onGoogleDriveFileOpen(Object obj) {
		
		StudyData studyData = StudyDataParser.getStudyData((byte[]) obj);

		// Informo
		mHandler.obtainMessage(StorageHelperMessage.GOOGLE_DRIVE_FILE_OPENED.getValue() 
							   ,-1, -1, studyData).sendToTarget();
		
	}
	
	private void onGoogleDriveConnected() {
		
	}
	
	private void onGoogleDriveSuspended() {
		
	}
	
	private void onGoogleDriveDisconnected() {
		
	}
	
	private void onGoogleDriveConnectionFailed(Message msg) {
		mHandler.obtainMessage(StorageHelperMessage.GOOGLE_DRIVE_CONNECTION_FAILED.getValue()
							   , -1, -1, msg).sendToTarget();
	}
	
	// Método que recibe el paquete de muestras, lo procesa y lo manda a almacenar con writeSamples
	public boolean saveSamplesBatch(short[] toStore, int channel) {
		
		if(local.studyFilesOk == false || recording == false) return false;
		
		StorageBuffer storageBuffer = mStorageBuffers.get(channel);
		storageBuffer.storeSamples(toStore);
		boolean success = false;
		
		// Llené el buffer?
		int indexOverflow = storageBuffer.getStoringIndex();

		if(indexOverflow == 0) {
			
			File studyFile = storageBuffer.storageData.getStudyFile();
			local.saveFile(studyFile, storageBuffer);
			
		}
		
		return success;
	}

/*****************************************************************************************
* Métodos auxiliares																     *
*****************************************************************************************/
	private final Handler mGoogleDriveManagerHandler = new Handler() {
		
    	// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			GoogleDriveManagerMessage googleDriveManagerMessage = GoogleDriveManagerMessage.values(msg.what);
			
			switch (googleDriveManagerMessage) {
				
				// Succesfully connected to Google Play Services
				case GOOGLE_DRIVE_FILE_OPEN:
					onGoogleDriveFileOpen(msg.obj);
					break;
				
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

				default:
					break;
			}
		}
	};

}//StorageHelper
