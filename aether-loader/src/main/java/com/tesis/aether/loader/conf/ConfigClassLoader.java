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
	private String path = "configClassLoader.xml";
	private HashMap<String, String> classExceptions = new HashMap<String, String>();
	private HashMap<String, String> packageExceptions = new HashMap<String, String>();

	public ConfigClassLoader() throws SAXException, IOException,
			ParserConfigurationException {
		loadConfig();
	}

	public ConfigClassLoader(String path) throws SAXException, IOException,
			ParserConfigurationException {
		this.path = path;
		loadConfig();
	}

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
		nListExceptions = doc.getElementsByTagName("packageException");
		for (int temp = 0; temp < nListExceptions.getLength(); temp++) {
			Node nNode = nListExceptions.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				src = getTagValue("srcPackage", eElement);
				dst = getTagValue("dstPackage", eElement);
				if (src != null && dst != null) {
					System.out.println("loading package exception " + src + " to " + dst);
					packageExceptions.put(src, dst);
				}
			}
		}
	}
	
	 private String getTagValue(String sTag, Element eElement){
		    NodeList nlList= eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		    Node nValue = (Node) nlList.item(0); 
		    if (nValue != null)
		    	return nValue.getNodeValue();
		    else
		    	return null;
		 }
	 
	 public HashMap<String, String> getClassExceptions(){
		 return classExceptions;
	 }
	 
	 public HashMap<String, String> getPackageExceptions(){
		 return packageExceptions;
	 }

}
