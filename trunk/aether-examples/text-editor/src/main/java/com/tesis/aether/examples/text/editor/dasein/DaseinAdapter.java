package com.tesis.aether.examples.text.editor.dasein;

import java.io.File;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.tesis.aether.examples.text.editor.common.CloudAdapter;

public class DaseinAdapter implements CloudAdapter {

	public static CloudAdapter INSTANCE = new DaseinAdapter();
	private BlobStoreSupport blobStoreSupport;

	public void connect(String accessKey, String secretKey) {
		CloudProvider provider = new AWSCloud();
		ProviderContext ctx = new ProviderContext();
		ctx.setAccessKeys(accessKey.getBytes(), secretKey.getBytes());
		ctx.setStorageKeys(accessKey.getBytes(), secretKey.getBytes());
		ctx.setEndpoint("http://s3.amazonaws.com");
		ctx.setRegionId("us-east-1");
		
		provider.connect(ctx);		
		blobStoreSupport = provider.getStorageServices().getBlobStoreSupport();
	}

	public void uploadFile(String fileToMonitor, String bucket, String localFile) {
		try {
			blobStoreSupport.upload(new File(localFile), bucket, fileToMonitor, false, null);
		} catch (CloudException e) {
			e.printStackTrace();
		} catch (InternalException e) {
			e.printStackTrace();
		}		
	}


	private CloudStoreObject getCloudStoreObject(String bucket, String fileToMonitor) {

		try {
			Iterable<CloudStoreObject> listFiles = blobStoreSupport.listFiles(bucket);
			for(CloudStoreObject object:listFiles) {
				if(object.getName().equals(fileToMonitor)) {
					return object;			
				}
			}
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (CloudException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	public void downloadFile(String fileToMonitor, String bucket, String localFile) {
		try {
			FileTransfer download = blobStoreSupport.download(getCloudStoreObject(bucket, fileToMonitor), new File(localFile));
			while(download.getPercentComplete() < 1) {
				Thread.sleep(3000);
			}
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (CloudException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
