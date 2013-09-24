/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Document.java is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.vector.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;


public class Document extends Observable{

	private String name;
	private int cardDoc=0;
	private Map<String,Integer> termsOcc = new HashMap<String, Integer>();


	public  Document(String name) {
		this.name=name;
	}

	/*
	 * add a term to the document
	 */
	public void addOccTerm(String term) {
		cardDoc++;
		if (! termsOcc.containsKey(term)) {
			termsOcc.put(term,new Integer(0));
		}
		int nbOcc = termsOcc.get(term).intValue()+1;
		termsOcc.put(term, new Integer(nbOcc));
		setChanged();
		notifyObservers(term);

	}

	public void addOccTerms(Collection<String> terms){
		for (String term : terms) {
			addOccTerm(term);
		}
	}

	public int getCardinality() {
		return this.cardDoc;
	}

	/**
	 * return the number of occurrences of the given term in the document
	 * @param term : a term
	 * @return the number of occurrences of given term if it apear in the document (0 if it does not appear)
	 */
	public int getNbOcc(String term) {
		if (!termsOcc.containsKey(term))
			return 0;
		return termsOcc.get(term).intValue();
	}


	public String[] getTerms() {
		String[] terms = new String[termsOcc.keySet().size()];
		return termsOcc.keySet().toArray(terms);
	}

	public boolean contains(String term) {
		return termsOcc.containsKey(term);
	}

	public double getTF(String term) {
		return ((double) getNbOcc(term))/cardDoc;
	}

	public String getName() {
	    return name;
	}

}
