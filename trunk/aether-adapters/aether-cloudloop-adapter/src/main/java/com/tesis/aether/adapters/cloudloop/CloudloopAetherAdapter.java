package com.tesis.aether.adapters.cloudloop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.cloudloop.TransferProgressObserver;
import com.cloudloop.adapter.AmazonCloudKey;
import com.cloudloop.encryption.EncryptionProvider;
import com.cloudloop.generated.CloudloopConfig;
import com.cloudloop.internal.util.PathUtil;
import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreDirectory;
import com.cloudloop.storage.CloudStoreFile;
import com.cloudloop.storage.CloudStoreObject;
import com.cloudloop.storage.CloudStoreObjectType;
import com.cloudloop.storage.CloudStorePath;
import com.cloudloop.storage.CloudStorePathNormalizer;
import com.cloudloop.storage.exceptions.InvalidPathException;
import com.cloudloop.storage.internal.CloudStoreObjectMetadata;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class CloudloopAetherAdapter extends AetherFrameworkAdapter {
	private static CloudloopAetherAdapter INSTANCE = null;
	private CloudStore serviceCloudStore = null;
	
	protected CloudloopAetherAdapter() {
		super();
	}

	public static CloudloopAetherAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CloudloopAetherAdapter();
		}
		return INSTANCE;
	}
	
	public void AmazonS3CloudStore(CloudStore cs){
		serviceCloudStore = cs;
	}

	public void initializeConnection(AmazonCloudKey key) {
	}

	public void createBucket(String bucketName) {
		try {
			service.createContainer(bucketName);
		} catch (CreateContainerException e) {
			e.printStackTrace();
		}
	}

	public boolean bucketExists(String bucketName) {
		return service.existsContainer(bucketName);
	}

	public CloudStorePathNormalizer getPathNormalizer() {
		return null;
	}

	public CloudStoreDirectory createDirectory(CloudStoreDirectory dir) {
		try {
				service.createFolder(dir.getPath().getAbsolutePath());
			} catch (FolderCreationException e) {
				e.printStackTrace();
			} catch (MethodNotSupportedException e) {
				e.printStackTrace();
			}
			return getCloudStoreDirectory(dir.getPath().getAbsolutePath());
	}

	public void removeDirectory(CloudStoreDirectory directory, boolean recursive) {
		try {
			service.deleteFolder(directory.getPath().getAbsolutePath());
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void moveDirectory(CloudStoreDirectory directory, CloudStoreDirectory newParent) {
		int i = 0;
	}

	public CloudStoreObject[] listDirectoryContents(CloudStoreDirectory directory, CloudStoreObjectType typeFilter, boolean recursive) {
		List<StorageObjectMetadata> listFiles;
		try {
			CloudStore parentStore = directory.getParentStore();
			listFiles = service.listFiles(directory.getPath().getAbsolutePath(), recursive);

			List<CloudStoreObject> items = new ArrayList<CloudStoreObject>();
			for (StorageObjectMetadata metadata : listFiles) {
				if (metadata.isFile()) {
					if (typeFilter.equals(CloudStoreObjectType.FILE) || typeFilter.equals(CloudStoreObjectType.OBJECT))
						items.add(generateCloudLoopObject(metadata, parentStore));
				} else {
					if (typeFilter.equals(CloudStoreObjectType.DIRECTORY) || typeFilter.equals(CloudStoreObjectType.OBJECT))
						items.add(generateCloudLoopObject(metadata, parentStore));
				}
			}

			return (CloudStoreObject[]) items.toArray(new CloudStoreObject[items.size()]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private CloudStoreObject generateCloudLoopObject(StorageObjectMetadata metadata, CloudStore cloudStore) {
		CloudStorePath cloudStorePath;// = new CloudStorePath(metadata.getPathAndName());
		if (metadata.isFile()) {
			cloudStorePath = new CloudStorePath(metadata.getPathAndName());
			CloudStoreFile file = new CloudStoreFile(cloudStore, cloudStorePath);
			CloudStoreObjectMetadata objectMetadata = new CloudStoreObjectMetadata();
			objectMetadata.setContentLengthInBytes(metadata.getLength()!=null?metadata.getLength().longValue():0);
			objectMetadata.setLastModifiedDate(metadata.getLastModified());
			objectMetadata.setETag(metadata.getMd5hash());
			file.setMetadata(objectMetadata);
			return file;
		} else {
			cloudStorePath = new CloudStorePath(metadata.getPathAndName() + "/");
			CloudStoreDirectory dir = new CloudStoreDirectory(cloudStore, cloudStorePath);
			CloudStoreObjectMetadata objectMetadata = new CloudStoreObjectMetadata();
			objectMetadata.setContentLengthInBytes(metadata.getLength()!=null?metadata.getLength().longValue():0);
			objectMetadata.setLastModifiedDate(metadata.getLastModified());
			objectMetadata.setETag(metadata.getMd5hash());
			dir.setMetadata(objectMetadata);
			return dir;
		}
	}

	public CloudStoreFile getFile(String path) {
		try {
			if ((path.endsWith("/") && service.checkDirectoryExists(path)) || (!path.endsWith("/") && service.checkFileExists(path))) {
				StorageObjectMetadata storageObject = service.getMetadataForObject(path);
				CloudStoreFile cloudStoreFile = (CloudStoreFile) generateCloudLoopObject(storageObject, serviceCloudStore);
				return cloudStoreFile; 
			}
			CloudStorePath cloudPath = new CloudStorePath(path);
			return new CloudStoreFile(serviceCloudStore, cloudPath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public CloudStoreDirectory getCloudStoreDirectory(String path) {
		CloudStorePath cloudPath = new CloudStorePath(path, true, PathUtil.ROOT_DIRECTORY);
		if (!cloudPath.isDirectory()) {
			throw new InvalidPathException("Path, " + cloudPath.getAbsolutePath() + " does not represent a directory location.");
		}

		try {
			if (path.endsWith("/") && service.checkDirectoryExists(path)) {
				StorageObjectMetadata storageObject = service.getMetadataForObject(path);
				CloudStoreDirectory cloudStoreDirectory = (CloudStoreDirectory) generateCloudLoopObject(storageObject, serviceCloudStore);
				return cloudStoreDirectory; 
			}
			return new CloudStoreDirectory(serviceCloudStore, cloudPath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void upload(CloudStoreFile file, TransferProgressObserver progressObserver) {
		try {
			service.uploadInputStream(file.getStreamToStore(), FilenameUtils.getFullPath(file.getPath().getAbsolutePath()), FilenameUtils.getName(file.getPath().getAbsolutePath()), file.getContentLengthInBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InputStream download(CloudStoreFile file, TransferProgressObserver progressObserver) {
		try {
			return service.getInputStream(file.getPath().getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean contains(CloudStoreObject object) {
		try {
			return service.checkObjectExists(object.getPath().getAbsolutePath());
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void removeFile(CloudStoreFile file) {
		try {
			service.deleteFile(file.getPath().getAbsolutePath());
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public CloudStoreObjectMetadata refreshMetaData(CloudStoreObject obj) {
		return obj.getMetadata();
	}

	public void refreshMetadata() {
	}
	public void writeMetadata(CloudStoreObject obj, CloudStoreObjectMetadata metadata) {
	}

	public boolean isEncrypted() {
		return false;
	}

	public EncryptionProvider getEncryptionProvider() {
		return null;
	}

	public void copyIntraStore(CloudStoreFile fromFile, CloudStoreFile toFile) {
	}

	public void copyIntraStore(CloudStoreFile fromFile, CloudStoreDirectory toDirectory) {
	}

	public void copyIntraStore(CloudStoreDirectory fromDir, CloudStoreDirectory toDirectory) {
	}
	
	public void moveFile(CloudStoreFile file, CloudStoreFile newFile) {
		int i = 0;
	}

	public void rename(CloudStoreDirectory directory, String newName) {
	}

	public void rename(CloudStoreFile file, String newName) {
	}

}
