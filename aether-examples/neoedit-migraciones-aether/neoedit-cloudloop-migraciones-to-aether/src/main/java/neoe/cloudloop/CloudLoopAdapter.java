package neoe.cloudloop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import neoe.ne.PropertiesProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.cloudloop.Cloudloop;
import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreDirectory;
import com.cloudloop.storage.CloudStoreFile;
import com.cloudloop.storage.CloudStoreObject;
import com.cloudloop.storage.CloudStoreObjectType;
import com.google.common.io.Files;

public class CloudLoopAdapter {

	private String bucket;
	private CloudStore storage;

	public CloudLoopAdapter() {
		try {
			URL cfgResource = ClassLoader.getSystemResource("cloudloop.xml");
			
			Cloudloop cloudloop = Cloudloop.loadFrom(new File(cfgResource.toURI()));
			storage = cloudloop.getStorage("amazon");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			CloudStoreDirectory directory = storage.getDirectory("/");
			CloudStoreObject[] listDirectoryContents = storage.listDirectoryContents(directory, CloudStoreObjectType.FILE, true);
			for (CloudStoreObject cloudStoreObject : listDirectoryContents) {
				File file = new File(bucket + "/" + cloudStoreObject.getPath().getPathText());
				Files.createParentDirs(file);
				FileUtils.touch(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateCachedFile(File selectedFile, File prefix) {
		String path = selectedFile.getAbsolutePath().replace(prefix.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			CloudStoreFile remoteFile = storage.getFile("/" + path);
			InputStream input = storage.download(remoteFile, null);

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void uploadFile(String selectedFile, File cacheDirectory) {

		File file = new File(selectedFile);
		String path = file.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			CloudStoreFile destinationFile = storage.getFile("/" + path);
			destinationFile.getMetadata().setContentLengthInBytes(file.length());
			destinationFile.setStreamToStore(new FileInputStream(selectedFile));
			storage.upload(destinationFile, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InputStream getInputStream(File selectedFile, File cacheDirectory) {

		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			CloudStoreFile remoteFile = storage.getFile("/" + path);
			return storage.download(remoteFile, null);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
