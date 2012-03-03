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

package jfs.sync.cloudloop;

import java.io.File;
import java.net.URI;
import java.net.URL;

import jfs.conf.JFSConst;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

import com.cloudloop.Cloudloop;
import com.cloudloop.storage.CloudStore;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich
 *          Exp $
 */
public class JFSCloudLoopFileProducer extends JFSFileProducer {

	private String bucket;
	private CloudStore storage;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSCloudLoopFileProducer(String uri) {
		super(JFSConst.SCHEME_CLOUDLOOP, uri);

		try {
			URI parsedUri = new URI(uri);
			this.bucket = parsedUri.getHost();

			URL cfgResource = ClassLoader.getSystemResource("cloudloop.xml");
			Cloudloop cloudloop = Cloudloop.loadFrom(new File(cfgResource.toURI()));
			storage = cloudloop.getStorage("amazon");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public final JFSFile getRootJfsFile() {
		return new JFSCloudLoopFile(this, "", storage, bucket);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public final JFSFile getJfsFile(String path) {
		return new JFSCloudLoopFile(this, path, storage, bucket);
	}
}