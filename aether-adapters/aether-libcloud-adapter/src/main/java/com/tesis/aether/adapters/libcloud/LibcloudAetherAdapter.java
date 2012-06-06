package com.tesis.aether.adapters.libcloud;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import simplecloud.storage.providers.amazon.S3Adapter;
import base.interfaces.IItem;
import base.types.Item;

import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class LibcloudAetherAdapter extends AetherFrameworkAdapter {
	private static LibcloudAetherAdapter INSTANCE = null;

	protected LibcloudAetherAdapter() {
		super();
	}

	public static LibcloudAetherAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LibcloudAetherAdapter();
		}
		return INSTANCE;
	}

	public IItem fetchItem(String path, Map<Object, Object> options) throws Exception {

		try {
			String container = (String) options.get(S3Adapter.Type.SRC_BUCKET);
			if ((path.endsWith("/") && service.checkDirectoryExists(container, path)) || (!path.endsWith("/") && service.checkFileExists(container, path))) {
				StorageObjectMetadata storageObject = service.getMetadataForObject(container, path);
				try {
					InputStream inputStream = service.getInputStream(container, path);
					Item item = new Item(inputStream, "application/xml", null, storageObject.getLength());
					return item;
				} catch (Exception e) {
					Item item = new Item(null, "application/xml", null, 0);
					return item;
				}
			}
			throw new Exception();
		} catch (FileNotExistsException e) {
			throw e;
		}
	}

	public boolean storeItem(String destinationPath, IItem item, Map<String, String> metadata, Map<Object, Object> options) {
		try {
			service.uploadInputStream(item.getContent(), (String) options.get(S3Adapter.Type.SRC_BUCKET), FilenameUtils.getFullPath(destinationPath), FilenameUtils.getName(destinationPath), item.getContentLength());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void deleteItem(String path, Map<Object, Object> options) {
		try {
			service.delete((String) options.get(S3Adapter.Type.SRC_BUCKET), path, false);
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public List<String> listItems(String path, Map<Object, Object> options) {
		List<StorageObjectMetadata> listFiles;
		try {
			listFiles = service.listFiles((String) options.get(S3Adapter.Type.SRC_BUCKET), path, true);

			List<String> items = new ArrayList<String>();
			for (StorageObjectMetadata metadata : listFiles) {
				if (metadata.isDirectory()) {
					items.add(metadata.getPathAndName() + "/");
				} else {
					items.add(metadata.getPathAndName());
				}
			}

			return items;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> fetchMetadata(String path, Map<Object, Object> options) {
		StorageObjectMetadata storageObject;
		storageObject = service.getMetadataForObject((String) options.get(S3Adapter.Type.SRC_BUCKET), path);
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("Last-Modified", storageObject.getLastModified().toString());
		metadata.put("ETag", storageObject.getMd5hash());
		if (storageObject.getLength() != null) 
			metadata.put("Content-Length", storageObject.getLength().toString());
		else
			metadata.put("Content-Length", "0");
		return metadata;
	}

	public void storeMetadata(String destinationPath, Map<String, String> metadata, Map<Object, Object> options) {
	}

	public void deleteMetadata(String path, Map<Object, Object> options) {
	}

	public void copyItem(String sourcePath, String destinationPath, Map<Object, Object> options) {
	}

	public void moveItem(String sourcePath, String destinationPath, Map<Object, Object> options) {
	}

	public void renameItem(String path, String name, Map<Object, Object> options) {
	}

}
