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

package jfs.plugins;

import javax.swing.JFrame;

/**
 * All JFS plugins have to implement this class. In addition they have to be
 * registered in the JFS configuration file.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSPlugin.java,v 1.10 2007/02/26 18:49:10 heidrich Exp $
 */
public interface JFSPlugin {

	/**
	 * Returns the plugin's identifier. A corresponding key has to be defined in
	 * the translation bundle in order to identify the plugin's title.
	 * 
	 * @return The plugin's identifier.
	 */
	public abstract String getId();

	/**
	 * Each plugin has to implement this method in order to be initialized by
	 * the main program. Usually a Swing GUI should pop up after calling this
	 * method.
	 * 
	 * @param frame
	 *            The frame the init method is called from.
	 */
	public abstract void init(JFrame frame);
}