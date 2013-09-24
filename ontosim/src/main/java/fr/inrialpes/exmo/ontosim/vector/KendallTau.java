package fr.inrialpes.exmo.ontosim.vector;

import java.util.Arrays;

import fr.inrialpes.exmo.ontosim.OntoSimException;

public class KendallTau extends VectorMeasure {

    @Override
    public double getMeasureValue(double[] v1, double[] v2) {
	int sum=0;
	for (int i=0 ; i<v1.length; i++) {
	    for (int j=i+1 ; j<v1.length ; j++) {
		double vij=(v1[i]-v1[j])*(v2[i]-v2[j]);
		if (vij>0) sum++;
		else if (vij<0) sum--;
	    }
	}
	
	double oldv=Double.NaN;	
	int sumT1=0;
	int nbT=0;
	double[] v1Copy = v1.clone(); 
	Arrays.sort(v1Copy);
	for (double v : v1Copy) {
	    if (v==oldv) nbT++; 
	    else  {
		sumT1+=nbT*(nbT+1);
		nbT=0;
		oldv=v;
	    }
	}
	oldv=Double.NaN;
	double sumT2=0;
	nbT=0;
	double[] v2Copy = v2.clone();
	Arrays.sort(v2Copy);
	for (double v : v2Copy) {
	    if (v==oldv) nbT++; 
	    else  {
		sumT2+=nbT*(nbT+1);
		nbT=0;
		oldv=v;
	    }
	}
	
	//System.out.println(sumT1+" - "+sumT2);
	int over=(v1.length*(v1.length-1));;
	
	return ((double)2*sum)/Math.sqrt((over-sumT1)*(over-sumT2));
    }	

    @Override
    public double getDissim(double[] o1, double[] o2) {
	throw new OntoSimException("Not a dissimilarity");
    }

    @Override
    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return TYPES.other;
    }

    @Override
    public double getSim(double[] o1, double[] o2) {
	throw new OntoSimException("Not a similarity");
    }

}
