package jfs.sync.cloudloop;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

import org.apache.commons.io.FilenameUtils;

import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreDirectory;
import com.cloudloop.storage.CloudStoreFile;
import com.cloudloop.storage.CloudStoreObject;
import com.cloudloop.storage.CloudStoreObjectType;
import com.cloudloop.storage.internal.CloudStoreObjectMetadata;

public class JFSCloudLoopFile extends JFSFile {

	private String bucket;
	private boolean canRead = true;
	private boolean canWrite = true;
	private JFSFile[] list;
	private String name;
	private String path;
	private boolean isDirectory;
	private String fullPath;
	private CloudStore adapter;
	private CloudStoreObject item;
	private InputStream inputStream;

	protected JFSCloudLoopFile(JFSFileProducer fileProducer, String relativePath, CloudStore adapter, String bucket) {
		super(fileProducer, relativePath);
		this.adapter = adapter;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		setFullPath();

		try {
			item = adapter.getFile("/" + fullPath);
			if (adapter.contains(item)) {
				isDirectory = false;
				item.refreshMetadata();
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			try {
				item = adapter.getDirectory("/" + fullPath + "/");
				if (adapter.contains(item)) {
					isDirectory = true;
					fullPath = fullPath + "/";
					item.refreshMetadata();
				} else {
					throw new Exception();
				}
			} catch (Exception e1) {
			}
		}
	}

	private void setFullPath() {
		if (name.isEmpty()) {
			fullPath = path;
		} else {
			fullPath = path + "/" + name;
		}
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isDirectory() {
		return isDirectory;
	}

	@Override
	public boolean canRead() {
		return canRead;
	}

	@Override
	public boolean canWrite() {
		return canWrite;
	}

	@Override
	public long getLength() {
		return item != null ? item.getContentLengthInBytes() : 0;
	}

	@Override
	public long getLastModified() {
		if (isDirectory()) {
			return 0;
		} else {
			return item.getLastModifiedDate().getTime();
		}
	}

	@Override
	public JFSFile[] getList() {
		try {

			if (list == null) {
				CloudStoreObject[] listItems = adapter.listDirectoryContents((CloudStoreDirectory) item, CloudStoreObjectType.OBJECT, false);

				if (listItems != null && listItems.length > 0) {
					List<JFSFile> objects = new ArrayList<JFSFile>();

					for (CloudStoreObject file : listItems) {
						String name = file.getPath().getAbsolutePath().replaceFirst("/" + fileProducer.getRootPath() + "/", "");
						if (name.endsWith("/")) {
							name = FilenameUtils.getFullPathNoEndSeparator(name);
						}
						objects.add(new JFSCloudLoopFile(fileProducer, name, adapter, bucket));
						
//						String foundFilePath;
//						if (file.endsWith("/")) {
//							foundFilePath = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.getFullPathNoEndSeparator(file));
//						} else {
//							foundFilePath = FilenameUtils.getFullPathNoEndSeparator(file);
//						}
//						String thisFilePath = FilenameUtils.getFullPathNoEndSeparator(fullPath);
//
//						if (foundFilePath.equals(thisFilePath) && !file.contains(" ") && file.startsWith(fullPath) && !file.equals(fullPath) && !file.endsWith("_$folder$")) {
//
//						}
					}

					if (objects.size() > 0) {
						list = objects.toArray(new JFSFile[objects.size()]);
					} else {
						list = new JFSCloudLoopFile[0];
					}
				} else {
					list = new JFSCloudLoopFile[0];
				}
			}
		} catch (Exception e) {
			list = new JFSCloudLoopFile[0];
		}

		return list;
	}

	@Override
	public boolean exists() {
		return item != null;
	}

	@Override
	public boolean mkdir() {
		try {
			CloudStoreDirectory directory = adapter.getDirectory("/" + fullPath + "/");
			adapter.createDirectory(directory);
			item = directory;
			item.refreshMetadata();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean setLastModified(long time) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean setReadOnly() {
		return true;
	}

	@Override
	public boolean delete() {
		try {
			if (isDirectory()) {
				adapter.removeDirectory((CloudStoreDirectory) item, false);
			} else {
				adapter.removeFile((CloudStoreFile) item);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() {
		if (!isDirectory && item != null) {
			inputStream = adapter.download((CloudStoreFile) item, null);
			return inputStream;
		} else {
			return null;
		}
	}

	@Override
	protected OutputStream getOutputStream() {
		return null;
	}

	@Override
	protected void closeInputStream() {
		try {
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void closeOutputStream() {
	}

	@Override
	protected boolean preCopyTgt(JFSFile srcFile) {
		return true;
	}

	@Override
	protected boolean preCopySrc(JFSFile tgtFile) {
		return true;
	}

	@Override
	protected boolean postCopyTgt(JFSFile srcFile) {
		return true;
	}

	@Override
	protected boolean postCopySrc(JFSFile tgtFile) {
		return true;
	}

	@Override
	public boolean flush() {
		return true;
	}

	@Override
	protected boolean upload(File file) {

		try {
			CloudStoreFile destinationFile = adapter.getFile("/" + fullPath);
			destinationFile.setStreamToStore(new FileInputStream(file));
			adapter.upload(destinationFile, null);
			item = destinationFile;
			item.refreshMetadata();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getMD5() {
		if (item != null) {
			return item.getETag().replaceAll("\"", "");
		} else {
			return "00000000";
		}
	}

}