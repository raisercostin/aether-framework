package neoe.jclouds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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

public class JCloudsAdapter {
	private BlobStoreContext context;
	private String bucket;
	
	public JCloudsAdapter() {
		context = new BlobStoreContextFactory().createContext("aws-s3", PropertiesProvider.getProperty("aws.access"), PropertiesProvider.getProperty("aws.secret"));
		populateCache();
	}

	public void populateCache() {

		bucket = PropertiesProvider.getProperty("aws.bucket");
		try {
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			PageSet<? extends StorageMetadata> list = context.getBlobStore().list(bucket, ListContainerOptions.Builder.recursive());
			for (StorageMetadata storageMetadata : list) {
				if (!storageMetadata.getType().equals(StorageType.FOLDER) && !storageMetadata.getType().equals(StorageType.RELATIVE_PATH)) {
					File file = new File(bucket + "/" + storageMetadata.getName());
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
		Blob blob = context.getBlobStore().getBlob(bucket, path);
		InputStream input = blob.getPayload().getInput();

		OutputStream out = null;

		try {

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
		
		BlobStore blobStore = context.getBlobStore();
		BlobBuilder blobBuilder = blobStore.blobBuilder(path);
		blobBuilder.payload(file);

		blobStore.putBlob(bucket, blobBuilder.build());

	}

	public InputStream getInputStream(File selectedFile, File cacheDirectory) {

		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		BlobStore blobStore = context.getBlobStore();
		
		Blob blob = blobStore.getBlob(bucket, path);
		return blob.getPayload().getInput();
		
	}

}
