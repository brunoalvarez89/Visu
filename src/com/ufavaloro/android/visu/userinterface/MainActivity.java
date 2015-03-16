/*****************************************************************************************
 * MainActivity.java																	 *
 * Clase que administra la interfaz de usuario.													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.userinterface;

import java.io.File;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.maininterface.MainInterface;
import com.ufavaloro.android.visu.maininterface.MainInterfaceMessage;
import com.ufavaloro.android.visu.storage.StorageInterfaceMessage;
import com.ufavaloro.android.visu.storage.datatypes.StorageData;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// Estudio
	private MainInterface mMainInterface;
	// View para manejar Status Bar y Navigation Bar
	private View mRootView;
	// Dialogs Theme
	private int mDialogTheme = android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth;
	// Request code for enabling Bluetooth
	private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 2;
	// Request código de apertura de archivo de Google Drive
	private static final int REQUEST_CODE_GOOGLE_DRIVE_FILE_BROWSER = 1;
	// Incoming Connection Progress Dialog
	private ProgressDialog mWaitingForConnectionDialog;

	// Método que se ejecuta luego de haberse creado el SurfaceView asociado
	public void setupAfterSurfaceCreated() {
		mMainInterface = new MainInterface(this, mMainInterfaceHandler);
		mMainInterface.getBluetoothProtocol().addSlaveBluetoothConnection();
	}

	/**
	 * Dialogs
	 */
	
	// Dialog de menú principal
	public void mainMenuDialog() {
		MainMenuDialog dialog = new MainMenuDialog(this, this, mDialogTheme);
	}
		
	// Dialog de nuevo estudio
	public void newStudyDialog() {
		NewStudyDialog dialog = new NewStudyDialog(this, this, mDialogTheme);
	}
	
	// Dialog para abrir un archivo desde la memoria interna
	public void loadFileFromLocalStorageDialog() {
		LoadFileFromLocalStorageDialog dialog = new LoadFileFromLocalStorageDialog(this, mMainInterface);
		dialog.setup();
	}

	// Dialog para abrir un archivo desde Google Drive
	public void loadFileFromGoogleDriveDialog() {
		LoadFileFromGoogleDriveDialog dialog = new LoadFileFromGoogleDriveDialog(this, mMainInterface);
		dialog.setup();
	}
		
	// Dialog de parar estudio
	public void stopStudyDialog() {
		StopStudyDialog dialog = new StopStudyDialog(this, mDialogTheme);
		dialog.setMainInterface(mMainInterface);
		dialog.setup();
	}
	
	// Dialog con las opciones del canal
	public void channelOptionsDialog(int channelNumber) {
		ChannelOptionsDialog dialog = new ChannelOptionsDialog(this, mDialogTheme, channelNumber);
		dialog.setMainActivity(this);
		dialog.setMainInterface(mMainInterface);
		dialog.setup();
	}
		
	// Dialog de configuración de canales ONLINE
	public void onlineChannelPropertiesDialog(int channel) {
		OnlineChannelPropertiesDialog dialog = new OnlineChannelPropertiesDialog(this, mDialogTheme, channel);
		dialog.setStudy(mMainInterface);
		dialog.setup();
		dialog.show();
	}
	
	// Dialog con las propiedades del canal OFFLINE
	public void offlineChannelPropertiesDialog(int channelNumber) {
		OfflineChannelPropertiesDialog dialog = new OfflineChannelPropertiesDialog(this, mDialogTheme, channelNumber);
		dialog.setStudy(mMainInterface);
		dialog.setup();
		dialog.show();
	}
	
	public void addBluetoothConnectionDialog() {
		AddBluetoothConnectionDialog dialog = new AddBluetoothConnectionDialog(this, this, mDialogTheme);
	}
	
	public void connectToRemoteDeviceDialog() {
		RemoteBluetoothDevicesDialog dialog = new RemoteBluetoothDevicesDialog(this, this, mDialogTheme);
	}
	
	public void waitingForConnectionDialog() {
		mMainInterface.getBluetoothProtocol().addSlaveBluetoothConnection();
		mWaitingForConnectionDialog = ProgressDialog.show(this, "Conexión Bluetooth", "Esperando..."); 
	}
	
	private final Handler mMainInterfaceHandler = new Handler() {
		
		// Método para manejar el mensaje
		@Override
		public void handleMessage(Message msg) {
			
			// Tipo de mensaje recibido
			MainInterfaceMessage mainInterfaceMessage = MainInterfaceMessage.values(msg.what);
			
			switch (mainInterfaceMessage) {
				
				case NOTHING:
					break;
					
				case BLUETOOTH_CONNECTED:
					//mWaitingForConnectionDialog.cancel();
					shortToast("Conectado");
					break;
					
				case BLUETOOTH_DISCONNECTED:
					shortToast("Desconectado");
					break;

				default:
					break;
			}
		}
		
	};
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
        	        if(mMainInterface.getDrawInterface() != null) mMainInterface.getDrawInterface().setUiVisibility(true);
                } else {
                    // The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
        	        if(mMainInterface.getDrawInterface() != null) mMainInterface.getDrawInterface().setUiVisibility(false);
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
	
	// MainInterface Getter (for Dialogs)
	public MainInterface getMainInterface() {
		return mMainInterface;
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
		 mMainInterface.getBluetoothProtocol().stopConnections();
	}
	
	// this.activity on back pressed
	public void onBackPressed() {
	    moveTaskToBack(true);
	}

	// this.activity onActivityResult
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
		switch (requestCode) {
        
		case REQUEST_CODE_GOOGLE_DRIVE_FILE_BROWSER:
        	if (resultCode == RESULT_OK) {
        		if(!mMainInterface.isGoogleDriveConnected()) return;
        		DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        		mMainInterface.getStorageInterface().loadFileFromGoogleDrive(driveId);
        		shortToast("Abriendo archivo...");
        	}    
        	break;
        	
		case REQUEST_CODE_ENABLE_BLUETOOTH:
			if (resultCode == Activity.RESULT_OK) {
				shortToast("Bluetooth activado");
				addBluetoothConnectionDialog();
			}
			
			if (resultCode == Activity.RESULT_CANCELED) {
				shortToast("Por favor, active el Bluetooth");
			}
		
			break;
        
        default:
        	break;
        
        }
        
    }

}//MainActivity
