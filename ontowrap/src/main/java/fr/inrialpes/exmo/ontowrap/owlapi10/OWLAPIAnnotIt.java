/*
 * $Id: OWLAPIAnnotIt.java 896 2008-11-25 14:45:46Z jdavid $
 *
 * Copyright (C) INRIA, 2007-2008, 2010
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

package fr.inrialpes.exmo.ontowrap.owlapi10;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * An iterator over annotations of an OWLEntity. This class permits to avoid the instantiation of HashSet for
 * each call of a getAnnotation method.
 * @author JD
 *
 */
public class OWLAPIAnnotIt implements Iterator<String> {

	/*private OWLOntology o;
	private OWLEntity e;*/
	private String lang;
	private String typeAnnot;

	private Iterator it;

	private String currentElem=null;

	public OWLAPIAnnotIt(OWLOntology o, OWLEntity e , String lang , String typeAnnot) throws OWLException {
	    /*this.o=o; this.e=e; */ this.lang=lang; this.typeAnnot=typeAnnot;
	    it = e.getAnnotations(o).iterator();
	}

	public boolean hasNext() {
	    try {
		setNext();
		return currentElem != null;
	    }
	    catch (NoSuchElementException e) {
		return false;
	    }
	}

	public String next() {
	    setNext();
	    String returnVal = currentElem;
	    currentElem=null;
	    return returnVal;
	}

	private void setNext() throws NoSuchElementException {
	    while (currentElem==null) {
        	    OWLAnnotationInstance annot = (OWLAnnotationInstance) it.next();
        	    try {
        		String annotUri = annot.getProperty().getURI().toString();
        		if (annotUri.equals(typeAnnot) || typeAnnot==null) {
        		    if ( annot.getContent() instanceof OWLDataValue &&
            			( lang==null || ((OWLDataValue) annot.getContent()).getLang().equals(lang)) ) {
        			currentElem = ((OWLDataValue) annot.getContent()).getValue().toString();
            			}
        		}
        	    } catch (OWLException e) {
        		e.printStackTrace();
        		currentElem=null;
        	    }
	    }

	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}

    }

