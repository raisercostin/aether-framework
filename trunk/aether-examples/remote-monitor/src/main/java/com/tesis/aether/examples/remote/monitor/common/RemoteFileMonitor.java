package com.tesis.aether.examples.remote.monitor.common;

public interface RemoteFileMonitor {

	public abstract void stopMonitoring();

	public abstract void startMonitoring(final String localFile, final String fileToMonitor, final String bucket, String accessKey, String secretKey);

}