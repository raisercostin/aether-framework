package neoe.dasein;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.aws.storage.S3;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.google.common.io.Files;

public class DaseinAdapter {

	private String bucket;
	private S3 blobStoreSupport;

	public DaseinAdapter() {
		Locale.setDefault(Locale.US);
		AWSCloud cloud = new AWSCloud();
		ProviderContext context = new ProviderContext();
		context.setAccessKeys(PropertiesProvider.getProperty("aws.access").getBytes(), PropertiesProvider.getProperty("aws.secret").getBytes());
		context.setEndpoint("http://s3.amazonaws.com");
		context.setRegionId("us-east-1");
		cloud.connect(context);
		blobStoreSupport = new S3(cloud);

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
			Collection<CloudStoreObject> listFiles = blobStoreSupport.listFiles(bucket);
			for (CloudStoreObject cloudStoreObject : listFiles) {
				if (cloudStoreObject.getName().endsWith("/") && cloudStoreObject.getSize() == 0) {
					// FOLDER
					continue;
				} else {
					// BLOB
					File file = new File(bucket + "/" + cloudStoreObject.getName());
					Files.createParentDirs(file);
					FileUtils.touch(file);
				}

			}

		} catch (Exception e) {
			e.printStackTrace( );
		}

	}

	public void uploadFile(String fn, File cacheDirectory) {
		String path = fn.replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);
		
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		
		try {
			blobStoreSupport.upload(new File(fn), bucket, path, false, null);
		} catch (CloudException e) {
			e.printStackTrace();
		} catch (InternalException e) {
			e.printStackTrace();
		}

	}

	public void updateCachedFile(File selectedFile, File cacheDirectory) {
		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);
		
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		
		FileTransfer download;
		try {
			download = blobStoreSupport.download(bucket, path, selectedFile, null);

			while (!download.isComplete()) {
				Thread.sleep(500);
			}
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (CloudException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public InputStream getInputStream(File f, File cacheDirectory) {
		updateCachedFile(f, cacheDirectory);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
