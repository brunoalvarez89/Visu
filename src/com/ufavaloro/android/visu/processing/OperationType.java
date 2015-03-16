package com.ufavaloro.android.visu.processing;

public enum OperationType {

	NULL(-1),
	TIME_DERIVATIVE(1),
	TIME_SELF_MULTIPLY(2),
	TIME_MAF(3),
	TIME_LOWPASS(4),
	TIME_HIGHPASS(5),
	FREQUENCY_FFT(6),
	EKG_QRS_ADAPTIVE_THRESHOLD(7),
	EKG_QRS_FIRST_DERIVATIVE_SLOPE(8);
	
	private final int value;

	private OperationType(int value){
		this.value=value;
	}
	
	public static OperationType values(int what) {
		switch(what){
		case 1: return TIME_DERIVATIVE;
		case 2: return TIME_SELF_MULTIPLY;
		case 3: return TIME_MAF;
		case 4: return TIME_LOWPASS;
		case 5: return TIME_HIGHPASS;
		case 6: return FREQUENCY_FFT;
		case 7: return EKG_QRS_ADAPTIVE_THRESHOLD;
		case 8: return EKG_QRS_FIRST_DERIVATIVE_SLOPE;
		
		default: return NULL;
		}	
	}
	
	public int getValue(){return value;}
	

}//BluetoothHelperMessage
