package com.tesis.aether.core.factory;

import java.util.Map;

import com.tesis.aether.core.exception.MissingConfigurationItemsException;
import com.tesis.aether.core.exception.ServiceCreationException;
import com.tesis.aether.core.exception.WrongServiceTypeException;
import com.tesis.aether.core.factory.builder.ServiceBuilder;
import com.tesis.aether.core.services.CloudServiceConstants;
import com.tesis.aether.core.services.storage.StorageService;

public class ServiceFactory {

	private Map<String,ServiceBuilder> serviceBuilders;

	//Collections2.filter(serviceBuilders.keySet(), new ServiceTypePredicate(CloudServiceConstants.STORAGE_KIND));
	
	public static ServiceFactory instance = new ServiceFactory();
	
	protected ServiceFactory() {			
	}
	
	public StorageService getStorageService(ServiceRequest request) throws MissingConfigurationItemsException, ServiceCreationException {
		if(request.size() == 1) {
			String serviceType = request.getServices().iterator().next();
			int accountNumber = request.getAccountsForService(serviceType).iterator().next();
			
			return getStorageService(serviceType, accountNumber);
			
		} /*else {
			MultiStorageService multiStorageService = new MultiStorageService();
			for(String serviceType: request.getServices()) {
				for(int accountNumber: request.getAccountsForService(serviceType)) {
					StorageService storageService = getStorageService(serviceType, accountNumber);
					multiStorageService.addStorageService(storageService);
				}				
			}						
			
			return multiStorageService;
		}*/ return null;
			
	}
	
	public StorageService getStorageService(String serviceKey, int accountNumber) throws MissingConfigurationItemsException, ServiceCreationException {
		
		try {
			ServiceBuilder serviceBuilder = getServiceBuilders().get(serviceKey);
			if(!serviceBuilder.getServiceKind().equals(CloudServiceConstants.STORAGE_KIND)) {
				throw new WrongServiceTypeException("Service " + serviceKey + " is not a storage service");
			}
			return (StorageService)serviceBuilder.createService(accountNumber);
		} catch (MissingConfigurationItemsException e) {
			throw e;
		} catch(Exception e) {
			e.printStackTrace();
			throw new ServiceCreationException("Unknown service creation error");
		}

	}
	
	public StorageService getStorageService(String serviceKey) throws MissingConfigurationItemsException, ServiceCreationException {
		return getStorageService(serviceKey, 1);
	}

	public void addServiceBuilder(String key, ServiceBuilder value) {
		this.serviceBuilders.put(key, value);
	}
	
	public void setServiceBuilders(Map<String,ServiceBuilder> serviceBuilders) {
		this.serviceBuilders = serviceBuilders;
	}

	public Map<String,ServiceBuilder> getServiceBuilders() {
		return serviceBuilders;
	}	
	
}