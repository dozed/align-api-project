package fr.inrialpes.exmo.align.impl.eval;

import com.sun.javafx.collections.IterableChangeBuilder;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Enumeration;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stefan
 * Date: 21.09.13
 * Time: 01:17
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerBuilder {

    private final OWLOntologyManager manager;
    private final OWLOntology localOntology;

    private final OWLAPI3Ontology localWrappedOntology = new OWLAPI3Ontology();

    public ReasonerBuilder() {
        manager = OWLManager.createOWLOntologyManager();
        try {
            localOntology = manager.createOntology();
            localWrappedOntology.setOntology(localOntology);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Could not create local ontology.");
        }
    }

    public Reasoner build() {
        return new Reasoner(localOntology);
    }

    /**
     * Adds a ontology to a local ontology.
     * Allows to merge multiple ontologies to a single local ontology.
     * @param ontology
     * @return
     */
    public ReasonerBuilder add(Ontology<?> ontology) {
        if (!(ontology instanceof OWLAPI3Ontology)) throw new IllegalArgumentException("Only OWLAPI3Ontology are supported currently.");

        OWLAPI3Ontology o = (OWLAPI3Ontology) ontology;

        for (OWLAxiom axiom : o.getOntology().getAxioms()) {
            manager.addAxiom(localOntology, axiom);
        }

        return this;
    }

    /**
     * Adds a Cell to the local ontology.
     * Requires that the ontologies have been added before.
     * @param elements
     * @return
     */
    public ReasonerBuilder add(Iterable<Cell> elements) {
        for (Cell cell : elements) {
            if (cell instanceof ObjectCell) {
                OWLAxiom axiom = correspondenceToAxiom((ObjectCell) cell);
                manager.addAxiom(localOntology, axiom);
            } else {
                throw new IllegalArgumentException("Only ObjectCells are allowed.");
            }
        }

        return this;
    }

    public OWLAxiom correspondenceToAxiom(ObjectCell co) {
        OWLDataFactory owlfactory = manager.getOWLDataFactory();

        // retrieve entity1 and entity2
        // create the axiom in function of their labels
        // use the local ontology -> requires that the ontologies used by the cell have been added
        Object e1 = co.getObject1();
        Object e2 = co.getObject2();
        Relation r = co.getRelation();

        try {
            if (localWrappedOntology.isClass(e1)) {
                if (localWrappedOntology.isClass(e2)) {
                    OWLClass entity1 = owlfactory.getOWLClass(IRI.create(localWrappedOntology.getEntityURI(e1)));
                    OWLClass entity2 = owlfactory.getOWLClass(IRI.create(localWrappedOntology.getEntityURI(e2)));
                    if (r instanceof EquivRelation) {
                        return owlfactory.getOWLEquivalentClassesAxiom(entity1, entity2);
                    } else if (r instanceof SubsumeRelation) {
                        return owlfactory.getOWLSubClassOfAxiom(entity2, entity1);
                    } else if (r instanceof SubsumedRelation) {
                        return owlfactory.getOWLSubClassOfAxiom(entity1, entity2);
                    } else if (r instanceof IncompatRelation) {
                        return owlfactory.getOWLDisjointClassesAxiom(entity1, entity2);
                    }
                } else if (localWrappedOntology.isIndividual(e2) && (r instanceof HasInstanceRelation)) {
                    return owlfactory.getOWLClassAssertionAxiom(owlfactory.getOWLClass(IRI.create(localWrappedOntology.getEntityURI(e1))),
                            owlfactory.getOWLNamedIndividual(IRI.create(localWrappedOntology.getEntityURI(e2))));
                }
            } else if (localWrappedOntology.isDataProperty(e1) && localWrappedOntology.isDataProperty(e2)) {
                OWLDataProperty entity1 = owlfactory.getOWLDataProperty(IRI.create(localWrappedOntology.getEntityURI(e1)));
                OWLDataProperty entity2 = owlfactory.getOWLDataProperty(IRI.create(localWrappedOntology.getEntityURI(e2)));
                if (r instanceof EquivRelation) {
                    return owlfactory.getOWLEquivalentDataPropertiesAxiom(entity1, entity2);
                } else if (r instanceof SubsumeRelation) {
                    return owlfactory.getOWLSubDataPropertyOfAxiom(entity2, entity1);
                } else if (r instanceof SubsumedRelation) {
                    return owlfactory.getOWLSubDataPropertyOfAxiom(entity1, entity2);
                } else if (r instanceof IncompatRelation) {
                    return owlfactory.getOWLDisjointDataPropertiesAxiom(entity1, entity2);
                }
            } else if (localWrappedOntology.isObjectProperty(e1) && localWrappedOntology.isObjectProperty(e2)) {
                OWLObjectProperty entity1 = owlfactory.getOWLObjectProperty(IRI.create(localWrappedOntology.getEntityURI(e1)));
                OWLObjectProperty entity2 = owlfactory.getOWLObjectProperty(IRI.create(localWrappedOntology.getEntityURI(e2)));
                if (r instanceof EquivRelation) {
                    return owlfactory.getOWLEquivalentObjectPropertiesAxiom(entity1, entity2);
                } else if (r instanceof SubsumeRelation) {
                    return owlfactory.getOWLSubObjectPropertyOfAxiom(entity2, entity1);
                } else if (r instanceof SubsumedRelation) {
                    return owlfactory.getOWLSubObjectPropertyOfAxiom(entity1, entity2);
                } else if (r instanceof IncompatRelation) {
                    return owlfactory.getOWLDisjointObjectPropertiesAxiom(entity1, entity2);
                }
            } else if (localWrappedOntology.isIndividual(e1)) {
                if (localWrappedOntology.isIndividual(e2)) {
                    OWLIndividual entity1 = owlfactory.getOWLNamedIndividual(IRI.create(localWrappedOntology.getEntityURI(e1)));
                    OWLIndividual entity2 = owlfactory.getOWLNamedIndividual(IRI.create(localWrappedOntology.getEntityURI(e2)));
                    if (r instanceof EquivRelation) {
                        return owlfactory.getOWLSameIndividualAxiom(entity1, entity2);
                    } else if (r instanceof IncompatRelation) {
                        return owlfactory.getOWLDifferentIndividualsAxiom(entity1, entity2);
                    }
                } else if (localWrappedOntology.isClass(e2) && (r instanceof InstanceOfRelation)) {
                    return owlfactory.getOWLClassAssertionAxiom(owlfactory.getOWLClass(IRI.create(localWrappedOntology.getEntityURI(e2))),
                            owlfactory.getOWLNamedIndividual(IRI.create(localWrappedOntology.getEntityURI(e1))));
                }
            }
        } catch (OntowrapException owex) {
            throw new RuntimeException("Error interpreting URI " + owex);
        }
        throw new RuntimeException("Cannot convert correspondence " + co);
    }
}
