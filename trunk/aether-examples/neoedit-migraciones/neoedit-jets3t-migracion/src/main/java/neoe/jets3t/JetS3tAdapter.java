package neoe.jets3t;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.GSCredentials;

import com.google.common.io.Files;

public class JetS3tAdapter {

	private String bucket;
	private GoogleStorageService s3Service;

	public JetS3tAdapter() {
		GSCredentials awsCredentials = new GSCredentials(PropertiesProvider.getProperty("gs.access"), PropertiesProvider.getProperty("gs.secret"));
		try {
			s3Service = new GoogleStorageService(awsCredentials);
			populateCache();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	public void populateCache() {

		bucket = PropertiesProvider.getProperty("gs.bucket");
		try {
			FileUtils.cleanDirectory(new File(bucket));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			GSObject[] listObjects = s3Service.listObjects(bucket);
			for (GSObject s3Object : listObjects) {
				if (!s3Object.isDirectoryPlaceholder()) {
					File file = new File(bucket + "/" + s3Object.getKey());
					Files.createParentDirs(file);
					FileUtils.touch(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateCachedFile(File selectedFile, File cacheDirectory) {
		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			StorageObject object = s3Service.getObject(bucket, path);
			InputStream input = object.getDataInputStream();

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
		} catch (S3ServiceException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}

	}

	public InputStream getInputStream(File selectedFile, File cacheDirectory) {
		String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);
		try {
			StorageObject object = s3Service.getObject(bucket, path);
			return object.getDataInputStream();
		} catch (ServiceException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void uploadFile(String selectedFile, File cacheDirectory) {

		File file = new File(selectedFile);
		String path = file.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
		path = FilenameUtils.separatorsToUnix(path);

		try {
			StorageObject object = new StorageObject(file);
			object.setKey(path);
			s3Service.putObject(bucket, object);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
