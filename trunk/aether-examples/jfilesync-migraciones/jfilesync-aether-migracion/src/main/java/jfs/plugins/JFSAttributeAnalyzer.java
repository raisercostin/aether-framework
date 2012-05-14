/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */

package jfs.plugins;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jfs.conf.JFSConfig;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSText;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerManager;

/**
 * This plugin is able to modify the data/time of the source or target file
 * trees. This is commonly used to correct time stamps of file trees (e.g.,
 * after summer and winter time modification).
 * 
 * @author Jens Heidrich
 * @version $Id: JFSAttributeAnalyzer.java,v 1.5 2004/02/11 12:27:42 heidrich
 *          Exp $
 */
public class JFSAttributeAnalyzer implements JFSPlugin {

	/** Vector storing all read only files! */
	private Vector<String> readOnlyFiles = new Vector<String>();

	/**
	 * @see JFSPlugin#getId()
	 */
	public String getId() {
		return "plugin.aa.name";
	}

	/**
	 * @see JFSPlugin#init(JFrame)
	 */
	public void init(JFrame frame) {
		JFSText t = JFSText.getInstance();
		readOnlyFiles.clear();

		// Create main panel:
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));

		JLabel desciption = new JLabel(t.get("plugin.aa.description"));
		panel.add(desciption);

		// Create configuration panel:
		JPanel confPanel = new JPanel(new GridLayout(1, 2));
		panel.add(confPanel);

		JLabel boxLabel = new JLabel(t.get("plugin.aa.location"));
		boxLabel.setHorizontalAlignment(JLabel.CENTER);
		confPanel.add(boxLabel);

		Vector<String> location = new Vector<String>();
		location.add(t.get("plugin.aa.source"));
		location.add(t.get("plugin.aa.target"));

		JComboBox box = new JComboBox(location);
		confPanel.add(box);

		int result = JOptionPane.showConfirmDialog(frame, panel,
				t.get(getId()), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			int i = box.getSelectedIndex();
			boolean sourceTree = false;

			if (i == 0)
				sourceTree = true;

			modifyAttributes(sourceTree);

			// View result:
			JList list = new JList(readOnlyFiles);
			JScrollPane pane = new JScrollPane(list);
			JOptionPane.showMessageDialog(frame, pane, t
					.get("plugin.aa.result.title"),
					JOptionPane.INFORMATION_MESSAGE);
			readOnlyFiles.clear();
		}
	}

	/**
	 * Modifies the write protection attribute of source or target files.
	 * 
	 * @param source
	 *            Determines whether source date/time stamps (true) or target
	 *            stamps (false) have to be modified.
	 */
	public void modifyAttributes(boolean source) {
		JFSConfig config = JFSConfig.getInstance();
		for (JFSDirectoryPair pair : config.getDirectoryList()) {
			String path;
			if (source) {
				path = pair.getSrc();
			} else {
				path = pair.getTgt();
			}
			JFSFileProducerManager pm = JFSFileProducerManager.getInstance();
			JFSFileProducer factory = pm.createProducer(path);
			JFSFile file = factory.getRootJfsFile();

			traverse(file);

			pm.shutDownProducer(path);
		}
	}

	/**
	 * Modifies a given file object and traverses the whole file system tree
	 * structure.
	 * 
	 * @param file
	 *            The file to modify.
	 */
	public void traverse(JFSFile file) {
		if (!file.canWrite()) {
			readOnlyFiles.add(file.getPath());
		}

		if (file.isDirectory()) {
			JFSFile[] files = file.getList();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					traverse(files[i]);
				}
			}
		}
	}
}