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

package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Columns;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action sorts the currently active {@link com.mucommander.ui.main.table.FileTable} by owner.
 * If the table is already sorted by owner, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByOwnerAction extends SortByAction {

    public SortByOwnerAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties, Columns.OWNER);
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new SortByOwnerAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "SortByOwner";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.VIEW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_DOWN_MASK); }
    }
}
