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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConst;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This class launches and stops the JFS server.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSServer.java,v 1.23 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSServer extends Thread {

	/** All active sockets. */
	private Vector<Socket> sockets = new Vector<Socket>();

	/** The used server socket. */
	private ServerSocket serverSocket;

	/**
	 * The default constructor is package protected. You have to use a server
	 * factory in order to get a server.
	 * 
	 * @see JFSServerFactory
	 */
	JFSServer() {
	}

	/**
	 * Stopps the server. This will close the server socket and therewith cause
	 * a SocketException which will shutdown the server at once. As an
	 * alternative you may send an interrupt, which will shutdown the server
	 * after accepting a last socket connection. ATTENTION: If there is no such
	 * last client requesting a connection, the server will not shut down after
	 * getting an interrupt, but wait for the last connection.
	 */
	public synchronized void stopServer() {
		JFSText t = JFSText.getInstance();

		// Shut down all sockets using a new vector in order to avoid
		// concurrent modification when socket is interrupted:
		for (Socket s : new Vector<Socket>(sockets)) {
			try {
				s.close();
			} catch (IOException e) {
				// Ignore IO exceptions...
			}
		}
		sockets.clear();

		// Shut down server socket:
		interrupt();
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(
					t.get("error.external") + " " + e);
		}

		// If a command line server was used, exit application in order to
		// stop waiting for an input line from the input stream:
		if (JFSServerFactory.getInstance().isCommandLine()) {
			System.exit(0);
		}
	}

	/**
	 * @see Runnable#run()
	 */
	public void run() {
		JFSText t = JFSText.getInstance();
		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.server.start"));
		out.println(t.get("general.appName") + " "
				+ JFSConst.getInstance().getString("jfs.version"));

		try {
			int port = JFSConfig.getInstance().getServerPort();
			serverSocket = new ServerSocket(port);
			out.println(t.get("cmd.server.socket") + " ["
					+ serverSocket.getLocalPort() + "]");

			// Start server loop:
			while (!isInterrupted()) {
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(JFSConfig.getInstance().getServerTimeout());
				out.println(t.get("cmd.server.clientSocket") + " ["
						+ socket.getLocalSocketAddress() + "]");
				JFSClient handler = new JFSClient(this, socket);
				handler.start();
			}

			serverSocket.close();
		} catch (SocketException e) {
			// Ignore socket exceptions...
		} catch (Exception e) {
			JFSLog.getErr().getStream().println(
					t.get("error.external") + " " + e);
		}

		out.println(t.get("cmd.server.stop"));
	}

	/**
	 * @return Returns the sockets.
	 */
	public Vector<Socket> getSockets() {
		return sockets;
	}

	/**
	 * Transfers the contents of two streams.
	 * 
	 * @param in
	 *            The input stream.
	 * @param out
	 *            The output stream.
	 * @param length
	 *            The number of bytes to transfer.
	 * @return True if the operation was performed successfully.
	 */
	public static boolean transferContent(InputStream in, OutputStream out,
			long length) {
		JFSText t = JFSText.getInstance();
		boolean success = true;
		long transferedBytes = 0;

		try {
			byte[] buf = new byte[JFSConfig.getInstance().getBufferSize()];
			int len;
			int maxLen = JFSConfig.getInstance().getBufferSize();

			if (length < maxLen)
				maxLen = (int) length;

			while (transferedBytes < length
					&& (len = in.read(buf, 0, maxLen)) > 0) {
				out.write(buf, 0, len);
				transferedBytes += len;

				long r = length - transferedBytes;
				if (r < maxLen)
					maxLen = (int) r;
			}
		} catch (SocketTimeoutException e) {
			JFSLog.getOut().getStream().println(t.get("error.timeout"));
			success = false;
		} catch (SocketException e) {
			JFSLog.getOut().getStream()
					.println(t.get("error.socket") + " " + e);
			success = false;
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(t.get("error.io") + " " + e);
			success = false;
		}

		if (transferedBytes != length)
			success = false;

		return success;
	}

	/**
	 * Used to perform basic tests of the JFS server.
	 * 
	 * @param args
	 *            Provided command-line arguments.
	 */
	public static void main(String[] args) {
		// Start server:
		String host = "localhost";
		int port = 55201;
		JFSConfig.getInstance().setServerPort(port);
		boolean success;

		JFSServer server = JFSServerFactory.getInstance().getServer();
		server.start();
		JFSServerAccess access = JFSServerAccess.getInstance(host, port, ".");
		PrintStream out = JFSLog.getOut().getStream();

		for (int i = 0; i < 10; i++) {
			// Test getting info for directories and files:
			JFSFileInfo info;
			info = new JFSFileInfo("test/", "");
			info = access.getInfo(info);
			info.print();
			info = new JFSFileInfo("test/", "dirs");
			info = access.getInfo(info);
			info.print();
			info = new JFSFileInfo("test/", "dirs/source/c/IconSource.gif");
			info = access.getInfo(info);
			info.print();

			// Test putting info for files:
			info.setLastModified(System.currentTimeMillis() - 100000);
			success = access.putInfo(info);
			out.println("Putting Info: " + success);
			info = access.getInfo(info);
			info.print();

			// Test getting contents from file:
			try {
				File file = new File("test/dirs/source/CopyOfIconSource.gif");
				InputStream is = access.getContents(info);
				FileOutputStream fileOut = new FileOutputStream(file);
				success = transferContent(is, fileOut, info.getLength());
				fileOut.close();
			} catch (Exception e) {
				success = false;
			}
			out.println("Getting Contents: " + success);
			info = new JFSFileInfo("test/", "dirs/source/CopyOfIconSource.gif");
			info = access.getInfo(info);
			info.print();

			// Test putting contents to file:
			File file = new File("test/dirs/source/c/IconSource.gif");
			info = new JFSFileInfo("test/", "dirs/source/Copy2OfIconSource.gif");
			info.setLastModified(file.lastModified());
			info.setLength(file.length());
			try {
				FileInputStream fileIn = new FileInputStream(file);
				OutputStream os = access.putContents(info);
				success = transferContent(fileIn, os, info.getLength());
				fileIn.close();
			} catch (Exception e) {
				success = false;
			}
			out.println("Putting Contents: " + success);
			info = new JFSFileInfo("test/", "dirs/source/Copy2OfIconSource.gif");
			info = access.getInfo(info);
			info.print();

			// Test creating new directory:
			info = new JFSFileInfo("test/", "dirs/source/newDir");
			success = access.mkdir(info);
			out.println("Creating Directory: " + success);
			out.println();

			// Test deleting files and dirs:
			info = new JFSFileInfo("test/", "dirs/source/CopyOfIconSource.gif");
			success = access.delete(info);
			out.println("Deleting File: " + success);
			info = new JFSFileInfo("test/", "dirs/source/Copy2OfIconSource.gif");
			success = access.delete(info);
			out.println("Deleting File: " + success);
			info = new JFSFileInfo("test/", "dirs/source/newDir");
			success = access.delete(info);
			out.println("Deleting File: " + success);
			out.println();
		}

		// Shutdown server:
		server.stopServer();
	}
}