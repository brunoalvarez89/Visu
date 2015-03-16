/*****************************************************************************************
 * StorageInterface.java																	 *
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

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.ufavaloro.android.visu.connection.ProtocolMessage;
import com.ufavaloro.android.visu.main.MainInterface;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;
import com.ufavaloro.android.visu.storage.datatypes.PatientData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.ufavaloro.android.visu.storage.googledrive.GoogleDriveInterface;
import com.ufavaloro.android.visu.storage.googledrive.GoogleDriveInterfaceMessage;
import com.ufavaloro.android.visu.storage.local.LocalStorageInterface;
import com.ufavaloro.android.visu.userinterface.MainActivity;

public class StorageInterface {
		
/*****************************************************************************************
* Inicio de atributos de clase		 									   				 *
*****************************************************************************************/
	int mGoogleDriveFolderIterator = 0;
	
	// Flag de buffers creados correctamente
	public boolean buffersOk = false;
	
	public boolean recording = false;
	
	// Flag de información del paciente almacenada
	public boolean patientDataOk = false;

	public LocalStorageInterface local;
	public GoogleDriveInterface googleDrive;
	//public StudyData[] studyData;
	
	public MainInterface study;
	
	private Handler mHandler;

/*****************************************************************************************
* Inicio de métodos de clase			        									     *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales					        									     *
*****************************************************************************************/
	// Constructor
	public StorageInterface(Activity contextActivity, Handler handler) {
		
		mHandler = handler;
		
		// Creo managers
		local = new LocalStorageInterface();
		googleDrive = new GoogleDriveInterface(contextActivity, mGoogleDriveInterfaceHandler);
		
		// Creo carpetas raíz locales
		local.createRootFolders();
	}
	
	// Método que crea los archivos correspondientes a los estudios
	public void createLocalStudyFiles(StudyData[] studyData) {		
		local.createStudyFiles(studyData);
	}

	public void createGoogleDriveStudyFiles(StudyData[] studyData) {
		googleDrive.createStudyFiles(studyData);
	}

	public void createStudyFolders(StudyData[] studyData) {
		local.createStudyFolders(studyData);		
		googleDrive.createStudyFolders(studyData);
	}
	
/*****************************************************************************************
* Métodos de lectura/escritura sobre estudios existente    							     *
*****************************************************************************************/

	// Método que lee muestras de un archivo
	public void loadFileFromLocalStorage(String filePath) {
				
		byte[] fileInputBuffer = local.loadFile(filePath);
		
		StudyData studyData = StudyDataParser.getStudyData(fileInputBuffer);
		
		// Informo
		mHandler.obtainMessage(StorageInterfaceMessage.LOCAL_STORAGE_FILE_OPENED.getValue() 
							   ,-1, -1, studyData).sendToTarget();
		
	}

	public void loadFileFromGoogleDrive(DriveId driveId) {
		googleDrive.loadFile(driveId);
	}
	
	private void onGoogleDriveFileOpen(Object obj) {
		
		StudyData studyData = StudyDataParser.getStudyData((byte[]) obj);

		// Informo
		mHandler.obtainMessage(StorageInterfaceMessage.GOOGLE_DRIVE_FILE_OPENED.getValue() 
							   ,-1, -1, studyData).sendToTarget();
		
	}
	
	private void onGoogleDriveConnected() {
		
	}
	
	private void onGoogleDriveSuspended() {
		
	}
	
	private void onGoogleDriveDisconnected() {
		
	}
	
	private void onGoogleDriveConnectionFailed(Message msg) {
		mHandler.obtainMessage(StorageInterfaceMessage.GOOGLE_DRIVE_CONNECTION_FAILED.getValue()
							   , -1, -1, msg).sendToTarget();
	}
	
	// Método que recibe el paquete de muestras, lo procesa y lo manda a almacenar con writeSamples
	public boolean setSamples(StudyData studyData, short[] toStore) {
		
		if(local.studyFilesOk == false || recording == false || studyData.isMarkedForStoring() == false) return false;
		
		SamplesBuffer samplesBuffer = studyData.getSamplesBuffer();
		samplesBuffer.writeSamples(toStore);
		boolean success = false;
		
		// Llené el buffer?
		int indexOverflow = samplesBuffer.getStoringIndex();

		if(indexOverflow == 0) {
			
			File studyFile = studyData.getStorageData().getStudyFile();
			local.saveFile(studyFile, samplesBuffer);
			
		}
		
		return success;
	}

/*****************************************************************************************
* Métodos auxiliares																     *
*****************************************************************************************/
	private final Handler mGoogleDriveInterfaceHandler = new Handler() {
		
    	// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			GoogleDriveInterfaceMessage googleDrivInterfaceMessage = GoogleDriveInterfaceMessage.values(msg.what);
			
			switch (googleDrivInterfaceMessage) {
				
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
