package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.tesis.aether.core.exception.CopyFileException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MigrationException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class AsyncStorageServiceWrapper {
	
	private ExtendedStorageService wrappedService;
	private ExecutorService executorService;
	
	public AsyncStorageServiceWrapper(ExtendedStorageService objectToWrap, int min) {
		this.executorService = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		this.wrappedService = objectToWrap;
	}
	
	public Future<URI> getPublicURLForPath(final String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		return executorService.submit(new Callable<URI>() {
			public URI call() throws Exception {
				return wrappedService.getPublicURLForPath(remotePath);
			}
		});
	}

	public Future<List<StorageObjectMetadata>> listFiles(final String remotePath, final boolean recursive) throws MethodNotSupportedException {
		return executorService.submit(new Callable<List<StorageObjectMetadata>>() {
			public List<StorageObjectMetadata> call() throws Exception {
				return wrappedService.listFiles(remotePath, recursive);
			}
		});
	}

	public Future<Long> sizeOf(final String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Long>() {
			public Long call() throws Exception {
				return wrappedService.sizeOf(remotePath);
			}
		});
	}

	public Future<Date> lastModified(final String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Date>() {
			public Date call() throws Exception {
				return wrappedService.lastModified(remotePath);
			}
		});
	}

	public Future<InputStream> getInputStream(final String remotePathFile) throws FileNotExistsException {
		return executorService.submit(new Callable<InputStream>() {
			public InputStream call() throws Exception {
				return wrappedService.getInputStream(remotePathFile);
			}
		});
	}

	public Future<Void> uploadInputStream(final InputStream stream, final String remoteDirectory, final String filename, final Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.uploadInputStream(stream, remoteDirectory, filename, contentLength);
				return null;
			}
		});
	}

	public Future<Void> deleteFile(final String remotePathFile) throws DeleteException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.deleteFile(remotePathFile);
				return null;
			}
		});
	}

	public Future<Void> deleteFolder(final String remotePath) throws DeleteException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.deleteFolder(remotePath);
				return null;
			}
		});
	}

	public Future<Void> createFolder(final String remotePath) throws FolderCreationException, MethodNotSupportedException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.createFolder(remotePath);
				return null;
			}
		});
	}

	public Future<Boolean> checkFileExists(final String remotePath) throws MethodNotSupportedException {
		return executorService.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return wrappedService.checkFileExists(remotePath);
			}
		});
	}

	public Future<Boolean> checkDirectoryExists(final String remotePath) throws MethodNotSupportedException {
		return executorService.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return wrappedService.checkDirectoryExists(remotePath);
			}
		});
	}

	public Future<StorageObjectMetadata> getMetadataForObject(final String remotePathFile) {
		return executorService.submit(new Callable<StorageObjectMetadata>() {
			public StorageObjectMetadata call() throws Exception {
				return wrappedService.getMetadataForObject(remotePathFile);
			}
		});
	}

	public Future<StorageObject> getStorageObject(final String remotePathFile) throws FileNotExistsException {
		return executorService.submit(new Callable<StorageObject>() {
			public StorageObject call() throws Exception {
				return wrappedService.getStorageObject(remotePathFile);
			}
		});
	}

	public Future<Void> downloadToDirectory(final String remotePathFile, final File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.downloadToDirectory(remotePathFile, localDirectory);
				return null;
			}
		});
	}

	public Future<Void> downloadFileToDirectory(final String remotePathFile, final File localDirectory) throws FileNotExistsException, DownloadException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.downloadFileToDirectory(remotePathFile, localDirectory);
				return null;
			}
		});
	}

	public Future<Void> downloadDirectoryToDirectory(final String remotePathFile, final File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.downloadDirectoryToDirectory(remotePathFile, localDirectory);
				return null;
			}
		});
	}

	public Future<Void> upload(final File localPath, final String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.upload(localPath, remoteDirectory);
				return null;
			}
		});
	}

	public Future<Void> uploadDirectory(final File localDirectory, final String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.uploadDirectory(localDirectory, remoteDirectory);
				return null;
			}
		});
	}

	public Future<Void> uploadSingleFile(final File localFile, final String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.uploadSingleFile(localFile, remoteDirectory);
				return null;
			}
		});
	}

	public Future<Void> delete(final String remotePathFile, final boolean recursive) throws DeleteException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.delete(remotePathFile, recursive);
				return null;
			}
		});
	}

	public Future<Void> migrateData(final String startingPath, final ExtendedStorageService target, final String targetPath) throws MigrationException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.migrateData(startingPath, target, targetPath);
				return null;
			}
		});
	}

	public Future<Boolean> checkObjectExists(final String remotePath) throws MethodNotSupportedException {
		return executorService.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return wrappedService.checkObjectExists(remotePath);
			}
		});
	}

	public Future<Void> moveFile(final String from, final String toDirectory) throws MoveFileException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.moveFile(from, toDirectory);
				return null;
			}
		});
	}

	public Future<Void> copyFile(final String from, final String toDirectory) throws CopyFileException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.copyFile(from, toDirectory);
				return null;
			}
		});
	}

	public Future<Void> delete(final StorageObjectMetadata file, final boolean recursive) throws DeleteException {
		return executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				wrappedService.delete(file, recursive);
				return null;
			}
		});
	}

}