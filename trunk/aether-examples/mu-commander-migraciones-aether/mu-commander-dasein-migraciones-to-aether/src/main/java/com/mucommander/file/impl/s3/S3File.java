/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.s3;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileAttributes;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileOperation;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.FileURL;
import com.mucommander.file.PermissionBits;
import com.mucommander.file.ProtocolFile;
import com.mucommander.file.UnsupportedFileOperation;
import com.mucommander.file.UnsupportedFileOperationException;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.runtime.JavaVersions;

/**
 * Super class of {@link S3Root}, {@link S3Bucket} and {@link S3Object}.
 * 
 * @author Maxence Bernard
 */
public abstract class S3File extends ProtocolFile {

	protected BlobStoreSupport service;
	// En el hash esta <NombreCarpeta, ListaArchivoos>
	protected static HashMap<String, ArrayList<CloudStoreObject>> files = null;
	protected AbstractFile parent;
	protected boolean parentSet;
	protected static boolean existBucket = true;

	protected S3File(FileURL url, BlobStoreSupport service) {
		super(url);

		this.service = service;
	}

	protected void loadFileList(String bucket) {

		try {
			files = new HashMap<String, ArrayList<CloudStoreObject>>();
			Iterable<CloudStoreObject> listFiles = service.listFiles(bucket);
			if (listFiles != null) {
				for (CloudStoreObject object : listFiles) {
					processObject(object, files);
				}
			}
			existBucket = true;
		} catch (Exception e) {
			e.printStackTrace();
			existBucket = false;
		}
	}
	
	protected String getDirectory(String object) {
		String directory = "";
		try {
			if (object.endsWith("/"))
				return object;
			String aux = (object.startsWith("/") ? object.substring(1) : object);
			String[] st = aux.split("/");
			if (st.length > 1) {
				for (int i = 0; i < st.length - 1; i++) {
					directory += st[i] + "/";
				}
			}
		} catch (Exception e) {
			Logger.getAnonymousLogger().warning(
					"Error al obtener el directorio de " + object
							+ "   -  Error: " + e.getMessage());
		}
		return directory;
	}


	protected CloudStoreObject getCloudStoreObject(String fullPath, String bucketName, boolean isDirectory) {
		String name;
		if (isDirectory)
			name = fullPath;
		else {
			name = getDirectory(fullPath);
			if (name.endsWith("/"))
				name = name.substring(0, name.length()-1);
		}
		ArrayList<CloudStoreObject> objs = files.get(name);

		if (objs == null) 
			return null;
		CloudStoreObject fileObject = null;
		for (CloudStoreObject cso : objs) {
			if (cso.getDirectory().equals(bucketName)) {
				if (isDirectory) {
					if (cso.getName().equals(getObjectKey(true, bucketName))) {
						fileObject = cso;
						break;
					}
				} else {
					if (cso.getName().equals(getObjectKey(false, bucketName))) {
						fileObject = cso;
						break;
					}
				}
			}
		}
		return fileObject;
	}
	
		protected String getObjectKey(String bucketName) {
			String urlPath = fileURL.getPath();
			// Strip out the bucket name from the path
			return urlPath.substring(bucketName.length() + 2, urlPath.length());
		}

		protected String getObjectKey(boolean wantTrailingSeparator, String bucketName) {
			String objectKey = getObjectKey(bucketName);
			return wantTrailingSeparator ? addTrailingSeparator(objectKey)
					: removeTrailingSeparator(objectKey);
		}


	//______________________________________________________________________
	
//	public HashMap<String, ArrayList<CloudStoreObject>> loadFileTree(String bucket) throws Exception {
//		HashMap<String, ArrayList<CloudStoreObject>> elements = new HashMap<String, ArrayList<CloudStoreObject>>();
//		Iterable<CloudStoreObject> listFiles = service.listFiles(bucket);
//		
//		for (CloudStoreObject object : listFiles) {
//			processObject(object.getName(), elements);
//		}
//		return elements;
//	}

	public void processObject(CloudStoreObject object, HashMap<String, ArrayList<CloudStoreObject>> tl) {
		String objectPath = object.getDirectory() + "/" + object.getName();
		String[] directories = getDirectories(objectPath);
		String name = getNameObject(objectPath);
		String actualDirectory = "";
		String previousDirectory = "";
		for (int i = 0; i < directories.length; i++){
			previousDirectory = actualDirectory;
			if (i != 0)
				actualDirectory += "/" + directories[i];
			else
				actualDirectory += directories[i];
			if (!tl.containsKey(actualDirectory)){
				CloudStoreObject cso = new CloudStoreObject();
				cso.setContainer(false);
				cso.setCreationDate(null);
				if (actualDirectory.startsWith("/"))
					actualDirectory = actualDirectory.substring(1);
				
				//cso.setDirectory(directories[0]);
				cso.setDirectory(object.getDirectory());
				
				String nameAux = actualDirectory.substring(directories[0].length()) + "/";
				if ("/".equals(nameAux))
					nameAux = "";
				else
					if (nameAux.startsWith("/"))
						nameAux = nameAux.substring(1);

				//cso.setLocation("http://" + directories[0] + ".s3.amazonaws.com/" + nameAux);
				cso.setLocation(object.getLocation());

				cso.setName(nameAux);
				cso.setProviderRegionId("us-east-1");
				ArrayList<CloudStoreObject> temp = new ArrayList<CloudStoreObject>();
				temp.add(cso);
				tl.put(actualDirectory, temp);

			
				if (!"".equals(previousDirectory)) {
					ArrayList<CloudStoreObject> prev = tl.get(previousDirectory);
					if (!existCloudStoreObject(cso, prev))
						prev.add(cso);
				}
			}
		}
		if (!name.equals("")) {
			tl.get(actualDirectory).add(object);
		}
	}

	private boolean existCloudStoreObject (CloudStoreObject o, ArrayList<CloudStoreObject> list) {
		for (CloudStoreObject obj : list) {
			if (o.getName().equals(obj.getName()))
				return true;
		}
		return false;
	}
	
	private String getNameObject(String object) {
		String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		if (st.length > 0 && !st[st.length - 1].equals("*")) {
			if (st.length > 1) {
				return st[st.length -1];
			} else {
				return st[st.length - 1];
			}
		} else {
			return "";
		}
	}

	private String[] getDirectories(String object) {
		String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		ArrayList<String> directories = new ArrayList<String>();
		if (st.length > 1) {
			for (int i = 1; i < st.length; i = i + 2) {
				directories.add(st[i-1]);
			}
		}
		return directories.toArray(new String[]{});
	}
	
	
	
	
	//______________________________________________________________________
	
	
	
	
	
	protected IOException getIOException(Exception e) throws IOException {
		return getIOException(e, fileURL);
	}

	protected static IOException getIOException(Exception e, FileURL fileURL)
			throws IOException {

		Throwable cause = e.getCause();
		if (cause instanceof IOException)
			return (IOException) cause;

		if (JavaVersions.JAVA_1_6.isCurrentOrHigher())
			return new IOException(e);

		return new IOException(e.getMessage());
	}

	protected AbstractFile[] listObjects(String bucketName, String prefix,
			S3File parent) throws IOException {
		if (files == null) {
			loadFileList(bucketName);
		}
		try {
			String path;
			if (prefix != null && !"".equals(prefix.trim()))
				path = bucketName + "/" + prefix;
			else
				path = bucketName;
			
			if (path.endsWith("/") && path.length()-1 > 0)
				path = path.substring(0, path.length()-1);
			
			
			ArrayList<AbstractFile> abstractFiles = new ArrayList<AbstractFile>();
			ArrayList<CloudStoreObject> objects = files.get(path);
			
//			if (!objects.isEmpty() && !prefix.equals("")) {
//				// This happens only when the directory does not exist
//				throw new IOException();
//			}

			FileURL childURL;
			if (objects != null) {
				for (CloudStoreObject o : objects) {
					if (!"".equals(o.getName()) && !(o.getName().equals(prefix) && o.getDirectory().equals(bucketName))) {//(o.getDirectory().equals(path) && "".equals(o.getName()))) {
						childURL = (FileURL) fileURL.clone();
						childURL.setPath(o.getDirectory() + "/" + o.getName());
						abstractFiles.add(FileFactory.getFile(childURL, parent, service, o));
					}
				}
			}
			return abstractFiles.toArray(new AbstractFile[]{});
		} catch (Exception e) {
			throw getIOException(e);
		}
	}
	
//	protected void processObject(CloudStoreObject object, HashMap<String, CloudStoreObject> elements) {
//		
//		String[] directories = getDirectories(object);
//		String name = getName(object.getName());
//		for (int i = 0; i < directories.length; i++){
//			if (!elements.containsKey(directories[i])) {
//				CloudStoreObject temp = new CloudStoreObject();
////				temp.setContainer(service.)
////				elements.put(directories[i], temp);
//			}
//		}
////		if (!name.equals("")) {
////			tl.addArchive(name);
////		}
//	}
//
//	private String getName(String object) {
//		String aux = object.replace("/", "/*/");
//		String[] st = aux.split("/");
//		if (st.length > 0 && !st[st.length - 1].equals("*")) {
//			if (st.length > 1) {
//				return st[st.length -1];
//			} else {
//				return st[st.length - 1];
//			}
//		} else {
//			return "";
//		}
//	}
//
//	private String[] getDirectories(CloudStoreObject object) {
//		String aux = object.getName().replace("/", "/*/");
//		String[] st = aux.split("/");
//		ArrayList<String> directories = new ArrayList<String>();
//		if (st.length > 1) {
//			for (int i = 1; i < st.length; i = i + 2) {
//				directories.add(st[i-1]);
//			}
//		}
//		return directories.toArray(new String[]{});
//	}
//	

	// ////////////////////
	// Abstract methods //
	// ////////////////////

	public abstract FileAttributes getFileAttributes();

	// ///////////////////////////////
	// ProtocolFile implementation //
	// ///////////////////////////////

	@Override
	public AbstractFile getParent() {
		if (!parentSet) {
			FileURL parentFileURL = this.fileURL.getParent();
			if (parentFileURL != null) {
				try {
					parent = FileFactory.getFile(parentFileURL, null, service);
				} catch (IOException e) {
					// No parent
				}
			}

			parentSet = true;
		}

		return parent;
	}

	@Override
	public void setParent(AbstractFile parent) {
		this.parent = parent;
		this.parentSet = true;
	}

	// Delegates to FileAttributes

	@Override
	public long getDate() {
		return getFileAttributes().getDate();
	}

	@Override
	public long getSize() {
		return getFileAttributes().getSize();
	}

	@Override
	public boolean exists() {
		return getFileAttributes().exists();
	}

	@Override
	public boolean isDirectory() {
		return getFileAttributes().isDirectory();
	}

	@Override
	public FilePermissions getPermissions() {
		return getFileAttributes().getPermissions();
	}

	@Override
	public Object getUnderlyingFileObject() {
		return getFileAttributes();
	}

	// Unsupported operations, no matter the kind of resource (object, bucket,
	// service)

	@Override
	public boolean isSymlink() {
		return false;
	}

	@Override
	public PermissionBits getChangeablePermissions() {
		return PermissionBits.EMPTY_PERMISSION_BITS;
	}

	@Override
	@UnsupportedFileOperation
	public void changePermission(int access, int permission, boolean enabled)
			throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(
				FileOperation.CHANGE_PERMISSION);
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public boolean canGetGroup() {
		return false;
	}

	@Override
	@UnsupportedFileOperation
	public OutputStream getAppendOutputStream()
			throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
	}

	@Override
	@UnsupportedFileOperation
	public RandomAccessOutputStream getRandomAccessOutputStream()
			throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(
				FileOperation.RANDOM_WRITE_FILE);
	}

	@Override
	@UnsupportedFileOperation
	public long getFreeSpace() throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(
				FileOperation.GET_FREE_SPACE);
	}

	@Override
	@UnsupportedFileOperation
	public long getTotalSpace() throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(
				FileOperation.GET_TOTAL_SPACE);
	}

	@Override
	@UnsupportedFileOperation
	public void changeDate(long lastModified)
			throws UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
	}
}
