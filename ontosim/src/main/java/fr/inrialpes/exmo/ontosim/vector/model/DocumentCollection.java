/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   DocumentCollection.java is part of OntoSim.
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
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;



public class DocumentCollection extends HashSet<Document> implements Observer {

    private static final long serialVersionUID = 1L;


	public static enum WEIGHT {TF,TFIDF};

	private TreeMap<String, Set<Document>> terms;

	public DocumentCollection(){
		super();
		terms = new TreeMap<String,Set<Document>>();
	}


	private void indexTermsOf(Document doc) {
		for (String term : doc.getTerms()) {
			addTermOcc(doc, term);
		}
	}

	private void addTermOcc(Document doc, String term) {
		if (!terms.containsKey(term))
			terms.put(term,new HashSet<Document>());
		terms.get(term).add(doc);
	}


	/**
	 * return the set of terms
	 * Be careful this method returns a reference to the set of terms and not a copy !!!
	 * @return a reference to the set of term contained in this tf.idf object
	 */
	public Set<String> getTerms() {
		return terms.keySet();
	}

	public String[] getDimensions() {
		String[] v = new String[terms.keySet().size()];
		return terms.keySet().toArray(v);
	}

	public double[] getTFIDFDocVector(Document doc) {
		if (! contains(doc))
			return null;
		double[] tfVector = new double[terms.size()];
		int i=0;
		for (String term : terms.keySet()) {
				tfVector[i] =Math.sqrt(doc.getTF(term))*(1+Math.log(((double) this.size())/(1+terms.get(term).size())));
				i++;
		}
		return tfVector;
	}

	public double[] getDocVector(Document doc, WEIGHT vectorType) {
		switch (vectorType) {
			case TF : return getTFDocVector(doc);
			case TFIDF : return getTFIDFDocVector(doc);
			default : return getTFDocVector(doc);
		}
	}

	public double[] getTFDocVector(Document doc) {
		if (! contains(doc))
			return null;
		double[] tfVector = new double[terms.size()];
		int i=0;
		for (String term : terms.keySet()) {
				tfVector[i] =Math.sqrt(doc.getTF(term));
				i++;
		}
		return tfVector;
	}

	public boolean add(Document o) {
		if (super.add(o)) {
			indexTermsOf(o);
			o.addObserver(this);
			return true;
		}
		return false;

	}

	public void clear() {
		super.clear();
		terms.clear();
	}


	public boolean remove(Document o) {
		boolean ok=true;
		super.remove(o);
		o.deleteObserver(this);
		for (String term : o.getTerms()) {
			ok = terms.get(term).remove(o);
		}

		return ok;
	}


	public void update(Observable o, Object arg) {
		if (Document.class.isInstance(o))
			addTermOcc((Document) o, (String) arg);
	}
}