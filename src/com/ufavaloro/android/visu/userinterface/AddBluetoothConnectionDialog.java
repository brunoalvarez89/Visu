package com.ufavaloro.android.visu.userinterface;


import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

public class AddBluetoothConnectionDialog extends AlertDialog {

	private BluetoothAdapter mBluetoothAdapter;
	private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 2;

	private MainActivity mMainActivity;
	private final CharSequence[] mDialogOptions = {"Conectarse a un dispositivo", 
					 					     	   "Esperar conexión"};
	
	public AddBluetoothConnectionDialog(Context context, MainActivity mainActivity, int theme) {
		super(context, theme);
		mMainActivity = mainActivity;
		checkBluetooth();
		if(mBluetoothAdapter.isEnabled()) setup();
	}
	
	public void checkBluetooth() {
		// Inicializo el adaptador BT local
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Si esta apagado, fuerzo encender Bluetooth
		if(mBluetoothAdapter.isEnabled() == false) {
			Intent intentActivarBT  = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mMainActivity.startActivityForResult(intentActivarBT, REQUEST_CODE_ENABLE_BLUETOOTH);
		}
	}
	
	public void setup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle("Agregar conexión Bluetooth");
		
		
		builder.setItems(mDialogOptions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
        		switch(item) {
        		
        		case 0:
        			mMainActivity.connectToRemoteDeviceDialog();
        			break;
        		
        		case 1:
        			mMainActivity.waitingForConnectionDialog();
        			break;
     
        		default:
        			break;
        		}
        	}
        });
	
	
		builder.create().show();
	}

}
