package com.mucommander.aether.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.model.BaseStorageItem;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.StorageOwner;
import org.jets3t.service.utils.ServiceUtils;

import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;


public class AetherAdapter {
	private ExtendedStorageService	service = ServiceFactory.instance.getFirstStorageService();

	public AetherAdapter() {
		
	}

	public S3Object getObject(String bucketName, String objectKey) throws Exception {
		try {

			if ((objectKey.endsWith("/") && service.checkDirectoryExists(bucketName, objectKey)) || (!objectKey.endsWith("/") && service.checkFileExists(bucketName, objectKey))) {
				StorageObjectMetadata storageObject = service.getMetadataForObject(bucketName, objectKey);
				S3Object object = new S3Object(objectKey);

				try {
					object.setDataInputStream(service.getInputStream(bucketName, objectKey));
				} catch (Exception e) {
					
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

	public void deleteBucket(String bucketName) throws ServiceException {
		try {
			service.deleteContainer(bucketName);
		} catch (Exception e) {
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

	private String[] getCommonPrefixes(List<StorageObjectMetadata> files) {
		List<String> retElements = new ArrayList<String>();
		for (StorageObjectMetadata metadata : files) {
			if (!metadata.isFile()) {
				retElements.add(metadata.getPathAndName() + "/");
			}
		}
		return (String[]) retElements.toArray(new String[retElements.size()]);
	}
	
	public StorageObjectsChunk listObjectsChunked(String bucketName, String prefix, String delimiter, long maxListingLength, String priorLastKey, boolean completeListing) throws ServiceException {
		return this.listObjectsInternal(bucketName, prefix, delimiter, maxListingLength, true, priorLastKey, null);
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

	public Map<String, Object> copyObject(String bucketName, String objectKey,
			String bucketName2, S3Object destObject, boolean b) {
		return null;
	}

	public S3Bucket getBucket(String bucketName) {
		try {
			List<StorageObjectMetadata> listContainers = service.listContainers();
			for (StorageObjectMetadata som : listContainers) {
				if (som.getName().equals(bucketName)) {
					S3Bucket bucket = new S3Bucket(bucketName);
					bucket.setOwner(new StorageOwner());
					return bucket;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public StorageObject getObject(String bucketName, String objectKey,
			Object object, Object object2, Object object3, Object object4,
			Object offset, Object object5) throws Exception {
		return this.getObject(bucketName, objectKey);
	}
}
