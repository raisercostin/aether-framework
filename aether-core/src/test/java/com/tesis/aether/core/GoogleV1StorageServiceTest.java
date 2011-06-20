package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.imp.google.storage.GoogleV1StorageService;
import com.tesis.aether.core.services.storage.imp.s3.S3StorageService;


public class GoogleV1StorageServiceTest extends StorageServiceTest {

	@Override
	protected ExtendedStorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.GOOGLE_STORAGE_ACCESS_KEY, "");
		properties.putProperty(StorageServiceConstants.GOOGLE_STORAGE_SECRET_KEY, "");

		ExtendedStorageService service = new GoogleV1StorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

	@Override
	protected String getContainer() {
		return "";
	}
}
