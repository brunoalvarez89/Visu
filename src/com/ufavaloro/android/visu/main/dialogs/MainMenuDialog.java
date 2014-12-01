package com.ufavaloro.android.visu.main.dialogs;

import com.ufavaloro.android.visu.main.MainActivity;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

public class MainMenuDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private final CharSequence[] mOptions = {"Nuevo estudio",
				 								 "Abrir estudio desde memoria local", 
					 							 "Abrir estudio desde Google Drive"};
	
	public MainMenuDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle("Menú principal");
		
		builder.setItems(mOptions, new DialogInterface.OnClickListener() {
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
     
        		default:
        			break;
        		}
        	}
        });
	
		builder.create().show();
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

}
