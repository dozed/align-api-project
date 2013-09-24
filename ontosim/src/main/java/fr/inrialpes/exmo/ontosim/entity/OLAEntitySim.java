/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   OLAEntitySim.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.Vector;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;

import ca.uqam.info.latece.sboa.impl.algorithms.OLAlignment;
import ca.uqam.info.latece.sboa.inter.ograph.OntologyGraph;
import ca.uqam.info.latece.sboa.inter.ograph.Vertice;
import ca.uqam.info.latece.sboa.inter.ograph.VerticesPool;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.util.AlignPairsSimple;

public class OLAEntitySim  extends OLAlignment implements Measure<Entity<OWLEntity>>  {

	private Map<OWLOntology,Set<OWLOntology>> alignedOnto;


	private AlignPairsSimple aps;

	public OLAEntitySim() {
		//extractor = ex;
		aps = new AlignPairsSimple();
		alignedOnto = new HashMap<OWLOntology, Set<OWLOntology>>();
	}

	public Alignment extractResults(OntologyGraph mGraph, Vector simVect, int[] indVect, int sz) {
		VerticesPool vPool = (VerticesPool)mGraph.getVerticePool();
		//BasicRelation eq = new BasicRelation("=");
		//double threshold = owg.getWeight(0);
		//alignRes.clear();
		for (int i = 0 ; i < sz ; i++){
			Vertice ver = (Vertice)vPool.get(indVect[i]);
			OWLEntity ent1 = ver.getFirstEntity();
			OWLEntity ent2 = ver.getSecondEntity();
			double val = simVect.get(i);
			//boolean auth1 = ot.haveAuthorizedURI(ent1,true);
			//boolean auth2 = ot.haveAuthorizedURI(ent2,true);
			boolean noInst = /*ver.getCategory().equalsIgnoreCase("Object")
								||*/ ver.getCategory().equalsIgnoreCase("PropertyInstance")
								|| ver.getCategory().equalsIgnoreCase("DataType")
								|| ver.getCategory().equalsIgnoreCase("DataValue")
								|| ver.getCategory().equalsIgnoreCase("Token")
								|| ver.getCategory().equalsIgnoreCase("Cardinality");
			if (/*(auth1 == true) && (auth2 == true) &&*/ (noInst == false)){
				aps.addPair(ent1,ent2,val);
			}
		}
		return this;
	}

	private void computeSims(OWLOntology o1, OWLOntology o2) {
		// execute OLA + fill tabs.
		try {
			if (alignedOnto.containsKey(o2)) {
				this.onto1.setOntology(o2);// = (Ontology<Object>) o2;
				this.onto2.setOntology(o1);// = (Ontology<Object>) o1;
			}
			else {
				this.onto1.setOntology(o1);
				this.onto2.setOntology(o2);
			}

			BasicParameters bp  = new BasicParameters();
			/*bp.setParameter("excludeURIs", "http://purl.org/dc/elements/1.1/");
			bp.setParameter("excludeURIs", "http://xmlns.com/foaf/0.1/");
			bp.setParameter("excludeURIs", "http://www.w3.org/2002/12/cal/ical#");*/
			this.align(null,(Parameters) bp);

			Set<OWLOntology> ontsMapped = alignedOnto.get(onto1.getOntology());
			if ( ontsMapped == null) {
				ontsMapped = new HashSet<OWLOntology>();
				alignedOnto.put((OWLOntology) onto1.getOntology(), ontsMapped);
			}
			ontsMapped.add((OWLOntology) onto2.getOntology());

		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	


	public void resetSim(){
	    aps = new AlignPairsSimple();
	    alignedOnto.clear();
	}


	public double getMeasureValue(Entity<OWLEntity> e1, Entity<OWLEntity> e2) {
		OWLOntology o1 = (OWLOntology) e1.getOntology().getOntology();
		OWLOntology o2 = (OWLOntology) e2.getOntology().getOntology();
		/*if (o1==null || o2==null) {
			return -1;
		}
		*/
		if ((!alignedOnto.containsKey(o1) || !alignedOnto.get(o1).contains(o2)) &&
				(!alignedOnto.containsKey(o2) || !alignedOnto.get(o2).contains(o1)))
			computeSims(o1,o2);

		double val = aps.getSimPair(e1.getObject(), e2.getObject());
		return val;//aps.getSimPair(e1.getObject(), e2.getObject());
	}


	public double getDissim(Entity<OWLEntity> e1, Entity<OWLEntity> e2) {
		return 1-getMeasureValue(e1,e2);
	}


	public double getSim(Entity<OWLEntity> e1, Entity<OWLEntity> e2) {
		return getMeasureValue(e1,e2);
	}

	public TYPES getMType() {
		return TYPES.similarity;
	}
}
