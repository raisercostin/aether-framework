package jfs.sync.dasein;

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

import org.apache.commons.io.FilenameUtils;
import org.dasein.cloud.aws.storage.S3;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

public class JFSDaseinFile extends JFSFile {

	private String bucket;
	private boolean canRead = true;
	private boolean canWrite = true;
	private JFSFile[] list;
	private String name;
	private String path;
	private boolean isDirectory;
	private String fullPath;
	private BlobStoreSupport adapter;
	private CloudStoreObject item;
	private FileInputStream fileInputStream;

	protected JFSDaseinFile(JFSFileProducer fileProducer, String relativePath, BlobStoreSupport adapter, String bucket) {
		super(fileProducer, relativePath);
		this.adapter = adapter;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		setFullPath();

		CloudStoreObject object = new CloudStoreObject();
		object.setContainer(false);
		object.setDirectory(bucket);
		object.setName(path);
		object.setProviderRegionId("us-east-1");

		item = object;
		
		try {
			if (adapter.exists(bucket, "/" + fullPath, false) >= 0l) {
				isDirectory = false;

			} else if (adapter.exists(bucket, "/" + fullPath + "/", false) >= 0l) {
				isDirectory = true;
				fullPath = fullPath + "/";
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public JFSDaseinFile(JFSFileProducer fileProducer, String relativePath, BlobStoreSupport adapter, String bucket, CloudStoreObject file) {
		super(fileProducer, relativePath);
		this.adapter = adapter;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		setFullPath();
		item = file;
		isDirectory = false;
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
		return item != null ? item.getSize() : 0;
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
				date = df.parse(item.getCreationDate().toString());
				return date.getTime();
			} catch (Exception e) {
				return 0;
			}
		}
	}

	@Override
	public JFSFile[] getList() {
		try {
			if (list == null) {
				Iterable<CloudStoreObject> listItems = adapter.listFiles(bucket);
				Iterator<CloudStoreObject> iterator = listItems.iterator();

				if (listItems != null && iterator.hasNext()) {
					List<JFSFile> objects = new ArrayList<JFSFile>();

					while (iterator.hasNext()) {
						CloudStoreObject file = iterator.next();
						String foundFilePath;
						if (file.getName().endsWith("/")) {
							foundFilePath = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.getFullPathNoEndSeparator(file.getName()));
						} else {
							foundFilePath = FilenameUtils.getFullPathNoEndSeparator(file.getName());
						}
						foundFilePath = foundFilePath.isEmpty() ? file.getName() : foundFilePath;
						
						String thisFilePath = FilenameUtils.getFullPathNoEndSeparator(fullPath);

						if (foundFilePath.equals(thisFilePath) && !file.getName().contains(" ") && file.getName().startsWith(fullPath) && !file.getName().equals(fullPath) && !file.getName().endsWith("_$folder$")) {
							String name = file.getName().replaceFirst(fileProducer.getRootPath() + "/", "");
							if (name.endsWith("/")) {
								name = FilenameUtils.getFullPathNoEndSeparator(name);
							}
							objects.add(new JFSDaseinFile(fileProducer, name, adapter, bucket, file));
						}
					}

					if (objects.size() > 0) {
						list = objects.toArray(new JFSFile[objects.size()]);
					} else {
						list = new JFSDaseinFile[0];
					}
				} else {
					list = new JFSDaseinFile[0];
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return list;
	}

	@Override
	public boolean exists() {
		return item != null;
	}

	@Override
	public boolean mkdir() {
		return true;
		/*try {
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
		}*/
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
			adapter.removeFile(bucket, fullPath, false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() {
		if (!isDirectory && item != null) {
			FileTransfer download;
			try {
				File toFile = new File(item.getName());
				download = adapter.download(item, toFile);

				while (!download.isComplete()) {
					Thread.sleep(500);
				}
				fileInputStream = new FileInputStream(toFile);
				return fileInputStream;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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
			fileInputStream.close();
			new File(item.getName()).delete();
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
			adapter.upload(file, bucket, fullPath, false, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getMD5() {
		return "00000000";
	}

}