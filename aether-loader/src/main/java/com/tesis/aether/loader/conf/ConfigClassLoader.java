package com.tesis.aether.loader.conf;


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

public class ConfigClassLoader {
	/**
	 * Path por defecto al xml de configuracion
	 */
	private String path = "configClassLoader.xml";
	/**
	 * Contiene los mapeos de clases
	 */
	private HashMap<String, String> classExceptions = new HashMap<String, String>();

	/**
	 * Constructor por defecto de la clase. Se encarga de cargar la configuracion correspondiente.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public ConfigClassLoader() throws SAXException, IOException,
			ParserConfigurationException {
		loadConfig();
	}

	/**
	 * Constructor de la clase que carga la configuracion desde el archivo especificado por parámetro
	 * @param path path al archivo de configuracion
	 * @throws SAXException Excepcion al parsear el archivo xml
	 * @throws IOException en caso de no encontrarse el archivo o no poseer los privilegios necesarios para lectura
	 * @throws ParserConfigurationException excepcion al parsear el archivo
	 */
	public ConfigClassLoader(String path) throws SAXException, IOException,
			ParserConfigurationException {
		this.path = path;
		loadConfig();
	}

	/**
	 * Carga la configuración del classloader
	 * @throws SAXException excepcion al parsear el archivo xml
	 * @throws IOException excepcion al ocurrir algun error con el archivo de configuracion
	 * @throws ParserConfigurationException excepcion al parsear el archivo xml
	 */
	private void loadConfig() throws SAXException, IOException,
			ParserConfigurationException {
		System.out.println("loading configuration from: " + this.path);
		File fXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nListExceptions = doc.getElementsByTagName("classException");
		String src, dst;
		for (int temp = 0; temp < nListExceptions.getLength(); temp++) {
			Node nNode = nListExceptions.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				src = getTagValue("srcClass", eElement);
				dst = getTagValue("dstClass", eElement);
				if (src != null && dst != null) {
					System.out.println("loading class exception " + src + " to " + dst);
					classExceptions.put(src, dst);
				}
			}
		}
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
	 
	 /**
	  * Retorna los mapeos de clases
	  * @return hashmap con los mapeos de clases
	  */
	 public HashMap<String, String> getClassExceptions(){
		 return classExceptions;
	 }
}
