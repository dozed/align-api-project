/*
 * $Id: WeightedPRecEvaluator.java 1494 2010-07-23 14:43:36Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2013
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

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
import java.net.URI;


/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall. The first alignment
 * is thus the expected one.
 *
 * Basic relation sensitivity has been implemented
 *
 * @author Jerome Euzenat
 * @version $Id: PRecEvaluator.java 1494 2010-07-23 14:43:36Z euzenat $ 
 */

public class WeightedPREvaluator extends BasicEvaluator implements Evaluator {

    protected double precision = 1.;

    protected double recall = 1.;

    protected double overall = 0.;

    protected double fmeasure = 0.;

    protected boolean relsensitive = false;

    protected long time = 0;

    protected double nbexpected = 0.;

    protected double nbfound = 0.;

    protected double nbcorrect1 = 0.; // nb of cells of reference correctly identified

    protected double nbcorrect2 = 0.; // nb of cells of alignment correctly identified

    /** Creation
     * Initiate Evaluator for precision and recall
     * @param align1 : the reference alignment
     * @param align2 : the alignment to evaluate
     * The two parameters are transformed into URIAlignment before being processed
     * Hence, if one of them is modified after initialisation, this will not be taken into account.
     **/
    public WeightedPREvaluator(Alignment align1, Alignment align2) throws AlignmentException {
	super(((BasicAlignment)align1).toURIAlignment(), ((BasicAlignment)align2).toURIAlignment());
    }

    public void init(){
	precision = 1.;
	recall = 1.;
	overall = 0.;
	fmeasure = 0.;
	time = 0;
	nbexpected = 0.;
	nbfound = 0.;
	nbcorrect1 = 0.;
	nbcorrect2 = 0.;
	result = 1.;
    }

    /**
     *
     * The formulas are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     *
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     *
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     */
    public double eval( Properties params ) throws AlignmentException {
	init();
	//for ( Cell c2 : align2 ) nbfound += c2.getStrength();
	if ( params.getProperty("relations") != null ) relsensitive = true;

	for ( Cell c1 : align1 ) {
	    URI uri1 = c1.getObject2AsURI();
	    nbexpected += c1.getStrength();
	    Set<Cell> s2 = align2.getAlignCells1( c1.getObject1() );
	    double diff = -1.0;
	    if( s2 != null ){
		for( Cell c2 : s2 ) {
		    URI uri2 = c2.getObject2AsURI();	
		    if ( uri1.equals( uri2 )
			 && ( !relsensitive || c1.getRelation().equals( c2.getRelation() ) ) ) {
			diff = c1.getStrength() - c2.getStrength();
			nbcorrect1 += ((diff>0.)?diff:-diff); //1. -
			break;
		    }
		}
	    }
	    if ( diff == -1.0 ) nbcorrect1 += c1.getStrength(); // the c1 not found
	}
	for ( Cell c2 : align2 ) {
	    URI uri2 = c2.getObject2AsURI();
	    nbfound += c2.getStrength();
	    Set<Cell> s1 = align1.getAlignCells1( c2.getObject1() );
	    double diff = -1.0;
	    if( s1 != null ){
		for( Cell c1 : s1 ) {
		    URI uri1 = c1.getObject2AsURI();	
		    if ( uri2.equals( uri1 )
			 && ( !relsensitive || c1.getRelation().equals( c2.getRelation() ) ) ) {
			diff = c1.getStrength() - c2.getStrength();
			nbcorrect2 += ((diff>0.)?diff:-diff); //1. -
			break;
		    }
		}
	    }
	    if ( diff == -1.0 ) nbcorrect2 += c2.getStrength(); // the c2 not found
	}

	// What is the definition if:
	// nbfound is 0 (p is 1., r is 0)
	// nbexpected is 0 [=> nbcorrect is 0] (r=1, p=0)
	// precision+recall is 0 [= nbcorrect is 0]
	// precision is 0 [= nbcorrect is 0]
	if ( nbfound != 0. ) precision = 1. - (nbcorrect2 / nbfound);
	if ( nbexpected != 0. ) recall = 1. - (nbcorrect1 / nbexpected);
	return computeDerived();
    }
    public double eval( Properties params, Object cache ) throws AlignmentException {
	return eval( params );
    }

    protected double computeDerived() {
	if ( precision != 0. ) {
	    fmeasure = 2 * precision * recall / (precision + recall);
	    overall = recall * (2 - (1 / precision));
	    result = recall / precision;
	} else { result = 0.; }
	String timeExt = align2.getExtension( Namespace.ALIGNMENT.uri, Annotations.TIME );
	if ( timeExt != null ) time = Long.parseLong(timeExt);
	//System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
	return (result);
    }

    public String HTMLString (){
	String result = "";
	result += "  <div  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"' typeof=\""+Namespace.ATLMAP.shortCut+":output\" href=''>";
	result += "    <dl>";
	//if ( ) {
	//    result += "    <dt>algorithm</dt><dd property=\""+Namespace.ATLMAP.shortCut+":algorithm\">"+align1.get+"</dd>";
	//}
	try {
	    result += "    <dt>input1</dt><dd rel=\""+Namespace.ATLMAP.shortCut+":input1\" href=\""+align1.getOntology1URI()+"\">"+align1.getOntology1URI()+"</dd>";
	    result += "    <dt>input2</dt><dd rel=\""+Namespace.ATLMAP.shortCut+":input2\" href=\""+align1.getOntology2URI()+"\">"+align1.getOntology2URI()+"</dd>";
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// result += "    <"+Namespace.ATLMAP.shortCut+":falseNegative>");
	// result += "    <"+Namespace.ATLMAP.shortCut+":falsePositive>");
	result += "    <dt>precision</dt><dd property=\""+Namespace.ATLMAP.shortCut+":precision\">"+precision+"</dd>\n";
	result += "    <dt>recall</dt><dd property=\""+Namespace.ATLMAP.shortCut+":recall\">"+recall+"</dd>\n";
	result += "    <dt>F-measure</dt><dd property=\""+Namespace.ATLMAP.shortCut+":fMeasure\">"+fmeasure+"</dd>\n";
	result += "    <dt>O-measure</dt><dd property=\""+Namespace.ATLMAP.shortCut+":oMeasure\">"+overall+"</dd>\n";
	if ( time != 0 ) result += "    <dt>time</dt><dd property=\""+Namespace.ATLMAP.shortCut+":time\">"+time+"</dd>\n";
    	result += "    <dt>result</dt><dd property=\""+Namespace.ATLMAP.shortCut+":result\">"+result+"</dd>\n";
	result += "  </dl>\n  </div>\n";
return result;
    }

    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'\n  xmlns:"+Namespace.ATLMAP.shortCut+"='"+Namespace.ATLMAP.prefix+"'>");
	writer.println("  <"+Namespace.ATLMAP.shortCut+":output "+SyntaxElement.RDF_ABOUT.print()+"=''>");
	//if ( ) {
	//    writer.println("    <"+Namespace.ATLMAP.shortCut+":algorithm "+SyntaxElement.RDF_RESOURCE.print()+"=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
	//}
	try {
	    writer.println("    <"+Namespace.ATLMAP.shortCut+":input1 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+align1.getOntology1URI()+"\"/>");
	    writer.println("    <"+Namespace.ATLMAP.shortCut+":input2 "+SyntaxElement.RDF_RESOURCE.print()+"=\""+align1.getOntology2URI()+"\"/>");
	} catch (AlignmentException e) { e.printStackTrace(); };
	// Other missing items (easy to get)
	// writer.println("    <"+Namespace.ATLMAP.shortCut+":falseNegative>");
	// writer.println("    <"+Namespace.ATLMAP.shortCut+":falsePositive>");
	writer.print("    <"+Namespace.ATLMAP.shortCut+":precision>");
	writer.print(precision);
	writer.print("</"+Namespace.ATLMAP.shortCut+":precision>\n    <"+Namespace.ATLMAP.shortCut+":recall>");
	writer.print(recall);
	writer.print("</"+Namespace.ATLMAP.shortCut+":recall>\n    <"+Namespace.ATLMAP.shortCut+":fMeasure>");
	writer.print(fmeasure);
	writer.print("</"+Namespace.ATLMAP.shortCut+":fMeasure>\n    <"+Namespace.ATLMAP.shortCut+":oMeasure>");
	writer.print(overall);
	writer.print("</"+Namespace.ATLMAP.shortCut+":oMeasure>\n");
	if ( time != 0 ) writer.print("    <time>"+time+"</time>\n");
    	writer.print("    <result>"+result);
	writer.print("</result>\n  </"+Namespace.ATLMAP.shortCut+":output>\n</"+SyntaxElement.RDF.print()+">\n");
    }

    public Properties getResults() {
	Properties results = new Properties();
	results.setProperty( "precision", Double.toString( precision ) );
	results.setProperty( "recall", Double.toString( recall ) );
	results.setProperty( "overall", Double.toString( overall ) );
	results.setProperty( "fmeasure", Double.toString( fmeasure ) );
	results.setProperty( "nbexpected", Double.toString( nbexpected ) );
	results.setProperty( "nbfound", Double.toString( nbfound ) );
	results.setProperty( "true positive", Double.toString( nbcorrect1 ) );
	if ( time != 0 ) results.setProperty( "time", Long.toString( time ) );
	return results;
    }

    public double getPrecision() { return precision; }
    public double getRecall() {	return recall; }
    public double getOverall() { return overall; }
    public double getFallout() throws AlignmentException { throw new AlignmentException("Fallout computation to be deprecated (version 4.2)"); }
    public double getNoise() { return 1.-precision; }
    public double getSilence() { return 1.-precision; }
    public double getFmeasure() { return fmeasure; }
    public double getExpected() { return nbexpected; }
    public double getFound() { return nbfound; }
    // JE 2013: does not fit in WGroupEval anymore
    public double getCorrect() { return nbcorrect1; }
    public double getCorrect1() { return nbcorrect1; }
    public double getCorrect2() { return nbcorrect2; }
    public long getTime() { return time; }
}

