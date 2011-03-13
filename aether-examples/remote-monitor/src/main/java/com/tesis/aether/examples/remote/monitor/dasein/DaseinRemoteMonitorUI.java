package com.tesis.aether.examples.remote.monitor.dasein;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;
import com.tesis.aether.examples.remote.monitor.common.ui.BaseRemoteMonitorUI;

public class DaseinRemoteMonitorUI extends BaseRemoteMonitorUI {

	@Override
	protected RemoteFileMonitor getRemoteFileMonitor() {
		return DaseinRemoteFileMonitor.INSTANCE;
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new DaseinRemoteMonitorUI().setVisible(true);
			}
		});
	}

}
