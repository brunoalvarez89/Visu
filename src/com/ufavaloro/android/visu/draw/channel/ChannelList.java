package com.ufavaloro.android.visu.draw.channel;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.data.StudyData;

import android.util.SparseArray;

public class ChannelList {

	// Visible Channel List. These channels will be drawn.
	private SparseArray<Channel> mVisibleChannels;
	// Hidden Channel List and their respective Labels. These channels will not be drawn.
	private SparseArray<Channel> mHiddenChannels;
	private SparseArray<Label> mHiddenChannelsLabels;
	// Channel colors
	public RGB[] mColorArray = new RGB[10];
	
	// Constructor
	public ChannelList() {
		mVisibleChannels = new SparseArray<Channel>();
		mHiddenChannels = new SparseArray<Channel>();
		mHiddenChannelsLabels = new SparseArray<Label>();
		colorSetup();
	}
	
	// Método que configura los colores de los canales
	private void colorSetup() {
		// Genero colores
		mColorArray[0] = new RGB(150, 0, 150); 			// Violeta
		mColorArray[1] = new RGB(200, 75, 0); 			// Naranja
		mColorArray[2] = new RGB(0, 116, 194); 			// Azul
		mColorArray[3] = new RGB(0, 153, 77); 			// Verde
		mColorArray[4] = new RGB(255, 51, 102);			// Rojo Maraca
		mColorArray[5] = new RGB(60, 60, 60); 			// Negro
		mColorArray[6] = new RGB(250, 0, 204); 			// Rosa
		mColorArray[7] = new RGB(179, 189, 0); 			// Marrón
		mColorArray[8] = new RGB(204, 204, 0); 			// Amarillo
	}
	
	public void hideChannel(int channelIndex) {
		int channelKey = mVisibleChannels.keyAt(channelIndex);
		mHiddenChannels.append(channelKey, mVisibleChannels.get(channelKey));
		Label label = new Label(channelKey+1);
		mHiddenChannelsLabels.append(channelKey, label);
		mVisibleChannels.remove(channelKey);
		// Actualizo todos los canales
		updateChannels();
	}
	
	public void removeChannel(int channelIndex) {
		int channelKey = mHiddenChannels.keyAt(channelIndex);
		mHiddenChannels.remove(channelKey);
		mHiddenChannelsLabels.remove(channelKey);
		updateChannels();
	}
	
	private void updateChannels() {
		for(int i = 0; i < mVisibleChannels.size(); i++) {
			mVisibleChannels.valueAt(i).update(mVisibleChannels.size(), i);
		}
	}
	
	public int size() {
		return mVisibleChannels.size();
	}
	
	public Channel getChannelAtKey(int channelKey) {
		return mVisibleChannels.get(channelKey);
	}
	
	public Channel getChannelAtIndex(int index) {
		return mVisibleChannels.valueAt(index);
	}
	
	public int getChannelKey(int index) {
		return mVisibleChannels.keyAt(index);
	}

	public SparseArray<Label> getHiddenChannelsLabels() {
		return mHiddenChannelsLabels;
	}

	public SparseArray<Channel> getHiddenChannels() {
		return mHiddenChannels;
	}

	// Add online channel
	public void addChannel(int channelNumber, int mTotalHeight, int mTotalWidth, int mTotalPages, StudyData studyData) {
		// Genero canal
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, mTotalPages, studyData);
		
		// Si el canal ya se encuentra en la lista
		if(mVisibleChannels.get(channelNumber) != null) {
			// Copio ese canal al final de la lista y actualizo su color y label de canal
			int newChannelNumber = mVisibleChannels.size() + mHiddenChannels.size();
			mVisibleChannels.append(newChannelNumber, mVisibleChannels.get(channelNumber));
			mVisibleChannels.get(channelNumber).setColor(mColorArray[newChannelNumber]);
			mVisibleChannels.get(channelNumber).getInfoBox().setChannelNumber(newChannelNumber);
			mVisibleChannels.get(channelNumber).getInfoBox().createChannelNumberLabel();
		} 
		
		// Reemplazo el canal actual con el nuevo canal
		mVisibleChannels.remove(channelNumber);
		mVisibleChannels.append(channelNumber, channel);
		
		// Actualizo todos los canales
		updateChannels();
	}

	// Add offline channel
	public void addChannel(int mTotalHeight, int mTotalWidth, StudyData studyData) {
		// Genero canal
		int channelNumber = mVisibleChannels.size() + mHiddenChannels.size();
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, studyData);
		// Agrego canal
		mVisibleChannels.append(channel.getChannelNumber(), channel);
		// Actualizo todos los canales
		updateChannels();
	}

	public void restoreChannel(int channelKey) {
		mVisibleChannels.append(channelKey, mHiddenChannels.get(channelKey));
		mHiddenChannels.remove(channelKey);
		mHiddenChannelsLabels.remove(channelKey);
		updateChannels();
	}
}
