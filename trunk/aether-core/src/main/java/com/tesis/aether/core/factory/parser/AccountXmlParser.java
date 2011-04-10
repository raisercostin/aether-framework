package com.tesis.aether.core.factory.parser;

import java.util.Map;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;


public class AccountXmlParser {
	
	public static AccountXmlParser INSTANCE = new AccountXmlParser();

	public static ServiceAccountProperties getAccountProperties(String serviceName, int accountNumber) {
		return null;		
	}

	public Map<String, CloudService> loadServices() {
		return null;

	}
	
}
