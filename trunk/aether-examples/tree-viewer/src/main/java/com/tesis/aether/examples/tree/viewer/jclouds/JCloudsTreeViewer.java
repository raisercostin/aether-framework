package com.tesis.aether.examples.tree.viewer.jclouds;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.TreeLoader;

public class JCloudsTreeViewer implements TreeFileViewer {

	public static TreeFileViewer INSTANCE = new JCloudsTreeViewer();
	private BlobStoreContext s3Context = null;
	private BlobStore blobStore = null;

	private JCloudsTreeViewer() {
	}

	public void connect(String accessKey, String secretKey) {
		s3Context = new BlobStoreContextFactory().createContext("s3",
				accessKey, secretKey);
		blobStore = s3Context.getBlobStore();
	}

	public TreeLoader loadFileTree(String bucket, String accessKey,
			String secretKey) throws Exception {
		if (blobStore == null) {
			connect(accessKey, secretKey);
		}
		if (blobStore == null)
			throw new Exception(
					"Error en la conexion. s3Context y/o blobStore no disponible.");

		TreeLoader tl = new TreeLoader();
		loadTree(tl, bucket);
		return tl;
	}

	private void loadTree(TreeLoader tl, String bucket) {
		PageSet<? extends StorageMetadata> list = blobStore.list(bucket,
				ListContainerOptions.NONE);
		for (StorageMetadata object : list) {
			if (object.getType().equals(StorageType.BLOB)) {
				tl.addArchive(FilenameUtils.getName(object.getName()));
			} else {
				String name = FilenameUtils.getName(object.getName());
				String path = FilenameUtils.getPathNoEndSeparator(object
						.getName());
				tl.addDirectory(name);
				tl.enterDirectory(name);
				loadTree(tl, path + "/" + name);
				tl.leaveDirectory();
			}
		}
	}
}
