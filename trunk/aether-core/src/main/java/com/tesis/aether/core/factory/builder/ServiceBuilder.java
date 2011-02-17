package com.tesis.aether.core.factory.builder;

import java.util.HashMap;
import java.util.Map;

import com.tesis.aether.core.exception.MissingConfigurationItemsException;
import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.factory.parser.AccountXmlParser;
import com.tesis.aether.core.services.CloudService;

public abstract class ServiceBuilder {

	private String serviceKind;
	private String serviceName;
	private Map<String, ServiceAccountProperties> cachedServiceProperties = new HashMap<String, ServiceAccountProperties>();
	
	protected abstract CloudService createService(ServiceAccountProperties serviceProperties);
	
	protected abstract boolean hasNecesaryData(ServiceAccountProperties serviceProperties) throws MissingConfigurationItemsException;
	
	public CloudService createService(int accountNumber) throws MissingConfigurationItemsException {
		String key = serviceName + "_" + accountNumber;
		if(!cachedServiceProperties.containsKey(key)) {			
			ServiceAccountProperties serviceAccountProperties = AccountXmlParser.getAccountProperties(serviceName, accountNumber);
			if(!hasNecesaryData(serviceAccountProperties)) {
				cachedServiceProperties.put(key, serviceAccountProperties);				
			}
		}
		
		return createService(cachedServiceProperties.get(key));
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceKind(String serviceKind) {
		this.serviceKind = serviceKind;
	}

	public String getServiceKind() {
		return serviceKind;
	}

}
