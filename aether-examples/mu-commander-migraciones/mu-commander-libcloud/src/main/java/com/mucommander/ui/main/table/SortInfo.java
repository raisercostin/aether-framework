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

package com.mucommander.ui.main.table;

import com.mucommander.conf.impl.MuConfiguration;

/**
 * This class holds information describes how a {@link FileTable} is currently sorted: sort criterion,
 * ascending/descending order, whether directories are displayed first or mixed with regular files.
 *
 * <p>The values are not meant to be changed outside this package: all setters are package-protected.
 * Use {@link FileTable} methods to change how the table is sorted.</p>
 *
 * @author Maxence Bernard
 */
public class SortInfo implements Cloneable {

    /** Current sort criteria */
    private int criterion = Columns.NAME;

    /** Ascending/descending order */
    private boolean ascendingOrder = true;

    /** Should folders be displayed first, or mixed with regular files */
    private boolean showFoldersFirst = MuConfiguration.getVariable(MuConfiguration.SHOW_FOLDERS_FIRST, MuConfiguration.DEFAULT_SHOW_FOLDERS_FIRST);


    public SortInfo() {
    }


    /**
     * Returns the column criterion currently used to sort the table.
     *
     * @return the current column criterion used to sort the table, see {@link Columns} for allowed values
     */
    public int getCriterion() {
        return criterion;
    }

    /**
     * Sets the column criterion currently used to sort the table.
     *
     * @param criterion the current column criterion used to sort the table, see {@link Columns} for allowed values
     */
    void setCriterion(int criterion) {
        this.criterion = criterion;
    }

    /**
     * Returns <code>true</code> if the current sort order of is ascending, <code>false</code> if it is descending.
     *
     * @return true if the current sort order is ascending, false if it is descending
     */
    public boolean getAscendingOrder() {
        return ascendingOrder;
    }

    /**
     * Sets the sort order of the column corresponding to the current criterion.
     *
     * @param ascending true if the current sort order is ascending, false if it is descending
     */
    void setAscendingOrder(boolean ascending) {
        this.ascendingOrder = ascending;
    }

    /**
     * Returns <code>true</code> if folders are sorted and displayed before regular files, <code>false</code> if they
     * are mixed with regular files and sorted altogether.
     *
     * @return true if folders are sorted and displayed before regular files, false if they are mixed with regular files and sorted altogether
     */
    public boolean getFoldersFirst() {
        return showFoldersFirst;
    }

    /**
     * Sets whether folders are currently sorted and displayed before regular files or mixed with them.
     *
     * @param showFoldersFirst true if folders are sorted and displayed before regular files, false if they are mixed with regular files and sorted altogether
     */
    void setFoldersFirst(boolean showFoldersFirst) {
        this.showFoldersFirst = showFoldersFirst;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch(CloneNotSupportedException e) {
            // Should never happen
            return null;
        }
    }
}
