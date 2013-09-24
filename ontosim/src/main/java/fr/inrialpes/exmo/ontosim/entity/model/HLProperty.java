package fr.inrialpes.exmo.ontosim.entity.model;

import java.util.Set;

public interface HLProperty<E> extends HLEntity<E> {

    public Set<HLProperty<E>> getSubProperties(int local, int asserted, int named);
    public Set<HLProperty<E>> getSuperProperties(int local, int asserted, int named);
    public Set<HLClass<E>> getRange(int asserted);
    public Set<HLClass<E>> getDomain(int asserted);
}
