package com.tesis.aether.examples.remote.monitor.jclouds;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;
import com.tesis.aether.examples.remote.monitor.common.ui.BaseRemoteMonitorUI;

public class JCloudsRemoteMonitorUI extends BaseRemoteMonitorUI {

	@Override
	protected RemoteFileMonitor getRemoteFileMonitor() {
		return JCloudsRemoteFileMonitor.INSTANCE;
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new JCloudsRemoteMonitorUI().setVisible(true);
			}
		});
	}

}
