package com.ufavaloro.android.visu.draw;

public class ReferenceMatrix {

	private int mHeight;
	private int mWidth;
	private int mTotalChannels;
	private int mVerticalDivisor;
	private int[][] mCoordMatrix;
	
	ReferenceMatrix(int mHeight, int mWidth) {
		this.mHeight = mHeight;
		this.mWidth = mWidth;
		mCoordMatrix = new int[mHeight][mWidth];
		mTotalChannels = 0;
	}
	
	public void refresh() {
		// Para la cantidad de canales totales
		for(int i = 0; i < mTotalChannels; i++) {
			// Para cada divisor horizontal
			for(int j = i*mHeight/mTotalChannels; j < (i+1)*mHeight/mTotalChannels; j++) {
				// Lleno a izquierda del divisor vertical
				for(int k = 0; k < mVerticalDivisor; k++) {
					mCoordMatrix[j][k] = i+1;
				}
				// Lleno a derecha del divisor vertical
				for(int k = mVerticalDivisor; k < mWidth; k++) {
					mCoordMatrix[j][k] = -(i+1);
				}
			}
		}
	}
	
	public void setVerticalDivisor(double verticalDivisor) {
		mVerticalDivisor = (int) verticalDivisor;
	}
	
	public void removeChannel() {
		mTotalChannels--;
		refresh();
	}
	
	public void addChannel() {
		mTotalChannels++;
		refresh();
	}
	
	public int getChannel(double y, double x) {
		return mCoordMatrix[(int) y][(int) x];
	}
}
