/*
 * $Id: AlignmentServiceProfile.java 1189 2010-01-03 17:57:13Z euzenat $
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006
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

import java.util.Properties;

public interface AlignmentServiceProfile {

    /**
     * Creates the Service object and declares it after any
     * required registery
     */
    public void init( Properties p, AServProtocolManager m ) throws AServException;

    /**
     * Shutdown the Service and undeclare it from any registery
     */
    public void close() throws AServException;
}
