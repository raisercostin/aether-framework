package com.tesis.aether.examples.tree.viewer.jets3t;

import java.io.IOException;
import java.net.MalformedURLException;

import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.ui.TreeViewerUI;

public class JetS3tTreeViewerUI extends TreeViewerUI{

	private static final long serialVersionUID = 1L;

	public JetS3tTreeViewerUI() throws MalformedURLException, IOException {
		super();
	}

	@Override
	protected TreeFileViewer getCloud() {
		return JetS3tTreeViewer.INSTANCE;
	}
	
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
					new JetS3tTreeViewerUI().setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

}
