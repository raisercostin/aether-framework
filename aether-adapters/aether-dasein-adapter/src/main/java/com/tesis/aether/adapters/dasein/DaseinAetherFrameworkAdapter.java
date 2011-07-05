package com.tesis.aether.adapters.dasein;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.encryption.Encryption;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class DaseinAetherFrameworkAdapter extends AetherFrameworkAdapter {
	private static DaseinAetherFrameworkAdapter INSTANCE = null;

	protected DaseinAetherFrameworkAdapter() {
		super();
	}

	public static DaseinAetherFrameworkAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DaseinAetherFrameworkAdapter();
		}
		return INSTANCE;
	}

	public FileTransfer download(CloudStoreObject sourceFile, File toFile) throws CloudException, InternalException {
		return this.download(sourceFile.getDirectory(), sourceFile.getName(), toFile, null);
	}

	public FileTransfer download(String directory, String fileName, File toFile, Encryption decryption) throws InternalException, CloudException {
		try {
			String path = FilenameUtils.getFullPath(toFile.getCanonicalPath());
			service.downloadFileToDirectory(directory, fileName, new File(path));

			FileTransfer fileTransfer = new FileTransfer();
			fileTransfer.setPercentComplete(1.0);
			fileTransfer.complete(null);

			return fileTransfer;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Iterable<CloudStoreObject> listFiles(String parentDirectory) throws CloudException, InternalException {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles(parentDirectory, "", true);

			List<CloudStoreObject> daseinMetadata = new ArrayList<CloudStoreObject>();
			for (StorageObjectMetadata metadata : listFiles) {
				daseinMetadata.add(generateDaseinMetadata(metadata));
			}

			return daseinMetadata;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private CloudStoreObject generateDaseinMetadata(StorageObjectMetadata metadata) {
		CloudStoreObject object = new CloudStoreObject();
		object.setCreationDate(metadata.getLastModified());
		if (metadata.getLength() != null) {
			object.setSize(metadata.getLength());
		} else {
			object.setSize(0);
		}
		object.setName(metadata.getPathAndName());
		// object.setLocation(metadata.getUri().toString());
		return object;
	}

	public void upload(File sourceFile, String directory, String fileName, boolean multiPart, Encryption encryption) throws CloudException, InternalException {
		try {

			String path = FilenameUtils.getPathNoEndSeparator(fileName);

			service.uploadSingleFile(sourceFile, directory, path);

		} catch (UploadException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		} catch (FileNotExistsException e) {
			e.printStackTrace();
		}
	}

	public void clear(String directory) throws CloudException, InternalException {

		try {
			service.delete(directory, "", true);
		} catch (DeleteException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}

	}

	public String createDirectory(String baseName, boolean findFreeName) throws InternalException, CloudException {
		try {
			service.createContainer(baseName);
			return baseName;
		} catch (CreateContainerException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}
	}

	public boolean exists(String directory) throws InternalException, CloudException {
		return service.existsContainer(directory);
	}

	public long exists(String directory, String fileName, boolean multiPart) throws InternalException, CloudException {
		try {
			if (service.checkObjectExists(directory, fileName)) {
				return 0L;
			} else {
				return -1L;
			}
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return -1L;
		}
	}

	public void moveFile(String fromDirectory, String fileName, String toDirectory) throws InternalException, CloudException {
		try {
			service.moveFile(fromDirectory, fileName, toDirectory, fileName);
		} catch (MoveFileException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}
	}

	public void removeDirectory(String directory) throws CloudException, InternalException {
		try {
			service.deleteContainer(directory);
		} catch (DeleteContainerException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}
	}

	public void removeFile(String directory, String name, boolean multipartFile) throws CloudException, InternalException {
		try {
			service.delete(directory, name, false);
		} catch (DeleteException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}
	}

	public void renameFile(String directory, String oldName, String newName) throws CloudException, InternalException {
		try {
			if (service.checkFileExists(directory, oldName)) {
				service.moveFile(directory, oldName, directory, newName);
			}
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		} catch (MoveFileException e) {
			e.printStackTrace();
			throw new InternalException(e.getMessage());
		}

	}

	/**
	 * UNIMPLEMENTED METHODS
	 */

	public long getMaxFileSizeInBytes() throws InternalException, CloudException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProviderTermForDirectory(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProviderTermForFile(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPublic(String bucket, String object) throws CloudException, InternalException {
		// TODO Auto-generated method stub
		return false;
	}

	public void makePublic(String directory) throws InternalException, CloudException {
		// TODO Auto-generated method stub

	}

	public void makePublic(String directory, String fileName) throws InternalException, CloudException {
		// TODO Auto-generated method stub

	}

	public String renameDirectory(String oldName, String newName, boolean findFreeName) throws CloudException, InternalException {
		// TODO Auto-generated method stub
		return null;
	}

}
