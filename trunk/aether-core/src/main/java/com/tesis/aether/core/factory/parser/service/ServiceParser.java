package com.tesis.aether.core.factory.parser.service;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.CloudService;

public class ServiceParser {
	
	private static final String VALUE = "value";
	private static final String KEY = "key";
	private static final String PARAMETER = "parameter";

	public CloudService parse(Element service_param_el, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {		
		ServiceAccountProperties properties = getParameters(service_param_el);
		
		return parse(properties, className);
	}

	protected CloudService parse(ServiceAccountProperties properties, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {	
		CloudService service = (CloudService) Class.forName(className).newInstance();
		service.setServiceProperties(properties);
		return service;
	}

	private ServiceAccountProperties getParameters(Element service_param_el) {
		NodeList localBaseFolder_element = service_param_el.getElementsByTagName(PARAMETER);	
		int countParams = localBaseFolder_element.getLength();
		ServiceAccountProperties properties = new ServiceAccountProperties();
		
		for (int j = 0; j < countParams; j++) {
			Element property_param_el = (Element) localBaseFolder_element.item(j);
			String key = property_param_el.getAttribute(KEY);
			String value = property_param_el.getAttribute(VALUE);
			properties.putProperty(key, value);
		}
		
		return properties;
	}	
	
}
