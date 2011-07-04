package com.tesis.aether.adapters.jets3t;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.BaseStorageItem;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.StorageOwner;
import org.jets3t.service.utils.ServiceUtils;

import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class JetS3tAetherFrameworkAdapter extends AetherFrameworkAdapter {

	private static JetS3tAetherFrameworkAdapter INSTANCE = null;
	private static final String AWS_SIGNATURE_IDENTIFIER = "AWS";
	private static final String AWS_REST_HEADER_PREFIX = "x-amz-";
	private static final String AWS_REST_METADATA_PREFIX = "x-amz-meta-";

	protected JetS3tAetherFrameworkAdapter() {
		super();
	}
	
	public static JetS3tAetherFrameworkAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JetS3tAetherFrameworkAdapter();
		}
		return INSTANCE;
	}

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

	public StorageObject getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, Long byteRangeStart, Long byteRangeEnd, String versionId) throws ServiceException {
		return this.getObject(bucketName, objectKey);
	}

	public S3Object getObject(S3Bucket bucketName, String objectKey) {
		return this.getObject(bucketName.getName(), objectKey);
	}

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

	public StorageObject getObjectDetailsImpl(String bucketName, String objectKey, Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, String versionId) throws ServiceException {
		return getObjectDetails(bucketName, objectKey);
	}

	public StorageObject putObjectImpl(String bucketName, StorageObject object) throws ServiceException {
		try {
			return (S3Object) this.putObject(bucketName, object);
		} catch (ServiceException se) {
			throw new S3ServiceException(se);
		}
	}

	public S3Object putObject(String bucketName, S3Object object) throws S3ServiceException {
		try {
			return (S3Object) this.putObject(bucketName, (StorageObject) object);
		} catch (ServiceException se) {
			throw new S3ServiceException(se);
		}
	}

	public StorageObject putObject(String bucketName, StorageObject object) throws ServiceException {
		try {

			String name = FilenameUtils.getName(object.getName());
			String path = FilenameUtils.getPathNoEndSeparator(object.getName());

			if (object.getDataInputStream() != null) {
				service.uploadInputStream(object.getDataInputStream(), bucketName, path, name, object.getContentLength());
			}
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

	public void deleteObject(String bucket, String objectKey) throws S3ServiceException {
		try {
			service.delete(bucket, objectKey, true);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void deleteObjectImpl(String bucketName, String objectKey, String versionId, String multiFactorSerialNumber, String multiFactorAuthCode) throws ServiceException {
		this.deleteObject(bucketName, objectKey);
	}

	// AETHER NO SOPORTA DELIMITER
	public S3Object[] listObjects(String bucketName, String prefix, String delimiter, long maxListingLength) {
		try {
			return listObjects(bucketName, prefix, delimiter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// AETHER NO SOPORTA DELIMITER
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

	public StorageObject[] listObjectsImpl(String bucketName, String prefix, String delimiter, long maxListingLength) throws ServiceException {
		return this.listObjects(bucketName, prefix, delimiter);
	}

	public StorageObjectsChunk listObjectsInternal(String bucketName, String prefix, String delimiter, long maxListingLength, boolean automaticallyMergeChunks, String priorLastKey, String priorLastVersion) throws ServiceException {

		S3Object[] listObjects = listObjects(bucketName, prefix, delimiter, maxListingLength);

		StorageObjectsChunk storageObjectsChunk = new StorageObjectsChunk(prefix, delimiter, listObjects, new String[0], null);
		return storageObjectsChunk;
	}

	public StorageObjectsChunk listObjectsChunkedImpl(String bucketName, String prefix, String delimiter, long maxListingLength, String priorLastKey, boolean completeListing) throws ServiceException {
		return this.listObjectsInternal(bucketName, prefix, delimiter, maxListingLength, true, priorLastKey, null);
	}

	public void deleteBucket(String bucketName) throws ServiceException {
		try {
			service.deleteContainer(bucketName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteBucketImpl(String bucketName) throws ServiceException {
		this.deleteBucket(bucketName);
	}

	public S3Bucket createBucket(String bucketName) throws S3ServiceException {
		try {
			service.createContainer(bucketName);
			S3Bucket bucket = new S3Bucket();
			bucket.setName(bucketName);
			bucket.setOwner(new StorageOwner());
			return bucket;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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

	public StorageBucket createBucketImpl(String bucketName, String location, AccessControlList acl) throws ServiceException {
		return this.createBucket(bucketName);
	}

	public S3Bucket[] listAllBuckets() throws S3ServiceException {
		try {
			List<StorageObjectMetadata> listContainers = service.listContainers();
			return toS3Bucket(listContainers);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public StorageBucket[] listAllBucketsImpl() throws ServiceException {
		return this.listAllBuckets();
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

	public String getBucketLocationImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	public S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	public void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status) throws S3ServiceException {
	}

	public void setBucketPolicyImpl(String bucketName, String policyDocument) throws S3ServiceException {
	}

	public String getBucketPolicyImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	public void deleteBucketPolicyImpl(String bucketName) throws S3ServiceException {
	}

	public void setRequesterPaysBucketImpl(String bucketName, boolean requesterPays) throws S3ServiceException {
	}

	public boolean isRequesterPaysBucketImpl(String bucketName) throws S3ServiceException {
		return false;
	}

	public BaseVersionOrDeleteMarker[] listVersionedObjectsImpl(String bucketName, String prefix, String delimiter, String keyMarker, String versionMarker, long maxListingLength) throws S3ServiceException {
		return null;
	}

	public VersionOrDeleteMarkersChunk listVersionedObjectsChunkedImpl(String bucketName, String prefix, String delimiter, long maxListingLength, String priorLastKey, String priorLastVersion, boolean completeListing) throws S3ServiceException {
		return null;
	}

	public void updateBucketVersioningStatusImpl(String bucketName, boolean enabled, boolean multiFactorAuthDeleteEnabled, String multiFactorSerialNumber, String multiFactorAuthCode) throws S3ServiceException {
	}

	public S3BucketVersioningStatus getBucketVersioningStatusImpl(String bucketName) throws S3ServiceException {
		return null;
	}

	public boolean isTargettingGoogleStorageService() {
		return false;
	}
	
	public String getSignatureIdentifier() {
		return AWS_SIGNATURE_IDENTIFIER;
	}

	public String getRestHeaderPrefix() {
		return AWS_REST_HEADER_PREFIX;
	}

	public String getRestMetadataPrefix() {
		return AWS_REST_METADATA_PREFIX;
	}

	public boolean isBucketAccessible(String bucketName) throws ServiceException {
		return false;
	}

	public int checkBucketStatus(String bucketName) throws ServiceException {
		return 0;
	}

	public StorageOwner getAccountOwnerImpl() throws ServiceException {
		return null;
	}

	public Map<String, Object> copyObjectImpl(String sourceBucketName, String sourceObjectKey, String destinationBucketName, String destinationObjectKey, AccessControlList acl, Map<String, Object> destinationMetadata, Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags,
			String versionId, String destinationObjectStorageClass) throws ServiceException {
		return null;
	}

	public void putBucketAclImpl(String bucketName, AccessControlList acl) throws ServiceException {
	}

	public void putObjectAclImpl(String bucketName, String objectKey, AccessControlList acl, String versionId) throws ServiceException {
	}

	public AccessControlList getObjectAclImpl(String bucketName, String objectKey, String versionId) throws ServiceException {
		return null;
	}

	public AccessControlList getBucketAclImpl(String bucketName) throws ServiceException {
		return null;
	}

	public void shutdownImpl() throws ServiceException {
	}

}
