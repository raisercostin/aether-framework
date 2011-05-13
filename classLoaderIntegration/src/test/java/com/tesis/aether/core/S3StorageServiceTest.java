package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.S3StorageService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;


public class S3StorageServiceTest extends StorageServiceTest {

	@Override
	protected ExtendedStorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, "d:/REMOTE_MOCK/");
		properties.putProperty(StorageServiceConstants.S3_ACCESS_KEY, "");
		properties.putProperty(StorageServiceConstants.S3_SECRET_KEY, "");
		properties.putProperty(StorageServiceConstants.S3_BUCKET, "");

		ExtendedStorageService service = new S3StorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

}
