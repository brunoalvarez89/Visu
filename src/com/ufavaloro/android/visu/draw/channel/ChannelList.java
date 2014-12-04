package com.ufavaloro.android.visu.draw.channel;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.datatypes.StudyData;

import android.util.SparseArray;

public class ChannelList {

	// Visible Channel List. These channels will be drawn.
	private SparseArray<Channel> mOnlineChannelList;
	private SparseArray<Channel> mOfflineChannelList;
	// Hidden Channel List and their respective Labels. These channels will not be drawn.
	private SparseArray<Channel> mHiddenChannelList;
	private SparseArray<Label> mHiddenChannelsLabels;
	// Channel colors
	public RGB[] mColorArray = new RGB[10];
	
	// Constructor
	public ChannelList() {
		mOnlineChannelList = new SparseArray<Channel>();
		mOfflineChannelList = new SparseArray<Channel>();
		mHiddenChannelList = new SparseArray<Channel>();
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
		int channelKey = mOnlineChannelList.keyAt(channelIndex);
		mHiddenChannelList.append(channelKey, mOnlineChannelList.get(channelKey));
		Label label = new Label(channelKey+1);
		mHiddenChannelsLabels.append(channelKey, label);
		mOnlineChannelList.remove(channelKey);
		// Actualizo todos los canales
		updateChannels();
	}
	
	public void removeChannel(int channelIndex) {
		int channelKey = mHiddenChannelList.keyAt(channelIndex);
		mHiddenChannelList.remove(channelKey);
		mHiddenChannelsLabels.remove(channelKey);
		updateChannels();
	}
	
	private void updateChannels() {
		for(int i = 0; i < mOnlineChannelList.size(); i++) {
			mOnlineChannelList.valueAt(i).update(mOnlineChannelList.size(), i);
		}
	}
	
	public int size() {
		return (mOnlineChannelList.size() + mOfflineChannelList.size());
	}
	
	public Channel getChannelAtKey(int channelKey) {
		return mOnlineChannelList.get(channelKey);
	}
	
	public Channel getChannelAtIndex(int index) {
		return mOnlineChannelList.valueAt(index);
	}
	
	public int getChannelKey(int index) {
		return mOnlineChannelList.keyAt(index);
	}

	public SparseArray<Label> getHiddenChannelsLabels() {
		return mHiddenChannelsLabels;
	}

	public SparseArray<Channel> getHiddenChannels() {
		return mHiddenChannelList;
	}

	// Add online channel
	public void addChannel(int mTotalHeight, int mTotalWidth, int mTotalPages, StudyData studyData) {
		int channelNumber = studyData.getAcquisitionData().getAdcChannel();
		// Genero canal
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, mTotalPages, studyData);
		
		// Si el canal ya se encuentra en la lista
		if(mOnlineChannelList.get(channelNumber) != null) {
			// Copio ese canal al final de la lista y actualizo su color y label de canal
			int newChannelNumber = mOnlineChannelList.size() + mHiddenChannelList.size();
			mOnlineChannelList.append(newChannelNumber, mOnlineChannelList.get(channelNumber));
			mOnlineChannelList.get(channelNumber).setColor(mColorArray[newChannelNumber]);
			mOnlineChannelList.get(channelNumber).getInfoBox().setChannelNumber(newChannelNumber);
			mOnlineChannelList.get(channelNumber).getInfoBox().createChannelNumberLabel();
		} 
		
		// Reemplazo el canal actual con el nuevo canal
		mOnlineChannelList.remove(channelNumber);
		mOnlineChannelList.append(channelNumber, channel);
		
		// Actualizo todos los canales
		updateChannels();
	}

	// Add offline channel
	public void addChannel(int mTotalHeight, int mTotalWidth, StudyData studyData) {
		// Genero canal
		int channelNumber = mOnlineChannelList.size() + mHiddenChannelList.size();
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, studyData);
		// Agrego canal
		mOnlineChannelList.append(channel.getChannelNumber(), channel);
		// Actualizo todos los canales
		updateChannels();
	}

	public void restoreChannel(int channelKey) {
		mOnlineChannelList.append(channelKey, mHiddenChannelList.get(channelKey));
		mHiddenChannelList.remove(channelKey);
		mHiddenChannelsLabels.remove(channelKey);
		updateChannels();
	}
}
