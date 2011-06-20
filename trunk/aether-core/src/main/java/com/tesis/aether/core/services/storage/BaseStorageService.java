package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.tesis.aether.core.exception.CopyFileException;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MigrationException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.CloudServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public abstract class BaseStorageService extends CloudService {

	public BaseStorageService() {
		super();
		setKind(CloudServiceConstants.STORAGE_KIND);
	}

	public abstract URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException;

	public abstract List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException;

	public abstract Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	public abstract Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	public abstract InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException;

	public abstract void uploadInputStream(InputStream stream, String container, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void deleteFile(String container, String remotePathFile) throws DeleteException;

	public abstract void deleteFolder(String container, String remotePath) throws DeleteException;

	public abstract void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException;

	public abstract boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException;

	public abstract boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException;

	public abstract StorageObjectMetadata getMetadataForObject(String container, String remotePathFile);

	public abstract StorageObject getStorageObject(String container, String remotePathFile) throws FileNotExistsException;

	public abstract void downloadToDirectory(String container, String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException;

	public abstract void downloadFileToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException;

	public abstract void downloadDirectoryToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException;

	public abstract void upload(File localPath, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void uploadDirectory(File localDirectory, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void uploadSingleFile(File localFile, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void delete(String container, String remotePathFile, boolean recursive) throws DeleteException;

	public abstract void migrateData(String container, String startingPath, ExtendedStorageService target, String targetContainer, String targetPath) throws MigrationException;

	public abstract boolean checkObjectExists(String container, String remotePath) throws MethodNotSupportedException;

	public abstract void moveFile(String fromContainer, String from, String toContainer, String toDirectory) throws MoveFileException;

	public abstract void copyFile(String fromContainer, String from, String toContainer, String toDirectory) throws CopyFileException;

	public abstract void delete(String container, StorageObjectMetadata file, boolean recursive) throws DeleteException;
	
	public abstract void createContainer(String name) throws CreateContainerException;

	public abstract void deleteContainer(String name) throws DeleteContainerException;
	
	public abstract List<StorageObjectMetadata> listContainers();

	public abstract boolean existsContainer(String name);
}