package com.tesis.aether.core.services;

import com.tesis.aether.core.auth.authenticable.Authenticable;
import com.tesis.aether.core.auth.authenticator.Authenticator;
import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.DisconnectionException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.factory.ServiceAccountProperties;

public abstract class CloudService implements Authenticable{

	private String kind;
	private String name;
	private ServiceAccountProperties serviceProperties;
	
	public abstract void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException;

	public abstract void disconnect() throws DisconnectionException, MethodNotSupportedException;
	
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getKind() {
		return kind;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setServiceProperties(ServiceAccountProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}
	public ServiceAccountProperties getServiceProperties() {
		return serviceProperties;
	}
	public String getServiceProperty(String key) {
		return serviceProperties.getProperty(key);
	}
	public void addServiceProperty(String key, String value) {
		this.serviceProperties.putProperty(key, value);
	}
	
}
