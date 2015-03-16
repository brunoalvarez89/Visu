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
import com.ufavaloro.android.visu.draw.channel.Channel;
import com.ufavaloro.android.visu.main.MainInterface;
import com.ufavaloro.android.visu.main.StudyType;
import com.ufavaloro.android.visu.storage.datatypes.AcquisitionData;

public class OfflineChannelPropertiesDialog extends Dialog {
	
	private EditText mEditTextStudyName;
	private EditText mEditTextPatientName;
	private EditText mEditTextPatientSurname;
	private EditText mEditTextStudyType;
	private EditText mEditTextAcquisitionTime;
	private EditText mEditTextBits;
	private EditText mEditTextSensor;
	private EditText mEditTextVMax;
	private EditText mEditTextVMin;
	private EditText mEditTextAMax;
	private TextView mTextViewAMax;
	private EditText mEditTextAMin;
	private TextView mTextViewAMin;
	
	private Context mContext;
	
	private MainInterface mStudy;
	
	private int mChannelNumber;
	
	public OfflineChannelPropertiesDialog(Context context, int theme, int channelNumber) {
		super(context);	
		mContext = context;
		mChannelNumber = channelNumber;
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
		
		mEditTextSensor = (EditText) findViewById(R.id.editTextSensor);
		mEditTextSensor.setEnabled(false);
		
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
		Channel channel = mStudy.getDrawInterface().getChannels().getChannelAtIndex(mChannelNumber);
		
		// Write Study Name
		mEditTextStudyName.setText(String.valueOf(channel.getStudyData().getPatientData().getStudyName()));
		// Write Patient Name
		mEditTextPatientName.setText(String.valueOf(channel.getStudyData().getPatientData().getPatientName()));
		// Write Patient Surname
		mEditTextPatientSurname.setText(String.valueOf(channel.getStudyData().getPatientData().getPatientSurname()));
		// Write Study Type
		char[] studyType = mStudy.getDrawInterface().getChannels().getChannelAtIndex(mChannelNumber).getStudyData().getAcquisitionData().getStudyType();
		int studyNumber = studyType[0];
		mEditTextStudyType.setText(String.valueOf(StudyType.values(studyNumber)));
		// Write Acquisition Time
		int totalSamples = channel.getStudyData().getSamplesBuffer().getSize();
		double fs = channel.getStudyData().getAcquisitionData().getFs();
		double totalTime = totalSamples * fs / 1000;
		mEditTextAcquisitionTime.setText(String.valueOf(totalTime));
		// Write Resolution
		mEditTextBits.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getBits()));
		// Write Sensor Name
		mEditTextSensor.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getSensor()));
		// Write Max Voltage
		mEditTextVMax.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getVMax()));
		// Write Min Voltage
		mEditTextVMin.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getVMin()));
		// Write Max Amplitude Value
		mEditTextAMax.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getAMax()));
		// Write Min Amplitude
		mEditTextAMin.setText(String.valueOf(channel.getStudyData().getAcquisitionData().getAMin()));
	}
	
	public void setStudy(MainInterface study) {
		mStudy = study;
	}

}
