package com.tesis.aether.core.services.storage.object;

import java.io.InputStream;

public class StorageObject {
	private InputStream stream;
	private StorageObjectMetadata metadata;

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * Retorna un input stream para el objeto remoto asociado, o null de no ser
	 * posible (ej: directorios)
	 * 
	 * @return
	 */
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
