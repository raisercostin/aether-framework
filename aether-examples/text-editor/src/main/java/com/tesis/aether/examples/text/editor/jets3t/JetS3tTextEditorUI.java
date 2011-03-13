package com.tesis.aether.examples.text.editor.jets3t;

import java.io.IOException;
import java.net.MalformedURLException;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;
import com.tesis.aether.examples.text.editor.common.ui.TextEditorUI;

public class JetS3tTextEditorUI extends TextEditorUI{

	public JetS3tTextEditorUI() throws MalformedURLException, IOException {
		super();
	}

	@Override
	protected CloudAdapter getCloud() {
		return JetS3tAdapter.INSTANCE;
	}
	
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
					new JetS3tTextEditorUI().setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

}
