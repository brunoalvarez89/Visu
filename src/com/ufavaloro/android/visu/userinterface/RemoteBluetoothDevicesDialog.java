package com.ufavaloro.android.visu.userinterface;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.main.MainInterface;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;

public class RemoteBluetoothDevicesDialog extends Dialog {
	
	/**
	 * Class Attributes
	 */
	private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mUnpairedDevicesArrayAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	
	private Button mScanForDevicesButton;
	private ListView mUnpairedDevicesListView;
	private ListView mPairedDevicesListView;
	
	private Context mContext;
	private MainActivity mMainActivity;
	
	public RemoteBluetoothDevicesDialog(Context context, MainActivity mainActivity, int theme) {
		super(context);	
		mContext = context;
		mMainActivity = mainActivity;
		setup();
	}
	
	public void setup() {
		setCanceledOnTouchOutside(true);
		setTitle("Seleccione un Dispositivo");
		inflate();
		setListeners();
		setListViewAdapters();
		registerBluetoothReceivers();
		initializeBluetoothAdapter();
		show();
	}
	
	private void inflate() {
		setContentView(R.layout.dialog_remote_devices);
		
		mScanForDevicesButton = (Button) findViewById(R.id.scanForDevicesButton);
		
		mUnpairedDevicesListView = (ListView) findViewById(R.id.unpairedDevicesListView);
		
		mPairedDevicesListView = (ListView) findViewById(R.id.pairedDevicesListView);
	}
	
	private void setListeners() {
		
		mScanForDevicesButton.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(View v){
				scanForDevices(v);
			}
		});	
		
		mUnpairedDevicesListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
				// Cancelamos el escaneo porque es costoso y nos vamos a conectar
				mBluetoothAdapter.cancelDiscovery();
				// Obtengo el texto del elemento seleccionado
				String info = ((TextView) v).getText().toString();
				// Obtengo MAC adress (ultimos 17 caracteres del objeto seleccionado)
				String MAC = info.substring(info.length()-17, info.length());
				// Creo el intent resultado con el MAC adress y el nombre del dispositivo
				Intent intent = new Intent();
				intent.putExtra("MAC", MAC);
				// Seteo el resultado y termino con la Activity
				//setResult(Activity.RESULT_OK, intent);
				// Termino esta Activity y vuelvo a ChatBluetooth
				//finish();
			}
		});
		
		mPairedDevicesListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
				// Cancelamos el escaneo porque es costoso y nos vamos a conectar
				mBluetoothAdapter.cancelDiscovery();
				// Obtengo el texto del elemento seleccionado
				String info = ((TextView) v).getText().toString();
				// Obtengo MAC adress (ultimos 17 caracteres del objeto seleccionado)
				String MAC = info.substring(info.length()-17);
				// Creo el intent resultado con el MAC adress y el nombre del dispositivo
				Intent intent = new Intent();
				intent.putExtra("MAC", MAC);
				// Seteo el resultado y termino con la Activity
				//setResult(Activity.RESULT_OK, intent);
				// Termino esta Activity y vuelvo a ChatBluetooth
				//finish();
			}
		});
		
		mPairedDevicesListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) {
            	// Cancelamos el escaneo porque es costoso y nos vamos a conectar
				mBluetoothAdapter.cancelDiscovery();
				// Obtengo el texto del elemento seleccionado
				String info = ((TextView) v).getText().toString();
				// Obtengo MAC adress (ultimos 17 caracteres del objeto seleccionado)
				String MAC = info.substring(info.length()-17);
                return true;
            }
        }); 
		
	}
	
	private void setListViewAdapters() {
		// Inicializo Adapters
		mUnpairedDevicesArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
					
		// Asocio los Array Adapters a sus respectivas ListViews 
		mUnpairedDevicesListView.setAdapter(mUnpairedDevicesArrayAdapter);
		mPairedDevicesListView.setAdapter(mPairedDevicesArrayAdapter);
	}
	
	private void registerBluetoothReceivers() {
		// Me registro al Broadcast de dispositivo encontrado
		mContext.registerReceiver(BluetoothBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		// Me registro al Broadcast de busqueda finalizada
		mContext.registerReceiver(BluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
	}
	
	private void initializeBluetoothAdapter() {
		// Inicializo el adaptador BT local
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Si esta apagado, fuerzo encender Bluetooth
		if(mBluetoothAdapter.isEnabled() == false) {
			Intent intentActivarBT  = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mMainActivity.startActivityForResult(intentActivarBT, REQUEST_CODE_ENABLE_BLUETOOTH);
		}
		
		// Obtengo una lista de los dispositivos ya sincronizados y los agrego a su Adapter
		Set<BluetoothDevice> DispositivosViejos = mBluetoothAdapter.getBondedDevices();
		
		// Si hay dispositivos sincronizados, los agrego al Adapter
		if (DispositivosViejos.size() > 0) {
			for(BluetoothDevice Device : DispositivosViejos ) {
				mPairedDevicesArrayAdapter.add(Device.getName() + "\n" + Device.getAddress());
			}
		}
	}
	
	private final BroadcastReceiver BluetoothBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			mUnpairedDevicesArrayAdapter.notifyDataSetChanged();
			String action = intent.getAction();
			// Cuando se encuentra un dispositivo
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Obtengo el objeto BluetoothDevice del Intent
				BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Si no estaba previamente sincronizado, lo agrego
				if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
					mUnpairedDevicesArrayAdapter.add(remoteDevice.getName() + "\n" + remoteDevice.getAddress());
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if(mUnpairedDevicesArrayAdapter.getCount() == 0) {
					String noRemoteDevices = "No se encontraron dispositivos.";
					mUnpairedDevicesArrayAdapter.add(noRemoteDevices);
				}
			}	
		}
	};
	
	// Metodo para buscar Dispositivos Bluetooth
	public void scanForDevices(View v) {
		// Si ya estoy escaneando, cancelo
		if(mBluetoothAdapter.isDiscovering() == true) {
			mBluetoothAdapter.cancelDiscovery();
		}
		// Inicio el escaneo
		mBluetoothAdapter.startDiscovery();
		// Limpio ArrayAdapter para agregar los nuevos devices
		mUnpairedDevicesArrayAdapter.clear();
		// Registro mi Broadcast Receiver
		mContext.registerReceiver(BluetoothBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	}
	
}
