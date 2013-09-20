package fr.inrialpes.exmo.align.impl.eval;

import com.sun.javafx.collections.IterableChangeBuilder;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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

    public ReasonerBuilder() {
        manager = OWLManager.createOWLOntologyManager();
        try {
            localOntology = manager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Could not create local ontology.");
        }
    }

    public Reasoner build() {
        return new Reasoner(localOntology);
    }

    public ReasonerBuilder add(Ontology<?> ontology) {
        if (!(ontology instanceof OWLAPI3Ontology)) throw new IllegalArgumentException("Only OWLAPI3Ontology are supported currently.");

        OWLAPI3Ontology o = (OWLAPI3Ontology) ontology;

        for (OWLAxiom axiom : o.getOntology().getAxioms()) {
            manager.addAxiom(localOntology, axiom);
        }

        return this;
    }

    public ReasonerBuilder add(List<Cell> elements) {


        return this;
    }
}
