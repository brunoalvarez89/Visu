package com.ufavaloro.android.visu.storage.datatypes;

public class AdcData {
	
	// Frecuencia de muestreo
	public double fs;
	public int fsBytes = (Double.SIZE)/8;
	
	// Resoluci�n
	public int bits;
	public int bitsBytes = (Integer.SIZE)/8;
	
	// Voltaje m�ximo
	public double vMax;
	public int vMaxBytes = (Double.SIZE)/8;
	
	// Voltaje m�nimo
	public double vMin;
	public int vMinBytes = (Double.SIZE)/8;
	
	// Amplitud m�xima
	public double aMax;
	public int aMaxBytes = (Double.SIZE)/8;
	
	// Amplitud m�nima
	public double aMin;
	public int aMinBytes = (Double.SIZE)/8;
	
	// Sensor que provee las muestras
	public char[] sensor;
	public int sensorSize = 50;
	public int sensorBytes = sensorSize*((Character.SIZE)/8);
	
	// N�mero de canal del ADC asociado al buffer
	public int adcChannel = -1;
	public int adcChannelBytes = (Integer.SIZE)/8;
	
	// N�mero de canal Bluetooth asociado al buffer
	public int bluetoothChannel;
	public int bluetoothChannelBytes = (Integer.SIZE)/8;

	// Tama�o en bytes de los paquetes de muestras recibidos
	public int samplesPerPackage;
	
	// Tama�o en bytes de los datos del ADC
	public int adcBytes;
	
	// Constructor
	public AdcData(double fs, int bits, double vMax, double vMin, double aMax,
			   	   double aMin, char[] sensor, int samplesPerPackage, int adcChannel) {

		this.fs = fs;
	
		this.bits = bits;
		
		this.vMax = vMax;
		this.vMin = vMin;
		
		this.aMax = aMax;
		this.aMin = aMin;
		
		this.sensor = sensor;
		
		this.adcChannel = adcChannel;
		
		this.samplesPerPackage = samplesPerPackage;
		
		adcBytes = fsBytes + bitsBytes + vMaxBytes + vMinBytes + aMaxBytes + aMinBytes + sensorBytes;
	
	}

}
