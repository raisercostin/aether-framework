package com.tesis.aether.examples.remote.monitor.jclouds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;

public class JCloudsRemoteFileMonitor implements RemoteFileMonitor{

	public static JCloudsRemoteFileMonitor INSTANCE = new JCloudsRemoteFileMonitor();
	private Timer timer = null;
	private BlobStoreContext s3Context;
	private BlobStore blobStore;
	private Date previousExecutionDate = new Date(0);
	
	private JCloudsRemoteFileMonitor() {
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

		s3Context = new BlobStoreContextFactory().createContext("s3", accessKey, secretKey);
		blobStore = s3Context.getBlobStore();

		timer.schedule(new TimerTask() {
			public void run() {
				Date lastModified = blobStore.getBlob(bucket, fileToMonitor).getMetadata().getLastModified();
				if(lastModified.after(previousExecutionDate)) {
					previousExecutionDate = lastModified;
					downloadFile(fileToMonitor, bucket, localFile);
				}
				
			}


		}, 0, 30000);
	}			
	
	private void downloadFile(String fileToMonitor, String bucket, String localFile) {
		Blob blob = blobStore.getBlob(bucket, fileToMonitor);

		InputStream inputStream = blob.getPayload().getInput();

		OutputStream out = null;

		try {

			out = new FileOutputStream(new File(localFile));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

		} catch (Exception e) {

		} finally {
			try {
				inputStream.close();
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
}
