package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.tesis.aether.core.exception.CopyFileException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MigrationException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public abstract class ExtendedStorageService extends BaseStorageService {

	// METADATA
	@Override
	public StorageObjectMetadata getMetadataForObject(String container, String remotePathFile) {

		String name = FilenameUtils.getName(remotePathFile);
		String path = FilenameUtils.getPathNoEndSeparator(remotePathFile);

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setType(StorageObjectConstants.FILE_TYPE);
		if(!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}

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
	public StorageObject getStorageObject(String container, String remotePathFile) throws FileNotExistsException {
		StorageObject storageObject = new StorageObject();
		try {
		storageObject.setStream(getInputStream(container, remotePathFile));
		} catch (Exception e) {
			//File is a directory
		}
		storageObject.setMetadata(getMetadataForObject(container, remotePathFile));

		return storageObject;
	}

	//DOWNLOAD
	@Override
	public void downloadToDirectory(String container, String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException {
		if (checkFileExists(container, remotePathFile)) {
			downloadFileToDirectory(container, remotePathFile, localDirectory);
		} else if (checkDirectoryExists(container, remotePathFile)) {
			downloadDirectoryToDirectory(container, remotePathFile, localDirectory);
		}
	}

	@Override
	public void downloadFileToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException {

		StorageObject storageObject = getStorageObject(container, remotePathFile);
		InputStream inputStream = storageObject.getStream();

		OutputStream out = null;

		try {

			if (!localDirectory.exists()) {
				FileUtils.forceMkdir(localDirectory);
			}

			out = new FileOutputStream(new File(localDirectory + "/" + storageObject.getMetadata().getName()));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public void downloadDirectoryToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException {
		try {
			List<StorageObjectMetadata> listFiles = listFiles(container, remotePathFile, true);
			String basePath = FilenameUtils.getFullPathNoEndSeparator(remotePathFile);
			for (StorageObjectMetadata file : listFiles) {
				if (file.getType().equals(StorageObjectConstants.FILE_TYPE)) {
					String pathToDownload = localDirectory.getCanonicalPath() + "/" + file.getPath().replace(basePath, "");
					downloadFileToDirectory(container, file.getPathAndName(), new File(pathToDownload));
				}
			}
		} catch (IOException e) {
			throw new DownloadException("Unexpected error while trying to download file");
		}
	}

	// SUBIDA
	@Override
	public void upload(File localPath, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		if (localPath.isDirectory()) {
			uploadDirectory(localPath, container, remoteDirectory);
		} else if (localPath.isFile()) {
			uploadSingleFile(localPath, container, remoteDirectory, localPath.getName());
		}
	}

	@Override
	public void uploadDirectory(File localDirectory, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		try {
			// TODO checkeo de directorio existente y si es directorio
			Collection<File> listFiles = FileUtils.listFiles(localDirectory, null, true);
			String basePath = FilenameUtils.getFullPathNoEndSeparator(localDirectory.getCanonicalPath());

			for (File file : listFiles) {
				if (file.isFile()) {
					String canonicalPath = file.getCanonicalPath();
					String pathToUpload = remoteDirectory + FilenameUtils.getPath(canonicalPath.replace(basePath, ""));

					uploadSingleFile(file, container, pathToUpload, file.getName());
				}
			}
		} catch (IOException e) {
			throw new UploadException("Unexpected error while trying to upload files");
		}
	}

	@Override
	public void uploadSingleFile(File localFile, String container, String remoteDirectory, String fileName) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		try {
			uploadInputStream(new FileInputStream(localFile), container, remoteDirectory, fileName, localFile.length());
		} catch (FileNotFoundException e) {
			throw new UploadException("The file you are trying to upload doesn't exist");
		} 
	}

	// SISTEMA DE ARCHIVOS
	@Override
	public void delete(String container, String remotePathFile, boolean recursive) throws DeleteException {
		try {

			if (checkFileExists(container, remotePathFile)) {
				deleteFile(container, remotePathFile);
			} else if (checkDirectoryExists(container, remotePathFile)) {
				List<StorageObjectMetadata> listFiles = listFiles(container, remotePathFile, false);
				
				if(recursive) {
					for(StorageObjectMetadata childrenFile: listFiles) {
						delete(container, childrenFile, true);
					}
				}
				
				boolean isEmpty = listFiles.size() == 0;			
				if (isEmpty || recursive) {
					deleteFolder(container, remotePathFile);
				} else {
					throw new DeleteException(remotePathFile + " is not empty and recursive deletion is disabled.");
				}
			}
		} catch (Exception e) {
			throw new DeleteException(remotePathFile + " could not be deleted.");
		}
	}

	@Override
	public void delete(String container, StorageObjectMetadata file, boolean recursive) throws DeleteException {
		try {
			if (file.isFile()) {
				deleteFile(container, file.getPathAndName());
			} else if (file.isDirectory()) {
				List<StorageObjectMetadata> listFiles = listFiles(container, file.getPathAndName(), false);
				
				if(recursive) {
					for(StorageObjectMetadata childrenFile: listFiles) {
						delete(container, childrenFile, true);
					}
				}
				
				boolean isEmpty = listFiles.size() == 0;			
				if (isEmpty || recursive) {
					deleteFolder(container, file.getPathAndName());
				} else {
					throw new DeleteException(file.getPathAndName() + " is not empty and recursive deletion is disabled.");
				}
			}
		} catch (Exception e) {
			throw new DeleteException(file.getPathAndName() + " could not be deleted.");
		}
	}

	@Override
	public void copyFile(String fromContainer, String from, String toContainer, String toDirectory) throws CopyFileException {
		try {
			List<StorageObjectMetadata> listFiles = listFiles(fromContainer, from, false);
			
			String finalDirectory = toDirectory + "/" + FilenameUtils.getName(from);

			createFolder(toContainer, finalDirectory);
			
			for(StorageObjectMetadata file: listFiles) {
				if(file.isFile()) {					
					InputStream stream = getInputStream(fromContainer, file.getPathAndName());
					uploadInputStream(stream, toContainer, finalDirectory, file.getName(), file.getLength());					
				} else if(file.isDirectory()) {
					copyFile(fromContainer, from + "/" + file.getName(), toContainer, finalDirectory);
				}
			}
		} catch (Exception e) {
			throw new CopyFileException(from + " could not be copied to " + toDirectory);
		}
	}

	@Override
	public void moveFile(String fromContainer, String from, String toContainer, String toDirectory) throws MoveFileException {
		try {
			copyFile(fromContainer, from, toContainer, toDirectory);
			delete(fromContainer, from, true);
		} catch (Exception e) {
			throw new MoveFileException("Error while moving file " + from);
		}
	}

	@Override
	public boolean checkObjectExists(String container, String remotePath) throws MethodNotSupportedException {
		if (checkFileExists(container, remotePath) || checkDirectoryExists(container, remotePath)) {
			return true;
		} else {
			return false;
		}
	}

	// INTERACCION CON OTROS SERVICIOS
	@Override
	public void migrateData(String container, String startingPath, ExtendedStorageService target, String targetContainer, String targetPath) throws MigrationException {
		try {
			List<StorageObjectMetadata> listFiles = listFiles(container, startingPath, false);
			
			String finalDirectory = targetPath + "/" + FilenameUtils.getName(startingPath);

			target.createFolder(targetContainer, finalDirectory);
			
			for(StorageObjectMetadata file: listFiles) {
				if(file.isFile()) {					
					InputStream stream = getInputStream(container, file.getPathAndName());
					target.uploadInputStream(stream, targetContainer, finalDirectory, file.getName(), file.getLength());					
				} else if(file.isDirectory()) {
					migrateData(container, startingPath + "/" + file.getName(), target, targetContainer, finalDirectory);
				}
			}
		} catch (Exception e) {
			throw new MigrationException("Error while migrating data");
		}
	}
	
}
