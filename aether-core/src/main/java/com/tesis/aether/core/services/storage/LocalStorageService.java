package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.tesis.aether.core.auth.authenticator.Authenticator;
import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DisconnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public class LocalStorageService extends ExtendedStorageService {

	public LocalStorageService() {
		super();
		setName(StorageServiceConstants.S3);
	}
	
	@Override
	public URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		File remoteFile = initRemoteFile(remotePath);

		if(!checkObjectExists(remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}

		return remoteFile.toURI();
	}

	@Override
	public void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException {
		try {
			File remotePathFile = initRemoteFile(remotePath);
			FileUtils.forceMkdir(remotePathFile);
		} catch (IOException e) {
			throw new FolderCreationException(remotePath + " could not be created. The path might be invalid or blocked.");
		}
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException {
		File dirToList = initRemoteFile(remotePath);

		if (checkDirectoryExists(remotePath)) {
			List<StorageObjectMetadata> files = new ArrayList<StorageObjectMetadata>();
			for (String file : dirToList.list()) {
				StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(initRemoteFile(remotePath + "/" + file));
				if (storageObjectMetadata.isDirectory() && recursive == true) {
					files.addAll(listFiles(storageObjectMetadata.getPathAndName(), true));
				}
				files.add(storageObjectMetadata);
			}
			return files;
		} else if (checkFileExists(remotePath)) {
			return Arrays.asList(toStorageObjectMetadata(dirToList));
		} else {
			return new ArrayList<StorageObjectMetadata>();
		}
	}

	@Override
	public Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		if(!checkObjectExists(remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}		
		File remoteFile = initRemoteFile(remotePath);
		return remoteFile.length();		
	}

	@Override
	public Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		if(!checkObjectExists(remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}		
		File remoteFile = initRemoteFile(remotePath);
		return new Date(remoteFile.lastModified());	
	}

	@Override
	public InputStream getInputStream(String remotePathFile) throws FileNotExistsException {
	
		File remoteFile = initRemoteFile(remotePathFile);
		try {
			return new FileInputStream(remoteFile);
		} catch (FileNotFoundException e) {
			throw new FileNotExistsException("File " + remotePathFile + " does not exist");
		}	
	}

	@Override
	public void uploadInputStream(InputStream inputStream, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		
		File toFileFile = initRemoteFile(remoteDirectory + "/" + filename);
		
		OutputStream out = null;

		try {
			
			createFolder(remoteDirectory);

			out = new FileOutputStream(toFileFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {
			throw new UploadException("Stream could not be uploaded to " + remoteDirectory + " with name " + filename);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
			}
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean checkFileExists(String remotePath) throws MethodNotSupportedException {
		File dir = initRemoteFile(remotePath);
		if (dir.exists() && dir.isFile()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException {
		File dir = initRemoteFile(remotePath);
		if (dir.exists() && dir.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {

	}

	@Override
	public void disconnect() throws DisconnectionException, MethodNotSupportedException {

	}

	public File initRemoteFile(String path) {
		String basePath = this.getServiceProperty(StorageServiceConstants.LOCAL_BASE_FOLDER);
		return new File(basePath + path);
	}

	private StorageObjectMetadata toStorageObjectMetadata(File dirToList) {
		File baseToIgnore = new File(this.getServiceProperty(StorageServiceConstants.LOCAL_BASE_FOLDER));
		String pathAndFilename = dirToList.getPath().replace(baseToIgnore.getPath(), "");

		String path = FilenameUtils.separatorsToUnix(FilenameUtils.getPathNoEndSeparator(pathAndFilename));
		String name = dirToList.getName();

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(new Date(dirToList.lastModified()));
		metadata.setPathAndName(path + "/" + name);
		if (dirToList.isFile()) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		}

		return metadata;
	}

	@Override
	public void deleteFile(String remotePath) throws DeleteException {
		File remotePathFile = initRemoteFile(remotePath);
		
		boolean success = remotePathFile.delete();
		if (!success) {
			throw new DeleteException(remotePath + " is blocked or not empty.");
		}
		
	}

	@Override
	public void deleteFolder(String remotePath) throws DeleteException {
		File remotePathFile = initRemoteFile(remotePath);
		
		boolean success = remotePathFile.delete();
		if (!success) {
			throw new DeleteException(remotePath + " is blocked or not empty.");
		}
		
	}

}
