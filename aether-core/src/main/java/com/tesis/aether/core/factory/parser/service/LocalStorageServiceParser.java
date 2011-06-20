package com.tesis.aether.core.factory.parser.service;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.imp.local.LocalStorageService;

public class LocalStorageServiceParser extends StorageServiceParser{

	@Override
	protected CloudService parse(ServiceAccountProperties properties) {
		ExtendedStorageService service = new LocalStorageService();
		service.setServiceProperties(properties);
		return service;
	}

}

