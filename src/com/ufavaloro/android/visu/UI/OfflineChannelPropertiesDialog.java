package com.ufavaloro.android.visu.UI;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.ufavaloro.android.visu.R;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;
import com.ufavaloro.android.visu.study.Study;
import com.ufavaloro.android.visu.study.StudyType;

public class OfflineChannelPropertiesDialog extends Dialog {
	
	private EditText mEditTextStudyName;
	private EditText mEditTextPatientName;
	private EditText mEditTextPatientSurname;
	private EditText mEditTextStudyType;
	private EditText mEditTextAcquisitionTime;
	private EditText mEditTextBits;
	private EditText mEditTextFs;
	private EditText mEditTextVMax;
	private EditText mEditTextVMin;
	private EditText mEditTextAMax;
	private TextView mTextViewAMax;
	private EditText mEditTextAMin;
	private TextView mTextViewAMin;
	
	private Context mContext;
	
	private Study mStudy;
	
	public OfflineChannelPropertiesDialog(Context context, int theme, int channel) {
		super(context);	
		mContext = context;
	}
	
	public void setup() {
		setCanceledOnTouchOutside(true);
		setTitle("Propiedades del Canal");
		inflate();
		setEditTexts();
	}
	
	private void inflate() {
		
		setContentView(R.layout.dialog_offline_channel_properties);
		
		mEditTextStudyName = (EditText) findViewById(R.id.editTextOfflineStudyName);
		mEditTextStudyName.setEnabled(false);
		
		mEditTextPatientName = (EditText) findViewById(R.id.editTextOfflinePatientName);
		mEditTextPatientName.setEnabled(false);
		
		mEditTextPatientSurname = (EditText) findViewById(R.id.editTextOfflinePatientSurname);
		mEditTextPatientSurname.setEnabled(false);
		
		mEditTextStudyType = (EditText) findViewById(R.id.editTextOfflineStudyType);
		mEditTextStudyType.setEnabled(false);
		
		mEditTextAcquisitionTime = (EditText) findViewById(R.id.editTextOfflineAcquisitionTime);
		mEditTextAcquisitionTime.setEnabled(false);
		
		mEditTextBits = (EditText) findViewById(R.id.editTextOfflineBits);
		mEditTextBits.setEnabled(false);
		
		mEditTextFs = (EditText) findViewById(R.id.editTextOfflineFs);
		mEditTextFs.setEnabled(false);
		
		mEditTextVMax = (EditText) findViewById(R.id.editTextOfflineVMax);
		mEditTextVMax.setEnabled(false);
		
		mEditTextVMin = (EditText) findViewById(R.id.editTextOfflineVMin);
		mEditTextVMin.setEnabled(false);
		
		mEditTextAMax = (EditText) findViewById(R.id.editTextOfflineAMax);
		mEditTextAMax.setEnabled(false);
		mTextViewAMax = (TextView) findViewById(R.id.textViewOfflineAMax);
		
		mEditTextAMin = (EditText) findViewById(R.id.editTextOfflineAMin);
		mEditTextAMin.setEnabled(false);
		mTextViewAMin = (TextView) findViewById(R.id.textViewOfflineAMin);

	}

	private void setEditTexts() {
		/*
		mEditTextStudyName;
		mEditTextPatientName;
		mEditTextPatientSurname;
		mEditTextStudyType;
		mEditTextAcquisitionTime;
		mEditTextBits;
		mEditTextFs;
		mEditTextVMax;
		mEditTextVMin;
		mEditTextAMax;
		mTextViewAMax;
		mEditTextAMin;
		mTextViewAMin;
		*/
	}
	
	public void setStudy(Study study) {
		mStudy = study;
	}

}