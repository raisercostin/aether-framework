package org.dasein.cloud.aws.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.aws.storage.S3;
import org.dasein.cloud.aws.storage.S3Method.S3Response;
import org.dasein.cloud.storage.FileTransfer;

public class HTTPS3 extends S3 {

	static private final Logger logger = Logger.getLogger(HTTPS3.class);
	private AWSCloud provider;

	public HTTPS3(AWSCloud provider) {
		super(provider);
		this.provider = provider;
	}

	public long exists(String bucket, String object, boolean multiPart) throws InternalException, CloudException {
		if (object == null) {
			return 0L;
		}
		if (!multiPart) {
			S3Method method = new S3Method(provider, S3Action.GET_OBJECT);
			S3Response response;

			try {
				response = method.invoke(bucket, object, "s3.amazonaws.com");
				if (response != null && response.headers != null) {
					for (Header header : response.headers) {
						if (header.getName().equalsIgnoreCase("Content-Length")) {
							return Long.parseLong(header.getValue());
						}
					}
				}
				return 0L;
			} catch (S3Exception e) {
				if (e.getStatus() != HttpStatus.SC_NOT_FOUND) {
					String code = e.getCode();

					if (code == null || (!code.equals("NoSuchBucket") && !code.equals("NoSuchKey"))) {
						throw new CloudException(e);
					}
				}
				return -1L;
			}
		} else {
			if (exists(bucket, object + ".properties", false) == -1L) {
				return -1L;
			}
			try {
				File propsFile = File.createTempFile("props", ".properties");
				Properties props = new Properties();
				String str;

				try {
					get(bucket, object + ".properties", propsFile, null);
					props.load(new FileInputStream(propsFile));
				} finally {
					propsFile.delete();
				}
				str = props.getProperty("length");
				if (str == null) {
					return 0L;
				} else {
					return Long.parseLong(str);
				}
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
				throw new InternalException(e);
			}
		}
	}

	
	
	
	private void copy(InputStream input, OutputStream output, FileTransfer xfer) throws IOException {
		try {
			byte[] bytes = new byte[10240];
			long total = 0L;
			int count;

			if (xfer != null) {
				xfer.setBytesTransferred(0L);
			}
			while ((count = input.read(bytes, 0, 10240)) != -1) {
				if (count > 0) {
					output.write(bytes, 0, count);
					total = total + count;
					if (xfer != null) {
						xfer.setBytesTransferred(total);
					}
				}
			}
			output.flush();
		} finally {
			input.close();
			output.close();
		}
	}

	private void get(String bucket, String object, File toFile, FileTransfer transfer) throws InternalException, CloudException {
		IOException lastError = null;
		int attempts = 0;

		while (attempts < 5) {
			S3Method method = new S3Method(provider, S3Action.GET_OBJECT);
			S3Response response;

			try {
				response = method.invoke(bucket, object);
				try {
					copy(response.input, new FileOutputStream(toFile), transfer);
					return;
				} catch (FileNotFoundException e) {
					logger.error(e);
					e.printStackTrace();
					throw new InternalException(e);
				} catch (IOException e) {
					lastError = e;
					logger.warn(e);
					try {
						Thread.sleep(10000L);
					} catch (InterruptedException ignore) {
					}
				} finally {
					response.close();
				}
			} catch (S3Exception e) {
				logger.error(e.getSummary());
				throw new CloudException(e);
			}
			attempts++;
		}
		logger.error(lastError);
		lastError.printStackTrace();
		throw new InternalException(lastError);
	}
}
