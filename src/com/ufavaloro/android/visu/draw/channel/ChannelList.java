package com.ufavaloro.android.visu.draw.channel;

import com.ufavaloro.android.visu.draw.RGB;
import com.ufavaloro.android.visu.storage.data.StudyData;

import android.util.SparseArray;

public class ChannelList {

	// Channel List. These channels will be drawn.
	private SparseArray<Channel> mChannelList;
	// Deleted Channel List and their respective Labels. These channels will not be drawn.
	private SparseArray<Channel> mDeletedChannels;
	private SparseArray<Label> mDeletedChannelsLabels;
	// Channel colors
	public RGB[] mColorArray = new RGB[10];
	
	// Constructor
	public ChannelList() {
		mChannelList = new SparseArray<Channel>();
		mDeletedChannels = new SparseArray<Channel>();
		mDeletedChannelsLabels = new SparseArray<Label>();
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
	
	public void removeChannelAtIndex(int channelIndex) {
		int channelKey = mChannelList.keyAt(channelIndex);
		mDeletedChannels.append(channelKey, mChannelList.get(channelKey));
		Label label = new Label(channelKey+1);
		mDeletedChannelsLabels.append(channelKey, label);
		mChannelList.remove(channelKey);
		// Actualizo todos los canales
		updateChannels();
	}
	
	private void updateChannels() {
		for(int i = 0; i < mChannelList.size(); i++) {
			mChannelList.valueAt(i).update(mChannelList.size(), i);
		}
	}
	
	public int size() {
		return mChannelList.size();
	}
	
	public Channel getChannelAtKey(int channelKey) {
		return mChannelList.get(channelKey);
	}
	
	
	public Channel getChannelAtIndex(int index) {
		return mChannelList.valueAt(index);
	}
	
	
	public int getChannelKey(int index) {
		return mChannelList.keyAt(index);
	}
	
	
	public SparseArray<Label> getDeletedChannelsLabels() {
		return mDeletedChannelsLabels;
	}

	
	public SparseArray<Channel> getDeletedChannels() {
		return mDeletedChannels;
	}

	
	// Add online channel
	public void addChannel(int channelNumber, int mTotalHeight, int mTotalWidth, int mTotalPages, StudyData studyData) {
		// Genero canal
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, mTotalPages, studyData);
		
		// Si el canal ya se encuentra en la lista
		if(mChannelList.get(channelNumber) != null) {
			// Copio ese canal al final de la lista y actualizo su color y label de canal
			int newChannelNumber = mChannelList.size() + mDeletedChannels.size();
			mChannelList.append(newChannelNumber, mChannelList.get(channelNumber));
			mChannelList.get(channelNumber).setColor(mColorArray[newChannelNumber]);
			mChannelList.get(channelNumber).getInfoBox().setChannelNumber(newChannelNumber);
			mChannelList.get(channelNumber).getInfoBox().createChannelNumberLabel();
		} 
		
		// Reemplazo el canal actual con el nuevo canal
		mChannelList.remove(channelNumber);
		mChannelList.append(channelNumber, channel);
		
		// Actualizo todos los canales
		updateChannels();
	}

	
	// Add offline channel
	public void addChannel(int mTotalHeight, int mTotalWidth, StudyData studyData) {
		// Genero canal
		int channelNumber = mChannelList.size() + mDeletedChannels.size();
		RGB color = mColorArray[channelNumber];
		Channel channel = new Channel(channelNumber, mTotalHeight, mTotalWidth, color, studyData);
		// Agrego canal
		mChannelList.append(channel.getChannelNumber(), channel);
		// Actualizo todos los canales
		updateChannels();
	}

	public void restoreChannel(int channelKey) {
		mChannelList.append(channelKey, mDeletedChannels.get(channelKey));
		mDeletedChannels.remove(channelKey);
		mDeletedChannelsLabels.remove(channelKey);
		updateChannels();
	}
}
