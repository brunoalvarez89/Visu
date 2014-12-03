package com.ufavaloro.android.visu.storage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.storage.datatypes.AdcData;
import com.ufavaloro.android.visu.storage.datatypes.PatientData;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

public class StudyDataParser {
	
	public static StudyData getStudyData(byte[] fileInputBuffer) {
	
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileInputBuffer);
		DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		
		AcquisitionData acquisitionData = null;
		PatientData patientData = null;
		ArrayList<Short> samples = null;
		
		try {
			
		// Leo PatientData
		// Obtengo nombre del paciente
		byte[] patientNameBytes = new byte[50*((Character.SIZE)/8)];
		dataInputStream.read(patientNameBytes);
		char[] patientName = DataConversion.byteArrayToCharArray(patientNameBytes);
		
		// Obtengo apellido del paciente
		byte[] patientSurnameBytes = new byte[50*((Character.SIZE)/8)];
		dataInputStream.read(patientSurnameBytes);
		char[] patientSurname = DataConversion.byteArrayToCharArray(patientSurnameBytes);
		
		// Obtengo nombre del estudio
		byte[] studyNameBytes = new byte[100*((Character.SIZE)/8)];
		dataInputStream.read(studyNameBytes);
		char[] studyName = DataConversion.byteArrayToCharArray(studyNameBytes);
		
		patientData = new PatientData(patientName, patientSurname, studyName);

		// Leo AcquisitionData
		// Obtengo nombre del sensor adquisidor
		byte[] sensorBytes = new byte[50*((Character.SIZE)/8)];
		dataInputStream.read(sensorBytes);
		char[] sensor = DataConversion.byteArrayToCharArray(sensorBytes);
		
		// Obtengo tipo de estudio
		byte[] studyTypeBytes = new byte[50*((Character.SIZE)/8)];
		dataInputStream.read(studyTypeBytes);
		char[] studyType = DataConversion.byteArrayToCharArray(studyTypeBytes);
		
		// Obtengo frecuencia de muestreo
		byte[] fsBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(fsBytes);
		double fs = DataConversion.byteArrayToDouble(fsBytes);
		
		// Obtengo resolución
		byte[] bitsBytes = new byte[(Integer.SIZE)/8];
		dataInputStream.read(bitsBytes);
		int bits = DataConversion.byteArrayToInt(bitsBytes);
		
		// Obtengo voltaje máximo
		byte[] vMaxBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(vMaxBytes);
		double vMax = DataConversion.byteArrayToDouble(vMaxBytes);
		
		// Obtengo voltaje mínimo
		byte[] vMinBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(vMinBytes);
		double vMin = DataConversion.byteArrayToDouble(vMinBytes);
		
		// Obtengo amplitud máxima
		byte[] aMaxBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(aMaxBytes);
		double aMax = DataConversion.byteArrayToDouble(aMaxBytes);
		
		// Obtengo amplitud mínima
		byte[] aMinBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(aMinBytes);
		double aMin = DataConversion.byteArrayToDouble(aMinBytes);
		
		// Obtengo muestras totales almacenadas
		byte[] totalSamplesBytes = new byte[(Double.SIZE)/8];
		dataInputStream.read(totalSamplesBytes);
		double totalSamples = DataConversion.byteArrayToDouble(totalSamplesBytes);
		
		AdcData adcData = new AdcData(fs, bits, vMax, vMin, aMax, aMin, sensor, -1, -1);
		acquisitionData = new AcquisitionData(adcData);
	
		
		// Salteo padding
		int i = 0;
		while(dataInputStream.read() == 0) i++; dataInputStream.read();
		
		
		// Obtengo muestras
		samples = new ArrayList<Short>();
		short sample;
		
		do {
			sample = dataInputStream.readShort();
			if(sample != -1) samples.add(sample);
		}while(sample != -1);
			

		// Cierro
		dataInputStream.close();
		} catch (IOException ioe) {}
		
		// Genero Data y vuelvo
		StudyData studyData = new StudyData();

		studyData.setAcquisitionData(acquisitionData);
		studyData.setPatientData(patientData);
		studyData.setSamplesBuffer(samples);
	
		return studyData;	
	}
	
}
