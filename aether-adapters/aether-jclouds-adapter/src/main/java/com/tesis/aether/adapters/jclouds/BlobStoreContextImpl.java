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

public class BlobStoreContextImpl implements BlobStoreContext {
	
	public BlobRequestSigner getSigner() {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStreamMap createInputStreamMap(String container, ListContainerOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStreamMap createInputStreamMap(String container) {
		// TODO Auto-generated method stub
		return null;
	}

	public BlobMap createBlobMap(String container, ListContainerOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	public BlobMap createBlobMap(String container) {
		// TODO Auto-generated method stub
		return null;
	}

	public AsyncBlobStore getAsyncBlobStore() {
		// TODO Auto-generated method stub
		return null;
	}

	public BlobStore getBlobStore() {
		return new BlobStoreAetherImpl();
	}

	public ConsistencyModel getConsistencyModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public <S, A> RestContext<S, A> getProviderSpecificContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public Utils getUtils() {
		// TODO Auto-generated method stub
		return null;
	}

	public Utils utils() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
 
}
