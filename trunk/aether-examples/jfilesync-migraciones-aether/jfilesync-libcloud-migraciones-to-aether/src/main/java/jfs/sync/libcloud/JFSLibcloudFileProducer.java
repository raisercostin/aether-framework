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

package jfs.sync.libcloud;

import java.net.URI;

import jfs.conf.JFSConst;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import simplecloud.storage.providers.amazon.S3Adapter;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich
 *          Exp $
 */
public class JFSLibcloudFileProducer extends JFSFileProducer {

	private String bucket;
	private S3Adapter amazon;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSLibcloudFileProducer(String uri) {
		super(JFSConst.SCHEME_LIBCLOUD, uri);

		try {
			URI parsedUri = new URI(uri);
			this.bucket = parsedUri.getHost();

			String identity = PropertiesProvider.getProperty("aws.access");
			String credential = PropertiesProvider.getProperty("aws.secret");

			amazon = new S3Adapter(identity, credential, "s3.amazonaws.com");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public final JFSFile getRootJfsFile() {
		return new JFSLibcloudFile(this, "", amazon, bucket);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public final JFSFile getJfsFile(String path) {
		return new JFSLibcloudFile(this, path, amazon, bucket);
	}
}