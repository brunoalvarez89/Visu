package com.ufavaloro.android.visu.draw.channel.infobox;

import java.util.ArrayList;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.draw.channel.Label;
import com.ufavaloro.android.visu.draw.channel.ScreenBitmap;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;


public class InfoBox{
	
	public StudyData studyData;
	
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
	private double mInterLabelPadding = mHeight*0; 
	
	// Label con el # de canal
	private Label mChannelLabel;
	private final double mChannelLabelHeightPercent = 0.2;
	
	// Label de Nombre de Paciente
	private Label mPatientLabel;
	private final double mPatientLabelHeightPercent = 0.1;
	
	// Parameter Label 
	private Label mParameterLabel;
	private final double mParameterLabelHeightPercent = 0.2;
	private ScreenBitmap mParameterBitmap;
	private final double mParameterBitmapHeightPercent = 0.2;

	// Label de Pausa
	private Label mPausedLabel;
	private final double mPausedLabelHeightPercent = 0.1;

	// Canal que representa
	private int mAdcChannelNumber;
	private int mChannelIndex;
	
	// Color
	private RGB mColor;

	// Application Context (needed for decodifying bitmaps)
	private Context mContext;
	
	public InfoBox(int channelNumber, Context context, StudyData studyData) {
		this.studyData = studyData;
		mContext = context;
		mAdcChannelNumber = channelNumber;
		
		createChannelLabel();
		createPatientLabel();
		createParameterLabel();
		createPausedLabel();
	}
	
	// Cuando se agrega o elimina un canal, se redimensionan los boxes
	public void update(int height, int channelIndex) {
		
		setHeight(height);
		setChannelIndex(channelIndex);
		
		mInterLabelPadding = mHeight*0.02;
		
		mLabelList.clear();
		
		createChannelLabel();
		createPatientLabel();
		createParameterLabel();
		createPausedLabel();
		
		// Actualizo tamaños
		updateChannelLabelSize();
		updatePatientLabelSize();
		updateParameterLabelSize();
		updatePausedLabelSize();
		
		// Seteo tamaño mínimo global de texto
		setMinimumSize();
		
		// Actualizo posiciones
		updateChannelLabelPosition();
		updatePatientLabelPosition();
		updateParameterLabelPosition();
		UpdatePausedLabelPosition();

	}

	// Channel Label
	public void createChannelLabel() {
		String channelLabel;
		char[] studyType =  studyData.getAcquisitionData().getStudyType();
		if(studyType[0] != 0) {
			char[] aux = studyData.getAcquisitionData().getStudyType();
			channelLabel = String.valueOf(StudyType.values((int)studyType[0]));
		} else {
			channelLabel = "Canal " + String.valueOf(mAdcChannelNumber + 1);
		}
		mChannelLabel = new Label(0, 0, 0, channelLabel);
	}

	protected void updateChannelLabelSize() {
		int textSize = getBoundedTextSize(mChannelLabel, mLabelWidthPercent * mWidth
										  , mChannelLabelHeightPercent * mHeight);
		mChannelLabel.setTextSize(textSize);
		
		mLabelList.add(mChannelLabel);
	}

	private void updateChannelLabelPosition() {
		
		mChannelLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mChannelLabel.setY((int) (mChannelLabel.getBoundingBox().height() 
								  + mChannelIndex*mHeight
								  + mInterLabelPadding));
	
	}
	
	public Label getChannelLabel() {
		return mChannelLabel;
	}
	
	// Patient Label
	protected void createPatientLabel() {
		String patientLabel;
		if(studyData.getPatientData() != null) {
			String patientName = "";
			String patientSurname ="";
			if(studyData.getPatientData().getPatientName() != null) {
				patientName = String.valueOf(studyData.getPatientData().getPatientName());
				patientName = patientName.trim().replace('_', ' ');
			} else {
				patientName = "";
			}
			
			if(studyData.getPatientData().getPatientSurname() != null) {
				patientSurname = String.valueOf(studyData.getPatientData().getPatientSurname());
				patientSurname = patientSurname.trim().replace('_', ' ');
			} else {
				patientSurname = "";
			}
			
			patientLabel = String.valueOf(patientSurname) + ", " + String.valueOf(patientName);
		
		} else {
			patientLabel = "Sin Paciente";
		}
		
		mPatientLabel = new Label(0, 0, 0, patientLabel);
	}
	
	protected void updatePatientLabelSize() {
		int textSize = getBoundedTextSize(mPatientLabel, mLabelWidthPercent * mWidth
										  , mPatientLabelHeightPercent * mHeight);
		mPatientLabel.setTextSize(textSize);

		mLabelList.add(mPatientLabel);
	}
	
	private void updatePatientLabelPosition() {
		mPatientLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPatientLabel.setY((int) (mPatientLabel.getBoundingBox().height()
					         + mChannelLabel.getY()
					         + mInterLabelPadding));
	}
	
	public Label getPatientLabel() {
		return mPatientLabel;
	}
	
	// Parameter Label	
	private void createParameterLabel() {
		String parameterLabel = "?";
		mParameterLabel = new Label(0, 0, 0, parameterLabel);
		mParameterBitmap = new ScreenBitmap(mContext, R.drawable.heart);
	}
	
	private void updateParameterLabelSize() {
		int textSize = getBoundedTextSize(mParameterLabel, mLabelWidthPercent * mWidth
				  						  , mParameterLabelHeightPercent * mHeight);
		mParameterLabel.setTextSize(textSize);

		mLabelList.add(mParameterLabel);
	}
	
	private void updateParameterLabelPosition() {
		mParameterBitmap.scale(mParameterLabel.getBoundingBox().height()*2
				   , mParameterLabel.getBoundingBox().height()*2);
		mParameterBitmap.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mParameterBitmap.setY((int) (mPatientLabel.getY() + mInterLabelPadding));
		
		mParameterLabel.setX((int) (mParameterBitmap.getX() + mParameterBitmap.mWidth + mLeftPadding));
		mParameterLabel.setY((int) (mParameterLabel.getBoundingBox().height()
					         + mPatientLabel.getY()
					         + mInterLabelPadding));
	}
	
	public Label getParameterLabel() {
		return mParameterLabel;
	}
	
	public ScreenBitmap getParameterBitmap() {
		return mParameterBitmap;
	}
	
	// Paused Label
	private void createPausedLabel() {
		String text = "EN PAUSA";
		mPausedLabel = new Label(0, 0, 0, text);
	}
	
	private void updatePausedLabelSize() {
		int textSize = getBoundedTextSize(mPausedLabel, mLabelWidthPercent * mWidth
										  , mPausedLabelHeightPercent * mHeight);
		mPausedLabel.setTextSize(textSize);
		
		mLabelList.add(mPausedLabel);
	}	
	
	private void UpdatePausedLabelPosition() {	
		mPausedLabel.setX((int) (mVerticalDivisorXPosition + mLeftPadding));
		mPausedLabel.setY((int) ((mChannelIndex+1)*mHeight - mInterLabelPadding));
	}	
	
	public Label getPausedLabel() {
		return mPausedLabel;
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

	public int getChannel() {
		return mAdcChannelNumber;
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

