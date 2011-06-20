package com.tesis.aether.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;

public class CodecUtil {
	public static String getMd5FromFile(File file) {
		MessageDigest md5;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DigestInputStream dis = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DigestInputStream(bis, md5);

			// read the file and update the hash calculation
			while (dis.read() != -1)
				;

			// get the hash value as byte array
			byte[] hash = md5.digest();

			return byteArray2Hex(hash);
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
}
