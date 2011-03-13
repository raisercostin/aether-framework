package com.tesis.aether.examples.text.editor.jclouds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;

public class JCloudsAdapter implements CloudAdapter {

	public static CloudAdapter INSTANCE = new JCloudsAdapter();
	private BlobStoreContext s3Context;
	private BlobStore blobStore;
	
	private JCloudsAdapter() {
	}

	public void connect(String accessKey, String secretKey) {
		s3Context = new BlobStoreContextFactory().createContext("s3", accessKey, secretKey);
		blobStore = s3Context.getBlobStore();
	}
	
	public void uploadFile(String fileToMonitor, String bucket, String localFile) {
		Blob blob = blobStore.newBlob(fileToMonitor);
		blob.setPayload(new File(localFile));
		blobStore.putBlob(bucket, blob);
	}
		
	public void downloadFile(String fileToMonitor, String bucket, String localFile) {
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
