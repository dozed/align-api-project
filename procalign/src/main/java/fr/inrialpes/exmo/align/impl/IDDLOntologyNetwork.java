/*
 * $Id: IDDLOntologyNetwork.java 987 2009-05-27 13:48:33Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010, 2013
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

import java.util.ArrayList;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.LogicOntologyNetwork;

import fr.paris8.iut.info.iddl.IDDLReasoner;
import fr.paris8.iut.info.iddl.IDDLException;
import fr.paris8.iut.info.iddl.conf.Semantics;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id: BasicOntologyNetwork.java 987 2009-05-27 13:48:33Z euzenat $ 
 */

public class IDDLOntologyNetwork extends BasicOntologyNetwork implements LogicOntologyNetwork {

    IDDLReasoner reasoner = null;
    String semantics = "DL";

    protected void init() throws AlignmentException {
	//for( URI u : getOntologies() ){
	//	reasoner.addOntology( u );
	//}
	ArrayList<Alignment> allist = new ArrayList<Alignment>();
	for( Alignment al : alignments ){
	    //reasoner.addAlignment( al );
	    allist.add( al );
	}
	if ( reasoner == null ){
	    try {
		reasoner = new IDDLReasoner( allist );
	    } catch ( IDDLException iddlex ) {
		throw new AlignmentException( "Cannot initialise IDDLReasoner", iddlex );
	    }
	    setSemantics( semantics );
	}
    }

    public void setSemantics( String s ){
	semantics = s;
	if ( reasoner != null ) {
	    reasoner.setSemantics( Semantics.valueOf( s ) );
	}
    };
    public String getSemantics(){
	return semantics;
    };
    public boolean isConsistent() throws AlignmentException {
	init();
	return reasoner.isConsistent();
    }; 
    public boolean isEntailed( Alignment al ) throws AlignmentException {
	init();
	try {
	    return reasoner.isEntailed( al );
	} catch ( IDDLException idex ) {
	    throw new AlignmentException( "Cannot test entailment", idex );
	}
    };

}

