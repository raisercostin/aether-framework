package com.tesis.aether.core.services.storage.imp.s3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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

public class AwsS3StorageService extends ExtendedStorageService {

	private AmazonS3Client s3Context;

	public AwsS3StorageService() {
		super();
		setName(StorageServiceConstants.S3);
	}

	@Override
	public void deleteFile(String container, String remotePathFile) {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		s3Context.deleteObject(new DeleteObjectRequest(container, sanitizedPath));
	}

	@Override
	public void deleteFolder(String container, String remotePath) {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		s3Context.deleteObject(new DeleteObjectRequest(container, sanitizedPath + "/"));
	}

	@Override
	public void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		Iterable<String> split = Splitter.on("/").split(sanitizedPath);

		StringBuilder accumulatedPath = new StringBuilder();

		InputStream input = new ByteArrayInputStream(new byte[0]);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);

		for (String partialPath : split) {
			accumulatedPath.append(partialPath);
			accumulatedPath.append("/");

			try {
				s3Context.putObject(new PutObjectRequest(container, accumulatedPath.toString(), input, metadata));
			} catch (Exception e) {
				throw new FolderCreationException(remotePath + " could not be created.");
			}

		}

	}

	@Override
	public boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		try {
			ObjectMetadata objectMetadata = s3Context.getObjectMetadata(container, sanitizedPath);
			if (objectMetadata != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException {
		String sanitizedPath = sanitizeRemotePath(remotePath);

		if (sanitizedPath.isEmpty()) {
			return true;
		} else {
			try {
				ObjectMetadata objectMetadata = s3Context.getObjectMetadata(container, sanitizedPath + "/");
				if (objectMetadata != null) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

	}

	@Override
	public List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException {

		String sanitizedPath = sanitizeRemotePath(remotePath);

		if (checkFileExists(container, sanitizedPath)) {
			return Arrays.asList(getMetadataForObject(container, sanitizedPath));
		} else {
			List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();

			ObjectListing listObjects;
			// Recursive
			if (recursive) {
				listObjects = s3Context.listObjects(container, sanitizedPath);
			} else {
				listObjects = s3Context.listObjects(new ListObjectsRequest(container, sanitizedPath + "/", null, "/", 2000));
			}

			List<S3ObjectSummary> objectSummaries = listObjects.getObjectSummaries();
			for (S3ObjectSummary s3ObjectSummary : objectSummaries) {
				if (!s3ObjectSummary.getKey().equals(sanitizedPath + "/")) {
					if (s3ObjectSummary.getKey().endsWith("/")) {
						// Folder
						result.add(toFolderStorageObjectMetadata(s3ObjectSummary.getKey(), container));
						
					} else {
						// File
						result.add(toStorageObjectMetadata(s3ObjectSummary));
					}
				}
			}

			for (String folder : listObjects.getCommonPrefixes()) {
				result.add(toFolderStorageObjectMetadata(folder, container));
			}

			return result;
		}
	}

	@Override
	public URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		try {
			return new URI(s3Context.getResourceUrl(container, sanitizedPath));
		} catch (Exception e) {
			throw new URLExtractionException(e.getMessage());
		}
	}

	@Override
	public Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		try {
			return s3Context.getObjectMetadata(container, sanitizedPath).getContentLength();
		} catch (Exception e) {
			throw new MetadataFetchingException(e.getMessage());
		}
	}

	@Override
	public Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePath);
		try {
			return s3Context.getObjectMetadata(container, sanitizedPath).getLastModified();
		} catch (Exception e) {
			throw new MetadataFetchingException(e.getMessage());
		}
	}

	@Override
	public InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException {
		String sanitizedPath = sanitizeRemotePath(remotePathFile);

		try {
			S3Object blob = s3Context.getObject(container, sanitizedPath);
			return blob.getObjectContent();
		} catch (Exception e) {
			throw new FileNotExistsException(e.getMessage());
		}
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
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentLength);
		try {
			s3Context.putObject(container, blobFullName, stream, metadata);
		} catch (Exception e) {
			throw new UploadException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(stream);
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
		metadata.setContainer(container);
		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}

		String sanitizedPath = sanitizeRemotePath(remotePathFile);
		ObjectMetadata blobMetadata = s3Context.getObjectMetadata(container, sanitizedPath);

		try {
			metadata.setUri(new URI(s3Context.getResourceUrl(container, sanitizedPath)));
		} catch (Exception e) {
		}

		try {
			metadata.setLength(blobMetadata.getContentLength());
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
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {
		s3Context = new AmazonS3Client(new BasicAWSCredentials(getServiceProperty(StorageServiceConstants.S3_ACCESS_KEY), getServiceProperty(StorageServiceConstants.S3_SECRET_KEY)));
	}

	@Override
	public void disconnect() throws DisconnectionException, MethodNotSupportedException {
		s3Context = null;
	}

	@Override
	public void createContainer(String name) throws CreateContainerException {
		try {
			s3Context.createBucket(name);
		} catch (Exception e) {
			throw new CreateContainerException(e.getMessage());
		}
	}

	@Override
	public void deleteContainer(String name) throws DeleteContainerException {
		s3Context.deleteBucket(name);
	}

	@Override
	public List<StorageObjectMetadata> listContainers() {
		List<Bucket> listBuckets = s3Context.listBuckets();
		List<StorageObjectMetadata> result = new ArrayList<StorageObjectMetadata>();

		for (Bucket bucket : listBuckets) {
			StorageObjectMetadata storageObjectMetadata = toStorageObjectMetadata(bucket);
			result.add(storageObjectMetadata);
		}

		return result;
	}

	private StorageObjectMetadata toStorageObjectMetadata(S3ObjectSummary s3ObjectSummary) {
		String name = FilenameUtils.getName(s3ObjectSummary.getKey());
		String path = FilenameUtils.getPathNoEndSeparator(s3ObjectSummary.getKey());

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(s3ObjectSummary.getLastModified());
		metadata.setMd5hash(s3ObjectSummary.getETag());
		metadata.setContainer(s3ObjectSummary.getBucketName());

		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}
		metadata.setType(StorageObjectConstants.FILE_TYPE);
		metadata.setLength(s3ObjectSummary.getSize());
		/*
		 * if (blobMetadata.getType().equals(StorageType.BLOB)) {
		 * metadata.setType(StorageObjectConstants.FILE_TYPE);
		 * metadata.setLength(((MutableBlobMetadata)
		 * blobMetadata).getContentMetadata().getContentLength()); } else if
		 * (blobMetadata.getType().equals(StorageType.CONTAINER)) {
		 * metadata.setType(StorageObjectConstants.CONTAINER_TYPE); } else {
		 * metadata.setType(StorageObjectConstants.DIRECTORY_TYPE); }
		 */

		return metadata;
	}

	private StorageObjectMetadata toFolderStorageObjectMetadata(String folder, String container) {

		folder = folder.substring(0, folder.length() - 1);

		String name = FilenameUtils.getName(folder);
		String path = FilenameUtils.getPathNoEndSeparator(folder);

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(null);
		metadata.setMd5hash("00000000");
		metadata.setContainer(container);

		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}
		metadata.setType(StorageObjectConstants.FILE_TYPE);
		metadata.setLength(0L);

		metadata.setType(StorageObjectConstants.DIRECTORY_TYPE);

		return metadata;
	}

	private StorageObjectMetadata toStorageObjectMetadata(Bucket bucket) {

		String name = FilenameUtils.getName(bucket.getName());
		String path = "";

		StorageObjectMetadata metadata = new StorageObjectMetadata();
		metadata.setPath(path);
		metadata.setName(name);
		metadata.setLastModified(bucket.getCreationDate());
		metadata.setMd5hash("00000000");
		metadata.setContainer(bucket.getName());

		if (!path.trim().isEmpty()) {
			metadata.setPathAndName(path + "/" + name);
		} else {
			metadata.setPathAndName(name);
		}

		metadata.setType(StorageObjectConstants.CONTAINER_TYPE);

		return metadata;
	}

	@Override
	public boolean existsContainer(String name) {
		return s3Context.doesBucketExist(name);
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

}
