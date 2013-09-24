package fr.inrialpes.exmo.ontosim.util.measures;

import fr.inrialpes.exmo.ontosim.Measure;

public final class SimilarityUtility extends MeasureUtility {
    public static <O> double getVal(Measure<O> m, O x, O y) {
	return m.getSim(x, y);
    }

    public static <O> Measure<O> convert(final Measure<O> m) {
	return new Measure<O>() {

	    @Override
	    public double getDissim(O o1, O o2) {
		return m.getDissim(o1, o2);
	    }

	    @Override
	    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	    }

	    @Override
	    public double getMeasureValue(O o1, O o2) {
		return m.getSim(o1, o2);
	    }

	    @Override
	    public double getSim(O o1, O o2) {
		return m.getSim(o1, o2);
	    }

	};
    }
}
