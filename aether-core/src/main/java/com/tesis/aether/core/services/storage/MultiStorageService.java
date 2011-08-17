package com.tesis.aether.core.services.storage;

import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.tesis.aether.core.auth.authenticator.Authenticator;
import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DisconnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class MultiStorageService extends ExtendedStorageService {

	private List<ExtendedStorageService> services;

	@Override
	public URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		for (ExtendedStorageService service : services) {
			try {
				return service.getPublicURLForPath(container, remotePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		throw new URLExtractionException("None of the configured services returned valid data");
	}

	@Override
	public List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException {
		//TODO Implementar
		return null;
	}

	@Override
	public Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		for (ExtendedStorageService service : services) {
			try {
				return service.sizeOf(container, remotePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		throw new MetadataFetchingException("None of the configured services returned valid data");
	}

	@Override
	public Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		for (ExtendedStorageService service : services) {
			try {
				return service.lastModified(container, remotePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		throw new MetadataFetchingException("None of the configured services returned valid data");
	}

	@Override
	public InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException {
		for (ExtendedStorageService service : services) {
			try {
				return service.getInputStream(container, remotePathFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		throw new FileNotExistsException("None of the configured services returned valid data");
	}

	@Override
	public void uploadInputStream(InputStream stream, String container, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		for (ExtendedStorageService service : services) {
			service.uploadInputStream(stream, container, remoteDirectory, filename, contentLength);
		}
	}

	@Override
	public void deleteFile(String container, String remotePathFile) throws DeleteException {
		for (ExtendedStorageService service : services) {
			try {
				service.deleteFile(container, remotePathFile);
			} catch (Exception e) {
				System.out.println("Could not delete file in service: " + service.toString());
			}
		}
	}

	@Override
	public void deleteFolder(String container, String remotePath) throws DeleteException {
		for (ExtendedStorageService service : services) {
			try {
				service.deleteFolder(container, remotePath);
			} catch (Exception e) {
				System.out.println("Could not delete folder in service: " + service.toString());
			}
		}
	}

	@Override
	public void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException {
		for (ExtendedStorageService service : services) {
			try {
				service.createFolder(container, remotePath);
			} catch (Exception e) {
				System.out.println("Could not create folder in service: " + service.toString());
			}
		}
	}

	@Override
	public boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException {
		for (ExtendedStorageService service : services) {
			try {
				return service.checkFileExists(container, remotePath);
			} catch (Exception e) {
			}
		}
		
		return false;
	}

	@Override
	public boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException {
		for (ExtendedStorageService service : services) {
			try {
				return service.checkDirectoryExists(container, remotePath);
			} catch (Exception e) {
			}
		}
		
		return false;
	}

	@Override
	public void createContainer(String name) throws CreateContainerException {
		for (ExtendedStorageService service : services) {
			try {
				service.createContainer(name);
			} catch (Exception e) {
				System.out.println("Could not create container in service: " + service.toString());
			}
		}
	}

	@Override
	public void deleteContainer(String name) throws DeleteContainerException {
		for (ExtendedStorageService service : services) {
			try {
				service.deleteContainer(name);
			} catch (Exception e) {
				System.out.println("Could not delete container in service: " + service.toString());
			}
		}
	}

	@Override
	public List<StorageObjectMetadata> listContainers() {
		//TODO Implementar
		return null;
	}

	@Override
	public boolean existsContainer(String name) {
		for (ExtendedStorageService service : services) {
			try {
				return service.existsContainer(name);
			} catch (Exception e) {
			}
		}
		
		return false;
	}

	@Override
	public void connect(Authenticator authenticator) throws ConnectionException, MethodNotSupportedException {
		for (ExtendedStorageService service : services) {
			try {
				service.connect(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void disconnect() throws DisconnectionException, MethodNotSupportedException {
		for (ExtendedStorageService service : services) {
			try {
				service.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addService(ExtendedStorageService service) {
		this.services.add(service);
	}

	public void setServices(List<ExtendedStorageService> services) {
		this.services = services;
	}

	public List<ExtendedStorageService> getServices() {
		return services;
	}

}
