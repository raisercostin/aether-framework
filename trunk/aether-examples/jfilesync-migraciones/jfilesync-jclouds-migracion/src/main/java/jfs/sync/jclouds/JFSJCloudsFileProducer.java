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

package jfs.sync.jclouds;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;

import jfs.conf.JFSConst;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich
 *          Exp $
 */
public class JFSJCloudsFileProducer extends JFSFileProducer {

	private BlobStore blobStore;
	private String bucket;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSJCloudsFileProducer(String uri) {
		super(JFSConst.SCHEME_JCLOUDS, uri);

		try {
			URI parsedUri = new URI(uri);
			this.bucket = parsedUri.getHost();

			String identity = PropertiesProvider.getProperty("gs.access");
			String credential = PropertiesProvider.getProperty("gs.secret");

			BlobStoreContextFactory factory = new BlobStoreContextFactory();
			BlobStoreContext context = factory.createContext("googlestorage", identity, credential);
			blobStore = context.getBlobStore();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public final JFSFile getRootJfsFile() {
		return new JFSJCloudsFile(this, "", blobStore, bucket);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public final JFSFile getJfsFile(String path) {
		return new JFSJCloudsFile(this, path, blobStore, bucket);
	}
}