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

package jfs.sync.external;

import java.util.HashMap;

import jfs.conf.JFSConfig;
import jfs.server.JFSServerAccess;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerFactory;

/**
 * This class produces factories for local JFS files.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSExternalFileProducerFactory.java,v 1.1 2005/05/06 11:06:56
 *          heidrich Exp $
 */
public class JFSExternalFileProducerFactory extends JFSFileProducerFactory {
	/** The map of file producers. */
	private HashMap<String, JFSExternalFileProducer> producers = new HashMap<String, JFSExternalFileProducer>();

	/**
	 * @see JFSFileProducerFactory#resetProducers()
	 */
	public final void resetProducers() {
		producers.clear();
	}

	/**
	 * @see JFSFileProducerFactory#createProducer(String)
	 */
	public final JFSFileProducer createProducer(String uri) {
		JFSExternalFileProducer p = new JFSExternalFileProducer(uri);
		producers.put(uri, p);
		return p;
	}

	/**
	 * @see JFSFileProducerFactory#shutDownProducer(String)
	 */
	public final void shutDownProducer(String uri) {
		JFSExternalFileProducer p = producers.get(uri);
		if (p != null && JFSConfig.getInstance().getServerShutDown()) {
			JFSServerAccess sa = JFSServerAccess.getInstance(p.getHost(), p
					.getPort(), p.getRootPath());
			sa.shutDown();
		}
	}

	/**
	 * @see JFSFileProducerFactory#cancelProducer(String)
	 */
	public final void cancelProducer(String uri) {
		JFSExternalFileProducer p = producers.get(uri);
		if (p != null) {
			JFSServerAccess sa = JFSServerAccess.getInstance(p.getHost(), p
					.getPort(), p.getRootPath());
			sa.cancel();
		}
	}
}