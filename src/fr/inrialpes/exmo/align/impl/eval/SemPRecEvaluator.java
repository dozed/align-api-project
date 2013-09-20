/*
 * $Id: SemPRecEvaluator.java 1842 2013-03-24 17:42:41Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2013
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
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

// ---- IDDL
import fr.paris8.iut.info.iddl.IDDLReasoner;
import fr.paris8.iut.info.iddl.IDDLException;
import fr.paris8.iut.info.iddl.conf.Semantics;

// ----  HermiT Implementation

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyIRIMapperImpl;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.BufferingMode;

import org.semanticweb.HermiT.Reasoner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread;
import java.lang.Runnable;
import java.lang.IllegalArgumentException;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Evaluate proximity between two alignments.
 * This function implements Precision/Recall. The first alignment
 * is thus the expected one.
 *
 * @author Jerome Euzenat
 * @version $Id: SemPRecEvaluator.java 1842 2013-03-24 17:42:41Z euzenat $
 */

public class SemPRecEvaluator extends PRecEvaluator implements Evaluator {

    final static Logger logger = LoggerFactory.getLogger(SemPRecEvaluator.class);
    private int nbfoundentailed = 0; // nb of returned cells entailed by the reference alignment
    private int nbexpectedentailed = 0; // nb of reference cells entailed by returned alignment

    private Semantics semantics = null; // the semantics used for interpreting alignments
    // null means that we use the reduced semantics with HermiT, otherwise, this is an IDDL semantics

    /**
     * Creation
     * Initiate Evaluator for precision and recall
     *
     * @param al1 : the reference alignment
     * @param al2 : the alignment to evaluate
     */
    public SemPRecEvaluator(Alignment al1, Alignment al2) throws AlignmentException {
        super(al1, al2);
        logger.debug("Created a SemPREvaluator");
        convertToObjectAlignments(al1, al2);
    }

    public void init(Properties params) {
        super.init();
        nbexpectedentailed = 0;
        nbfoundentailed = 0;
        // Set the semantics to be used
        String sem = params.getProperty("semantics");
        if (sem != null) {
            semantics = Semantics.valueOf(sem);
        }
    }

    /**
     * The formulas are standard:
     * given a reference alignment A
     * given an obtained alignment B
     * which are sets of cells (linking one entity of ontology O to another of ontolohy O').
     * <p/>
     * P = |A inter B| / |B|
     * R = |A inter B| / |A|
     * F = 2PR/(P+R)
     * with inter = set intersection and |.| cardinal.
     * <p/>
     * In the implementation |B|=nbfound, |A|=nbexpected and |A inter B|=nbcorrect.
     * <p/>
     * This takes semantics as a parameter which should be a litteral of fr.paris8.iut.info.iddl.conf.Semantics
     */
    public double eval(Properties params) throws AlignmentException {
        init(params);
        nbfound = align2.nbCells();
        nbexpected = align1.nbCells();

        nbfoundentailed = nbEntailedCorrespondences((ObjectAlignment) align1, (ObjectAlignment) align2);
        nbexpectedentailed = nbEntailedCorrespondences((ObjectAlignment) align2, (ObjectAlignment) align1);

        precision = (double) nbfoundentailed / (double) nbfound;
        recall = (double) nbexpectedentailed / (double) nbexpected;
        return computeDerived();
    }

    public int getFoundEntailed() {
        return nbfoundentailed;
    }

    public int getExpectedEntailed() {
        return nbexpectedentailed;
    }

    public Properties getResults() {
        Properties results = super.getResults();
        results.setProperty("nbexpectedentailed", Integer.toString(nbexpectedentailed));
        results.setProperty("nbfoundentailed", Integer.toString(nbfoundentailed));
        return results;
    }

    public int nbEntailedCorrespondences(ObjectAlignment al1, ObjectAlignment al2) throws AlignmentException {
        logger.trace("Computing entailment (semantics: {})", semantics);
        if (semantics != null) { // IDDL
            ArrayList<Alignment> allist = new ArrayList<Alignment>();
            allist.add(al1);
            try {
                reasoner = new IDDLReasoner(allist, semantics);
            } catch (IDDLException idex) {
                throw new AlignmentException("Cannot create IDDLReasoner", idex);
            }
        } else { // Hermit
            loadPipedAlignedOntologies(al1);
        }
        if (!reasoner.isConsistent()) return al2.nbCells(); // everything is entailed
        logger.debug("{} is consistent", al1);
        int entailed = 0;
        for (Cell c2 : al2) {
            logger.trace(c2.getObject1() + " {} {}", c2.getRelation().getRelation(), c2.getObject2());
            if (semantics != null) { // IDDL
                try {
                    if (((IDDLReasoner) reasoner).isEntailed(al2, c2)) {
                        logger.trace("      --> entailed");
                        entailed++;
                    }
                } catch (IDDLException idex) { // counted as non entailed
                    logger.warn("Cannot be translated.");
                }
            } else { // Hermit
                try {
                    if (reasoner.isEntailed(correspToAxiom(al2, (ObjectCell) c2))) {
                        logger.trace("      --> entailed");
                        entailed++;
                    }
                } catch (AlignmentException aex) { // type mismatch -> 0
                    logger.warn("Cannot be translated.");
                }
            }
        }
        return entailed;
    }

    /**
     * It would be useful to directly use the Ontologies since they are already loaded
     * Two implementation of Alignment loading: one with intermediate file and one without.
     */
    protected OWLOntologyManager manager = null;
    protected OWLReasoner reasoner = null;

    /* 
     * Loads the Aligned ontologies without intermediate file
     */
    public void loadPipedAlignedOntologies(final ObjectAlignment align) throws AlignmentException {
        PipedInputStream in = new PipedInputStream();
        try {
            final PipedOutputStream out = new PipedOutputStream(in);
            Thread myThread = new Thread(
                    new Runnable() {
                        public void run() {
                            PrintWriter writer = null;
                            try {
                                writer = new PrintWriter(
                                        new BufferedWriter(
                                                new OutputStreamWriter(out, "UTF-8")), true);
                                OWLAxiomsRendererVisitor renderer = new OWLAxiomsRendererVisitor(writer);
                                renderer.init(new Properties());
                                // Generate the ontology as OWL Axioms
                                align.render(renderer);
                            } catch (Exception ex) {
                                // No way to handle this exception???
                                // At worse, the other end will raise an exception
                                logger.error("Cannot render alignment to OWL", ex);
                            } finally {
                                if (writer != null) {
                                    writer.flush();
                                    writer.close();
                                }
                            }
                        }
                    }
            );
            myThread.start();
        } catch (UnsupportedEncodingException ueex) {
            throw new AlignmentException("Cannot render alignment to OWL", ueex);
        } catch (IOException ioex) {
            throw new AlignmentException("Cannot render alignment to OWL", ioex);
        }

        manager = OWLManager.createOWLOntologyManager();
        //logger.trace( "{} ----> {}", align.getOntology1URI(), align.getFile1() );
        //logger.trace( "{} ----> {}", align.getOntology2URI(), align.getFile2() );
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(align.getOntology1URI()),
                IRI.create(align.getFile1())));
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(align.getOntology2URI()),
                IRI.create(align.getFile2())));
        try {
            manager.loadOntologyFromOntologyDocument(IRI.create(align.getFile1()));
            manager.loadOntologyFromOntologyDocument(IRI.create(align.getFile2()));
            // Load the ontology stream
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(in);
            reasoner = new Reasoner(ontology);
        } catch (OWLOntologyCreationException ooce) {
            throw new AlignmentException("Hermit : Cannot load alignment", ooce);
        } catch (IllegalArgumentException ilex) {
            throw new AlignmentException("Hermit : Cannot load alignment", ilex);
        }
    }

    /* 
     * Loads the Aligned ontologies through an intermediate file
     */
    public void loadFileAlignedOntologies(ObjectAlignment align) throws AlignmentException {
        // Render the alignment
        PrintWriter writer = null;
        File merged = null;
        try {
            merged = File.createTempFile("spreval", ".owl");
            merged.deleteOnExit();
            writer = new PrintWriter(new FileWriter(merged, false), true);
            OWLAxiomsRendererVisitor renderer = new OWLAxiomsRendererVisitor(writer);
            renderer.init(new Properties());
            align.render(renderer);
        } catch (UnsupportedEncodingException ueex) {
            throw new AlignmentException("Cannot render alignment to OWL", ueex);
        } catch (IOException ioex) {
            throw new AlignmentException("Cannot render alignment to OWL", ioex);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }

        // Load the ontology
        manager = OWLManager.createOWLOntologyManager();
        //logger.trace( "{} ----> {}", align.getOntology1URI(), align.getFile1() );
        //logger.trace( "{} ----> {}", align.getOntology2URI(), align.getFile2() );
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(align.getOntology1URI()),
                IRI.create(align.getFile1())));
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(align.getOntology2URI()),
                IRI.create(align.getFile2())));
        try {
            manager.loadOntologyFromOntologyDocument(IRI.create(align.getFile1()));
            manager.loadOntologyFromOntologyDocument(IRI.create(align.getFile2()));
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(merged);
            reasoner = new Reasoner(ontology);
        } catch (OWLOntologyCreationException ooce) {
            throw new AlignmentException("Hermit : Cannot load alignment", ooce);
        } catch (IllegalArgumentException ilex) {
            throw new AlignmentException("Hermit : Cannot load alignment", ilex);
        }
    }

    // In fact, it should be possible to do all EDOAL
    public OWLAxiom correspToAxiom(ObjectAlignment al, ObjectCell corresp) throws AlignmentException {
        OWLDataFactory owlfactory = manager.getOWLDataFactory();

        LoadedOntology onto1 = al.ontology1();
        LoadedOntology onto2 = al.ontology2();
        // retrieve entity1 and entity2
        // create the axiom in function of their labels
        Object e1 = corresp.getObject1();
        Object e2 = corresp.getObject2();
        Relation r = corresp.getRelation();
        try {
            if (onto1.isClass(e1)) {
                if (onto2.isClass(e2)) {
                    OWLClass entity1 = owlfactory.getOWLClass(IRI.create(onto1.getEntityURI(e1)));
                    OWLClass entity2 = owlfactory.getOWLClass(IRI.create(onto2.getEntityURI(e2)));
                    if (r instanceof EquivRelation) {
                        return owlfactory.getOWLEquivalentClassesAxiom(entity1, entity2);
                    } else if (r instanceof SubsumeRelation) {
                        return owlfactory.getOWLSubClassOfAxiom(entity2, entity1);
                    } else if (r instanceof SubsumedRelation) {
                        return owlfactory.getOWLSubClassOfAxiom(entity1, entity2);
                    } else if (r instanceof IncompatRelation) {
                        return owlfactory.getOWLDisjointClassesAxiom(entity1, entity2);
                    }
                } else if (onto2.isIndividual(e2) && (r instanceof HasInstanceRelation)) {
                    return owlfactory.getOWLClassAssertionAxiom(owlfactory.getOWLClass(IRI.create(onto1.getEntityURI(e1))),
                            owlfactory.getOWLNamedIndividual(IRI.create(onto2.getEntityURI(e2))));
                }
            } else if (onto1.isDataProperty(e1) && onto2.isDataProperty(e2)) {
                OWLDataProperty entity1 = owlfactory.getOWLDataProperty(IRI.create(onto1.getEntityURI(e1)));
                OWLDataProperty entity2 = owlfactory.getOWLDataProperty(IRI.create(onto2.getEntityURI(e2)));
                if (r instanceof EquivRelation) {
                    return owlfactory.getOWLEquivalentDataPropertiesAxiom(entity1, entity2);
                } else if (r instanceof SubsumeRelation) {
                    return owlfactory.getOWLSubDataPropertyOfAxiom(entity2, entity1);
                } else if (r instanceof SubsumedRelation) {
                    return owlfactory.getOWLSubDataPropertyOfAxiom(entity1, entity2);
                } else if (r instanceof IncompatRelation) {
                    return owlfactory.getOWLDisjointDataPropertiesAxiom(entity1, entity2);
                }
            } else if (onto1.isObjectProperty(e1) && onto2.isObjectProperty(e2)) {
                OWLObjectProperty entity1 = owlfactory.getOWLObjectProperty(IRI.create(onto1.getEntityURI(e1)));
                OWLObjectProperty entity2 = owlfactory.getOWLObjectProperty(IRI.create(onto2.getEntityURI(e2)));
                if (r instanceof EquivRelation) {
                    return owlfactory.getOWLEquivalentObjectPropertiesAxiom(entity1, entity2);
                } else if (r instanceof SubsumeRelation) {
                    return owlfactory.getOWLSubObjectPropertyOfAxiom(entity2, entity1);
                } else if (r instanceof SubsumedRelation) {
                    return owlfactory.getOWLSubObjectPropertyOfAxiom(entity1, entity2);
                } else if (r instanceof IncompatRelation) {
                    return owlfactory.getOWLDisjointObjectPropertiesAxiom(entity1, entity2);
                }
            } else if (onto1.isIndividual(e1)) {
                if (onto2.isIndividual(e2)) {
                    OWLIndividual entity1 = owlfactory.getOWLNamedIndividual(IRI.create(onto1.getEntityURI(e1)));
                    OWLIndividual entity2 = owlfactory.getOWLNamedIndividual(IRI.create(onto2.getEntityURI(e2)));
                    if (r instanceof EquivRelation) {
                        return owlfactory.getOWLSameIndividualAxiom(entity1, entity2);
                    } else if (r instanceof IncompatRelation) {
                        return owlfactory.getOWLDifferentIndividualsAxiom(entity1, entity2);
                    }
                } else if (onto2.isClass(e2) && (r instanceof InstanceOfRelation)) {
                    return owlfactory.getOWLClassAssertionAxiom(owlfactory.getOWLClass(IRI.create(onto2.getEntityURI(e2))),
                            owlfactory.getOWLNamedIndividual(IRI.create(onto1.getEntityURI(e1))));
                }
            }
        } catch (OntowrapException owex) {
            throw new AlignmentException("Error interpreting URI " + owex);
        }
        throw new AlignmentException("Cannot convert correspondence " + corresp);
    }

}

