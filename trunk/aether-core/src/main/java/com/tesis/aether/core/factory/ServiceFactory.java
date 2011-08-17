package com.tesis.aether.core.factory;

import java.util.Map;

import com.tesis.aether.core.exception.MissingConfigurationItemsException;
import com.tesis.aether.core.exception.ServiceCreationException;
import com.tesis.aether.core.exception.WrongServiceTypeException;
import com.tesis.aether.core.factory.parser.AccountXmlParser;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.CloudServiceConstants;
import com.tesis.aether.core.services.storage.BaseStorageService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;

public class ServiceFactory {

	private Map<String, CloudService> services;

	public static ServiceFactory instance = new ServiceFactory();
	// TODO REMOVER ESTO
	public static final String SERVICES_FILE = "src/main/resources/config.xml";

	protected ServiceFactory() {
		try {
			setServices(AccountXmlParser.INSTANCE.loadServices(SERVICES_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna el servicio de Storage por defecto según el XML configurado por
	 * el usuario.
	 * 
	 * @return
	 */
	public ExtendedStorageService getFirstStorageService() {
		for (String key : services.keySet()) {
			if (services.get(key).getKind().equals(CloudServiceConstants.STORAGE_KIND)) {
				return (ExtendedStorageService) services.get(key);
			}
		}

		return null;

	}

	public Map<String, CloudService> getServices() {
		return services;
	}

	/**
	 * Retorna el servicio de Storage que posee el nombre pasado como parámetro.
	 * 
	 * @param serviceKey
	 * @return
	 * @throws MissingConfigurationItemsException
	 * @throws ServiceCreationException
	 */
	public BaseStorageService getStorageService(String serviceKey) throws MissingConfigurationItemsException, ServiceCreationException {
		return getStorageService(serviceKey, 1);
	}

	public ExtendedStorageService getStorageService(String serviceKey, int accountNumber) throws MissingConfigurationItemsException, ServiceCreationException {

		try {
			CloudService cloudService = getServices().get(serviceKey);
			if (!cloudService.getKind().equals(CloudServiceConstants.STORAGE_KIND)) {
				throw new WrongServiceTypeException("Service " + serviceKey + " is not a storage service");
			}
			return (ExtendedStorageService) cloudService;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceCreationException("Unknown service creation error");
		}

	}

	public void setServices(Map<String, CloudService> services) {
		this.services = services;
	}

}