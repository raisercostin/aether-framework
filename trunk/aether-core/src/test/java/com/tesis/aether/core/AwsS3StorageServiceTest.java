package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.imp.s3.AwsS3StorageService;


public class AwsS3StorageServiceTest extends StorageServiceTest {

	@Override
	protected ExtendedStorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.S3_ACCESS_KEY, "");
		properties.putProperty(StorageServiceConstants.S3_SECRET_KEY, "");

		ExtendedStorageService service = new AwsS3StorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

	@Override
	protected String getContainer() {
		return "dev-tests";
	}

}
