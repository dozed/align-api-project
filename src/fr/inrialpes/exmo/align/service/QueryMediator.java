/*
 * $Id: QueryMediator.java 1102 2009-08-16 21:29:13Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2009
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

/*
 * QueryMediator.java
 *
 * Created on May 20, 2006, 12:15 AM
 *
 */

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.queryprocessor.QueryProcessor;
import fr.inrialpes.exmo.queryprocessor.Result;
import fr.inrialpes.exmo.queryprocessor.Type;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import org.xml.sax.SAXException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;

/**
 * QueryMediator
 * 
 * A query processor that mediates queries through an ontology alignment.
 * This implementation rely on an embedded QueryProcessor.
 * Another possible implementation would be to subclass a query processor.
 * This would however provide few benefits (speed gained by no indirection)
 * against loss in generality.
 * 
 * @author Arun Sharma
 * @author Jérôme Euzenat
 */
public class QueryMediator implements QueryProcessor {
    
    private Alignment alignment;
    private QueryProcessor processor;
    
    // May be usefull to prohibit this...
    //public QueryMediator( ) {
    //}
    
    public QueryMediator( QueryProcessor proc, Alignment a ) {
	processor = proc;
        alignment = a;
    }
    
    public QueryMediator( QueryProcessor proc, String alignmentURI ) throws SAXException,ParserConfigurationException,IOException {
	processor = proc;
	AlignmentParser aparser = new AlignmentParser(0);
	try { alignment = aparser.parse( alignmentURI ); }
	catch ( Exception ex ) {
	    throw new ParserConfigurationException("Error on parsing");
	}
    }

    public QueryMediator( Alignment a ) {
	// For this to work we need to generate a processor
	this( (QueryProcessor)null, a );
    }
    
    public QueryMediator( String alignmentURI ) throws SAXException,ParserConfigurationException,IOException {
	// For this to work we need to generate a processor
	this( (QueryProcessor)null, alignmentURI );
    }

    /**
     * @param query -- The query string
     * @param type -- The query type, can be one of SELECT, ASK, CONSTRUCT, or DESCRIBE
     * @return Result, result form depends on type
     */
    // JE: There is a flaw in the query API: it should be defined with
    // throws QueryException because if something fails, this will be
    // done silently. (same for the other).
    public Result query(String query, Type type) {
	try {
	    String newQuery = rewriteQuery( query );
	    return processor.query( newQuery, type );
	} catch (AlignmentException e) { return (Result)null; }
    }
    
    /**
     *@param query  -- The query string
     */
    public Result query(String query) {
	try {
	    String newQuery = rewriteQuery( query );
	    return processor.query( newQuery );
	} catch (AlignmentException e) { return (Result)null; }
    }

    /**
     *@param query -- The query string
     *@return query results as string
     */
    public String queryWithStringResults(String query) {
	try {
	    String newQuery = rewriteQuery( query );
	    return processor.queryWithStringResults( newQuery );
	} catch (AlignmentException e) { return (String)null; }
    }
    
    /**
     *@param query -- the query string
     *@return the type of the query
     */
    public int getType(String query){
	return processor.getType( query );
    }
    
    public void loadOntology(String uri){
	processor.loadOntology( uri );
    }
    
    /**
     * @param aQuery -- query to be re-written
     * @return -- rewritten query:
     * - replaces all the prefix namespaces, if present, in the query by actual IRIs
     * - replaces all entity IRI by their counterpart in the ontology
     *
     * Caveats:
     * - This does only work for alignments with =
     * - This does not care for the *:x status of alignments
     * - This does work from ontology1 to ontology2, not the otherway round
     *    (use invert() in this case).
     */    
    public String rewriteQuery( String aQuery ) throws AlignmentException {
	return rewriteSPARQLQuery( aQuery, alignment );
    }

    public static String rewriteSPARQLQuery( String aQuery, Alignment align ) throws AlignmentException {
	// The first part expands the prefixes of the query
        //aQuery = aQuery.replaceFirst("^[ \t\n]+","").replaceAll("PREFIX", "prefix");
	aQuery = aQuery.trim().replaceAll("PREFIX", "prefix");
        String mainQuery = ""; 
        if( aQuery.indexOf("prefix") != -1 )  {
            String[] pref = aQuery.split("prefix");               
            for(int j=0; j < pref.length; j++)  {
                String str = "";
                if(!pref[0].equals(""))   
                    str = pref[0];
                else
                    str = pref[pref.length-1];
                mainQuery = str.substring(str.indexOf('>')+1, str.length());
            }
                
            for(int i = 0; i < pref.length; i++)  {       
                String currPrefix = pref[i].trim();       
                if(!currPrefix.equals("") && currPrefix.indexOf('<') != -1 && currPrefix.indexOf('>') != -1)  {
                    int begin = currPrefix.indexOf('<');
                    int end = currPrefix.indexOf('>');
                    String ns = currPrefix.substring(0, begin).trim();
                    String iri = currPrefix.substring(begin+1, end).trim();
		    mainQuery = Pattern.compile(ns+"([A-Za-z0-9_-]+)").matcher(mainQuery).replaceAll("<"+iri+"#$1>");
		    //mainQuery = mainQuery.replaceAll(ns+"([A-Za-z0-9_-]+)", "<"+iri+"#$1>");
                }
            }
        } else mainQuery = aQuery;
	// The second part replaces the named items by their counterparts
	for( Cell cell : align ){
	    mainQuery = mainQuery.replaceAll(
					     cell.getObject1AsURI(align).toString(),
					     cell.getObject2AsURI(align).toString() );
	}
        return mainQuery;
    }
    
}
