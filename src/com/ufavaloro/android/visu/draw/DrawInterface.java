/*****************************************************************************************
 * DrawHelper.java																	 	 *
 * Clase que permite graficar las muestras en pantalla.									 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.draw;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.draw.channel.Channel;
import com.ufavaloro.android.visu.draw.channel.ChannelList;
import com.ufavaloro.android.visu.draw.channel.InfoBox;
import com.ufavaloro.android.visu.draw.channel.Label;
import com.ufavaloro.android.visu.draw.channel.ScreenBitmap;
import com.ufavaloro.android.visu.draw.channel.SignalBox;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;
import com.ufavaloro.android.visu.userinterface.MainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawInterface extends SurfaceView implements SurfaceHolder.Callback {

/*****************************************************************************************
* Inicio de atributos de clase 															 *
*****************************************************************************************/
	// Canales
	private ChannelList mChannelList = new ChannelList();

	// Thread de graficación
	private DrawingThread mDrawingThread;

	// Pincel
	private Paint mPaint = new Paint();

	// Array de Punteros Touch (dedos apoyados en la pantalla)
	private SparseArray<TouchPointer> mTouchPointer = new SparseArray<TouchPointer>();

	// Alto de la pantalla
	private int mTotalHeight;

	// Ancho de la pantalla
	private int mTotalWidth;

	// Cantidad de páginas del visualizador
	private int mTotalPages = 4;

	// Sensibilidad del Zoom en X
	private double mHorizontalSensitivity = 0.025;

	// Sensibilidad del Zoom en Y
	private int mVerticalSensitivity = 3;

	// Valor anterior de la distancia del Zoom en X
	private double mOldDx = 0;

	// Valor anterior del perímetro del Zoom en Y
	private double mOldDy = 0;

	// Sensibilidad del desplazamiento de la Señal
	private int mPanSensitivity;

	// Valor anterior en X del desplazamiento anterior
	private float mOldX = 0;

	// Bool para reconocer el segundo tapping para pausear
	private boolean mFirstTap = false;

	// Se utiliza para calcular el tiempo entre eventos táctiles
	private long mTapTime = 0;

	// Matriz de ubicación
	private ReferenceMatrix mReferenceMatrix;

	private int mPressedChannel;

	private boolean mFlagUp = false;

	private boolean mUiVisibility = false;

	public boolean currentlyRecording;
	
	private boolean mDrawOk = false;
	
	public boolean onlineDrawBuffersOk = false;
		
	private double mHorizontalZoomThreshold;
	private double mVerticalZoomThreshold;
	private double mDxAcum = 0;
	private double mDyAcum = 0;
	private boolean mHeartBeat;
	private int mHeartBeatChannel;
	
	IconsManager mIconsManager;
/*****************************************************************************************
* Inicio de métodos de clase 															 *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales 																	 *
*****************************************************************************************/
	// Constructor
	public DrawInterface(Context context, AttributeSet attrs) {
		// Método de superclase
		super(context, attrs);
		// Le informamos al Holder que va a recibir llamados de este SurfaceView
		getHolder().addCallback(this);
	}

	// Método para inicializar las variables principales
	private void initialSetup() {
		
		// Obtengo ancho y alto de la pantalla
		mTotalWidth = this.getWidth();
		mTotalHeight = this.getHeight();

		// Umbral de detección de Zoom
		mHorizontalZoomThreshold = (float) (mTotalWidth*0.005);
		mVerticalZoomThreshold = (float) (mTotalHeight*0.001);

		mHorizontalZoomThreshold = (float) (mTotalWidth*0.005);
		mVerticalZoomThreshold = (float) (mTotalHeight*0.001);
		
		// Genero matriz posicional
		mReferenceMatrix = new ReferenceMatrix(mTotalHeight, mTotalWidth);
		mReferenceMatrix.setVerticalDivisor(Channel.getSignalBoxWidthPercent()*mTotalWidth);
		
		// Inicializo bitmaps
		mIconsManager = new IconsManager(getContext());
		bitmapSetup();
	}
	
	// Método que configura los Bitmaps
	private void bitmapSetup() {
		mIconsManager.setIconsWidth((int) (0.07 * mTotalWidth));
		mIconsManager.setIconsHeight((int) (0.11 * mTotalHeight));
		mIconsManager.setIconsLeftPadding((int) (0.05 * mTotalHeight));
		mIconsManager.setIconsUpperPadding((int) (0.1 * mTotalHeight));
		mIconsManager.setup();
		
		// Fondo de pantalla inicial
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.background_logo);
		mIconsManager.setBackgroundLogoWidth((int) (0.5 * mTotalWidth));
		mIconsManager.setBackgroundLogoHeight((int) (0.3 * mTotalHeight));
		mIconsManager.setBackgroundLogo(Bitmap.createScaledBitmap(bitmap
																  , mIconsManager.getBackgroundLogoWidth()
																  , mIconsManager.getBackgroundLogoHeight()
																  , false));
		mIconsManager.setBackgroundLogoX((int) ((0.5 * mTotalWidth) / 2));
		mIconsManager.setBackgroundLogoY((int) ((0.7 * mTotalHeight) / 2));
		
		// Heart icon
		
	}

	// Método que recibe y almacena muestras
	public void drawSamples(short[] samples, int channelNumber) {
		Channel channel = mChannelList.getChannelAtKey(channelNumber);
		
		if(mChannelList.size() == 0 || channel == null || mDrawOk == false) return;
		
		if (!channel.isPaused()) channel.setSamples(samples);
	}
	
	public void drawSample(short sample, int channelNumber) {
		Channel channel = mChannelList.getChannelAtKey(channelNumber);
		
		if(mChannelList.size() == 0 || channel == null || mDrawOk == false) return;
		
		if (!channel.isPaused()) channel.setSample(sample);
	}

	public synchronized void draw(short sample, int channelNumber) {
		Channel channel = mChannelList.getChannelAtKey(channelNumber);
		
		if(mChannelList.size() == 0 || channel == null || mDrawOk == false) return;
		
		if (!channel.isPaused()) channel.setSample(sample);
	}
	
	// Método que se llama cuando se dibuja en el SurfaceView
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		
		// Método de superclase
		super.onDraw(canvas);
		
		// Fondo gris
		canvas.drawColor(Color.LTGRAY);
		
		// Dibujo canales
		drawChannels(canvas);
		
		// Dibujo Bitmaps
		drawBitmaps(canvas);
		
		// Dibujo divisiones
		drawDivisions(canvas);
	
		// Dibujo otros labels
		drawOtherLabels(canvas);
	
	}

/*****************************************************************************************
* Métodos para dibujar en el SurfaceView 												 *
*****************************************************************************************/
	// Método que grafica los canales
	private synchronized void drawChannels(Canvas canvas) {
		// Dibujo signal boxes
		drawSignalBoxes(canvas);	
		// Dibujo info boxes
		drawInfoBoxes(canvas);
	}
	
	// Método que grafica Bitmaps
	private synchronized void drawBitmaps(Canvas canvas) {

		ColorFilter filter = new LightingColorFilter(Color.LTGRAY, 1);
		mPaint.setColorFilter(filter);
		mPaint.setAlpha(250);

		// Dibujo ícono de estudio nuevo
		if (mUiVisibility == true || mChannelList.size() == 0) {
			canvas.drawBitmap(mIconsManager.getNewStudyIcon()
							  , mIconsManager.getNewStudyIconX()
							  , mIconsManager.getNewStudyIconY()
							  , mPaint);
		}

		// Dibujo ícono de configurar canales
		if (mUiVisibility == true && mChannelList.getOnlineChannelsQty() > 0) {
			canvas.drawBitmap(mIconsManager.getConfigureChannelsIcon()
							  , mIconsManager.getConfigureChannelsIconX()
							  , mIconsManager.getConfigureChannelsIconY()
							  , mPaint);
		}
		
		// Dibujo ícono de parar estudio
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == true) {
			canvas.drawBitmap(mIconsManager.getStopStudyIcon()
							  , mIconsManager.getStopStudyIconX()
							  , mIconsManager.getStopStudyIconY()
							  , mPaint);
		}

		// Dibujo fondo
		if (mChannelList.size() == 0) {
			mPaint.setAlpha(130);
			canvas.drawBitmap(mIconsManager.getBackgroundLogo()
							  , mIconsManager.getBackgroundLogoX()
							  , mIconsManager.getBackgroundLogoY()
							  , mPaint);
			mPaint.setAlpha(255);
		}

		mPaint.setColorFilter(null);

	}

	// Método que dibuja el SignalBox
	private synchronized void drawSignalBoxes(Canvas canvas) {

		// Dibujo señal
		drawSignals(canvas);
		
		// Dibujo labels de amplitud
		drawAmplitudeLabels(canvas);
		
		// Dibujo labels de voltage
		//drawVoltageLabels(canvas);
		
		// Dibujo labels de tiempo
		drawTimeLabels(canvas);
	}

	// Método que grafica la señal
	private synchronized void drawSignals(Canvas canvas) {
		for (int i = 0; i < mChannelList.size(); i++) {

			Channel channel = mChannelList.getChannelAtIndex(i);
			
			// Obtengo resolución del canal
			int bits = channel.getStudyData().getAcquisitionData().getBits();
			float adcSteps = (float) (Math.pow(2, bits) - 1);
			float halfAdcSteps = adcSteps / 2;

			// Obtengo offset del canal
			float offset = channel.getSignalBox().getMidLine();

			// Obtengo color del canal
			int[] rgb = channel.getColor().getRGB();
			setPaint(Color.rgb(rgb[0], rgb[1], rgb[2]), 5);

			// Obtengo zooms del canal
			float verticalZoom = channel.getSignalBox().getDrawBuffer().getVerticalZoom();
			float horizontalZoom = channel.getSignalBox().getDrawBuffer().getHorizontalZoom();

			// Calculo correción de zoom horizontal
			float zoomCorrection = (1 - horizontalZoom) * SignalBox.getWidth();

			// Dibujo punto a punto el canal
			for (int j = 0; j < SignalBox.getWidth() - 1; j++) {

				// Obtengo muestras
				int y0Sample = channel.getSignalBox().getDrawBuffer().getSample(j);
				int yfSample = channel.getSignalBox().getDrawBuffer().getSample(j + 1);

				// Valor x inicial
				float x0 = horizontalZoom * (j) + zoomCorrection;

				// Valor y inicial
				float y0 = offset - (float) ((verticalZoom * (y0Sample - halfAdcSteps) / halfAdcSteps));

				// Valor x final
				float xf = horizontalZoom * (j + 1) + zoomCorrection;

				// Valor y final
				float yf = offset - (float) ((verticalZoom * (yfSample - halfAdcSteps) / halfAdcSteps));

				// Dibujo si y solo si el punto está dentro de los límites gráficos del signalbox
				float lowerBound = offset - (InfoBox.getBoxHeight() / 2);
				float upperBound = offset + (InfoBox.getBoxHeight() / 2) - 1;

				if ((y0 > lowerBound && y0 < upperBound) && (yf > lowerBound && yf < upperBound)) {
					canvas.drawPoint(x0, y0, mPaint);
					//canvas.drawLine(x0, y0, xf, yf, mPaint);
				}
				
			}
		}
	}
	
	// Método para dibujar las amplitudes de la señal
	private synchronized void drawAmplitudeLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			Channel channel = mChannelList.getChannelAtIndex(i);

			float padding = (float) (SignalBox.getWidth() * 0.02);
			setPaint(Color.GRAY, 1);
			mPaint.setAlpha(100);

			Label labelMinAmplitude = channel.getSignalBox().getLabelMinAmplitude();
			String minAmplitude = labelMinAmplitude.getText();
			float yMinAmplitude = labelMinAmplitude.getY();
			float correction = channel.getSignalBox().getAmplitudeCorrection();

			mPaint.setTextSize(labelMinAmplitude.getTextSize());
			canvas.drawLine(0, yMinAmplitude - correction, padding, yMinAmplitude - correction, mPaint);
			canvas.drawText(minAmplitude, padding, yMinAmplitude, mPaint);

			Label labelMaxAmplitude = channel.getSignalBox().getLabelMaxAmplitude();
			String maxAmplitude = labelMaxAmplitude.getText();
			float yMaxAmplitude = labelMaxAmplitude.getY();

			mPaint.setTextSize(labelMaxAmplitude.getTextSize());
			canvas.drawLine(0, yMaxAmplitude, padding, yMaxAmplitude, mPaint);
			canvas.drawText(maxAmplitude, padding, yMaxAmplitude, mPaint);

			mPaint.setAlpha(255);
		}

	}

	// Método para dibujar los valores de voltaje de la señal
	private synchronized void drawVoltageLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			SignalBox signalBox = mChannelList.getChannelAtIndex(i).getSignalBox();
						
			float padding = (float) (SignalBox.getWidth() * 0.02);
			setPaint(Color.GRAY, 1);
			mPaint.setAlpha(100);

			Label labelMinVoltage = signalBox.getLabelMinVoltage();
			String minVoltage = labelMinVoltage.getText();
			float yMinVoltage = labelMinVoltage.getY();
			float correction = signalBox.getVoltageCorrection();

			mPaint.setTextSize(labelMinVoltage.getTextSize());
			canvas.drawLine(0, yMinVoltage - 3, padding, yMinVoltage - 3,
					mPaint);
			canvas.drawText(minVoltage, padding, yMinVoltage - 3, mPaint);

			Label labelMaxVoltage = signalBox.getLabelMaxVoltage();
			String maxVoltage = labelMaxVoltage.getText();
			float yMaxVoltage = labelMaxVoltage.getY();

			mPaint.setTextSize(labelMaxVoltage.getTextSize());
			canvas.drawLine(0, yMaxVoltage - correction + 3, padding,
					yMaxVoltage - correction + 3, mPaint);
			canvas.drawText(maxVoltage, padding, yMaxVoltage + 3, mPaint);
		}
	}

	// Método para dibujar los valores de tiempo de la señal
	private synchronized void drawTimeLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			Channel channel = mChannelList.getChannelAtIndex(i);

			float timePixels = channel.getSignalBox().getLabelTimePixels();
			Label labelTime = channel.getSignalBox().getTimeLabel();

			String time = labelTime.getText();
			float yTime = labelTime.getY();
			float xTime = labelTime.getX() - 20;

			// Barra horizontal de tiempo
			setPaint(Color.GRAY, 1);
			mPaint.setAlpha(150);
			mPaint.setTextSize(labelTime.getTextSize());
			canvas.drawLine(xTime, yTime, xTime + timePixels, yTime, mPaint);

			// Label que muestra el tiempo
			float labelTimeWidth = labelTime.getBoundingBox().width();
			float labelTimeHeigth = labelTime.getBoundingBox().height();
			canvas.drawText(time,
						   (float) (xTime - (labelTimeWidth * 0.5) + (timePixels * 0.5)),
					       (float) (yTime - (labelTimeHeigth * 0.25)), mPaint);

			// Barritas verticales de los costados
			canvas.drawLine(xTime, yTime - 5, xTime, yTime + 5, mPaint);
			canvas.drawLine(xTime + timePixels, yTime - 5, xTime + timePixels,
					yTime + 5, mPaint);
		}
	}

	// Método que dibuja todas las Figuras en el SurfaceView
	private synchronized void drawInfoBoxes(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {
			
			// Obtengo Channel
			Channel channel = mChannelList.getChannelAtIndex(i);
						
			Label label;

			// Obtengo color del canal
			int[] rgb = channel.getColor().getRGB();
			setPaint(Color.rgb(rgb[0], rgb[1], rgb[2]), 2);
			mPaint.setAlpha(200);

			// Dibujo Label de número de Canal
			label = channel.getInfoBox().getChannelLabel();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			// Dibujo label de Fs
			label = channel.getInfoBox().getPatientLabel();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);
			
			// Dibujo Parameter Label y Bitmap
			if(mHeartBeat == true && mHeartBeatChannel == channel.getChannelNumber()) {
				ScreenBitmap bitmap = channel.getInfoBox().getParameterBitmap();
				canvas.drawBitmap(bitmap.getBitmap()
								  , bitmap.getX()
								  , bitmap.getY()
								  , mPaint);
			}
			
			label = channel.getInfoBox().getParameterLabel();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);
						

			// Dibujo Label de Pausa
			setPaint(Color.RED, 2);
			if (channel.isPaused() && channel.isOnline()) {
				label = channel.getInfoBox().getPausedLabel();
				mPaint.setTextSize(label.getTextSize());
				canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);
			}
			
		}

		mPaint.setAlpha(255);

	}
	
	// Método para dibujar las divisiones entre canales
	private synchronized void drawDivisions(Canvas canvas) {
		
		setPaint(Color.GRAY, 3);
		mPaint.setAlpha(130);
		ColorFilter filter = new LightingColorFilter(Color.LTGRAY, 1);
		mPaint.setColorFilter(filter);
		
		for (int i = 0; i < mChannelList.size(); i++) {
			canvas.drawLine(0, i*Channel.getHeight(), mTotalWidth, i*Channel.getHeight(), mPaint);
		}

		if (mChannelList.size() > 0) {
			canvas.drawLine(SignalBox.getWidth(), 0, SignalBox.getWidth(), mTotalHeight, mPaint);
		}

		mPaint.setAlpha(255);
		mPaint.setColorFilter(null);
	}

	// Dibujo otros labels
	private synchronized void drawOtherLabels(Canvas canvas) {
		// Label de los canales ocultados
		if(mChannelList.getHiddenChannelsLabels().size() > 0 && mUiVisibility == true || mChannelList.size() == 0) {
			for(int i = 0; i < mChannelList.getHiddenChannelsLabels().size(); i++) {
				int channelKey = mChannelList.getHiddenChannelsLabels().keyAt(i);
				Label label = mChannelList.getHiddenChannelsLabels().get(channelKey);
				label.setTextSize(getBoundedTextSize(label, mIconsManager.getIconsWidth(), mIconsManager.getIconsHeight()));
				label.setX((int) ((0.05 * mTotalHeight) + (i*mIconsManager.getIconsWidth())));
				label.setY(mTotalHeight - mIconsManager.getIconsHeight());
				Channel deletedChannel = mChannelList.getHiddenChannels().get(channelKey);
				
				int[] rgb = deletedChannel.getColor().getRGB();
				setPaint(Color.rgb(rgb[0], rgb[1], rgb[2]), 5);
				
				mPaint.setAlpha(100);
				canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);
			}
		}
	}
	
	// Método que configura el color y grosor del brush
	private synchronized void setPaint(int color, float strokeWidth) {
		mPaint.setColor(color);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(strokeWidth);
	}

/*****************************************************************************************
* Métodos para manejar los canales y sus características 								 *
*****************************************************************************************/
	/*************************************************************************************
	* Agregar/eliminar canales						 *
	*************************************************************************************/
	public synchronized void addChannel(StudyData studyData, boolean online) {
		if(online) {
			mChannelList.addChannel(mTotalHeight, mTotalWidth, mTotalPages, getContext(), studyData);
		} else {
			mChannelList.addChannel(mTotalHeight, mTotalWidth, getContext(), studyData);
		}

		mReferenceMatrix.addChannel();
		mPanSensitivity = (int) (SignalBox.getWidth() * 0.012);
	}

	public synchronized void hideChannel(int channelIndex) {
		mChannelList.hideChannel(channelIndex);
		mReferenceMatrix.removeChannel();
	}
	
	public synchronized void removeChannel(int channelIndex) {
		mChannelList.removeChannel(channelIndex);
		mReferenceMatrix.removeChannel();
	}
	
	/*************************************************************************************
	* Menú de usuario 																	 *
	*************************************************************************************/
	// Menú con las opciones del canal
	private void channelMenu(int channelNumber) {
		((MainActivity) getContext()).channelOptionsDialog(channelNumber);
	}

/*****************************************************************************************
* Métodos para el manejo de eventos táctiles 											 *
*****************************************************************************************/
	/*************************************************************************************
	* Listener principal 																 *
	*************************************************************************************/
	// Método que responde a los eventos táctiles
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int touchPointerIndex = event.getActionIndex();
		int touchPointerId = event.getPointerId(touchPointerIndex);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// Puntero apoyado
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:

			TouchPointer touchPointer = new TouchPointer();
			touchPointer.x = event.getX(touchPointerIndex);
			touchPointer.x0 = touchPointer.x;
			touchPointer.y = event.getY(touchPointerIndex);
			touchPointer.y0 = touchPointer.y;
			mTouchPointer.put(touchPointerId, touchPointer);

			onTouch_PauseChannel();
			onTouch_NewStudyIcon();
			onTouch_ConfigureChannelsIcon();
			onTouch_StopStudyIcon();
			onTouch_HiddenChannelLabel();
			mLongPressHandler.postDelayed(longPressed, 1000);

			break;

		// Puntero levantado
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			
			mTouchPointer.remove(touchPointerId);
			mFlagUp = true;
			mLongPressHandler.removeCallbacks(longPressed);
			
			break;

		// Puntero en movimiento
		case MotionEvent.ACTION_MOVE:
			
			for (int i = 0; i < event.getPointerCount(); i++) {
				TouchPointer tp2 = mTouchPointer.get(event.getPointerId(i));
				if (tp2 != null) {
					tp2.x = event.getX(i);
					tp2.y = event.getY(i);
				}
			}
			
			onTouch_Pan();
			onTouch_HorizontalZoom();
			onTouch_VerticalZoom();
			
			break;
		}
		return true;
	}
	
	// Deleted Channel Labels
	private void onTouch_HiddenChannelLabel() {
		if (mUiVisibility == true || mChannelList.size() == 0) {

			TouchPointer tp = mTouchPointer.valueAt(0);
		
			for(int i = 0; i < mChannelList.getHiddenChannelsLabels().size(); i++) {
				Label label = mChannelList.getHiddenChannelsLabels().valueAt(i);
				int width = label.getBoundingBox().width();
				int height = label.getBoundingBox().height();
				if (tp.x > label.getX() && tp.x < label.getX() + width) {
					if (tp.y < label.getY() && tp.y > label.getY() - height) {
						mChannelList.restoreChannel(mChannelList.getHiddenChannels().keyAt(i));
						mReferenceMatrix.addChannel();
					}
				}
			}
		}
	}

	// Botón de nuevo estudio
	private void onTouch_NewStudyIcon() {
		if (mUiVisibility == true || mChannelList.size() == 0) {

			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = mIconsManager.getNewStudyIcon().getWidth();
			int height = mIconsManager.getNewStudyIcon().getHeight();

			if (tp.x > mIconsManager.getNewStudyIconX() && tp.x < mIconsManager.getNewStudyIconX() + width) {
				if (tp.y > mIconsManager.getNewStudyIconY() && tp.y < mIconsManager.getNewStudyIconY() + height) {
					((MainActivity) getContext()).mainMenuDialog();
				}
			}
		}
	}

	// Botón de configurar canales
	private void onTouch_ConfigureChannelsIcon() {
		if (mUiVisibility == true && mChannelList.getOnlineChannelsQty() > 0) {
			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = mIconsManager.getConfigureChannelsIcon().getWidth();
			int height = mIconsManager.getConfigureChannelsIcon().getHeight();

			if (tp.x > mIconsManager.getConfigureChannelsIconX() && tp.x < mIconsManager.getConfigureChannelsIconX() + width) {
				if (tp.y > mIconsManager.getConfigureChannelsIconY() && tp.y < mIconsManager.getConfigureChannelsIconY() + height) {
					((MainActivity) getContext()).onlineChannelPropertiesDialog(-1);
				}
			}
		}
	}
	
	// Botón de parar estudio
	private void onTouch_StopStudyIcon() {
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == true) {

			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = mIconsManager.getStopStudyIcon().getWidth();
			int height = mIconsManager.getStopStudyIcon().getHeight();

			if (tp.x > mIconsManager.getStopStudyIconX() && tp.x < mIconsManager.getStopStudyIconX() + width) {
				if (tp.y > mIconsManager.getStopStudyIconY() && tp.y < mIconsManager.getStopStudyIconY() + height) {
					((MainActivity) getContext()).stopStudyDialog();
				}
			}
		}
	}

	// Qué hago cuando el evento es Zoom en X?
	private void onTouch_HorizontalZoom() {

		if (mChannelList.size() == 0 || mTouchPointer.size() != 2) return;

		// Calculo distancia entre los dedos
		TouchPointer tp1 = mTouchPointer.valueAt(0);
		TouchPointer tp2 = mTouchPointer.valueAt(1);

		double newDx = Math.abs(tp1.x - tp2.x);

		int channelNumber = mReferenceMatrix.getChannel(tp1.y0, tp1.x0) - 1;

		if (channelNumber >= 0) {

			Channel channel = mChannelList.getChannelAtIndex(channelNumber);
			
			float oldZoomValue = 1;
			float newZoomValue = 1;
			
			// Si la distancia aumenta => Zoom In
			if (newDx > mOldDx) {
				mDxAcum = (newDx-mOldDx) + mDxAcum;
				if(mDxAcum > mHorizontalZoomThreshold) {
					oldZoomValue = channel.getHorizontalZoom();
					newZoomValue = (float) (oldZoomValue + mHorizontalSensitivity);
					channel.setHorizontalZoom(newZoomValue);
					mDxAcum = 0;
				}
			}

			// Si la distancia disminuye => Zoom Out
			if (newDx < mOldDx) {
				mDxAcum = (newDx-mOldDx) + mDxAcum;
				if(mDxAcum < -mHorizontalZoomThreshold) {
					oldZoomValue = channel.getHorizontalZoom();
					if (oldZoomValue >= 1 + mHorizontalSensitivity) {
						newZoomValue = (float) (oldZoomValue - mHorizontalSensitivity);
						channel.setHorizontalZoom(newZoomValue);
					}
					mDxAcum = 0;
				}
			}

			// Renuevo el valor de la distancia
			mOldDx = newDx;
		}
	}

	// Qué hago cuando el evento es Zoom en Y ?
	private void onTouch_VerticalZoom() {

		if (mChannelList.size() == 0 || mTouchPointer.size() != 2) return;

		// Obtengo las coordenadas de los tres dedos
		TouchPointer tp1 = mTouchPointer.valueAt(0);
		TouchPointer tp2 = mTouchPointer.valueAt(1);

		double newDy = Math.abs(tp1.y - tp2.y);
		int channelNumber = mReferenceMatrix.getChannel(tp1.y0, tp1.x0) - 1;
		
		if (channelNumber >= 0) {
			
			Channel channel = mChannelList.getChannelAtIndex(channelNumber);
			
			float oldZoomValue = 1;
			float newZoomValue = 1;

			// Si aumenta el perímetro => Zoom Out
			if (newDy > mOldDy) {
				mDyAcum = (newDy-mOldDy) + mDyAcum;
				if(mDyAcum > mVerticalZoomThreshold) {
					if (channel.getVerticalZoom() < Channel.getHeight()/2 - mVerticalSensitivity) {
						oldZoomValue = channel.getVerticalZoom();
						newZoomValue = (float) (oldZoomValue + mVerticalSensitivity);
						channel.setVerticalZoom(newZoomValue);
					}
					mDyAcum = 0;
				}
			}

			// Si disminuye el perímetro => Zoom In
			if (newDy < mOldDy) {
				mDyAcum = (newDy-mOldDy) + mDyAcum;
				if(mDyAcum < -mVerticalZoomThreshold) {
					if (channel.getVerticalZoom() > mVerticalSensitivity) {
						oldZoomValue = channel.getVerticalZoom();
						newZoomValue = (float) (oldZoomValue - mVerticalSensitivity);
						channel.setVerticalZoom(newZoomValue);
					}
					mDyAcum = 0;
				}
			}

			// Renuevo el valor del perímetro
			mOldDy = newDy;
		}
	}

	// Qué hago cuando el evento es desplazar la Señal?
	private void onTouch_Pan() {

		if (mChannelList.size() == 0) return;
		if (mTouchPointer.size() == 1) {

			Channel channel;
			TouchPointer tp = mTouchPointer.valueAt(0);
			int channelNumber = mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;

			if (channelNumber >= 0) {

				channel = mChannelList.getChannelAtIndex(channelNumber);

				if (channel.isPaused() == true || !channel.isOnline()) {
					// Si muevo el dedo hacia la derecha...
					if (tp.x > mOldX) {
						channel.getSignalBox().panRight(mPanSensitivity);
					}

					// Si muevo el dedo hacia la izquierda...
					if (tp.x < mOldX) {
						channel.getSignalBox().panLeft(mPanSensitivity);
					}

					mOldX = (float) tp.x;
				}
			}
		}
	}

	// Qué hago cuando el evento es un Double Tap?
	private void onTouch_PauseChannel() {

		if (mChannelList.size() == 0) return;

		TouchPointer tp = mTouchPointer.valueAt(0);
		int totalPointers = mTouchPointer.size();
		long time = System.currentTimeMillis();
		int channelNumber = mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;
		long deltaTime = time - mTapTime;
		
		// Si no es el primer tap, el tiempo entre este tap y el anterior es < a
		// 300 mS, hay un solo dedo apoyado y este fue apoyado en la zona de
		// visualizacion
		if (mFirstTap == true && deltaTime <= 300 && tp.x < SignalBox.getWidth()) {
			if (totalPointers == 1 && channelNumber == mPressedChannel && channelNumber >= 0 && mFlagUp == true) {
				// Flipeo el bool de pausa
				Channel channel = mChannelList.getChannelAtIndex(channelNumber);
				boolean oldPausedValue = channel.isPaused();
				boolean newPausedValue = !oldPausedValue;
				channel.setPaused(newPausedValue);
				// Si es el caso que estoy des-pauseando, igualo los índices del Buffer.
				// Esto hace que se resuma la graficación desde la posición de la última
				// muestra registrada.
				if (channel.isPaused() == false) channel.getSignalBox().resetGraphingIndex();
				// Vuelvo a poner en false el bool de Primer Tap
				mFirstTap = false;
				mFlagUp = false;
				mTapTime = 0;
			}
			// Si es el primer tap
		} else if (tp.x < SignalBox.getWidth()) {
			if (channelNumber >= 0) {
				mPressedChannel = channelNumber;
					// Pongo en true el bool de Primer Tap
					mFirstTap = true;
					// Obtengo el tiempo actual para restarle al tiempo del segundo tap y 
					// chequear que no haya más de 300 mS entre Taps
					mTapTime = time;
				}
		}
	}

	// Qué hago cuando el evento es un Long Press sobre un InfoBox?
	final Handler mLongPressHandler = new Handler();
	Runnable longPressed = new Runnable() {

		public void run() {

			if (mChannelList.size() < 0) return;

			final TouchPointer tp = mTouchPointer.valueAt(0);
			final int channelNumber = -mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;
			int isInfoBox = mReferenceMatrix.getChannel(tp.y0, tp.x0);

			if (isInfoBox < 0) {
				channelMenu(channelNumber);
			}
		}
	};

/*****************************************************************************************
* Thread de graficación y métodos afines 												 *
*****************************************************************************************/
	// Thread de graficación (inner class)
	private class DrawingThread extends Thread {
		
		// Objeto que "une" el Bitmap (Canvas) con el SurfaceView
		private SurfaceHolder mSurfaceHolder;
		
		// Bitmap sobre el cual dibujo
		private Canvas mCanvas;
		
		// Candado que traba el thread
		private Object mPauseLock;
		
		// Flag de pausa
		private boolean mPaused;
		
		// Flag de run
		private boolean mRun;
		
		// Tiempo de espera entre ciclos de graficación
		private long mSleep;

		// Constructor
		public DrawingThread(SurfaceHolder mSurfaceHolder, DrawInterface mPlotSurfaceView) {
			this.mSurfaceHolder = mSurfaceHolder;
			mRun = false;
			mPauseLock = new Object();
			mPaused = false;
			mSleep = 0;
		}

		// Thread.run()
		@SuppressLint("WrongCall")
		public void run() {

			this.setPriority(MAX_PRIORITY);

			while (mRun) {

				mCanvas = null;

				// Dibujo muestra
				try {
					// Lockeo el canvas para poder dibujar
					mCanvas = mSurfaceHolder.lockCanvas();
					// Si el locking fue exitoso, obtuve un objeto de tipo
					// Canvas
					if (mCanvas != null) {
						// Si no hay otro onDraw dibujando sobre el Canvas
						synchronized (mSurfaceHolder) {
							// Dibujo
							onDraw(mCanvas);
						}
					}
					// Terminé de dibujar
				} finally {
					// Unlockeo el Canvas
					if (mCanvas != null)
						mSurfaceHolder.unlockCanvasAndPost(mCanvas);
				}

				// Candado de pausa
				// Deja el Thread en espera utilizando wait() hasta que mPaused
				// == false
				synchronized (mPauseLock) {
					while (mPaused) {
						try {
							mPauseLock.wait();
						} catch (InterruptedException e) {
						}
					}
				}

				try {
					Thread.sleep(mSleep);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}// while(mRun)
		}// run()

		// Pauseo el Thread
		public void onPause() {
			synchronized (mPauseLock) {
				mPaused = true;
			}
		}

		// Resumo el Thread
		public void onResume() {
			synchronized (mPauseLock) {
				mPaused = false;
				mPauseLock.notifyAll();
			}
		}

		// Setter de mRun
		public void setRunning(boolean mRun) {
			this.mRun = mRun;
		}

	}//DrawingThread

	// Método para empezar a graficar
	private void startDrawingThread() {
		mDrawingThread = new DrawingThread(getHolder(), this);
		mDrawingThread.setRunning(true);
		mDrawingThread.start();
	}

	// Método para parar la graficación
	private void stopDrawingThread() {
		boolean retry = true;
		mDrawingThread.setRunning(false);
		while (retry) {
			try {
				mDrawingThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

/*****************************************************************************************
* Otros métodos 																		 *
*****************************************************************************************/
	// Método para obtener el tamaño de texto apropiado para los labels del
	// InfoBox
	private int getBoundedTextSize(Label label, double boxWidth, double boxHeight) {
		
		Rect rect = new Rect();
		int i = 0;

		while (true) {
			mPaint.setTextSize(i);
			mPaint.getTextBounds(label.getText(), 0, label.getText().length(),
					rect);
			double width = rect.width();
			double height = rect.height();
			if (width > boxWidth || height > boxHeight) {
				break;
			}
			i++;
		}

		return i;
	}

	// Método para saber si el Status Bar y Action Bar están visibles
	public void setUiVisibility(boolean mUiVisibility) {
		this.mUiVisibility = mUiVisibility;
	}

	public void startDrawing() {
		mDrawOk = true;
	}
	
	public void stopDrawing() {
		mDrawOk = false;
	}
	
	public synchronized void heartBeat(final int channel) {
		
		Runnable otherRunnable = new Runnable() {
			  public void run() {
					mHeartBeat = true;
					mHeartBeatChannel = channel;
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mHeartBeat = false;
			  }
			};
			
		Thread thread = new Thread(null, otherRunnable, "Background");
		thread.start();
		
	}

/*****************************************************************************************
* Métodos de Ciclo de Vida 																 *
*****************************************************************************************/
	// Método que se llama cuando se crea el SurfaceView
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// Inicializo variables
		initialSetup();
		// Inicializo thread de graficación
		startDrawingThread();
		// Todo OK. Instancio mSurfaceViewVisualizador en Visualizador.java
		((MainActivity) getContext()).setupAfterSurfaceCreated();
	}

	// Método que se llama cuando el SurfaceView sufre algún cambio
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	// Método que se llama cuando se destruye el SurfaceView
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mDrawingThread.setRunning(false);
		stopDrawingThread();
	}

	public ChannelList getChannels() {
		return mChannelList;
	}

}// DrawingSurface
