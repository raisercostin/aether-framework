package com.tesis.aether.core.factory.parser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tesis.aether.core.factory.parser.service.StorageServiceParser;
import com.tesis.aether.core.services.CloudService;

public class AccountXmlParser {
	private final String ELEMENT_STORAGE_SERVICE = "storageService";
	private final String ATTRIBUTE_CLASS = "class";
	private final String PARSERS_PACKAGE = "com.tesis.aether.core.factory.parser.service.";
	public static AccountXmlParser INSTANCE = new AccountXmlParser();
	
	private AccountXmlParser(){}
	
	public Map<String, CloudService> loadServices(String xmlfile) throws Exception {
		try {
			String xml = TranscoderStringUtils.readFileAsString(xmlfile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			StringReader sreader = new StringReader(xml);
			InputSource is = new InputSource(sreader);
			Document doc = builder.parse(is);

			return parseServices(doc);
		} catch (Exception e) {
			throw e;
		}
	}

	private Map<String, CloudService> parseServices(Document doc) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		Map<String, CloudService> services = new HashMap<String, CloudService>();

		NodeList storageService_node = doc.getElementsByTagName(ELEMENT_STORAGE_SERVICE);
		int countParams = storageService_node.getLength();

		for (int j = 0; j < countParams; j++) {
			Element storageService_param_el = (Element) storageService_node.item(j);
			String storageService_class = storageService_param_el.getAttribute(ATTRIBUTE_CLASS);
			StorageServiceParser parser = (StorageServiceParser) Class.forName(PARSERS_PACKAGE + storageService_class).newInstance();
			CloudService service = parser.parse(storageService_param_el);
			services.put(service.getName(), service);
		}

		return services;
	}

}
