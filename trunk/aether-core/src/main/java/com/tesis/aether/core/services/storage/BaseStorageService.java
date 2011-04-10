package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.tesis.aether.core.exception.CopyFileException;
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

	public abstract URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException;

	public abstract List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException;

	public abstract Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	public abstract Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	public abstract InputStream getInputStream(String remotePathFile) throws FileNotExistsException;

	public abstract void uploadInputStream(InputStream stream, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void deleteFile(String remotePathFile) throws DeleteException;

	public abstract void deleteFolder(String remotePath) throws DeleteException;

	public abstract void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException;

	public abstract boolean checkFileExists(String remotePath) throws MethodNotSupportedException;

	public abstract boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException;

	public abstract StorageObjectMetadata getMetadataForObject(String remotePathFile);

	public abstract StorageObject getStorageObject(String remotePathFile) throws FileNotExistsException;

	public abstract void downloadToDirectory(String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException;

	public abstract void downloadFileToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException;

	public abstract void downloadDirectoryToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException;

	public abstract void upload(File localPath, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void uploadDirectory(File localDirectory, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void uploadSingleFile(File localFile, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	public abstract void delete(String remotePathFile, boolean recursive) throws DeleteException;

	public abstract void migrateData(String startingPath, ExtendedStorageService target, String targetPath) throws MigrationException;

	public abstract boolean checkObjectExists(String remotePath) throws MethodNotSupportedException;

	public abstract void moveFile(String from, String toDirectory) throws MoveFileException;

	public abstract void copyFile(String from, String toDirectory) throws CopyFileException;

	public abstract void delete(StorageObjectMetadata file, boolean recursive) throws DeleteException;

}