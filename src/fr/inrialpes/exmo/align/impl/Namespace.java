/*
 * $Id: Namespace.java 1739 2012-07-15 23:26:48Z euzenat $
 *
 * Copyright (C) 2005 Digital Enterprise Research Insitute (DERI) Galway
 * Copyright (C) 2006 Digital Enterprise Research Institute (DERI) Innsbruck
 * Sourceforge version 1.2 - 2008 - then NamespaceDefs.java
 * Copyright (C) INRIA, 2008-2010, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl;

import java.util.HashMap;
import java.util.Map;

// TODO: get shortcut by uri

// JE: for extendibility purposes, it would be useful that this class be something
// else than an enum class.

public enum Namespace {
    ALIGNMENT("http://knowledgeweb.semanticweb.org/heterogeneity/alignment", "align", true),
	ALIGNSVC("http://exmo.inrialpes.fr/align/service","alignsvc",true),
	EDOAL("http://ns.inria.org/edoal/1.0/", "edoal", true),
	DUBLIN_CORE("http://purl.org/dc/elements/1.1/", "dc", false),
	RDF_SCHEMA("http://www.w3.org/2000/01/rdf-schema#", "rdfs", false),
	SOAP_ENV("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV", false),
	XSD("http://www.w3.org/2001/XMLSchema", "xsd", true),
	XSI("http://www.w3.org/1999/XMLSchema-instance", "xsi", false),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf", false),
	NONE("", "fake", false),
	ATLMAP("http://www.atl.external.lmco.com/projects/ontology/ResultsOntology.n3#", "map", false);

	public final String uri;

	public final String shortCut;

	/**
	 * records if a sharp must be concatenated to the namespace
	 */
	private final boolean addSharp;

	public final String prefix;

	private static final Map<String, Namespace> register = new HashMap<String, Namespace>();

	Namespace(final String sUri, final String sShort, final boolean sharp ) {
	    uri = sUri;
	    shortCut = sShort;
	    addSharp = sharp;
	    if ( addSharp )
		prefix = uri+"#";
	    else
		prefix = uri;
	}

	public String getUri() {
		return uri;
	}

	public String getUriPrefix() {
		return prefix;
	}

	public String getShortCut() {
		return shortCut;
	}

	public boolean getSharp() {
		return addSharp;
	}

	/**
	 * Determines a namespace instance depending on it's url.
	 * 
	 * @param url
	 *            the url of the namespace.
	 * @return the namespace instance, or null, if no mathing namespace could be
	 *         found.
	 */
	public static Namespace getNSByUri(final String url) {
	    Namespace result = null;
	    if (register.size() <= 0) {
		for ( Namespace ns : Namespace.values() ) {
		    register.put(ns.getUri(), ns);
		    if ( ns.getUri().equals( url ) ) result = ns;
		}
	    } else { result = register.get(url); }
	    return result;
	}
}
