/*
 * $Id: Annotations.java 1135 2009-09-09 05:49:05Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2009
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

package fr.inrialpes.exmo.align.impl;

public class Annotations {

    public static String ID = "id";
    public static String METHOD = "method";
    public static String DERIVEDFROM = "derivedFrom";
    public static String PARAMETERS = "parameters";
    public static String CERTIFICATE = "certificate";
    public static String TIME = "time";
    public static String LIMITATIONS = "limitations";
    public static String PROPERTIES = "properties";
    public static String PRETTY = "pretty";
    public static String PROVENANCE = "provenance";

    /* Set to true for rejecting the use of deprecated (non deterministic) primitives */
    // JE2009: Unrelated to Annotations...
    public static boolean STRICT_IMPLEMENTATION = false;

}
