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

import simplecloud.storage.interfaces.IStorageAdapter;
import simplecloud.storage.providers.nirvanix.NirvanixAdapter;
import base.interfaces.IItem;
import base.types.Item;

import com.google.common.io.Files;

public class LibCloudAdapter {
	private String bucket;
	private IStorageAdapter amazon;

	public LibCloudAdapter() {
		amazon = new NirvanixAdapter(PropertiesProvider.getProperty("nirvanix.app.name"), PropertiesProvider.getProperty("nirvanix.app.key"), PropertiesProvider.getProperty("nirvanix.user"), PropertiesProvider.getProperty("nirvanix.pass"));
		populateCache();
	}

	public void populateCache() {

		bucket = "nirvanix";
		try {
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(NirvanixAdapter.Type.PAGE_NUMBER, "0");
		options.put(NirvanixAdapter.Type.PAGE_SIZE, "1000");

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
		IItem fetchItem = amazon.fetchItem("/" + path, options);
		return fetchItem.getContent();
	}

}
