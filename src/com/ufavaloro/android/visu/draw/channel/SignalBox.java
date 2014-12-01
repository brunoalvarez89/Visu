/*****************************************************************************************
 * SignalBox.java																	 	*
 * Clase que representa el rectángulo de la pantalla donde se grafica la señal.			*
 ****************************************************************************************/
package com.ufavaloro.android.visu.draw.channel;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Rect;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.data.AcquisitionData;
import com.ufavaloro.android.visu.storage.data.StudyData;

public class SignalBox{

	// Draw buffer
	private DrawBuffer mDrawBuffer;
		
	// Ancho y alto
	private static float mWidth;
	private static float mHeight;	
	
	// Límites gráficos y línea media
	private float mUpperBound;
	private float mLowerBound;
	private float mMidLine;

	private RGB mColor;
	
	private boolean mPaused;
			
	private int mAdcChannelNumber;
	private int mChannelIndex;
	
	private Label mMaxAmplitudeLabel;
	private Label mMinAmplitudeLabel;
	
	private Label mMaxVoltageLabel;
	private Label mMinVoltageLabel;
	
	private Label mTimeLabel;
	private int mTimeLabelPixels;
	
	private float mAmplitudeCorrection;
	private float mVoltageCorrection;
	
	private StudyData mStudyData;
	
/*****************************************************************************************
* Inicio de métodos de clase														   	 *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales																	 *
*****************************************************************************************/
	// Constructor para Online SignalBox
	SignalBox(int adcChannelNumber, int totalPages, StudyData studyData) {
		
		mAdcChannelNumber = adcChannelNumber;				
		mPaused = false;
		mStudyData = studyData;
		mDrawBuffer = new DrawBuffer(mAdcChannelNumber, (int) mWidth, totalPages 
									 , mStudyData.getAcquisitionData().getBits());

		createMaxAmplitudeLabel("");
		createMinAmplitudeLabel("");
		createMaxVoltageLabel();
		createMinVoltageLabel();
		createTimeLabel();
					
	}
	
	// Constructor para Ofline SignalBox
	public SignalBox(int channelNumber, StudyData studyData) {
		mAdcChannelNumber = channelNumber;				
		mPaused = false;
		mStudyData = studyData;
		int totalPages = (int) (studyData.getSamplesBuffer().getSize() / mWidth);
		mDrawBuffer = new DrawBuffer(mAdcChannelNumber, studyData, totalPages);

		createMaxAmplitudeLabel("");
		createMinAmplitudeLabel("");
		createMaxVoltageLabel();
		createMinVoltageLabel();
		createTimeLabel();
	}

	private void createMaxAmplitudeLabel(String units) {
		mMaxAmplitudeLabel = new Label(mStudyData.getAcquisitionData().getAMax());
		mMaxAmplitudeLabel.setUnits(units);
	}
	
	private void createMinAmplitudeLabel(String units) {
		mMinAmplitudeLabel = new Label(mStudyData.getAcquisitionData().getAMin());
		mMinAmplitudeLabel.setUnits(units);
	}
	
	private void createMaxVoltageLabel() {
		mMaxVoltageLabel = new Label(mStudyData.getAcquisitionData().getVMax());
		mMaxVoltageLabel.setUnits("V");
	}
	
	private void createMinVoltageLabel() {
		mMinVoltageLabel = new Label(mStudyData.getAcquisitionData().getVMin());
		mMinVoltageLabel.setUnits("V");
	}
	
	private void createTimeLabel() {
		mTimeLabel = new Label();
		mTimeLabel.setUnits("s");
		mTimeLabelPixels = (int) (mWidth*0.15);
	}

	public void update(int height, int channelIndex) {
		
		setHeight(height);
		setChannelIndex(channelIndex);
		getDrawBuffer().setVerticalZoom(mHeight/4);
		getDrawBuffer().setHorizontalZoom(1);
		setUpperBound(mChannelIndex*mHeight);
		setLowerBound((mChannelIndex+1)*mHeight);
		setMidLine(getUpperBound() + getHeight()/2);
		
		// Acutalizo labels de Amax y Amin
		updateAmplitudeLabelsSize();
		updateAmplitudeLabelsPosition();
		
		// Actualizo labels de Vmax y Vmin
		updateVoltageLabelsSize();
		updateVoltageLabelsPosition();
		
		// Actualizo label de Tiempo
		updateTimeLabelSize();
		updateTimeLabelPosition();
	
	}

	private void updateTimeLabelPosition() {
		int yTime = (int) (mMidLine + (mHeight*0.5)*0.9);
		mTimeLabel.setY(yTime);
		
		int xTime = (int) (mWidth*0.85);
		mTimeLabel.setX(xTime);
		
	}

	private void updateVoltageLabelsPosition() {
		mVoltageCorrection = mMinVoltageLabel.getBoundingBox().height();

		int yMaxVoltage = (int) (mMidLine - mHeight/2 + mVoltageCorrection);	
		mMaxVoltageLabel.setY(yMaxVoltage);
		
		
		int yMinVoltage = (int) (mMidLine + mHeight/2);
		mMinVoltageLabel.setY(yMinVoltage);
		
	}

	private void updateAmplitudeLabelsPosition() {
		
		float yMaxAmplitude = mMidLine - mDrawBuffer.getVerticalZoom();	
		mMaxAmplitudeLabel.setY((int) yMaxAmplitude);
		
		
		float yMinAmplitude = mMidLine + mDrawBuffer.getVerticalZoom();
		mAmplitudeCorrection = mMinAmplitudeLabel.getBoundingBox().height();
		mMinAmplitudeLabel.setY((int) (yMinAmplitude + mAmplitudeCorrection));
		
	}

	private void updateDrawBuffer() {
		mDrawBuffer.setHorizontalZoom(1);
		mDrawBuffer.setVerticalZoom(mHeight/4);
	}
	
	private void updateAmplitudeLabelsSize() {
		
		mMaxAmplitudeLabel.setTextSize(getBoundedTextSize(mMaxAmplitudeLabel));
		mMinAmplitudeLabel.setTextSize(getBoundedTextSize(mMinAmplitudeLabel));

	}

	protected void updateVoltageLabelsSize() {
		
		mMaxVoltageLabel.setTextSize(getBoundedTextSize(mMaxVoltageLabel));
		mMinVoltageLabel.setTextSize(getBoundedTextSize(mMinVoltageLabel));

	}
	
	protected void updateTimeLabelSize() {
		
		mTimeLabel.setTextSize(getBoundedTextSize(mTimeLabel));

	}
	
	private int getBoundedTextSize(Label label) {
		Rect rect = new Rect();
		double altoColumna = 0.05 * mHeight;

		int i = 0;
		Paint paint = new Paint();
		
		while (true) {
			paint.setTextSize(i);
			paint.getTextBounds(label.getText(), 0, label.getText().length(), rect);
			double alto = rect.height();
			if (alto > altoColumna) {
				break;
			}
			i++;
		}

		label.setBoundingBox(rect);
		return i;
	}
	
	public void panRight(int panSensitivity) {
		mDrawBuffer.decreaseGraphingIndex(panSensitivity);
	}
	
	public void panLeft(int panSensitivity) {
		mDrawBuffer.increaseGraphingIndex(panSensitivity);
	}
		
	public void resetGraphingIndex() {
		int storingIndex = mDrawBuffer.getStoringIndex();
		mDrawBuffer.setGraphingIndex(storingIndex);
	}

	public static int getWidth() {
		return (int) mWidth;
	}
	
	public static void setWidth(float width) {
		mWidth = width;
	}
	
	public static int getHeight() {
		return (int) mHeight;
	}
	
	public static void setHeight(float height) {
		mHeight = height;
	}
	
/*****************************************************************************************
* Setters																				 *
*****************************************************************************************/
	public void setPaused(boolean mPaused) {
		this.mPaused = mPaused;
	}
	
	public void setColor(RGB mColor) {
		this.mColor = mColor;
	}

/*****************************************************************************************
* Getters																				 *
*****************************************************************************************/
	public boolean getPaused() {
		return mPaused;
	}
	
	public int getGraphingIndex() {
		return mDrawBuffer.getGraphingIndex();
	}

	public int getChannel() {
		return mAdcChannelNumber;
	}
	
	public RGB getColor() {
		return mColor;
	}
		
	public Label getLabelMaxAmplitude() {
		return mMaxAmplitudeLabel;
	}
	
	public Label getLabelMinAmplitude() {
		return mMinAmplitudeLabel;
	}
	
	public Label getLabelMaxVoltage() {
		return mMaxVoltageLabel;
	}
	
	public Label getLabelMinVoltage() {
		return mMinVoltageLabel;
	}
	
	public Label getTimeLabel() {
		return mTimeLabel;
	}
	
	public float getAmplitudeCorrection() {
		return mAmplitudeCorrection;
	}

	public float getVoltageCorrection() {
		return mVoltageCorrection;
	}
	
	public int getLabelTimePixels() {
		return mTimeLabelPixels;
	}	
	
	public DrawBuffer getDrawBuffer() {
		return mDrawBuffer;
	}

	public float getUpperBound() {
		return mUpperBound;
	}

	public void setUpperBound(float upperBound) {
		mUpperBound = upperBound;
	}

	public float getLowerBound() {
		return mLowerBound;
	}

	public void setLowerBound(float lowerBound) {
		mLowerBound = lowerBound;
	}

	public float getMidLine() {
		return mMidLine;
	}

	public void setMidLine(float midLine) {
		mMidLine = midLine;
	}
	
	public void setChannelIndex(int channelIndex) {
		mChannelIndex = channelIndex;
	}
	
}//SignalBox
