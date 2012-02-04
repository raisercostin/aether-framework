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

package jfs.sync.external;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConst;
import jfs.server.JFSServerAccess;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

/**
 * This class produces external JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSExternalFileProducer.java,v 1.1 2005/05/06 11:06:56 heidrich
 *          Exp $
 */
public class JFSExternalFileProducer extends JFSFileProducer {
	/** The remote host name. */
	private final String host;

	/** The remote port number. */
	private final int port;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSExternalFileProducer(String uri) {
		super(JFSConst.SCHEME_EXTERNAL, uri);
		String hostAndPort = getHostAndPort(uri);
		host = getHost(hostAndPort);
		port = getPort(hostAndPort);
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public JFSFile getRootJfsFile() {
		JFSServerAccess a = JFSServerAccess.getInstance(host, port,
				getRootPath());
		return new JFSExternalFile(a, this);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public JFSFile getJfsFile(String path) {
		JFSServerAccess a = JFSServerAccess.getInstance(host, port,
				getRootPath());
		return new JFSExternalFile(a, this, path);
	}

	/**
	 * Extracts the host and port part of a given path of the form
	 * 'ext://host:port/directory'.
	 * 
	 * @param path
	 *            The path to analyze.
	 * @return The host and port part of the path.
	 */
	private static String getHostAndPort(String path) {
		int start = path.indexOf("://");
		int end = path.indexOf("/", start + 3);

		if (start != -1 && end != -1) {
			return path.substring(start + 3, end);
		} else if (start != -1) {
			return path.substring(start + 3);
		} else {
			return "";
		}
	}

	/**
	 * Extracts the host name from a given host and port string of the form
	 * 'host:port'.
	 * 
	 * @param hostAndPort
	 *            The host and port.
	 * @return Returns the host.
	 */
	private static String getHost(String hostAndPort) {
		int sep = hostAndPort.indexOf(":");

		if (sep != -1) {
			return hostAndPort.substring(0, sep);
		} else {
			return hostAndPort;
		}
	}

	/**
	 * Extracts the port from a given host and port string of the form
	 * 'host:port'.
	 * 
	 * @param hostAndPort
	 *            The host and port.
	 * @return Returns the port.
	 */
	private static int getPort(String hostAndPort) {
		int sep = hostAndPort.indexOf(":");

		if (sep != -1) {
			try {
				return Integer.parseInt(hostAndPort.substring(sep + 1));
			} catch (NumberFormatException e) {
				return JFSConfig.getInstance().getServerPort();
			}
		} else {
			return JFSConfig.getInstance().getServerPort();
		}
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}
}