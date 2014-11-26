package com.ufavaloro.android.visu.draw.channel;


import android.util.SparseArray;

public class ChannelList {

	private SparseArray<Channel> mChannelList;
	private SparseArray<Channel> mDeletedChannels;

	public ChannelList() {
		mChannelList = new SparseArray<Channel>();
		mDeletedChannels = new SparseArray<Channel>();
	}
	
	public void addChannel(Channel channel) {
		// Agrego canal
		mChannelList.append(channel.getChannelNumber(), channel);
		// Actualizo todos los canales
		updateChannels();
	}
	
	public void removeChannelAtKey(int channelKey) {
		mDeletedChannels.append(channelKey, mChannelList.get(channelKey));
		mChannelList.remove(channelKey);
		// Actualizo todos los canales
		updateChannels();
	}
	
	public void removeChannelAtIndex(int index) {
	
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
}
