package com.tesis.aether.core.services.storage.object;

import java.io.InputStream;

public class StorageObject {
	private InputStream stream;
	private StorageObjectMetadata metadata;
	
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	public InputStream getStream() {
		return stream;
	}
	public void setMetadata(StorageObjectMetadata metadata) {
		this.metadata = metadata;
	}
	public StorageObjectMetadata getMetadata() {
		return metadata;
	}
}
