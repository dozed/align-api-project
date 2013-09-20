/*
 * $Id: ErrorMsg.java 1830 2013-03-09 18:20:23Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2011
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
 * Contains the messages that should be sent according to the protocol
 */

public class ErrorMsg extends Message {

    public ErrorMsg ( int surr, Message rep, String from, String to, String cont, Properties param ) {
	super( surr, rep, from, to, cont, param );
    }
    public String HTMLString(){
	String message = "Generic error: "+content;
	if ( parameters != null ) {
	    message += "<ul>";
	    for ( String key : parameters.stringPropertyNames()) {
		message += "<li>"+key+" = "+parameters.getProperty( key )+"</li>";
	    }
	    message += "/<ul>";
	}
	return message;
    }
    public String RESTString(){
	return "<error>" + content + "</error>";
    }
    public String HTMLRESTString(){
	return HTMLString();
    }
    public String SOAPString(){
	String res = "    <ErrorMsg>\n";
	res += "      <msgid>"+surrogate+"</msgid>\n"+"        <sender>"+sender+"</sender>\n" + "        <receiver>"+receiver+"</receiver>\n" ;
	// Would be better to use inReplyTo's surrogate, but these ints are inconvenients
	if ( inReplyTo != null ) res += "      <in-reply-to>"+inReplyTo+"</in-reply-to>\n";
	res += "      "+RESTString()+"\n"+"    </ErrorMsg>\n";
	return res;
    }
}
