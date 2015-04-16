package com.ufavaloro.android.visu.connection;

import android.os.Handler;

public class Connection {

	private ConnectionType mConnectionType;
	private ConnectionMode mConnectionMode;
	protected int mConnectionIndex;
	protected Handler mConnectionInterfaceHandler;
	private boolean mIsConnected;
	
	public Connection(ConnectionType connectionType, ConnectionMode connectionMode,
			Handler connectionInterfacehandler, int connectionIndex) {
		
		mConnectionType = connectionType;
		mConnectionMode = connectionMode;
		mConnectionInterfaceHandler = connectionInterfacehandler;
		mIsConnected = false;
		mConnectionIndex = connectionIndex;
		
		if(mConnectionMode == ConnectionMode.SLAVE) slaveConnection();
	}
	
	public void slaveConnection() {}

	public boolean isConnected() {
		return mIsConnected;
	}
	
	public void setConnected(boolean value) {
		mIsConnected = value;
	}
	
	public void stop() {}
}
