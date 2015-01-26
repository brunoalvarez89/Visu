/*****************************************************************************************
 * GoogleDriveClient.java																 *
 * Clase que administra algunas funciones básicas de Google Drive	 					 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage.googledrive;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;


public class GoogleDriveClient implements 
com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, 
com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {

	// Cliente para las APIs de Google
	public GoogleApiClient mGoogleApiClient;
	
	// Flag de archivo creado
	public boolean createdFileOk;
	
	private Handler mHandler;
	private CreateFolderThread mCreateFolderThread;
	private CreateFileThread mCreateFileThread;
	private LoadFileThread mLoadFileThread;
	
	private int mFolderIterator = 0;
	private Activity mContextActivity;
	private ArrayList<DriveFolder> mFolderList = new ArrayList<DriveFolder>();
	
/*****************************************************************************************
* Inicio de métodos de clase		 									   				 *
*****************************************************************************************/
	// Constructor
	public GoogleDriveClient(Activity mContextActivity, Handler mGoogleDriveHandler) {	
	
		this.mHandler = mGoogleDriveHandler;
		this.mContextActivity = mContextActivity;
		
		connect();
	
	}
	
	// Método para conectarse con Drive
	public void connect() {

		// Genero objeto GoogleApiClient
		if (mGoogleApiClient == null) {

	    	mGoogleApiClient = new GoogleApiClient.Builder(mContextActivity)
            .addApi(Drive.API)
            .addScope(Drive.SCOPE_FILE)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
	    	
    	}

        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
		
	}

	// Método para desconectarse de Drive
	public void disconnect() {
		
		if(mGoogleApiClient != null) { 
			
			mGoogleApiClient.disconnect();
					
		}
		
	}
	
	// Método que responde a una conexión fallida
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if(result.hasResolution()) {
			try {
				result.startResolutionForResult(mContextActivity, 1001);
			} catch (SendIntentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//mHandler.obtainMessage(GoogleDriveClientMessage.CONNECTION_FAILED.getValue()
				//				   , -1, -1, result.getErrorCode()).sendToTarget();
		}
	}

	// Método que responde a una conexón exitosa
	@Override
	public void onConnected(Bundle arg0) {
		mHandler.obtainMessage(GoogleDriveClientMessage.CONNECTED.getValue())
										  .sendToTarget();
	}

	// Método que responde a una conexión suspendida
	@Override
	public void onConnectionSuspended(int arg0) {
		mHandler.obtainMessage(GoogleDriveClientMessage.CONNECTION_SUSPENDED.getValue())
										  .sendToTarget();
	}
	
	public synchronized void createFolder(String folderName, DriveFolder parentFolder) {
		
		if(!isConnected()) return;
		
		mCreateFolderThread = new CreateFolderThread(folderName, parentFolder, false);
		mCreateFolderThread.start();
		
	}	
	
	public synchronized void createIteratingFolder(String folderName, DriveFolder parentFolder) {
		
		if(!isConnected()) return;
		

		// Instancio thread
		//Thread.State.TERMINATED
		mCreateFolderThread = new CreateFolderThread(folderName + "_" + mFolderIterator
													 , parentFolder, true);
		mCreateFolderThread.start();
		
	}

	// Método que genera un Thread para generar un archivo
	public synchronized void createFile(byte[] fileOutputBuffer, String fileName, DriveFolder folder) {
		
		if(!isConnected()) return;
		
		mCreateFileThread = new CreateFileThread(fileOutputBuffer, fileName, folder);
		mCreateFileThread.start();
		
	}

	// Método para abrir un archivo
	public synchronized void loadFile(DriveId driveId) {
		
		if(!isConnected()) return;
		
		mLoadFileThread = new LoadFileThread(driveId);
		mLoadFileThread.start();
		
	}

	public boolean isCreatingFolder() {
		if(mCreateFolderThread != null) {
			if(mCreateFolderThread.getState() != Thread.State.TERMINATED) return true;
		}
		return false;
	}
	
	public boolean isCreatingFile() {
		if(mCreateFileThread != null) {
			if(mCreateFileThread.getState() != Thread.State.TERMINATED) return true;
		}
		return false;
	}
	
	public boolean isOpeningFile() {
		if(mLoadFileThread != null) {
			if(mLoadFileThread.getState() != Thread.State.TERMINATED) return true;
		}
		return false;
	}
	
	public DriveFolder getLastFolder() {
		int folderQty = mFolderList.size();
		return mFolderList.get(folderQty - 1);
	}
	
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public boolean isConnected() {
		return mGoogleApiClient.isConnected();
	}
	
	// Thread para crear una carpeta
	private class CreateFolderThread extends Thread {
		
		private String mmFolderName;
		private DriveFolder mmParentFolder;
		private boolean mmIterate;
		
		// Constructor de clase
		public CreateFolderThread(String mmFolderName, DriveFolder mmParentFolder, boolean mmIterate) {		
			this.mmFolderName = mmFolderName;
			this.mmParentFolder = mmParentFolder;
			this.mmIterate = mmIterate;
		}
		
		// Thread.run()
		public void run() {
			
			MetadataBufferResult metadataBufferResult = checkIfFolderExists(mmFolderName, mmParentFolder);
			
			// Si hubo un problema, salta la ficha acá
			if(!metadataBufferResult.getStatus().isSuccess()) {
				mHandler.obtainMessage(GoogleDriveClientMessage.FOLDER_NOT_CREATED.getValue(), -1, -1
						  						  ,-1).sendToTarget();
			}
			
			MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();
			
			// La carpeta no existe!
			if(metadataBuffer.getCount() == 0) { 

				MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(mmFolderName).build();
	            
				DriveFolderResult folderResult = mmParentFolder.createFolder(mGoogleApiClient, changeSet).await();
								
				mFolderList.add(folderResult.getDriveFolder());
				
				mHandler.obtainMessage(GoogleDriveClientMessage.FOLDER_CREATED.getValue())
												 .sendToTarget();
			
				mFolderIterator = 0;
			// La carpeta ya existe!
			} else {
				
				Metadata metadata = metadataBuffer.get(0);
				DriveId folderId = metadata.getDriveId();
				DriveFolder driveFolder = Drive.DriveApi.getFolder(mGoogleApiClient, folderId);
			
				if(mmIterate == true) {
					mFolderIterator++;
					int indexOfScore = mmFolderName.indexOf('_');
					String folderName = mmFolderName.substring(0, indexOfScore);
					createIteratingFolder(folderName, mmParentFolder);
					mHandler.obtainMessage(GoogleDriveClientMessage.FOLDER_ITERATE.getValue())
	  						  						  .sendToTarget();
					
				} else {				
					mFolderList.add(driveFolder);
					mHandler.obtainMessage(GoogleDriveClientMessage.FOLDER_ALREADY_EXISTS.getValue())
												      .sendToTarget();
				}

			}			

		
		}
		
		// Chequeo si la carpeta existe
		private MetadataBufferResult checkIfFolderExists(String folderName, DriveFolder parentFolder) {
			
			// Genero filtro para ver si ya existe la carpeta
			Filter filter = Filters.eq(SearchableField.TITLE, folderName);
			
			// Hago query al Drive
			Query query = new Query.Builder().addFilter(filter).build();
			MetadataBufferResult metadataBufferResult = parentFolder.queryChildren(mGoogleApiClient, query)
													   			   .await();
			return metadataBufferResult;
		}
	
	}//CreateFolderThread
	
	// Thread para crear un archivo
	private class CreateFileThread extends Thread {
		
		private byte[] mmFileOutputBuffer;
		private String mmFileName;
		private DriveFolder mmFolder;
		
		// Constructor de clase
		public CreateFileThread(byte[] mmFileOutputBuffer, String mmFileName, DriveFolder mmFolder) {
			this.mmFileOutputBuffer = mmFileOutputBuffer;
			this.mmFileName = mmFileName;
			this.mmFolder = mmFolder;
		}
		
		// Thread.run()
		public void run() {
				
			// Genero instancia remota de nuevo contenido
			DriveContentsResult result = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
			
			if(!result.getStatus().isSuccess()) return;
			
			// Escribo en la instancia
			try {

				// Obtengo outpustream para escribir en la instancia remota
				OutputStream outputStream = result.getDriveContents().getOutputStream();
				
				// Escribo buffer
				outputStream.write(mmFileOutputBuffer);

				// Cierro stream
				outputStream.close();
				
		   } catch (IOException e1) {}
		   
			// Genero changeSet de metadata
			MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
												  .setTitle(mmFileName)
												  .build();
		
			// Genero archivo binario remoto
			DriveFileResult fileResult = mmFolder.createFile(mGoogleApiClient, metadataChangeSet, 
														  result.getDriveContents()).await();
			
			if(!fileResult.getStatus().isSuccess()) return;
			
			mHandler.obtainMessage(GoogleDriveClientMessage.FILE_CREATED.getValue())
				  							  .sendToTarget();

		
		}
	
	}//CreateFileThread
	
	// Thread para abrir un archivo
	private class LoadFileThread extends Thread {
		
		private DriveId mmDriveId;
		
		// Constructor de clase
		public LoadFileThread(DriveId mmDriveId) {
			this.mmDriveId = mmDriveId;
		}
		
		// Thread.run()
		public void run() {
		
			DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mmDriveId);
			
			DriveContentsResult driveContentsResult = driveFile.open(mGoogleApiClient, 
													  DriveFile.MODE_READ_ONLY, null).await();
			
			if (!driveContentsResult.getStatus().isSuccess()) return;
	        
	        DriveContents driveContents = driveContentsResult.getDriveContents();
	       
	        InputStream inputStream = driveContents.getInputStream();
	      
	        DataInputStream dataInputStream = new DataInputStream(inputStream);
	        
	        byte[] fileInputBuffer = null;
			
	        
			try {
	    	
				fileInputBuffer = new byte[dataInputStream.available()];
				dataInputStream.readFully(fileInputBuffer);
					
				// Cierro stream
				dataInputStream.close();
				
				// Descarto modificaciones por si las dudas
	            driveContents.discard(mGoogleApiClient);

			} catch (IOException e) {
				mHandler.obtainMessage(GoogleDriveClientMessage.FILE_NOT_OPENED.getValue(), -1, -1)
												  .sendToTarget();
			}
			
	        mHandler.obtainMessage(GoogleDriveClientMessage.FILE_OPENED.getValue(), -1, -1
	        								  , fileInputBuffer)
				  							  .sendToTarget();

		}
		
	
	}//OpenFileThread

}// DriveManager
