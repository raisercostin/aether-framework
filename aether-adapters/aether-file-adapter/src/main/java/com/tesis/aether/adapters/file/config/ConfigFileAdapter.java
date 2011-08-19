package com.tesis.aether.adapters.file.config;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigFileAdapter {
	/**
	 * Path por defecto al xml de configuracion
	 */
	private String path = "configFileAdapter.xml";
	private String NAME = "name";
	private String VALUE = "value";
	private String PROPERTY = "property";
	/**
	 * Contiene las propiedades de configuracion
	 */
	private HashMap<String, String> properties = null;

	/**
	 * Constructor por defecto de la clase. Se encarga de cargar la configuracion correspondiente.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public ConfigFileAdapter() throws SAXException, IOException,
			ParserConfigurationException {
	}

	/**
	 * Constructor de la clase que carga la configuracion desde el archivo especificado por parámetro
	 * @param path path al archivo de configuracion
	 * @throws SAXException Excepcion al parsear el archivo xml
	 * @throws IOException en caso de no encontrarse el archivo o no poseer los privilegios necesarios para lectura
	 * @throws ParserConfigurationException excepcion al parsear el archivo
	 */
	public ConfigFileAdapter(String path) throws SAXException, IOException,
			ParserConfigurationException {
		this.path = path;
	}

	/**
	 * Carga la configuración del classloader
	 * @throws SAXException excepcion al parsear el archivo xml
	 * @throws IOException excepcion al ocurrir algun error con el archivo de configuracion
	 * @throws ParserConfigurationException excepcion al parsear el archivo xml
	 */
	public HashMap<String, String> getProperties() throws SAXException, IOException,
			ParserConfigurationException {
		properties = new HashMap<String, String>(); 
		System.out.println("loading configuration from: " + this.path);
		File fXmlFile = new File(path);
		System.out.println("Path de carga: " + fXmlFile.getAbsolutePath());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName(PROPERTY);
		String name, value;
		if (nList.getLength() == 0) {
			System.out.println("No se han encontrado propiedades...");
		} else {
			System.out.println("Se han encontrado " + nList.getLength() + " propiedades...");
		}
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				name = getTagValue(NAME, eElement);
				value = getTagValue(VALUE, eElement);
				if (name != null && value != null) {
					System.out.println("loading property " + name + " value " + value);
					properties.put(name, value);
				}
			}
		}
		return properties;
	}
	
	/**
	 * Retorna el valor del tag especificado
	 * @param sTag nombre del tag
	 * @param eElement elemento xml en el cual buscar el tag
	 * @return valor del tag especificado
	 */
	 private String getTagValue(String sTag, Element eElement){
	    NodeList nlList= eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	    Node nValue = (Node) nlList.item(0); 
	    if (nValue != null)
	    	return nValue.getNodeValue();
	    else
	    	return null;
	 }
}
