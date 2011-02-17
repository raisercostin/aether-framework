package com.tesis.aether.core.services;

import java.io.File;

import com.tesis.aether.core.exception.FileNotExistsException;

public class Validations {

	public static void checkLocalFileExists(String localFile) throws FileNotExistsException {
		if (!new File(localFile).exists()) {
			throw new FileNotExistsException(localFile + " does not exist.");
		}
	}
}
