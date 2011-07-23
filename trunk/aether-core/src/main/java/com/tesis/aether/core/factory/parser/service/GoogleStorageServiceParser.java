package com.tesis.aether.core.factory.parser.service;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.imp.google.storage.GoogleV1StorageService;

public class GoogleStorageServiceParser extends ServiceParser {

	@Override
	protected CloudService parse(ServiceAccountProperties properties) {
		ExtendedStorageService service = new GoogleV1StorageService();
		service.setServiceProperties(properties);
		return service;
	}

}

