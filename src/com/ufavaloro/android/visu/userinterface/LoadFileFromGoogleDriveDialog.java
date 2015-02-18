package com.ufavaloro.android.visu.userinterface;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.ufavaloro.android.visu.maininterface.MainInterface;

import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.widget.Toast;

public class LoadFileFromGoogleDriveDialog{

	private MainInterface mStudy;
	private MainActivity mStudyActivity;
	private static final int REQUEST_CODE_OPENER = 1;

	public LoadFileFromGoogleDriveDialog(MainActivity studyActivity, MainInterface study) {
		mStudyActivity = studyActivity;
		mStudy = study;
	}
	
	public void setup() {
		
		if(!mStudy.isGoogleDriveConnected()) {
			Toast.makeText(mStudyActivity, "No se encuentra conectado a Google Drive", Toast.LENGTH_SHORT).show();
			return;
		}
		
		GoogleApiClient googleApiClient = mStudy.getStorageInterface().googleDrive.getGoogleApiClient();
		
		IntentSender intentSender = Drive.DriveApi
                						 .newOpenFileActivityBuilder()
                						 .setMimeType(new String[] {})
                						 .build(googleApiClient);
        try {
        	mStudyActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } 
        catch (SendIntentException e) {}
        
	}
	
}
