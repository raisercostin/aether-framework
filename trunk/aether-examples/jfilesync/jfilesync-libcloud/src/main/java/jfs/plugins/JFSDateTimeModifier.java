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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jfs.conf.JFSConfig;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSLog;
import jfs.conf.JFSSettings;
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
 * @version $Id: JFSDateTimeModifier.java,v 1.10 2005/04/03 15:03:30 heidrich
 *          Exp $
 */
public class JFSDateTimeModifier implements JFSPlugin {

	/**
	 * @see JFSPlugin#getId()
	 */
	public String getId() {
		return "plugin.dtm.name";
	}

	/**
	 * @see JFSPlugin#init(JFrame)
	 */
	public void init(JFrame frame) {
		JFSText t = JFSText.getInstance();

		// Create main panel:
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));

		JLabel desciption = new JLabel(t.get("plugin.dtm.description"));
		panel.add(desciption);

		// Create configuration panel:
		JPanel confPanel = new JPanel(new GridLayout(1, 4));
		panel.add(confPanel);

		JLabel boxLabel = new JLabel(t.get("plugin.dtm.location"));
		boxLabel.setHorizontalAlignment(JLabel.CENTER);
		confPanel.add(boxLabel);

		Vector<String> location = new Vector<String>();
		location.add(t.get("plugin.dtm.source"));
		location.add(t.get("plugin.dtm.target"));

		JComboBox box = new JComboBox(location);
		confPanel.add(box);

		JLabel diffLabel = new JLabel(t.get("plugin.dtm.difference"));
		diffLabel.setHorizontalAlignment(JLabel.CENTER);
		confPanel.add(diffLabel);

		SpinnerNumberModel diffModel = new SpinnerNumberModel();
		diffModel.setValue(new Integer(60));

		JSpinner spinner = new JSpinner(diffModel);
		confPanel.add(spinner);

		int result = JOptionPane.showConfirmDialog(frame, panel,
				t.get(getId()), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			int i = box.getSelectedIndex();
			long ms = diffModel.getNumber().longValue() * 60000;
			boolean sourceTree = false;

			if (i == 0)
				sourceTree = true;

			modifyDateTimeStamps(sourceTree, ms);
		}
	}

	/**
	 * Modifies the date/time stamps of source or target files.
	 * 
	 * @param source
	 *            Determines whether source date/time stamps (true) or target
	 *            stamps (false) have to be modified.
	 * @param ms
	 *            The modification interval.
	 */
	public void modifyDateTimeStamps(boolean source, long ms) {
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

			traverse(file, ms);
			file.flush();

			pm.shutDownProducer(path);
		}
	}

	/**
	 * Modifies a given file object and traverses the whole file system tree
	 * structure.
	 * 
	 * @param file
	 *            The file to modify.
	 * @param ms
	 *            The modification interval.
	 */
	public void traverse(JFSFile file, long ms) {
		if (!file.isDirectory()) {
			if (!JFSSettings.getInstance().isDebug()) {
				file.setLastModified(file.getLastModified() + ms);
			} else {
				JFSLog.getOut().getStream().println(
						file.getName() + ", Old: " + file.getLastModified()
								+ ", New:" + file.getLastModified() + ms);
			}
		} else {
			JFSFile[] files = file.getList();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					traverse(files[i], ms);
				}
			}
		}
	}
}