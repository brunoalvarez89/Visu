/*****************************************************************************************
 * MainActivity.java																	 *
 * Clase que administra las muestras recibidas a través de Bluetooth y las envía al		 *
 * surfaceview para poder graficarlas.													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.main;

import java.io.File;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.main.dialogs.OfflineChannelPropertiesDialog;
import com.ufavaloro.android.visu.main.dialogs.OnlineChannelConfigDialog;
import com.ufavaloro.android.visu.main.dialogs.ChannelOptionsDialog;
import com.ufavaloro.android.visu.main.dialogs.LoadFileFromGoogleDrive;
import com.ufavaloro.android.visu.main.dialogs.LoadFileFromLocalStorage;
import com.ufavaloro.android.visu.main.dialogs.MainMenuDialog;
import com.ufavaloro.android.visu.main.dialogs.NewStudyDialog;
import com.ufavaloro.android.visu.main.dialogs.StopStudyDialog;
import com.ufavaloro.android.visu.storage.data.StorageData;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// Estudio
	private Study mStudy;
	// Request código de apertura de archivo de Google Drive
	private static final int GOOGLE_DRIVE_REQUEST_CODE_OPENER = 1;
	// View para manejar Status Bar y Navigation Bar
	private View mRootView;
		
	// Método que se ejecuta luego de haberse creado el SurfaceView asociado
	public void setupAfterSurfaceCreated() {
		mStudy = new Study(this);		
		mStudy.newBluetoothConnection();    
	}
	
	// Método que se ejecuta una vez conectado al sensor y configurado el SurfaceView
	public void onConfigurationOk() {		
		// Creo canales de dibujo
		int totalAdcChannels = mStudy.getTotalAdcChannels();
		for(int i = 0; i < totalAdcChannels; i++) {
			mStudy.addChannel(i);
		}
		
		// Empiezo a dibujar
		mStudy.startDrawing();		
	}

/*****************************************************************************************
* Dialogs													     	     				 *
*****************************************************************************************/
	// Dialog de menú principal
	public void mainMenuDialog() {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		MainMenuDialog mainMenuDialog = new MainMenuDialog(this, theme);
		mainMenuDialog.setMainActivity(this);
		mainMenuDialog.setup();
	}
		
	// Dialog de nuevo estudio
	public void newStudyDialog() {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		NewStudyDialog newStudyDialog = new NewStudyDialog(this, theme);
		newStudyDialog.setStudy(mStudy);
		newStudyDialog.setup();
		newStudyDialog.show();
	}
	
	// Dialog para abrir un archivo desde la memoria interna
	public void loadFileFromLocalStorageDialog() {
		LoadFileFromLocalStorage loadFileFromLocalStorage = new LoadFileFromLocalStorage(this, mStudy);
		loadFileFromLocalStorage.setup();
	}

	// Dialog para abrir un archivo desde Google Drive
	public void loadFileFromGoogleDriveDialog() {
		LoadFileFromGoogleDrive loadFromGoogleDrive = new LoadFileFromGoogleDrive(this, mStudy);
		loadFromGoogleDrive.setup();
	}
		
	// Dialog de parar estudio
	public void stopStudyDialog() {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		StopStudyDialog stopStudyDialog = new StopStudyDialog(this, theme);
		stopStudyDialog.setStudy(mStudy);
		stopStudyDialog.setup();
	}
	
	// Dialog con las opciones del canal
	public void channelOptionsDialog(final int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		ChannelOptionsDialog channelOptionsDialog = new ChannelOptionsDialog(this, theme);
		channelOptionsDialog.setMainActivity(this);
		channelOptionsDialog.setStudy(mStudy);
		channelOptionsDialog.setChannel(channel);
		channelOptionsDialog.setup();
	}
		
	// Dialog de configuración de canales ONLINE
	public void onlineChannelConfigDialog(int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		OnlineChannelConfigDialog onlineChannelConfigDialog = new OnlineChannelConfigDialog(this, theme, channel);
		onlineChannelConfigDialog.setStudy(mStudy);
		onlineChannelConfigDialog.setup();
		onlineChannelConfigDialog.show();
	}
	
	// Dialog con las propiedades del canal OFFLINE
	public void offlineChannelPropertiesDialog(final int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		OfflineChannelPropertiesDialog offlineChannelPropertiesDialog = new OfflineChannelPropertiesDialog(this, theme, channel);
		offlineChannelPropertiesDialog.setStudy(mStudy);
		offlineChannelPropertiesDialog.setup();
		offlineChannelPropertiesDialog.show();
	}
	
/*****************************************************************************************
* Visibilidad de Status Bar y Navigation Bar									 *
*****************************************************************************************/
	// Método que muestra Status y Navigation bars
	private void showSystemUi(boolean visible) {
	    int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	            | View.SYSTEM_UI_FLAG_IMMERSIVE;
	    if (!visible) {
	    // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
	        flag |= View.SYSTEM_UI_FLAG_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
	    }
	    mRootView.setSystemUiVisibility(flag);
	}

	// Método que escucha la pantalla para decidir cuando mostrar la UI
	private void setOnSystemUiVisibilityChangeListener() {
	    mRootView.setOnSystemUiVisibilityChangeListener(
        new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
            	
            	// Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
        	        if(mStudy.draw != null) mStudy.draw.setUiVisibility(true);
                } else {
                    // The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
        	        if(mStudy.draw != null) mStudy.draw.setUiVisibility(false);
                }
                
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showSystemUi(false);
                    }
                }, 4000);
            }
        });
	}

/*****************************************************************************************
* Otros métodos																			 *
*****************************************************************************************/
	// Getter de la carpeta de estudios
	public static File getStudiesFolder() {
		return StorageData.studiesFolder;
	}
	
	// Toast corto
	public void shortToast(String toToast) {
		Toast.makeText(getApplicationContext(), toToast, Toast.LENGTH_SHORT).show();
	}
	
	// Toast largo
	public void longToast(String toToast) {
		Toast.makeText(getApplicationContext(), toToast, Toast.LENGTH_LONG).show();
	}
	
/*****************************************************************************************
* Ciclo de vida de la activity 											 				 *
*****************************************************************************************/
	// this.activity on create	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_main);
	}
	
	// this.activity on start
	@Override
	public void onStart() {
		super.onStart();	
	}
	
	// this.activity on resume
	@Override
	public void onResume() {
		super.onResume();
		mRootView = getWindow().getDecorView();
		setOnSystemUiVisibilityChangeListener();
		showSystemUi(false);
	}
		
	// this.activity on pause
	@Override
	public void onPause() {
		super.onPause();	
	}
	
	// this.activity on stop
	@Override
	public void onStop() {
		super.onStop();
	}
	
	// this.activity on destroy
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Paro todos los Threads
		 mStudy.bluetooth.stopConnections();
	}
	
	// this.activity on back pressed
	public void onBackPressed() {
	    moveTaskToBack(true);
	}

	// this.activity onActivityResult
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
		switch (requestCode) {
        
		case GOOGLE_DRIVE_REQUEST_CODE_OPENER:
        	if (resultCode == RESULT_OK) {
        		if(!mStudy.googleDriveConnectionOk()) return;
        		DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        		mStudy.loadFileFromGoogleDrive(driveId);
        		shortToast("Abriendo archivo...");
        	}               
        break;
        
        default:
        	break;
        
        }
        
    }

}//MainActivity
