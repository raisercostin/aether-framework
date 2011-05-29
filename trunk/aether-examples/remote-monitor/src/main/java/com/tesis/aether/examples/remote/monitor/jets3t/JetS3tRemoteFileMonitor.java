package com.tesis.aether.examples.remote.monitor.jets3t;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;

public class JetS3tRemoteFileMonitor implements RemoteFileMonitor {

	public static JetS3tRemoteFileMonitor INSTANCE = new JetS3tRemoteFileMonitor();
	private Timer timer = null;
	private RestS3Service s3Service;
	private Date previousExecutionDate = new Date(0);

	private JetS3tRemoteFileMonitor() {
	}

	public void stopMonitoring() {
		timer.cancel();
		timer = null;
	}

	public void startMonitoring(final String localFile, final String fileToMonitor, final String bucket, String accessKey, String secretKey) {

		if (timer != null) {
			timer.cancel();
		}

		timer = new Timer();

		AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
		try {
			s3Service = new RestS3Service(credentials);
		} catch (S3ServiceException e) {
			e.printStackTrace();
		}

		timer.schedule(new TimerTask() {
			public void run() {
				Date lastModified = getLastModifiedDate(fileToMonitor, bucket);
				if (lastModified.after(previousExecutionDate)) {
					previousExecutionDate = lastModified;
					downloadFile(fileToMonitor, bucket, localFile);
				}

			}

		}, 0, 30000);
	}
	
	public Date getLastModifiedDate(String fileToMonitor, String bucket) {

		try {
			
			StorageObject fileObject = s3Service.getObjectDetails(bucket, fileToMonitor);
			return fileObject.getLastModifiedDate();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void downloadFile(String fileToMonitor, String bucket, String localFile) {
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
