package com.ufavaloro.android.visu.userinterface;


import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

public class MainMenuDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private final CharSequence[] mConnectedOptions = {"Nuevo estudio",
											 "Abrir estudio desde memoria local", 
					 					     "Abrir estudio desde Google Drive",
					 					     "Desconectar"};
	private final CharSequence[] mDisconnectedOptions = {"Agregar conexión Bluetooth",
														 "Abrir estudio desde memoria local", 
     												     "Abrir estudio desde Google Drive"};
	
	public MainMenuDialog(Context context, MainActivity mainActivity, int theme) {
		super(context, theme);
		mMainActivity = mainActivity;
		setup();
	}
	
	public void setup() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle("Menú principal");
		
		if(mMainActivity.getMainInterface().getConnectionInterface().getConnection(0) != null) {
			builder.setItems(mConnectedOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
	        		switch(item) {
	        		
	        		// Nuevo estudio on-line
	        		case 0:
	        			mMainActivity.newStudyDialog();
	        			break;
	        		
	        		// Abro desde memoria interna
	        		case 1:
	        			mMainActivity.loadFileFromLocalStorageDialog();
	        			break;
	        		
	        		// Abro desde Google Drive
	        		case 2:
	        			mMainActivity.loadFileFromGoogleDriveDialog();
	        			break;
	        			
	        		// Desconecto
	        		case 3:
	        			//TODO
	        			mMainActivity.getMainInterface().getConnectionInterface().removeConnection(0);
	     
	        		default:
	        			break;
	        		}
	        	}
	        });
		} else {
			builder.setItems(mDisconnectedOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
	        		switch(item) {
	        		
	        		// Nuevo estudio on-line
	        		case 0:
	        			mMainActivity.addBluetoothConnectionDialog();
	        			break;
	        		
	        		// Abro desde memoria interna
	        		case 1:
	        			mMainActivity.loadFileFromLocalStorageDialog();
	        			break;
	        		
	        		// Abro desde Google Drive
	        		case 2:
	        			mMainActivity.loadFileFromGoogleDriveDialog();
	        			break;
	     
	        		default:
	        			break;
	        		}
	        	}
	        });
		}
			
	
		builder.create().show();
	}
	
}
