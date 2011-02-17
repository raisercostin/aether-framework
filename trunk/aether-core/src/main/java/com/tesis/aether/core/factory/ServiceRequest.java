package com.tesis.aether.core.factory;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ServiceRequest {
	
	private Multimap<String, Integer> requests = HashMultimap.create();
	
	public ServiceRequest(String serviceType, int accountNumber) {
		requests.put(serviceType, accountNumber);
	}

	public ServiceRequest with(String serviceType, int accountNumber) {
		requests.put(serviceType, accountNumber);		
		return this;
	}

	public Collection<Integer> getAccountsForService(String service) {
		return requests.get(service);
	}
	
	public Set<String> getServices() {
		return requests.keySet();
	}

	public int size() {
		return requests.size();
	}
	
}
