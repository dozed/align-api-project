/*
 * $Id: EntityList.java 1721 2012-04-26 19:58:33Z euzenat $
 *
 * Copyright (C) INRIA, 2012
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

package fr.inrialpes.exmo.align.service.msg;

import java.util.Properties;

/**
 * A set of entities matching another entity
 */

public class EntityList extends Success {

    String pretty = null;

    public EntityList( int surr, Message rep, String from, String to, String cont, Properties param ) {
	super( surr, rep, from, to, cont, param );
    }

    public String HTMLString(){
	String id[] = content.split(" ");
	String result = "No entity.";

	if ( id.length >= 1 ) {
	    result = "Entities: <ul>";
	    for ( int i = id.length-1; i >= 0; i-- ){
		result += "<li>"+id[i]+"</a></li>";
	    }
	    result += "</ul>";
	}
	return result;
    }

    public String HTMLRESTString(){
	return HTMLString();
    }

    public String RESTString(){
	String msg = "<entityList>\n";
	String id[] = content.split(" ");
	for ( int i = id.length-1; i >= 0; i-- ){
	    if ( id[i].trim() != "" ) {
		msg += "        <entity>"+id[i].trim()+"</entity>\n";
	    }
	}	
	msg += "      </entityList>";
	return msg;
    }
}
