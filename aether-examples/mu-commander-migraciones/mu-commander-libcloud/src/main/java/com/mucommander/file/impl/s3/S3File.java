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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import simplecloud.storage.interfaces.IStorageAdapter;
import simplecloud.storage.providers.nirvanix.NirvanixAdapter;

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

	protected IStorageAdapter service;
	// En el hash esta <NombreCarpeta, ListaArchivoos>
	protected static HashMap<String, ArrayList<Map<String, String>>> files = null;
	protected String bucketName = null;
	protected AbstractFile parent;
	protected boolean parentSet;
	protected Map<Object, Object> defaultOptions = null;

	protected S3File(FileURL url, IStorageAdapter service2, String bucketName) {
		super(url);
		this.service = service2;
		this.bucketName = bucketName;
		initializeOptions(bucketName);
	}

	protected void initializeOptions(String bucket) {
		if (defaultOptions == null)
			defaultOptions = new HashMap<Object, Object>();
		defaultOptions.put(NirvanixAdapter.Type.PAGE_NUMBER, "0");
		defaultOptions.put(NirvanixAdapter.Type.PAGE_SIZE, "1000");
	}

	// copia de dasein
	protected void loadFileList(String bucket) {
		if (defaultOptions == null)
			initializeOptions(bucket);

		try {
			files = new HashMap<String, ArrayList<Map<String, String>>>();

			List<String> listItems = service.listItems("/", defaultOptions);

			for (String blob : listItems) {
				processObject(blob, files);
				/*
				 * if (!blob.endsWith("/")) { File blobFile = new File(bucket +
				 * "/" + blob); Files.createParentDirs(blobFile);
				 * FileUtils.touch(blobFile); }
				 */
			}
			//			
			//			
			// Iterable<CloudStoreObject> listFiles = service.listFiles(bucket);
			// for (CloudStoreObject object : listFiles) {
			// processObject(object, files);
			// }

		} catch (Exception e) {
			e.printStackTrace();
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

	protected Map<String, String> getObjectMetadata(String fullPath,
			String bucketName, boolean isDirectory) {
		String name;
		if (isDirectory)
			name = fullPath;
		else {
			name = getDirectory(fullPath);
			if (name.endsWith("/"))
				name = name.substring(0, name.length() - 1);
		}
		if ("".equals(name))
			name = "/";
		ArrayList<Map<String, String>> objs = files.get(name);

		if (objs == null)
			return null;
		Map<String, String> fileObject = null;
		for (Map<String, String> cso : objs) {
			if (isDirectory) {
				if (cso.get(DefaultFileAttributes.PATH).equals("/"+
						getObjectKey(true, bucketName))) {
					fileObject = cso;
					break;
				}
			} else {
				if (cso.get(DefaultFileAttributes.PATH).equals("/"+
						getObjectKey(false, bucketName))) {
					fileObject = cso;
					break;
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

	protected String getObjectKey(boolean wantTrailingSeparator,
			String bucketName) {
		String objectKey = getObjectKey(bucketName);
		return wantTrailingSeparator ? addTrailingSeparator(objectKey)
				: removeTrailingSeparator(objectKey);
	}

	// ______________________________________________________________________

	// public HashMap<String, ArrayList<CloudStoreObject>> loadFileTree(String
	// bucket) throws Exception {
	// HashMap<String, ArrayList<CloudStoreObject>> elements = new
	// HashMap<String, ArrayList<CloudStoreObject>>();
	// Iterable<CloudStoreObject> listFiles = service.listFiles(bucket);
	//		
	// for (CloudStoreObject object : listFiles) {
	// processObject(object.getName(), elements);
	// }
	// return elements;
	// }

	public void processObject(String objectPath,
			HashMap<String, ArrayList<Map<String, String>>> tl) {

		String[] directories = getDirectories(objectPath);
		String name = getNameObject(objectPath);
		String actualDirectory = "/";
		String previousDirectory = "";
		// Se cargan todos los directorios intermedios
		for (int i = 0; (i < directories.length) || (i == 0 && directories.length == 0); i++) {
//			if (i != 0)
			previousDirectory = actualDirectory;

			if (directories.length > 0) {
				if (i == 0 )
					actualDirectory += directories[i];
				else
					actualDirectory += "/" + directories[i];
			}
				
//			else
//				actualDirectory += directories[i];
			if (!tl.containsKey(actualDirectory)) {
				Map<String, String> cso = new HashMap<String, String>();
				cso.put(DefaultFileAttributes.ISDIRECTORY, Boolean.TRUE.toString());
				cso.put(DefaultFileAttributes.CREATIONDATE, null);
//				if (actualDirectory.startsWith("/"))
//					actualDirectory = actualDirectory.substring(1);

				cso.put(DefaultFileAttributes.BUCKET, bucketName);

				String nameAux = ("/".equals(actualDirectory)?actualDirectory:actualDirectory + "/");
				if (!nameAux.startsWith("/"))
					nameAux = "/" + nameAux;
    			if (nameAux.endsWith("/") && nameAux.length()-1 > 0)
    				nameAux = nameAux.substring(0, nameAux.length()-1);
				// cso.setLocation("http://" + directories[0] +
				// ".s3.amazonaws.com/" + nameAux);
				// cso.setLocation(object.getLocation());

				cso.put(DefaultFileAttributes.PATH, nameAux);
				// cso.setProviderRegionId("us-east-1");
				ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
				//temp.add(cso);
				tl.put(actualDirectory, temp);

				if (!"".equals(previousDirectory)) {
					ArrayList<Map<String, String>> prev = tl
							.get(previousDirectory);
					if (!existCloudStoreObject(cso, prev))
						prev.add(cso);
				}
			}
		}
		if (!name.equals("")) {
			// agregar los atributos para el archivo
			Map<String, String> object = null;
			try {
				object = service.fetchMetadata("/" + objectPath, defaultOptions);
			} catch (Exception e) {
				Logger.getAnonymousLogger().warning("(1)Error al obtener la metadata del archivo: "	+ objectPath);
			}
			if (object != null) {
				object
						.put(DefaultFileAttributes.ISDIRECTORY, Boolean.FALSE
								.toString());
				object.put(DefaultFileAttributes.CREATIONDATE, null);
				object.put(DefaultFileAttributes.BUCKET, bucketName);
				object.put(DefaultFileAttributes.PATH, "/"+objectPath);

				tl.get(actualDirectory).add(object);
			} else {
				Logger.getAnonymousLogger().warning(
						"(2)Error al obtener la metadata del archivo: "
								+ objectPath);
			}
		}
	}

	private boolean existCloudStoreObject(Map<String, String> o,
			ArrayList<Map<String, String>> list) {
		for (Map<String, String> obj : list) {
			if (o.get(DefaultFileAttributes.PATH).equals(obj.get(DefaultFileAttributes.PATH)))
				return true;
		}
		return false;
	}

	private String getNameObject(String object) {
		String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		if (st.length > 0 && !st[st.length - 1].equals("*")) {
			if (st.length > 1) {
				return st[st.length - 1];
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
				directories.add(st[i - 1]);
			}
		}
		return directories.toArray(new String[] {});
	}

	// Fin copia de dasein

	// ______________________________________________________________________

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

	// protected static void handleAuthException(S3ServiceException e, FileURL
	// fileURL) throws AuthException {
	// int code = e.getResponseCode();
	// if(code==401 || code==403)
	// throw new AuthException(fileURL);
	// }

	protected AbstractFile[] listObjects(String bucketName, String prefix,
			S3File parent) throws IOException {
		if (files == null) {
			loadFileList(bucketName);
		}
		try {
			String path = prefix;
			if (prefix == null || "".equals(prefix.trim()))
				path = "/";

			if (path.endsWith("/") && path.length() - 1 > 0)
				path = path.substring(0, path.length() - 1);

			ArrayList<AbstractFile> abstractFiles = new ArrayList<AbstractFile>();
			ArrayList<Map<String, String>> objects = files.get(path);

			// se obtienen los archivos
			// HashMap<Object, Object> options = new HashMap<Object, Object>();
			// options.put(Type.SRC_BUCKET, bucketName);
			// List<String> files = service.listItems(path, options);

			FileURL childURL;
			if (objects != null) {
				for (Map<String, String> o : objects) {
					if (!o.get(DefaultFileAttributes.PATH).equals(prefix)) {
						childURL = (FileURL) fileURL.clone();
						childURL.setPath(o.get(DefaultFileAttributes.BUCKET) + o.get(DefaultFileAttributes.PATH));
						abstractFiles.add(FileFactory.getFile(childURL, parent,
								service, o));
					}
				}
			}
			return abstractFiles.toArray(new AbstractFile[] {});
		} catch (Exception e) {
			throw getIOException(e);
		}

		// ArrayList<CloudStoreObject> objects = files.get(path);
		//			
		// // if (!objects.isEmpty() && !prefix.equals("")) {
		// // // This happens only when the directory does not exist
		// // throw new IOException();
		// // }
		//
		// FileURL childURL;
		// if (objects != null) {
		// for (CloudStoreObject o : objects) {
		// if (!"".equals(o.getName()) && !(o.getName().equals(prefix) &&
		// o.getDirectory().equals(bucketName)))
		// {//(o.getDirectory().equals(path) && "".equals(o.getName()))) {
		// childURL = (FileURL) fileURL.clone();
		// childURL.setPath(o.getDirectory() + "/" + o.getName());
		// abstractFiles.add(FileFactory.getFile(childURL, parent, service, o));
		// }
		// }
		// }
		// return abstractFiles.toArray(new AbstractFile[]{});
		// } catch (Exception e) {
		// throw getIOException(e);
		// }
	}

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

	protected class DefaultFileAttributes {
		public static final String ISDIRECTORY = "Is-Directory";
		public static final String PATH = "File-Path";
		public static final String BUCKET = "Bucket";
		public static final String EXIST = "Exist";
		public static final String LASTMODIFIED = "Last-Modified";
		public static final String TYPE = "File-Type";
		public static final String DEFAULTFILETYPE = "application/octet-stream";
		public static final String CONTENTLENGTH = "Content-Length";
		public static final String CREATIONDATE = "Creation-Date";
	}

}
