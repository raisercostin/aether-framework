package jfs.sync.jets3t;

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
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

public class JFSJetS3tFile extends JFSFile {

	private String bucket;
	private boolean canRead = true;
	private boolean canWrite = true;
	private JFSFile[] list;
	private String name;
	private String path;
	private boolean isDirectory;
	private String fullPath;
	private RestS3Service adapter;
	private StorageObject itemData;

	//jets3t://testbuckettt/NewFolder/
	protected JFSJetS3tFile(JFSFileProducer fileProducer, String relativePath, RestS3Service adapter, String bucket) {
		super(fileProducer, relativePath);
		this.adapter = adapter;
		this.bucket = bucket;
		String pathAndName = FilenameUtils.separatorsToUnix(fileProducer.getRootPath() + relativePath).replaceAll("//", "/");
		this.name = FilenameUtils.getName(pathAndName);
		this.path = FilenameUtils.getFullPathNoEndSeparator(pathAndName);
		setFullPath();

		try {
			itemData = adapter.getObject(bucket, fullPath);
			isDirectory = false;
		} catch (S3ServiceException e) {
			try {
				itemData = adapter.getObject(bucket, fullPath + "/");
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
		return itemData != null ? itemData.getContentLength() : 0;
	}

	@Override
	public long getLastModified() {
		if (isDirectory()) {
			return 0;
		} else {
			return itemData.getLastModifiedDate().getTime();
		}
	}

	@Override
	public JFSFile[] getList() {
		try {
			if (list == null) {
				StorageObjectsChunk listObjectsChunked = adapter.listObjectsChunked(bucket, fullPath, "/", 50, null, true);
				StorageObject[] listItems = listObjectsChunked.getObjects();
				String[] listDirs = listObjectsChunked.getCommonPrefixes();
				if (listItems != null && listItems.length > 0) {
					List<JFSFile> objects = new ArrayList<JFSFile>();

					for(String dir: listDirs) {
						String name = dir.replaceFirst(fileProducer.getRootPath() + "/", "");
						if (name.endsWith("/")) {
							name = FilenameUtils.getFullPathNoEndSeparator(name);
						}
						objects.add(new JFSJetS3tFile(fileProducer, name, adapter, bucket));
					}
					
					for (StorageObject s3Object : listItems) {
						String file = s3Object.getName();
						if (file.startsWith(fullPath) && !file.equals(fullPath) && !file.endsWith("_$folder$")) {
							String name = file.replaceFirst(fileProducer.getRootPath() + "/", "");
							if (name.endsWith("/")) {
								name = FilenameUtils.getFullPathNoEndSeparator(name);
							}
							objects.add(new JFSJetS3tFile(fileProducer, name, adapter, bucket));
						}
					}

					if (objects.size() > 0) {
						list = objects.toArray(new JFSFile[objects.size()]);
					} else {
						list = new JFSJetS3tFile[0];
					}
				} else {
					list = new JFSJetS3tFile[0];
				}
			}
		} catch (Exception e) {
			list = new JFSJetS3tFile[0];
		}

		return list;
	}

	@Override
	public boolean exists() {
		return itemData != null;
	}

	@Override
	public boolean mkdir() {
		try {
			StorageObject object = new StorageObject(fullPath + "/", new byte[0]);
			itemData = adapter.putObject(bucket, object);
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
			adapter.deleteObject(bucket, fullPath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected InputStream getInputStream() {
		if (!isDirectory && itemData != null) {
			try {
				return itemData.getDataInputStream();
			} catch (ServiceException e) {
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
			itemData.getDataInputStream().close();
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
			StorageObject object = new StorageObject(file);
			object.setKey(fullPath);
			itemData = adapter.putObject(bucket, object);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getMD5() {
		if (itemData != null) {
			return itemData.getETag().replaceAll("\"", "");
		} else {
			return "00000000";
		}
	}

}