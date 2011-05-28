package org.jclouds.blobstore;

import org.jclouds.blobstore.BlobStoreContext;

import com.tesis.aether.adapters.jclouds.BlobStoreContextImpl;

public class BlobStoreContextFactory {

   public BlobStoreContextFactory() {
   }

   public BlobStoreContext createContext(String provider, String identity, String credential) {
	   return new BlobStoreContextImpl();
   }

}