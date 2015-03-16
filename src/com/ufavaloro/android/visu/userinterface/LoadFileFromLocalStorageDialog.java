package com.ufavaloro.android.visu.userinterface;

import android.widget.Toast;

import com.samsung.sprc.fileselector.FileOperation;
import com.samsung.sprc.fileselector.FileSelector;
import com.samsung.sprc.fileselector.OnHandleFileListener;
import com.ufavaloro.android.visu.main.MainInterface;

public class LoadFileFromLocalStorageDialog {

	private final String[] mFileFilter = { "*.*", ".txt"};
	private MainActivity mMainActivity;
	private MainInterface mStudy;
	
	public LoadFileFromLocalStorageDialog(MainActivity mainActivity, MainInterface study) {
		mMainActivity = mainActivity;
		mStudy = study;
	}
	
	// M�todo para abrir archivo desde memoria interna
	public void setup() {
		
		FileSelector fileSelector = new FileSelector(mMainActivity, FileOperation.LOAD, 
									mLoadFileListener, mFileFilter);
		
		fileSelector.show();

	}
	
	// Recibo la ruta del archivo a abrir
	OnHandleFileListener mLoadFileListener = new OnHandleFileListener() {
		@Override
		public void handleFile(final String filePath) {
			mStudy.getStorageInterface().loadFileFromLocalStorage(filePath);
			Toast.makeText(mMainActivity, "Abriendo estudio...", Toast.LENGTH_SHORT).show();
		}
	};
}
