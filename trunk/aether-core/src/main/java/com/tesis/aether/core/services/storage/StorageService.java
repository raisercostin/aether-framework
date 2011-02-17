package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.tesis.aether.core.exception.CopyFileException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public abstract class StorageService extends CloudService {

	// METADATA
	public StorageObjectMetadata getMetadataForObject(String remotePathFile) {

		String name = FilenameUtils.getName(remotePathFile);
		String path = FilenameUtils.getPathNoEndSeparator(remotePathFile);

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setType(StorageObjectConstants.FILE_TYPE);
		metadata.setPathAndName(path + "/" + name);

		try {
			metadata.setUri(getPublicURLForPath(remotePathFile));
		} catch (Exception e) {
		}

		try {
			metadata.setLength(sizeOf(remotePathFile));
		} catch (Exception e) {
		}

		try {
			metadata.setLastModified(lastModified(remotePathFile));
		} catch (Exception e) {
		}

		return metadata;
	}

	public abstract URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException;

	public abstract List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException;

	public abstract Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	public abstract Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;

	// BAJADA
	public abstract InputStream getInputStream(String remotePathFile) throws FileNotExistsException;

	public StorageObject getStorageObject(String remotePathFile) throws FileNotExistsException {
		StorageObject storageObject = new StorageObject();
		storageObject.setStream(getInputStream(remotePathFile));
		storageObject.setMetadata(getMetadataForObject(remotePathFile));
		
		return storageObject;
	}

	public void downloadToDirectory(String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException {
		if (checkFileExists(remotePathFile)) {
			downloadFileToDirectory(remotePathFile, localDirectory);
		} else if (checkDirectoryExists(remotePathFile)) {
			downloadDirectoryToDirectory(remotePathFile, localDirectory);
		}
	}

	public void downloadFileToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException {

		StorageObject storageObject = getStorageObject(remotePathFile);
		InputStream inputStream = storageObject.getStream();

		OutputStream out = null;

		try {
			out = new FileOutputStream(new File(localDirectory + "/" + storageObject.getMetadata().getName()));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {

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

	public void downloadDirectoryToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException {
		try {
			List<StorageObjectMetadata> listFiles = listFiles(remotePathFile, true);
			for (StorageObjectMetadata file : listFiles) {
				if (file.getType().equals(StorageObjectConstants.FILE_TYPE)) {
					String pathToDownload = localDirectory.getCanonicalPath() + FilenameUtils.getPath(file.getPath().replace(remotePathFile, ""));
					downloadFileToDirectory(file.getPathAndName(), new File(pathToDownload));
				}
			}
		} catch (IOException e) {
			throw new DownloadException("Unexpected error while trying to download file");
		}
	}

	// SUBIDA
	public void upload(File localPath, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		if (localPath.isDirectory()) {
			uploadDirectory(localPath, remoteDirectory);
		} else if (localPath.isFile()) {
			uploadSingleFile(localPath, remoteDirectory);
		}
	}

	public void uploadDirectory(File localDirectory, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		try {
			// TODO checkeo de directorio existente y si es directorio
			Collection<File> listFiles = FileUtils.listFiles(localDirectory, null, true);
			String basePath = localDirectory.getCanonicalPath();

			for (File file : listFiles) {
				if (file.isFile()) {
					String canonicalPath = file.getCanonicalPath();
					String pathToUpload = remoteDirectory + FilenameUtils.getPath(canonicalPath.replace(basePath, ""));

					uploadSingleFile(file, pathToUpload);
				}
			}
		} catch (IOException e) {
			throw new UploadException("Unexpected error while trying to upload files");
		}
	}

	public abstract void uploadSingleFile(File localFile, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;

	// SISTEMA DE ARCHIVOS
	public abstract void delete(String remotePathFile, boolean recursive) throws DeleteException, MethodNotSupportedException;

	public abstract void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException;

	public void copyFile(String from, String toDirectory) throws CopyFileException {

	}

	public void moveFile(String from, String toDirectory) throws MoveFileException {

	}

	public abstract boolean checkFileExists(String remotePath) throws MethodNotSupportedException;

	public abstract boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException;

	public boolean checkObjectExists(String remotePath) throws MethodNotSupportedException {
		if (checkFileExists(remotePath) || checkDirectoryExists(remotePath)) {
			return true;
		} else {
			return false;
		}
	}

	// INTERACCION CON OTROS SERVICIOS
	public void migrateData(String startingPath, StorageService target) throws FileNotExistsException {

	}

}
