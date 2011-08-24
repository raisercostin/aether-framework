package com.tesis.aether.examples.tree.file.viewer.common;

import org.apache.commons.io.FilenameUtils;
import com.tesis.aether.adapters.file.File;
import com.tesis.aether.examples.tree.file.viewer.common.TreeLoader;

public class TreeViewer {

	public static TreeViewer INSTANCE = new TreeViewer();

	private TreeViewer() {
	}

	public TreeLoader loadFileTree(String directory) throws Exception {
		TreeLoader tl = new TreeLoader();
		loadTree(tl, directory);
		return tl;
	}

	private void loadTree(TreeLoader tl, String directory) {
		File f = new File(directory);
		File[] list = f.listFiles();
		for (File object : list) {
			if (!object.isDirectory()) {
				tl.addArchive(object.getName());
			} else {
				String name = object.getName();
				String path = FilenameUtils.getPathNoEndSeparator(object
						.getPath());
				String next = path + (!"".equals(path) ? "/" : "") + name;
				if (!next.equals(directory)) {
					tl.addDirectory(name);
					tl.enterDirectory(name);
					loadTree(tl, path + (!"".equals(path) ? "/" : "") + name);
					tl.leaveDirectory();
				}
			}
		}
	}
}
