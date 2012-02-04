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
import org.dasein.cloud.google.GoogleAppEngine;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.google.common.io.Files;

public class DaseinAdapter {

	private String bucket;
	private BlobStoreSupport blobStoreSupport;

	public DaseinAdapter() {
		Locale.setDefault(Locale.US);
		GoogleAppEngine cloud = new GoogleAppEngine();
		ProviderContext context = new ProviderContext();
		context.setAccessKeys(PropertiesProvider.getProperty("gs.access").getBytes(), PropertiesProvider.getProperty("gs.secret").getBytes());
		cloud.connect(context);
		blobStoreSupport = cloud.getStorageServices().getBlobStoreSupport();

		populateCache();
	}

	public void populateCache() {

		bucket = PropertiesProvider.getProperty("gs.bucket");
		try {
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Collection<CloudStoreObject> listFiles = (Collection<CloudStoreObject>) blobStoreSupport.listFiles(bucket);
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
			CloudStoreObject object = new CloudStoreObject();
			object.setContainer(false);
			object.setDirectory(bucket);
			object.setName(path);
			object.setProviderRegionId("us-east-1");
			download = blobStoreSupport.download(object, selectedFile);

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
