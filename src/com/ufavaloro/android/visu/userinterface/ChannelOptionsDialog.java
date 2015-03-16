package com.ufavaloro.android.visu.userinterface;

import org.apache.http.auth.MalformedChallengeException;

import com.ufavaloro.android.visu.draw.channel.Channel;
import com.ufavaloro.android.visu.maininterface.MainInterface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ChannelOptionsDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private MainInterface mStudy;
	private int mChannelNumber;
	private final CharSequence[] mOnlineChannelOptions = {"Configurar", "Ocultar"};
	private final CharSequence[] mOfflineChannelOptions = {"Propiedades", "Ocultar"};
	
	public ChannelOptionsDialog(Context context, int theme, int channelNumber) {
		super(context, theme);
		mChannelNumber = channelNumber;
	}

	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		Channel channel = mStudy.getDrawInterface().getChannels().getChannelAtIndex(mChannelNumber);
		//builder.setTitle("Canal " + (mChannel.getChannelNumber() + 1));
	
		// The channel is an on-line channel (connected to an ADC)
		if(channel.isOnline()) {
			builder.setItems(mOnlineChannelOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item) {
					// Configurar
					case 0:
						mMainActivity.onlineChannelPropertiesDialog(mChannelNumber);
						break;
						
					// Ocultar canal
	    			case 1: 
	    				mStudy.getDrawInterface().hideChannel(mChannelNumber);
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.getDrawInterface().removeChannel(mChannelNumber);
	    				
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
						mMainActivity.offlineChannelPropertiesDialog(mChannelNumber);
						break;
						
					// Ocultar canal
	    			case 1: 
	    				mStudy.getDrawInterface().hideChannel(mChannelNumber);
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.getDrawInterface().removeChannel(mChannelNumber);
	    				
	    			default:
	    				break;
					}
	        	}
	        });
		}
	
		builder.create().show();
	}
		
	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

	public void setMainInterface(MainInterface study) {
		mStudy = study;
	}
}
