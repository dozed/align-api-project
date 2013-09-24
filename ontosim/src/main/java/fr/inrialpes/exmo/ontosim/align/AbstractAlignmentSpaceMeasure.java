/**
 *   Copyright 2008-2009, 2011 INRIA, Université Pierre Mendès France
 *   
 *   AbstractAlignmentSpaceMeasure.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package fr.inrialpes.exmo.ontosim.align;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.ontosim.AlignmentSpaceMeasure;
import fr.inrialpes.exmo.ontowrap.Ontology;

public abstract class AbstractAlignmentSpaceMeasure<S> implements AlignmentSpaceMeasure<S> {

    protected OntologyNetwork network;
 
   // protected Matrix<Ontology,Ontology> measureMatrix = new MatrixDouble<Ontology,Ontology>();

    //private Set<Alignment> alignments=new HashSet<Alignment>();
    private Map<URI,Set<Alignment>> uriToAlign = new HashMap<URI, Set<Alignment>>();

    public AbstractAlignmentSpaceMeasure() {
	super();
	network = new BasicOntologyNetwork();
    }

    public AbstractAlignmentSpaceMeasure( OntologyNetwork noo ) {
	super();
	network = noo;
    }

    public void setAlignmentSpace( OntologyNetwork noo ){
	network = noo;
    }

    public boolean addAlignment( Alignment al ) throws AlignmentException {
	network.addAlignment( al );
	return true; // I do not need to know. If I wanted I would have checked before
    }

	/*
	protected boolean addUriAlignMap(URI uri, Alignment a) {
		Set<Alignment> aligns = uriToAlign.get(uri);
		if (aligns==null) {
			aligns = new HashSet<Alignment>();
			uriToAlign.put(uri, aligns);
		}
		return aligns.add(a);
	}

	public boolean addAlignment(Alignment a) {
		if (alignments.add(a)) {
			try {
				addUriAlignMap(a.getOntology1URI(),a);
				addUriAlignMap(a.getOntology2URI(),a);
				return true;
			} catch (AlignmentException e) {
				alignments.remove(a);
				e.printStackTrace();
			}
		}
		return false;
	}
	*/

	protected Set<Alignment> getAlignments(Ontology<?> o1, Ontology<?> o2) {
	    Set<Alignment> result = new HashSet<Alignment>();
	    Set<Alignment> s1 = network.getSourceAlignments( o1.getURI() );
	    for ( Alignment al : network.getTargetingAlignments( o2.getURI() ) ) {
		if ( s1.contains( al ) ) result.add( al );
	    }
	    return result;

	    /*
		if ((uriToAlign.containsKey(o1.getURI()) && uriToAlign.containsKey(o2.getURI()))){
			Set<Alignment> aligns = new HashSet<Alignment>();
			for (Alignment a : uriToAlign.get(o1.getURI())) {
				try {
					if ( (a.getOntology1URI().equals(o1.getURI()) && a.getOntology2URI().equals(o2.getURI())) ||
							(a.getOntology1URI().equals(o2.getURI()) && a.getOntology2URI().equals(o1.getURI())) )
						aligns.add(a);
				} catch (AlignmentException e) {
					e.printStackTrace();
				}
			}
			return aligns;
		}
		return null;
	    */
	}


}
