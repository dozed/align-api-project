package fr.inrialpes.exmo.ontosim.aggregation;

import java.util.NoSuchElementException;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

/**
 * A dummy extractor which returns the first value of the vector
 * @author jerome David
 *
 */
public final class DummyAS extends AggregationScheme {
    
    public final double getValue(double[] vals) {
	try {
	    return vals[0];
	}
	catch (ArrayIndexOutOfBoundsException e) {
	    throw new OntoSimException("The array is empty");
	}
    }

    public final <O> double getValue(Measure<O> measure, Matching<O> matching) {
	try {
        	Matching.Entry<O> entry = matching.iterator().next();
        	return measure.getMeasureValue(entry.getSource(), entry.getTarget());
	}
	catch (NoSuchElementException e) {
	    throw new OntoSimException("The matching is empty");
	}	
    }

}
