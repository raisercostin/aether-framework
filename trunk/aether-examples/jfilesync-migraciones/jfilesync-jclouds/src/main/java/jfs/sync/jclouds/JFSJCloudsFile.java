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

package jfs.sync.jclouds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.local.JFSLocalFile;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

public class JFSJCloudsFile extends JFSFile {

	private BlobStore blobStore;
	private String bucket;
	private boolean canRead = true;
	private boolean canWrite = true;
	private Blob blob;
	private JFSFile[] list;
	private String name;
	private String path;
	private boolean isDirectory;
	private String fullPath;

	//jclouds://testbuckettt/NewFolder/
	protected JFSJCloudsFile(JFSFileProducer fileProducer, String relativePath, BlobStore blobStore, String bucket) {
		super(fileProducer, relativePath);
		this.blobStore = blobStore;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		blob = blobStore.getBlob(bucket, pathAndName);
		setFullPath();
		setIsDirectory();
	}

	private void setFullPath() {
		if (name.isEmpty()) {
			fullPath = path;
		} else {
			fullPath = path + "/" + name;
		}
	}

	private void setIsDirectory() {
		if (blob == null) {
			isDirectory = blobStore.directoryExists(bucket, fullPath);
		} else {
			isDirectory = !blob.getMetadata().getType().equals(StorageType.BLOB);
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
		return blob != null ? blob.getMetadata().getContentMetadata().getContentLength() : 0;
	}

	@Override
	public long getLastModified() {
		if (isDirectory()) {
			return 0;
		} else {
			return blob.getMetadata().getLastModified().getTime();
		}
	}

	@Override
	public JFSFile[] getList() {
		if (list == null) {
			PageSet<? extends StorageMetadata> files = blobStore.list(bucket, ListContainerOptions.Builder.inDirectory(fullPath));

			if (files != null) {
				List<JFSFile> objects = new ArrayList<JFSFile>();

				for (StorageMetadata storageMetadata : files) {
					if (!storageMetadata.getName().equals(fullPath)) {
						String name = storageMetadata.getName().replaceFirst(fileProducer.getRootPath() + "/", "");
						objects.add(new JFSJCloudsFile(fileProducer, name, blobStore, bucket));
					}
				}
				if (objects.size() > 0) {
					list = objects.toArray(new JFSFile[objects.size()]);
				} else {
					list = new JFSLocalFile[0];
				}
			} else {
				list = new JFSLocalFile[0];
			}
		}

		return list;
	}

	@Override
	public boolean exists() {
		return blob != null;
	}

	@Override
	public boolean mkdir() {
		try {
			String directory = fullPath;
			blobStore.createDirectory(bucket, directory);
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
				blobStore.deleteDirectory(bucket, fullPath);
			} else {
				blobStore.removeBlob(bucket, fullPath);
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
			return blob.getPayload().getInput();
		}
	}

	@Override
	protected OutputStream getOutputStream() {
		return null;
	}

	@Override
	protected void closeInputStream() {
		try {
			blob.getPayload().getInput().close();
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
			String blobName = fullPath;
			BlobBuilder blobBuilder = blobStore.blobBuilder(blobName);
			blobBuilder.payload(file);
			blobStore.putBlob(bucket, blobBuilder.build());
			blob = blobStore.getBlob(bucket, blobName);
			setIsDirectory();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public String getMD5() {
		if(blob != null) {
			return blob.getMetadata().getETag().replaceAll("\"", "");
		} else {
			return "00000000";
		}
	}

}