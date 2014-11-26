package com.ufavaloro.android.visu.storage.googledrive;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.ufavaloro.android.visu.storage.StorageBuffer;

public class GoogleDriveManager {
	
	GoogleDriveClient mGoogleDriveClient;
	Handler mHandler;
	
	public GoogleDriveManager(Activity contextActivity, Handler mHandler) {
		mGoogleDriveClient = new GoogleDriveClient(contextActivity, mGoogleDriveClientHandler);
		this.mHandler = mHandler;
	}
	
	// Gettr de GoogleApiClient
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleDriveClient.getGoogleApiClient();
	}

	public void loadFile(DriveId driveId) {
		mGoogleDriveClient.loadFile(driveId);
	}
	
	public void createStudyFolders(ArrayList<StorageBuffer> storageBuffers) {
		
		StorageBuffer storageBuffer = storageBuffers.get(0);
		
		// Obtengo nombre y apellido del paciente y genero carpeta
		String patientName = new String(storageBuffer.patientData.getPatientName());
		String patientSurname = new String(storageBuffer.patientData.getPatientSurname());
		String patient =  patientSurname + "_" + patientName;
		
		createPatientFolder(patient);
		
		
		// Obtengo fecha y creo carpeta
	    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		String date = day + "-" + month + "-" + year;
		
		createDateFolder(date);
		
		
		// Obtengo el nombre del estudio y creo carpeta
		String studyName = new String(storageBuffer.patientData.getStudyName());

		createStudyNameFolder(studyName);
		
	}
	
	private void createRootFolder() {

		DriveFolder parentFolder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
		String folderName = "Visualizador";
		
		mGoogleDriveClient.createFolder(folderName, parentFolder);
		
	}
	
	private void createStudiesFolder() {
		
		while(mGoogleDriveClient.isCreatingFolder());
		
		// Parent Folder = .Visualizador
		DriveFolder parentFolder = mGoogleDriveClient.getLastFolder();
			
		// Nombre de la carpeta
		String folderName = "Estudios";
			
		mGoogleDriveClient.createFolder(folderName, parentFolder);
	
	}
	
	public void createPatientFolder(String patient) {
		
		while(mGoogleDriveClient.isCreatingFolder());

		// Parent Folder = _Estudios
		DriveFolder parentFolder = mGoogleDriveClient.getLastFolder();
			
		// Nombre de la carpeta
		String folderName = patient;
			
		mGoogleDriveClient.createFolder(folderName, parentFolder);
		
	}
	
	public void createDateFolder(String date) {
		
		while(mGoogleDriveClient.isCreatingFolder());
		
		// Parent Folder = _Estudios
		DriveFolder parentFolder = mGoogleDriveClient.getLastFolder();
			
		// Nombre de la carpeta
		String folderName = date;
			
		mGoogleDriveClient.createFolder(folderName, parentFolder);
	
		
	}
	
	public void createStudyNameFolder(String studyName) {
		
		while(mGoogleDriveClient.isCreatingFolder());
		
		// Parent Folder = _Estudios
		DriveFolder parentFolder = mGoogleDriveClient.getLastFolder();
			
		// Nombre de la carpeta
		String folderName = studyName;
			
		mGoogleDriveClient.createFolder(folderName, parentFolder);
		
	}
	
	public void createStudyFiles(ArrayList<StorageBuffer> storageBuffers) {
		
		while(mGoogleDriveClient.isCreatingFile());
		
		// Parent Folder = _Estudios
		DriveFolder folder = mGoogleDriveClient.getLastFolder();
					
		for(int i = 0; i < storageBuffers.size(); i++) {
			
			while(mGoogleDriveClient.isCreatingFile());

			File file = storageBuffers.get(i).storageData.getStudyFile();
			String fileName = file.getName();
			
			FileInputStream fileInputStream;
			
			try {
			
				fileInputStream = new FileInputStream(file);
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				
				byte[] fileOutputBuffer = new byte[dataInputStream.available()];
				dataInputStream.readFully(fileOutputBuffer);
				
				dataInputStream.close();
				
				mGoogleDriveClient.createFile(fileOutputBuffer, fileName, folder);
			
			} catch (IOException e) {}
						
		}
	}
	
	private void onConnected() {
		mHandler.obtainMessage(GoogleDriveManagerMessage.GOOGLE_DRIVE_CONNECTED.getValue()).sendToTarget();
		createRootFolder();
		createStudiesFolder();
	}
	
	private void onDisconnected() {
		mHandler.obtainMessage(GoogleDriveManagerMessage.GOOGLE_DRIVE_DISCONNECTED.getValue()).sendToTarget();
		if(mGoogleDriveClient != null) {
			mGoogleDriveClient.connect();
		}
	}
	
	private void onConnectionFailed(Message msg) {
		mHandler.obtainMessage(GoogleDriveManagerMessage.GOOGLE_DRIVE_CONNECTION_FAILED.getValue()
							   , -1, -1, msg).sendToTarget();
	}
	
	public void connect() {
		mGoogleDriveClient.connect();
	}
	
	public void disconnect() {
		mGoogleDriveClient.disconnect();
	}
	
	public boolean isConnected() {
		return mGoogleDriveClient.isConnected();
	}
	
	private void onConnectionSuspended() {
		mHandler.obtainMessage(GoogleDriveManagerMessage.GOOGLE_DRIVE_SUSPENDED.getValue()).sendToTarget();
	}
	
	private void onFileOpen(Object object) {
		mHandler.obtainMessage(GoogleDriveManagerMessage.GOOGLE_DRIVE_FILE_OPEN.getValue(), -1, -1, object)
		  					   .sendToTarget();
	}
	
	// Handler para recibir las notificaciones de cuando se cree o abra un archivo/carpeta
	private final Handler mGoogleDriveClientHandler = new Handler() {
		
    	// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			GoogleDriveClientMessage googleDriveClientMessage = GoogleDriveClientMessage.values(msg.what);
			
			switch (googleDriveClientMessage) {
				
				// Succesfully connected to Google Play Services
				case CONNECTED:
					onConnected();
					break;
				
				// Disconnected from Google Play Services
				case DISCONNECTED:
					onDisconnected();
					break;
					
				// Connection to Google Play Services is suspended
				case CONNECTION_SUSPENDED:
					onConnectionSuspended();
					break;
				
				// Connection failed
				case CONNECTION_FAILED:
					onConnectionFailed(msg);
					
				// Folder created succesfully
				case FOLDER_CREATED:
					break;
					
				// Folder creaton failed
				case FOLDER_NOT_CREATED:
					break;
					
				// Folder already exists
				case FOLDER_ALREADY_EXISTS:
					break;
				
				// Folder already exists, but iterate 
				case FOLDER_ITERATE:
					
					break;
				// File created succesfully
				case FILE_CREATED:
					break;
					
				// File creation failed
				case FILE_NOT_CREATED:
					break;
				
				// File already exists
				case FILE_ALREADY_EXISTS:
					break;
				
				// File opened succesfully
				case FILE_OPENED:
					onFileOpen(msg.obj);
					break;
					
				// File opening failed
				case FILE_NOT_OPENED:
					break;
			
				// Default case
				default: 
					break;
			
			}//switch
		}
	};//mGoogleDriveHandler

}
