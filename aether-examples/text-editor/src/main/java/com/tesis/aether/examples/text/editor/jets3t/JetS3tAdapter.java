package com.tesis.aether.examples.text.editor.jets3t;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.ObjectUtils;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;

public class JetS3tAdapter implements CloudAdapter {

	public static CloudAdapter INSTANCE = new JetS3tAdapter();
	private RestS3Service s3Service;

	private JetS3tAdapter() {
	}

	public void connect(String accessKey, String secretKey) {
		AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
		try {
			s3Service = new RestS3Service(credentials);
		} catch (S3ServiceException e) {
			e.printStackTrace();
		}
	}

	public void uploadFile(String fileToMonitor, String bucket, String localFile) {

		try {

			S3Object fileObject = ObjectUtils.createObjectForUpload(fileToMonitor, new File(localFile), null, false);

			s3Service.putObject(bucket, fileObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadFile(String fileToMonitor, String bucket, String localFile) {
		try {			
			StorageObject fileObject = s3Service.getObject(bucket, fileToMonitor);
			
			InputStream inputStream = fileObject.getDataInputStream();
			OutputStream out = new FileOutputStream(localFile);
			byte buf[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			inputStream.read();
			out.close();
			inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
