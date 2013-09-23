/*
 * $Id: GraphPatternRendererVisitor.java 1833 2013-03-15 10:26:19Z euzenat $
 *
 * Copyright (C) INRIA, 2012-2013
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

package fr.inrialpes.exmo.align.impl.renderer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

/**
 * Translate correspondences into Graph Patterns
 *
 */

// JE: create a string... problem with increment.

public abstract class GraphPatternRendererVisitor extends IndentedRendererVisitor implements EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    protected boolean ignoreerrors = false;
    protected static boolean blanks = false;
    protected boolean weakens = false;
    protected boolean corese = false;
    private boolean inClassRestriction = false;
    private String instance = null;
    private String value = "";
    private String uriType = null;
    private String datatype = "";
    private Object valueRestriction = null;        
    private static int flagRestriction;
    private Constructor op = null;          
    private Integer nbCardinality = null;
    private String opOccurence = "";    
    private static int numberNs;
	private static int number = 1;	
    private static String sub = ""; 
    private static String obj = "";
    private String strBGP = "";
    private String strBGP_Weaken = "";
    protected List<String> listBGP = new ArrayList<String>();
    private Set<String> subjectsRestriction = new HashSet<String>();
    private Set<String> objectsRestriction = new HashSet<String>();
    protected Hashtable<String,String> prefixList = new Hashtable<String,String>();
    
    private static int count = 1;
	    
    public GraphPatternRendererVisitor( PrintWriter writer ){
		super( writer );
    }

    public static void resetVariablesName( String s, String o ) {
    	count = 1;
    	sub = "?" + s;
    	obj = "?" + o + count;    	
    }   
    
    public void resetVariables( String s, String o ) {
    	resetVariablesName(s, o);
    	strBGP = "";
		strBGP_Weaken = "";
		listBGP.clear();
		objectsRestriction.clear();
		flagRestriction = 0;
    }
    
    public String getGP(){
    	return strBGP;
    }
    
    public List<String> getBGP() {
    	return listBGP;
    }
    
    public String getPrefixDomain( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring(0, index+1);
    }
    
    public String getPrefixName( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring( index+1 );
    }
    
    public static String getNamespace(){
    	return "ns" + numberNs++;
    }
    
    public void createQueryFile( String dir, String query ) {
    	BufferedWriter out = null;
    	try {
	    FileWriter writer = new FileWriter( dir+"query"+number +".rq" );
	    out = new BufferedWriter( writer );
	    number++;
	    out.write( query );
	    if ( out != null ) // there was at least one file
		out.close();
	} catch(IOException ioe) {
	    System.err.println( ioe );
	}
    }

    public void visit( final ClassId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {
    		String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		prefixList.put( "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf" );
    		if( !prefixList.containsKey(prefix) && !prefix.equals("") ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}

			if ( !subjectsRestriction.isEmpty() ) {
				Iterator<String> listSub = subjectsRestriction.iterator();
				while ( listSub.hasNext() ) {
					String str = listSub.next();
					strBGP += str + " rdf:type " + shortCut + ":"+ tag + " ." + NL;			
					strBGP_Weaken += str + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
				}
				subjectsRestriction.clear();
			}
			else {
				strBGP += sub + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
				strBGP_Weaken += sub + " rdf:type " + shortCut + ":"+ tag + " ." + NL;
			}
    	}
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {    	
    	op = e.getOperator();
		if (op == Constructor.OR) {			
			int size = e.getComponents().size();			
			for ( final ClassExpression ce : e.getComponents() ) {
			    strBGP += "{" + NL;
			    strBGP_Weaken += "{" + NL;
			    ce.accept( this );			    			    
			    size--;
			    if( size != 0 ) {
			    	strBGP += "}" + " UNION " + NL;
			    	strBGP_Weaken += "}" + " UNION " + NL;
			    }
			    else {
			    	strBGP += "}" + NL;
			    	strBGP_Weaken += "}" + NL;
			    }
			}			
		}
		else if ( op == Constructor.NOT ) {			
		    strBGP += "FILTER (NOT EXISTS {" + NL;
		    strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final ClassExpression ce : e.getComponents() ) {			    
			    ce.accept( this );				
			}
		    strBGP += "})" + NL;
		    strBGP_Weaken += "})" + NL;
		}
		else {			
			for ( final ClassExpression ce : e.getComponents() ) {			    			    
			    ce.accept( this );
			    if ( !strBGP_Weaken.equals("") ) {
			    	listBGP.add(strBGP_Weaken);
			    	strBGP_Weaken = "";
			    }
			}
		}
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
    	String str = "";
    	instance = "";
	    value = "";
	    flagRestriction = 1;
	    c.getValue().accept( this );
	    flagRestriction = 0;
	    
	    if( !instance.equals("") )
	    	valueRestriction = instance;
	    else if( !value.equals("") )
	    	valueRestriction = value;
	    
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			opOccurence = ">";
			inClassRestriction = true;
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			opOccurence = "<";
			inClassRestriction = true;
		}
		flagRestriction = 1;
	    c.getRestrictionPath().accept( this );
	    flagRestriction = 0;
		String temp = obj;
		if ( inClassRestriction && !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			if (op == Constructor.COMP) {			
				String tmp = "";
				while ( listObj.hasNext() )
					tmp = listObj.next();
				str = "FILTER (" + tmp + opOccurence + valueRestriction + ")" +NL;		    
			}
			else {
				while ( listObj.hasNext() ) {
					str += "FILTER (" + listObj.next() + opOccurence + valueRestriction + ")" +NL;	
				}
			}
			strBGP += str;
			strBGP_Weaken += str;
		}
		valueRestriction = null;
		inClassRestriction = false;		
		obj = temp;
		if( op == Constructor.AND ){		
			if ( blanks ) {
	    		obj = "_:o" + ++count;
	    	}
	    	else {
	    		obj = "?o" + ++count;
	    	} 
		}
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {	
    	String str = "";
    	datatype = "";
    	inClassRestriction = true;
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;
			
			strBGP += str;
			strBGP_Weaken += str;
		}
		objectsRestriction.clear();
		inClassRestriction = false;
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {					
    	inClassRestriction = true;
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
    	Iterator<String> listObj = objectsRestriction.iterator();
    	while ( listObj.hasNext() ) {
			subjectsRestriction.add(listObj.next());			
		}
    	c.getDomain().accept( this );    	
    	objectsRestriction.clear();
    	inClassRestriction = false;
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
		String str="";
		inClassRestriction = true;
    	if( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "=";
		}
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = ">";
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "<";
		}
		flagRestriction = 1;
		c.getRestrictionPath().accept( this );	
		flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			if (op == Constructor.COMP) {			
				String tmp = "";
				while ( listObj.hasNext() )
					tmp = listObj.next();
				str += "FILTER(COUNT(" + tmp + ")" + opOccurence + nbCardinality + ")" +NL;	    
			}
			else{
				while ( listObj.hasNext() ) {
					str += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" +NL;	
				}
			}			
			
			strBGP += str;
			strBGP_Weaken += str;
		}
		nbCardinality = null;
		opOccurence = "";
		inClassRestriction = false;
    }
    
    public void visit( final PropertyId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {	
    		String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if( !prefixList.containsKey(prefix) && !prefix.equals("") ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
    		String temp = obj;
    		if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )
    			obj = "\"" + valueRestriction.toString() + "\"";
    		if ( flagRestriction == 1 && inClassRestriction )
				objectsRestriction.add(obj);
    		
		    strBGP += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
		    strBGP_Weaken += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
    		obj = temp;    		
		}
    }

    public void visit( final PropertyConstruction e ) throws AlignmentException {
    	op = e.getOperator();
		if ( op == Constructor.OR ){	
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" +NL;
			    strBGP_Weaken += "{" +NL;
			    re.accept( this );
			    size--;
			    if( size != 0 ){
			    	strBGP += "}" + " UNION " + NL;
			    	strBGP_Weaken += "}" + " UNION " + NL;
			    }
			    else {
			    	strBGP += "}" +NL;
			    	strBGP_Weaken += "}" +NL;
			    }
			}		    
			objectsRestriction.add( obj );
		}
		else if ( op == Constructor.NOT ) {
			strBGP += "FILTER (NOT EXISTS {" + NL;
			strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final PathExpression re : e.getComponents() ) {				
			    re.accept( this );			    
			}
			strBGP += "})" + NL;
			strBGP_Weaken += "})" + NL;
		}
		else if ( op == Constructor.COMP ){			
			int size = e.getComponents().size();
			String tempSub = sub;			
			if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
	    		obj = "_:o" + ++count;
	    	}
			for ( final PathExpression re : e.getComponents() ) {			    
			    re.accept( this );
			    size--;
			    if ( size != 0 ) {
			    	sub = obj;
			    	if( size == 1 && valueRestriction != null && !inClassRestriction ) {
			    		obj = "\"" + valueRestriction.toString() + "\"";
			    	}
			    	else {			    		
			    		if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
				    		obj = "_:o" + ++count;
				    	}
				    	else {
				    		obj = "?o" + ++count;
				    	}
			    	}
			    }
			}
			objectsRestriction.add( obj );
			sub = tempSub;
		}		
		else {			
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {			  
			    re.accept( this );
			    size--;	    
			    objectsRestriction.add( obj );
			    if( size != 0 && valueRestriction == null ){
			    	obj = "?o" + ++count;			    			    	
			    }
			    if ( !strBGP_Weaken.equals("") && !inClassRestriction ) {
			    	listBGP.add(strBGP_Weaken);
			    	strBGP_Weaken = "";
			    }
			}		
		}
		obj = "?o" + ++count;    	
    }

    public void visit( final PropertyValueRestriction c ) throws AlignmentException {
    	String str = "";
    	value = "";
    	uriType = "";
    	flagRestriction = 1;
		c.getValue().accept( this );
		flagRestriction = 0;
  		if ( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") = ";    		
    	}
    	else if ( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") > ";			
    	}
    	else {    		
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") < ";
    	}
    	str += "\"" + value + "\")" + NL;
		
		strBGP += str;
		strBGP_Weaken += str;
    	value = "";
    	uriType = "";
    }

    public void visit( final PropertyDomainRestriction c ) throws AlignmentException {
    	flagRestriction = 1;
		c.getDomain().accept( this );    	
    	flagRestriction = 0;	
    }

    public void visit( final PropertyTypeRestriction c ) throws AlignmentException {
    	String str = "";		
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;			
			strBGP += str;
			strBGP_Weaken += str;
		}
		objectsRestriction.clear();
    }
    
    public void visit( final RelationId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if ( !prefixList.containsKey(prefix) && !prefix.equals("") ) {
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
			strBGP += sub + " " + shortCut + ":"+ tag + "";
			strBGP_Weaken += sub + " " + shortCut + ":"+ tag + "";
		    
		    if ( op == Constructor.TRANSITIVE && flagRestriction == 1 ) {
		    	strBGP += "*";
		    	strBGP_Weaken += "*";
		    }
		    if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )			    
					obj = valueRestriction.toString();
		    if ( flagRestriction == 1 && inClassRestriction && op != Constructor.COMP )
					objectsRestriction.add(obj);
	    	
		    strBGP += " " + obj + " ." + NL;
		    strBGP_Weaken += " " + obj + " ." + NL;		    
		}
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
		op = e.getOperator();
		if ( op == Constructor.OR )  {	
			int size = e.getComponents().size();			
			if ( valueRestriction != null && !inClassRestriction )
				obj = valueRestriction.toString();
			String temp = obj;
			for ( final PathExpression re : e.getComponents() ) {			    
			    strBGP += "{" + NL;
			    strBGP_Weaken += "{" + NL;
			    re.accept( this );
			    obj = temp;
			    size--;
			    if ( size != 0 ) {
			    	strBGP += "}" + "UNION " + NL;
			    	strBGP_Weaken += "}" + "UNION " + NL;
			    }
			    else {
			    	strBGP += "}" + NL;
			    	strBGP_Weaken += "}" + NL;
			    }
			}			
			objectsRestriction.add( obj );
		}
		else if ( op == Constructor.NOT ) {		
			strBGP += "FILTER (NOT EXISTS {" + NL;
			strBGP_Weaken += "FILTER (NOT EXISTS {" + NL;
			for ( final PathExpression re : e.getComponents() ) {				
			    re.accept( this );			    
			}
			strBGP += "})" + NL;
			strBGP_Weaken += "})" + NL;
		}
		else if ( op == Constructor.COMP ) {
			int size = e.getComponents().size();
			String temp = sub;	
			if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
	    		obj = "_:o" + ++count;
	    	}
			for ( final PathExpression re : e.getComponents() ) {			    
			    re.accept( this );
			    size--;
			    if( size != 0 ) {
			    	sub = obj;
			    	if ( size == 1 && valueRestriction != null && !inClassRestriction ) {
			    		obj = valueRestriction.toString();
			    	}
			    	else {
			    		if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
				    		obj = "_:o" + ++count;
				    	}
				    	else {
				    		obj = "?o" + ++count;				    		
				    	}
			    		objectsRestriction.add( obj );
			    	}			    					    	
			    } 
			}			
			sub = temp;
		}
		else if ( op == Constructor.INVERSE ) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    re.accept( this );
			    sub = tempSub;
			}
		}
		else if ( op == Constructor.SYMMETRIC ) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" + NL;			    
			    re.accept( this );
			    objectsRestriction.add( obj );
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    strBGP += "} UNION {" + NL;			    
			    re.accept( this );
			    objectsRestriction.add( obj );
			    strBGP +="}" + NL;			    
			}
			sub = tempSub;
		}
		else if (op == Constructor.TRANSITIVE){						
			for ( final PathExpression re : e.getComponents() ) {			    
			    flagRestriction = 1;
				re.accept( this );
				flagRestriction = 0;
			}
		}
		else if ( op == Constructor.REFLEXIVE ) {			
			for ( final PathExpression re : e.getComponents() ) {			    
			    strBGP += "{" + NL;
				re.accept( this );
			    strBGP += "} UNION {" + NL + "FILTER(" + sub + "=" + obj + ")" + NL + "}";
			}
		}
		else {			
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = valueRestriction.toString();
			for ( final PathExpression re : e.getComponents() ) {			    
			    re.accept( this );
			    size--;
			    objectsRestriction.add( obj );
			    if ( size != 0 && valueRestriction == null ) {
			    	obj = "?o" + ++count;			    			    	
			    }
			    if ( !strBGP_Weaken.equals("") && !inClassRestriction ) {
			    	listBGP.add(strBGP_Weaken);
			    	strBGP_Weaken = "";
			    }
			}		
		}
		obj = "?o" + ++count;    	
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
    	sub = obj;
    	flagRestriction = 1;		
    	c.getCoDomain().accept( this );    	
    	flagRestriction = 0;
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
    	flagRestriction = 1;
		c.getDomain().accept( this );
    	flagRestriction = 0;
    }

    public void visit( final InstanceId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if ( !prefixList.containsKey( prefix) ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
			if ( flagRestriction != 1 )
				strBGP += shortCut + ":"+ tag + " ?p ?o1 ." +NL;
			else
				instance = shortCut + ":"+ tag;
		}
    }
    
    public void visit( final Value e ) throws AlignmentException {
    	if (e.getType() != null) {
	    	String str = e.getType().toString();
	    	int index;
	    	if ( str.contains("#") )
	    		index = str.lastIndexOf("#");
	    	else
	    		index = str.lastIndexOf("/");
	    	uriType = str.substring( index+1 );
    	}
    	value = e.getValue();
    	if ( uriType != null && uriType.equals("") ) {
    		uriType = "string";
    	}
    	
    }
	
    public void visit( final Apply e ) throws AlignmentException {}

    public void visit( final Transformation transf ) throws AlignmentException {}

    public void visit( final Datatype e ) throws AlignmentException {
    	int index;
    	if ( e.getType().contains("#") )
    		index = e.getType().lastIndexOf("#");
    	else
    		index = e.getType().lastIndexOf("/");
    	datatype = e.getType().substring( index+1 );
    }

}
