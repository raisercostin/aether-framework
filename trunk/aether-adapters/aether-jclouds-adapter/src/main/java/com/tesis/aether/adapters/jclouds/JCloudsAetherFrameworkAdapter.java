package com.tesis.aether.adapters.jclouds;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.BlobBuilderImpl;
import org.jclouds.blobstore.domain.internal.BlobImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.domain.Location;
import org.jclouds.encryption.internal.JCECrypto;
import org.jclouds.rest.RestContextBuilder;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public class JCloudsAetherFrameworkAdapter extends AetherFrameworkAdapter implements BlobStore{
	private static JCloudsAetherFrameworkAdapter INSTANCE = null;

	protected JCloudsAetherFrameworkAdapter() {
		super();
	}

	public static JCloudsAetherFrameworkAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JCloudsAetherFrameworkAdapter();
		}
		return INSTANCE;
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
			service.delete(container, "/", true);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void clearContainer(String container, ListContainerOptions options) {
		clearContainer(container);
	}

	public boolean directoryExists(String container, String directory) {
		try {
			return service.checkDirectoryExists(container, directory);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void createDirectory(String container, String directory) {
		try {
			service.createFolder(container, directory);
		} catch (FolderCreationException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void deleteDirectory(String container, String name) {
		try {
			service.deleteFolder(container, name);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public boolean blobExists(String container, String name) {
		try {
			return service.checkFileExists(container, name);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public BlobBuilder blobBuilder(String name) {
		try {
			return new BlobBuilderImpl(new JCECrypto()).name(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String putBlob(String container, Blob blob, PutOptions options) {
		return putBlob(container, blob);
	}

	public String putBlob(String container, Blob blob) {
		try {

			String name = FilenameUtils.getName(blob.getMetadata().getName());
			String path = FilenameUtils.getPathNoEndSeparator(blob.getMetadata().getName());

			service.uploadInputStream(blob.getPayload().getInput(), container, path, name, blob.getPayload().getContentMetadata().getContentLength());

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
			StorageObject storageObject = service.getStorageObject(container, name);
			return generateJcloudsMetadata(storageObject.getMetadata());
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Blob getBlob(String container, String name) {
		try {
			StorageObject storageObject = service.getStorageObject(container, name);
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
			service.deleteFile(container, name);
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
		} else if(storageObjectMetadata.isContainer()) {
			mutableBlobMetadataImpl.setType(StorageType.CONTAINER);
		}
		mutableBlobMetadataImpl.getContentMetadata().setContentLength(storageObjectMetadata.getLength());
		mutableBlobMetadataImpl.setLastModified(storageObjectMetadata.getLastModified());

		return mutableBlobMetadataImpl;
	}

	public PageSet<? extends StorageMetadata> list() {
		List<StorageObjectMetadata> listContainers;
		try {
			listContainers = service.listContainers();

			List<MutableStorageMetadata> jCloudsMetadata = new ArrayList<MutableStorageMetadata>();
			for (StorageObjectMetadata metadata : listContainers) {
				jCloudsMetadata.add(generateJcloudsMetadata(metadata));
			}

			return new PageSetImpl<StorageMetadata>(jCloudsMetadata, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	public PageSet<? extends StorageMetadata> list(String container) {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles(container, "", false);

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

	public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions options) {
		List<StorageObjectMetadata> listFiles;
		try {
			String dir = options.getDir();
			if(dir != null) {
				listFiles = service.listFiles(container, dir, options.isRecursive());
			} else {
				listFiles = service.listFiles(container, "", options.isRecursive());			
			}

			List<MutableStorageMetadata> jCloudsMetadata = new ArrayList<MutableStorageMetadata>();
			if (!listFiles.isEmpty()) {
				for (StorageObjectMetadata metadata : listFiles) {
					jCloudsMetadata.add(generateJcloudsMetadata(metadata));
				}
			} else {
				StorageObjectMetadata metadata = new StorageObjectMetadata();
				if (dir != null)
					metadata.setPathAndName(dir);
				metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
				jCloudsMetadata.add(generateJcloudsMetadata(metadata));
			}

			return new PageSetImpl<StorageMetadata>(jCloudsMetadata, null);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean containerExists(String container) {
		return service.existsContainer(container);
	}

	public boolean createContainerInLocation(Location location, String container) {
		try {
			service.createContainer(container);
			return true;
		} catch (CreateContainerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void deleteContainer(String container) {
		try {
			service.deleteContainer(container);
		} catch (DeleteContainerException e) {
			e.printStackTrace();
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

	public Set<? extends Location> listAssignableLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean createContainerInLocation(Location location, String container, CreateContainerOptions options) {
		// TODO Auto-generated method stub
		return false;
	}

}
