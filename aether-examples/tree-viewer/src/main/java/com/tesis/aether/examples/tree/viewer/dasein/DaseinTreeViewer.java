package com.tesis.aether.examples.tree.viewer.dasein;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.TreeLoader;

public class DaseinTreeViewer implements TreeFileViewer {

	public static TreeFileViewer INSTANCE = new DaseinTreeViewer();
	private BlobStoreSupport blobStoreSupport = null;

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

	public TreeLoader loadFileTree(String bucket, String accessKey,
			String secretKey) throws Exception {
		if (blobStoreSupport == null) {
			connect(accessKey, secretKey);
		}
		if (blobStoreSupport == null)
			throw new Exception(
					"Error en la conexion. blobStoreSupport no disponible.");

		TreeLoader tl = new TreeLoader();
		loadTree(tl, bucket);
		return tl;
	}

	private void loadTree(TreeLoader tl, String path) {
		try {
			Iterable<CloudStoreObject> listFiles = blobStoreSupport
					.listFiles(path);
			for (CloudStoreObject object : listFiles) {
				if (object.isContainer()) {
					tl.addDirectory(object.getName());
					tl.enterDirectory(object.getName());
					loadTree(tl, path + "/" + object.getName());
					tl.leaveDirectory();
				} else {
					tl.addArchive(object.getName());
				}
			}
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (CloudException e) {
			e.printStackTrace();
		}
	}
}
