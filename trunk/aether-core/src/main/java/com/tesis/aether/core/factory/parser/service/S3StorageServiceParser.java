package com.tesis.aether.core.factory.parser.service;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.imp.s3.S3StorageService;

public class S3StorageServiceParser extends ServiceParser {

	@Override
	protected CloudService parse(ServiceAccountProperties properties) {
		ExtendedStorageService service = new S3StorageService();
		service.setServiceProperties(properties);
		return service;
	}

}

