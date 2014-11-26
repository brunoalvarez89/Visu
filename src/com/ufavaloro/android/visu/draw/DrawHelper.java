/*****************************************************************************************
 * DrawHelper.java																	 	 *
 * Clase que permite graficar las muestras en pantalla.									 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.draw;

import java.text.DecimalFormat;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.draw.channel.Channel;
import com.ufavaloro.android.visu.draw.channel.ChannelList;
import com.ufavaloro.android.visu.draw.channel.InfoBox;
import com.ufavaloro.android.visu.draw.channel.Label;
import com.ufavaloro.android.visu.draw.channel.SignalBox;
import com.ufavaloro.android.visu.storage.data.StudyData;
import com.ufavaloro.android.visu.study.StudyActivity;

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

public class DrawHelper extends SurfaceView implements SurfaceHolder.Callback {

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

	public RGB[] mColorArray = new RGB[10];

	private int mPressedChannel;

	private boolean mFlagUp = false;

	private boolean mUiVisibility = false;

	public boolean currentlyRecording;

	private Label mLabelAwaitingConnections;
	
	private boolean mDrawOk = false;
	
	public boolean onlineDrawBuffersOk = false;
		
	private double mHorizontalZoomThreshold;
	private double mVerticalZoomThreshold;
	private double mDxAcum = 0;
	private double mDyAcum = 0;

/*****************************************************************************************
* Inicio de métodos de clase 															 *
*****************************************************************************************/
/*****************************************************************************************
* Métodos principales 																	 *
*****************************************************************************************/
	// Constructor
	public DrawHelper(Context context, AttributeSet attrs) {
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
		
		// Inicializo colores
		colorSetup();
		
		// Inicializo bitmaps
		bitmapSetup();
		
		// Inicializo Labels
		labelSetup();
	}
	
	// Método que configura los colores de los canales
	private void colorSetup() {
		// Genero colores
		mColorArray[0] = new RGB(150, 0, 150); 			// Violeta
		mColorArray[1] = new RGB(200, 75, 0); 			// Naranja
		mColorArray[2] = new RGB(0, 116, 194); 			// Azul
		mColorArray[3] = new RGB(0, 153, 77); 			// Verde
		mColorArray[4] = new RGB(255, 51, 102);			// Rojo
		mColorArray[5] = new RGB(60, 60, 60); 			// Negro
		mColorArray[6] = new RGB(250, 0, 204); 			// Rosa
		mColorArray[7] = new RGB(179, 189, 0); 			// Marrón
		mColorArray[8] = new RGB(204, 204, 0); 			// Amarillo

	}
	
	// Método que configura los Labels
	private void labelSetup() {
		
		// Label de esperando conexión
		mLabelAwaitingConnections = new Label();
		mLabelAwaitingConnections.setText("Esperando conexiones entrantes");
		int textSize = getBoundedTextSize(mLabelAwaitingConnections
											, 0.9 * BitmapManager.getBackgroundLogoWidth()
											, 0.15 * BitmapManager.getBackgroundLogoHeight());
		mLabelAwaitingConnections.setTextSize(textSize);
		int widthCorrection = BitmapManager.getBackgroundLogoWidth() - mLabelAwaitingConnections.getBoundingBox().width();
		int heightCorrection = mLabelAwaitingConnections.getBoundingBox().height();
		mLabelAwaitingConnections.setX(BitmapManager.getBackgroundLogoX() + (widthCorrection / 2));
		mLabelAwaitingConnections.setY(BitmapManager.getBackgroundLogoY() 
									   + BitmapManager.getBackgroundLogoHeight()
									   + heightCorrection);

	}
	
	// Método que configura los Bitmaps
	private void bitmapSetup() {
		
		// Seteo ancho y largo de los íconos
		BitmapManager.setIconsWidth((int) (0.07 * mTotalWidth));
		BitmapManager.setIconsHeight((int) (0.11 * mTotalHeight));

		// Ícono de abrir estudio
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.open_study);
		BitmapManager.setOpenStudyIconX((int) (0.05 * mTotalHeight));
		BitmapManager.setOpenStudyIconY((int) (0.1 * mTotalHeight));
		BitmapManager.setOpenStudyIcon(Bitmap.createScaledBitmap(bitmap
																 , BitmapManager.getIconsWidth()
																 , BitmapManager.getIconsHeight()
																 , false));

		// Ícono de nuevo estudio
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.new_study);
		BitmapManager.setNewStudyIconX((int) (0.05 * mTotalHeight));
		BitmapManager.setNewStudyIconY((int) (0.1 * mTotalHeight) + BitmapManager.getOpenStudyIconY());
		BitmapManager.setNewStudyIcon(Bitmap.createScaledBitmap(bitmap
				 												, BitmapManager.getIconsWidth()
																, BitmapManager.getIconsHeight()
																, false));

		// Ícono de parar estudio
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.stop_study);
		BitmapManager.setStopStudyIcon(Bitmap.createScaledBitmap(bitmap
																 , BitmapManager.getIconsWidth()
																 , BitmapManager.getIconsHeight()
																 , false));
		BitmapManager.setStopStudyIconX(BitmapManager.getNewStudyIconX());
		BitmapManager.setStopStudyIconY(BitmapManager.getNewStudyIconY());

		// Fondo de pantalla inicial
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.background_logo);
		BitmapManager.setBackgroundLogoWidth((int) (0.5 * mTotalWidth));
		BitmapManager.setBackgroundLogoHeight((int) (0.3 * mTotalHeight));
		BitmapManager.setBackgroundLogo(Bitmap.createScaledBitmap(bitmap
																  , BitmapManager.getBackgroundLogoWidth()
																  , BitmapManager.getBackgroundLogoHeight()
																  , false));
		BitmapManager.setBackgroundLogoX((int) ((0.5 * mTotalWidth) / 2));
		BitmapManager.setBackgroundLogoY((int) ((0.7 * mTotalHeight) / 2));
	}

	// Método que recibe y almacena muestras
	public synchronized void draw(short[] samples, int channelNumber) {

		Channel channel = mChannelList.getChannelAtKey(channelNumber);
		
		if(mChannelList.size() == 0 || channel == null || mDrawOk == false) return;
		
		if (channel.getPaused() == false) channel.storeSamples(samples);

	}

	// Método que se llama cuando se dibuja en el SurfaceView
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		
		// Método de superclase
		super.onDraw(canvas);
		
		// Fondo gris
		canvas.drawColor(Color.LTGRAY);
		
		// Dibujo signal boxes
		drawSignalBoxes(canvas);
		
		// Dibujo info boxes
		drawInfoBoxes(canvas);
		
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
	// Método que grafica Bitmaps
	private void drawBitmaps(Canvas canvas) {

		ColorFilter filter = new LightingColorFilter(Color.LTGRAY, 1);
		mPaint.setColorFilter(filter);

		// Dibujo ícono de estudio nuevo
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == false) {
			canvas.drawBitmap(BitmapManager.getNewStudyIcon()
							  , BitmapManager.getNewStudyIconX()
							  , BitmapManager.getNewStudyIconY()
							  , mPaint);
		}

		// Dibujo ícono de parar estudio
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == true) {
			canvas.drawBitmap(BitmapManager.getStopStudyIcon()
							  , BitmapManager.getStopStudyIconX()
							  , BitmapManager.getStopStudyIconY()
							  , mPaint);
		}

		// Dibujo ícono de abrir estudio
		if (mChannelList.size() == 0 || mUiVisibility == true) {
			canvas.drawBitmap(BitmapManager.getOpenStudyIcon()
							  , BitmapManager.getOpenStudyIconX()
							  , BitmapManager.getOpenStudyIconY()
							  , mPaint);
		}

		// Dibujo fondo
		if (mChannelList.size() == 0) {
			mPaint.setAlpha(130);
			canvas.drawBitmap(BitmapManager.getBackgroundLogo()
							  , BitmapManager.getBackgroundLogoX()
							  , BitmapManager.getBackgroundLogoY()
							  , mPaint);
			mPaint.setAlpha(255);
		}

		mPaint.setColorFilter(null);

	}

	// Método que dibuja el SignalBox
	private void drawSignalBoxes(Canvas canvas) {

		// Dibujo señal
		drawSignals(canvas);
		
		// Dibujo labels de amplitud
		drawAmplitudeLabels(canvas);
		
		// Dibujo labels de voltage
		drawVoltageLabels(canvas);
		
		// Dibujo labels de tiempo
		drawTimeLabels(canvas);
	}

	// Método que grafica la señal
	private void drawSignals(Canvas canvas) {
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
				}
				
			}
		}
	}
	
	// Método para dibujar las amplitudes de la señal
	private void drawAmplitudeLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			Channel channel = mChannelList.getChannelAtIndex(i);

			float padding = (float) (SignalBox.getWidth() * 0.02);
			setPaint(Color.BLACK, 1);
			mPaint.setAlpha(150);

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
	private void drawVoltageLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			SignalBox signalBox = mChannelList.getChannelAtIndex(i).getSignalBox();
						
			float padding = (float) (SignalBox.getWidth() * 0.02);
			setPaint(Color.GRAY, 1);
			mPaint.setAlpha(150);

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

			mPaint.setAlpha(255);
		}
	}

	// Método para dibujar los valores de tiempo de la señal
	private void drawTimeLabels(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {

			Channel channel = mChannelList.getChannelAtIndex(i);

			float timePixels = channel.getSignalBox().getLabelTimePixels();
			double ts = 1 / channel.getStudyData().getAcquisitionData().getFs();

			Label labelTime = channel.getSignalBox().getTimeLabel();

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(1);
			String result = df.format(ts * timePixels);

			labelTime.setText(result + " s");

			String time = labelTime.getText();
			float yTime = labelTime.getY();
			float xTime = labelTime.getX() - 20;

			// Barra horizontal de tiempo
			mPaint.setTextSize(labelTime.getTextSize());
			canvas.drawLine(xTime, yTime, xTime + timePixels, yTime, mPaint);

			// Label que muestra el tiempo
			float labelTimeWidth = labelTime.getBoundingBox().width();
			float labelTimeHeigth = labelTime.getBoundingBox().height();
			canvas.drawText(
					time,
					(float) (xTime - (labelTimeWidth * 0.5) + (timePixels * 0.5)),
					(float) (yTime - (labelTimeHeigth * 0.25)), mPaint);

			// Barritas verticales de los costados
			canvas.drawLine(xTime, yTime - 5, xTime, yTime + 5, mPaint);
			canvas.drawLine(xTime + timePixels, yTime - 5, xTime + timePixels,
					yTime + 5, mPaint);
		}

	}

	// Método que dibuja todas las Figuras en el SurfaceView
	private void drawInfoBoxes(Canvas canvas) {

		for (int i = 0; i < mChannelList.size(); i++) {
			
			// Obtengo Channel
			Channel channel = mChannelList.getChannelAtIndex(i);
						
			Label label;

			// Obtengo color del canal
			int[] rgb = channel.getColor().getRGB();
			setPaint(Color.rgb(rgb[0], rgb[1], rgb[2]), 2);
			mPaint.setAlpha(200);

			// Dibujo Label de número de Canal
			label = channel.getInfoBox().getLabelChannel();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			// Dibujo Label de tiempo de adquisición
			label = channel.getInfoBox().getLabelTimer();
			mPaint.setTextSize(label.getTextSize());
			// canvas.drawText(label.getText(), label.getX(), label.getY(),
			// mPaint);

			// Dibujo label de Fs
			label = channel.getInfoBox().getLabelFs();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			// Dibujo label de Bits
			label = channel.getInfoBox().getLabelBits();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			// Dibujo Label de Zoom X
			label = channel.getInfoBox().getHorizontalZoomLabel();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			// Dibujo Label de Zoom Y
			label = channel.getInfoBox().getLabelZoomY();
			mPaint.setTextSize(label.getTextSize());
			canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);

			/* Dibujo Label de Pausa
			setPaint(Color.RED, 2);
			if (channel.getInfoBox().getPaused() == true) {
				label = infoBox.getLabelPaused();
				mPaint.setTextSize(label.getTextSize());
				canvas.drawText(label.getText(), label.getX(), label.getY(),
						mPaint);
			}
			*/
		}

		mPaint.setAlpha(255);

	}
	
	// Método para dibujar las divisiones entre canales
	private void drawDivisions(Canvas canvas) {
		
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
	private void drawOtherLabels(Canvas canvas) {
		if(mChannelList.size() == 0) {
			mPaint.setTextSize(mLabelAwaitingConnections.getTextSize());
			canvas.drawText(mLabelAwaitingConnections.getText()
							, mLabelAwaitingConnections.getX()
							, mLabelAwaitingConnections.getY()
							, mPaint);
		}
		
		if(mChannelList.getDeletedChannelsLabels().size() > 0 && mUiVisibility == true) {
			for(int i = 0; i < mChannelList.getDeletedChannelsLabels().size(); i++) {
				int channelKey = mChannelList.getDeletedChannelsLabels().keyAt(i);
				Label label = mChannelList.getDeletedChannelsLabels().get(channelKey);
				label.setTextSize(getBoundedTextSize(label, BitmapManager.getIconsWidth(), BitmapManager.getIconsHeight()));
				label.setX((int) ((0.05 * mTotalHeight) + (i*BitmapManager.getIconsWidth())));
				label.setY(mTotalHeight - BitmapManager.getIconsHeight());
				canvas.drawText(label.getText(), label.getX(), label.getY(), mPaint);
			}
		}
	}

	// Método que configura el color y grosor del brush
	private void setPaint(int color, float strokeWidth) {
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
	// Método para agregar un canal online
	public synchronized void addChannel(StudyData studyData) {
		
		int channelNumber = mChannelList.size();
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color
									  , mTotalPages, studyData);
		mChannelList.addChannel(channel);

		mReferenceMatrix.addChannel();
		
		// Seteo sensibilidad de desplazamiento
		mPanSensitivity = (int) (SignalBox.getWidth() * 0.012);

	}

	public synchronized void removeChannel(int channelIndex) {
	
		mChannelList.removeChannelAtIndex(channelIndex);
		mReferenceMatrix.removeChannel();
	}
	
	/*************************************************************************************
	* Menú de usuario 																	 *
	*************************************************************************************/
	// Menú con las opciones del canal
	private void channelMenu(int channel) {
		((StudyActivity) getContext()).channelOptionsDialog(channel);
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

			onTouch_Pause();
			onTouch_DialogMenu();
			onTouch_NewStudyIcon();
			onTouch_OpenStudyIcon();
			onTouch_StopStudyIcon();
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

	// Botón de nuevo estudio
	private void onTouch_NewStudyIcon() {
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == false) {

			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = BitmapManager.getNewStudyIcon().getWidth();
			int height = BitmapManager.getNewStudyIcon().getHeight();

			if (tp.x > BitmapManager.getNewStudyIconX() && tp.x < BitmapManager.getNewStudyIconX() + width) {
				if (tp.y > BitmapManager.getNewStudyIconY() && tp.y < BitmapManager.getNewStudyIconY() + height) {
					((StudyActivity) getContext()).newStudyDialog();
				}
			}
		}
	}

	// Botón de parar estudio
	private void onTouch_StopStudyIcon() {
		if (mUiVisibility == true && mChannelList.size() != 0 && currentlyRecording == true) {

			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = BitmapManager.getNewStudyIcon().getWidth();
			int height = BitmapManager.getNewStudyIcon().getHeight();

			if (tp.x > BitmapManager.getNewStudyIconX() && tp.x < BitmapManager.getNewStudyIconX() + width) {
				if (tp.y > BitmapManager.getNewStudyIconY() && tp.y < BitmapManager.getNewStudyIconY() + height) {
					((StudyActivity) getContext()).stopStudyDialog();
				}
			}
		}
	}

	// Botón de abrir estudio
	private void onTouch_OpenStudyIcon() {
		if (mUiVisibility == true || mChannelList.size() == 0) {

			TouchPointer tp = mTouchPointer.valueAt(0);
			int width = BitmapManager.getOpenStudyIcon().getWidth();
			int height = BitmapManager.getOpenStudyIcon().getHeight();

			if (tp.x > BitmapManager.getOpenStudyIconX() && tp.x < BitmapManager.getOpenStudyIconX() + width) {
				if (tp.y > BitmapManager.getOpenStudyIconY() && tp.y < BitmapManager.getOpenStudyIconY() + height) {
					((StudyActivity) getContext()).actionDialog();
				}
			}
		}
	}

	// Menú de opciones de InfoBox
	private void onTouch_DialogMenu() {
		TouchPointer tp = mTouchPointer.valueAt(0);
		int totalPointers = mTouchPointer.size();
		long time = System.currentTimeMillis();
		mTapTime = time;
		int channel = mReferenceMatrix.getChannel(tp.y0, tp.x0) + 1;
		boolean flag = false;
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
			channel.getInfoBox().getLabelBits().setText(String.valueOf(mDyAcum));

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

			SignalBox signalBox;

			TouchPointer tp = mTouchPointer.valueAt(0);

			int channel = mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;

			if (channel >= 0) {

				signalBox = mChannelList.getChannelAtIndex(channel).getSignalBox();

				if (signalBox.getPaused() == true) {
					// Si muevo el dedo hacia la derecha...
					if (tp.x > mOldX) {
						signalBox.panRight(mPanSensitivity);
					}

					// Si muevo el dedo hacia la izquierda...
					if (tp.x < mOldX) {
						signalBox.panLeft(mPanSensitivity);
					}

					mOldX = (float) tp.x;
				}
			}
		}
	}

	// Qué hago cuando el evento es un Double Tap?
	private void onTouch_Pause() {

		if (mChannelList.size() == 0) return;

		TouchPointer tp = mTouchPointer.valueAt(0);
		int totalPointers = mTouchPointer.size();
		long time = System.currentTimeMillis();
		int channel = mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;

		// Si no es el primer tap, el tiempo entre este tap y el anterior es < a
		// 300 mS, hay un solo dedo apoyado y este fue apoyado en la zona de
		// visualizacion
		if (mFirstTap == true && (time - mTapTime) <= 300 && tp.x < SignalBox.getWidth()) {
			if (totalPointers == 1 && channel == mPressedChannel
					&& channel >= 0 && mFlagUp == true) {
				// Flipeo el bool de pausa
				SignalBox signalBox = mChannelList.getChannelAtIndex(channel).getSignalBox();
				if (true) {
					boolean oldPausedValue = signalBox.getPaused();
					boolean newPausedValue = !oldPausedValue;
					signalBox.setPaused(newPausedValue);
					// Si es el caso que estoy des-pauseando, igualo los índices
					// del Buffer.
					// Esto hace que se resuma la graficación desde la posición
					// de la última
					// muestra registrada.
					if (signalBox.getPaused() == false)
						signalBox.resetGraphingIndex();
					// Vuelvo a poner en false el bool de Primer Tap
					mFirstTap = false;
					mFlagUp = false;
					mTapTime = 0;
				}
			}
			// Si es el primer tap
		} else if (tp.x < SignalBox.getWidth()) {
			if (channel >= 0) {
				SignalBox signalBox = mChannelList.getChannelAtIndex(channel).getSignalBox();
				mPressedChannel = channel;
				if (true) {
					// Pongo en true el bool de Primer Tap
					mFirstTap = true;
					// Obtengo el tiempo actual para restarle al tiempo del
					// segundo tap y chequear
					// que no haya más de 300 mS entre Taps
					mTapTime = time;
				}
			}
		}
	}

	// Qué hago cuando el evento es un Long Press?
	final Handler mLongPressHandler = new Handler();
	Runnable longPressed = new Runnable() {

		public void run() {

			if (mChannelList.size() < 0) return;

			final TouchPointer tp = mTouchPointer.valueAt(0);
			final int channel = -mReferenceMatrix.getChannel(tp.y0, tp.x0) - 1;
			int isInfoBox = mReferenceMatrix.getChannel(tp.y0, tp.x0);

			if (isInfoBox < 0) {
				SignalBox signalBox = mChannelList.getChannelAtIndex(channel).getSignalBox();
				channelMenu(channel);
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
		public DrawingThread(SurfaceHolder mSurfaceHolder,
				DrawHelper mPlotSurfaceView) {
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
		((StudyActivity) getContext()).setupAfterSurfaceCreated();
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

}// DrawingSurface
