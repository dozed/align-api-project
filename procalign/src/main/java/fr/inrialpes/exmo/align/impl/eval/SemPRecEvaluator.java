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

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

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
        ReasonerBuilder builder = new ReasonerBuilder()
                .add(al1.getOntologyObject1())
                .add(al1.getOntologyObject2())
                .add(al1);

        Reasoner reasoner = builder.build();

        if (!reasoner.isConsistent()) return al2.nbCells(); // everything is entailed

        int entailed = 0;
        for (Cell c2 : al2) {
            if (reasoner.isEntailed(builder.correspondenceToAxiom((ObjectCell) c2))) {
                entailed++;
            } else {
                logger.debug("could not find: " + c2.getObject1AsURI(al2) + " - " + c2.getObject2AsURI(al2) + " - " + c2.getRelation());
            }
        }

        return entailed;
    }

}

