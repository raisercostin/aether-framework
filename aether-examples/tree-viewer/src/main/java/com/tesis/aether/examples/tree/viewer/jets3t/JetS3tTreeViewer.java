package com.tesis.aether.examples.tree.viewer.jets3t;

import java.util.Vector;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import com.tesis.aether.examples.tree.viewer.common.TreeFileViewer;
import com.tesis.aether.examples.tree.viewer.common.TreeLoader;

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

		S3Object[] objects;
		try {
			objects = s3Service.listObjects(bucket);
		} catch (Exception e) {
			e.printStackTrace();
			return new TreeLoader("Error al procesar el pedido: " + e.getMessage());
		}
		TreeLoader tl = new TreeLoader();
		for (int o = 0; o < objects.length; o++) {
			processObject(objects[o].getName(), objects[o].isDirectoryPlaceholder(), tl);
			tl.leaveToRoot();
		}
		return tl;
	}
	
	public void processObject(String object, boolean isContainer, TreeLoader tl) {
		System.out.println("Objecto: " + object + "  isContainer: " + isContainer);
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
