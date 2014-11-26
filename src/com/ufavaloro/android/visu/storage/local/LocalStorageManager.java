package com.ufavaloro.android.visu.storage.local;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.ufavaloro.android.visu.storage.StorageBuffer;
import com.ufavaloro.android.visu.storage.data.StorageData;

import android.os.Environment;

public class LocalStorageManager {
	
	// Flags
	public boolean rootFoldersOk = false;
	public boolean studyFoldersOk = false;
	public boolean studyFilesOk = false;
	
	// Tamaño total del header de los estudios en bytes
	private int mTotalHeaderBytes = 1024;
	
	// Método que crea las carpetas raíz
	public void createRootFolders() {
		
		if(checkExternalStorage() == false) return;
		
		// Voy a sdcard
		StorageData.sdCardFolder = Environment.getExternalStorageDirectory();

		// Voy a sdcard/Visualizador
		StorageData.rootFolderPath = StorageData.sdCardFolder.getPath() + "/.Visualizador";
		StorageData.rootFolder = new File(StorageData.rootFolderPath);
		
		// Si no existe la dirección, la creo
	    if(StorageData.rootFolder.exists() == false) { 
	    	StorageData.rootFolder.mkdir();
	    }

	    
		// Voy a sdcard/Visualizador/Estudios
	    StorageData.studiesFolderPath = StorageData.rootFolderPath + "/.Estudios";
	    StorageData.studiesFolder = new File(StorageData.studiesFolderPath);
		
	    // Si no existe la dirección, la creo
	    if(StorageData.studiesFolder.exists() == false) { 
	    	StorageData.studiesFolder.mkdir();
	    }
	    
	    rootFoldersOk = true;
	    
	}

	// Método que crea las carpetas de los estudio
	public void createStudyFolders(ArrayList<StorageBuffer> storageBuffers) {
		
		if(rootFoldersOk == false || checkExternalStorage() == false) return;

		StorageBuffer storageBuffer = storageBuffers.get(0);
		int totalStorageBuffers = storageBuffers.size();

		// Obtengo nombre del paciente
		String patientName = new String(storageBuffer.patientData.getPatientName());
		
		// Obtego apellido del paciente
		String patientSurname = new String(storageBuffer.patientData.getPatientSurname());
		
		// Obtengo el nombre del estudio
		String studyName = new String(storageBuffer.patientData.getStudyName());

		
		// Genero carpeta con el nombre y apellido del paciente
		String patientPath =  StorageData.studiesFolderPath + "/" + patientSurname + "_" + patientName;
		
	    File patientFolder = new File(patientPath);
	    if(patientFolder.exists() == false) {
	    	patientFolder.mkdir();
	    }
	    
	    
	    // Genero carpeta con la fecha
	    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		
		String datePath = patientPath + "/" + day + "-" + month + "-" + year;
		
		File dateFolder = new File(datePath);
		if(dateFolder.exists() == false) {
	    	dateFolder.mkdir();
	    }
				
		
		// Genero carpeta con el nombre del estudio
		String studyFolderPath = datePath + "/" + studyName + "_0";
		
		File studyFolder = new File(studyFolderPath);
		int j = 0;
		
		while(studyFolder.exists() == true) {
			j++;
			studyFolderPath = datePath + "/" + studyName + "_" + (j);
			studyFolder = new File(studyFolderPath);
		}
		
	    studyFolder.mkdir();

	    // Almaceno todas las carpetas y rutas en los buffers
	    for(int i = 0; i < totalStorageBuffers; i++) {
	    	
	    	storageBuffer = storageBuffers.get(i);
	    	
		    storageBuffer.storageData.setStudyFolder(studyFolder);
			storageBuffer.storageData.setStudyFolderPath(studyFolderPath);
			storageBuffer.storageData.setDateFolder(dateFolder);
			storageBuffer.storageData.setDateFolderPath(datePath);
		    storageBuffer.storageData.setPatientFolder(patientFolder);
		    storageBuffer.storageData.setPatientFolderPath(patientPath);
		    
	    }
	    
	    studyFoldersOk = true;
		
	}

	// Método que crea los archivos correspondientes a los estudios
	public void createStudyFiles(ArrayList<StorageBuffer> storageBuffers) {		
			
		if(studyFoldersOk == false || checkExternalStorage() == false) return;
		
		// Creo los archivos de almacenamiento
		for(int i = 0; i < storageBuffers.size(); i++) {
			
			StorageBuffer storageBuffer = storageBuffers.get(i);
			
			// Genero archivo de almacenamiento
	    	String studyFolderPathString = storageBuffer.storageData.getStudyFolderPath();
	    	String sensorString = new String(storageBuffer.acquisitionData.getSensor());
	    	String studyPath = studyFolderPathString + "/" + sensorString + "@Canal" + (i+1) + ".vis";
			
	    	File studyFile = new File(studyPath);
	
			// Escribo Header
			try {
				
				// Creo el archivo
				studyFile.createNewFile();
				
		    	// Obtengo tamaño del header
			    int headerBytes = storageBuffer.patientData.getPatientDataBytes()
			    				  + storageBuffer.acquisitionData.getAcquisitionDataBytes();
			    int paddingBytes = mTotalHeaderBytes - headerBytes;
			    
			    // Genero buffer temporal de tamaño headerBytes
				ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes + paddingBytes);
				
				// Almaceno nombre del paciente
				char[] patientName = storageBuffer.patientData.getPatientName();
				int patientNameSize = storageBuffer.patientData.getPatientNameSize();
				writeCharArray(byteBuffer, patientName, patientNameSize);
				
				// Almaceno apellido del paciente
				char[] patientSurname = storageBuffer.patientData.getPatientSurname();
				int patientSurnameSize = storageBuffer.patientData.getPatientSurnameSize();
				writeCharArray(byteBuffer, patientSurname, patientSurnameSize);
				
				// Almaceno nombre del estudio
				char[] studyName = storageBuffer.patientData.getStudyName();
				int studyNameSize = storageBuffer.patientData.getStudyNameSize();
				writeCharArray(byteBuffer, studyName, studyNameSize);
				
				// Almaceno nombre del sensor que me está enviando las muestras
				char[] sensor = storageBuffer.acquisitionData.getSensor();
				int sensorSize = storageBuffer.acquisitionData.getSensorSize();
				writeCharArray(byteBuffer, sensor, sensorSize);

				// Almaceno tipo de estudio
				char[] studyType = storageBuffer.acquisitionData.getStudyType();
				int studyTypeSize = storageBuffer.acquisitionData.getStudyTypeSize();
				writeCharArray(byteBuffer, studyType, studyTypeSize);

		    	// Almaceno Fs
				double fs = storageBuffer.acquisitionData.getFs();
				byteBuffer.putDouble(fs);
				
				// Almaceno Resolución
				int bits = storageBuffer.acquisitionData.getBits();
				byteBuffer.putInt(bits);
				
				// Almaceno VMax
				double vMax = storageBuffer.acquisitionData.getVMax();
				byteBuffer.putDouble(vMax);
				
				// Almaceno VMin
				double vMin = storageBuffer.acquisitionData.getVMin();
				byteBuffer.putDouble(vMin);
				
				// Almaceno AMax
				double aMax = storageBuffer.acquisitionData.getAMax();
				byteBuffer.putDouble(aMax);
	
				// Almaceno AMin
				double aMin = storageBuffer.acquisitionData.getAMin();
				byteBuffer.putDouble(aMin);
				
				// Almaceno cantidad de muestras
				double totalSamples = storageBuffer.acquisitionData.getTotalSamples();
				byteBuffer.putDouble(totalSamples);
				
				// Agrego padding de ceros hasta 1kb
				byte[] padding = new byte[paddingBytes];
				byteBuffer.put(padding);
				
				// Escribo todo en el archivo y guardo la dirección en el buffer
				writeHeaderToFile(studyFile, byteBuffer);
				storageBuffer.storageData.setStudyFile(studyFile);
				storageBuffer.storageData.setStudyFilePath(studyPath);
				
			} catch (IOException e) {}
	    }
		
		// Archivos OK
		studyFilesOk = true;

	}
			
	// Método que escribe el header un estudios
	private void writeHeaderToFile(File file, ByteBuffer header) {
		
		try {
			
			FileOutputStream fos = new FileOutputStream(file, true);
			
			fos.write(header.array());
			
			fos.close();
		
		} catch (IOException e) {}
	
	}
	
	// Método para escribir un array de caracteres en un byte buffer
	private void writeCharArray(ByteBuffer byteBuffer, char[] toWrite, int size) {
		
		char[] aux = new char[size];
		
		for(int i = 0; i < toWrite.length; i++) aux[i] = toWrite[i];
		
		for(int i = 0; i < size; i++) byteBuffer.putChar(aux[i]);
	}
	
	public byte[] loadFile(String filePath) {
	
		byte[] inputBuffer = null;
		
		try {
			
			FileInputStream fileInputStream = new FileInputStream(filePath);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);

			inputBuffer = new byte[dataInputStream.available()];
			
			dataInputStream.readFully(inputBuffer);
			
			dataInputStream.close();
						
		} catch (IOException e) {}
	
		return inputBuffer;

	}
	
	public void saveFile(File file, StorageBuffer storageBuffer) {
		
		// Si se puede guardar
		if(checkExternalStorage() == true) {
					
			// Obtengo archivo de almacenamiento y su tamaño
			File studyFile = storageBuffer.storageData.getStudyFile();
			int size = storageBuffer.getSize();
			
			// Obtengo buffer a almacenar
			short[] storingBuffer = storageBuffer.getBuffer();
			
			// Genero buffer temporal de tamaño size*sizeof(short)
			ByteBuffer byteBuffer = ByteBuffer.allocate(size*((Short.SIZE)/8));
							
			// Lleno el array de bytes con el buffer
			for(int i = 0; i < size; i++) byteBuffer.putShort(storingBuffer[i]);
			
			// Escribo
			writeSamples(studyFile, byteBuffer);
		} 

	}
	
	// Método que escribe las muestras en el archivo del estudio (append)
	private boolean writeSamples(File file, ByteBuffer storingBuffer) {
		
		// Posición del header (en bytes) donde está el double (8 bytes) de muestras totales
		int totalSamplesPointer = 644;
		
		// Cantidad de muestras anterior
		double previousSize;

		// Cantidad de muestras a agregar
		double size = storingBuffer.capacity()/2;
		byte[] sizeBytes = new byte[8];
		
		// Nueva cantidad de muestras
		double newSize;
		byte[] newSizeBytes = new byte[8];
		
		// Obtengo OutputStream e InputStream
		FileOutputStream fos;
		FileInputStream fis;
		RandomAccessFile raf;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate((int) (size*2));
		
		try {
			
			/*
			// Abro InputStream para obtener la cantidad de muestras almacenadas
			fis = new FileInputStream(file);
			fis.skip(totalSamplesPointer);
			for(int i = 0; i < 8; i++) sizeBytes[i] = (byte) fis.read();
			previousSize = byteArrayToDouble(sizeBytes);
			newSize = previousSize + size;
			newSizeBytes = doubleToByteArray(newSize);
			fis.close();
			
			// Abro RandomAccesFile en modo normal para sobreescribir la cantidad de muestras 
			fos = new FileOutputStream(file);
			raf = new RandomAccessFile(file, "rw");
			raf.seek(totalSamplesPointer);
			raf.write(newSizeBytes);
			fos.close();
			raf.close();
			*/
			
			
			// Abro en modo append para guardar las nuevas muestras
			fos = new FileOutputStream(file, true);
			
			fos.write(storingBuffer.array());
			fos.close();
			
		
		} catch (IOException e) { return false; }
		
		return true;
	}
	
	// Método que chequea si es posible leer/escribir en la memoria
	private boolean checkExternalStorage() {
		
	    // Chequeo estado 
	    String state = Environment.getExternalStorageState();
	    
	    // Comparo
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        
	    	return true;
	    } 
	    else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	       
	    	return false;
	    } 
	    else {
	      
	    	return false;
	    
	    }
	    
	}
	
	
}//SdManager
