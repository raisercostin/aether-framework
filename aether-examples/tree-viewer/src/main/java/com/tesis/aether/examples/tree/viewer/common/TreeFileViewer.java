package com.tesis.aether.examples.tree.viewer.common;

public interface TreeFileViewer {

	public abstract void connect(String accessKey, String secretKey);

	public abstract TreeLoader loadFileTree(String bucket, String accessKey, String secretKey) throws Exception;

}