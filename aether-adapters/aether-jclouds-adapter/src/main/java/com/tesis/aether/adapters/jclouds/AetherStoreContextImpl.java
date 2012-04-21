package com.tesis.aether.adapters.jclouds;

import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobMap;
import org.jclouds.blobstore.BlobRequestSigner;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.rest.RestContext;
import org.jclouds.rest.Utils;

public class AetherStoreContextImpl implements BlobStoreContext {

	public BlobStore getBlobStore() {
		return JCloudsAetherFrameworkAdapter.getInstance();
	}
	
	/**
	 * UNIMPLEMENTED METHODS
	 */
	
	public BlobRequestSigner getSigner() {
		return null;
	}

	public InputStreamMap createInputStreamMap(String container, ListContainerOptions options) {
		return null;
	}

	public InputStreamMap createInputStreamMap(String container) {
		return null;
	}

	public BlobMap createBlobMap(String container, ListContainerOptions options) {
		return null;
	}

	public BlobMap createBlobMap(String container) {
		return null;
	}

	public AsyncBlobStore getAsyncBlobStore() {
		return null;
	}

	public ConsistencyModel getConsistencyModel() {
		return null;
	}

	public <S, A> RestContext<S, A> getProviderSpecificContext() {
		return null;
	}

	public Utils getUtils() {
		return null;
	}

	public Utils utils() {
		return null;
	}

	public void close() {
		
	}
 
}
