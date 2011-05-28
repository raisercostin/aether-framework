package com.tesis.aether.adapters.jclouds;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.BlobImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Location;

import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class BlobStoreAetherImpl implements BlobStore {

	// Aca ya se debe contar con el StorageService instanciado de Aether
	// Se debe convertir de los objetos internos a los de jclouds para que
	// el usuario no perciba cambio alguno

	private ExtendedStorageService service;

	public BlobStoreAetherImpl() {

		service = ServiceFactory.instance.getFirstStorageService();
		try {
			service.connect(null);
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public BlobStoreContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob newBlob(String name) {

		MutableBlobMetadataImpl mutableBlobMetadataImpl = new MutableBlobMetadataImpl();
		mutableBlobMetadataImpl.setName(name);
		mutableBlobMetadataImpl.setType(StorageType.BLOB);

		Blob blob = new BlobImpl(mutableBlobMetadataImpl);

		return blob;
	}

	public void clearContainer(String container) {
		try {
			service.delete("/", true);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void clearContainer(String container, ListContainerOptions options) {
		clearContainer(container);
	}

	public boolean directoryExists(String container, String directory) {
		try {
			return service.checkDirectoryExists(directory);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void createDirectory(String container, String directory) {
		try {
			service.createFolder(directory);
		} catch (FolderCreationException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void deleteDirectory(String containerName, String name) {
		try {
			service.deleteFolder(name);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public boolean blobExists(String container, String name) {
		try {
			return service.checkFileExists(name);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String putBlob(String container, Blob blob) {
		try {

			String name = FilenameUtils.getName(blob.getMetadata().getName());
			String path = FilenameUtils.getPathNoEndSeparator(blob.getMetadata().getName());

			service.uploadInputStream(blob.getPayload().getInput(), path, name, blob.getPayload().getContentMetadata().getContentLength());

			return null;
		} catch (UploadException e) {
			e.printStackTrace();
			return null;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return null;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public BlobMetadata blobMetadata(String container, String name) {
		try {
			StorageObject storageObject = service.getStorageObject(name);
			return generateJcloudsMetadata(storageObject.getMetadata());
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Blob getBlob(String container, String name) {
		try {
			StorageObject storageObject = service.getStorageObject(name);
			Blob blob = new BlobImpl(generateJcloudsMetadata(storageObject.getMetadata()));
			blob.setPayload(storageObject.getStream());

			return blob;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Blob getBlob(String container, String name, GetOptions options) {
		return getBlob(container, name);
	}

	public void removeBlob(String container, String name) {
		try {
			service.deleteFile(name);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	private MutableBlobMetadataImpl generateJcloudsMetadata(StorageObjectMetadata storageObjectMetadata) {
		MutableBlobMetadataImpl mutableBlobMetadataImpl = new MutableBlobMetadataImpl();
		mutableBlobMetadataImpl.setName(storageObjectMetadata.getPathAndName());
		if(storageObjectMetadata.isFile()) {
			mutableBlobMetadataImpl.setType(StorageType.BLOB);
		} else if(storageObjectMetadata.isDirectory()) {
			mutableBlobMetadataImpl.setType(StorageType.FOLDER);
		}
		mutableBlobMetadataImpl.getContentMetadata().setContentLength(storageObjectMetadata.getLength());
		mutableBlobMetadataImpl.setLastModified(storageObjectMetadata.getLastModified());

		return mutableBlobMetadataImpl;
	}

	public PageSet<? extends StorageMetadata> list() {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles("", true);

			List<MutableStorageMetadata> jCloudsMetadata = new ArrayList<MutableStorageMetadata>();
			for (StorageObjectMetadata metadata : listFiles) {
				jCloudsMetadata.add(generateJcloudsMetadata(metadata));
			}

			return new PageSetImpl<StorageMetadata>(jCloudsMetadata, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	public PageSet<? extends StorageMetadata> list(String container) {
		return list();
	}

	public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions options) {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles(options.getDir(), options.isRecursive());

			List<MutableStorageMetadata> jCloudsMetadata = new ArrayList<MutableStorageMetadata>();
			for (StorageObjectMetadata metadata : listFiles) {
				jCloudsMetadata.add(generateJcloudsMetadata(metadata));
			}

			return new PageSetImpl<StorageMetadata>(jCloudsMetadata, null);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * NOT IMPLEMENTED METHODS
	 */
	public long countBlobs(String container) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long countBlobs(String container, ListContainerOptions options) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean containerExists(String container) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean createContainerInLocation(Location location, String container) {
		// TODO Auto-generated method stub
		return false;
	}

	public void deleteContainer(String container) {
		// TODO Auto-generated method stub

	}

	public Set<? extends Location> listAssignableLocations() {
		// TODO Auto-generated method stub
		return null;
	}

}
