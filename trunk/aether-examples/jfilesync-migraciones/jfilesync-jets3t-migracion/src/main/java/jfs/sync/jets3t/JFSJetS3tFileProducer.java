/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */

package jfs.sync.jets3t;

import java.net.URI;

import jfs.conf.JFSConst;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.GSCredentials;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich
 *          Exp $
 */
public class JFSJetS3tFileProducer extends JFSFileProducer {

	private String bucket;
	private GoogleStorageService s3Service;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSJetS3tFileProducer(String uri) {
		super(JFSConst.SCHEME_JETS3T, uri);

		try {
			URI parsedUri = new URI(uri);
			this.bucket = parsedUri.getHost();

			String identity = PropertiesProvider.getProperty("gs.access");
			String credential = PropertiesProvider.getProperty("gs.secret");
			GSCredentials gsCredentials = new GSCredentials(identity, credential);
			s3Service = new GoogleStorageService(gsCredentials);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public final JFSFile getRootJfsFile() {
		return new JFSJetS3tFile(this, "", s3Service, bucket);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public final JFSFile getJfsFile(String path) {
		return new JFSJetS3tFile(this, path, s3Service, bucket);
	}
}