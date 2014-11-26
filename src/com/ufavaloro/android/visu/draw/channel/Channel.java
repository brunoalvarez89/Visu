package com.ufavaloro.android.visu.draw.channel;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.data.StudyData;

public class Channel {

	private SignalBox mSignalBox;
	private InfoBox mInfoBox;
	private int mAdcChannelNumber;
	private int mChannelIndex;
	private boolean mOnline;
	private static int mWidth;
	private double mWidthMarginPercent = 0.95;
	private static int mHeight;
	private double mHeightMarginPercent = 0.95;
	private static int mTotalScreenHeight;
	private static double mSignalBoxWidthPercent = 0.8;
	private static double mInfoBoxWidthPercent = 1 - mSignalBoxWidthPercent;
	private RGB mColor;
	private StudyData mStudyData;
	
	// Offline channel constructor
	public Channel(StudyData studyData, ArrayList<Short> samples) {
		mSignalBox = new SignalBox(studyData, null, samples);
	}
	
	// Online channel constructor
	public Channel(int channelNumber, int totalScreenHeight, int totalScreenWidth
				   , RGB channelColor, int totalPages, StudyData studyData) {
		
		setStudyData(studyData);
		mAdcChannelNumber = channelNumber;
		mWidth = (int) (totalScreenWidth);
		mTotalScreenHeight = totalScreenHeight;
		mColor = channelColor;
		mOnline = true;
		
		// Creo SignalBox
		SignalBox.setWidth((float) (totalScreenWidth*mSignalBoxWidthPercent));
		mSignalBox = new SignalBox(channelNumber, totalPages, studyData);
		
		// Creo InfoBox
		InfoBox.setWidth((float) (totalScreenWidth*mInfoBoxWidthPercent));
		InfoBox.setVerticalDivisorXPosition((int) SignalBox.getWidth());
		mInfoBox = new InfoBox(channelNumber, studyData);
	
	}
	
	public void update(int totalChannels, int channelIndex) {
		// Actualizo Altura
		mHeight = (mTotalScreenHeight/totalChannels);
		// Actualizo índice del canal
		mChannelIndex = channelIndex;
		// Actualizo SignalBox
		mSignalBox.update(mHeight, mChannelIndex);
		//Actualizo InfoBox
		mInfoBox.update(mHeight, mChannelIndex);
	}

	public static double getSignalBoxWidthPercent() {
		return mSignalBoxWidthPercent;
	}
	
	public SignalBox getSignalBox() {
		return mSignalBox;
	}
	
	public void setSignalBox(SignalBox mSignalBox) {
	this.mSignalBox = mSignalBox;
	}

	public InfoBox getInfoBox() {
		return mInfoBox;
	}

	public void setInfoBox(InfoBox mInfoBox) {
		this.mInfoBox = mInfoBox;
	}

	public int getChannelNumber() {
		return mAdcChannelNumber;
	}
	
	public void storeSamples(short[] samples) {
		mSignalBox.getDrawBuffer().storeSamples(samples);
	}

	public boolean getPaused() {
		return mSignalBox.getPaused();
	}
	
	public RGB getColor() {
		return mColor;
	}
	
	public static int getWidth() {
		return mWidth;
	}
	
	public static int getHeight() {
		return mHeight;
	}

	public StudyData getStudyData() {
		return mStudyData;
	}

	public void setStudyData(StudyData studyData) {
		mStudyData = studyData;
	}

	public float getHorizontalZoom() {
		return mSignalBox.getDrawBuffer().getHorizontalZoom();
	}
	
	public void setHorizontalZoom(float newZoomValue) {
		mSignalBox.getDrawBuffer().setHorizontalZoom(newZoomValue);
		
		// Actualizo Label de Zoom X
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(1);

		Label l = mInfoBox.getHorizontalZoomLabel();
		l.setText("Zoom X: " + df.format(newZoomValue) + "x");
	}
	
	public float getVerticalZoom() {
		return mSignalBox.getDrawBuffer().getVerticalZoom();
	}
	
	public void setVerticalZoom(float newZoomValue) {
		mSignalBox.getDrawBuffer().setVerticalZoom(newZoomValue);
		
		// Actualizo el Label de Zoom Y
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(1);

	}
}
