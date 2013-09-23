/*
 * $Id: DiffEvaluator.java 1734 2012-07-09 14:53:48Z euzenat $
 *
 * Copyright (C) INRIA, 2010, 2012
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

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.io.PrintWriter;
import java.net.URI;


/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall/Fallout. The first alignment
 * is thus the expected one.
 *
 * @version $Id: DiffEvaluator.java 1734 2012-07-09 14:53:48Z euzenat $ 
 */

public class DiffEvaluator extends BasicEvaluator implements Evaluator {

    Set<Cell> truepositive;
    Set<Cell> falsenegative;
    Set<Cell> falsepositive;

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     * The two parameters are transformed into URIAlignment before being processed
     * Hence, if one of them is modified after initialisation, this will not be taken into account.
     **/
    public DiffEvaluator(Alignment align1, Alignment align2) throws AlignmentException {
	super(((BasicAlignment)align1).toURIAlignment(), ((BasicAlignment)align2).toURIAlignment());
	truepositive = new HashSet<Cell>();
	falsenegative = new HashSet<Cell>();
	falsepositive = new HashSet<Cell>();
    }

    public void init(){
	truepositive = new HashSet<Cell>();
	falsenegative = new HashSet<Cell>();
	falsepositive = new HashSet<Cell>();
    }

    public void diff(){
	// True and false positives
	try {
	    boolean has = false;
	    // Alignment
	    for ( Cell c1 : this.align2 ) {
		URI uri1_1 = c1.getObject1AsURI();
		URI uri1_2 = c1.getObject2AsURI();
		String rel1 = c1.getRelation().getRelation().toString();
		has = false; 
		// Reference alignment
		for( Cell c2 : this.align1) {
		    URI uri2_1 = c2.getObject1AsURI();
		    URI uri2_2 = c2.getObject2AsURI();
		    String rel2 = c2.getRelation().getRelation().toString();
		    if ( (uri1_1.toString().equals(uri2_1.toString())) && 
			 (uri1_2.toString().equals(uri2_2.toString())) && 
			 (rel1.equals(rel2))
			 ) {
			truepositive.add(c1);
			has = true;
			break;
		    }
		}
		if (!has) {
		    falsepositive.add(c1);  
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(); 
	}
	
	// False negative
	try {
	    boolean has;
	    // Reference alignment
	    for ( Cell c1 : this.align1 ) {
		URI uri1_1 = c1.getObject1AsURI();
		URI uri1_2 = c1.getObject2AsURI();
		String rel1 = c1.getRelation().getRelation().toString();
		has = false;
		// Alignment
		for( Cell c2 : this.align2) {
		    URI uri2_1 = c2.getObject1AsURI();
		    URI uri2_2 = c2.getObject2AsURI();
		    String rel2 = c2.getRelation().getRelation().toString();
		    if ( (uri1_1.toString().equals(uri2_1.toString())) && 
			 (uri1_2.toString().equals(uri2_2.toString())) &&
			 (rel1.equals(rel2))    
			 ) {
			has = true;
			break;
		    }
		}
		if (!has) {
		    falsenegative.add(c1); 
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(); 
	}
    }

   public double eval( Properties params ) throws AlignmentException {
	init();
	diff();
	return 1.0;
    }

    public double eval( Properties params, Object cache ) throws AlignmentException {
	return eval( params );
    }

    public String HTMLString (){
	String result = "";
	result += "  <div  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"' typeof=\""+Namespace.ATLMAP.shortCut+":output\" href=''>";
        result += "     <dl>";
        result += writeCellsHTML( truepositive,  "<span style=\"color: green\">Correct correspondences</span>");
        result += writeCellsHTML( falsepositive, "<span style=\"color: red\">Incorrect correspondences</span>");
        result += writeCellsHTML( falsenegative, "<span style=\"color: orange\">Missing correspondences</span>");
        result += "     </dl>\n  </div>\n";
        return result;
    }

    private String writeCellsHTML( Set<Cell> set, String what ) { 
	String result = ""; 
	try {
	    result += "              <dt> " + what + "</dt><dd>\n";
	    for ( Cell c : set ){
		result +=  "                        " + c.getObject1AsURI() + " " + c.getRelation().getRelation() + " " +  c.getObject2AsURI() + "<br />\n"; 
	    }
	    result += "</dd>\n";
	} catch (AlignmentException e) {
	    e.printStackTrace(); 
	}
	return result;  
    }

    public void printAsCells ( String what, Set<Cell> set, PrintWriter writer ) {
	writer.println("  <"+what+" rdf:parseType=\"Collection\">");
	for ( Cell c : set ){
	    try {
		writer.println("    <Cell>");
		writer.println("      <entity1 rdf:resource=\""+c.getObject1AsURI()+"\">");
		writer.println("      <entity2 rdf:resource=\""+c.getObject2AsURI()+"\">");
		writer.println("      <relation>"+c.getRelation().getRelation()+"</relation>");
		writer.println("    </Cell>");
	    } catch (AlignmentException e) {
		e.printStackTrace(); 
	    }
	}
	writer.println("  </"+what+">");
    }

    public void write( PrintWriter writer ) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<DiffAlignment>");
	// Should be good to add the reference alignments
	printAsCells( "truePositive", truepositive, writer );
	printAsCells( "falsePositive", falsepositive, writer );
	printAsCells( "falseNegative", falsenegative, writer );
	writer.println("</DiffAlignment>");
    }

    public Properties getResults() {
	Properties results = new Properties();
	results.setProperty( "true positive", Integer.toString( truepositive.size() ) );
	results.setProperty( "false negative", Integer.toString( falsenegative.size() ) );
	results.setProperty( "false positive", Integer.toString( falsepositive.size() ) );
	return results;
    }

    public Set<Cell> getTruePositive() { return truepositive; }
    public Set<Cell> getFalseNegative() { return falsenegative; }
    public Set<Cell> getFalsePositive() { return falsepositive; }
}

