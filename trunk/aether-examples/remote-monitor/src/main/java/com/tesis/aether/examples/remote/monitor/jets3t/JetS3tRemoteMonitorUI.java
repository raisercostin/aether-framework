package com.tesis.aether.examples.remote.monitor.jets3t;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;
import com.tesis.aether.examples.remote.monitor.common.ui.BaseRemoteMonitorUI;

public class JetS3tRemoteMonitorUI extends BaseRemoteMonitorUI {

	@Override
	protected RemoteFileMonitor getRemoteFileMonitor() {
		return JetS3tRemoteFileMonitor.INSTANCE;
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new JetS3tRemoteMonitorUI().setVisible(true);
			}
		});
	}

}
