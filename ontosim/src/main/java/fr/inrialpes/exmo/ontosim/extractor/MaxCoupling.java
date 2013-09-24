package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.BasicMatching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;
import fr.inrialpes.exmo.ontosim.util.HungarianAlgorithm;
import fr.inrialpes.exmo.ontosim.util.measures.SimilarityUtility;


public class MaxCoupling extends AbstractExtractor {

    // Note : when everything will use Extractor interface, 
    // it should be safe to remove the array copy in hungarian algorithm
    @SuppressWarnings("unchecked") // generic issue : we can not create an array of generic...
    @Override
    public <O> Matching<O> extract(Measure<O> m, Set<? extends O> src, Set<? extends O> trg) {
	//this.getClass().g
	O[] s1=null;
	O[] s2=null;
	
	boolean transpose=false;
	if (src.size()>trg.size()) {
	    transpose=true;
	    s2= (O[]) new Object[src.size()];
	    s1= (O[]) new Object[trg.size()];
	    src.toArray(s2);
	    trg.toArray(s1);
	}
	else {
	    s1= (O[]) new Object[src.size()];
	    s2= (O[]) new Object[trg.size()];
	    src.toArray(s1);
	    trg.toArray(s2);
	}
	

	double[][] values = SimilarityUtility.getVals(m, s1, s2);
	
	/*
	 * Apparently Hungarian fails (infinite loop) if all values are the sames
	 * this allow to return an empty matching in this case
	 */
	
	boolean allEquals=true;
	double v00 = 0;
	if (values.length>0 && values[0].length>0) v00=values[0][0];
	for (int i=0; i<values.length; i++) {
	    for (int j=0; j<values[i].length; j++)
		if (values[i][j]!=v00) {
		    allEquals=false;
		    break;
		}
	    if (!allEquals) break;
	}
	
	Matching<O> ma = new BasicMatching<O>();
	Matching<O> ma2add = ma;
	if (transpose)
	    ma2add = ma.transposeView();

	if (allEquals) {
	    for (int i=0;i<s1.length;i++)
		ma2add.add(s1[i], s2[i]);
	}
	else {
	    int[][] assignment = HungarianAlgorithm.hgAlgorithm(values, "max"); // Call Hungarian algorithm.
	    for (int i = 0; i < assignment.length; i++) {
		    ma2add.add(	s1[assignment[i][0]],
			    	s2[assignment[i][1]]);
		}
	}

	return ma;
    }
}
