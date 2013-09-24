package fr.inrialpes.exmo.ontosim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DistFile {

    /**
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
	File f1 = new File(args[0]);
	
	int[] dist = new int[200];
	
	BufferedReader br = new BufferedReader(new FileReader(f1));
	
	String line=null;
	while ((line=br.readLine())!=null) {
	    String nb = line.substring(line.lastIndexOf(';')+1);
	    double val = Double.parseDouble(nb);
	    int idx = (int) (val/0.005);
	    //System.out.println(idx);
	    if (idx==dist.length) idx--;
	    dist[idx]++;
	 }
	
	for (int i=0 ; i< dist.length ; i++) {
	    System.out.println(i+"\t"+dist[i]);
	}

    }

}
