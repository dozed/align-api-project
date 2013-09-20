/*
 * $Id: Extensions.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010
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

package fr.inrialpes.exmo.align.impl; 

import java.util.Hashtable;
import java.util.Collection;
import java.util.Set;

/**
 * This contains the metadata extensions
 * They are indexed by namespace + local name.
 * Unfortunatelly there is no way to deal with this in a simple hastable with a pair as key.
 * So it is implemented the old way with concatenation... unfortunately
 */

public class Extensions {

    private Hashtable<String,String[]> table;
 
    public Extensions() {
	table = new Hashtable<String,String[]>();
    }
  
    public Extensions( Hashtable<String,String[]> ht ) {
	table = ht;
    }
  
    public void setExtension( String ns, String label, String value ){
	final String[] ext = { ns, label, value };
	table.put( ns+label, ext );
    }

    public void unsetExtension( String ns, String label ){
	table.remove( ns+label );
    }

    public String getExtension( String ns, String label ){
	String[] ext = table.get( ns+label );
	if ( ext != null ) return ext[2];
	else return (String)null;
    }
    
    public String[] getExtensionCell( String ns, String label ){
	return table.get( ns+label );
    }
    
    // JE: why can't I type this Set<String>
    public Set keySet() {
	return table.keySet();
    }

    public Collection<String[]> getValues() {
	return table.values();
    }

    @SuppressWarnings( "unchecked" )
    public Object clone() {
	return new Extensions( (Hashtable<String,String[]>)table.clone() ); //[W:unchecked]
    }    
}
