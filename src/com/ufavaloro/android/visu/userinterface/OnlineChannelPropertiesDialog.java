package com.ufavaloro.android.visu.userinterface;

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
import com.ufavaloro.android.visu.main.MainInterface;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;

public class OnlineChannelPropertiesDialog extends Dialog {
	
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
	
	private int mSelectedChannel;
	private Context mContext;
	
	private MainInterface mStudy;
	
	public OnlineChannelPropertiesDialog(Context context, int theme, int channel) {
		super(context);	
		mContext = context;
		if(channel != -1) mSelectedChannel = channel;
	}
	
	public void setup() {
		setCanceledOnTouchOutside(true);
		setTitle("Configuración de los canales");
		inflate();
		setListeners();
		populateSpinners();
	}
	
	private void inflate() {
		setContentView(R.layout.dialog_online_channel_properties);
		
		mSpinnerChannel = (Spinner) findViewById(R.id.spinnerOnlineChannel);
		mSpinnerStudyType = (Spinner) findViewById(R.id.spinnerOnlineStudyType);
		
		mEditTextBits = (EditText) findViewById(R.id.editTextOnlineBits);
		mEditTextBits.setEnabled(false);
		
		mEditTextFs = (EditText) findViewById(R.id.editTextOnlineFs);
		mEditTextFs.setEnabled(false);
		
		mEditTextVMax = (EditText) findViewById(R.id.editTextOnlineVMax);
		mEditTextVMax.setEnabled(false);
		
		mEditTextVMin = (EditText) findViewById(R.id.editTextOnlineVMin);
		mEditTextVMin.setEnabled(false);
		
		mEditTextAMax = (EditText) findViewById(R.id.editTextOnlineAMax);
		mTextViewAMax = (TextView) findViewById(R.id.textViewOnlineAMax);
		
		mEditTextAMin = (EditText) findViewById(R.id.editTextOnlineAMin);
		mTextViewAMin = (TextView) findViewById(R.id.textViewOnlineAMin);
	}
	
	private void setListeners() {
		
		// Channel Spinner
		mSpinnerChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				mSelectedChannel = arg2;
				AcquisitionData acquisitionData = mStudy.onlineStudyData[mSelectedChannel].getAcquisitionData();
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
				mStudy.setStudyType(intStudyType, mSelectedChannel);
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

				AcquisitionData acquisitionData = mStudy.onlineStudyData[mSelectedChannel].getAcquisitionData();
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
				
				mStudy.setAMax(d, mSelectedChannel);
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

				AcquisitionData acquisitionData = mStudy.onlineStudyData[mSelectedChannel].getAcquisitionData();
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
				
				mStudy.setAMin(d, mSelectedChannel);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
		});
	}

	private void populateSpinners() {
		
		// Populate Channel Spinner
		ArrayList<String> channels = new ArrayList<String>();
		for(int i = 0; i < mStudy.getConnectionInterface().getProtocol(0).getTotalAdcChannels(); i++) {
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
	
	public void setStudy(MainInterface study) {
		mStudy = study;
	}

}
