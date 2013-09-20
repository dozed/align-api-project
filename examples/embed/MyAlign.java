/*
 * $Id: MyAlign.java 851 2008-09-27 08:58:49Z euzenat $
 *
 * Copyright (C)  INRIA 2003-2005, 2008
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

// Align API
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

// Align API Implementation
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment;
import fr.inrialpes.exmo.align.impl.method.SMOANameAlignment;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor;

// Java classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;

/** 
Compile with:
javac -cp .:../../lib/procalign.jar MyAlign.java

Run with:
java -cp .:../../lib/procalign.jar MyAlign
 */

public class MyAlign {

    public static void main(String[] args) {
	try {
	    URI uri1 = new URI("file:../rdf/edu.umbc.ebiquity.publication.owl");
	    URI uri2 = new URI("file:../rdf/edu.mit.visus.bibtex.owl");

	    Parameters p = new BasicParameters();
	    AlignmentProcess A1 = new SubsDistNameAlignment();
	    A1.init( uri1, uri2 );
	    AlignmentProcess A2 = new SMOANameAlignment();
	    A2.init( uri1, uri2 ); 
	    //AlignmentProcess A3 = new NameAndPropertyAlignment();
	    AlignmentProcess A3 = new EditDistNameAlignment();
	    A3.init( uri1, uri2 ); 
	    A1.align((Alignment)null,p); A1.cut("prop", .5); 
	    A2.align((Alignment)null,p); A3.align(A2,p); 
	    Evaluator E = new PRecEvaluator(A1, A3); 
	    E.eval(p); 
	    PrintWriter pw = new PrintWriter (
			       new BufferedWriter(
				 new OutputStreamWriter( System.out, "UTF-8" )),
			       true);
	    AlignmentVisitor V = new SWRLRendererVisitor( pw ); 
	    if ( ((PRecEvaluator)E).getPrecision() > .6 ) A3.render(V);
	    pw.flush(); 
	    pw.close(); // necessary when the program is really embedded
	} catch (Exception e) { e.printStackTrace(); };
    }
}
