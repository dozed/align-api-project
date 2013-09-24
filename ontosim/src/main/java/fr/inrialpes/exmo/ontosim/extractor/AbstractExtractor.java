package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;
import fr.inrialpes.exmo.ontosim.util.matrix.Matrix;
import fr.inrialpes.exmo.ontosim.util.measures.CachedMeasure;

public abstract class AbstractExtractor implements Extractor {
    
    public final <O> Matching<O> extract(Matrix<O,O> m) {
	Measure<O> measure = new CachedMeasure<O>(m, Measure.TYPES.similarity);
	return extract(measure,m.getDimR(),m.getDimC());
    }

    public abstract <O> Matching<O> extract(Measure<O> m, Set<? extends O> s, Set<? extends O> t);
}
