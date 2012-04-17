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

package jfs.sync;

/**
 * This class produces and destroys factories for JFS files.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSFileProducerFactory.java,v 1.1 2005/05/06 11:06:57 heidrich
 *          Exp $
 */
public abstract class JFSFileProducerFactory {
	/**
	 * Resets all producers of the factory.
	 */
	public abstract void resetProducers();

	/**
	 * Returns a new procucer for a special URI.
	 * 
	 * @param uri
	 *            The URI to create the producer for.
	 * @return The created producer.
	 */
	public abstract JFSFileProducer createProducer(String uri);

	/**
	 * Shuts down an existing producer for a special URI.
	 * 
	 * @param uri
	 *            The URI to distroy the producer for.
	 */
	public abstract void shutDownProducer(String uri);

	/**
	 * Cancels an existing producer for a special URI.
	 * 
	 * @param uri
	 *            The URI to distroy the producer for.
	 */
	public abstract void cancelProducer(String uri);
}