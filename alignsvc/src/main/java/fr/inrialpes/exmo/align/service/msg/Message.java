/*
 * $Id: Message.java 1572 2011-04-24 22:10:26Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2007, 2009, 2011
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

public class Message {

    int surrogate = 0;
    Message inReplyTo = null;
    String receiver = "";
    String sender = "";
    String content = "";
    Properties parameters = null;

    public Message ( int surr, Message rep, String from, String to, String cont, Properties param ) {
	surrogate = surr;
	inReplyTo = rep;
	receiver = to;
	sender = from;
	content = cont;
	parameters = param;
    }

    public String HTMLString(){
	return "<h1>Message</h1><dl><dt>id:</dt><dd>"+surrogate+"</dd><dt>sender:</dt><dd>"+sender+"</dd><dt>receiver:</dt><dd>"+receiver+"</dd><dt>in-reply-to:</dt><dd>"+inReplyTo+"</dd><dt>content:</dt><dd>"+content+"</dd></dl>";
    }

    public String RESTString(){
	return "<ErrorMsg/>";
    }

    public String HTMLRESTString(){
	return "<ErrorMsg/>";
    }

    public String SOAPString(){
	return "<id>"+surrogate+"</id>"+"<sender>"+sender+"</sender>" + "<receiver>"+receiver+"</receiver>" + "<in-reply-to>" + inReplyTo+ "</in-reply-to>" + "<content>" + content + "</content>";	
    }

    public int getId () {
	return surrogate;
    }

    public Message getInReplyTo() {
	return inReplyTo;
    }

    public String getReceiver() {
	return receiver;
    }

    public String getContent() {
	return content;
    }

    public String getSender() {
	return sender;
    }

    public Properties getParameters() {
	return parameters;
    }

}
