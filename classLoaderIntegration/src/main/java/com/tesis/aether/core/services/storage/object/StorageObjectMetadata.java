package com.tesis.aether.core.services.storage.object;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public class StorageObjectMetadata {
	private String path;
	private String type; 
	private String name;
	private String pathAndName;
	private Long length;
	private Date lastModified;
	private URI uri;
		
	public boolean isDirectory(){
		return this.getType().equals(StorageObjectConstants.DIRECTORY_TYPE);
	}
	
	public boolean isFile(){
		return this.getType().equals(StorageObjectConstants.FILE_TYPE);		
	}
	
	public void setLength(Long length) {
		this.length = length;
	}
	public Long getLength() {
		return length;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setPathAndName(String pathAndName) {
		this.pathAndName = pathAndName;
	}
	public String getPathAndName() {
		return pathAndName;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public Date getLastModified() {
		return lastModified;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}
}
