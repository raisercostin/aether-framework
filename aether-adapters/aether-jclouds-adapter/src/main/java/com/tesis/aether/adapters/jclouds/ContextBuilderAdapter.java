package com.tesis.aether.adapters.jclouds;

import java.util.Properties;

import javax.annotation.Nullable;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.rest.RestContextSpec;

import com.google.inject.Module;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;

public class ContextBuilderAdapter  extends AetherFrameworkAdapter {
	private static ContextBuilderAdapter INSTANCE = null;

	protected ContextBuilderAdapter() {
		super();
	}

	public static ContextBuilderAdapter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ContextBuilderAdapter();
		}
		return INSTANCE;
	}
	
	public BlobStoreContext createContext(String provider, String identity, String credential) {
		return new AetherStoreContextImpl();
	}

	public BlobStoreContext createContext(String provider, Properties overrides) {
		return new AetherStoreContextImpl();
	}

	public BlobStoreContext createContext(String provider, Iterable<? extends Module> modules, Properties overrides) {
		return new AetherStoreContextImpl();
	}

	public BlobStoreContext createContext(String provider, @Nullable String identity, @Nullable String credential, Iterable<? extends Module> modules) {
		return new AetherStoreContextImpl();
	}

	public BlobStoreContext createContext(String provider, @Nullable String identity, @Nullable String credential, Iterable<? extends Module> modules, Properties overrides) {
		return new AetherStoreContextImpl();
	}
	
	public <S, A> BlobStoreContext createContext(RestContextSpec<S, A> contextSpec) {
		return new AetherStoreContextImpl();
	}

	public <S, A> BlobStoreContext createContext(RestContextSpec<S, A> contextSpec, Properties overrides) {
		return new AetherStoreContextImpl();
	}
}
