package com.ufavaloro.android.visu.draw.channel;

import java.util.ArrayList;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

import android.graphics.Paint;
import android.graphics.Rect;


public class InfoBox{
	
	private StudyData mStudyData;
	
	// Ancho y Alto
	private static float mWidth;
	private static float mHeight;

	// Array de Labels
	private ArrayList<Label> mLabelList = new ArrayList<Label>();

	// Posición de la línea que divide un SignalBox de un InfoBox
	private static int mVerticalDivisorXPosition;
	
	// Porcentaje del ancho del InfoBox que pueden ocupar los labels
	private final double mLabelWidthPercent = 0.85;
	
	// Padding a izquierda para centrar los Labels
	private final double mLeftPadding = (mWidth*(1-mLabelWidthPercent))/3;
	
	// Padding entre Labels para espaciarlos mejor
	private final double mInterLabelPadding = 0;//mHeight*0.01; 
	
	// Label con el # de canal
	private Label mChannelLabel;
	private final double mChannelLabelHeightPercent = 0.2;
	
	// Label de Tiempo de adquisición
	private Label mElapsedTimeLabel;
	private final double mElapsedTimeLabelHeightPercent = 0.1;
	
	// Label de Fs
	private Label mPatientLabel;
	private final double mPatientLabelHeightPercent = 0.1;
	
	// Label de Resolución
	private Label mBitsLabel;
	private final double mBitsLabelHeightPercent = 0.1;
	
	// Label Zoom Horizontal
	private Label mHorizontalZoomLabel;
	private final double mHorizontalZoomLabelHeightPercent = 0.1;
	
	// Label Zoom Vertical
	private Label mVerticalZoomLabel;
	private final double mVerticalZoomLabelHeightPercent = 0.1;
	
	// Label de Pausa
	private Label mPausedLabel;
	private final double mPausedLabelHeightPercent = 0.1;

	// Canal que representa
	private int mAdcChannelNumber;
	private int mChannelIndex;
	
	// Color
	private RGB mColor;


	InfoBox(int channelNumber, StudyData studyData) {
		
		mStudyData = studyData;
		mAdcChannelNumber = channelNumber;
		
		createChannelNumberLabel();
		createElapsedTimeLabel();
		//createPatientLabel();
		//createBitsLabel();
		//createHorizontalZoomLabel();
		//createVerticalZoomLabel();
		//createPausedLabel();
	}
		
	public void createChannelNumberLabel() {
		String text = "Canal " + String.valueOf(mAdcChannelNumber + 1);
		mChannelLabel = new Label(0, 0, 0, text);
	}

	private void createElapsedTimeLabel() {
		String text = "00m 00s";
		mElapsedTimeLabel = new Label(0, 0, 0, text);
	}
	
	private void createPatientLabel() {
		double fs = mStudyData.getAcquisitionData().getFs();
		String text = "Fs: " + (int) fs + " Hz";
		mPatientLabel = new Label(0, 0, 0, text);
	}
	
	private void createBitsLabel() {
		double bits = mStudyData.getAcquisitionData().getBits();
		String text = "Bits: " + (int) bits;
		mBitsLabel = new Label(0, 0, 0, text);
	}
	
	private void createHorizontalZoomLabel() {
		String text = "Zoom X: 1x";
		mHorizontalZoomLabel = new Label(0, 0, 0, text);
	}
	
	private void createVerticalZoomLabel() {
		String text = "Zoom Y: 1x";
		mVerticalZoomLabel = new Label(0, 0, 0, text);
	}
	
	private void createPausedLabel() {
		String text = "EN PAUSA";
		mPausedLabel = new Label(0, 0, 0, text);
	}
	
	private void updateChannelNumberLabelSize() {
		int textSize = getBoundedTextSize(mChannelLabel, mLabelWidthPercent * mWidth
										  , mChannelLabelHeightPercent * mHeight);
		mChannelLabel.setTextSize(textSize);
		
		mLabelList.add(mChannelLabel);
	}

	private void updateElapsedTimeLabelSize() {
		int textSize = getBoundedTextSize(mElapsedTimeLabel, mLabelWidthPercent * mWidth
										  , mElapsedTimeLabelHeightPercent * mHeight);
		mElapsedTimeLabel.setTextSize(textSize);
		
		mLabelList.add(mElapsedTimeLabel);
	}

	private void updatePatientLabelSize() {
		int textSize = getBoundedTextSize(mPatientLabel, mLabelWidthPercent * mWidth
										  , mPatientLabelHeightPercent * mHeight);
		mPatientLabel.setTextSize(textSize);

		mLabelList.add(mPatientLabel);
	}
	
	private void updateBitsLabelSize() {
		int textSize = getBoundedTextSize(mBitsLabel, mLabelWidthPercent * mWidth
										  , mBitsLabelHeightPercent * mHeight);
		mBitsLabel.setTextSize(textSize);
		
		mLabelList.add(mBitsLabel);
	}
	
	private void updateHorizontalZoomLabelSize(int zoomValue) {
		int textSize = getBoundedTextSize(mHorizontalZoomLabel, mLabelWidthPercent * mWidth
										  , mHorizontalZoomLabelHeightPercent * mHeight);
		mHorizontalZoomLabel.setTextSize(textSize);
		
		mLabelList.add(mHorizontalZoomLabel);
	}
	
	private void updateVerticalZoomLabelSize(int zoomValue) {
		int textSize = getBoundedTextSize(mVerticalZoomLabel, mLabelWidthPercent * mWidth
										  , mVerticalZoomLabelHeightPercent * mHeight);
		mVerticalZoomLabel.setTextSize(textSize);
		
		mLabelList.add(mVerticalZoomLabel);
	}

	private void updatePausedLabelSize() {
		int textSize = getBoundedTextSize(mPausedLabel, mLabelWidthPercent * mWidth
										  , mPausedLabelHeightPercent * mHeight);
		mPausedLabel.setTextSize(textSize);
		
		mLabelList.add(mPausedLabel);
	}	
	
	private void setMinimumSize() {
		
		int minTextSize = (int) mLabelList.get(0).getTextSize();
		
		// Obtengo tamaño de texto mínimo
		for(int i = 1; i < mLabelList.size(); i++) {
			if(mLabelList.get(i).getTextSize() < minTextSize) {
				minTextSize = (int) mLabelList.get(i).getTextSize();
			}
		}
		
		// Seteo como tamaño de texto el mínimo de todos los tamaños calculados
		// mediante getMaxTextSize()
		for (int i = 1; i < mLabelList.size(); i++) {
			mLabelList.get(i).setTextSize(minTextSize);
		}
	}

	// Acualizo el Label con el # de canal
	private void updateChannelNumberLabelPosition() {
		
		mChannelLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mChannelLabel.setY((int) (mChannelLabel.getBoundingBox().height() 
								  + mChannelIndex*mHeight
								  + mInterLabelPadding));
	
	}

	// Acualizo el Label de Tiempo de adquisición
	private void updateElapsedTimeLabelPosition() {
	
		mElapsedTimeLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mElapsedTimeLabel.setY((int) (mElapsedTimeLabel.getBoundingBox().height() 
							          + mChannelLabel.getY()
							          + mInterLabelPadding));
	
	}

	// Update Patient Label
	private void updatePatientLabelPosition() {
		
		mPatientLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPatientLabel.setY((int) (mPatientLabel.getBoundingBox().height()
					         + mElapsedTimeLabel.getY()
					         + mInterLabelPadding));
	
	}
		
	// Acualizo el Label de Resolución
	private void updateBitsLabelPosition() {
		
		mBitsLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mBitsLabel.setY((int) (mBitsLabel.getBoundingBox().height()
					    	   + mPatientLabel.getY()
						       + mInterLabelPadding));
	
	}
		
	// Acualizo el Label Zoom Horizontal
	private void updateHorizontalZoomLabelPosition(int zoomValue) {
		
		mHorizontalZoomLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mHorizontalZoomLabel.setY((int) (mHorizontalZoomLabel.getBoundingBox().height()
								  	     + mBitsLabel.getY()
								  	     + mInterLabelPadding));
		
	}
		
	// Acualizo el Label Zoom Vertical
	private void updateVerticalZoomLabelPosition(int zoomValue) {
		
		mVerticalZoomLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mVerticalZoomLabel.setY((int) (mVerticalZoomLabel.getBoundingBox().height()
									   + mHorizontalZoomLabel.getY()
									   + mInterLabelPadding));
		
	}

	// Acualizo el Label de Pausa
	private void UpdatePausedLabelPosition() {
		
		mPausedLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPausedLabel.setY((int) (mPausedLabel.getBoundingBox().height()
						  		 + mVerticalZoomLabel.getY()
						  		 + mInterLabelPadding));
		
	}	
		
	// Cuando se agrega o elimina un canal, se redimensionan los boxes
	public void update(int height, int channelIndex) {
		
		setHeight(height);
		setChannelIndex(channelIndex);
		
		mLabelList.clear();
		
		// Actualizo tamaños
		updateChannelNumberLabelSize();
		updateElapsedTimeLabelSize();
		//updatePatientLabelSize();
		//updateBitsLabelSize();
		//updateHorizontalZoomLabelSize(1);
		//updateVerticalZoomLabelSize(1);
		//updatePausedLabelSize();
		
		// Seteo tamaño mínimo global de texto
		setMinimumSize();
		
		// Actualizo posiciones
		updateChannelNumberLabelPosition();
		updateElapsedTimeLabelPosition();
		//updatePatientLabelPosition();
		//updateBitsLabelPosition();
		//updateHorizontalZoomLabelPosition(1);
		//updateVerticalZoomLabelPosition(1);
		//UpdatePausedLabelPosition();

	}

	public int getChannel() {
		return mAdcChannelNumber;
	}
	
	public Label getLabelChannel() {
		return mChannelLabel;
	}
	
	public Label getLabelTimer() {
		return mElapsedTimeLabel;
	}
	
	public Label getLabelFs() {
		return mPatientLabel;
	}
	
	public Label getLabelBits() {
		return mBitsLabel;
	}
	
	public Label getHorizontalZoomLabel() {
		return mHorizontalZoomLabel;
	}
	
	public Label getLabelZoomY() {
		return mVerticalZoomLabel;
	}
	
	public Label getLabelPaused() {
		return mPausedLabel;
	}
	
	public static void setWidth(float boxWidth) {
		mWidth = boxWidth;
	}
	
	public static void setHeight(float boxHeight) {
		mHeight = boxHeight;
	}

	public static float getBoxHeight() {
		return mHeight;
	}
	
	public static float getBoxWidth() {
		return InfoBox.mWidth;
	}

	public RGB getColor() {
		return mColor;
	}
	
	public void setColor(RGB mColor) {
		this.mColor = mColor;
	}

	// Método para obtener el tamaño de texto apropiado para los labels del
	// InfoBox
	private int getBoundedTextSize(Label label, double boxWidth, double boxHeight) {
		
		Rect rect = new Rect();
		Paint paint = new Paint();

		int i = 0;
		
		while (true) {
			
			paint.setTextSize(i);
			paint.getTextBounds(label.getText(), 0, label.getText().length(), rect);
			
			double width = rect.width();
			double height = rect.height();
			
			if (width > boxWidth || height > boxHeight) {
				break;
				
			}
			
			i++;
			
		}

		return i;
	}
	
	public static int getVerticalDivisorXPosition() {
		return mVerticalDivisorXPosition;
	}

	public static void setVerticalDivisorXPosition(int mVerticalDivisorXPosition) {
		InfoBox.mVerticalDivisorXPosition = mVerticalDivisorXPosition;
	}

	public void setChannelIndex(int channelIndex) {
		mChannelIndex = channelIndex;
	}
	
	public void setChannelNumber(int channelNumber) {
		mAdcChannelNumber = channelNumber;
	}
}

