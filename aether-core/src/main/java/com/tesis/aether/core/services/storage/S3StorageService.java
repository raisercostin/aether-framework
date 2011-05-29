package com.tesis.aether.core.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.aws.s3.S3ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
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

public class S3StorageService extends ExtendedStorageService {

	private BlobStoreContext s3Context;
	private BlobStore blobStore;

	public S3StorageService() {
		super();
		setName(StorageServiceConstants.S3);
	}

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
			PageSet<? extends StorageMetadata> list = sanitizedPath.isEmpty() ? blobStore.list(getServiceProperty(StorageServiceConstants.S3_BUCKET)) : blobStore.list(getServiceProperty(StorageServiceConstants.S3_BUCKET), ListContainerOptions.Builder.inDirectory(sanitizedPath));
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
		blobStore.putBlob(getServiceProperty(StorageServiceConstants.S3_BUCKET), blob);

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

		boolean blobExists = sanitizedPath.isEmpty() ? false : blobStore.blobExists(getServiceProperty(StorageServiceConstants.S3_BUCKET), sanitizedPath);
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
		// s3Context = new BlobStoreContextFactory().createContext("s3",
		// getServiceProperty(StorageServiceConstants.S3_ACCESS_KEY),
		// getServiceProperty(StorageServiceConstants.S3_SECRET_KEY));
		// blobStore = s3Context.getBlobStore();

		Properties properties = new Properties();
		properties.put("jclouds.user-threads", "0");
		properties.put("jclouds.identity", getServiceProperty(StorageServiceConstants.S3_ACCESS_KEY));
		properties.put("jclouds.max-session-failures", "2");
		properties.put("jclouds.aws.default_regions", "us-standard");
		properties.put("jclouds.s3.service-path", "/");
		properties.put("jclouds.aws.header.tag", "amz");
		properties.put("jclouds.aws.auth.tag", "AWS");
		properties.put("jclouds.relax-hostname", "true");
		properties.put("jclouds.max-connection-reuse", "75");
		properties.put("jclouds.endpoint", "https://s3.amazonaws.com");
		properties.put("jclouds.credential", getServiceProperty(StorageServiceConstants.S3_SECRET_KEY));
		properties.put("jclouds.aws.regions", "us-standard,us-west-1,EU,ap-southeast-1");
		properties.put("jclouds.s3.virtual-host-buckets", "true");
		properties.put("jclouds.blobstore.metaprefix", "x-amz-meta-");
		properties.put("jclouds.endpoint.ap-southeast-1", "https://s3-ap-southeast-1.amazonaws.com");
		properties.put("jclouds.endpoint.EU", "https://s3-eu-west-1.amazonaws.com");
		properties.put("jclouds.max-connections-per_context", "20");
		properties.put("jclouds.so-timeout", "60000");
		properties.put("jclouds.max-connections-per-host", "0");
		properties.put("jclouds.endpoint.us-west-1", "https://s3-us-west-1.amazonaws.com");
		properties.put("jclouds.endpoint.us-standard", "https://s3.amazonaws.com");
		properties.put("jclouds.io-worker-threads", "20");
		properties.put("jclouds.blobstore.directorysuffix", "_$folder$");
		properties.put("jclouds.api-version", "2006-03-01");
		properties.put("jclouds.connection-timeout", "60000");
		properties.put("jclouds.provider", "s3");
		properties.put("jclouds.session-interval", "60");

		S3ContextBuilder builder = new S3ContextBuilder(properties);
		s3Context = builder.buildBlobStoreContext();
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
		metadata.setPathAndName(path + "/" + name);
		if (blobMetadata.getType().equals(StorageType.BLOB)) {
			metadata.setType(StorageObjectConstants.FILE_TYPE);
			metadata.setLength(((MutableBlobMetadata) blobMetadata).getContentMetadata().getContentLength());
		} else {
			metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);
		}

		return metadata;
	}

}
