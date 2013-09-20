package fr.inrialpes.exmo.align.impl;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import java.util.Collection;

/**
 * Transforms a URIAlignment to a ObjectAlignment
 */
public class AlignmentTransformer {

    public static ObjectAlignment toObjectAlignment( URIAlignment al ) throws AlignmentException {
        AlignmentTransformer t = new AlignmentTransformer();
        return t.asObjectAlignment(al);
    }

    public ObjectAlignment asObjectAlignment(URIAlignment al) throws AlignmentException {
        String f1 = al.getOntologyObject1().getFormalism();
        String f2 = al.getOntologyObject1().getFormalism();

        // improve this
        if ("INSTANCES".equals(f1) && "INSTANCES".equals(f2)) {
            return asObjectAlignmentFromInstanceMatching(al);
        } else {
            return asObjectAlignmentDefault(al);
        }
    }

    private ObjectAlignment asObjectAlignmentFromInstanceMatching(URIAlignment al) {
        ObjectAlignment alignment = new ObjectAlignment();



        return alignment;
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
