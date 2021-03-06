/*****************************************************************************************
 * SignalBox.java																	 	*
 * Clase que representa el rect�ngulo de la pantalla donde se grafica la se�al.			*
 ****************************************************************************************/
package com.ufavaloro.android.visu.draw.channel;

import java.text.DecimalFormat;

import android.graphics.Paint;
import android.graphics.Rect;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

public class SignalBox{

	// Draw buffer
	private DrawBuffer mDrawBuffer;
		
	// Ancho y alto
	private static float mWidth;
	private static float mHeight;	
	
	// L�mites gr�ficos y l�nea media
	private float mUpperBound;
	private float mLowerBound;
	private float mMidLine;

	private RGB mColor;
				
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
	
	protected StudyData studyData;
	
/*****************************************************************************************
* Inicio de m�todos de clase														   	 *
*****************************************************************************************/
/*****************************************************************************************
* M�todos principales																	 *
*****************************************************************************************/
	// Constructor para Online SignalBox
	SignalBox(int adcChannelNumber, int totalPages, StudyData studyData) {
		mAdcChannelNumber = adcChannelNumber;				
		this.studyData = studyData;
		mDrawBuffer = new DrawBuffer(mAdcChannelNumber, (int) mWidth, totalPages 
									 , studyData.getAcquisitionData().getBits());

		createMaxAmplitudeLabel();
		createMinAmplitudeLabel();
		createMaxVoltageLabel();
		createMinVoltageLabel();
		createTimeLabel();
					
	}
	
	// Constructor para Ofline SignalBox
	public SignalBox(int channelNumber, StudyData studyData) {
		mAdcChannelNumber = channelNumber;				
		this.studyData = studyData;
		int totalPages = (int) (studyData.getSamplesBuffer().getSize() / mWidth);
		mDrawBuffer = new DrawBuffer(mAdcChannelNumber, studyData, totalPages);

		createMaxAmplitudeLabel();
		createMinAmplitudeLabel();
		createMaxVoltageLabel();
		createMinVoltageLabel();
		createTimeLabel();
	}

	private void createMaxAmplitudeLabel() {
		mMaxAmplitudeLabel = new Label(studyData.getAcquisitionData().getAMax());
		char[] aux = studyData.getAcquisitionData().getStudyType();
		int studyType = aux[0];
		mMaxAmplitudeLabel.setUnits(StudyType.getUnits(StudyType.values(studyType)));
	}
	
	private void createMinAmplitudeLabel() {
		mMinAmplitudeLabel = new Label(studyData.getAcquisitionData().getAMin());
		char[] aux = studyData.getAcquisitionData().getStudyType();
		int studyType = aux[0];
		mMinAmplitudeLabel.setUnits(StudyType.getUnits(StudyType.values(studyType)));
	}
	
	private void createMaxVoltageLabel() {
		mMaxVoltageLabel = new Label(studyData.getAcquisitionData().getVMax());
		mMaxVoltageLabel.setUnits("V");
	}
	
	private void createMinVoltageLabel() {
		mMinVoltageLabel = new Label(studyData.getAcquisitionData().getVMin());
		mMinVoltageLabel.setUnits("V");
	}
	
	private void createTimeLabel() {
		mTimeLabel = new Label();
		mTimeLabelPixels = (int) (mWidth*0.15);
		double ts = 1 / studyData.getAcquisitionData().getFs();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(1);
		String result = df.format(ts * mTimeLabelPixels * mDrawBuffer.getHorizontalZoom());

		mTimeLabel.setText(result + " s");
	}

	public void update(int height, int channelIndex) {
		
		setHeight(height);
		setChannelIndex(channelIndex);
		getDrawBuffer().setVerticalZoom(mHeight/4);
		getDrawBuffer().setHorizontalZoom(1);
		setUpperBound(mChannelIndex*mHeight);
		setLowerBound((mChannelIndex+1)*mHeight);
		setMidLine(getUpperBound() + getHeight()/2);
		
		createMaxAmplitudeLabel();
		createMinAmplitudeLabel();
		createMaxVoltageLabel();
		createMinVoltageLabel();
		
		// Actualizo labels de Amax y Amin
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
	
	public void updateVerticalZoom(float newZoomValue) {
		getDrawBuffer().setVerticalZoom(newZoomValue);
		updateAmplitudeLabelsPosition();
	}
	
	public void updateHorizontalZoom(float newZoomValue) {
		getDrawBuffer().setHorizontalZoom(newZoomValue);
		mTimeLabelPixels = (int) (mWidth*0.15);
		double ts = 1 / studyData.getAcquisitionData().getFs();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(1);
		String result = df.format(ts * mTimeLabelPixels * 1/mDrawBuffer.getHorizontalZoom());

		mTimeLabel.setText(result + " s");
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
