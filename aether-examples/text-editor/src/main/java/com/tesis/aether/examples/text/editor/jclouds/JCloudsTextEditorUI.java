package com.tesis.aether.examples.text.editor.jclouds;

import java.io.IOException;
import java.net.MalformedURLException;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;
import com.tesis.aether.examples.text.editor.common.ui.TextEditorUI;

public class JCloudsTextEditorUI extends TextEditorUI{

	public JCloudsTextEditorUI() throws MalformedURLException, IOException {
		super();
	}

	@Override
	protected CloudAdapter getCloud() {
		return JCloudsAdapter.INSTANCE;
	}
	
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
					new JCloudsTextEditorUI().setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

}
