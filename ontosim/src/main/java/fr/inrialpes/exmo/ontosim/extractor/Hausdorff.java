package fr.inrialpes.exmo.ontosim.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.BasicMatching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;
import fr.inrialpes.exmo.ontosim.util.measures.DissimilarityUtility;

public class Hausdorff extends AbstractExtractor {


    public final <O> Matching<O> extract(Measure<O> m, Set<? extends O> src, Set<? extends O> trg) {
	Measure<O> m2=m;
	if (m2.getMType()==Measure.TYPES.similarity)
	    m2 = DissimilarityUtility.convert(m);
	Map<O, Set<O>> mins1 = new HashMap<O, Set<O>>();
	Map<O, Set<O>> mins2 = new HashMap<O, Set<O>>();
	Map<O, Double> vals = new HashMap<O, Double>();
 
	double max = 0;
	for (O t : trg) {
	    mins2.put(t, new HashSet<O>());
	    vals.put(t, Double.POSITIVE_INFINITY);
	}

	for (O s : src) {
	    double vs = Double.POSITIVE_INFINITY;
	    Set<O> minS = new HashSet<O>();
	    for (O t : trg) {
		double v = m2.getMeasureValue(s, t);
		if (vs > v) {
		    minS.clear();
		    minS.add(t);
		    vs = v;
		} else if (vs == v)
		    minS.add(t);

		double vt = vals.get(t);
		if (vt >= v) {
		    Set<O> minT = mins2.get(t);
		    if (vt > v) {
			minT.clear();
			//vt = v;
			vals.put(t, v);
		    }
		    minT.add(s);
		}
	    }
	    if (max <= vs) {
		max = vs;
		if (max<vs) mins1.clear();
		mins1.put(s, minS);
		vals.put(s, vs);
	    }
	}

	boolean removeall = false;
	for (O t : trg) {
	    double v = vals.get(t);
	    if (max > v) {
		mins2.remove(t);
	    } else if (v > max) {
		max = v;
		removeall = true;
	    }
	}
	
	
	Matching<O> matching = new BasicMatching<O>();
	if (!removeall) {
	    for (O e : mins1.keySet())
		if (vals.get(e)==max)
		    for (O f : mins1.get(e))
			matching.add(e, f);
	}

	for (O e : mins2.keySet())
	    if (vals.get(e) == max)
		for (O f : mins2.get(e))
		    matching.add(f, e);
	return matching;
    }

}
