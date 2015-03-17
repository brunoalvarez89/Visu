package com.ufavaloro.android.visu.userinterface;

import java.util.ArrayList;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.main.MainInterface;

import android.app.Dialog;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewStudyDialog extends Dialog {
		
	private EditText mEditTextPatientName;
	private EditText mEditTextPatientSurname;
	private EditText mEditTextStudyName;
	private ListView mListViewChannels;
	private Button mButtonStartNewStudy;
	private Button mButtonCancelNewStudy;
	
	private Context mContext;
	private MainActivity mMainActivity;
	private SparseArray<Integer> mChannelsToStore = new SparseArray<Integer>();
	
	public NewStudyDialog(Context context, MainActivity mainActivity, int theme) {
		super(context, theme);
		mContext = context;
		mMainActivity = mainActivity;
		setup();
	}
	
	public void setup() {
		setCanceledOnTouchOutside(false);
		setTitle("Seleccione una acción");
		inflate();
		setListeners();
		populateListView();
		show();
	}
	
	private void inflate() {
		setContentView(R.layout.dialog_new_study);
		
		// Botón de iniciar estudio
		mButtonStartNewStudy = (Button) findViewById(R.id.buttonStartStudy);
		
		// Botón de cancelar
		mButtonCancelNewStudy = (Button) findViewById(R.id.buttonCancel);

		// EditText con el nombre del paciente
		mEditTextPatientName = (EditText) findViewById(R.id.editTextPatientName);

		// EditText con el apellido del paciente
		mEditTextPatientSurname = (EditText) findViewById(R.id.editTextPatientSurname);
		
		// EditText con el nombre del estudio
		mEditTextStudyName = (EditText) findViewById(R.id.editTextStudyName);

		// ListView con los canales
		mListViewChannels = (ListView) findViewById(R.id.listViewChannels);
	}
	
	private void setListeners() {
		
		// Botón de iniciar estudio
		mButtonStartNewStudy.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				newStudyDialogResult();
			}
		});
		
		// Botón de cancelar
		mButtonCancelNewStudy.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		// ListView con los canales
		mListViewChannels.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int channel, long arg3) {
				if(mChannelsToStore.get(channel) == null) {
					view.setBackgroundColor(mContext.getResources().getColor(R.color.common_action_bar_splitter));
					mChannelsToStore.append(channel, channel);
				} else {
					view.setBackgroundColor(0);
					mChannelsToStore.remove(channel);
				}
			}
		});
		
	}
	
	private void populateListView() {
		// Populate Channel ListView
		ArrayList<String> channels = new ArrayList<String>();
		int totalAdcChannels = mMainActivity.getMainInterface().getConnectionInterface().getProtocol(0).getTotalAdcChannels();
		for(int i = 0; i < totalAdcChannels; i++) {
			channels.add("Canal " + String.valueOf(i+1));
		}
	    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, channels);	    
	    mListViewChannels.setAdapter(arrayAdapter);
	    mListViewChannels.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	private void newStudyDialogResult() {
		
		// Extraigo nombre
		String patientName = mEditTextPatientName.getText().toString();
		
		// Extraigo apellido
		String patientSurname = mEditTextPatientSurname.getText().toString();
		
		// Extraigo nombre del estudio
		String studyName = mEditTextStudyName.getText().toString();
	
		// Chequeo si completo todo		
		boolean nameEmpty = patientName.isEmpty();
		boolean surnameEmpty = patientSurname.isEmpty();
		boolean studyEmpty = studyName.isEmpty();	
				
		// Si escribio el nombre y el apellido => PatientInfoOk = true
		if(nameEmpty == false && surnameEmpty == false && studyEmpty == false) {
			
			// Elimino espacios
			patientName = patientName.trim().replace(' ', '_');
			patientSurname = patientSurname.trim().replace(' ', '_');
			studyName = studyName.trim().replace(' ', '_');
			
			// Creo estudios y empiezo a almacenar
			mMainActivity.getMainInterface().newStudy(patientName, patientSurname, studyName, mChannelsToStore);
			mMainActivity.getMainInterface().startRecording();
		
		} else {
			Toast.makeText(mContext, "Por favor complete los campos requeridos", Toast.LENGTH_SHORT).show();
		}
	
	    // Cierro dialog
		dismiss();
		
	}

}
