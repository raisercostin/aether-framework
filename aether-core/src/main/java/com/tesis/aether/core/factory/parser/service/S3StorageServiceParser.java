package com.tesis.aether.core.factory.parser.service;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.S3StorageService;

public class S3StorageServiceParser extends StorageServiceParser {

	@Override
	protected CloudService parse(ServiceAccountProperties properties) {
		ExtendedStorageService service = new S3StorageService();
		service.setServiceProperties(properties);
		return service;
	}

}

