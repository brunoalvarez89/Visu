package com.ufavaloro.android.visu.storage.data;

import java.nio.ByteBuffer;

public class DataConversion {

	// M�todo para pasar de byte[] a char[]
	public static char[] byteArrayToCharArray(byte[] bytes) {
		
		char[] aux = new char[bytes.length/2];
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		
		for(int i = 0; i < aux.length; i++) {
			aux[i] = byteBuffer.getChar();
		}
		
		return aux;
		
	}

	// M�todo para pasar de byte[] a double
	public static double byteArrayToDouble(byte[] bytes) {
		
		return ByteBuffer.wrap(bytes).getDouble();
		
	}
	
	// M�todo para pasar de byte[] a short
	public static short byteArrayToShort(byte[] bytes) {
		
		return ByteBuffer.wrap(bytes).getShort();
	
	}
	
	// M�todo para pasar de byte[] a int
	public static int byteArrayToInt(byte[] bytes) {
		
		return ByteBuffer.wrap(bytes).getInt();
	
	}

}
