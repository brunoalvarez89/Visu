package com.ufavaloro.android.visu.UI;

import org.apache.http.auth.MalformedChallengeException;

import com.ufavaloro.android.visu.draw.channel.Channel;
import com.ufavaloro.android.visu.study.Study;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ChannelOptionsDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private Study mStudy;
	private Channel mChannel;
	private final CharSequence[] mOnlineChannelOptions = {"Configurar", "Ocultar", "Eliminar"};
	private final CharSequence[] mOfflineChannelOptions = {"Propiedades", "Ocultar", "Eliminar"};
	
	public ChannelOptionsDialog(Context context, int theme) {
		super(context, theme);
	}

	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle("Canal " + (mChannel.getChannelNumber() + 1));
	
		// The channel is an on-line channel (connected to an ADC)
		if(mChannel.isOnline()) {
			builder.setItems(mOnlineChannelOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item) {
					// Configurar
					case 0:
						mMainActivity.onlineChannelPropertiesDialog(mChannel.getChannelNumber());
						break;
						
					// Ocultar canal
	    			case 1: 
	    				mStudy.hideChannel(mChannel.getChannelNumber());
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.removeChannel(mChannel.getChannelNumber());
	    				
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
	    				mStudy.hideChannel(mChannel.getChannelNumber());
	    				break;
	    				
	    			// Remover canal
	    			case 2:
	    				mStudy.removeChannel(mChannel.getChannelNumber());
	    				
	    			default:
	    				break;
					}
	        	}
	        });
		}
	
		builder.create().show();
	}
	
	public void setChannel(Channel channel) {
		mChannel = channel;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

	public void setStudy(Study study) {
		mStudy = study;
	}
}
