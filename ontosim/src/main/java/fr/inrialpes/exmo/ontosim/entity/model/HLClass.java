package fr.inrialpes.exmo.ontosim.entity.model;

import java.util.Set;

public interface HLClass<E> extends HLEntity<E> {

    public Set<HLIndividual<E>> getInstances(int local, int asserted, int named);

    public Set<HLClass<E>> getSubClasses(int local, int asserted, int named);
    public Set<HLClass<E>> getSuperClasses(int local, int asserted, int named);

    public Set<HLProperty<E>> getProperties(int local, int asserted, int named);
    public Set<HLProperty<E>> getDataProperties(int local, int asserted, int named);
    public Set<HLProperty<E>> getObjectProperties(int local, int asserted, int named); 

}
