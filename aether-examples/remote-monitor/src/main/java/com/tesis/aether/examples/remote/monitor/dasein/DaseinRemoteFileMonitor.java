package com.tesis.aether.examples.remote.monitor.dasein;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.tesis.aether.examples.remote.monitor.common.RemoteFileMonitor;

public class DaseinRemoteFileMonitor implements RemoteFileMonitor {

	public static RemoteFileMonitor INSTANCE = new DaseinRemoteFileMonitor();
	private Timer timer = null;
	private BlobStoreSupport blobStoreSupport;
	private Date previousExecutionDate = new Date(0);

	private DaseinRemoteFileMonitor() {
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

		CloudProvider provider = new AWSCloud();
		ProviderContext ctx = new ProviderContext();
		ctx.setAccessKeys(accessKey.getBytes(), secretKey.getBytes());
		ctx.setStorageKeys(accessKey.getBytes(), secretKey.getBytes());
		ctx.setEndpoint("http://s3.amazonaws.com");
		ctx.setRegionId("us-east-1");
		
		provider.connect(ctx);		
		blobStoreSupport = provider.getStorageServices().getBlobStoreSupport();
		
		timer.schedule(new TimerTask() {
			public void run() {
				CloudStoreObject cloudStoreObject = getLastModifiedDate(bucket, fileToMonitor);
				Date lastModified = cloudStoreObject.getCreationDate();
				if (lastModified.after(previousExecutionDate)) {
					previousExecutionDate = lastModified;
					downloadFile(cloudStoreObject, localFile);
				}

			}

		}, 0, 30000);
	}

	private CloudStoreObject getLastModifiedDate(String bucket, String fileToMonitor) {

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

	private void downloadFile(CloudStoreObject cloudStoreObject, String localFile) {
		try {
			FileTransfer download = blobStoreSupport.download(cloudStoreObject, new File(localFile));
			while(download.getPercentComplete() < 1) {
				Thread.sleep(3000);
			}
		} catch (CloudException e) {
			e.printStackTrace();
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
