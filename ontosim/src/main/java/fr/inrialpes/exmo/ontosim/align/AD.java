/**
 *   Copyright 2010-2012 INRIA, Université Pierre Mendès France
 *   Pieces of code gently provided by Mathieu d'Aquin (Open university)
 *   
 *   AD.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package fr.inrialpes.exmo.ontosim.align;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.OntologyNetwork;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory;

/**
 * This class provides all what is necessary for computing Agreement/Disagreement
 * measures between ontologies as defined by Mathieu d'Aquin in his K-Cap paper
 * and refined (and to be refined by ourselves).
 *
 * It is fully reliant on Jena. Hence the provided 
 */

public abstract class AD extends AbstractAlignmentSpaceMeasure<HeavyLoadedOntology<OntModel>> {

    protected String[] lrelations = {"subClassOf",  "equivalentClass", "domain", "range", "disjointWith", "type", "sameAs", "differentFrom", "subPropertyOf"};
    private String[] rrelations = {"disjointWith", "equivalentClass", "sameAs", "differentFrom"};

    //public HashMap<String,Set<String>> correspondences = new HashMap<String,Set<String>>();
    private final boolean silent = false;

    public AD() {
	super();
	String df = OntologyFactory.getDefaultFactory();
	if ( !df.contains("JENA") && !silent ) {
	    System.err.println("Your default ontology factory does not seem to be Jena" );
	    System.err.println("The Agreement/Disagreement measures will not work.");
	    System.err.println("For changing this, use:");
	    System.err.println("OntologyFactory.setDefaultFactory()");
	    System.err.println("To discard this message, recompile ontosim's AD.java");
	}
    }

    public AD(OntologyNetwork noo) {
	super(noo);
	setAlignmentSpace(noo);
    }

    public Set<Cell> getImages(String uri, LoadedOntology<?> o1, LoadedOntology<?> o2) {
	HashSet<Cell> res = new HashSet<Cell>();
	URI c = URI.create(uri);
	Set<Alignment> aligns = network.getAlignments(o1.getURI(), o2.getURI());
	for (Alignment a : aligns) {
	    try {
		Set<Cell> c1 = a.getAlignCells1(c);
		if (c1!=null) res.addAll(c1);
	    } catch (AlignmentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return res;
    }

    public void setAlignmentSpace(OntologyNetwork noo) {
	super.setAlignmentSpace(noo);
	/*for (Alignment a : noo.getAlignments()) {
	    Enumeration<Cell> cells = a.getElements();
	    while (cells.hasMoreElements()) {
		Cell c = cells.nextElement();
		if (c.getRelation() instanceof EquivRelation) {
		    try {
			Set<String> trg = correspondences.get(c.getObject1AsURI().toString());
			if (trg==null) {
			    trg= new HashSet<String>();
			    correspondences.put(c.getObject1AsURI().toString(), trg);
			}
			trg.add(c.getObject2AsURI().toString());

			trg = correspondences.get(c.getObject2AsURI().toString());
			if (trg==null) {
			    trg= new HashSet<String>();
			    correspondences.put(c.getObject2AsURI().toString(), trg);
			}
			trg.add(c.getObject1AsURI().toString());

		    } catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		}
	    }
	}*/
	try {
	    network.invert();
	} catch (AlignmentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public boolean addAlignment( Alignment a ) throws AlignmentException {
	if (super.addAlignment(a) && super.addAlignment(a.inverse())) {
	    /*Enumeration<Cell> cells = a.getElements();
	    while (cells.hasMoreElements()) {
		Cell c = cells.nextElement();
		if (c.getRelation() instanceof EquivRelation) {
		    try {
			Set<String> trg = correspondences.get(c.getObject1AsURI().toString());
			if (trg==null) {
			    trg= new HashSet<String>();
			    correspondences.put(c.getObject1AsURI().toString(), trg);
			}
			trg.add(c.getObject2AsURI().toString());

			trg = correspondences.get(c.getObject2AsURI().toString());
			if (trg==null) {
			    trg= new HashSet<String>();
			    correspondences.put(c.getObject2AsURI().toString(), trg);
			}
			trg.add(c.getObject1AsURI().toString());
		    } catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		}
	    }*/
	    return true;
	}
	return false;
    }

    private final static double A1 = 0.25;
    private final static double A2 = 0.75;
    private final static double D1 = 0.75;
    private final static double D2 = 0.25;

    protected final static HashMap<String[], double[]> agreementValues = new HashMap<String[], double[]>();
    private final static HashMap<String[], double[]> disagreementValues = new HashMap<String[], double[]>();
    static{
	createAgreementValues() ;
	createDisagreementValues();
    }

    private final static void createAgreementValues() {
	double[] l1 =     {0  ,0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l1RM = new String[0];
	double[] l2 =     {1  ,A2  , 0, 0, 0   , A2  , A1  , 0   , A1  , 0}; String[] l2RM = {"subClassOf"};
	double[] l3 =     {A1 , A2 , 0, 0, 0   , 0   , A1  , 0   , 0   , 0}; String[] l3RM = {"subClassOf-1"};
	double[] l4 =     {A2 , 1  , 0, 0, 0   , A1  , A2  , 0   , A1  , 0}; String[] l4RM = {"equivalentClass"};
	double[] l5 =     {0  , 0  , 0, 0, 1   , 0   , 0   , A2  , 0   , 0}; String[] l5RM = {"disjointWith"};
	double[] l6 =     {0  , 0  , 1, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l6RM = {"domain"};
	double[] l7 =     {0  , 0  , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l7RM = {"domain-1"};
	double[] l8 =     {0  , 0  , 0, 1, 0   , 0   , 0   , 0   , 0   , 0}; String[] l8RM = {"range"};
	double[] l9 =     {0  , 0  , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l9RM = {"range-1"};
	double[] l10 =    {0 , 0   , 1, 1, 0   , 0   , 0   , 0   , 0   , 0}; String[] l10RM = {"domain", "range"};
	double[] l10b =   {0 , 0   , 1, 1, 0   , 0   , 0   , 0   , 0   , 0}; String[] l10bRM = {"range", "domain"};
	double[] l11 =    {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l11RM = {"domain-1", "range-1"};
	double[] l11b =   {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l11bRM = {"range-1", "domain-1"};	
	double[] l12 =    {A2, A1  , 0, 0, 0   , A1  , A1  , 0   , 1   , 0}; String[] l12RM = {"subPropertyOf"};
	double[] l13 =    {0 , A1  , 0, 0, 0   , 0   , A1  , 0   , A1  , 0}; String[] l13RM = {"subPropertyOf-1"};
	double[] l14 =    {A2, A1  , 0, 0, 0   , 1   , A1  , 0   , A1  , 0}; String[] l14RM = {"type"};
	double[] l15 =    {0 , A1  , 0, 0, 0   , A1  , A1  , 0   , 0   , 0}; String[] l15RM = {"type-1"};
	double[] l16 =    {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l16RM = {"sameAs"};
	double[] l17 =    {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 1}; String[] l17RM = {"matching"};
	double[] l18 =    {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l18RM = {"matching-1"};
	double[] l19 =    {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0   , 0}; String[] l19RM = {"mismatching"};
	double[] l20 =    {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 1}; String[] l20RM = {"sameAs", "matching"};
	double[] l20b =   {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l20bRM = {"matching", "sameAs"};
	double[] l21 =    {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l21RM = {"sameAs", "matching-1"};
	double[] l21b =   {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l21bRM = {"matching-1", "sameAs"};
	double[] l22 =    {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l22RM = {"sameAs", "mismatching"};
	double[] l22b =   {A1, A2  , 0, 0, 0   , A1  , 1   , 0   , A1  , 0}; String[] l22bRM = {"mismatching", "sameAs"};
	double[] l23 =    {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 0}; String[] l23RM = {"diff"};
	double[] l24 =    {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 1}; String[] l24RM = {"diff", "matching"};
	double[] l24b =   {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 1}; String[] l24bRM = {"matching", "diff"};
	double[] l25 =    {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 0}; String[] l25RM = {"diff", "matching-1"};
	double[] l25b =   {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 0}; String[] l25bRM = {"matching-1", "diff"};
	double[] l26 =    {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 0}; String[] l26RM = {"diff", "mismatching"};
	double[] l26b =   {0 , 0   , 0, 0, A2  , 0   , 0   , 1   , 0   , 0}; String[] l26bRM = {"mismatching", "diff"};
	double[] l27 =    {0 , 0   , 0, 0, 0   , 0   , 0   , 0   , 0  , 1};  String[] l27RM = {"matching","matching-1"};


	agreementValues.put(l1RM, l1);
	agreementValues.put(l2RM, l2);
	agreementValues.put(l3RM, l3);
	agreementValues.put(l4RM, l4);
	agreementValues.put(l5RM, l5);
	agreementValues.put(l6RM, l6);
	agreementValues.put(l7RM, l7);
	agreementValues.put(l8RM, l8);
	agreementValues.put(l9RM, l9);
	agreementValues.put(l10RM, l10);
	agreementValues.put(l10bRM, l10b);
	agreementValues.put(l11RM, l11);
	agreementValues.put(l11bRM, l11b);
	agreementValues.put(l12RM, l12);
	agreementValues.put(l13RM, l13);
	agreementValues.put(l14RM, l14);
	agreementValues.put(l15RM, l15);
	agreementValues.put(l16RM, l16);
	agreementValues.put(l17RM, l17);
	agreementValues.put(l18RM, l18);
	agreementValues.put(l19RM, l19);
	agreementValues.put(l20RM, l20);
	agreementValues.put(l20bRM, l20b);
	agreementValues.put(l21RM, l21);
	agreementValues.put(l21bRM, l21b);
	agreementValues.put(l22RM, l22);
	agreementValues.put(l22bRM, l22b);
	agreementValues.put(l23RM, l23);
	agreementValues.put(l24RM, l24);
	agreementValues.put(l24bRM, l24b);
	agreementValues.put(l25RM, l25);
	agreementValues.put(l25bRM, l25b);
	agreementValues.put(l26RM, l26);
	agreementValues.put(l26bRM, l26b);
	agreementValues.put(l27RM, l27);

    }

    private final static void createDisagreementValues() {
	double[] l1 = {D1, D1, D2, D2, D1, D1, D1, D1, D1, D2}; String[] l1RM = new String[0];
	double[] l2 = {0, D2, D1, D1, 1, D2, D1, 1, D2, D1}; String[] l2RM = {"subClassOf"};
	double[] l3 = {D1, D2, D1, D1, 1, D1, D1, 1, D1, D1}; String[] l3RM = {"subClassOf-1"};
	double[] l4 = {D2, 0, D1, D1, 1, D1, D2, 1, D1, D1}; String[] l4RM = {"equivalentClass"};
	double[] l5 = {1, 1, D1, D1, 0, 1, 1, D2, 1, D1}; String[] l5RM = {"disjointWith"};
	double[] l6 = {D1, D1, 0, 0, D1, D1, D1, D1, D1, D1}; String[] l6RM = {"domain"};
	double[] l7 = {D1, D1, D1, D1, D1, D1, D1, D1, D1, D1}; String[] l7RM = {"domain-1"};
	double[] l8 = {D1, D1, 0, 0, D1, D1, D1, D1, D1, D1}; String[] l8RM = {"range"};
	double[] l9 = {D1, D1, D1, D1, D1, D1, D1, D1, D1, D1}; String[] l9RM = {"range-1"};
	double[] l10 = {D1, D1, 0, 0, D1, D1, D1, D1, D1, D1}; String[] l10RM = {"domain", "range"};
	double[] l10b = {D1, D1, 0, 0, D1, D1, D1, D1, D1, D1}; String[] l10bRM = {"range", "domain"};
	double[] l11 = {D1, D1, D1, D1, D1, D1, D1, D1, D1, D1}; String[] l11RM = {"domain-1", "range-1"};
	double[] l11b = {D1, D1, D1, D1, D1, D1, D1, D1, D1, D1}; String[] l11bRM = {"range-1", "domain-1"};	
	double[] l12 = {D2, D1, D1, D1, 1, D1, D1, 1, 0, D1}; String[] l12RM = {"subPropertyOf"};
	double[] l13 = {D1, D1, D1, D1, 1, D1, D1, 1, D1, D1}; String[] l13RM = {"subPropertyOf-1"};
	double[] l14 = {D2, D1, D1, D1, 1, 0, D1, 1, D1, D1}; String[] l14RM = {"type"};
	double[] l15 = {D1, D1, D1, D1, 1, D1, D1, 1, D1, D1}; String[] l15RM = {"type-1"};
	double[] l16 = {D1, D2, D1, D1, 1, D1, 0, 1, D1, D1}; String[] l16RM = {"sameAs"};
	double[] l17 = {D1, D1, D1, D1, D1, D1, 0, 0, D1, 0}; String[] l17RM = {"matching"};
	double[] l18 = {D1, D1, D1, D1, D1, D1, 0, 0, D1, D1}; String[] l18RM = {"matching-1"};
	double[] l19 = {D1, D1, D1, D1, D1, D1, 0, 0, D1, D2}; String[] l19RM = {"mismatching"};
	double[] l20 = {D1, D2, D1, D1, 1, D1, 0, 1, D1, 0}; String[] l20RM = {"sameAs", "matching"};
	double[] l20b = {D1, D2, D1, D1, 1, D1, 0, 1, D1, 0}; String[] l20bRM = {"matching", "sameAs"};
	double[] l21 = {D1, D2, D1, D1, 1, D1, 0, 1, D1, D1}; String[] l21RM = {"sameAs", "matching-1"};
	double[] l21b = {D1, D2, D1, D1, 1, D1, 0, 1, D1, D1}; String[] l21bRM = {"matching-1", "sameAs"};
	double[] l22 = {D1, D2, D1, D1, 1, D1, 0, 1, D1, D1}; String[] l22RM = {"sameAs", "mismatching"};
	double[] l22b = {D1, D2, D1, D1, 1, D1, 0, 1, D1, D2}; String[] l22bRM = {"mismatching", "sameAs"};
	double[] l23 = {1, 1, D1, D1, D2, 1, 1, 0, 1, D2}; String[] l23RM = {"diff"};
	double[] l24 = {1, 1, D1, D1, D2, 1, 1, 0, 1, 0}; String[] l24RM = {"diff", "matching"};
	double[] l24b = {1, 1, D1, D1, D2, 1, 1, 0, 1, 0}; String[] l24bRM = {"matching", "diff"};
	double[] l25 = {1, 1, D1, D1, D2, 1, 1, 0, 1, D1}; String[] l25RM = {"diff", "matching-1"};
	double[] l25b = {1, 1, D1, D1, D2, 1, 1, 0, 1, D1}; String[] l25bRM = {"matching-1", "diff"};
	double[] l26 = {1, 1, D1, D1, D2, 1, 1, 0, 1, D2}; String[] l26RM = {"diff", "mismatching"};
	double[] l26b = {1, 1, D1, D1, D2, 1, 1, 0, 1, D2}; String[] l26bRM = {"mismatching", "diff"};

	disagreementValues.put(l1RM, l1);
	disagreementValues.put(l2RM, l2);
	disagreementValues.put(l3RM, l3);
	disagreementValues.put(l4RM, l4);
	disagreementValues.put(l5RM, l5);
	disagreementValues.put(l6RM, l6);
	disagreementValues.put(l7RM, l7);
	disagreementValues.put(l8RM, l8);
	disagreementValues.put(l9RM, l9);
	disagreementValues.put(l10RM, l10);
	disagreementValues.put(l10bRM, l10b);
	disagreementValues.put(l11RM, l11);
	disagreementValues.put(l11bRM, l11b);
	disagreementValues.put(l12RM, l12);
	disagreementValues.put(l13RM, l13);
	disagreementValues.put(l14RM, l14);
	disagreementValues.put(l15RM, l15);
	disagreementValues.put(l16RM, l16);
	disagreementValues.put(l17RM, l17);
	disagreementValues.put(l18RM, l18);
	disagreementValues.put(l19RM, l19);
	disagreementValues.put(l20RM, l20);
	disagreementValues.put(l20bRM, l20b);
	disagreementValues.put(l21RM, l21);
	disagreementValues.put(l21bRM, l21b);
	disagreementValues.put(l22RM, l22);
	disagreementValues.put(l22bRM, l22b);
	disagreementValues.put(l23RM, l23);
	disagreementValues.put(l24RM, l24);
	disagreementValues.put(l24bRM, l24b);
	disagreementValues.put(l25RM, l25);
	disagreementValues.put(l25bRM, l25b);
	disagreementValues.put(l26RM, l26);
	disagreementValues.put(l26bRM, l26b);
    }



    private HashMap<LoadedOntology<?>, Vector<String[]>> statementListCache = new HashMap<LoadedOntology<?>, Vector<String[]>>();


    private void addToVector(Vector<String[]> v, OntModel o, Object e) throws OntowrapException {
	StmtIterator it = o.listStatements((Resource) e, null, (RDFNode) null); 
	while (it.hasNext()) {
	    Statement st = it.next();
	    if (st.getObject().isURIResource()) {
		v.add(new String[] {st.getSubject().getURI(),st.getPredicate().getURI(),st.getObject().asNode().getURI()});
	    }
	}
    }


    protected Vector<String[]> listStatements(LoadedOntology<OntModel> onto) throws OntowrapException {
	if (statementListCache.containsKey(onto)) return statementListCache.get(onto);
	Vector<String[]> result = new Vector<String[]>();
	for (Object c : onto.getEntities()) {
	    addToVector(result,onto.getOntology(),c);
	}
	statementListCache.put(onto, result);
	//System.out.println("   Found "+result.size()+" statements");
	return result;
    }


    Map<URI,Map<URI,Set<URI>>> imageCache = new HashMap<URI,Map<URI,Set<URI>>>();


    private boolean match(String p1, String p2, LoadedOntology<?> onto1) {
	Set<Alignment> aligns = network.getTargetingAlignments(onto1.getURI());
	for (Alignment a : aligns)
	    try {
		Set<Cell> cells = a.getAlignCells2(URI.create(p2));
		if (cells!=null)
		for (Cell c : cells) {
		    try {
			if (c.getObject1AsURI().toString().equals(p1)) return true;
		    } catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    } catch (AlignmentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	return false;
	/*Set<String> c=correspondences.get(p1);
	return (c!=null) && c.contains(p2);*/
	// TODO use label splitter
	//return p1.toLowerCase().equals(p2.toLowerCase());
    }

    private boolean contains(String[] lrelations2, String string) {
	for (String r : lrelations2) if (r.equals(string)) return true;
	return false;
    }


    private String[][] getRModules(String s, String p, String o, HeavyLoadedOntology<OntModel>[] ontos) {
	String[][] result = new String[ontos.length][];
	int i = 0;
	for (HeavyLoadedOntology<OntModel> onto : ontos){
	    try {
		result[i++] = getRModules(s, p, o, onto);
	    } catch (OntowrapException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return result;
    }

    private String[] getUris(Set<?> entities, HeavyLoadedOntology<?> onto) {
	ArrayList<String> res = new ArrayList<String>();
	for (Object e : entities) {
	    try {
		res.add(onto.getEntityURI(e).toString());
	    } catch (OntowrapException e1) {
		// TODO Auto-generated catch block
		// e1.printStackTrace();
	    }
	}
	String[] result = new String[res.size()];
	res.toArray(result);
	return result;
    }

    protected String[] getRModules(String s, String predFull, String o, HeavyLoadedOntology<OntModel> onto1) throws OntowrapException {

	//System.err.println(predFull);
	String p=null;
	try {
	    p = new URI(predFull).getFragment();
	} catch (URISyntaxException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	// case where the predicate is from OWL language but not in the table
	if (!contains(lrelations, p) &&(predFull.startsWith(OWL.NS) || predFull.startsWith(OWL2.NS))) {/*skip it*/ return new String[0];}

	if (s==null ||o==null) return new String[0];
	//System.out.println(s+" - "+p+" - "+o);
	OntModel onto = onto1.getOntology();
	HashSet<String> result = new HashSet<String>();	
	StmtIterator it = onto.listStatements(onto.getResource(s), null, onto.getResource(o));
	while (it.hasNext()) {
	    Statement st = it.next();
	    String rel = st.getPredicate().getLocalName();
	    String relFull = st.getPredicate().getURI();
	    //System.err.println(rel);
	    if (contains(lrelations, rel) && !result.contains(rel)){
		result.add(rel);
	    }
	    else if (match(relFull, predFull.toString(), onto1) && !result.contains("matching")){
		result.add("matching");
	    }
	    // JD : added ! before match
	    else if (!match(relFull, predFull.toString(), onto1) && !result.contains("mismatching")){
		result.add("mismatching");		
	    }
	}

	// JD: switch s and o
	it = onto.listStatements(onto.getResource(o), null, onto.getResource(s));
	while (it.hasNext()) {
	    Statement st = it.next();
	    String rel = st.getPredicate().getLocalName();
	    String relFull = st.getPredicate().getURI();
	    if (contains(lrelations, rel)){
		/*if (contains(rrelations, rel) && !result.contains(rel)) result.add(rel);
			else*/ if (!contains(rrelations, rel) && !result.contains(rel+"-1")) result.add(rel+"-1");
	    }
	    else if (match(relFull, predFull.toString(), onto1) && !result.contains("matching-1")){
		result.add("matching-1");
	    }
	    // JD : added ! before match
	    else if (!match(rel, predFull.toString(), onto1) && !result.contains("mismatching")){
		result.add("mismatching");		
	    }
	}

	if (onto.getOntResource(s).isClass()) {
	    OntClass c = onto.getOntResource(s).asClass();
	    if (!result.contains("subClassOf")){

		String[] scos = getUris(onto1.getSuperClasses(c, OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("subClassOf");
	    }
	    if (!result.contains("subClassOf-1")){
		String[] scos = getUris(onto1.getSubClasses(c, OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("subClassOf-1");
	    }
	    if (!result.contains("disjointWith") && onto.getOntResource(s).isClass()){
		OntResource res = (OntResource) onto.getOntResource(s);
		ExtendedIterator<OntClass> iter = res.asClass().listDisjointWith();
		ArrayList<String> results = new ArrayList<String>();
		while (iter.hasNext()) {
		    results.add(iter.next().getLocalName());
		}
		String[] scos = new String[results.size()];
		results.toArray(scos);
		//String[] scos = es.getAllDisjointWith(onto, se);
		// FIXME this is not returning the correct thing
		if (contains(scos, o)) result.add("disjointWith"); 
	    }

	}
	if (onto.getOntResource(s).isIndividual()) {
	    if (!result.contains("type") ){
		String[] scos = getUris(onto1.getClasses(onto1.getEntity(URI.create(s)), OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("type");
	    }
	    if (!result.contains("type-1")){
		String[] scos = getUris(onto1.getInstances(onto1.getEntity(URI.create(s)), OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("type-1");
	    }
	}

	if (onto.getOntResource(s).isProperty()) {
	    OntProperty prop = onto.getOntResource(s).asProperty(); 
	    if (!result.contains("domain")){
		String[] scos = getUris(onto1.getDomain(prop, OntologyFactory.INDIRECT),onto1);
		if (contains(scos, o)) result.add("domain");
	    }
	    /*if (!result.contains("domain-1")){
	    String[] scos = getUris(onto1.getDomain(prop, OntologyFactory.INDIRECT),onto1);
		if (contains(scos, o)) result.add("domain-1");
	}*/
	    if (!result.contains("range")){
		String[] scos = getUris(onto1.getRange(prop, OntologyFactory.INDIRECT),onto1);
		if (contains(scos, o)) result.add("range");
	    }
	    /*if (!result.contains("range-1")){
	    String[] scos = getUris(onto1.getRange(prop, OntologyFactory.INDIRECT),onto1);
		if (contains(scos, o)) result.add("range-1");
	}*/
	    if (!result.contains("subPropertyOf")){
		String[] scos = getUris(onto1.getSuperProperties(prop, OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("subPropertyOf");
	    }
	    if (!result.contains("subPropertyOf-1")){
		String[] scos = getUris(onto1.getSubProperties(prop, OntologyFactory.LOCAL, OntologyFactory.INDIRECT, OntologyFactory.NAMED),onto1);
		if (contains(scos, o)) result.add("subPropertyOf-1");
	    }
	}


	// TODO eliminate redundancy
	// minimalize:
	if (result.contains("subClassOf") && result.contains("subClassOf-1")){
	    result.remove("subClassOf");
	    result.remove("subClassOf-1");
	    result.add("equivalentClass");
	}

	String[] res = new String[result.size()];
	result.toArray(res);
	//System.out.println(Arrays.toString(res));
	return res;
    }

    /*    

		Vector<String> result = new Vector<String>();
		try {
			EntityResult[] ses = es.getEntitiesByKeyword(onto, s, conf);
			EntityResult[] oes = es.getEntitiesByKeyword(onto, o, conf);
			if (ses.length == 0 || oes.length == 0) {
				if (ses.length == 0 && s.endsWith("s")){
					ses = es.getEntitiesByKeyword(onto, s.substring(0, s.length()-1), conf);
				}
				if (oes.length == 0 && o.endsWith("s")){
					oes = es.getEntitiesByKeyword(onto, o.substring(0, o.length()-1), conf);
				}
				if (ses.length == 0 || oes.length == 0) 		
					return new String[0];
			}
			String se = ses[0].getURI();
			String oe = oes[0].getURI();
			String[][] srfs = es.getRelationsTo(onto, se);	
			String[][] srts = es.getRelationsTo(onto, se);
			for (String[] srf : srfs){
				if (srf[2].equals(oe)){
					if (contains(lrelations, srf[1]) && !result.contains(srf[1])){
						result.add(srf[1]);
					}
					else if (match(srf[1], p) && !result.contains("matching")){
						result.add("matching");
					}
					else if (match(srf[1], p) && !result.contains("mismatching")){
						result.add("mismatching");		
					}		 
				}
			}
			for (String[] srf : srts){
				if (srf[2].equals(oe)){
					if (contains(lrelations, srf[1])){
						if (contains(rrelations, srf[1]) && !result.contains(srf[1])) result.add(srf[1]);
						else if (!contains(rrelations, srf[1]) && !result.contains(srf[1]+"-1")) result.add(srf[1]+"-1");
					}
					else if (match(srf[1], p) && !result.contains("matching-1")){
						result.add("matching-1");
					}
					else if (match(srf[1], p) && !result.contains("mismatching")){
						result.add("mismatching");		
					}		 
				}
			}
			if (!result.contains("subClassOf")){
				String[] scos = es.getAllSuperClasses(onto, se);
				if (contains(scos, oe)) result.add("subClassOf");
			}
			if (!result.contains("subClassOf-1")){
				String[] scos = es.getAllSubClasses(onto, se);
				if (contains(scos, oe)) result.add("subClassOf-1");
			}
			if (!result.contains("type")){
				String[] scos = es.getAllClasses(onto, se);
				if (contains(scos, oe)) result.add("type");
			}
			if (!result.contains("type-1")){
				String[] scos = es.getAllInstances(onto, se);
				if (contains(scos, oe)) result.add("type-1");
			}
			if (!result.contains("domain")){
				String[] scos = es.getAllDomain(onto, se);
				if (contains(scos, oe)) result.add("domain");
			}
			if (!result.contains("domain-1")){
				String[] scos = es.getAllDomainOf(onto, se);
				if (contains(scos, oe)) result.add("domain-1");
			}
			if (!result.contains("range")){
				String[] scos = es.getAllRange(onto, se);
				if (contains(scos, oe)) result.add("range");
			}
			if (!result.contains("range-1")){
				String[] scos = es.getAllRangeOf(onto, se);
				if (contains(scos, oe)) result.add("range-1");
			}
			if (!result.contains("subPropertyOf")){
				String[] scos = es.getAllSuperProperties(onto, se);
				if (contains(scos, oe)) result.add("subPropertyOf");
			}
			if (!result.contains("subPropertyOf-1")){
				String[] scos = es.getAllSubProperties(onto, se);
				if (contains(scos, oe)) result.add("subPropertyOf-1");
			}
			if (!result.contains("disjointWith")){
				String[] scos = es.getAllDisjointWith(onto, se);
				// FIXME this is not returning the correct thing
				if (contains(scos, oe)) result.add("disjointWith"); 
			}
			// equiv class
			//	same as
			// diferent from
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// TODO eliminate redundancy
		// minimalize:
		if (result.contains("subClassOf") && result.contains("subClassOf-1")){
			result.remove(result.indexOf("subClassOf"));
			result.remove(result.indexOf("subClassOf-1"));
			result.add("equivalentClass");
		}
		// System.out.println("R-Module for "+onto+ " = "+result);
		return toArray(result);
	}
     */

    protected double[] get(HashMap<String[], double[]> map, String[] rm) {

	Set<String[]> keys = map.keySet();
	for(String[] key : keys) if (equals(rm, key)) return map.get(key);
	return null;
    }
    private boolean equals(String[] rm, String[] key) {
	if (rm.length != key.length) return false;
	int i = 0;
	for (String r : rm)
	    if(!r.equals(key[i++])) return false;
	return true;
    }
    private String toString(String[] rm) {
	String result = "{ ";
	for (String r : rm) 
	    result += r+" ";
	result += "}";
	return result;
    }
    protected int indexOf(String[] lrelations2, String rel) {
	for (int i = 0; i < lrelations2.length; i++){
	    if (rel.equals(lrelations2[i])) return i;
	}
	return -1;
    }

    protected String getFragment(String p) {
	return p.substring(p.lastIndexOf("#")+1);
    }




    private double disagreement(String s, String p, String o, String[] rm) {
	double[] line = get(disagreementValues, rm);
	if (line == null) {
	    /*System.err.println("Hummm... can't find RM for dis"+toString(rm)); */return Double.NaN;
	}
	String rel = getFragment(p);
	int index = indexOf(lrelations, rel);
	double result = 0;
	if (index!=-1) result = line[index];
	else result = line[line.length-1];
	//System.out.println("Disgreement with "+toString(rm)+" = "+result);
	return result;
    }

    public HashMap<String,Double> disags= new HashMap<String,Double>();
    public HashMap<String,Integer> countsD= new HashMap<String,Integer>();


    public double disagreement(HeavyLoadedOntology<OntModel> onto1, HeavyLoadedOntology<OntModel> onto2) {
	//System.out.println("--- DISAGREEMENT BETWEEN : "+onto1.getURI()+" and "+onto2.getURI());
	try {
	    //System.out.println("Getting statements for "+onto1);
	    Vector<String[]> ST1 = listStatements(onto1);
	    //System.out.println("Getting statements for "+onto2);
	    Vector<String[]> ST2 = listStatements(onto2);
	    int count1 = 0;
	    double sum1 = 0.;
	    for (String[] st : ST1){
		//if (correspondences.get(st[0])!=null)
		for (Cell cs : getImages(st[0],onto1, onto2)) {//(String s : correspondences.get(st[0])) {
		    String s = cs.getObject2AsURI().toString();
		    if (onto2.getOntology().getOntResource(s)==null) {
			//System.err.println(s+" not in "+onto2.getURI());
			continue;
		    }
		    //if (onto2.getOntology().getOntResource(s)!=null && correspondences.get(st[2])!=null)
		    for (Cell co : getImages(st[2],onto1, onto2)) {//for (String p : correspondences.get(st[2])) {
			//if (onto2.getOntology().getOntResource(p)!=null)  {
			String o = co.getObject2AsURI().toString();
			if (onto2.getOntology().getOntResource(o)==null) {
			    //System.err.println(o+" not in "+onto2.getURI());
			    continue;
			}
			String[] RM = getRModules(s, st[1], o, onto2);
			if (RM.length != 0){
			    double ag = disagreement(st[0], st[1], st[2], RM);
			    //System.out.println("Disgreement "+ag+" for onto2 on "+st[0]+"-"+st[1]+"-"+st[2]);

			    String k = s+";"+st[0];
			    double a=ag;
			    int sum=1;
			    if (disags.containsKey(k)) {
				a +=disags.get(k);
				sum += countsD.get(k);
			    }
			    disags.put(k, a);
			    countsD.put(k, sum);

			    sum1 += ag;
			    count1++;
			}
		    }
		}

	    }
	    int count2 = 0;
	    double sum2 = 0.;
	    for (String[] st : ST2){
		//if (correspondences.get(st[0])!=null)
		for (Cell cs : getImages(st[0],onto2, onto1)) { // for (String s : correspondences.get(st[0])) {
		    String s = cs.getObject2AsURI().toString();
		    if (onto1.getOntology().getOntResource(s)==null) {
			//System.err.println(s+" not in "+onto1.getURI());
			continue;
		    }
		    //if (onto1.getOntology().getOntResource(s)!=null && correspondences.get(st[2])!=null)
		    for (Cell co : getImages(st[2],onto2, onto1)) {//for (String p : correspondences.get(st[2])) {
			String o = co.getObject2AsURI().toString();
			if (onto1.getOntology().getOntResource(o)==null) {
			    //System.err.println(o+" not in "+onto1.getURI());
			    continue;
			}
			//if (onto1.getOntology().getOntResource(p)!=null)  {
			String[] RM = getRModules(s, st[1], o, onto1);
			if (RM.length != 0){
			    double ag = disagreement(st[0], st[1], st[2], RM);
			    //System.out.println("Disgreement "+ag+" for onto1 on "+st[0]+"-"+st[1]+"-"+st[2]);

			    String k = s+";"+st[0];
			    double a=ag;
			    int sum=1;
			    if (disags.containsKey(k)) {
				a +=disags.get(k);
				sum += countsD.get(k);
			    }
			    disags.put(k, a);
			    countsD.put(k, sum);

			    sum2 += ag;
			    count2++;
			}
		    }
		}
	    }
	    if (count1==0 && count2==0) return 0;
	    return (sum1+sum2)/(((double)count1)+((double)count2));
	}
	catch (OntowrapException e) {
	    throw new OntoSimException(e);
	}
	catch (AlignmentException e) {
	    throw new OntoSimException(e);
	}
    }

    private double agreement(String s, String p, String o, String[] rm) {


	//JM get the line of result (row in the Table 1)
	double[] line = get(agreementValues, rm);

	if (line == null) {
	    /*System.err.println("Hummm... can't find RM "+Arrays.toString(rm)); */return Double.NaN;
	}

	//JM get the relation in the String p
	String rel = getFragment(p);

	//JM get the index of this relation in lrelations array (array of every relations)
	//JM return -1 if the relation rel doesn't exist 
	int index = indexOf(lrelations, rel);

	double result = 0;

	//JM return the values
	if (index!=-1) result = line[index];
	//JM if the relation doesn't exist, return the R values
	else result = line[line.length-1];

	//System.out.println("Agreement with "+toString(rm)+" = "+result);
	return result;
    }


    public HashMap<String,Double> ags= new HashMap<String,Double>();
    public HashMap<String,Integer> countsA= new HashMap<String,Integer>();

    public double agreement(HeavyLoadedOntology<OntModel> onto1, HeavyLoadedOntology<OntModel> onto2) {
	//System.out.println("--- AGREEMENT BETWEEN : "+onto1.getURI()+" and "+onto2.getURI());
	try {
	    //System.out.println("Getting statements for "+onto1);
	    Vector<String[]> ST1 = listStatements(onto1);
	    //System.out.println("Getting statements for "+onto2);
	    Vector<String[]> ST2 = listStatements(onto2);
	    int count1 = 0;
	    double sum1 = 0.;
	    for (String[] st : ST1){
		//if (correspondences.get(st[0])!=null)
		for (Cell cs : getImages(st[0],onto1, onto2)) {//(String s : correspondences.get(st[0])) {
		    String s = cs.getObject2AsURI().toString();
		    if (onto2.getOntology().getOntResource(s)==null) {
			//System.err.println(s+" not in "+onto2.getURI());
			continue;
		    }
		    //if (onto2.getOntology().getOntResource(s)!=null && correspondences.get(st[2])!=null)
		    for (Cell co : getImages(st[2],onto1, onto2)) {//for (String p : correspondences.get(st[2])) {
			String o = co.getObject2AsURI().toString();
			if (onto2.getOntology().getOntResource(o)==null) {
			    //System.err.println(o+" not in "+onto2.getURI());
			    continue;
			}
			//if (onto2.getOntology().getOntResource(o)!=null) {
			String[] RM = getRModules(s, st[1], o, onto2);
			if (RM.length != 0){
			    double ag = agreement(st[0], st[1], st[2], RM);
			    String k = s+";"+st[0];
			    double a=ag;
			    int sum=1;
			    if (ags.containsKey(k)) {
				a +=ags.get(k);
				sum += countsA.get(k);
			    }
			    ags.put(k, a);
			    countsA.put(k, sum);

			    //System.out.println("Agreement "+ag+" for onto2 on "+st[0]+"-"+st[1]+"-"+st[2]);
			    sum1 += ag;
			    count1++;
			}
		    }
		}
	    }
	    int count2 = 0;
	    double sum2 = 0.;
	    for (String[] st : ST2){
		//if (correspondences.get(st[0])!=null)
		for (Cell cs : getImages(st[0],onto2, onto1)) { // for (String s : correspondences.get(st[0])) {
		    String s = cs.getObject2AsURI().toString();
		    if (onto1.getOntology().getOntResource(s)==null) {
			//System.err.println(s+" not in "+onto1.getURI());
			continue;
		    }
		    //if (onto1.getOntology().getOntResource(s)!=null && correspondences.get(st[2])!=null)
		    for (Cell co : getImages(st[2],onto2, onto1)) {//for (String p : correspondences.get(st[2])) {
			String o = co.getObject2AsURI().toString();
			if (onto1.getOntology().getOntResource(o)==null) {
			    //System.err.println(o+" not in "+onto1.getURI());
			    continue;
			}
			//if (onto1.getOntology().getOntResource(p)!=null) {
			String[] RM = getRModules(s, st[1], o, onto1);
			if (RM.length != 0){
			    double ag = agreement(st[0], st[1], st[2], RM);
			    //System.out.println("Agreement "+ag+" for onto2 on "+st[0]+"-"+st[1]+"-"+st[2]);
			    String k = s+";"+st[0];
			    double a=ag;
			    int sum=1;
			    if (ags.containsKey(k)) {
				a +=ags.get(k);
				sum += countsA.get(k);
			    }
			    ags.put(k, a);
			    countsA.put(k, sum);

			    sum2 += ag;
			    count2++;
			}
		    }
		}
	    }

	    //System.out.println("Agg between "+onto1.getURI()+" & "+onto2.getURI());
	    for (String s : ags.keySet()) {
		//System.out.println(s+";"+ags.get(s)+";"+countsA.get(s));
	    }

	    if (count1==0 && count2==0) return 0;
	    return (sum1+sum2)/(((double)count1)+((double)count2));
	}
	catch (OntowrapException e) {
	    throw new OntoSimException(e);
	}
	catch (AlignmentException e) {
	    throw new OntoSimException(e);
	}
    }



    /*public double[] getConsensus(String s, String p, String o){
		String key = s+"||"+p+"||"+o;
		if (this.consValues.containsKey(key)) return consValues.get(key);
		System.out.println("C:: "+s+"--"+p+"--"+o);
		s = s.replaceAll(" ", "-").toLowerCase();
		o = o.replaceAll(" ", "-").toLowerCase();
		String[] ontos = getMatchingOntos(s, p, o);
		System.out.println("Got "+ontos.length+" ontologies");
		if (ontos.length == 0){
			double[] v = new double[] {0.0, 0.0, 0.0};
			consValues.put(key, v);
			return  new double[] {0.0, 0.0, 0.0}; 
		}
		String[][] rm = getRModules(s, p, o, ontos);
		double ag = agreement(s, p, o, rm);
		double dg = disagreement(s, p, o, rm);
		double res = consensus(ag, dg);
		double[] result = new double[3];
		result[0] = res;
		result[1] = ag;
		result[2] = dg;
		System.out.println("C:: "+s+"--"+p+"--"+o+" :: "+res+" "+ag+" "+dg);
		consValues.put(key, result);
		return result;

		// return new double[] {0.5, 0.2, 0.1};
	}*/

    public final fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return TYPES.other;
    }

    public double getDissim(HeavyLoadedOntology<OntModel> o1, HeavyLoadedOntology<OntModel> o2) {
	throw new OntoSimException(this.getClass()+" is not a dissimilarity");
    }


    public double getSim(HeavyLoadedOntology<OntModel> o1, HeavyLoadedOntology<OntModel> o2) {
	//throw new OntoSimException(this.getClass()+" is not a similarity");
	return getMeasureValue(o1,o2);
    }

    public static void main(String[] args) throws OntowrapException, AlignmentException {
	JENAOntologyFactory fact = new JENAOntologyFactory();
	HeavyLoadedOntology<OntModel> onto1 =fact.loadOntology((new File("/Users/jerome/Recherche/jeux_de_tests/conferences/conference/confious.owl")).toURI());
	HeavyLoadedOntology<OntModel> onto2 =fact.loadOntology((new File("/Users/jerome/Recherche/jeux_de_tests/conferences/conference/crs_dr.owl")).toURI());//

	AlignmentParser ap = new AlignmentParser(0);
	Alignment a = ap.parse((new File("/Users/jerome/Recherche/jeux_de_tests/conferences/conferenceAlignmentsMajority/confious-crs_dr-majority.rdf")).toURI());//reference-alignment/confOf-ConfOf.rdf

	BasicOntologyNetwork oo = new BasicOntologyNetwork();
	oo.addOntology(onto1.getURI());
	oo.addOntology(onto2.getURI());
	oo.addAlignment(a);

	Agreement m = new Agreement(oo);

	System.err.println("AGREEMENT : "+m.agreement(onto1, onto2));
	System.err.println("DISAGREEMENT : "+m.disagreement(onto1, onto2));


	for (String s : m.ags.keySet()) {
	    System.err.println(s+";"+m.ags.get(s)+";"+m.countsA.get(s));
	}
    }

}
