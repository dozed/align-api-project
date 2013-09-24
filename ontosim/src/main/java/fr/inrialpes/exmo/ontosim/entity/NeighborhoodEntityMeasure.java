package fr.inrialpes.exmo.ontosim.entity;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.entity.model.HLClass;
import fr.inrialpes.exmo.ontosim.entity.model.HLEntity;
import fr.inrialpes.exmo.ontosim.entity.model.HLIndividual;
import fr.inrialpes.exmo.ontosim.entity.model.HLProperty;
import fr.inrialpes.exmo.ontosim.set.MaxCoupling;
import fr.inrialpes.exmo.ontosim.set.SetMeasure;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;

public class NeighborhoodEntityMeasure<E> implements Measure<HLEntity<E>> {
    
    private SetMeasure<HLEntity<E>> initialM;
    
    
    @SuppressWarnings("unchecked")
    public NeighborhoodEntityMeasure() {
	initialM = new MaxCoupling(new EntityLexicalMeasure<E>());
    }
    
    public NeighborhoodEntityMeasure(SetMeasure<HLEntity<E>> intialM) {
	this.initialM = intialM;
    }

    protected void getVal(Set<? extends HLEntity<E>> s1, Set<? extends HLEntity<E>> s2, List<Double> values) {
	if (s1.size()>0 && s2.size()>0)
	    try {
		values.add(initialM.getMeasureValue(s1,s2));
	    }
		catch (NullPointerException e) {};
    }
    
    protected List<Double> getVals(HLClass<E> e1, HLClass<E> e2) {
	List<Double> vals = new Vector<Double>(3);
	
	getVal(	e1.getSubClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getSubClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	
	getVal(	e1.getSuperClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getSuperClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	
	getVal(	
		e1.getProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	
	return vals;
    }
    
    protected List<Double> getVals(HLProperty<E> e1, HLProperty<E> e2) {
	List<Double> vals = new Vector<Double>(4);
	
	getVal(	
		e1.getSubProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getSubProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	getVal(	
		e1.getSuperProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getSuperProperties(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	getVal(	
		e1.getDomain(OntologyFactory.ASSERTED),
		e2.getDomain(OntologyFactory.ASSERTED),
		vals
	);
	return vals;
    }
    
    protected List<Double> getVals(HLIndividual<E> e1, HLIndividual<E> e2) {
	List<Double> vals = new Vector<Double>(1);
	
	getVal(
		e1.getClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		e2.getClasses(OntologyFactory.LOCAL, OntologyFactory.DIRECT, OntologyFactory.NAMED),
		vals
	);
	return vals;
    }
    
 

    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return initialM.getMType();
    }

    public double getMeasureValue(HLEntity<E> o1, HLEntity<E> o2) {
	List<Double> sims=null;
	if (o1 instanceof HLClass && o2 instanceof HLClass)
	    sims=getVals((HLClass<E>) o1, (HLClass<E>) o2);
	else if (o1 instanceof HLProperty && o2 instanceof HLProperty)
	    sims=getVals((HLProperty<E>) o1, (HLProperty<E>) o2);
	else if (o1 instanceof HLIndividual && o2 instanceof HLIndividual)
	    sims=getVals((HLIndividual<E>) o1, (HLIndividual<E>) o2);
	else
	    return 0;
	double isim = initialM.getLocalMeasure().getMeasureValue(o1, o2);
	for (double v : sims) {
	    isim+=v;
	}
	return isim/(sims.size()+1);
    }
    
    public double getSim(HLEntity<E> o1, HLEntity<E> o2) {
	if (initialM.getMType()==TYPES.similarity)
	    return getMeasureValue(o1,o2);
	else if (initialM.getMType()==TYPES.dissimilarity) 
	    return 1-getMeasureValue(o1,o2);
	throw new OntoSimException("Not a similarity");
    }
    public double getDissim(HLEntity<E> o1, HLEntity<E> o2) {
	if (initialM.getMType()==TYPES.dissimilarity)
	    return getMeasureValue(o1,o2);
	else if (initialM.getMType()==TYPES.similarity) 
	    return 1-getMeasureValue(o1,o2);
	throw new OntoSimException("Not a dissimilarity");
    }

}
