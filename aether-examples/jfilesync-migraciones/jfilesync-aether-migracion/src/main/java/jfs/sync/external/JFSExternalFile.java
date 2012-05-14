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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import jfs.conf.JFSConfig;
import jfs.server.JFSFileInfo;
import jfs.server.JFSServerAccess;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSProgress;

/**
 * Represents an external file and uses a JFS server to get the necessary
 * information.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSExternalFile.java,v 1.10 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSExternalFile extends JFSFile {

	/** The retrieved file information object from the server. */
	private JFSFileInfo info = null;

	/** The list of included files. */
	private JFSFile[] list = null;

	/** The server access object to use. */
	private JFSServerAccess access = null;

	/**
	 * Creates a new external root file and reads the structure from server.
	 * 
	 * @param access
	 *            The server access object to use.
	 * @param fileProducer
	 *            The assigned file producer.
	 */
	public JFSExternalFile(JFSServerAccess access, JFSFileProducer fileProducer) {
		super(fileProducer, "");
		this.access = access;

		// Get information object from server:
		info = access.getInfo(new JFSFileInfo(fileProducer.getRootPath(), ""));

		// Create dummy object with default values, if the server connection
		// failed:
		if (info == null)
			info = new JFSFileInfo(fileProducer.getRootPath(), "");
	}

	/**
	 * Creates a new external file for a certain path using a specific file
	 * producer.
	 * 
	 * @param access
	 *            The server access object to use.
	 * @param fileProducer
	 *            The assigned file producer.
	 * @param path
	 *            The path to create the external file for.
	 */
	public JFSExternalFile(JFSServerAccess access, JFSFileProducer fileProducer, String path) {
		super(fileProducer, path);
		this.access = access;
		info = new JFSFileInfo(fileProducer.getRootPath(), path);
	}

	/**
	 * Creates an external file based on a previously read-in structure.
	 * 
	 * @param access
	 *            The server access object to use.
	 * @param fileProducer
	 *            The assigned file producer.
	 * @param info
	 *            The previously read-in file information object.
	 */
	private JFSExternalFile(JFSServerAccess access, JFSFileProducer fileProducer, JFSFileInfo info) {
		super(fileProducer, info.getRelativePath());
		this.access = access;
		this.info = info;
	}

	/**
	 * @see JFSFile#canRead()
	 */
	public boolean canRead() {
		return info.canRead();
	}

	/**
	 * @see JFSFile#canWrite()
	 */
	public boolean canWrite() {
		return info.canWrite();
	}

	/**
	 * @see JFSFile#getInputStream()
	 */
	protected InputStream getInputStream() {
		return access.getContents(info);
	}

	/**
	 * @see JFSFile#getOutputStream()
	 */
	protected OutputStream getOutputStream() {
		return access.putContents(info);
	}

	/**
	 * @see JFSFile#closeInputStream()
	 */
	protected void closeInputStream() {
		// No operation has to be performed for External Files. Closing
		// input stream would close the socket. The generated file
		// input stream on server side, will be closed automatically
		// by the JFS server. The socket is closed after all operations
		// are done.
	}

	/**
	 * @see JFSFile#closeOutputStream()
	 */
	protected void closeOutputStream() {
		// No operation has to be performed for External Files. Closing
		// output stream would close the socket. The generated file
		// output stream on server side, will be closed automatically
		// by the JFS server. The socket is closed after all operations
		// are done.
	}

	/**
	 * @see JFSFile#delete()
	 */
	public boolean delete() {
		return access.delete(info);
	}

	/**
	 * @see JFSFile#exists()
	 */
	public boolean exists() {
		return info.exists();
	}

	/**
	 * @see JFSFile#getLastModified()
	 */
	public long getLastModified() {
		return info.getLastModified();
	}

	/**
	 * @see JFSFile#getLength()
	 */
	public long getLength() {
		return info.getLength();
	}

	/**
	 * @see JFSFile#getList()
	 */
	public JFSFile[] getList() {
		if (list == null) {
			JFSFileInfo[] files = info.getList();

			if (files != null) {
				list = new JFSExternalFile[files.length];

				for (int i = 0; i < files.length; i++) {
					list[i] = new JFSExternalFile(access, fileProducer, files[i]);
				}
			} else {
				list = new JFSExternalFile[0];
			}
		}

		return list;
	}

	/**
	 * @see JFSFile#getFile()
	 */
	public final File getFile() {
		return null;
	}

	/**
	 * @see JFSFile#getName()
	 */
	public String getName() {
		return info.getName();
	}

	/**
	 * @see JFSFile#getPath()
	 */
	public String getPath() {
		return info.getVirtualPath();
	}

	/**
	 * @see JFSFile#isDirectory()
	 */
	public boolean isDirectory() {
		return info.isDirectory();
	}

	/**
	 * @see JFSFile#mkdir()
	 */
	public boolean mkdir() {
		return access.mkdir(info);
	}

	/**
	 * @see JFSFile#setLastModified(long)
	 */
	public boolean setLastModified(long time) {
		info.setLastModified(time);

		return true;
	}

	/**
	 * @see JFSFile#setReadOnly()
	 */
	public boolean setReadOnly() {
		if (!JFSConfig.getInstance().isSetCanWrite()) {
			return true;
		}

		info.setReadOnly();

		return true;
	}

	/**
	 * @see JFSFile#preCopyTgt(JFSFile)
	 */
	protected boolean preCopyTgt(JFSFile srcFile) {
		// Set last modified and read-only only when file is no directory:
		if (!srcFile.isDirectory()) {
			info.setLastModified(srcFile.getLastModified());
			info.setLength(srcFile.getLength());
			if (!srcFile.canWrite())
				info.setReadOnly();
		}

		return true;
	}

	/**
	 * @see JFSFile#preCopySrc(JFSFile)
	 */
	protected boolean preCopySrc(JFSFile tgtFile) {
		return true;
	}

	/**
	 * @see JFSFile#postCopyTgt(JFSFile)
	 */
	protected boolean postCopyTgt(JFSFile srcFile) {
		// Update information object after copy. This method is only
		// called if all operations were performed successfully:
		info.setDirectory(srcFile.isDirectory());
		info.setExists(srcFile.exists());
		info.setLength(srcFile.getLength());

		if (JFSProgress.getInstance().isCanceled()) {
			access.closeSocket();
		}

		access.releaseSocket();

		return true;
	}

	/**
	 * @see JFSFile#postCopySrc(JFSFile)
	 */
	protected boolean postCopySrc(JFSFile tgtFile) {
		access.releaseSocket();
		return true;
	}

	/**
	 * @see JFSFile#flush()
	 */
	public boolean flush() {
		// Post data to server to perform changes:
		return access.putInfo(info);
	}

	@Override
	protected boolean upload(File file) {
		return false;
	}

	@Override
	public String getMD5() {
		return "00000000";
	}
}