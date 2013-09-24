/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   IterativeNodeSim.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.entity.triplebased;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.set.SetMeasure;
import fr.inrialpes.exmo.ontosim.set.WeightedMaxSum;
import fr.inrialpes.exmo.ontosim.util.URI2Triples;
import fr.inrialpes.exmo.ontosim.util.matrix.Matrix;
import fr.inrialpes.exmo.ontosim.util.matrix.MatrixDouble;
import fr.inrialpes.exmo.ontosim.util.measures.CachedMeasure;

public class IterativeNodeSim extends CachedMeasure<Node> {

    private Model m1;
    private Model m2;

    public URI2Triples o1Triples = new URI2Triples();
    public URI2Triples o2Triples = new URI2Triples();

    // Blank nodes map
    public URI2Triples o1BTriples = new URI2Triples();
    public URI2Triples o2BTriples = new URI2Triples();


    public Set<Node> extNodes1 = new HashSet<Node>();
    public Set<Node> extNodes2 = new HashSet<Node>(); 
    
    public Set<Node> litNodes1 = new HashSet<Node>();
    public Set<Node> litNodes2 = new HashSet<Node>();
    
    private double diff;

    private Measure<Node> intialSim;

    public IterativeNodeSim(final Model m1, final String prefix1, final Set<String> uris1, final Model m2, final String prefix2, final Set<String> uris2, Measure<String> ssim, double epsilon) {
	super(Measure.TYPES.similarity);
	this.m1=m1;
	this.m2=m2;
	
	//load triples
	Thread t1 = new Thread() {
	    public void run() {
		loadTriples(m1,prefix1,uris1,o1Triples,o1BTriples, extNodes1, litNodes1);
	    }
	};
	
	Thread t2 = new Thread() {
	    public void run() {
		loadTriples(m2,prefix2,uris2,o2Triples,o2BTriples, extNodes2, litNodes2);
	    }
	};
	
	t1.start();
	t2.start();
	
	try {
	    t1.join();
	    t2.join();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
		
	compute(ssim,epsilon);
    }
    
    public IterativeNodeSim(Model m1, Set<String> uris1, Model m2, Set<String> uris2, Measure<String> ssim, double epsilon) {
	this(m1,null,uris1,m2,null,uris2,ssim,epsilon);
    }
    
    public IterativeNodeSim(Model m1, String prefix1, Model m2, String prefix2, Measure<String> ssim, double epsilon) {
	this(m1,prefix1,null,m2,prefix2,null,ssim,epsilon);
    }
    
    
    private void compute(Measure<String> ssim, double epsilon) {
	//initial sim computation simN0
	this.mValues = new MatrixDouble<Node, Node>();
	computeInitialSim(ssim,mValues);
	intialSim = new CachedMeasure<Node>(mValues,ssim.getMType());
	
	System.out.println("initial sim computed");
	// Triple Sim
	Measure<Triple> tsS = new TripleSimS(intialSim);

	SetMeasure<Triple>  setM = new WeightedMaxSum<Triple>(tsS);//new MaxCoupling<Triple>(tsS,Double.NEGATIVE_INFINITY);

	diff=Double.POSITIVE_INFINITY;
	int nbIt=0;

	while (diff>epsilon) {
	    nbIt++;
	    System.out.println("Iteration "+nbIt+ " - "+diff);
	    diff=0;
	    MatrixDouble<Node,Node> newSims;
	    newSims= updateSim(o1BTriples, o2BTriples, setM);
	    //System.out.println("Iteration "+nbIt+ " - "+diff);
	    mValues.putAll(newSims);
	    newSims = updateSim(o1Triples, o2Triples, setM);
	    mValues.putAll(newSims);
	    
	}
    }

    protected MatrixDouble<Node,Node> updateSim(URI2Triples set1, URI2Triples set2, SetMeasure<Triple> setM) {
	MatrixDouble<Node,Node> newSims = new MatrixDouble<Node, Node>();
	for (Node n1 : set1.uri2triples.keySet()) {
	    for (Node n2 : set2.uri2triples.keySet()) {
		double sim=0;
		int n=0;
		for (int i=0 ; i< 3/*set1.uri2triples.get(n1).length*/ ; i++) {
		    if (set1.uri2triples.get(n1)[i].size()>0 && set2.uri2triples.get(n2)[i].size()>0) {
			int size = Math.min(set1.uri2triples.get(n1)[i].size(),set2.uri2triples.get(n2)[i].size());
			sim += setM.getSim(set1.uri2triples.get(n1)[i],set2.uri2triples.get(n2)[i])*size;
			n+=size;
		    }
		}
		if (n>0) {
		    	sim=sim/n;
        		double oldSim = mValues.get(n1,n2);
        		if (!Double.isNaN(oldSim))
        		    diff += Math.abs(oldSim-sim);
        		else
        		    diff += sim;
        		if (sim!=oldSim) {
        		    newSims.put(n1,n2, sim);
        		}
        		
		}
	    }
	}
	return newSims;
    }

    public void computeInitialSim(final Measure<String> ssim, final Matrix<Node,Node> simMD) {

	Thread t1 = new Thread() {
	    public void run() {
        	for (Node n1 : litNodes1) {
        	    for (Node n2 : litNodes2) {
        		double s = ssim.getSim(n1.getLiteralLexicalForm(), n2.getLiteralLexicalForm());
        		if (s>0)
        		    simMD.put(n1,n2,s);
        	    }
        	}
        	litNodes1=null;
        	litNodes2=null;
        	System.out.println("finish with lit nodes");
	    }
	};
	
	Thread t2 = new Thread() {
	    public void run() {
        	for (Node n1 : extNodes1) {
        	    for (Node n2 : extNodes2) {
        		if (n1.getURI().equals(n2.getURI()))
        		    simMD.put(n1,n2,1.0);
        	    }
        	    
        	}
        	extNodes1=null;
        	extNodes2=null;
        	System.out.println("finish with ext nodes");
	    }
	};
	
	Thread t3 = new Thread() {
	    public void run() {
        	for (Node n1 : o1Triples.uri2triples.keySet()) {
        	    for (Node n2 : o2Triples.uri2triples.keySet()) {
        		double s = ssim.getSim(n1.getLocalName(), n2.getLocalName());
        		if (s>0)
        		    simMD.put(n1,n2,s);
        	    }
        	}
        	System.out.println("finish with local nodes");
	    }
	};
	t1.start();t2.start();t3.start();
	
	try {
	    t1.join();t2.join();t3.join();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	

	
	//Compute literal sims
	/*Iterator<?> st1It = m1.listStatements();
	while (st1It.hasNext()) {
	    Triple t1 = ((Statement) st1It.next()).asTriple();
	    if (!t1.getObject().isLiteral()) continue;
	    Iterator<?> st2It = m2.listStatements();
	    while (st2It.hasNext()) {
		Triple t2 = ((Statement) st2It.next()).asTriple();
		if (!t2.getObject().isLiteral()) continue;
		if ( !simMD.containsKey(t1.getObject(), t2.getObject())) {
		    simMD.put(t1.getObject(), t2.getObject(), ssim.getSim(t1.getObject().getLiteralLexicalForm(), t2.getObject().getLiteralLexicalForm()));
		}	
	   }
	   //System.out.println(t1.getObject().getLiteralLexicalForm());
	}*/
	
	// compute localName sim
	/*for (Node n1 : o1Triple s.uri2triples.keySet()) {
	    for (Node n2 : o2Triples.uri2triples.keySet()) {
		simMD.put(n1, n2, ssim.getSim(n1.getLocalName(), n2.getLocalName()));
	    }
	}*/
    }

    private static boolean isLocal(Node n, String prefix, Set<String> uris) {
	if ((n.isURI()) && ( (prefix != null && n.getURI().startsWith(prefix)) || (uris!=null && uris.contains(n.getURI())) )) {
	    //System.out.println(n.getURI());
	    return true;
	}
	return false;
    }

    public static void loadTriples(Model m, String prefix, Set<String> objects, URI2Triples uri2triples, URI2Triples blanck2triples, Set<Node> extNodes, Set<Node> litNodes) {
	StmtIterator i = m.listStatements();
	int nb=0;
	while (i.hasNext()) {
	    Statement stmt1 = i.next();
	    Triple current = stmt1.asTriple();
	    if (current.getSubject().isBlank()) {
		blanck2triples.addTripleSubject(current.getSubject(), current);
	    }
	    else if (isLocal(current.getSubject(), prefix, objects)) {
		uri2triples.addTripleSubject(current.getSubject(), current);
	    }
	    else {
		extNodes.add(current.getSubject());
	    }

	    if (current.getPredicate().isBlank()) {
		blanck2triples.addTriplePredicate(current.getPredicate(), current);
	    } 
	    else if (isLocal(current.getPredicate(),prefix,objects)) {
		uri2triples.addTriplePredicate(current.getPredicate(), current);
	    }
	    else {
		extNodes.add(current.getPredicate());
		
	    }
	   
	    if (current.getObject().isBlank()) {
		blanck2triples.addTripleObject(current.getObject(), current);
	    }
	    else if (isLocal(current.getObject(),prefix,objects)) 
		    uri2triples.addTripleObject(current.getObject(), current);
	    else if (current.getObject().isLiteral()){
		litNodes.add(current.getObject());
	    }
	    else {
		extNodes.add(current.getObject());
	    }
	    nb++;
	}
	//System.out.println(baseURI+" : "+nb+" triples");
    }
    
    /*public static void loadTriples(Model m, String prefix, URI2Triples uri2triples, URI2Triples blanck2triples) {
	StmtIterator i = m.listStatements();
	int nb=0;
	while (i.hasNext()) {
	    Statement stmt1 = (Statement)i.next();
	    
	    
	    
	    Triple current = stmt1.asTriple();
	    if (current.getSubject().isURI() && current.getSubject().getURI().startsWith(prefix)) {
		uri2triples.addTripleSubject(current.getSubject(), current);
	    }
	    else if (current.getSubject().isBlank()) {
		blanck2triples.addTripleSubject(current.getSubject(), current);
	    }

	    if (current.getPredicate().isURI() && current.getPredicate().getURI().startsWith(prefix)) {
		uri2triples.addTriplePredicate(current.getPredicate(), current);
	    }
	    else if (current.getPredicate().isBlank()) {
		blanck2triples.addTriplePredicate(current.getPredicate(), current);
	    }

	    if (current.getObject().isURI() && current.getObject().getURI().startsWith(prefix)) {
		uri2triples.addTripleObject(current.getObject(), current);
	    }
	    else if (current.getObject().isBlank()) {
		blanck2triples.addTripleObject(current.getObject(), current);
	    }
	    nb++;
	}
	//System.out.println(baseURI+" : "+nb+" triples");
    }*/

    public Collection<Model> getModels() {
	Vector<Model> models = new Vector<Model>(2);
	 Collections.addAll(models, m1, m2);
	 return models;
    }

    public Matrix<Node,Node> getmatrix() {
	return this.mValues;
    }
}
