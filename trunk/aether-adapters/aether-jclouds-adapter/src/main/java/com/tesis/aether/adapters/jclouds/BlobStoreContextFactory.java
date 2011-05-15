package com.tesis.aether.adapters.jclouds;

import org.jclouds.blobstore.BlobStoreContext;

public class BlobStoreContextFactory {

   public BlobStoreContextFactory() {
   }

   public BlobStoreContext createContext(String provider, String identity, String credential) {
	   return new BlobStoreContextImpl();
   }

}