/*****************************************************************************************
 * MainActivity.java																	 *
 * Clase que administra la interfaz de usuario.													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.UI;

import java.io.File;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.storage.datatypes.StorageData;
import com.ufavaloro.android.visu.study.Study;
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
		MainMenuDialog dialog = new MainMenuDialog(this, theme);
		dialog.setMainActivity(this);
		dialog.setup();
	}
		
	// Dialog de nuevo estudio
	public void newStudyDialog() {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		NewStudyDialog dialog = new NewStudyDialog(this, theme);
		dialog.setStudy(mStudy);
		dialog.setup();
		dialog.show();
	}
	
	// Dialog para abrir un archivo desde la memoria interna
	public void loadFileFromLocalStorageDialog() {
		LoadFileFromLocalStorageDialog dialog = new LoadFileFromLocalStorageDialog(this, mStudy);
		dialog.setup();
	}

	// Dialog para abrir un archivo desde Google Drive
	public void loadFileFromGoogleDriveDialog() {
		LoadFileFromGoogleDriveDialog dialog = new LoadFileFromGoogleDriveDialog(this, mStudy);
		dialog.setup();
	}
		
	// Dialog de parar estudio
	public void stopStudyDialog() {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		StopStudyDialog dialog = new StopStudyDialog(this, theme);
		dialog.setStudy(mStudy);
		dialog.setup();
	}
	
	// Dialog con las opciones del canal
	public void channelOptionsDialog(final int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		ChannelOptionsDialog dialog = new ChannelOptionsDialog(this, theme);
		dialog.setMainActivity(this);
		dialog.setStudy(mStudy);
		dialog.setChannel(channel);
		dialog.setup();
	}
		
	// Dialog de configuración de canales ONLINE
	public void onlineChannelPropertiesDialog(int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		OnlineChannelPropertiesDialog dialog = new OnlineChannelPropertiesDialog(this, theme, channel);
		dialog.setStudy(mStudy);
		dialog.setup();
		dialog.show();
	}
	
	// Dialog con las propiedades del canal OFFLINE
	public void offlineChannelPropertiesDialog(final int channel) {
		int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
		OfflineChannelPropertiesDialog dialog = new OfflineChannelPropertiesDialog(this, theme, channel);
		dialog.setStudy(mStudy);
		dialog.setup();
		dialog.show();
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
