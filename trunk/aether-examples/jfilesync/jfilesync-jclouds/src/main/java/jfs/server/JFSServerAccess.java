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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This class provides methods for JFS clients to access the server's
 * functionality. It is used by a JFSExternalFile in order to access a JFS
 * server.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSServerAccess.java,v 1.18 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSServerAccess extends Thread {

	/** Stores the only instance of the class. */
	private static HashMap<String, JFSServerAccess> instances = new HashMap<String, JFSServerAccess>();

	/** The sockets on client side for different hosts and ports. */
	private Socket socket = null;

	/** The last time when releasing a socket. */
	private long lastSocketRelease = -1;

	/** The host. */
	private String host;

	/** The port. */
	private int port;

	/**
	 * Creates a new server access object for the client.
	 * 
	 * @param host
	 *            The server host.
	 * @param port
	 *            The server port.
	 */
	private JFSServerAccess(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Creates a new server access object for the client.
	 * 
	 * @param host
	 *            The server host.
	 * @param port
	 *            The server port.
	 * @param path
	 *            The path for the connection in order to identify the location
	 *            on server side.
	 */
	public synchronized static JFSServerAccess getInstance(String host,
			int port, String path) {
		String key = "ext://" + host + ":" + port + "/" + path;
		JFSServerAccess instance = instances.get(key);

		if (instance == null) {
			instance = new JFSServerAccess(host, port);
			instances.put(key, instance);
		}

		return instance;
	}

	/**
	 * Checks whether a socket is available and creates a new one if necessary.
	 * 
	 * @throws UnknownHostException
	 *             Thrown, if the host is unknown.
	 * @throws IOException
	 *             Thrown, if the socket cannot be created.
	 */
	private synchronized void checkSocket() throws UnknownHostException,
			IOException {
		int timeout = JFSConfig.getInstance().getServerTimeout();

		if (socket == null || !socket.isConnected() || socket.isClosed()
				|| !socket.isBound() || socket.isInputShutdown()
				|| socket.isOutputShutdown()) {
			socket = new Socket(host, port);
			socket.setSoTimeout(timeout);
		} else {
			// Test whether socket is alive if it was not used for some time:
			if (lastSocketRelease == -1
					|| System.currentTimeMillis() - lastSocketRelease > timeout / 2) {
				try {
					OutputStream out = socket.getOutputStream();
					ObjectOutputStream oo = new ObjectOutputStream(out);
					JFSTransmission t = new JFSTransmission(
							JFSTransmission.CMD_IS_ALIVE);
					oo.writeObject(t);
					InputStream in = socket.getInputStream();
					ObjectInputStream oi = new ObjectInputStream(in);
					// Read boolean from stream (even if currently not used):
					((Boolean) oi.readObject()).booleanValue();
				} catch (Exception e) {
					closeSocket();
					socket = new Socket(host, port);
					socket.setSoTimeout(timeout);
				}
			}
		}
	}

	/**
	 * Releases a socket.
	 */
	public synchronized void releaseSocket() {
		lastSocketRelease = System.currentTimeMillis();
	}

	/**
	 * Closes a socket.
	 */
	public synchronized void closeSocket() {
		if (socket != null) {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (Exception e) {
				JFSLog.getErr().getStream().println(
						JFSText.getInstance().get("error.external") + " " + e);
			}
		}
	}

	/**
	 * Gets a file information object from a JFS server. In case of problems
	 * null is returned.
	 * 
	 * @param info
	 *            The file information object identifying the file to get info
	 *            about.
	 * @return The retrieved file information object.
	 */
	public synchronized JFSFileInfo getInfo(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			JFSTransmission t = new JFSTransmission(
					JFSTransmission.CMD_GET_INFO, info);
			oo.writeObject(t);

			// Get results:
			InputStream in = socket.getInputStream();
			ObjectInputStream oi = new ObjectInputStream(in);
			info = (JFSFileInfo) oi.readObject();

			releaseSocket();

			return info;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return null;
		}
	}

	/**
	 * Puts a file information object to a JFS server. In case of problems false
	 * is returned.
	 * 
	 * @param info
	 *            The file information object to put.
	 * @return True if and only if the putting was successful.
	 */
	public synchronized boolean putInfo(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			JFSTransmission t = new JFSTransmission(
					JFSTransmission.CMD_PUT_INFO, info);
			oo.writeObject(t);

			// Get results:
			InputStream in = socket.getInputStream();
			ObjectInputStream oi = new ObjectInputStream(in);
			boolean b = ((Boolean) oi.readObject()).booleanValue();

			releaseSocket();

			return b;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return false;
		}
	}

	/**
	 * Allows to get the contents of a file from a JFS server by returning an
	 * input stream. The file is identified by a file information object. In
	 * case of problems null is returned. Just the contents is transfered, no
	 * file attributes (like the last modified property) is set. This has to be
	 * done separately. ATTENTION: The streams and sockets cannot be closed by
	 * this method. This has to be done by the methods using this method.
	 * 
	 * @param info
	 *            The file information object identifying the file to get the
	 *            contents from.
	 * @return The input stream.
	 */
	public synchronized InputStream getContents(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);

			// Create new file information object to avoid transmitting the
			// whole file system's structure:
			JFSFileInfo getInfo = new JFSFileInfo(info.getRootPath(), info
					.getRelativePath());
			JFSTransmission t = new JFSTransmission(
					JFSTransmission.CMD_GET_CONTENTS, getInfo);
			oo.writeObject(t);

			// Get results and don't close streams. This has to be done by the
			// methods using this method:
			InputStream in = socket.getInputStream();

			return in;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return null;
		}
	}

	/**
	 * Allows to put the contents of a file to a JFS server by providing an
	 * input stream. The file is identified by a file information object. In
	 * case of problems null is returned. The contents is transfered and file
	 * attributes (the last modified and can write property) are set on server
	 * side. Moreover, the name, path, and virtual path information of the JFS
	 * file information object are completed with server information. ATTENTION:
	 * The streams and sockets cannot be closed by this method. This has to be
	 * done by the methods using this method.
	 * 
	 * @param info
	 *            The file information object to put.
	 * @return The output stream.
	 */
	public synchronized OutputStream putContents(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			JFSTransmission t = new JFSTransmission(
					JFSTransmission.CMD_PUT_CONTENTS, info);
			oo.writeObject(t);

			// Get results and don't close streams. This has to be done by the
			// methods using this method:
			InputStream in = socket.getInputStream();
			ObjectInputStream oi = new ObjectInputStream(in);
			JFSFileInfo serverInfo = ((JFSFileInfo) oi.readObject());

			if (serverInfo != null) {
				info.setName(serverInfo.getName());
				info.setPath(serverInfo.getPath());
				info.setRootPath(serverInfo.getRootPath());
				info.setRelativePath(serverInfo.getRelativePath());
			}

			return out;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return null;
		}
	}

	/**
	 * Creates a new directory on a JFS server. In case of problems false is
	 * returned.
	 * 
	 * @param info
	 *            The file information object to put.
	 * @return True if and only if the creation was successful.
	 */
	public synchronized boolean mkdir(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);

			// Create new file information object to avoid transmitting the
			// whole file system's structure:
			JFSFileInfo mkdirInfo = new JFSFileInfo(info.getRootPath(), info
					.getRelativePath());
			JFSTransmission t = new JFSTransmission(JFSTransmission.CMD_MKDIR,
					mkdirInfo);
			oo.writeObject(t);

			// Get results:
			InputStream in = socket.getInputStream();
			ObjectInputStream oi = new ObjectInputStream(in);
			JFSFileInfo serverInfo = ((JFSFileInfo) oi.readObject());
			boolean b = serverInfo.exists();

			if (serverInfo != null) {
				info.setName(serverInfo.getName());
				info.setPath(serverInfo.getPath());
				info.setRootPath(serverInfo.getRootPath());
				info.setRelativePath(serverInfo.getRelativePath());
			}

			releaseSocket();

			return b;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return false;
		}
	}

	/**
	 * Deletes a file on a JFS server. In case of problems false is returned.
	 * 
	 * @param info
	 *            The file information object to put.
	 * @return True if and only if the creation was successful.
	 */
	public synchronized boolean delete(JFSFileInfo info) {
		try {
			checkSocket();

			// Request information:
			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);

			// Create new file information object to avoid transmitting the
			// whole file system's structure:
			JFSFileInfo deleteInfo = new JFSFileInfo(info.getRootPath(), info
					.getRelativePath());
			JFSTransmission t = new JFSTransmission(JFSTransmission.CMD_DELETE,
					deleteInfo);
			oo.writeObject(t);

			// Get results:
			InputStream in = socket.getInputStream();
			ObjectInputStream oi = new ObjectInputStream(in);
			boolean b = ((Boolean) oi.readObject()).booleanValue();

			releaseSocket();

			return b;
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);

			return false;
		}
	}

	/**
	 * Shuts down the server from client side.
	 */
	public synchronized void shutDown() {
		try {
			checkSocket();

			OutputStream out = socket.getOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);

			JFSTransmission t = new JFSTransmission(
					JFSTransmission.CMD_IS_SHUTDOWN);
			oo.writeObject(t);

			releaseSocket();
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					JFSText.getInstance().get("error.external") + " " + e);
		}
	}

	/**
	 * Cancels the current access and closes sockets.
	 */
	public void cancel() {
		if (socket != null) {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (Exception e) {
			}
		}
	}
}