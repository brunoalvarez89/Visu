package com.ufavaloro.android.visu.draw.channel;

import java.util.ArrayList;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.ufavaloro.android.visu.study.StudyType;

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
	
	// Label de Nombre de Paciente
	private Label mPatientLabel;
	private final double mPatientLabelHeightPercent = 0.1;

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
		
		String patientLabel;
		String channelLabel;
		if(mStudyData.getPatientData() != null) {
			String patientName = String.valueOf(studyData.getPatientData().getPatientName());
			patientName = patientName.trim().replace('_', ' ');
			
			String patientSurname = String.valueOf(studyData.getPatientData().getPatientSurname());
			patientSurname = patientSurname.trim().replace('_', ' ');
			
			patientLabel = String.valueOf(patientSurname) + ", " + String.valueOf(patientName);
			
			char[] aux = mStudyData.getAcquisitionData().getStudyType();
			int studyType = aux[0];
			channelLabel = String.valueOf(StudyType.values(studyType));
		} else {
			patientLabel = "Sin Paciente";
			channelLabel = "Canal " + String.valueOf(mAdcChannelNumber + 1);
		}
		
		createChannelLabel(channelLabel);
		//createElapsedTimeLabel();
		createPatientLabel(patientLabel);
		//createBitsLabel();
		//createHorizontalZoomLabel();
		//createVerticalZoomLabel();
		//createPausedLabel();
	}
	
	// Cuando se agrega o elimina un canal, se redimensionan los boxes
	protected void update(int height, int channelIndex) {
		
		setHeight(height);
		setChannelIndex(channelIndex);
		
		mLabelList.clear();
		
		// Actualizo tamaños
		updateChannelLabelSize();
		updatePatientLabelSize();
		//updatePausedLabelSize();
		
		// Seteo tamaño mínimo global de texto
		setMinimumSize();
		
		// Actualizo posiciones
		updateChannelLabelPosition();
		updatePatientLabelPosition();
		//UpdatePausedLabelPosition();

	}

	protected void createChannelLabel(String text) {
		mChannelLabel = new Label(0, 0, 0, text);
	}

	protected void createPatientLabel(String text) {
		mPatientLabel = new Label(0, 0, 0, text);
	}

	private void createPausedLabel() {
		String text = "EN PAUSA";
		mPausedLabel = new Label(0, 0, 0, text);
	}
	
	protected void updateChannelLabelSize() {
		int textSize = getBoundedTextSize(mChannelLabel, mLabelWidthPercent * mWidth
										  , mChannelLabelHeightPercent * mHeight);
		mChannelLabel.setTextSize(textSize);
		
		mLabelList.add(mChannelLabel);
	}

	protected void updatePatientLabelSize() {
		int textSize = getBoundedTextSize(mPatientLabel, mLabelWidthPercent * mWidth
										  , mPatientLabelHeightPercent * mHeight);
		mPatientLabel.setTextSize(textSize);

		mLabelList.add(mPatientLabel);
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
	private void updateChannelLabelPosition() {
		
		mChannelLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mChannelLabel.setY((int) (mChannelLabel.getBoundingBox().height() 
								  + mChannelIndex*mHeight
								  + mInterLabelPadding));
	
	}

	// Update Patient Label
	private void updatePatientLabelPosition() {
		
		mPatientLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPatientLabel.setY((int) (mPatientLabel.getBoundingBox().height()
					         + mChannelLabel.getY()
					         + mInterLabelPadding));
	
	}
		
	// Acualizo el Label de Pausa
	private void UpdatePausedLabelPosition() {
		
		mPausedLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPausedLabel.setY((int) (mHeight - mInterLabelPadding));
		
	}	
		
	public int getChannel() {
		return mAdcChannelNumber;
	}
	
	public Label getLabelChannel() {
		return mChannelLabel;
	}

	public Label getPatientLabel() {
		return mPatientLabel;
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

	// Método para obtener el tamaño de texto apropiado
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

