/**
 *   Copyright 2008-2010 INRIA, Université Pierre Mendès France
 *   
 *   DegreeOfAgreement.java is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class DegreeOfAgreement {
    public static void main(String[] args) {
	PrintWriter logMatrix = null;
	ArrayList<ArrayList<String>> degrees = new ArrayList<ArrayList<String>>();				
	StringTokenizer st;
	ArrayList<String> measure;// = new ArrayList<String>();
	String[] out;
	String fileName = "";
	if (args[0].equals("-r")) {
	    //String[] headers = new String[91];
	    File file = new File(".");			
	    out = file.list();			
	    //robustness scenario - for comparing each degradated measure 10 times with non-degradated one
	    int j=0;
	    for (int i=0; i<=out.length-1; i++) {		
		//load closeness_matrixes from files
		fileName = out[i];
		degrees = new ArrayList<ArrayList<String>>();
		if (fileName.matches(".*.cls$")) {
		    System.out.println("for robustness "+fileName.substring(0,fileName.indexOf(".")));				
		    System.out.println(fileName.substring(0,fileName.indexOf(".")));
		    BufferedReader in = null;				
		    try {
			in = new BufferedReader(new FileReader (fileName));				
			String s="";
			//1st row headers
			//in.readLine();
			//int h=0;
			while ((s = in.readLine()) != null) {													
			    measure = new ArrayList<String>();						
			    st = new StringTokenizer(s,",");
			    //headers[h++]=st.nextToken();
			    st.nextToken();
			    while(st.hasMoreTokens()) {
				measure.add(st.nextToken());
			    }
			    degrees.add(measure);
			}
			//control printing:
			/*
			  for (ArrayList<String> ar : degrees) {
			  for(String ss : ar) {
			  System.out.println(ss+"|");
			  }
			  }
			*/
			//compare each degradation step (10%,20%,...) with non-degradated variant (0. position in degrees)
			logMatrix = new PrintWriter(new FileWriter(fileName.substring(0,fileName.indexOf("."))));
			//prepare header
			for(int r=1;r<=10;r++) logMatrix.print(",-");
			logMatrix.println(",avg,std");
			logMatrix.print("deg10%");
			//end of header
			int agreed=0;
			double sum=0;//for computation average and standard deviation						
			double degree;
			double[] values = new double[10];//for storing degree values enable to count standard deviation 
			for(int k=1;k<degrees.size();k++) {																																	
			    //compare degradated measure with nondegradated
			    agreed=0;							
			    for(int p=0;p<degrees.get(k).size();p++) {
				if(degrees.get(k).get(p).equals(degrees.get(0).get(p))) {
				    agreed++;									
				}
			    }					
			    //#agreed/#pair of ontologies
			    degree=(double)agreed/degrees.get(k).size();
			    if (k<=10) values[k-1]=degree;
			    else values[(k%10)]=degree;
			    //System.out.print(","+degree);
			    logMatrix.print(","+degree);
			    sum+=degree;
			    //System.out.println("("+k+",k,"+l+"l,"+agreed+" agreed)");													
			    //System.out.println();
			    if ((k % 10) == 0) {								
				double average = sum/10;
				logMatrix.print(","+average);//average
				//computation of standard deviation:
				double sd=0; 
				for (double d : values) {
				    sd+=(d-average)*(d-average);									
				}
				sd=Math.sqrt(sd/10);
				//end of computation standard deviation
				logMatrix.print(","+sd);//standard deviation
				logMatrix.println();
				//printing header
				if (k < 81)
				    logMatrix.print("deg"+(k+10)+"%");
				else if (k < 91) logMatrix.print("deg100%");
				values = new double[10];
				sum=0;
			    }							
			}
			//end of computation one degree_matrix comparing degradated vs. nondegradated variants of measures
		    }
		    catch(IOException e) {
			e.printStackTrace();
		    }
		    catch(Exception e) {
			e.printStackTrace();
		    }
		    logMatrix.close();
		}
	    }
	    j++;
	}
	else {//for comparing each-to-each measure without any degradation (-c), we see directly their agreement, or clusters
	    //out = new String[1];
	    //out[0]="degree1";			
	    //System.out.println(fileName.substring(0,fileName.indexOf(".")));				
	    BufferedReader in = null;				
	    try {
		in = new BufferedReader(new FileReader ("degree1"));				
		String s="";
		//1st row headers
		//in.readLine();
		while ((s = in.readLine()) != null) {						
		    measure = new ArrayList<String>();						
		    st = new StringTokenizer(s,",");
		    st.nextToken();
		    while(st.hasMoreTokens()) {
			measure.add(st.nextToken());
		    }
		    degrees.add(measure);
		}
		//for one closeness_matrix compute degree of agreement between all measures
		logMatrix = new PrintWriter(new FileWriter("degree1.dgr"));
		int agreed=0;
		double degree;
		for(int k=0;k<degrees.size();k++) {			
		    for(int l=0;l<degrees.size();l++) {				
			//if (l<=k) System.out.print(",-");
			if (l<=k) logMatrix.print(",-");
			else {
			    //compare two measures and compute degree of agreement
			    agreed=0;
			    for(int p=0;p<degrees.get(k).size();p++) {
				if(degrees.get(k).get(p).equals(degrees.get(l).get(p))) {
				    agreed++;
				}
			    }					
			    //#agreed/#pair of ontologies
			    degree=(double)agreed/degrees.get(k).size();
			    //System.out.print(","+degree);
			    logMatrix.print(","+degree);
			    //System.out.println("("+k+",k,"+l+"l,"+agreed+" agreed)");
			}
		    }
		    //System.out.println();
		    logMatrix.println();
		}
		//end of computation one degree_matrix
	    }
	    catch(IOException e) {
		e.printStackTrace();
	    }
	    logMatrix.close();
	}									
    }
}
