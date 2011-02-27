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
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
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

public class S3StorageService extends ExtendedStorageService {

	private BlobStoreContext s3Context;
	private BlobStore blobStore;
	
	@Override
	public URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath).getUri();
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);
		
		if (checkFileExists(sanitizedPath)) {
			BlobMetadata blobMetadata = blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
			return Arrays.asList(toStorageObjectMetadata(blobMetadata));
		} else {
			PageSet<? extends StorageMetadata> list = blobStore.list(getServiceProperty(StorageServiceConstants.S3_BUCKET), ListContainerOptions.Builder.inDirectory(sanitizedPath));
			List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();
			for (StorageMetadata file : list) {
				StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(file);
				if(storageObjectMetadata.isDirectory() && recursive == true) {
					result.addAll(listFiles(storageObjectMetadata.getPathAndName(), true));
				}
				result.add(storageObjectMetadata);
			}
			return result;
		}
		
	}

	@Override
	public Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath).getContentMetadata().getContentLength();
	}

	@Override
	public Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		return blobStore.blobMetadata(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath).getLastModified();
	}

	@Override
	public InputStream getInputStream(String remotePathFile) throws FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);

		Blob blob = blobStore.getBlob(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);

		return blob.getPayload().getInput();
	}

	@Override
	public void uploadInputStream(InputStream stream, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remoteDirectory);
		
		if (!checkObjectExists(sanitizedPath)) {
			try {
				createFolder(sanitizedPath);
			} catch (FolderCreationException e) {
				throw new UploadException("Destination path could not be created.");
			}
		}
		
		Blob blob = blobStore.newBlob(sanitizedPath + "/" + filename);
		blob.setPayload(stream);
		blob.getPayload().getContentMetadata().setContentLength(contentLength);
		blobStore.putBlob(getServiceProperty(StorageServiceConstants.S3_BUCKET), blob);
		
		if(stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void deleteFile(String remotePathFile) {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		blobStore.removeBlob(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
	}

	@Override
	public void deleteFolder(String remotePath) {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		blobStore.deleteDirectory(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
	}
	
	@Override
	public void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		Iterable<String> split = Splitter.on("/").split(sanitizedPath);

		StringBuilder accumulatedPath = new StringBuilder();

		for (String partialPath : split) {
			accumulatedPath.append(partialPath);

			try {
				blobStore.createDirectory(getServiceProperty(StorageServiceConstants.S3_BUCKET), accumulatedPath.toString());
			} catch (Exception e) {
				throw new FolderCreationException(remotePath + " could not be created.");
			}

			accumulatedPath.append("/");
		}
		
	}

	@Override
	public boolean checkFileExists(String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		
		boolean blobExists = blobStore.blobExists(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
		return blobExists;
	}

	@Override
	public boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		
		boolean directoryExists = blobStore.directoryExists(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
		return directoryExists;
	}

	@Override
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {
		s3Context = new BlobStoreContextFactory().createContext("s3", getServiceProperty(StorageServiceConstants.S3_ACCESS_KEY), getServiceProperty(StorageServiceConstants.S3_SECRET_KEY));
		blobStore = s3Context.getBlobStore();
	}

	@Override
	public void disconnect() throws DisconnectionException, MethodNotSupportedException {
		s3Context.close();
		s3Context = null;
	}
		
	private String sanitizeRemotePath(String... remoteDirectoryComponents) {

		StringBuffer remotePath = new StringBuffer();
		for (String component : remoteDirectoryComponents) {
			remotePath.append("/").append(component);
		}

		String withoutStartingSeparator = remotePath.delete(0, 1).toString();

		String separatorsToUnix = FilenameUtils.separatorsToUnix(withoutStartingSeparator);

		return FilenameUtils.normalizeNoEndSeparator(separatorsToUnix, true);
	}

	private StorageObjectMetadata toStorageObjectMetadata(StorageMetadata blobMetadata) {
		String name = FilenameUtils.getName(blobMetadata.getName());
		String path = FilenameUtils.getPathNoEndSeparator(blobMetadata.getName());
		
		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(blobMetadata.getLastModified());
		metadata.setPathAndName(path + "/" + name);
		if(blobMetadata.getType().equals(StorageType.BLOB)) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			metadata.setLength(((MutableBlobMetadata)blobMetadata).getContentMetadata().getContentLength());
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);			
		}
		
		return metadata;
	}

}
