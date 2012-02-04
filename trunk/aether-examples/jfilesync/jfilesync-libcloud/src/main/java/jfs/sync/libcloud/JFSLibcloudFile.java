package jfs.sync.libcloud;

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

import simplecloud.storage.providers.amazon.S3Adapter;
import base.interfaces.IItem;
import base.types.Item;

public class JFSLibcloudFile extends JFSFile {

	private String bucket;
	private boolean canRead = true;
	private boolean canWrite = true;
	private JFSFile[] list;
	private String name;
	private String path;
	private boolean isDirectory;
	private String fullPath;
	private S3Adapter adapter;
	private Map<Object, Object> options = null;
	private Map<String, String> metadata = null;
	private IItem item;

	//libcloud://testbuckettt/NewFolder/
	protected JFSLibcloudFile(JFSFileProducer fileProducer, String relativePath, S3Adapter adapter, String bucket) {
		super(fileProducer, relativePath);
		this.adapter = adapter;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		setFullPath();
		options = new HashMap<Object, Object>();
		options.put(S3Adapter.Type.SRC_BUCKET, bucket);

		try {
			item = adapter.fetchItem("/" + fullPath, options);
			metadata = adapter.fetchMetadata("/" + fullPath, options);
			isDirectory = false;
		} catch (Exception e) {
			try {
				item = adapter.fetchItem("/" + fullPath + "/", options);
				metadata = adapter.fetchMetadata("/" + fullPath + "/", options);
				isDirectory = true;
				fullPath = fullPath + "/";
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
		return item != null ? item.getContentLength() : 0;
	}

	@Override
	public long getLastModified() {
		if (isDirectory()) {
			return 0;
		} else {
			Date date;
			try {
				String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
				SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				date = df.parse(metadata.get("Last-Modified"));
				return date.getTime();
			} catch (ParseException e) {
				return 0;
			}
		}
	}

	@Override
	public JFSFile[] getList() {
		if (list == null) {
			List<String> listItems = adapter.listItems("/", options);

			if (listItems != null && listItems.size() > 0) {
				List<JFSFile> objects = new ArrayList<JFSFile>();

				for (String file : listItems) {
					String foundFilePath;
					if (file.endsWith("/")) {
						foundFilePath = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.getFullPathNoEndSeparator(file));
					} else {
						foundFilePath = FilenameUtils.getFullPathNoEndSeparator(file);
					}
					String thisFilePath = FilenameUtils.getFullPathNoEndSeparator(fullPath);

					if (foundFilePath.equals(thisFilePath) && !file.contains(" ") && file.startsWith(fullPath) && !file.equals(fullPath) && !file.endsWith("_$folder$")) {
						String name = file.replaceFirst(fileProducer.getRootPath() + "/", "");
						if(name.endsWith("/")) {
							name = FilenameUtils.getFullPathNoEndSeparator(name);
						}
						objects.add(new JFSLibcloudFile(fileProducer, name, adapter, bucket));
					}
				}

				if (objects.size() > 0) {
					list = objects.toArray(new JFSFile[objects.size()]);
				} else {
					list = new JFSLibcloudFile[0];
				}
			} else {
				list = new JFSLibcloudFile[0];
			}
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
			Item uploadItem = new Item(new ByteArrayInputStream(new byte[0]), "binary/octet-stream", null, 0);
			if (adapter.storeItem("/" + fullPath + "/", uploadItem, new HashMap<String, String>(), options)) {
				item = adapter.fetchItem("/" + fullPath + "/", options);
				metadata = adapter.fetchMetadata("/" + fullPath + "/", options);
				isDirectory = true;
				fullPath = fullPath + "/";
				return true;
			} else {
				return false;
			}
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
			adapter.deleteItem("/" + fullPath, options);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() {
		if (!isDirectory && item != null) {
			return item.getContent();
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
			item.getContent().close();
		} catch (IOException e) {
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
			InputStream stream = new BufferedInputStream(new FileInputStream(file));
			Item uploadItem = new Item(stream, "application/xml", null, file.length());
			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(S3Adapter.Type.SRC_BUCKET, bucket);
			if (adapter.storeItem("/" + fullPath, uploadItem, new HashMap<String, String>(), options)) {
				item = adapter.fetchItem("/" + fullPath, options);
				metadata = adapter.fetchMetadata("/" + fullPath, options);
				isDirectory = false;
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getMD5() {
		if (metadata != null) {
			return metadata.get("ETag").replaceAll("\"", "");
		} else {
			return "00000000";
		}
	}

}