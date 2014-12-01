package com.ufavaloro.android.visu.main.dialogs;

import com.ufavaloro.android.visu.main.Study;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class StopStudyDialog extends AlertDialog {

	private Context mContext;
	private Study mStudy;
	
	public StopStudyDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public void setup() {
		Builder builder = new Builder(mContext);
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
		
		builder.create().show();
	}
	
	// Resultado del dialog de parar estudio
	private void stopStudyDialogResult(boolean result) {
		
		// Parar estudio == true
		if(result == true) {
			mStudy.stopRecording();
			mStudy.saveStudyToGoogleDrive();
			Toast.makeText(mContext, "Estudio finalizado", Toast.LENGTH_SHORT).show();
		}
		
		dismiss();
	}
	
	public void setStudy(Study study) {
		mStudy = study;
	}
}
