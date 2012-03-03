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

import java.io.Serializable;

import jfs.conf.JFSConfig;

/**
 * The transmitted object by the JFS client in order to get information from the
 * server or to update information stored on server side.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSTransmission.java,v 1.10 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSTransmission implements Serializable {
	/** The UID. */
	private static final long serialVersionUID = 43L;

	/**
	 * The server wants to get information about a certain file (identified by
	 * the attached file info object).
	 */
	public static final byte CMD_GET_INFO = 0;

	/**
	 * The server wants to update information about a certain file (identified
	 * and supplied by the attached file info object).
	 */
	public static final byte CMD_PUT_INFO = 1;

	/**
	 * The server wants to get the contents of a certain file (identified by the
	 * attached file info object).
	 */
	public static final byte CMD_GET_CONTENTS = 2;

	/**
	 * The server wants to update the contents of a certain file (identified by
	 * the attached file info object and supplied by the input stream).
	 */
	public static final byte CMD_PUT_CONTENTS = 3;

	/**
	 * The server wants to create a directory (identified by the attached file
	 * info object).
	 */
	public static final byte CMD_MKDIR = 4;

	/**
	 * The server wants to delete a file (identified by the attached file info
	 * object).
	 */
	public static final byte CMD_DELETE = 5;

	/**
	 * Checks whether the socket is still alive.
	 */
	public static final byte CMD_IS_ALIVE = 6;

	/**
	 * Shut down the server.
	 */
	public static final byte CMD_IS_SHUTDOWN = 7;

	/** The transmitted command identifier. */
	private byte command = -1;

	/** The transmited file information object. */
	private JFSFileInfo info = null;

	/** The transmitted passphrase to be allowed to access the server. */
	private String passphrase = JFSConfig.getInstance().getServerPassPhrase();

	/**
	 * Creates a new transmission.
	 * 
	 * @param command
	 *            The transmitted command.
	 */
	public JFSTransmission(byte command) {
		this.command = command;
	}

	/**
	 * Creates a new transmission.
	 * 
	 * @param command
	 *            The transmitted command.
	 * @param info
	 *            The transmited file information object.
	 */
	public JFSTransmission(byte command, JFSFileInfo info) {
		this.command = command;
		this.info = info;
	}

	/**
	 * Returns the transmitted command.
	 * 
	 * @return The command identifier.
	 */
	public byte getCommand() {
		return command;
	}

	/**
	 * Returns the transmited file information object.
	 * 
	 * @return File info object.
	 */
	public JFSFileInfo getInfo() {
		return info;
	}

	/**
	 * Returns the transmitted passphrase to be allowed to access the server.
	 * 
	 * @return The passphrase.
	 */
	public String getPassphrase() {
		return passphrase;
	}
}