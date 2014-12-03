package com.ufavaloro.android.visu.main.dialogs;

import com.ufavaloro.android.visu.main.MainActivity;
import com.ufavaloro.android.visu.main.Study;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ChannelOptionsDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private Study mStudy;
	private int mChannel;
	private final CharSequence[] mOnlineChannelOptions = {"Configurar", "Ocultar", "Eliminar"};
	private final CharSequence[] mOfflineChannelOptions = {"Propiedades", "Ocultar", "Eliminar"};
	
	public ChannelOptionsDialog(Context context, int theme) {
		super(context, theme);
	}

	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle("Canal " + (mChannel + 1));
	
		// The channel is an on-line channel (connected to an ADC)
		if(mStudy.mOnlineStudyData[mChannel] != null) {
			builder.setItems(mOnlineChannelOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item) {
					// Configurar
					case 0:
						mMainActivity.onlineChannelConfigDialog(mChannel);
						break;
						
					// Ocultar canal
	    			case 1: 
	    				mStudy.hideChannel(mChannel);
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.removeChannel(mChannel);
	    				
	    			default:
	    				break;
					}
	        	}
	        });
		// The channel is an offline channel (loaded from local storage or google drive)
		} else {
			builder.setItems(mOnlineChannelOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item) {
					// Properties
					case 0:
						mMainActivity.offlineChannelPropertiesDialog(mChannel);
						break;
						
					// Ocultar canal
	    			case 1: 
	    				mStudy.hideChannel(mChannel);
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.removeChannel(mChannel);
	    				
	    			default:
	    				break;
					}
	        	}
	        });
		}
	
		builder.create().show();
	}
	
	public void setChannel(int channel) {
		mChannel = channel;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

	public void setStudy(Study study) {
		mStudy = study;
	}
}
