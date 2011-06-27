/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2010 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.impl.rest.httpclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.io.FilenameUtils;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseStorageItem;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.StorageOwner;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.ServiceUtils;

import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class RestS3Service extends S3Service {

	private static final String AWS_SIGNATURE_IDENTIFIER = "AWS";
	private static final String AWS_REST_HEADER_PREFIX = "x-amz-";
	private static final String AWS_REST_METADATA_PREFIX = "x-amz-meta-";
	private ExtendedStorageService service;

	public RestS3Service(ProviderCredentials credentials) throws S3ServiceException {
		this(credentials, null, null);
	}

	public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription, CredentialsProvider credentialsProvider) throws S3ServiceException {
		this(credentials, invokingApplicationDescription, credentialsProvider, Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME));
	}

	public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription, CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties) throws S3ServiceException {
		this(credentials, invokingApplicationDescription, credentialsProvider, jets3tProperties, new HostConfiguration());
	}

	public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription, CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties, HostConfiguration hostConfig) throws S3ServiceException {
		super(credentials, invokingApplicationDescription, credentialsProvider, jets3tProperties, hostConfig);
		initStorageService();
	}

	public void initStorageService() {
		service = ServiceFactory.instance.getFirstStorageService();
		try {
			service.connect(null);
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public S3Object getObject(String bucketName, String objectKey) {
		try {
			com.tesis.aether.core.services.storage.object.StorageObject storageObject = service.getStorageObject(bucketName, objectKey);
			S3Object object = new S3Object(objectKey);
			object.setDataInputStream(storageObject.getStream());
			object.addAllMetadata(generateJetS3tMetadata(storageObject.getMetadata()));
			object.setBucketName(bucketName);
			object.setStorageClass("STANDARD");
			object.setETag(storageObject.getMetadata().getMd5hash());
			return object;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public S3Object getObject(S3Bucket bucketName, String objectKey) {
		return this.getObject(bucketName.getName(), objectKey);
	}

	@Override
	public StorageObject getObjectDetails(String bucketName, String objectKey) throws ServiceException {
		try {
			com.tesis.aether.core.services.storage.object.StorageObject storageObject = service.getStorageObject(bucketName, objectKey);
			StorageObject object = new StorageObject(objectKey);
			object.addAllMetadata(generateJetS3tMetadata(storageObject.getMetadata()));
			object.setStorageClass("STANDARD");
			object.setETag(storageObject.getMetadata().getMd5hash());
			return object;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public S3Object getObjectDetails(S3Bucket bucket, String objectKey) throws S3ServiceException {
		try {
			com.tesis.aether.core.services.storage.object.StorageObject storageObject = service.getStorageObject(bucket.getName(), objectKey);
			S3Object object = new S3Object(objectKey);
			object.addAllMetadata(generateJetS3tMetadata(storageObject.getMetadata()));
			object.setBucketName(bucket.getName());
			object.setStorageClass("STANDARD");
			object.setETag(storageObject.getMetadata().getMd5hash());
			return object;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public S3Object putObject(String bucketName, S3Object object) throws S3ServiceException {
		try {
			return (S3Object) this.putObject(bucketName, (StorageObject) object);
		} catch (ServiceException se) {
			throw new S3ServiceException(se);
		}
	}

	@Override
	public StorageObject putObject(String bucketName, StorageObject object) throws ServiceException {
		try {

			String name = FilenameUtils.getName(object.getName());
			String path = FilenameUtils.getPathNoEndSeparator(object.getName());

			service.uploadInputStream(object.getDataInputStream(), bucketName, path, name, object.getContentLength());

			object.setLastModifiedDate(new Date());

			return object;
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

	public void deleteObject(S3Bucket bucket, String objectKey) throws S3ServiceException {
		try {
			service.delete(bucket.getName(), objectKey, true);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	// AETHER NO SOPORTA DELIMITER
	@Override
	public S3Object[] listObjects(String bucketName, String prefix, String delimiter, long maxListingLength) {
		try {
			return listObjects(bucketName, prefix, delimiter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// AETHER NO SOPORTA DELIMITER
	@Override
	public S3Object[] listObjects(String bucketName, String prefix, String delimiter) throws S3ServiceException {
		List<StorageObjectMetadata> listFiles;
		try {
			if (prefix != null) {
				listFiles = service.listFiles(bucketName, prefix, true);
			} else {
				listFiles = service.listFiles(bucketName, "", true);
			}
			return aetherMetadataListToS3ObjectArray(listFiles, bucketName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public S3Object[] listObjects(String bucketName) throws S3ServiceException {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles(bucketName, "", true);
			return aetherMetadataListToS3ObjectArray(listFiles, bucketName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected StorageObjectsChunk listObjectsInternal(String bucketName, String prefix, String delimiter, long maxListingLength, boolean automaticallyMergeChunks, String priorLastKey, String priorLastVersion) throws ServiceException {

		S3Object[] listObjects = listObjects(bucketName, prefix, delimiter, maxListingLength);

		StorageObjectsChunk storageObjectsChunk = new StorageObjectsChunk(prefix, delimiter, listObjects, new String[0], null);
		return storageObjectsChunk;
	}

	public void deleteBucket(String bucketName) throws ServiceException {
		try {
			service.deleteContainer(bucketName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public S3Bucket createBucket(S3Bucket bucket) throws S3ServiceException {
		try {
			service.createContainer(bucket.getName());
			return bucket;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public S3Bucket[] listAllBuckets() throws S3ServiceException {
		try {
			List<StorageObjectMetadata> listContainers = service.listContainers();
			return toS3Bucket(listContainers);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * UTIL
	 */
	private S3Bucket[] toS3Bucket(List<StorageObjectMetadata> listContainers) {
		S3Bucket[] buckets = new S3Bucket[listContainers.size()];
		for (int i = 0; i < listContainers.size(); i++) {
			S3Bucket bucket = new S3Bucket(listContainers.get(i).getName());
			bucket.setOwner(new StorageOwner());
			buckets[i] = bucket;
		}
		return buckets;
	}

	private Map<String, Object> generateJetS3tMetadata(StorageObjectMetadata metadata) {
		Map<String, Object> jets3metadata = new HashMap<String, Object>();
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_LAST_MODIFIED_DATE, metadata.getLastModified());
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_CONTENT_LENGTH, metadata.getLength().toString());
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_CONTENT_MD5, ServiceUtils.toBase64(ServiceUtils.fromHex(metadata.getMd5hash())));
		return jets3metadata;
	}

	private S3Object[] aetherMetadataListToS3ObjectArray(List<StorageObjectMetadata> listFiles, String bucketName) {
		List<S3Object> jCloudsMetadata = new ArrayList<S3Object>();
		for (StorageObjectMetadata metadata : listFiles) {
			if (metadata.isFile()) {
				S3Object object = new S3Object(metadata.getPathAndName());
				object.setBucketName(bucketName);
				object.setStorageClass("STANDARD");
				object.setETag(metadata.getMd5hash());
				object.addAllMetadata(generateJetS3tMetadata(metadata));
				jCloudsMetadata.add(object);
			}
		}

		return (S3Object[]) jCloudsMetadata.toArray(new S3Object[jCloudsMetadata.size()]);
	}

	/**
	 * UNIMPLEMENTED METHODS
	 */
	@Override
	protected String getBucketLocationImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	@Override
	protected S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	@Override
	protected void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status) throws S3ServiceException {
	}

	@Override
	protected void setBucketPolicyImpl(String bucketName, String policyDocument) throws S3ServiceException {
	}

	@Override
	protected String getBucketPolicyImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	@Override
	protected void deleteBucketPolicyImpl(String bucketName) throws S3ServiceException {
	}

	@Override
	protected void setRequesterPaysBucketImpl(String bucketName, boolean requesterPays) throws S3ServiceException {
	}

	@Override
	protected boolean isRequesterPaysBucketImpl(String bucketName) throws S3ServiceException {
		return false;
	}

	@Override
	protected BaseVersionOrDeleteMarker[] listVersionedObjectsImpl(String bucketName, String prefix, String delimiter, String keyMarker, String versionMarker, long maxListingLength) throws S3ServiceException {
		return null;
	}

	@Override
	protected VersionOrDeleteMarkersChunk listVersionedObjectsChunkedImpl(String bucketName, String prefix, String delimiter, long maxListingLength, String priorLastKey, String priorLastVersion, boolean completeListing) throws S3ServiceException {
		return null;
	}

	@Override
	protected void updateBucketVersioningStatusImpl(String bucketName, boolean enabled, boolean multiFactorAuthDeleteEnabled, String multiFactorSerialNumber, String multiFactorAuthCode) throws S3ServiceException {
	}

	@Override
	protected S3BucketVersioningStatus getBucketVersioningStatusImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	@Override
	protected boolean isTargettingGoogleStorageService() {
		return false;
	}

	@Override
	public String getEndpoint() {
		return this.jets3tProperties.getStringProperty("s3service.s3-endpoint", Constants.S3_DEFAULT_HOSTNAME);
	}

	@Override
	protected String getVirtualPath() {
		return this.jets3tProperties.getStringProperty("s3service.s3-endpoint-virtual-path", "");
	}

	@Override
	protected String getSignatureIdentifier() {
		return AWS_SIGNATURE_IDENTIFIER;
	}

	@Override
	public String getRestHeaderPrefix() {
		return AWS_REST_HEADER_PREFIX;
	}

	@Override
	public String getRestMetadataPrefix() {
		return AWS_REST_METADATA_PREFIX;
	}

	@Override
	protected int getHttpPort() {
		return this.jets3tProperties.getIntProperty("s3service.s3-endpoint-http-port", 80);
	}

	@Override
	protected int getHttpsPort() {
		return this.jets3tProperties.getIntProperty("s3service.s3-endpoint-https-port", 443);
	}

	@Override
	protected boolean getHttpsOnly() {
		return this.jets3tProperties.getBoolProperty("s3service.https-only", true);
	}

	@Override
	protected boolean getDisableDnsBuckets() {
		return this.jets3tProperties.getBoolProperty("s3service.disable-dns-buckets", false);
	}

	@Override
	protected boolean getEnableStorageClasses() {
		return this.jets3tProperties.getBoolProperty("s3service.enable-storage-classes", false);
	}

}
