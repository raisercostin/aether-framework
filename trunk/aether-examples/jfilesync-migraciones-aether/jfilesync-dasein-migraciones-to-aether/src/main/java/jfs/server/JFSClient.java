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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This class creates a simple JFS client that listens at a given port on the
 * localhost and handles a client request. A client may send (1) a JFS
 * transmission object which contains the command that should be executed on
 * server side and (2) file contents as a byte stream. Depending on the
 * transmitted command the server sends back (1) file information objects, (2)
 * file contents as a byte stream, (3) or a boolean value indicating whether a
 * certain action was performed successfully.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSClient.java,v 1.15 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSClient extends Thread {

	/** The JFS server that manages the client. */
	private JFSServer server;

	/** The socket to read from and write to. */
	private Socket socket;

	/**
	 * Creates a new server thread for a client request.
	 * 
	 * @param server
	 *            The JFS server that manages the client.
	 * @param socket
	 *            Client socket.
	 */
	public JFSClient(JFSServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
		server.getSockets().add(socket);
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		JFSText t = JFSText.getInstance();
		try {
			try {
				while (!interrupted()) {
					handleClient();
				}
			} catch (SocketTimeoutException e) {
				JFSLog.getOut().getStream().println(t.get("error.timeout"));
			}
			server.getSockets().remove(socket);
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (SocketException e) {
			// Ignore socket exceptions...
		} catch (EOFException e) {
			// Ignore end-of-file exceptions thrown, when the JFS client is
			// shut down before the client socket on server side terminates.
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					t.get("error.external") + " " + e);
		}
	}

	/**
	 * Reads the contents of the input stream.
	 * 
	 * @throws SocketException
	 *             Thrown in case of socket problems.
	 * @throws IOException
	 *             Thrown in case of IO problems.
	 * @throws ClassNotFoundException
	 *             Thrown if the transmission class is missing.
	 */
	private void handleClient() throws SocketException, IOException,
			ClassNotFoundException {
		// Translation object:
		JFSText text = JFSText.getInstance();

		// Read from client:
		InputStream in = socket.getInputStream();
		ObjectInputStream oi = new ObjectInputStream(in);
		JFSTransmission t = (JFSTransmission) oi.readObject();
		JFSFileInfo info = t.getInfo();

		// Check for authentication:
		if (!t.getPassphrase().equals(
				JFSConfig.getInstance().getServerPassPhrase())) {
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.accessDenied"));

			return;
		}

		// Write to client:
		OutputStream out = socket.getOutputStream();
		ObjectOutputStream oo;
		File file;
		boolean success;

		switch (t.getCommand()) {
		case JFSTransmission.CMD_GET_INFO:
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.gettingInfo") + " "
							+ info.getVirtualPath());
			info.update();
			oo = new ObjectOutputStream(out);
			oo.writeObject(info);

			break;

		case JFSTransmission.CMD_PUT_INFO:
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.puttingInfo") + " "
							+ info.getVirtualPath());
			success = info.updateFileSystem();
			oo = new ObjectOutputStream(out);
			oo.writeObject(new Boolean(success));

			break;

		case JFSTransmission.CMD_GET_CONTENTS:
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.gettingContents") + " "
							+ info.getVirtualPath());
			file = info.complete();

			FileInputStream inFile = new FileInputStream(file);
			JFSServer.transferContent(inFile, out, file.length());
			inFile.close();

			break;

		case JFSTransmission.CMD_PUT_CONTENTS:
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.puttingContents") + " "
							+ info.getVirtualPath());
			file = info.complete();

			if (file.exists())
				file.delete();

			oo = new ObjectOutputStream(out);
			oo.writeObject(info);

			FileOutputStream outFile = new FileOutputStream(info.getPath());
			success = JFSServer.transferContent(in, outFile, info.getLength());
			outFile.close();

			// Updates the file system; that is sets last modified and can
			// write property:
			if (!success) {
				file.delete();
			} else {
				if (!info.updateFileSystem()) {
					JFSLog.getOut().getStream().println(
							text.get("error.update"));
					file.delete();
				}
			}

			break;

		case JFSTransmission.CMD_MKDIR:
			JFSLog.getOut().getStream().println(
					text.get("cmd.server.mkdir") + " " + info.getVirtualPath());
			file = info.complete();
			success = true;

			if (!file.exists())
				success = file.mkdir();

			if (success)
				info.setExists(true);

			oo = new ObjectOutputStream(out);
			oo.writeObject(info);

			break;

		case JFSTransmission.CMD_DELETE:
			JFSLog.getOut().getStream()
					.println(
							text.get("cmd.server.delete") + " "
									+ info.getVirtualPath());
			file = info.complete();
			success = true;

			if (file.exists())
				success = file.delete();

			oo = new ObjectOutputStream(out);
			oo.writeObject(new Boolean(success));

			break;

		case JFSTransmission.CMD_IS_ALIVE:
			JFSLog.getOut().getStream().println(text.get("cmd.server.isAlive"));
			oo = new ObjectOutputStream(out);
			oo.writeObject(new Boolean(true));

			break;

		case JFSTransmission.CMD_IS_SHUTDOWN:
			JFSLog.getOut().getStream()
					.println(text.get("cmd.server.shutdown"));
			interrupt();
			JFSServerFactory.getInstance().getServer().stopServer();

			break;
		}
	}
}