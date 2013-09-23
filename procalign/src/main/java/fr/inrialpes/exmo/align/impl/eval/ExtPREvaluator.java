/*
 * $Id: ExtPREvaluator.java 1825 2013-03-06 20:28:52Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2010, 2012-2013
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
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.io.PrintWriter;

import java.net.URI;

/**
 * Implements extended precision and recall between alignments.
 * These are the measures corresponding to [Ehrig&Euzenat2005].
 * The implementation is based on that of PRecEvaluator.
 * <p/>
 * This currently (4.4) implements all three mesures with the following
 * changes:
 * - relations have been implemented by generalising Table 2, 5 and 7
 * - functions are parameterised by symALPHA, editALPHA, editBETA, oriented
 * - the distance in the three is measured by param/(d+param) or param^d
 * In the first case (for param=.5): 1, .5, .25, .16, .125
 * In the second case: 1, .5, .25, .125
 * - it is possible to avoid using confidences (see below)
 * <p/>
 * Dealing with confidence the way this was suggested in [Ehrig&Euzenat2005]
 * may not be a good idea as it seems. Indeed, typically incorrect correspondences
 * are those with low confidence. Hence, when they are close to the target, the fact
 * that the correspondence has a low confidence will penalise heavily the bonus
 * provided by relaxed measures... hence we have introduced a switch for
 * avoiding the confidence computation.
 * <p/>
 * This evaluator is far less tolerant than the classical PRecEvaluator
 * because it has to load the ontologies
 * <p/>
 * This class is expensive because it computes all similarities together
 * It may be wiser to have different evaluators.
 *
 * @author Jerome Euzenat
 * @version $Id: ExtPREvaluator.java 1825 2013-03-06 20:28:52Z euzenat $
 */

public class ExtPREvaluator extends BasicEvaluator implements Evaluator {

    private HeavyLoadedOntology<Object> onto1;
    private HeavyLoadedOntology<Object> onto2;

    private double symALPHA = .5;
    private double editALPHA = .4;
    private double editBETA = .6;
    private double oriented = .5;

    private double symprec = 1.;
    private double symrec = 1.;
    private double effprec = 1.;
    private double effrec = 1.;
    private double precorientprec = 1.;
    private double precorientrec = 1.;
    private double recorientprec = 1.;
    private double recorientrec = 1.;

    private int nbexpected = 0;
    private int nbfound = 0;

    private double symsimilarity = 0;
    private double effsimilarity = 0;
    private double orientPrecsimilarity = 0;
    private double orientRecsimilarity = 0;

    private boolean withConfidence = true;
    private boolean relsensitive = false;

    /**
     * Creation *
     */
    public ExtPREvaluator(Alignment align1, Alignment align2) throws AlignmentException {
        super(align1, align2);
        convertToObjectAlignments(align1, align2);
    }

    public void setConfidence(boolean b) {
        withConfidence = b;
    }

    public boolean getConfidence() {
        return withConfidence;
    }

    public double getSymPrecision() {
        return symprec;
    }

    public double getSymRecall() {
        return symrec;
    }

    public double getSymSimilarity() {
        return symsimilarity;
    }

    public double getEffPrecision() {
        return effprec;
    }

    public double getEffRecall() {
        return effrec;
    }

    public double getEffSimilarity() {
        return effsimilarity;
    }

    public double getPrecisionOrientedPrecision() {
        return precorientprec;
    }

    public double getPrecisionOrientedRecall() {
        return precorientrec;
    }

    public double getRecallOrientedPrecision() {
        return recorientprec;
    }

    public double getRecallOrientedRecall() {
        return recorientrec;
    }

    public double getPrecisionOrientedSimilarity() {
        return orientPrecsimilarity;
    }

    public double getRecallOrientedSimilarity() {
        return orientRecsimilarity;
    }

    public int getExpected() {
        return nbexpected;
    }

    public int getFound() {
        return nbfound;
    }

    /**
     * This is a partial implementation of [Ehrig & Euzenat 2005]
     * because the relations are not taken into account
     * (they are supposed to be always =)
     */
    public double eval(Properties params) throws AlignmentException {
        return eval(params, (Object) null);
    }

    public double eval(Properties params, Object cache) throws AlignmentException {
        if (params.getProperty("noconfidence") != null) withConfidence = false;
        if (params.getProperty("relations") != null) relsensitive = true;
        LoadedOntology<Object> o1 = (LoadedOntology<Object>) ((ObjectAlignment) align1).getOntologyObject1();
        LoadedOntology<Object> o2 = (LoadedOntology<Object>) ((ObjectAlignment) align1).getOntologyObject2();
        if (!(o1 instanceof HeavyLoadedOntology) || !(o2 instanceof HeavyLoadedOntology))
            throw new AlignmentException("ExtPREvaluation: requires HeavyLoadedOntology");
        onto1 = (HeavyLoadedOntology<Object>) o1;
        onto2 = (HeavyLoadedOntology<Object>) o2;
        nbexpected = align1.nbCells();
        nbfound = align2.nbCells();

        for (Cell c1 : align1) {
            Set<Cell> s2 = align2.getAlignCells1(c1.getObject1());
            try {
                URI uri1 = onto2.getEntityURI(c1.getObject2());
                if (s2 != null) {
                    for (Cell c2 : s2) {
                        URI uri2 = onto2.getEntityURI(c2.getObject2());
                        if (uri1.equals(uri2)
                                && (!relsensitive || c1.getRelation().equals(c2.getRelation()))) {
                            symsimilarity += 1.;
                            effsimilarity += 1.;
                            orientPrecsimilarity += 1.;
                            orientRecsimilarity += 1.;
                            c1 = null; // out of the loop.
                            break;
                        }
                    }
                    // if nothing has been found
                    // JE: Full implementation would require computing a matrix
                    // of distances between both set of correspondences and
                    // running the Hungarian method...
                    if (c1 != null) {
                        // Add guards
                        symsimilarity += computeSymSimilarity(c1, align2);
                        effsimilarity += computeEffSimilarity(c1, align2);
                        orientPrecsimilarity += computePrecisionOrientedSimilarity(c1, align2);
                        orientRecsimilarity += computeRecallOrientedSimilarity(c1, align2);
                    }
                }
            } catch (OntowrapException owex) {
                // This may be ignored as well
                throw new AlignmentException("Cannot find entity URI", owex);
            }
        }

        if (nbfound != 0) symprec = symsimilarity / (double) nbfound;
        if (nbexpected != 0) symrec = symsimilarity / (double) nbexpected;
        if (nbfound != 0) effprec = effsimilarity / (double) nbfound;
        if (nbexpected != 0) effrec = effsimilarity / (double) nbexpected;
        if (nbfound != 0) precorientprec = orientPrecsimilarity / (double) nbfound;
        if (nbexpected != 0) precorientrec = orientPrecsimilarity / (double) nbexpected;
        if (nbfound != 0) recorientprec = orientRecsimilarity / (double) nbfound;
        if (nbexpected != 0) recorientrec = orientRecsimilarity / (double) nbexpected;
        //System.err.println(">>>> " + nbcorrect + " : " + nbfound + " : " + nbexpected);
        //System.err.println(">>>> " + symsimilarity + " : " + effsimilarity + " : " + orientRecsimilarity + " : " + orientPrecsimilarity);
        return (result);
    }

    /**
     * Symmetric relaxed precision and recal similarity
     * This computes similarity depending on structural measures:
     * the similarity is symALPHA^(val1+val2), symALPHA being lower than 1.
     * valx is the length of the subclass chain.
     * Table 1 (& 2) of [Ehrig2005]
     */
    protected double computeSymSimilarity(Cell c1, Alignment s2) {
        double sim = 0; // the similarity between the pair of elements
        try {
            for (Cell c2 : align2) {
                int val1 = 0; // the similatity between the o1 objects
                int val2 = 0; // the similarity between the o2 objects
                if (onto1.getEntityURI(c1.getObject1()).equals(onto1.getEntityURI(c2.getObject1()))) {
                    val1 = 0;
                } else {
                    val1 = Math.abs(relativePosition(c1.getObject1(), c2.getObject1(), onto1));
                    //System.err.println( c1.getObject1()+" -- "+c2.getObject1()+" = "+val1 );
                    if (val1 == 0) continue;
                }
                if (onto2.getEntityURI(c1.getObject2()).equals(onto2.getEntityURI(c2.getObject2()))) {
                    val2 = 0;
                } else {
                    val2 = Math.abs(relativePosition(c1.getObject2(), c2.getObject2(), onto2));
                    //System.err.println( c1.getObject2()+" -- "+c2.getObject2()+" = "+val2 );
                    if (val2 == 0) continue;
                }
                double val = Math.pow(symALPHA, val1 + val2);
                if (withConfidence) val *= 1. - Math.abs(c1.getStrength() - c2.getStrength());
                //System.err.println( "               => "+symALPHA+"^"+val1+"+"+val2+" * "+(1. - Math.abs( c1.getStrength() - c2.getStrength() ))+"  =  "+val );
                if (relsensitive && !c1.getRelation().equals(c2.getRelation())) {
                    if ((c1.getRelation().getRelation().equals("=") &&
                            (c2.getRelation().getRelation().equals("<") || c2.getRelation().getRelation().equals(">")))
                            || (c2.getRelation().getRelation().equals("=") &&
                            (c1.getRelation().getRelation().equals("<") || c1.getRelation().getRelation().equals(">")))) {
                        val = val / 2;
                    } else {
                        val = 0.;
                    }
                }
                if (val > sim) sim = val;
            }
        } catch (OntowrapException aex) {
            return 0;
        } catch (AlignmentException aex) {
            return 0;
        }
        return sim;
    }

    /**
     * Effort-based relaxed precision and recal similarity
     * Note: it will be better if the parameters were replaced by the actual sibling (choice)
     * Table 3 of [Ehrig2005]
     */
    protected double computeEffSimilarity(Cell c1, Alignment s2) {
        double sim = 0; // the similarity between the pair of elements
        try {
            for (Cell c2 : align2) {
                int val1 = 0; // the similatity between the o1 objects
                int val2 = 0; // the similarity between the o2 objects
                double val = 0.; // the current agregated value
                if (onto1.getEntityURI(c1.getObject1()).equals(onto1.getEntityURI(c2.getObject1()))) {
                    val = 1.;
                } else {
                    val1 = relativePosition(c1.getObject1(), c2.getObject1(), onto1);
                    if (val1 == 0) {
                        continue;
                    }
                    if (val1 > 0) {
                        val = Math.pow(editBETA, val1); // Beta is more valued
                    } else {
                        val = Math.pow(editALPHA, -val1);
                    }
                }
                if (onto2.getEntityURI(c1.getObject2()).equals(onto2.getEntityURI(c2.getObject2()))) {
                    // val remains val
                } else {
                    val2 = relativePosition(c1.getObject2(), c2.getObject2(), onto2);
                    if (val2 == 0) {
                        continue;
                    }
                    if (val2 > 0) {
                        val *= Math.pow(editBETA, val2);
                    } else {
                        val *= Math.pow(editALPHA, -val2);
                    }
                }
                if (c1.getStrength() != 0. && c2.getStrength() != 0.) { // Definition 9
                    // Here the measure should also take into account relations
                    if (relsensitive && !c1.getRelation().equals(c2.getRelation())) val = val / 2;
                    if (val > sim) sim = val;
                }
            }
        } catch (OntowrapException aex) {
            return 0;
        } catch (AlignmentException aex) {
            return 0;
        }
        return sim;
    }

    /**
     * Oriented relaxed precision and recal similarity
     * Table 4 (& 5) of [Ehrig2005]
     */
    protected double computePrecisionOrientedSimilarity(Cell c1, Alignment s2) {
        double sim = 0; // the similarity between the pair of elements
        double relsim = 0.;// the similarity between the relations
        try {
            for (Cell c2 : align2) {
                int val1 = 0; // the similatity between the o1 objects
                int val2 = 0; // the similarity between the o2 objects
                double val = 0.; // the current agregated value
                if (onto1.getEntityURI(c1.getObject1()).equals(onto1.getEntityURI(c2.getObject1()))) {
                    val = 1.;
                } else {
                    val1 = relativePosition(c1.getObject1(), c2.getObject1(), onto1);
                    if (val1 == 0) {
                        continue;
                    }
                    if (val1 > 0) {
                        val = Math.pow(oriented, val1);
                    } else {
                        val = 1.;
                    }
                }
                if (onto2.getEntityURI(c1.getObject2()).equals(onto2.getEntityURI(c2.getObject2()))) {
                    // val remains val
                } else {
                    val2 = relativePosition(c1.getObject2(), c2.getObject2(), onto2);
                    if (val2 == 0) {
                        continue;
                    }
                    if (val2 > 0) { // This is the inverse from o1 because queries flow from o1 to o2
                        val *= 1.;
                    } else {
                        val *= Math.pow(oriented, -val2);
                    }
                }
                if (withConfidence) val *= 1. - Math.abs(c1.getStrength() - c2.getStrength());
                // Here the measure should also take into account relations
                if (relsensitive && !c1.getRelation().equals(c2.getRelation())) {
                    if ((c1.getRelation().getRelation().equals("=") && c2.getRelation().getRelation().equals(">"))
                            || (c2.getRelation().getRelation().equals("=") && c1.getRelation().getRelation().equals(">"))) {
                    } else if ((c1.getRelation().getRelation().equals("=") && c2.getRelation().getRelation().equals("<"))
                            || (c2.getRelation().getRelation().equals("=") && c1.getRelation().getRelation().equals("<"))) {
                        val = val / 2;
                    } else {
                        val = 0.;
                    }
                }
                if (val > sim) sim = val;
            }
        } catch (OntowrapException aex) {
            return 0;
        } catch (AlignmentException aex) {
            return 0;
        }
        return sim;
    }

    /**
     * Oriented relaxed precision and recal similarity
     * Table 6 (& 7) of [Ehrig2005]
     */
    protected double computeRecallOrientedSimilarity(Cell c1, Alignment s2) {
        double sim = 0; // the similarity between the pair of elements
        double relsim = 0.;// the similarity between the relations
        try {
            for (Cell c2 : align2) {
                int val1 = 0; // the similatity between the o1 objects
                int val2 = 0; // the similarity between the o2 objects
                double val = 0.; // the current agregated value
                if (onto1.getEntityURI(c1.getObject1()).equals(onto1.getEntityURI(c2.getObject1()))) {
                    val = 1.;
                } else {
                    val1 = relativePosition(c1.getObject1(), c2.getObject1(), onto1);
                    if (val1 == 0) {
                        continue;
                    }
                    if (val1 > 0) {
                        val = 1.;
                    } else {
                        val = Math.pow(oriented, -val1);
                    }
                }
                if (onto2.getEntityURI(c1.getObject2()).equals(onto2.getEntityURI(c2.getObject2()))) {
                    // val remains val
                } else {
                    val2 = relativePosition(c1.getObject2(), c2.getObject2(), onto2);
                    if (val2 == 0) {
                        continue;
                    }
                    if (val2 > 0) { // This is the inverse from o1 because queries flow from o1 to o2
                        val *= Math.pow(oriented, val2);
                    } else {
                        val *= 1.;
                    }
                }
                if (withConfidence) val *= 1. - Math.abs(c1.getStrength() - c2.getStrength());
                // Here the measure should also take into account relations
                if (relsensitive && !c1.getRelation().equals(c2.getRelation())) {
                    if ((c1.getRelation().getRelation().equals("=") && c2.getRelation().getRelation().equals("<"))
                            || (c2.getRelation().getRelation().equals("=") && c1.getRelation().getRelation().equals("<"))) {
                    } else if ((c1.getRelation().getRelation().equals("=") && c2.getRelation().getRelation().equals(">"))
                            || (c2.getRelation().getRelation().equals("=") && c1.getRelation().getRelation().equals(">"))) {
                        val = val / 2;
                    } else {
                        val = 0.;
                    }
                }
                if (val > sim) sim = val;
            }
        } catch (OntowrapException aex) {
            return 0;
        } catch (AlignmentException aex) {
            return 0;
        }
        return sim;
    }

    /**
     * Returns the relative position of two entities:
     * 0: unrelated
     * n: o1 is a n-step sub-entity of o2
     * -n: o2 is a n-step sub-entity of o1
     */
    protected int relativePosition(Object o1, Object o2, HeavyLoadedOntology<Object> onto) throws AlignmentException {
        try {
            if (onto.isClass(o1) && onto.isClass(o2)) {
                return superClassPosition(o1, o2, onto);
            } else if (onto.isProperty(o1) && onto.isProperty(o2)) {
                return superPropertyPosition(o1, o2, onto);
            } else if (onto.isIndividual(o1) && onto.isIndividual(o2)) {
                return 0;
            }
            return 0;
        } catch (OntowrapException owex) {
            throw new AlignmentException("Cannot access class hierarchy", owex);
        }
    }

    public int superClassPosition(Object class1, Object class2, HeavyLoadedOntology<Object> onto) throws AlignmentException {
        int result = -isSuperClass(class2, class1, onto);
        if (result != 0) return result;
        else return isSuperClass(class1, class2, onto);
    }

    /**
     * This is a strange method which returns an integer representing how
     * directly a class is superclass of another or not.
     * <p/>
     * This would require computing the transitive reduction of the superClass
     * relation which is currently returned by HeavyLoadedOntology.
     */
    public int isSuperClass(Object class1, Object class2, HeavyLoadedOntology<Object> ontology) throws AlignmentException {
        try {
            URI uri1 = ontology.getEntityURI(class1);
            Set<?> bufferedSuperClasses = null;
            @SuppressWarnings("unchecked")
            Set<Object> superclasses = (Set<Object>) ontology.getSuperClasses(class2, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY);
            int level = 0;
            int foundlevel = 0;

            while (!superclasses.isEmpty()) {
                bufferedSuperClasses = superclasses;
                superclasses = new HashSet<Object>();
                level++;
                for (Object entity : bufferedSuperClasses) {
                    if (ontology.isClass(entity)) {
                        URI uri2 = ontology.getEntityURI(entity);
                        if (uri1.equals(uri2)) {
                            if (foundlevel == 0 || level < foundlevel) foundlevel = level;
                        } else {
                            superclasses.addAll(ontology.getSuperClasses(entity, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY));
                        }
                    }
                }
            }
            return foundlevel;
        } catch (OntowrapException owex) {
            throw new AlignmentException("Cannot find entity URI", owex);
        }
    }

    public int superPropertyPosition(Object prop1, Object prop2, HeavyLoadedOntology<Object> onto) throws AlignmentException {
        int result = -isSuperProperty(prop2, prop1, onto);
        if (result == 0) return result;
        else return isSuperProperty(prop1, prop2, onto);
    }

    public int isSuperProperty(Object prop1, Object prop2, HeavyLoadedOntology<Object> ontology) throws AlignmentException {
        try {
            URI uri1 = ontology.getEntityURI(prop1);
            Set<?> bufferedSuperProperties = null;
            @SuppressWarnings("unchecked")
            Set<Object> superproperties = (Set<Object>) ontology.getSuperProperties(prop2, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY);
            int level = 0;
            int foundlevel = 0;

            while (!superproperties.isEmpty()) {
                bufferedSuperProperties = superproperties;
                superproperties = new HashSet<Object>();
                level++;
                for (Object entity : bufferedSuperProperties) {
                    if (ontology.isProperty(entity)) {
                        URI uri2 = ontology.getEntityURI(entity);
                        if (uri1.equals(uri2)) {
                            if (foundlevel == 0 || level < foundlevel) foundlevel = level;
                        } else {
                            superproperties.addAll(ontology.getSuperProperties(entity, OntologyFactory.DIRECT, OntologyFactory.ANY, OntologyFactory.ANY));
                        }
                    }
                }
            }
            return foundlevel;
        } catch (OntowrapException owex) {
            throw new AlignmentException("Cannot find entity URI", owex);
        }
    }

    /**
     * This now output the results in Lockheed format.
     */
    public void write(PrintWriter writer) throws java.io.IOException {
        writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
        writer.println("<" + SyntaxElement.RDF.print() + " xmlns:" + Namespace.RDF.shortCut + "='" + Namespace.RDF.prefix + "'\n  xmlns:" + Namespace.ATLMAP.shortCut + "='" + Namespace.ATLMAP.prefix + "'>");
        writer.println("  <" + Namespace.ATLMAP.shortCut + ":output " + SyntaxElement.RDF_ABOUT.print() + "=''>");
        //if ( ) {
        //    writer.println("    <"+Namespace.ATLMAP.shortCut+":algorithm "+SyntaxElement.RDF_RESOURCE.print()+"=\"http://co4.inrialpes.fr/align/algo/"+align1.get+"\">");
        //}
        writer.println("    <" + Namespace.ATLMAP.shortCut + ":input1 " + SyntaxElement.RDF_RESOURCE.print() + "=\"" + ((ObjectAlignment) align1).getOntologyObject1().getURI() + "\">");
        writer.println("    <" + Namespace.ATLMAP.shortCut + ":input2 " + SyntaxElement.RDF_RESOURCE.print() + "=\"" + ((ObjectAlignment) align1).getOntologyObject2().getURI() + "\">");
        writer.print("    <" + Namespace.ATLMAP.shortCut + ":symmetricprecision>");
        writer.print(symprec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":symmetricprecision>\n    <" + Namespace.ATLMAP.shortCut + ":symmetricrecall>");
        writer.print(symrec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":symmetricrecall>\n    <" + Namespace.ATLMAP.shortCut + ":effortbasedprecision>");
        writer.print(effprec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":effortbasedprecision>\n    <" + Namespace.ATLMAP.shortCut + ":effortbasedrecall>");
        writer.print(effrec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":effortbasedrecall>\n    <" + Namespace.ATLMAP.shortCut + ":precisionorientedprecision>");
        writer.print(precorientprec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":precisionorientedprecision>\n    <" + Namespace.ATLMAP.shortCut + ":precisionorientedrecall>");
        writer.print(precorientrec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":precisionorientedrecall>\n    <" + Namespace.ATLMAP.shortCut + ":recallorientedprecision>");
        writer.print(recorientprec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":recallorientedprecision>\n    <" + Namespace.ATLMAP.shortCut + ":recallorientedrecall>");
        writer.print(recorientrec);
        writer.print("</" + Namespace.ATLMAP.shortCut + ":recallorientedrecall>\n  </" + Namespace.ATLMAP.shortCut + ":output>\n</" + SyntaxElement.RDF.print() + ">\n");
    }

    public Properties getResults() {
        Properties results = new Properties();
        results.setProperty("symmetric precision", Double.toString(symprec));
        results.setProperty("symmetric recall", Double.toString(symrec));
        results.setProperty("symmetric similarity", Double.toString(symsimilarity));
        results.setProperty("effort-based precision", Double.toString(effprec));
        results.setProperty("effort-based recall", Double.toString(effrec));
        results.setProperty("effort-based similarity", Double.toString(effsimilarity));
        results.setProperty("precision-oriented precision", Double.toString(precorientprec));
        results.setProperty("precision-oriented recall", Double.toString(precorientrec));
        results.setProperty("recall-oriented precision", Double.toString(recorientprec));
        results.setProperty("recall-oriented recall", Double.toString(recorientrec));
        results.setProperty("oriented precision similarity", Double.toString(orientPrecsimilarity));
        results.setProperty("oriented recall similarity", Double.toString(orientRecsimilarity));
        results.setProperty("nbexpected", Integer.toString(nbexpected));
        results.setProperty("nbfound", Integer.toString(nbfound));
        return results;
    }
}

