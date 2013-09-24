package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.BasicMatching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

public class Max extends AbstractExtractor {


    @Override
    public <O> Matching<O> extract(Measure<O> m, Set<? extends O> src, Set<? extends O> trg) {
	double max = Double.NEGATIVE_INFINITY;
	Matching <O> matching = new BasicMatching<O>();
	for (O s : src) 
	    for (O t : trg) {
		double v = m.getMeasureValue(s, t);
		if (v>max) {
		    matching.clear();
		    matching.add(s,t);
		    max=v;
		}
		else if (v==max) 
		    matching.add(s,t);
	    }
	return matching;
    }

}
