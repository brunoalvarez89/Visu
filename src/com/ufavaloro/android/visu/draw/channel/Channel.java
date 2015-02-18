package com.ufavaloro.android.visu.draw.channel;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.maininterface.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

public class Channel {

	private SignalBox mSignalBox;
	private InfoBox mInfoBox;
	private int mAdcChannelNumber;
	private int mChannelIndex;
	private boolean mOnline;
	private boolean mPaused;
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
	public Channel(int channelNumber, int totalScreenHeight, int totalScreenWidth
				   , RGB channelColor, StudyData studyData) {
		mStudyData = studyData;
		mAdcChannelNumber = channelNumber;
		mWidth = (int) (totalScreenWidth);
		mTotalScreenHeight = totalScreenHeight;
		mColor = channelColor;
		setOnline(false);
		mPaused = false;
		
		// Creo SignalBox
		SignalBox.setWidth((float) (totalScreenWidth*mSignalBoxWidthPercent));
		mSignalBox = new SignalBox(channelNumber, studyData);
		
		// Creo InfoBox
		InfoBox.setWidth((float) (totalScreenWidth*mInfoBoxWidthPercent));
		InfoBox.setVerticalDivisorXPosition((int) SignalBox.getWidth());
		mInfoBox = new InfoBox(channelNumber, studyData);
	}
	
	// Online channel constructor
	public Channel(int channelNumber, int totalScreenHeight, int totalScreenWidth
				   , RGB channelColor, int totalPages, StudyData studyData) {
		setStudyData(studyData);
		mAdcChannelNumber = channelNumber;
		mWidth = (int) (totalScreenWidth);
		mTotalScreenHeight = totalScreenHeight;
		mColor = channelColor;
		setOnline(true);
		mPaused = false;
		
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
	
	public RGB getColor() {
		return mColor;
	}
	
	public void setColor(RGB color) {
		mColor = color;
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
		mSignalBox.updateHorizontalZoom(newZoomValue);
	}
	
	public float getVerticalZoom() {
		return mSignalBox.getDrawBuffer().getVerticalZoom();
	}
	
	public void setVerticalZoom(float newZoomValue) {
		mSignalBox.updateVerticalZoom(newZoomValue);
	}

	public boolean isOnline() {
		return mOnline;
	}

	public void setOnline(boolean mOnline) {
		this.mOnline = mOnline;
	}

	
	public boolean isPaused() {
		return mPaused;
	}

	public void setPaused(boolean mPaused) {
		this.mPaused = mPaused;
	}
	
	public void setStudyType(int studyType) {
		mInfoBox.studyData.getAcquisitionData().setStudyType(studyType);
	}
	
	public void setPatientNameSurname(String patientName, String patientSurname) {
		
	}
	
	public void setStudyName(String studyName) {
		
	}

	public void setAMax(double aMax) {
		mSignalBox.studyData.getAcquisitionData().setAMax(aMax);
	}
	
	public void setAMin(double aMin) {
		mSignalBox.studyData.getAcquisitionData().setAMin(aMin);
	}
	
}
