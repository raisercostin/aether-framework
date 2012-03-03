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
import simplecloud.storage.interfaces.IStorageAdapter;
import simplecloud.storage.providers.amazon.S3Adapter;
import simplecloud.storage.providers.nirvanix.NirvanixAdapter;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich
 *          Exp $
 */
public class JFSLibcloudFileProducer extends JFSFileProducer {

	private IStorageAdapter nirvanix;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSLibcloudFileProducer(String uri) {
		super(JFSConst.SCHEME_LIBCLOUD, uri);

		try {

			String app_name = PropertiesProvider.getProperty("nirvanix.app.name");
			String app_key = PropertiesProvider.getProperty("nirvanix.app.key");
			String user = PropertiesProvider.getProperty("nirvanix.user");
			String pass = PropertiesProvider.getProperty("nirvanix.pass");
							
			nirvanix = new NirvanixAdapter(app_name, app_key, user, pass);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public final JFSFile getRootJfsFile() {
		return new JFSLibcloudFile(this, "", nirvanix);
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public final JFSFile getJfsFile(String path) {
		return new JFSLibcloudFile(this, path, nirvanix);
	}
}