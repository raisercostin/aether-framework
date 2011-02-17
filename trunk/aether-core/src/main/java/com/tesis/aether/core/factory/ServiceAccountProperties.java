package com.tesis.aether.core.factory;

import java.util.HashMap;
import java.util.Map;

public class ServiceAccountProperties {

	private Map<String,String> properties = new HashMap<String, String>();

	public void putProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return this.properties.get(key);
	}

	public void setProperties(Map<String,String> properties) {
		this.properties = properties;
	}

	public Map<String,String> getProperties() {
		return properties;
	}
	
}
