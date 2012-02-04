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

package jfs.sync.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSProgress;

/**
 * Represents a directory or a simple file. This object encapsulates the Java
 * File object.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFile.java,v 1.13 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSLocalFile extends JFSFile {

	/** The coresponding file object. */
	private File file = null;

	/** The name of the file. */
	private String name = null;

	/** The path of the file. */
	private String path = null;

	/** Tells whether the file is a directory. */
	private boolean isDirectory = false;

	/** Tells whether we can read the file. */
	private boolean canRead = true;

	/** Tells whether we can write to the file. */
	private boolean canWrite = true;

	/** Determines whether the file exists. */
	private boolean exists = false;

	/** The length of the file. Zero for directories. */
	private long length = 0;

	/** The time of last modification of the file. Zero for directories. */
	private long lastModified = 0;

	/**
	 * If the file is a directory this points to all files (and directories)
	 * within the directory. Null for all non directories.
	 */
	private JFSFile[] list = null;

	/** The last input stream opened for this file. */
	private InputStream in = null;

	/** The last output stream opened for this file. */
	private OutputStream out = null;

	/**
	 * Creates a new local JFS file object.
	 * 
	 * @param fileProducer
	 *            The assigned file producer.
	 * @param relativePath
	 *            The relative path of the JFS file starting from the root JFS
	 *            file.
	 */
	public JFSLocalFile(JFSFileProducer fileProducer, String relativePath) {
		super(fileProducer, relativePath);
		file = new File(fileProducer.getRootPath() + getRelativePath());
		name = file.getName();
		path = file.getPath();
		isDirectory = file.isDirectory();
		exists = file.exists();
		if (exists) {
			canRead = file.canRead();
			canWrite = file.canWrite();
		}
		if (!isDirectory) {
			length = file.length();
			lastModified = file.lastModified();
		}
	}

	/**
	 * @see JFSFile#getFile()
	 */
	public final File getFile() {
		return file;
	}

	/**
	 * @see JFSFile#getName()
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @see JFSFile#getPath()
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * @see JFSFile#isDirectory()
	 */
	public final boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * @see JFSFile#canRead()
	 */
	public final boolean canRead() {
		return canRead;
	}

	/**
	 * @see JFSFile#canWrite()
	 */
	public final boolean canWrite() {
		return canWrite;
	}

	/**
	 * @see JFSFile#getLength()
	 */
	public final long getLength() {
		return length;
	}

	/**
	 * @see JFSFile#getLastModified()
	 */
	public final long getLastModified() {
		return lastModified;
	}

	/**
	 * @see JFSFile#getList()
	 */
	public final JFSFile[] getList() {
		if (list == null) {
			String[] files = file.list();

			if (files != null) {
				list = new JFSFile[files.length];

				for (int i = 0; i < files.length; i++)
					list[i] = new JFSLocalFile(fileProducer, getRelativePath()
							+ File.separatorChar + files[i]);
			} else {
				list = new JFSLocalFile[0];
			}
		}

		return list;
	}

	/**
	 * @see JFSFile#exists()
	 */
	public final boolean exists() {
		return exists;
	}

	/**
	 * @see JFSFile#mkdir()
	 */
	public final boolean mkdir() {
		boolean success = file.mkdir();

		if (success)
			isDirectory = true;

		return success;
	}

	/**
	 * @see JFSFile#setLastModified(long)
	 */
	public final boolean setLastModified(long time) {
		boolean success = file.setLastModified(time);

		if (success)
			lastModified = time;

		return success;
	}

	/**
	 * @see JFSFile#setReadOnly()
	 */
	public final boolean setReadOnly() {
		if (!JFSConfig.getInstance().isSetCanWrite()) {
			return true;
		}

		boolean success = file.setReadOnly();

		if (success)
			canWrite = false;

		return success;
	}

	/**
	 * @see JFSFile#delete()
	 */
	public final boolean delete() {
		return file.delete();
	}

	/**
	 * @see JFSFile#getInputStream()
	 */
	protected InputStream getInputStream() {
		try {
			in = new FileInputStream(getPath());
			return in;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * @see JFSFile#getOutputStream()
	 */
	protected OutputStream getOutputStream() {
		try {
			out = new FileOutputStream(getPath());
			return out;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * @see JFSFile#closeInputStream()
	 */
	protected void closeInputStream() {
		JFSText t = JFSText.getInstance();
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(t.get("error.io") + " " + e);
		}
	}

	/**
	 * @see JFSFile#closeOutputStream()
	 */
	protected void closeOutputStream() {
		JFSText t = JFSText.getInstance();
		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(t.get("error.io") + " " + e);
		}
	}

	/**
	 * @see JFSFile#preCopyTgt(JFSFile)
	 */
	protected boolean preCopyTgt(JFSFile srcFile) {
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
		boolean success = true;

		// Set last modified and read-only only when file is no directory:
		if (!JFSProgress.getInstance().isCanceled() && !srcFile.isDirectory()) {
			exists = true;
			length = srcFile.getLength();
			success = success && setLastModified(srcFile.getLastModified());
			if (!srcFile.canWrite())
				success = success && setReadOnly();
		}

		return success;
	}

	/**
	 * @see JFSFile#postCopySrc(JFSFile)
	 */
	protected boolean postCopySrc(JFSFile tgtFile) {
		return true;
	}

	/**
	 * @see JFSFile#flush()
	 */
	public boolean flush() {
		return true;
	}
}