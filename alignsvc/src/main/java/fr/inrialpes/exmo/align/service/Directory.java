/*
 * $Id: Directory.java 1645 2011-11-19 16:05:22Z euzenat $
 *
 * Copyright (C) INRIA, 2007, 2009, 2011
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import org.semanticweb.owl.align.Alignment;

import java.util.Properties;

public interface Directory {

    /**
     * Create a connection and/or registration to a directory
     * Parameters can contain, e.g.:
     * - the directory address
     * - the declaration of the current service
     */
    public void open( Properties p ) throws AServException;

    /**
     * Register an alignment to the directory (if necessary)
     */
    public void register( Alignment al ) throws AServException;

    /**
     * Unregister an alignment to the directory (if necessary)
     */
    public void unregister( Alignment al ) throws AServException;

    /**
     * Shutdown the connection and/or registration to the directory
     */
    public void close() throws AServException;
}
