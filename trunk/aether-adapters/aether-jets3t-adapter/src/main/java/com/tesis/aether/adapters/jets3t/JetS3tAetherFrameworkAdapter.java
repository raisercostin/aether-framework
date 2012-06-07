package com.tesis.aether.adapters.jets3t;

import java.io.File;
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

	public S3Object getObject(String bucketName, String objectKey) throws Exception {
		try {

			if ((objectKey.endsWith("/") && service.checkDirectoryExists(bucketName, objectKey)) || (!objectKey.endsWith("/") && service.checkFileExists(bucketName, objectKey))) {
				StorageObjectMetadata storageObject = service.getMetadataForObject(bucketName, objectKey);
				S3Object object = new S3Object(objectKey);

				try {
					object.setDataInputStream(service.getInputStream(bucketName, objectKey));
				} catch (Exception e) {
					// TODO: handle exception
				}

				object.addAllMetadata(generateJetS3tMetadata(storageObject));
				object.setBucketName(bucketName);
				object.setStorageClass("STANDARD");
				object.setETag(storageObject.getMd5hash());
				return object;
			}
			throw new Exception();
		} catch (Exception e) {
			throw e;
		}
	}

	public StorageObject getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, Long byteRangeStart, Long byteRangeEnd, String versionId) throws Exception {
		return this.getObject(bucketName, objectKey);
	}

	public S3Object getObject(S3Bucket bucketName, String objectKey) throws Exception {
		return this.getObject(bucketName.getName(), objectKey);
	}

	public StorageObject getObjectDetails(String bucketName, String objectKey) throws ServiceException {
		StorageObjectMetadata storageObject = service.getMetadataForObject(bucketName, objectKey);
		if (storageObject.getLength() == null && storageObject.getMd5hash() == null) {
			throw new ServiceException("ResponseStatus: Not Found.");
		}
		StorageObject object = new StorageObject(objectKey);
		object.addAllMetadata(generateJetS3tMetadata(storageObject));
		object.setStorageClass("STANDARD");
		object.setETag(storageObject.getMd5hash());
		return object;
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
			return (StorageObject) this.putObject(bucketName, object);
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

	private String[] getCommonPrefixes(List<StorageObjectMetadata> files) {
		List<String> retElements = new ArrayList<String>();
		for (StorageObjectMetadata metadata : files) {
			if (!metadata.isFile()) {
				retElements.add(metadata.getPathAndName() + "/");
			}
		}
		return (String[]) retElements.toArray(new String[retElements.size()]);
	}
	
	public StorageObjectsChunk listObjectsInternal(String bucketName,
			String prefix, String delimiter, long maxListingLength,
			boolean automaticallyMergeChunks, String priorLastKey,
			String priorLastVersion) throws ServiceException {
		List<StorageObjectMetadata> listFiles;
		StorageObjectsChunk storageObjectsChunk;
		// se obtienen todos los elementos (archivos y directorios)
		try {
			if (prefix != null) {
				listFiles = service.listFiles(bucketName, prefix, false);
			} else {
				listFiles = service.listFiles(bucketName, "", false);
			}
			// Se obtienen los S3Objects (archivos)
			S3Object[] objects = aetherMetadataListToS3ObjectArray(listFiles,
					bucketName);

			//Se obtienen los common prefixed (directorios)
			String[] commonPrefixes = getCommonPrefixes(listFiles);

			storageObjectsChunk = new StorageObjectsChunk(
					prefix, delimiter, objects, commonPrefixes, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
		jets3metadata.put(BaseStorageItem.METADATA_HEADER_CONTENT_LENGTH, (metadata.getLength()!=null?metadata.getLength().toString():"0"));
		try {
			jets3metadata.put(BaseStorageItem.METADATA_HEADER_CONTENT_MD5, ServiceUtils.toBase64(ServiceUtils.fromHex(metadata.getMd5hash())));
		} catch (Exception e) {
		}
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
	
	private String getDirectory(String object) {
		String directory = "";
		try {
			if (object.endsWith("/"))
				return object;
			String aux = (object.startsWith("/") ? object.substring(1) : object);
			String[] st = aux.split("/");
			if (st.length > 1) {
				for (int i = 0; i < st.length - 1; i++) {
					directory += st[i] + "/";
				}
			}
		} catch (Exception e) {
			System.out.println(
					"Error al obtener el directorio de " + object
							+ "   -  Error: " + e.getMessage());
		}
		return directory;
	}

	private String getNameObject(String object) {
	String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		if (st.length > 0 && !st[st.length - 1].equals("*")) {
			if (st.length > 1) {
				return st[st.length -1];
			} else {
				return st[st.length - 1];
			}
		} else {
			return "";
		}
	}

	public Map<String, Object> copyObjectImpl(String bucketName,
			String objectKey, String bucketName2,
			String nameDestObject, AccessControlList acl,
			Map<String, Object> destinationMetadata, Calendar ifModifiedSince,
			Calendar ifUnmodifiedSince, String[] ifMatchTags,
			String[] ifNoneMatchTags, String versionId,
			String destinationObjectStorageClass) throws ServiceException {

		if (bucketName.equals(bucketName2)) {
			String dir1 = getDirectory(objectKey);
			String name1 = getNameObject(objectKey);
			String dir2 = getDirectory(nameDestObject);
			String name2 = getNameObject(nameDestObject);
			if (dir1.equals(dir2)) {
				if (name1.equals(name2))
					throw new S3ServiceException(
							"No se puede copiar un elemento sobre si mismo.");
				if (!"".equals(name1) && !"".equals(name2)) {
					try {
						// Se debe descargar, renombrar y volver asubir
						String fileName = System.currentTimeMillis() + name2;
						File f = File.createTempFile(fileName, "");
						f = new File((FilenameUtils.getFullPathNoEndSeparator(f
								.getCanonicalPath())));
						if (service.checkFileExists(bucketName, objectKey)) {
							System.out.println(">>> descargando " + objectKey);
							service.downloadFileToDirectory(bucketName,
									objectKey, f);
							File to = new File(f, name2);
							f = new File(f.getCanonicalPath() + "/" + objectKey);
							to.deleteOnExit();
							f.renameTo(to);
							System.out.println(">>> subiendo " + name2);
							service.upload(to, bucketName2, dir2);
							System.out
									.println(">>> Fin de subida " + objectKey);
							return (Map<String, Object>) getObjectDetails(
									bucketName2, nameDestObject)
									.getMetadataMap();
						} else {
							if (service.checkDirectoryExists(bucketName,
									objectKey)) {
								// por el momento queda sin hacer
							} else
								throw new S3ServiceException(
										"Error desconocido al copiar los objetos.");
						}
					} catch (Exception e) {
						throw new S3ServiceException(e);
					}
				} else {
					throw new S3ServiceException(
							"Error en los nombres de los archivos.");
				}
			}
		}
		try {
			service.copyFile(bucketName, objectKey, bucketName2, nameDestObject);
			return (Map<String, Object>) getObjectDetails(bucketName2,
					nameDestObject).getMetadataMap();
		} catch (Exception e) {
			throw new S3ServiceException(e);
		}
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
