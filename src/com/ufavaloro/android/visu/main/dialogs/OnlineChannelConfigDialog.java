package com.ufavaloro.android.visu.main.dialogs;

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
import com.ufavaloro.android.visu.main.Study;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.data.AcquisitionData;

public class OnlineChannelConfigDialog extends Dialog {
	
	private Spinner mSpinnerChannel;
	private Spinner mSpinnerStudyType;
	private EditText mEditTextBits;
	private EditText mEditTextFs;
	private EditText mEditTextVMax;
	private EditText mEditTextVMin;
	private EditText mEditTextAMax;
	private TextView mTextViewAMax;
	private EditText mEditTextAMin;
	private TextView mTextViewAMin;
	private Button mButtonChannelConfigOk;
	
	private int mSelectedChannel;
	private Context mContext;
	
	private Study mStudy;
	
	public OnlineChannelConfigDialog(Context context, int theme, int channel) {
		super(context);	
		mContext = context;
		if(channel != -1) mSelectedChannel = channel;
	}
	
	public void setup() {
		setCanceledOnTouchOutside(false);
		setTitle("Configuración de los canales");
		inflate();
		setListeners();
		populateSpinners();
	}
	
	private void inflate() {
		setContentView(R.layout.dialog_online_channel_config);
		
		mSpinnerChannel = (Spinner) findViewById(R.id.spinnerChannel);
		mSpinnerStudyType = (Spinner) findViewById(R.id.spinnerStudyType);
		
		mEditTextBits = (EditText) findViewById(R.id.editTextBits);
		mEditTextBits.setEnabled(false);
		
		mEditTextFs = (EditText) findViewById(R.id.editTextFs);
		mEditTextFs.setEnabled(false);
		
		mEditTextVMax = (EditText) findViewById(R.id.editTextVMax);
		mEditTextVMax.setEnabled(false);
		
		mEditTextVMin = (EditText) findViewById(R.id.editTextVMin);
		mEditTextVMin.setEnabled(false);
		
		mEditTextAMax = (EditText) findViewById(R.id.editTextAMax);
		mTextViewAMax = (TextView) findViewById(R.id.textViewAMax);
		
		mEditTextAMin = (EditText) findViewById(R.id.editTextAMin);
		mTextViewAMin = (TextView) findViewById(R.id.textViewAMin);
		
		mButtonChannelConfigOk = (Button) findViewById(R.id.buttonChannelConfigOk);

	}
	
	private void setListeners() {
		
		// Channel Spinner
		mSpinnerChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				mSelectedChannel = arg2;
				AcquisitionData acquisitionData = mStudy.mOnlineStudyData[mSelectedChannel].getAcquisitionData();
				char[] charStudyType = acquisitionData.getStudyType();
				int intStudyType = charStudyType[0];
				mSpinnerStudyType.setSelection(intStudyType);
				
				int bits = acquisitionData.getBits();
				mEditTextBits.setText(String.valueOf(bits));
				
				double fs = acquisitionData.getFs();
				mEditTextFs.setText(String.valueOf(fs));
				
				double vMax = acquisitionData.getVMax();
				mEditTextVMax.setText(String.valueOf(vMax));
				
				double vMin = acquisitionData.getVMin();
				mEditTextVMin.setText(String.valueOf(vMin));
				
				double aMax = acquisitionData.getAMax();
				mEditTextAMax.setText(String.valueOf(aMax));
				mTextViewAMax.setText("Valor Máximo (" + StudyType.getUnits(StudyType.values(intStudyType)) + ")");
				
				double aMin = acquisitionData.getAMin();
				mEditTextAMin.setText(String.valueOf(aMin));
				mTextViewAMin.setText("Valor Mínimo (" + StudyType.getUnits(StudyType.values(intStudyType)) + ")");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
			
		});
		
		// StudyType Spinner
		mSpinnerStudyType.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int intStudyType = arg2;
				mStudy.mOnlineStudyData[mSelectedChannel].getAcquisitionData().setStudyType(intStudyType);
				mTextViewAMax.setText("Valor Máximo (" + StudyType.getUnits(StudyType.values(intStudyType)) + ")");
				mTextViewAMin.setText("Valor Mínimo (" + StudyType.getUnits(StudyType.values(intStudyType)) + ")");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
			
		});
		
		// EditText Bits
		mEditTextBits.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
		
		// EditText Fs
		mEditTextFs.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
		
		// EditText VMax
		mEditTextVMax.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
		
		// EditText VMin
		mEditTextVMin.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
		
		// EditText AMax
		mEditTextAMax.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable text) {
				
				if(String.valueOf(text).isEmpty()) return;
				if(text.charAt(0) == '-' && text.length() == 1) return;

				AcquisitionData acquisitionData = mStudy.mOnlineStudyData[mSelectedChannel].getAcquisitionData();
				double d = 0;
				
				try {
					d = Double.parseDouble(String.valueOf(text));
				} 
				catch(NumberFormatException nfe) {	
					Toast.makeText(mContext, "Por favor, ingrese un número válido", Toast.LENGTH_SHORT).show();
					return;
				}
			
				if(d < acquisitionData.getAMin()) {
					Toast.makeText(mContext, "El valor máximo no puede ser menor al valor mínimo", Toast.LENGTH_SHORT).show();
					return;
				}
				
				acquisitionData.setAMax(d);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
		
		// EditText AMin
		mEditTextAMin.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable text) {
				
				if(String.valueOf(text).isEmpty()) return;
				if(text.charAt(0) == '-' && text.length() == 1) return;

				AcquisitionData acquisitionData = mStudy.mOnlineStudyData[mSelectedChannel].getAcquisitionData();
				double d = 0;
				
				try {
					d = Double.parseDouble(String.valueOf(text));
				} 
				catch(NumberFormatException nfe) {	
					Toast.makeText(mContext, "Por favor, ingrese un número válido", Toast.LENGTH_SHORT).show();
					return;
				}
			
				if(d > acquisitionData.getAMax()) {
					Toast.makeText(mContext, "El valor mínimo no puede ser mayor al valor máximo", Toast.LENGTH_SHORT).show();
					return;
				}
				
				acquisitionData.setAMin(d);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});

		// Config Ok Button
		mButtonChannelConfigOk.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
			
		});

		
	}

	private void populateSpinners() {
		
		// Populate Channel Spinner
		ArrayList<String> channels = new ArrayList<String>();
		for(int i = 0; i < mStudy.getTotalAdcChannels(); i++) {
			channels.add("Canal " + String.valueOf(i+1));
		}
	    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, channels);	    
	    mSpinnerChannel.setAdapter(arrayAdapter);
	    if(mSelectedChannel != -1) mSpinnerChannel.setSelection(mSelectedChannel);
	    
	    // Populate StudyType Spinner
	    ArrayList<String> studyTypes = new ArrayList<String>();
	    for(int i = 0; i < StudyType.getTotalStudyTypes(); i++) {
	    	studyTypes.add(StudyType.values(i).toString());
	    }
	    arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, studyTypes);	    
	    mSpinnerStudyType.setAdapter(arrayAdapter);
	    
	}
	
	public void setStudy(Study study) {
		mStudy = study;
	}

}
