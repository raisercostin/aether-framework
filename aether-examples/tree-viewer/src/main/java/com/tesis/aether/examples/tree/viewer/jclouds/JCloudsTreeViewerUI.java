package com.tesis.aether.examples.tree.viewer.jclouds;

import java.io.IOException;
import java.net.MalformedURLException;

import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.ui.TreeViewerUI;

public class JCloudsTreeViewerUI extends TreeViewerUI{

	private static final long serialVersionUID = 1L;

	public JCloudsTreeViewerUI() throws MalformedURLException, IOException {
		super();
	}

	@Override
	protected TreeFileViewer getCloud() {
		return JCloudsTreeViewer.INSTANCE;
	}
	
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
					new JCloudsTreeViewerUI().setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

}
