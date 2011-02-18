package com.tesis.aether.core;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.LocalStorageService;
import com.tesis.aether.core.services.storage.StorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;


public class LocalStorageServiceTest extends StorageServiceTest {

	@Override
	protected StorageService getStorageService() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, "d:/REMOTE_MOCK/");
		
		StorageService service = new LocalStorageService();
		service.setServiceProperties(properties);
		
		return service;
	}

}
