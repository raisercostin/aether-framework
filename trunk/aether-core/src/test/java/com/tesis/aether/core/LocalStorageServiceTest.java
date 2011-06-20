package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.imp.local.LocalStorageService;


public class LocalStorageServiceTest extends StorageServiceTest {

	@Override
	protected ExtendedStorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, "d:/REMOTE_MOCK/");
		
		ExtendedStorageService service = new LocalStorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

	@Override
	protected String getContainer() {
		return "TEST";
	}
}
