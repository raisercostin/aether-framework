package com.tesis.aether.core.predicates;

import com.google.common.base.Predicate;

public class ServiceTypePredicate implements Predicate<String> {

	private String serviceType;

	public ServiceTypePredicate(String storageKind) {
		this.serviceType = storageKind;
	}

	public boolean apply(String input) {
		return input.equals(serviceType);
	}

}
