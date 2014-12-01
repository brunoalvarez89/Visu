package com.ufavaloro.android.visu.main.dialogs;

import com.ufavaloro.android.visu.main.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ChannelOptionsDialog extends AlertDialog {

	private MainActivity mMainActivity;
	private int mChannel;
	private final CharSequence[] mOfflineChannelOptions = {"Configurar", "Ocultar"};
	//private final CharSequence[] mOnlineChannelOptions = {"Configurar", "Iniciar estudio", "Eliminar canal"};
	
	public ChannelOptionsDialog(Context context, int theme) {
		super(context, theme);
	}

	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		//builder.setTitle("Canal " + (channel + 1));
	
		builder.setItems(mOfflineChannelOptions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item) {
				// Configurar
				case 0:
					mMainActivity.channelConfigDialog(mChannel);
					break;
					
				// Eliminar canal
    			case 1: 
    				mMainActivity.removeChannel(mChannel);
    				break;
    				
    			default:
    				break;
				}
        	}
        });
	
		builder.create().show();
	}
	
	public void setChannel(int channel) {
		mChannel = channel;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}
}
