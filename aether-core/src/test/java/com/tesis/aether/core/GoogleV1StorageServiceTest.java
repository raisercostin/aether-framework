package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.S3StorageService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;


public class GoogleV1StorageServiceTest extends StorageServiceTest {

	@Override
	protected ExtendedStorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, "d:/REMOTE_MOCK/");
		properties.putProperty(StorageServiceConstants.GOOGLE_STORAGE_ACCESS_KEY, "");
		properties.putProperty(StorageServiceConstants.GOOGLE_STORAGE_SECRET_KEY, "");
		properties.putProperty(StorageServiceConstants.GOOGLE_STORAGE_BUCKET, "");

		ExtendedStorageService service = new S3StorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

}
