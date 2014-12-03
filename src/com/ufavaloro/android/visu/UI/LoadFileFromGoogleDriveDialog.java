package com.ufavaloro.android.visu.UI;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.ufavaloro.android.visu.study.Study;

import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.widget.Toast;

public class LoadFileFromGoogleDriveDialog{

	private Study mStudy;
	private MainActivity mStudyActivity;
	private static final int REQUEST_CODE_OPENER = 1;

	public LoadFileFromGoogleDriveDialog(MainActivity studyActivity, Study study) {
		mStudyActivity = studyActivity;
		mStudy = study;
	}
	
	public void setup() {
		
		if(!mStudy.googleDriveConnectionOk()) {
			Toast.makeText(mStudyActivity, "No se encuentra conectado a Google Drive", Toast.LENGTH_SHORT).show();
			return;
		}
		
		GoogleApiClient googleApiClient = mStudy.storage.googleDrive.getGoogleApiClient();
		
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
