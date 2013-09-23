package fr.inrialpes.exmo.align.impl;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.net.URI;
import java.util.Collection;

/**
 * Transforms a URIAlignment to a ObjectAlignment
 */
public class AlignmentTransformer {

    public static ObjectAlignment toObjectAlignment(Alignment al) throws AlignmentException {
        if (al instanceof ObjectAlignment) {
            return (ObjectAlignment) al;
        } else if (al instanceof URIAlignment) {
            return toObjectAlignment((URIAlignment) al);
        } else {
            throw new AlignmentException("Cannot convert to ObjectAlignment : " + al);
        }
    }

    public static ObjectAlignment toObjectAlignment(URIAlignment al) throws AlignmentException {
        AlignmentTransformer t = new AlignmentTransformer();
        return t.asObjectAlignment(al);
    }

    public ObjectAlignment asObjectAlignment(URIAlignment al) throws AlignmentException {
        String f1 = al.getOntologyObject1().getFormalism();
        String f2 = al.getOntologyObject1().getFormalism();

        // improve this
        if ("INSTANCES".equals(f1) && "INSTANCES".equals(f2)) {
            try {
                return asObjectAlignmentFromInstanceMatching(al);
            } catch (OWLOntologyCreationException e) {
                throw new AlignmentException("Could not transform URIAlignment.", e);
            }
        } else {
            return asObjectAlignmentDefault(al);
        }
    }

    private ObjectAlignment asObjectAlignmentFromInstanceMatching(URIAlignment al) throws AlignmentException, OWLOntologyCreationException {

        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ont1 = manager.createOntology();
        OWLOntology ont2 = manager.createOntology();

        OWLClass thing = df.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));

        // the ObjectAlignment
        ObjectAlignment oa = new ObjectAlignment();

        // creates an individual for each match
        for (Cell cell : al) {
            URI uri1 = cell.getObject1AsURI();
            URI uri2 = cell.getObject2AsURI();

            OWLNamedIndividual ind1 = df.getOWLNamedIndividual(IRI.create(uri1));
            OWLNamedIndividual ind2 = df.getOWLNamedIndividual(IRI.create(uri2));

            OWLClassAssertionAxiom cax1 = df.getOWLClassAssertionAxiom(thing, ind1);
            manager.addAxiom(ont1, cax1);

            OWLClassAssertionAxiom cax2 = df.getOWLClassAssertionAxiom(thing, ind2);
            manager.addAxiom(ont2, cax2);

            Cell clone = oa.createCell(cell.getId(), ind1, ind2, cell.getRelation(), cell.getStrength());
            oa.addCell(clone);
        }

        oa.init(wrapOWLOntology(ont1), wrapOWLOntology(ont2));

        return oa;
    }

    private OWLAPI3Ontology wrapOWLOntology(OWLOntology o) {
        OWLAPI3Ontology onto = new OWLAPI3Ontology();
        onto.setOntology(o);
        onto.setFormalism(null);
        onto.setFormURI(null);
        onto.setURI(null);
        onto.setFile(null);
        return onto;
    }

    private ObjectAlignment asObjectAlignmentDefault(URIAlignment al) throws AlignmentException {
        ObjectAlignment alignment = new ObjectAlignment();
        try {
            alignment.init(al.getFile1(), al.getFile2());
        } catch (AlignmentException aex) {
            try { // Really a friendly fallback
                alignment.init(al.getOntology1URI(), al.getOntology2URI());
            } catch (AlignmentException xx) {
                throw aex;
            }
        }
        alignment.setType(al.getType());
        alignment.setLevel(al.getLevel());
        alignment.setExtensions(al.convertExtension("ObjectURIConverted", "fr.inrialpes.exmo.align.ObjectAlignment#toObject"));
        LoadedOntology<Object> o1 = (LoadedOntology<Object>) alignment.getOntologyObject1(); // [W:unchecked]
        LoadedOntology<Object> o2 = (LoadedOntology<Object>) alignment.getOntologyObject2(); // [W:unchecked]
        Object obj1 = null;
        Object obj2 = null;

        try {
            for (Cell c : al) {
                try {
                    obj1 = o1.getEntity(c.getObject1AsURI(alignment));
                } catch (NullPointerException npe) {
                    throw new AlignmentException("Cannot dereference entity " + c.getObject1AsURI(alignment), npe);
                }
                try {
                    obj2 = o2.getEntity(c.getObject2AsURI(alignment));
                } catch (NullPointerException npe) {
                    throw new AlignmentException("Cannot dereference entity " + c.getObject2AsURI(alignment), npe);
                }
                //System.err.println( obj1+"  "+obj2+"  "+c.getRelation()+"  "+c.getStrength() );
                if (obj1 == null)
                    throw new AlignmentException("Cannot dereference entity " + c.getObject1AsURI(alignment));
                if (obj2 == null)
                    throw new AlignmentException("Cannot dereference entity " + c.getObject2AsURI(alignment));
                Cell newc = alignment.addAlignCell(c.getId(), obj1, obj2,
                        c.getRelation(), c.getStrength());
                Collection<String[]> exts = c.getExtensions();
                if (exts != null) {
                    for (String[] ext : exts) {
                        newc.setExtension(ext[0], ext[1], ext[2]);
                    }
                }
            }
        } catch (OntowrapException owex) {
            throw new AlignmentException("Cannot dereference entity", owex);
        }
        return alignment;
    }


}
