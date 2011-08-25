package com.tesis.aether.core.services.storage.imp.google.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

import com.google.common.base.Splitter;
import com.tesis.aether.core.auth.authenticator.Authenticator;
import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
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

public class GoogleV1StorageService extends ExtendedStorageService {

	private BlobStoreContext gsContext;
	private BlobStore blobStore;

	public GoogleV1StorageService() {
		super();
		setName(StorageServiceConstants.GOOGLE_STORAGE);
	}

	@Override
	public URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(container, sanitizedPath).getUri();
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		if (checkFileExists(container, sanitizedPath)) {
			BlobMetadata blobMetadata = blobStore.blobMetadata(container, sanitizedPath);
			return Arrays.asList(toStorageObjectMetadata(blobMetadata, container));
		} else {
			PageSet<? extends StorageMetadata> list = sanitizedPath.isEmpty() ? blobStore.list(container) : blobStore.list(container, ListContainerOptions.Builder.inDirectory(sanitizedPath));
			List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();
			for (StorageMetadata file : list) {
				if (!file.getName().isEmpty() && !file.getName().equals(sanitizedPath)) {
					StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(file, container);
					if (storageObjectMetadata.isDirectory() && recursive == true) {
						result.addAll(listFiles(container, storageObjectMetadata.getPathAndName(), true));
					}
					result.add(storageObjectMetadata);
				}
			}
			return result;
		}

	}

	@Override
	public Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(container, sanitizedPath).getContentMetadata().getContentLength();
	}

	@Override
	public Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(container, sanitizedPath).getLastModified();
	}

	@Override
	public InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);

		Blob blob = blobStore.getBlob(container, sanitizedPath);

		return blob.getPayload().getInput();
	}

	@Override
	public void uploadInputStream(InputStream stream, String container, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remoteDirectory);

		if (!sanitizedPath.trim().isEmpty() && !checkObjectExists(container, sanitizedPath)) {
			try {
				createFolder(container, sanitizedPath);
			} catch (FolderCreationException e) {
				throw new UploadException("Destination path could not be created.");
			}
		}

		String blobFullName = sanitizedPath.trim().isEmpty() ? filename : sanitizedPath + "/" + filename;
		Blob blob = blobStore.newBlob(blobFullName);
		blob.setPayload(stream);
		blob.getPayload().getContentMetadata().setContentLength(contentLength);
		blobStore.putBlob(container, blob);

		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void deleteFile(String container, String remotePathFile) {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		blobStore.removeBlob(container, sanitizedPath);
	}

	@Override
	public void deleteFolder(String container, String remotePath) {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		blobStore.deleteDirectory(container, sanitizedPath);
	}

	@Override
	public void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		Iterable<String> split = Splitter.on("/").split(sanitizedPath);

		StringBuilder accumulatedPath = new StringBuilder();

		for (String partialPath : split) {
			accumulatedPath.append(partialPath);

			try {
				blobStore.createDirectory(container, accumulatedPath.toString());
			} catch (Exception e) {
				throw new FolderCreationException(remotePath + " could not be created.");
			}

			accumulatedPath.append("/");
		}

	}

	@Override
	public boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		boolean blobExists = sanitizedPath.isEmpty() ? false : blobStore.blobExists(container, sanitizedPath);
		return blobExists;
	}

	@Override
	public boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		if (sanitizedPath.isEmpty()) {
			return true;
		} else {
			try {
				return blobStore.directoryExists(container, sanitizedPath);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
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

		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		BlobMetadata blobMetadata = blobStore.blobMetadata(container, sanitizedPath);
		
		try {
			metadata.setUri(blobMetadata.getUri());
		} catch (Exception e) {
		}

		try {
			metadata.setLength(blobMetadata.getContentMetadata().getContentLength());
		} catch (Exception e) {
		}

		try {
			metadata.setMd5hash(blobMetadata.getETag());
		} catch (Exception e) {
		}
		
		try {
			metadata.setLastModified(blobMetadata.getLastModified());
		} catch (Exception e) {
		}

		return metadata;
	}

	@Override
	public void disconnect() throws DisconnectionException, MethodNotSupportedException {
		gsContext.close();
		gsContext = null;
	}

	@Override
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {
		gsContext = new BlobStoreContextFactory().createContext("googlestorage",
				getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_ACCESS_KEY),
				getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_SECRET_KEY));
				blobStore = gsContext.getBlobStore();
	}

	@Override
	public void createContainer(String name) throws CreateContainerException {
		blobStore.createContainerInLocation(null, name);
	}

	@Override
	public void deleteContainer(String name) throws DeleteContainerException {
		blobStore.deleteContainer(name);
	}

	@Override
	public List<StorageObjectMetadata> listContainers() {
		PageSet<? extends StorageMetadata> list = blobStore.list();
		List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();
		for (StorageMetadata file : list) {
			if (!file.getName().isEmpty()) {
				StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(file, null);
				result.add(storageObjectMetadata);
			}
		}
		
		return result;
	}
	
	@Override
	public boolean existsContainer(String name) {
		return blobStore.containerExists(name);
	}

	private String sanitizeRemotePath(String... remoteDirectoryComponents) {

		StringBuffer remotePath = new StringBuffer();
		for (String component : remoteDirectoryComponents) {
			remotePath.append("/").append(component);
		}

		String withoutStartingSeparator = remotePath.delete(0, 1).toString();

		String separatorsToUnix = FilenameUtils.separatorsToUnix(withoutStartingSeparator);

		String normalizeNoEndSeparator = FilenameUtils.normalizeNoEndSeparator(separatorsToUnix, true);

		if (normalizeNoEndSeparator.startsWith("/")) {
			return normalizeNoEndSeparator.substring(1);
		} else {
			return normalizeNoEndSeparator;
		}
	}

	private StorageObjectMetadata toStorageObjectMetadata(StorageMetadata blobMetadata, String container) {
		String name = FilenameUtils.getName(blobMetadata.getName());
		String path = FilenameUtils.getPathNoEndSeparator(blobMetadata.getName());

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(blobMetadata.getLastModified());
		metadata.setMd5hash(blobMetadata.getETag());
		metadata.setContainer(container);
		
		if(!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}
		
		if (blobMetadata.getType().equals(StorageType.BLOB)) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			metadata.setLength(((MutableBlobMetadata) blobMetadata).getContentMetadata().getContentLength());
		} else if (blobMetadata.getType().equals(StorageType.CONTAINER)) {
			metadata.setType(StorageObjectConstants.CONTAINER_TYPE);
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		}

		return metadata;
	}
}
