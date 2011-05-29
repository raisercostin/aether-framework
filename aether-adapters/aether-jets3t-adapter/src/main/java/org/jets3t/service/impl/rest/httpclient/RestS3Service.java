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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseStorageItem;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.ProviderCredentials;

import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class RestS3Service extends S3Service {

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
			com.tesis.aether.core.services.storage.object.StorageObject storageObject = service.getStorageObject(objectKey);
			S3Object object = new S3Object(objectKey);
			object.setDataInputStream(storageObject.getStream());
			object.addAllMetadata(generateJetS3tMetadata(storageObject.getMetadata()));
			return object;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public StorageObject getObjectDetails(String bucketName, String objectKey) throws ServiceException {
		try {
			com.tesis.aether.core.services.storage.object.StorageObject storageObject = service.getStorageObject(objectKey);
			StorageObject object = new StorageObject(objectKey);
			object.addAllMetadata(generateJetS3tMetadata(storageObject.getMetadata()));
			return object;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}
	}

    public S3Object putObject(String bucketName, S3Object object) throws S3ServiceException {
        try {
            return (S3Object) this.putObject(bucketName, (StorageObject)object);
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }
    
	@Override
	public StorageObject putObject(String bucketName, StorageObject object) throws ServiceException {
		try {

			String name = FilenameUtils.getName(object.getName());
			String path = FilenameUtils.getPathNoEndSeparator(object.getName());

			service.uploadInputStream(object.getDataInputStream(), path, name, object.getContentLength());

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

	@Override
	public S3Object[] listObjects(String bucketName) throws S3ServiceException {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles("", true);
			return aetherMetadataListToS3ObjectArray(listFiles);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	//AETHER NO SOPORTA DELIMITER
	@Override
	public S3Object[] listObjects(String bucketName, String prefix, String delimiter, long maxListingLength) {
		try {
			return listObjects(bucketName, prefix, delimiter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//AETHER NO SOPORTA DELIMITER
	@Override
	public S3Object[] listObjects(String bucketName, String prefix, String delimiter) throws S3ServiceException {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles(prefix, true);
			return aetherMetadataListToS3ObjectArray(listFiles);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * UTIL
	 */
	private Map<String, Object> generateJetS3tMetadata(StorageObjectMetadata metadata) {
		Map<String, Object> jets3metadata = new HashMap<String, Object>();
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_LAST_MODIFIED_DATE, metadata.getLastModified());
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_CONTENT_LENGTH, metadata.getLength());
		return jets3metadata;
	}

	private S3Object[] aetherMetadataListToS3ObjectArray(List<StorageObjectMetadata> listFiles) {
		List<S3Object> jCloudsMetadata = new ArrayList<S3Object>();
		for (StorageObjectMetadata metadata : listFiles) {
			S3Object object = new S3Object(metadata.getPathAndName());
			object.addAllMetadata(generateJetS3tMetadata(metadata));
			jCloudsMetadata.add(object);
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
		return null;
	}

	@Override
	protected String getVirtualPath() {
		return null;
	}

	@Override
	protected String getSignatureIdentifier() {
		return null;
	}

	@Override
	public String getRestHeaderPrefix() {
		return null;
	}

	@Override
	public String getRestMetadataPrefix() {
		return null;
	}

	@Override
	protected int getHttpPort() {
		return 0;
	}

	@Override
	protected int getHttpsPort() {
		return 0;
	}

	@Override
	protected boolean getHttpsOnly() {
		return false;
	}

	@Override
	protected boolean getDisableDnsBuckets() {
		return false;
	}

	@Override
	protected boolean getEnableStorageClasses() {
		return false;
	}

}
