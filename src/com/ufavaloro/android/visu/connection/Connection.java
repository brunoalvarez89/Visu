package com.ufavaloro.android.visu.connection;

import android.os.Handler;

public class Connection {

	private ConnectionType mConnectionType;
	private Handler mConnectionInterfaceHandler;
	
	public Connection(ConnectionType connectionType, Handler connectionInterfacehandler) {
		mConnectionType = connectionType;
	}
	
	public void slaveConnection() {
		
	}
}
