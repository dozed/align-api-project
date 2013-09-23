/*
 * $Id: RenameProperties.java 1785 2012-10-16 11:03:44Z euzenat $
 *
 * Copyright (C) 2011-2012, INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.gen.alt;

import java.util.Properties;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class RenameProperties extends RenameThings {

    public RenameProperties( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.RENAME_PROPERTIES );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
	boolean synonym = false;
	boolean translate = false;
	boolean random = true;
	String method = params.getProperty( ParametersIds.RENAME_METHOD );
	if ( method != null ) {
	    random = false;
	    if ( method.equals( ParametersIds.RENAME_SYNONYM ) ) synonym = true;
	    else if ( method.equals( ParametersIds.RENAME_TRANSLATE ) ) translate = true;
	    else random = true;
	}
	modifiedModel = renameResource ( true, false, percentage, random, translate, synonym, 0);
	return this; // useless
    };

}
