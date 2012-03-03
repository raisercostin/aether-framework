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

import java.util.Collection;
import java.util.HashMap;

import jfs.conf.JFSConst;

/**
 * This class manages the list of plugins and stores all related objects in a
 * hash map using the plugin's identifier as key.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSPluginRepository.java,v 1.11 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSPluginRepository {

	/** Stores the only instance. */
	private static JFSPluginRepository instance = null;

	/** Stores all plugin objects. */
	private HashMap<String, JFSPlugin> plugins = new HashMap<String, JFSPlugin>();

	/**
	 * Probits instantiation and initializes the object.
	 */
	private JFSPluginRepository() {
		String[] pluginClasses = JFSConst.getInstance().getStringArray(
				"jfs.plugins");
		JFSPlugin plugin;

		for (int i = 0; i < pluginClasses.length; i++) {
			try {
				plugin = (JFSPlugin) Class.forName(pluginClasses[i])
						.newInstance();
				plugins.put(plugin.getId(), plugin);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Returns the reference of the only JFSPluginRepository object.
	 * 
	 * @return The only JFSPluginRepository instance.
	 */
	public static JFSPluginRepository getInstance() {
		if (instance == null)
			instance = new JFSPluginRepository();

		return instance;
	}

	/**
	 * Returns all stored plugins.
	 * 
	 * @return Collection of available plugins.
	 */
	public final Collection<JFSPlugin> getPlugins() {
		return plugins.values();
	}

	/**
	 * Returns a specific stored plugin.
	 * 
	 * @param id
	 *            The indetifier of the plugin.
	 * @return Plugin of given ID.
	 */
	public final JFSPlugin getPlugin(String id) {
		return plugins.get(id);
	}
}