package fr.inrialpes.exmo.ontosim.entity.model;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;

public interface HLEntity<E> extends Entity<E> {
    public HeavyLoadedOntology<E> getOntology();
    
}
