package com.tesis.aether.examples.text.editor.dasein;

import java.io.IOException;
import java.net.MalformedURLException;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;
import com.tesis.aether.examples.text.editor.common.ui.TextEditorUI;

public class DaseinTextEditorUI extends TextEditorUI{

	public DaseinTextEditorUI() throws MalformedURLException, IOException {
		super();
	}

	@Override
	protected CloudAdapter getCloud() {
		return DaseinAdapter.INSTANCE;
	}
	
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
					new DaseinTextEditorUI().setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

}
