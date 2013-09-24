package fr.inrialpes.exmo.ontosim.entity.model;

import java.util.Set;

public interface HLIndividual<E> extends HLEntity<E> {

    public Set<HLClass<E>> getClasses(int local, int asserted, int named);
}
