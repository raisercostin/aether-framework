package com.tesis.aether.examples.tree.viewer.dasein;

import java.util.Locale;
import java.util.Vector;

import org.dasein.cloud.CloudProvider;
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
    	Locale.setDefault(Locale.US);
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
		Iterable<CloudStoreObject> listFiles = blobStoreSupport.listFiles(bucket);
		for (CloudStoreObject object : listFiles) {
			processObject(object.getName(), tl);
			tl.leaveToRoot();
		}
		return tl;
	}

	public void processObject(String object, TreeLoader tl) {
		System.out.println("Adding object: " + object);
		String[] directories = getDirectories(object);
		String name = getName(object);
		for (int i = 0; i < directories.length; i++){
			if (tl.existDirectory(directories[i])){
				tl.enterDirectory(directories[i]);
			} else {
				tl.addDirectory(directories[i]);
				tl.enterDirectory(directories[i]);
			}
		}
		if (!name.equals("")) {
			tl.addArchive(name);
		}
	}

	private String getName(String object) {
		String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		if (st.length > 0 && !st[st.length - 1].equals("*")) {
			if (st.length > 1) {
				return st[st.length -1];
			} else {
				return st[st.length - 1];
			}
		} else {
			return "";
		}
	}

	private String[] getDirectories(String object) {
		String aux = object.replace("/", "/*/");
		String[] st = aux.split("/");
		Vector<String> directories = new Vector<String>();
		if (st.length > 1) {
			for (int i = 1; i < st.length; i = i + 2) {
				directories.add(st[i-1]);
			}
		}
		return directories.toArray(new String[]{});
	}

}
