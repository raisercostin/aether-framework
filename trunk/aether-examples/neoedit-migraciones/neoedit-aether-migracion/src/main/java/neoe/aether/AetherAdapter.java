package neoe.aether;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import neoe.ne.PropertiesProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

import com.google.common.io.Files;
import com.tesis.aether.core.exception.ConnectionException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.factory.ServiceFactory;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class AetherAdapter {
	private String					bucket;
	private ExtendedStorageService	service;

	public AetherAdapter() {
		service = ServiceFactory.instance.getFirstStorageService();
		try {
			service.connect(null);
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
		populateCache();
	}

	public void populateCache() {

		bucket = PropertiesProvider.getProperty("bucket");
		try {
			new File(bucket).mkdir();
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			List<StorageObjectMetadata> listFiles = service.listFiles(bucket, "", true);

			for (StorageObjectMetadata storageObjectMetadata : listFiles) {
				if (!storageObjectMetadata.isDirectory()) {
					File file = new File(bucket + "/" + storageObjectMetadata.getName());
					Files.createParentDirs(file);
					FileUtils.touch(file);
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void updateCachedFile(File selectedFile, File prefix) {
		String path = selectedFile.getAbsolutePath().replace(prefix.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		OutputStream out = null;
		InputStream input = null;

		try {
			input = service.getInputStream(bucket, path);

			out = new FileOutputStream(selectedFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {

		} finally {
			try {
				input.close();
			} catch (Exception e) {
			}
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (Exception e) {
			}
		}

	}

	public void uploadFile(String selectedFile, File cacheDirectory) {

		File file = new File(selectedFile);
		String path = file.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			service.uploadSingleFile(file, bucket, FilenameUtils.getFullPathNoEndSeparator(path), FilenameUtils.getName(path));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public InputStream getInputStream(File selectedFile, File cacheDirectory) {

		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			return service.getInputStream(bucket, path);
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return null;
		}

	}

}
