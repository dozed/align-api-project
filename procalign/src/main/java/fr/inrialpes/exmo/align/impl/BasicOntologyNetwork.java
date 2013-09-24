/*
 * $Id: BasicOntologyNetwork.java 1620 2011-05-31 13:49:54Z jdavid $
 *
 * Copyright (C) INRIA, 2009-2010
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl; 

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id: BasicOntologyNetwork.java 1620 2011-05-31 13:49:54Z jdavid $ 
 */

public class BasicOntologyNetwork implements OntologyNetwork {

    protected Hashtable<URI,OntologyTriple> ontologies;
    protected HashSet<Alignment> alignments;
    
    protected HashMap<URI,Map<URI,Set<Alignment>>> onto2Align;

    public BasicOntologyNetwork(){
	ontologies = new Hashtable<URI,OntologyTriple>();
	alignments = new HashSet<Alignment>();
	onto2Align = new HashMap<URI,Map<URI,Set<Alignment>>>();
    }

    public void addOntology( URI onto ){
	if ( ontologies.get( onto ) == null )
	    ontologies.put( onto, new OntologyTriple( onto ) );
    };
    public void remOntology( URI onto ) throws AlignmentException {
	OntologyTriple ot = ontologies.get( onto );
	if ( ot != null ) {
	    for( Alignment al : ot.sourceAlignments ){
		remAlignment( al );
	    }
	    for( Alignment al : ot.targettingAlignments ){
		remAlignment( al );
	    }
	    ontologies.remove( onto ); // Or set to null
	    
	    onto2Align.remove(onto);
	    for (Map<URI,Set<Alignment>> m : onto2Align.values())
		m.remove(onto);  
	}
    };
    public void addAlignment( Alignment al ) throws AlignmentException {
	URI o1 = al.getOntology1URI();
	addOntology( o1 );
	ontologies.get( o1 ).sourceAlignments.add( al );
	URI o2 = al.getOntology2URI();
	addOntology( o2 );
	ontologies.get( o2 ).targettingAlignments.add( al );
	alignments.add( al );
	
	Map<URI,Set<Alignment>> m = onto2Align.get(al.getOntology1URI());
	if (m==null) {
	    m=new HashMap<URI,Set<Alignment>>();
	    onto2Align.put(al.getOntology1URI(), m);
	}
	Set<Alignment> aligns=m.get(al.getOntology2URI());
	if (aligns==null) {
	    aligns = new HashSet<Alignment>();
	    m.put(al.getOntology2URI(), aligns);
	}
	aligns.add(al);
    }; 
    public void remAlignment( Alignment al ) throws AlignmentException {
	ontologies.get( al.getOntology1URI() ).sourceAlignments.remove( al );
	ontologies.get( al.getOntology2URI() ).targettingAlignments.remove( al );
	alignments.remove( al );
	onto2Align.get(al.getOntology1URI()).get(al.getOntology2URI()).remove(al);
    };
    public Set<Alignment> getAlignments(){
	return alignments;
    };
    public Set<URI> getOntologies(){
	return ontologies.keySet(); // ??
    };
    public Set<Alignment> getTargetingAlignments( URI onto ){
	if (!ontologies.containsKey(onto)) return Collections.emptySet();
	return ontologies.get( onto ).targettingAlignments;
    };
    public Set<Alignment> getSourceAlignments( URI onto ){
	if (!ontologies.containsKey(onto)) return Collections.emptySet();
	return ontologies.get( onto ).sourceAlignments;
    };
    public void invert() throws AlignmentException {
	HashSet<Alignment> newal = new HashSet<Alignment>();
	for ( Alignment al : alignments ) newal.add( al.inverse() );
	for ( Alignment al : newal ) addAlignment( al );
    }

    public Set<Alignment> getAlignments(URI srcOnto, URI dstOnto) {
	Map<URI,Set<Alignment>> m = onto2Align.get(srcOnto);
	if (m!=null) {
	    Set<Alignment> aligns = m.get(dstOnto);
	    if (aligns!=null) return Collections.unmodifiableSet(aligns);
	}
	return Collections.emptySet();
    }

}

class OntologyTriple {

    public URI onto;
    public HashSet<Alignment> targettingAlignments;
    public HashSet<Alignment> sourceAlignments;

    OntologyTriple( URI o ){
	onto = o;
	targettingAlignments = new HashSet<Alignment>();
	sourceAlignments = new HashSet<Alignment>();
    }
}



