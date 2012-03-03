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

package jfs.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConst;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSSettings;
import jfs.conf.JFSText;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerManager;
import jfs.sync.external.JFSExternalFileProducer;

/**
 * This dialog manages adding/changing directory pairs.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSConfigFilterView.java,v 1.10 2006/08/28 11:31:54 heidrich
 *          Exp $
 */
public class JFSConfigDirectoryView extends JDialog implements ActionListener {
	/** The UID. */
	private static final long serialVersionUID = 564L;

	/** The source element. */
	private JTextField srcElement;

	/** The target element. */
	private JTextField tgtElement;

	/** The source type combo box. */
	private JComboBox srcTypeCombo;

	/** The target type combo box. */
	private JComboBox tgtTypeCombo;

	/** The configuration object to modify. */
	private final JFSConfig config;

	/** The directory pair to modify. */
	private final JFSDirectoryPair pair;

	/**
	 * Initializes the config view.
	 * 
	 * @param dialog
	 *            The main frame.
	 * @param config
	 *            The configuration to change.
	 * @param pair
	 *            The directory pair to modify.
	 */
	public JFSConfigDirectoryView(JDialog dialog, JFSConfig config, JFSDirectoryPair pair) {
		super(dialog, true);
		this.config = config;
		this.pair = pair;

		// Get the translation object:
		JFSText t = JFSText.getInstance();

		// Create the modal dialog:
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(t.get("profile.dir.title"));
		setResizable(false);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JFSFileProducerManager fileProducerManager = JFSFileProducerManager.getInstance();

		// Create source row:
		JLabel srcLabel = new JLabel(t.get("profile.dir"));
		JLabel srcTypeLabel = new JLabel(t.get("profile.dir.type"));
		srcElement = new JTextField(pair.getSrc(), 40);
		srcTypeCombo = new JComboBox();
		srcTypeCombo.addItem(t.get("profile.dir.scheme.local"));
		srcTypeCombo.addItem(t.get("profile.dir.scheme.external"));
		srcTypeCombo.addItem(t.get("profile.dir.scheme.jclouds"));
		JButton srcChangeButton = JFSSupport.getButton("button.change", "button.change.src", this);
		JPanel srcRow1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		srcRow1Panel.add(srcLabel);
		srcRow1Panel.add(srcElement);
		JPanel srcRow2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		srcRow2Panel.add(srcTypeLabel);
		srcRow2Panel.add(srcTypeCombo);
		srcRow2Panel.add(srcChangeButton);
		JPanel srcRowPanel = new JPanel(new BorderLayout());
		srcRowPanel.setBorder(new TitledBorder(t.get("profile.dir.table.src")));
		srcRowPanel.add(srcRow1Panel, BorderLayout.NORTH);
		srcRowPanel.add(srcRow2Panel, BorderLayout.SOUTH);
		JFSFileProducer srcFileProducer = fileProducerManager.createProducer(pair.getSrc());
		if (srcFileProducer.getScheme().equals(JFSConst.SCHEME_LOCAL)) {
			srcTypeCombo.setSelectedIndex(0);
		} else {
			srcTypeCombo.setSelectedIndex(1);
		}

		// Create target row:
		JLabel tgtLabel = new JLabel(t.get("profile.dir"));
		JLabel tgtTypeLabel = new JLabel(t.get("profile.dir.type"));
		tgtElement = new JTextField(pair.getTgt(), 40);
		tgtTypeCombo = new JComboBox();
		tgtTypeCombo.addItem(t.get("profile.dir.scheme.local"));
		tgtTypeCombo.addItem(t.get("profile.dir.scheme.external"));
		tgtTypeCombo.addItem(t.get("profile.dir.scheme.jclouds"));
		JButton tgtChangeButton = JFSSupport.getButton("button.change", "button.change.tgt", this);
		JPanel tgtRow1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tgtRow1Panel.add(tgtLabel);
		tgtRow1Panel.add(tgtElement);
		JPanel tgtRow2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tgtRow2Panel.add(tgtTypeLabel);
		tgtRow2Panel.add(tgtTypeCombo);
		tgtRow2Panel.add(tgtChangeButton);
		JPanel tgtRowPanel = new JPanel(new BorderLayout());
		tgtRowPanel.setBorder(new TitledBorder(t.get("profile.dir.table.tgt")));
		tgtRowPanel.add(tgtRow1Panel, BorderLayout.NORTH);
		tgtRowPanel.add(tgtRow2Panel, BorderLayout.SOUTH);
		JFSFileProducer tgtFileProducer = fileProducerManager.createProducer(pair.getTgt());
		if (tgtFileProducer.getScheme().equals(JFSConst.SCHEME_LOCAL)) {
			tgtTypeCombo.setSelectedIndex(0);
		} else if (tgtFileProducer.getScheme().equals(JFSConst.SCHEME_EXTERNAL)) {
			tgtTypeCombo.setSelectedIndex(1);
		} else {
			tgtTypeCombo.setSelectedIndex(2);
		}

		// Create source and target panel:
		JPanel dirPanel = new JPanel(new GridLayout(2, 1));
		dirPanel.add(srcRowPanel);
		dirPanel.add(tgtRowPanel);

		// Create buttons in a separate panel:
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
		buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

		// Add all panels:
		cp.add(dirPanel, BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		// Pack and activate dialog:
		pack();
		JFSSupport.center(dialog, this);
		this.setVisible(true);
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		JFSText t = JFSText.getInstance();
		String cmd = event.getActionCommand();

		if (cmd.equals("button.cancel") || cmd.equals("button.ok")) {
			setVisible(false);
			dispose();
		}

		if (cmd.equals("button.ok")) {
			pair.setSrc(srcElement.getText());
			pair.setTgt(tgtElement.getText());
			if (!config.hasDirectoryPair(pair)) {
				config.addDirectoryPair(pair);
			}

			// Test for existing directory:
			JFSConfigView.createDirectoryDialog(this, pair.getSrc());
			JFSConfigView.createDirectoryDialog(this, pair.getTgt());
		}

		if (cmd.equals("button.change.src") || cmd.equals("button.change.tgt")) {
			boolean isSource = true;
			int type = srcTypeCombo.getSelectedIndex();

			if (cmd.equals("button.change.tgt")) {
				isSource = false;
				type = tgtTypeCombo.getSelectedIndex();
			}

			if (type == 0) {
				JFSSettings settings = JFSSettings.getInstance();

				int returnVal;
				JFileChooser chooser = new JFileChooser();
				chooser.setApproveButtonText(t.get("button.select"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Get file:
				if (isSource) {
					chooser.setDialogTitle(t.get("profile.dir.getSrc.title"));
					if (srcElement.getText().trim().equals("")) {
						chooser.setCurrentDirectory(settings.getLastSrcPairDir());
					} else {
						chooser.setCurrentDirectory(new File(srcElement.getText()));
					}
					returnVal = chooser.showOpenDialog(this);
					settings.setLastSrcPairDir(chooser.getCurrentDirectory());
				} else {
					chooser.setDialogTitle(t.get("profile.dir.getTgt.title"));
					if (tgtElement.getText().trim().equals("")) {
						chooser.setCurrentDirectory(settings.getLastTgtPairDir());
					} else {
						chooser.setCurrentDirectory(new File(tgtElement.getText()));
					}
					returnVal = chooser.showOpenDialog(this);
					settings.setLastTgtPairDir(chooser.getCurrentDirectory());
				}

				// Set file and check for existence:
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String directory = chooser.getSelectedFile().getPath();
					if (isSource) {
						srcElement.setText(directory);
					} else {
						tgtElement.setText(directory);
					}
					JFSConfigView.createDirectoryDialog(this, directory);
				}
			} else if (type == 1) {
				// Create dialog:
				JFSFileProducerManager fileProducerManager = JFSFileProducerManager.getInstance();
				String file = srcElement.getText();
				if (!isSource) {
					file = tgtElement.getText();
				}
				String hostStr = "localhost";
				int portInt = JFSConfig.getInstance().getServerPort();
				String dirStr = "/";
				JFSFileProducer fileProducer = fileProducerManager.createProducer(file);
				if (fileProducer.getScheme().equals(JFSConst.SCHEME_EXTERNAL)) {
					try {
						JFSExternalFileProducer externalFileProducer = (JFSExternalFileProducer) fileProducer;
						hostStr = externalFileProducer.getHost();
						portInt = externalFileProducer.getPort();
						dirStr = externalFileProducer.getOriginalRootPath();
					} catch (ClassCastException e) {
					}
				}

				JLabel hostLabel = new JLabel(t.get("profile.server.host"));
				JTextField host = new JTextField(hostStr, 20);

				JLabel portLabel = new JLabel(t.get("profile.server.port"));
				SpinnerNumberModel port = new SpinnerNumberModel(portInt, 0, 1000000, 1);
				JSpinner portSpinner = new JSpinner(port);

				JLabel dirLabel = new JLabel(t.get("profile.server.dir"));
				JTextField dir = new JTextField(dirStr, 20);

				JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				row1Panel.add(hostLabel);
				row1Panel.add(host);

				JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				row2Panel.add(portLabel);
				row2Panel.add(portSpinner);

				JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				row3Panel.add(dirLabel);
				row3Panel.add(dir);

				JPanel panel = new JPanel(new GridLayout(3, 1));
				panel.add(row1Panel);
				panel.add(row2Panel);
				panel.add(row3Panel);

				int result = JOptionPane.showConfirmDialog(this, panel, t.get("profile.server.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				if (result == JOptionPane.OK_OPTION) {
					String directory = "ext://" + host.getText();
					if (JFSConfig.getInstance().getServerPort() != port.getNumber().intValue()) {
						directory += ":" + port.getNumber();
					}
					directory += "/" + dir.getText();

					if (isSource) {
						srcElement.setText(directory);
					} else {
						tgtElement.setText(directory);
					}
				}
			}
		}
	}
}