package com.tesis.aether.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;

public class CodecUtil {
	public static String getMd5FromFile(File file) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DigestInputStream dis = new DigestInputStream(bis, md5);

			// read the file and update the hash calculation
			while (dis.read() != -1)
				;

			// get the hash value as byte array
			byte[] hash = md5.digest();

			return byteArray2Hex(hash);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
