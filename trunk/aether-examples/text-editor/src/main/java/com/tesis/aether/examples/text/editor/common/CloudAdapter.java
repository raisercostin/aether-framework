package com.tesis.aether.examples.text.editor.common;

public interface CloudAdapter {

	public abstract void connect(String accessKey, String secretKey);

	public abstract void uploadFile(String fileToMonitor, String bucket, String localFile);

	public abstract void downloadFile(String fileToMonitor, String bucket, String localFile);

}