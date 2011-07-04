package com.tesis.aether.core.framework.adapter;

import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;

public abstract class AetherFrameworkAdapter {
	protected ExtendedStorageService service;
	
	protected AetherFrameworkAdapter() {
		initStorageService();
	}
	
	public void initStorageService() {
		service = ServiceFactory.instance.getFirstStorageService();
		try {
			service.connect(null);
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
	}
}
