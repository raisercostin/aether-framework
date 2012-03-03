package neoe.libcloud;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import simplecloud.storage.providers.amazon.S3Adapter;
import base.interfaces.IItem;
import base.types.Item;

import com.google.common.io.Files;

public class LibCloudAdapter {
	private String bucket;
	private S3Adapter amazon;

	public LibCloudAdapter() {
		amazon = new S3Adapter(PropertiesProvider.getProperty("aws.access"), PropertiesProvider.getProperty("aws.secret"), "s3.amazonaws.com");
		populateCache();
	}

	public void populateCache() {

		bucket = PropertiesProvider.getProperty("aws.bucket");
		try {
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(S3Adapter.Type.SRC_BUCKET, bucket);

		try {
			List<String> listItems = amazon.listItems("/", options);

			for (String blob : listItems) {
				if (!blob.endsWith("/")) {
					File blobFile = new File(bucket + "/" + blob);
					Files.createParentDirs(blobFile);
					FileUtils.touch(blobFile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateCachedFile(File selectedFile, File cacheDirectory) {

		InputStream input = getInputStream(selectedFile, cacheDirectory);

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

		try {
			File file = new File(selectedFile);
			String path = file.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
			path = FilenameUtils.separatorsToUnix(path);

			InputStream stream = new BufferedInputStream(new FileInputStream(file));
			Item item = new Item(stream, "application/xml", null, file.length());
			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(S3Adapter.Type.SRC_BUCKET, bucket);
			if (amazon.storeItem("/" + path, item, new HashMap<String, String>(), options)) {
				System.out.println("Upload succeeded");
			} else {
				System.out.println("Upload failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public InputStream getInputStream(File selectedFile, File cacheDirectory) {
		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(S3Adapter.Type.SRC_BUCKET, bucket);
		IItem fetchItem = amazon.fetchItem("/" + path, options);
		return fetchItem.getContent();
	}

}
