/*****************************************************************************************
 * Studyctivity.java																	 *
 * Clase que administra las muestras recibidas a través de Bluetooth y las envía al		 *
 * surfaceview para poder graficarlas.													 *
 ****************************************************************************************/

package com.ufavaloro.android.visu.study;

import java.io.File;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.storage.data.StorageData;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.samsung.sprc.fileselector.FileOperation;
import com.samsung.sprc.fileselector.FileSelector;
import com.samsung.sprc.fileselector.OnHandleFileListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StudyActivity extends Activity {
	
/*****************************************************************************************
* Inicio de atributos de clase			        									     *
*****************************************************************************************/
	// Estudio
	public Study mStudy;

	// View para manejar Status Bar y Navigation Bar
	private View mRootView;
	
	private boolean mConfigurationOk;
	
/*****************************************************************************************
* Menúes de usuario													   					 *
*****************************************************************************************/
	/*************************************************************************************
	* Channel Options Dialog						   					 				 *
	*************************************************************************************/
	private final CharSequence[] mOfflineChannelOptions = {"Configurar", "Eliminar canal"};

	private final CharSequence[] mOnlineChannelOptions = {"Configurar", 
			  										      "Iniciar estudio", 
			  											  "Eliminar canal"};
	
	/*************************************************************************************
	* Channel Configuration Dialog									   					 *
	*************************************************************************************/
	private Dialog mChannelConfigDialog;
	
	/*************************************************************************************
	* New Study Dialog									   					 			 *
	*************************************************************************************/
	private Dialog mNewStudyDialog;
	private EditText mEditTextPatientName;
	private EditText mEditTextPatientSurname;
	private EditText mEditTextStudyName;
	private Button mButtonStartNewStudy;
	private Button mButtonCancelNewStudy;
	
	/*************************************************************************************
	* Open Study Dialog									   					 			 *
	*************************************************************************************/	
	private final String[] mFileFilter = { "*.*", ".txt"};
	private final CharSequence[] mStudySource = {"Nuevo estudio",
												 "Abrir estudio desde memoria local", 
		      									 "Abrir estudio desde Google Drive"};
	private static final int REQUEST_CODE_OPENER = 1;


	/*************************************************************************************
	* Stop Study Dialog									   					 			 *
	*************************************************************************************/
	AlertDialog mStopStudyDialog;
	
	
/*****************************************************************************************
* Inicio de métodos de clase           												   	 *
*****************************************************************************************/
	public void setupAfterSurfaceCreated() {	
		mStudy = new Study(this);		
		// Agrego conexión Bluetooth
		mStudy.newBluetoothConnection();    
	}
	
	public void onConfigurationOk() {	
		mConfigurationOk = true;
		
		// Creo canales de dibujo
		int totalAdcChannels = mStudy.getTotalAdcChannels();
		for(int i = 0; i < totalAdcChannels; i++) {
			mStudy.addChannel(i);
		}
		
		// Empiezo a dibujar
		mStudy.startDrawing();
		
		mStudy.removeChannel(1);
		mStudy.removeChannel(1);

	}
	
	private void removeChannel(int channel) {
		mStudy.removeChannel(channel);
	}
	
/*****************************************************************************************
* Menúes de usuario																         *
*****************************************************************************************/
	/*************************************************************************************
	* Channel Config Dialog												         		 *
	*************************************************************************************/
	// Que hago con el resultado del Dialog	
	protected void channelConfigDialog(final int channel) {
	
	}
	
	/*************************************************************************************
	* Channel Options Dialog													         *
	*************************************************************************************/
	// Dialog con las opciones del canal
	public void channelOptionsDialog(final int channel) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(StudyActivity.this);
		//builder.setTitle("Canal " + (channel + 1));
	
		builder.setItems(mOfflineChannelOptions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item) {
				// Configurar
				case 0:
					//openStudy();
					break;
					
				// Eliminar canal
    			case 1: 
    				removeChannel(channel);
    				break;
    				
    			default:
    				break;
				}
        	}
        });
	
	   AlertDialog alert = builder.create();
       alert.show();
	}
	
	/*************************************************************************************
	* New Study Dialog															         *
	*************************************************************************************/
	// Menú de nuevo estudio
	public void newStudyDialog() {
		
		boolean connected = mStudy.bluetooth.getConnected();
		
		if(connected == false) {
			
			longToast("No se encuentra conectado!");
			
			return;
		
		}
			
		// Genero Dialog
		mNewStudyDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
		mNewStudyDialog.setContentView(R.layout.activity_new_study);
		mNewStudyDialog.setCanceledOnTouchOutside(false);

		// Botón de iniciar estudio
		mButtonStartNewStudy = (Button) mNewStudyDialog.findViewById(R.id.buttonStartStudy);
		mButtonStartNewStudy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newStudyDialogResult();
			}
		});
		
		// Botón de cancelar
		mButtonCancelNewStudy = (Button) mNewStudyDialog.findViewById(R.id.buttonCancel);
		mButtonCancelNewStudy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mNewStudyDialog.dismiss();
			}
		});
		
		// Muestro Dialog
	    mNewStudyDialog.show();

	}

	// Qué hago con lo escrito en el menú de nuevo estudio
	protected void newStudyDialogResult() {
		
		// Extraigo nombre
		mEditTextPatientName = (EditText) mNewStudyDialog.findViewById(R.id.editTextPatientName);
		String patientName = mEditTextPatientName.getText().toString();
		
		// Extraigo apellido
		mEditTextPatientSurname = (EditText) mNewStudyDialog.findViewById(R.id.editTextPatientSurname);
		String patientSurname = mEditTextPatientSurname.getText().toString();
		
		// Extraigo nombre del estudio
		mEditTextStudyName = (EditText) mNewStudyDialog.findViewById(R.id.editTextStudyName);
		String studyName = mEditTextStudyName.getText().toString();
	
		// Chequeo si completo todo		
		boolean nameEmpty = patientName.isEmpty();
		boolean surnameEmpty = patientSurname.isEmpty();
		boolean studyEmpty = studyName.isEmpty();	
				
		// Si escribio el nombre y el apellido => PatientInfoOk = true
		if(nameEmpty == false && surnameEmpty == false && studyEmpty == false) {
			
			// Elimino espacios
			patientName = patientName.trim().replace(' ', '_');
			patientSurname = patientSurname.trim().replace(' ', '_');
			studyName = studyName.trim().replace(' ', '_');
			
			// Creo estudios y empiezo a almacenar
			mStudy.newStudy(patientName, patientSurname, studyName);
			mStudy.startRecording();
		
		} else {
			
			shortToast("Por favor complete los campos requeridos");
	
		}
	
	    // Cierro dialog
		mNewStudyDialog.dismiss();
		
	}

	/*************************************************************************************
	* Stop Study Dialog															         *
	*************************************************************************************/
	// Dialog de parar estudio
	public void stopStudyDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(StudyActivity.this);
		builder.setTitle("¡ATENCIÓN!");
		builder.setMessage("¿Está seguro que desea dejar de adquirir?");
		
		builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	stopStudyDialogResult(true);
		     }
		});
		
		builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	stopStudyDialogResult(false);
		     }
		});
			
		mStopStudyDialog = builder.create();
	    mStopStudyDialog.show();
	
	}
	
	// Resultado del dialog de parar estudio
	private void stopStudyDialogResult(boolean result) {
		
		// Parar estudio == true
		if(result == true) {
			
			mStudy.stopRecording();
			
			mStudy.saveStudyToGoogleDrive();
			
			longToast("Estudio finalizado");
			
		}
		
		mStopStudyDialog.dismiss();
	
	}
		
	/*************************************************************************************
	* Open Study Dialog															         *
	*************************************************************************************/
	// Menú de abrir estudio
	public void actionDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(StudyActivity.this);
		builder.setTitle("Seleccione una acción");
		
		builder.setItems(mStudySource, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
        		switch(item) {
        		
        		// Nuevo estudio on-line
        		case 0:
        			newStudyDialog();
        			break;
        		
        		// Abro desde memoria interna
        		case 1:
        			loadFileFromLocalStorage();
        			break;
        		
        		// Abro desde Google Drive
        		case 2:
        			loadFileFromGoogleDrive();
        			break;
     
        		default:
        			break;
        		}
        	}
        });
	
		AlertDialog alert = builder.create();
	    alert.show();
	    
	}
	
	// Método para abrir archivo desde memoria interna
	public void loadFileFromLocalStorage() {
		
		FileSelector fileSelector = new FileSelector(StudyActivity.this, FileOperation.LOAD, 
									mLoadFileListener, mFileFilter);
		
		fileSelector.show();

		
	}
	
	// Recibo la ruta del archivo a abrir
	OnHandleFileListener mLoadFileListener = new OnHandleFileListener() {
		@Override
		public void handleFile(final String filePath) {
			
			mStudy.loadFileFromLocalStorage(filePath);
			
			shortToast("Abriendo estudio...");
		
		}
	
	};
	
	// Método para abrir archivo desde Google Drive
	public void loadFileFromGoogleDrive() {
						
		if(!mStudy.googleDriveConnectionOk()) {
			
			shortToast("No se encuentra conectado a Google Drive!");
			
			return;
		
		}
		
		GoogleApiClient googleApiClient = mStudy.storage.googleDrive.getGoogleApiClient();
		
		IntentSender intentSender = Drive.DriveApi
                						 .newOpenFileActivityBuilder()
                						 .setMimeType(new String[] {})
                						 .build(googleApiClient);
        try {
            
        	startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        
        } catch (SendIntentException e) {}
		
	}
		
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
		switch (requestCode) {
        
		case REQUEST_CODE_OPENER:
            
        	if (resultCode == RESULT_OK) {
        			
        		if(!mStudy.googleDriveConnectionOk()) return;
             
        		DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        		
        		mStudy.loadFileFromGoogleDrive(driveId);
        		
        	}
                        
        break;
        
        default:
        
        	break;
        
        }
        
    }
	
/*****************************************************************************************
* Manejo de visibilidad de Status Bar y Navigation Bar									 *
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

}//MainActivity
