package com.tesis.aether.core.services.storage.imp.local;

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
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DisconnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;
import com.tesis.aether.core.util.CodecUtil;

public class LocalStorageService extends ExtendedStorageService {

	public LocalStorageService() {
		super();
		setName(StorageServiceConstants.LOCAL);
	}

	@Override
	public URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		File remoteFile = initRemoteFile(container, remotePath);

		if (!checkObjectExists(container, remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}

		return remoteFile.toURI();
	}

	@Override
	public void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException {
		try {
			File remotePathFile = initRemoteFile(container, remotePath);
			FileUtils.forceMkdir(remotePathFile);
		} catch (IOException e) {
			throw new FolderCreationException(remotePath + " could not be created. The path might be invalid or blocked.");
		}
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException {
		File dirToList = initRemoteFile(container, remotePath);

		if (checkDirectoryExists(container, remotePath)) {
			List<StorageObjectMetadata> files = new ArrayList<StorageObjectMetadata>();
			for (String file : dirToList.list()) {
				StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(container, initRemoteFile(container, remotePath + "/" + file));
				if (storageObjectMetadata.isDirectory() && recursive == true) {
					files.addAll(listFiles(container, storageObjectMetadata.getPathAndName(), true));
				}
				files.add(storageObjectMetadata);
			}
			return files;
		} else if (checkFileExists(container, remotePath)) {
			return Arrays.asList(toStorageObjectMetadata(container, dirToList));
		} else {
			return new ArrayList<StorageObjectMetadata>();
		}
	}

	@Override
	public Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		if (!checkObjectExists(container, remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}
		File remoteFile = initRemoteFile(container, remotePath);
		return remoteFile.length();
	}

	@Override
	public Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		if (!checkObjectExists(container, remotePath)) {
			throw new FileNotExistsException("File " + remotePath + " does not exist");
		}
		File remoteFile = initRemoteFile(container, remotePath);
		return new Date(remoteFile.lastModified());
	}

	@Override
	public InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException {

		File remoteFile = initRemoteFile(container, remotePathFile);
		try {
			return new FileInputStream(remoteFile);
		} catch (FileNotFoundException e) {
			throw new FileNotExistsException("File " + remotePathFile + " does not exist");
		}
	}

	@Override
	public void uploadInputStream(InputStream inputStream, String container, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {

		File toFileFile = initRemoteFile(container, remoteDirectory + "/" + filename);

		OutputStream out = null;

		try {

			createFolder(container, remoteDirectory);

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
	public boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException {
		File dir = initRemoteFile(container, remotePath);
		if (dir.exists() && dir.isFile()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException {
		File dir = initRemoteFile(container, remotePath);
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

	public File initRemoteFile(String container, String path) {
		return new File(this.getServiceProperty(StorageServiceConstants.LOCAL_BASE_FOLDER) + "/" + container + "/" + path);
	}

	private StorageObjectMetadata toStorageObjectMetadata(String container, File dirToList) {
		File baseToIgnore = initRemoteFile(container,"");
		String pathAndFilename = dirToList.getPath().replace(baseToIgnore.getPath(), "");

		String path = FilenameUtils.separatorsToUnix(FilenameUtils.getPathNoEndSeparator(pathAndFilename));
		String name = dirToList.getName();

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(new Date(dirToList.lastModified()));
		metadata.setMd5hash(CodecUtil.getMd5FromFile(dirToList));
		metadata.setContainer(container);

		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}

		if (dirToList.isFile()) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			metadata.setLength(dirToList.length());
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		}

		return metadata;
	}

	@Override
	public void deleteFile(String container, String remotePath) throws DeleteException {
		File remotePathFile = initRemoteFile(container, remotePath);

		boolean success = remotePathFile.delete();
		if (!success) {
			throw new DeleteException(remotePath + " is blocked or not empty.");
		}

	}

	@Override
	public void deleteFolder(String container, String remotePath) throws DeleteException {
		File remotePathFile = initRemoteFile(container, remotePath);
		boolean success = remotePathFile.delete();
		if (!success) {
			throw new DeleteException(remotePath + " is blocked or not empty.");
		}

	}

	@Override
	public StorageObjectMetadata getMetadataForObject(String container, String remotePathFile) {

		String name = FilenameUtils.getName(remotePathFile);
		String path = FilenameUtils.getPathNoEndSeparator(remotePathFile);

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		if (remotePathFile.endsWith("/"))
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		else
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			
		metadata.setContainer(container);
		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}

		metadata.setMd5hash(CodecUtil.getMd5FromFile(initRemoteFile(container, remotePathFile)));

		try {
			metadata.setUri(getPublicURLForPath(container, remotePathFile));
		} catch (Exception e) {
		}

		try {
			metadata.setLength(sizeOf(container, remotePathFile));
		} catch (Exception e) {
		}

		try {
			metadata.setLastModified(lastModified(container, remotePathFile));
		} catch (Exception e) {
		}

		return metadata;
	}

	@Override
	public void createContainer(String name) throws CreateContainerException {
		File initRemoteFile = initRemoteFile(name, "");
		try {
			FileUtils.forceMkdir(initRemoteFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CreateContainerException(e.getMessage());
		}
	}

	@Override
	public void deleteContainer(String name) throws DeleteContainerException {
		File initRemoteFile = initRemoteFile(name, "");
		try {
			FileUtils.deleteDirectory(initRemoteFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DeleteContainerException(e.getMessage());
		}
	}

	@Override
	public List<StorageObjectMetadata> listContainers() {
		File initRemoteFile = initRemoteFile("", "");
		List<StorageObjectMetadata> metadatas = new ArrayList<StorageObjectMetadata>();
		
		String[] list = initRemoteFile.list();
		for (String fileLocation : list) {
			File file = initRemoteFile(fileLocation, "");
			if (file.isDirectory()) {
				StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(FilenameUtils.getName(fileLocation), file);
				storageObjectMetadata.setType(StorageObjectConstants.CONTAINER_TYPE);
				metadatas.add(storageObjectMetadata);
			}
		}
		
		return metadatas;
	}

	@Override
	public boolean existsContainer(String name) {
		return initRemoteFile(name, "").exists();
	}

}
