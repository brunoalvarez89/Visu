/*****************************************************************************************
 * StorageData.java																		 *
 * Clase que posee informaci�n de las direcciones de almacenamiento.					 *
 * 																						 *
 * Los archivos se almacenan con la siguiente estructura de ruta:						 *
 * 																						 *
 * sdcard/Visualizador/Estudios/Nombre_Paciente/Fecha/Nombre_Estudio_#/Sensor@Canal.vis  *
 * 																						 *	
 * y con la siguiente estructura de datos binarios:										 *
 * 																						 *																					 *
 * - Header -																			 *
 * Nombre (50 chars = 100 bytes)														 *
 * Apellido (50 chars = 100 bytes)														 *
 * Nombre_Estudio (100 chars = 200 bytes)												 *
 * Nombre_Dispositivo_Adquisidor (50 chars = 100 bytes)									 *
 * Tipo_Estudio (50 chars = 100 bytes)													 *
 * Frecuencia_Muestreo (1 double = 8 bytes)												 *
 * Resoluci�n (1 int = 4 bytes)															 *
 * Voltaje_M�ximo (1 double = 8 bytes)													 *
 * Voltaje_M�nimo (1 double = 8 bytes)													 *
 * Amplitud_M�xima (1 double = 8 bytes)													 *
 * Amplitud_M�nima (1 double = 8 bytes)													 *
 * Cantidad_De_Muestras (1 double = 8 bytes)											 *
 * - Fin Header-																		 *
 * Muestras																				 *																					 *
 * EOF					  																 *
 * 																						 *
 * Tama�o total del Header = 100 + 100 + 200 + 100 + 100 + 8 + 4 + 8 + 8 + 8 + 8 + 8	 *
 * 						   = 652 bytes													 *	
 * 																						 *
 * Posici�n del puntero a cantidad de muestras = Byte 644								 *
 * 																						 *
 * Se agrega padding de ceros al header para que tenga un tama�o de 1024 bytes			 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.storage.datatypes;

import java.io.File;

public class StorageData {

	// Boolean para saber si tengo toda la info necesaria para almacenar
	private boolean mNewStudyDialogOk;

	// sdcard
	public static File sdCardFolder;
	
	// sdcard/Visualizador
	public static File rootFolder;
	public static String rootFolderPath;
	
	// sdcard/Visualizador/Estudios   
	public static File studiesFolder;
	public static String studiesFolderPath;
	
	// sdcard/Visualizador/Estudios/Nombre_Paciente
	private File mPatientFolder;
	private String mPatientFolderPath;
	
	// sdcard/Visualizador/Estudios/Nombre_Paciente/Fecha
	private File mDateFolder;
	private String mDateFolderPath;
	
	// sdcard/Visualizador/Estudios/Nombre_Paciente/Fecha/Nombre_Estudio
	private File mStudyFolder;
	private String mStudyFolderPath;
	
	// sdcard/Visualizador/Estudios/Nombre_Paciente/Fecha/Nombre_Estudio/archivo.txt
	private File mStudyFile;
	private String mStudyFilePath;
	
	// Puntero la cantidad de muestras almacenadas del header del archivo
	private static int mTotalSamplesPointer = 644;
	
	
	public int getTotalSamplesPointer() {
		return mTotalSamplesPointer;
	}
	
	public void setStudyFile(File mStudyFile) {
		this.mStudyFile = mStudyFile;
	}
	
	public File getStudyFile() {
		return mStudyFile;
	}
	
	public String getStudyFolderPath() {
		return mStudyFolderPath;
	}
	
	public void setStudyFolderPath(String mStudyFolderPath) {
		this.mStudyFolderPath = mStudyFolderPath;
	}
	
	public void setStudyFolder(File mStudyFolder) {
		this.mStudyFolder = mStudyFolder;
	}
	
	public File getStudyFolder() {
		return mStudyFolder;
	}
	
	public String getDateFolderPath() {
		return mDateFolderPath;
	}
	
	public void setDateFolderPath(String mDateFolderPath) {
		this.mDateFolderPath = mDateFolderPath;
	}
	
	public void setDateFolder(File mDateFolder) {
		this.mDateFolder = mDateFolder;
	}
	
	public File getDateFolder() {
		return mDateFolder;
	}
	
	public void setPatientFolderPath(String mPatientFolderPath) {
		this.mPatientFolderPath = mPatientFolderPath;
	}
	
	public String getPatientFolderPath() {
		return mPatientFolderPath;
	}
	
	public void setNewStudyOk(boolean mNewStudyDialogOk) {
		this.mNewStudyDialogOk = mNewStudyDialogOk;
	}
	
	public void setStudyFilePath(String mStudyFilePath) {

		this.mStudyFilePath = mStudyFilePath;
	}
	
	public void setPatientFolder(File mPatientFolder) {
		this.mPatientFolder = mPatientFolder;
	}
	
	public File getPatientFolder() {
		return mPatientFolder;
	}
	
	public boolean getNewStudyDialogOk() {
		return mNewStudyDialogOk;
	}

	public String getStudyFilePath() {
		return mStudyFilePath;
	}


}
