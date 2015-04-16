package com.ufavaloro.android.visu.userinterface;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.ufavaloro.android.visu.main.MainInterface;

import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.widget.Toast;

public class LoadFileFromGoogleDriveDialog{

	private MainInterface mStudy;
	private MainActivity mMainActivity;
	private static final int REQUEST_CODE_OPENER = 1;

	public LoadFileFromGoogleDriveDialog(MainActivity studyActivity, MainInterface study) {
		mMainActivity = studyActivity;
		mStudy = study;
	}
	
	public void setup() {
		mMainActivity.getMainInterface().getDrawInterface().stopDrawing();

		if(!mStudy.isGoogleDriveConnected()) {
			Toast.makeText(mMainActivity, "No se encuentra conectado a Google Drive", Toast.LENGTH_SHORT).show();
			return;
		}
		
		GoogleApiClient googleApiClient = mStudy.getStorageInterface().googleDrive.getGoogleApiClient();
		
		IntentSender intentSender = Drive.DriveApi
                						 .newOpenFileActivityBuilder()
                						 .setMimeType(new String[] {})
                						 .build(googleApiClient);
        try {
        	mMainActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } 
        catch (SendIntentException e) {}
        
	}
	
}
