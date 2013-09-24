package fr.inrialpes.exmo.ontosim.util.measures;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

public abstract class MeasureUtility {

    protected MeasureUtility(){}
    
    public static <O> double[] getVals(Measure<O> m, Matching<O> ma) {
	double[] res = new double[ma.size()];
	int i=0;
	for (Matching.Entry<O> x : ma) 
	    res[i++]=getVal(m,x.getSource(), x.getTarget());
	return res;
    }
    
    public static <O> double[][] getVals(Measure<O> m, O[] s1, O[] s2) {
	double[][] values = new double[s1.length][s2.length];
	for (int i = 0; i < s1.length; i++)
	    for (int j = 0; j < s2.length; j++) {
		values[i][j] = m.getSim(s1[i], s2[j]);
		
	    }
	return values;
    }
    
    public static <O> double getVal(Measure<O> m, O x, O y) {
	return m.getMeasureValue(x, y);
    }
    
    public static <O> Measure<O> convert(Measure<O> m) {
	return m;
    }
}
