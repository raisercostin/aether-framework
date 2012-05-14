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

package jfs.sync.aether;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.local.JFSLocalFile;

import org.apache.commons.io.FilenameUtils;

import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class JFSAetherFile extends JFSFile {

	private ExtendedStorageService	service;
	private String					bucket;
	private boolean					canRead		= true;
	private boolean					canWrite	= true;
	private StorageObjectMetadata	blob;
	private JFSFile[]				list;
	private String					name;
	private String					path;
	private boolean					isDirectory;
	private String					fullPath;
	private InputStream	stream;

	// aether://testbuckettt/NewFolder/
	protected JFSAetherFile(JFSFileProducer fileProducer, String relativePath, ExtendedStorageService service, String bucket) {
		super(fileProducer, relativePath);
		this.service = service;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		try {
			blob = service.getMetadataForObject(bucket, pathAndName);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		setFullPath();
		setIsDirectory();
	}

	private void setFullPath() {
		if (name.isEmpty()) {
			fullPath = path + "/";
		} else {
			fullPath = path + "/" + name;
		}
		fullPath = fullPath.replaceAll("//", "/");
	}

	private void setIsDirectory() {
		if (blob == null) {
			try {
				isDirectory = service.checkDirectoryExists(bucket, fullPath);
			} catch (MethodNotSupportedException e) {
				isDirectory = false;
			}
		} else {
			isDirectory = blob.isDirectory();
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
		return blob != null ? blob.getLength() : 0;
	}

	@Override
	public long getLastModified() {
		if (isDirectory()) {
			return 0;
		} else {
			return blob.getLastModified().getTime();
		}
	}

	@Override
	public JFSFile[] getList() {
		try {
			if (list == null) {
				List<StorageObjectMetadata> listFiles = service.listFiles(bucket, fullPath, false);

				List<JFSFile> objects = new ArrayList<JFSFile>();
				for (StorageObjectMetadata storageObjectMetadata : listFiles) {
					String name = !fileProducer.getRootPath().isEmpty() ? storageObjectMetadata.getPathAndName().replaceFirst(fileProducer.getRootPath() + "/", "") : storageObjectMetadata.getPathAndName();
					objects.add(new JFSAetherFile(fileProducer, name, service, bucket));
				}
				if (objects.size() > 0) {
					list = objects.toArray(new JFSFile[objects.size()]);
				} else {
					list = new JFSLocalFile[0];
				}
			}
		} catch (Exception e) {
			list = new JFSLocalFile[0];
		}
		return list;
	}

	@Override
	public boolean exists() {
		return blob != null || fullPath.isEmpty();
	}

	@Override
	public boolean mkdir() {
		try {
			String directory = fullPath;
			service.createFolder(bucket, directory);
			setIsDirectory();
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
				service.deleteFolder(bucket, fullPath);
			} else {
				service.deleteFile(bucket, fullPath);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() {
		if (isDirectory() || blob == null) {
			return null;
		} else {
			try {
				stream = service.getInputStream(blob.getContainer(), blob.getPathAndName());
				return stream;
			} catch (FileNotExistsException e) {
				return null;
			}
		}
	}

	@Override
	protected OutputStream getOutputStream() {
		return null;
	}

	@Override
	protected void closeInputStream() {
		try {
			if (stream != null) {
				stream.close();
			}
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
			String blobName = fullPath;
			service.uploadSingleFile(file, bucket, FilenameUtils.getFullPathNoEndSeparator(blobName), FilenameUtils.getName(blobName));
			blob = service.getMetadataForObject(bucket, blobName);
			setIsDirectory();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public String getMD5() {
		if (blob != null) {
			return blob.getMd5hash();
		} else {
			return "00000000";
		}
	}

}