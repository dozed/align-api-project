package fr.inrialpes.exmo.ontosim.util.measures;

/**
 *   Copyright 2008, 2009 Jérôme DAVID, EXMO LIG-INRIA Rhône Alpes, Université Pierre Mendès France
 *   
 *   MeasureFromFile.java is part of OntoSim.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.util.matrix.HalfMatrixDouble;

public class MeasureFromFile<O> implements Measure<O> {

    String measureName;
    public HalfMatrixDouble<O> values = new HalfMatrixDouble<O>();

    @SuppressWarnings("unchecked")
    public MeasureFromFile(File f, int index, String sep) throws Exception {
	BufferedReader br = new BufferedReader(new FileReader(f));
	String line=null;
	line=br.readLine();
	measureName =  line.split(sep)[index+2];
	while ((line=br.readLine())!=null) {
	    String[] lineTab = line.split(sep);
	    values.put((O)lineTab[0], (O)lineTab[1],
		    Double.valueOf(lineTab[index+2]).doubleValue());

	}
    }

    protected MeasureFromFile() {};

    @SuppressWarnings("unchecked")
    public static MeasureFromFile<URI>[] getMeasuresFromFile(File f, String sep) throws Exception {
	BufferedReader br = new BufferedReader(new FileReader(f));
	String line=null;
	line=br.readLine();
	String[] measuresName =  line.split(sep);
	MeasureFromFile<URI>[] measures = new MeasureFromFile[measuresName.length-2];
	for (int i=0 ; i < measures.length ; i++) {
	    measures[i]=new MeasureFromFile<URI>();
	}
	while ((line=br.readLine())!=null) {
	    String[] lineTab = line.split(sep);
	    for (int i=2 ; i < lineTab.length ; i++) {
		measures[i-2].values.put(URI.create(lineTab[0]), URI.create(lineTab[1]),
		    Double.valueOf(lineTab[i]).doubleValue());
	    }

	}
	return measures;
    }


    public double getDissim(O o1, O o2) {
	return 1- values.get(o1, o2);
    }

    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return TYPES.similarity;
    }

    public double getMeasureValue(O o1, O o2) {
	return values.get(o1, o2);
    }

    public double getSim(O o1, O o2) {
	return values.get(o1, o2);
    }



}
