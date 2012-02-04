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

package jfs.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConfigObserver;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This produces and maintains a JFS server.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSServerFactory.java,v 1.14 2007/07/20 08:12:05 heidrich Exp $
 */
public class JFSServerFactory implements JFSConfigObserver {

	/** The reference to a JFS server object if started. */
	private JFSServer server = null;

	/** Determines whether a command line server is started. */
	private boolean isCommandLine = false;

	/** Stores the only instance of the class. */
	private static JFSServerFactory instance = null;

	/**
	 * Creates a new server factory.
	 */
	private JFSServerFactory() {
		JFSConfig.getInstance().attach(this);
	}

	/**
	 * Returns the reference of the only object of the class.
	 * 
	 * @return The only instance.
	 */
	public static JFSServerFactory getInstance() {
		if (instance == null)
			instance = new JFSServerFactory();

		return instance;
	}

	/**
	 * @return Returns a server. If the server is not alive, a new one is
	 *         created.
	 */
	public JFSServer getServer() {
		if (!isServerAlive())
			server = new JFSServer();
		return server;
	}

	/**
	 * @return Returns whether a server is still alive; that is, the server
	 *         object exists and the thread is alive and not interrupted.
	 */
	public boolean isServerAlive() {
		return (server != null && server.isAlive() && !server.isInterrupted());
	}

	/**
	 * Starts the JFS server on the command line and waits for a shutdown.
	 */
	public void startCmdLineServer() {
		isCommandLine = true;
		JFSText t = JFSText.getInstance();
		JFSServer localServer = getServer();
		JFSLog.getOut().getStream().println(t.get("cmd.server.stop.howto"));

		BufferedReader cmdReader = new BufferedReader(new InputStreamReader(
				System.in));
		String input = "";

		localServer.start();

		while (input != null && !input.equals("stop")) {
			try {
				input = cmdReader.readLine().toLowerCase();
			} catch (Exception e) {
				input = null;
			}
		}

		isCommandLine = false;

		if (localServer.isAlive())
			localServer.stopServer();
	}

	/**
	 * Starts the JFS server service.
	 */
	public void startService() {
		isCommandLine = true;
		JFSServer localServer = getServer();
		localServer.start();
	}

	/**
	 * Stops the JFS server service.
	 */
	public void stopService() {
		isCommandLine = true;
		JFSConfig config = JFSConfig.getInstance();
		JFSServerAccess access = JFSServerAccess.getInstance("localhost",
				config.getServerPort(), config.getServerBase());
		access.shutDown();
	}

	/**
	 * @see JFSConfigObserver#updateConfig(JFSConfig)
	 */
	public void updateConfig(JFSConfig config) {
		// There is nothing to do for the task object in this case.
	}

	/**
	 * @see JFSConfigObserver#updateComparison(JFSConfig)
	 */
	public void updateComparison(JFSConfig config) {
		// There is nothing to do for the task object in this case.
	}

	/**
	 * @see JFSConfigObserver#updateServer(JFSConfig)
	 */
	public void updateServer(JFSConfig config) {
		// Restart the server, if it is alive:
		if (isServerAlive()) {
			server.stopServer();
			server = null;
			getServer();
			server.start();
		}
	}

	/**
	 * Determines whether the server was started from the command line.
	 * 
	 * @return True if and only if the server was started from the command line.
	 */
	public boolean isCommandLine() {
		return isCommandLine;
	}
}