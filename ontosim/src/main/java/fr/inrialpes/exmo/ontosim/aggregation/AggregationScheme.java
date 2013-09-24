package fr.inrialpes.exmo.ontosim.aggregation;

import java.util.HashMap;
import java.util.Map;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

/**
 * Since the constructors are protected, use AggregationScheme.getInstance(yourMeanClass.class) for having the single instance shared by the application.
 * @author jerome DAVID
 *
 */
public abstract class AggregationScheme {
    
    private static final Map<Class<? extends AggregationScheme>,AggregationScheme> INSTANCES=new HashMap<Class<? extends AggregationScheme>,AggregationScheme>();;
    protected AggregationScheme() {};
    
    public synchronized static <O extends AggregationScheme> O getInstance(Class<O> c) {
	AggregationScheme as = INSTANCES.get(c);
	if (as==null)
	    try {
		as=c.newInstance();
		INSTANCES.put(c, as);
	    } catch (Exception e) {
		throw new OntoSimException(e);
	    }
	    return c.cast(as);
    }
    
    public abstract double getValue(double[] vals);
    
    // Should add getValueSim(Measure,Matching), etc if we want to save 
    // the space and time to build a double[]
    public abstract <O> double getValue(Measure<O> measure,Matching<O> matching);
    
    //public <O> double getValue(Measure<O> measure,Matching<O> matching);
}
