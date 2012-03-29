/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.s3;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import simplecloud.storage.interfaces.IStorageAdapter;
import simplecloud.storage.providers.nirvanix.NirvanixAdapter;

import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;
import com.mucommander.text.Translator;

/**
 * A file protocol provider for the Amazon S3 protocol.
 * 
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {

	// static {
	// Turn off Jets3t logging: failed (404) HEAD request on non-existing object
	// are logged with a SEVERE level,
	// even though this is not an error per se. We don't want those to be
	// reported in the log, so we have no
	// choice but to disable logging entirely.
	// ((Jdk14Logger)LogFactory.getLog(RestS3Service.class)).getLogger().setLevel(Level.OFF);
	// }

	public AbstractFile getFile(FileURL url, Object... instantiationParams)
			throws IOException {
		Locale.setDefault(Locale.US);
		Credentials credentials = url.getCredentials();
		if (credentials == null || credentials.getLogin().equals("")
				|| credentials.getPassword().equals(""))
			throw new AuthException(url);

		IStorageAdapter service;
		String bucketName;

		if (instantiationParams.length == 0) {
			try {
				service = new NirvanixAdapter("nirvanix.app.name", "nirvanix.app.key", credentials.getLogin(), credentials.getPassword());
				
			} catch (Exception e) {
				throw S3File.getIOException(e, url);
			}
		} else {
			service = (IStorageAdapter) instantiationParams[0];
		}

		String path = url.getPath();
		//TEST
//		if (path.startsWith(path)) {
//			path = path.substring(1);
//		}
//		Map<String, String> a= null;
//		IItem fetchItem;
//		try {
//			HashMap<Object, Object> options = new HashMap<Object, Object>();
//			options.put(Type.SRC_BUCKET, path);
////			String path = selectedFile.getAbsolutePath().replace(cacheDirectory.getAbsolutePath() + "\\", "");
//			path = FilenameUtils.separatorsToUnix(path);
//			fetchItem = service.fetchItem("/build.xml", options);
//			a = service.fetchMetadata("/build.xml", options);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		//FIN TEST

		// Root resource
		if (("/").equals(path)) {
			String bckt = Translator.get("default_root_bucket");
			url.setPath(bckt);
			return new S3Bucket(url, service, bckt);

		}

		// Fetch the bucket name from the URL
		StringTokenizer st = new StringTokenizer(path, "/");
		bucketName = st.nextToken();

		// Object resource
		if (st.hasMoreTokens()) {
			 if(instantiationParams.length==2)
			 return new S3Object(url, service, bucketName,
			 (Map<String, String>)instantiationParams[1]);

			return new S3Object(url, service, bucketName);
		}

		 if(instantiationParams.length==2)
		 return new S3Bucket(url, service, bucketName,
		 (Map<String, String>)instantiationParams[1]);

		return new S3Bucket(url, service, bucketName);
//		 return null;
	}
}
