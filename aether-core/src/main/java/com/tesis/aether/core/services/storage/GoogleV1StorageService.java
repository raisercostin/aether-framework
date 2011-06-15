package com.tesis.aether.core.services.storage;

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
import com.tesis.aether.core.exception.DisconnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
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
	public URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath).getUri();
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		if (checkFileExists(sanitizedPath)) {
			BlobMetadata blobMetadata = blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
			return Arrays.asList(toStorageObjectMetadata(blobMetadata));
		} else {
			PageSet<? extends StorageMetadata> list = sanitizedPath.isEmpty() ? blobStore.list(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET)) : blobStore.list(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), ListContainerOptions.Builder.inDirectory(sanitizedPath));
			List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();
			for (StorageMetadata file : list) {
				if (!file.getName().isEmpty()) {
					StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(file);
					if (storageObjectMetadata.isDirectory() && recursive == true) {
						result.addAll(listFiles(storageObjectMetadata.getPathAndName(), true));
					}
					result.add(storageObjectMetadata);
				}
			}
			return result;
		}

	}

	@Override
	public Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath).getContentMetadata().getContentLength();
	}

	@Override
	public Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath).getLastModified();
	}

	@Override
	public InputStream getInputStream(String remotePathFile) throws FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);

		Blob blob = blobStore.getBlob(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);

		return blob.getPayload().getInput();
	}

	@Override
	public void uploadInputStream(InputStream stream, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remoteDirectory);

		if (!sanitizedPath.trim().isEmpty() && !checkObjectExists(sanitizedPath)) {
			try {
				createFolder(sanitizedPath);
			} catch (FolderCreationException e) {
				throw new UploadException("Destination path could not be created.");
			}
		}

		String blobFullName = sanitizedPath.trim().isEmpty() ? filename : sanitizedPath + "/" + filename;
		Blob blob = blobStore.newBlob(blobFullName);
		blob.setPayload(stream);
		blob.getPayload().getContentMetadata().setContentLength(contentLength);
		blobStore.putBlob(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), blob);

		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void deleteFile(String remotePathFile) {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		blobStore.removeBlob(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
	}

	@Override
	public void deleteFolder(String remotePath) {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		blobStore.deleteDirectory(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
	}

	@Override
	public void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		Iterable<String> split = Splitter.on("/").split(sanitizedPath);

		StringBuilder accumulatedPath = new StringBuilder();

		for (String partialPath : split) {
			accumulatedPath.append(partialPath);

			try {
				blobStore.createDirectory(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), accumulatedPath.toString());
			} catch (Exception e) {
				throw new FolderCreationException(remotePath + " could not be created.");
			}

			accumulatedPath.append("/");
		}

	}

	@Override
	public boolean checkFileExists(String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		boolean blobExists = sanitizedPath.isEmpty() ? false : blobStore.blobExists(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
		return blobExists;
	}

	@Override
	public boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		boolean directoryExists = blobStore.directoryExists(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
		return directoryExists;
	}

	@Override
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {
		gsContext = new BlobStoreContextFactory().createContext("googlestorage",
				getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_ACCESS_KEY),
				getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_SECRET_KEY));
				blobStore = gsContext.getBlobStore();
	}
	
	@Override
	public StorageObjectMetadata getMetadataForObject(String remotePathFile) {

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
		BlobMetadata blobMetadata = blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET), sanitizedPath);
		
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

	private StorageObjectMetadata toStorageObjectMetadata(StorageMetadata blobMetadata) {
		String name = FilenameUtils.getName(blobMetadata.getName());
		String path = FilenameUtils.getPathNoEndSeparator(blobMetadata.getName());

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(blobMetadata.getLastModified());
		metadata.setMd5hash(blobMetadata.getETag());
		
		if(!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}
		
		if (blobMetadata.getType().equals(StorageType.BLOB)) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			metadata.setLength(((MutableBlobMetadata) blobMetadata).getContentMetadata().getContentLength());
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		}

		return metadata;
	}

}
