package com.tesis.aether.examples.tree.viewer.jets3t;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.TreeLoader;

@SuppressWarnings("deprecation")
public class JetS3tTreeViewer implements TreeFileViewer {

	public static TreeFileViewer INSTANCE = new JetS3tTreeViewer();
	private RestS3Service s3Service = null;

	private JetS3tTreeViewer() {
	}

	public void connect(String accessKey, String secretKey) {
		AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
		try {
			s3Service = new RestS3Service(credentials);
		} catch (S3ServiceException e) {
			e.printStackTrace();
		}
	}

	public TreeLoader loadFileTree(String bucket, String accessKey,
			String secretKey) throws Exception {
		if (s3Service == null) {
			connect(accessKey, secretKey);
		}
		if (s3Service == null)
			throw new Exception(
					"Error en la conexion. s3Service no disponible.");

		TreeLoader tl = new TreeLoader();

		S3Bucket[] buckets = null;
		if (bucket == null) {
			buckets = s3Service.listAllBuckets();
		} else {
			buckets = new S3Bucket[] { s3Service.getBucket(bucket) };
		}

		for (int b = 0; b < buckets.length; b++) {
			tl.addDirectory(buckets[b].getName());
			tl.enterDirectory(buckets[b].getName());
			S3Object[] objects = s3Service.listObjects(buckets[b]);
			for (int o = 0; o < objects.length; o++) {
				tl.addArchive(objects[o].getKey());
			}
			tl.leaveDirectory();
		}
		return tl;
	}
}
