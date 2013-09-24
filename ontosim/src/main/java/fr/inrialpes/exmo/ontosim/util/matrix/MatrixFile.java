package fr.inrialpes.exmo.ontosim.util.matrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class MatrixFile<R, C> implements Matrix<R, C> {

    private final File f;
    private final RandomAccessFile matrixFile;
    //private long pos=0;
    
    private int rSeq=0;
    private int cSeq=0;
    
    private HashMap<R,Integer> rIdx=new LinkedHashMap<R,Integer>();
    private HashMap<C,Integer> cIdx=new LinkedHashMap<C,Integer>();
    
    public MatrixFile() {
	try {
	f=File.createTempFile("matrix", ".dat");
	matrixFile = new RandomAccessFile(f,"rw");
	}
	catch (IOException e) {throw new RuntimeException(e);}
    }	
    
    @Override
    public boolean containsCdim(C c) {
	return cIdx.containsKey(c);
    }

    @Override
    public boolean containsRdim(R r) {
	return rIdx.containsKey(r);
    }

    @Override
    public double get(R r, C c) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public Set<C> getDimC() {
	return cIdx.keySet();
    }

    @Override
    public Set<R> getDimR() {
	return rIdx.keySet();
    }

    @Override
    public Set<?> keySet() {
	Set<Object> keySet = new HashSet<Object>();
	keySet.addAll(rIdx.keySet());
	keySet.addAll(cIdx.keySet());
	return keySet;
    }

    @Override
    public void put(R r, C c, double value) {
	if (!rIdx.containsKey(r))
	    rIdx.put(r, rSeq++);
	if (!cIdx.containsKey(c))
	    cIdx.put(c, cSeq++);
	try {
	    	matrixFile.seek(matrixFile.length());
        	matrixFile.writeInt(rIdx.get(r));
        	matrixFile.writeInt(cIdx.get(c));
        	matrixFile.writeDouble(value);
	}
	catch (IOException e) {throw new RuntimeException("Not nossible to add value "+c+" for "+r+","+c,e);}
	
	
    }

    @Override
    public void putAll(Matrix<R, C> m) {
	//
    }

    @Override
    public MatrixDoubleArray<R, C> toArray() {
	double[][] vals = new double[rIdx.size()][cIdx.size()];
	for (int i=0 ; i<vals.length ; i++)
	    Arrays.fill(vals[i], 0);
	try {
	matrixFile.seek(0);
	while (matrixFile.getFilePointer()<matrixFile.length()) {
	    vals[matrixFile.readInt()][matrixFile.readInt()]=matrixFile.readDouble();
	}
	}
	catch (IOException e) {throw new RuntimeException(e);}
	/*for (int i=0 ; i<vals.length ; i++) {
	    StringBuffer b = new StringBuffer();
	    b.append('[');
	    for (int j=0 ; j<vals.length ; j++) {
		b.append(vals[i][j]);
		b.append(',');
	    }
	    b.deleteCharAt(b.length()-1);
	    b.append(']');
	    System.err.println(b);
	}*/
	return new MatrixDoubleArray<R,C>(new ArrayList<R>(rIdx.keySet()),new ArrayList<C>(cIdx.keySet()),vals);
    }

    @Override
    public MatrixDoubleArray<C, R> toArrayT() {
	double[][] vals = new double[cIdx.size()][rIdx.size()];
	for (int i=0 ; i<vals.length ; i++)
	    Arrays.fill(vals[i], 0);
	try {
	matrixFile.seek(0);
	while (matrixFile.getFilePointer()<matrixFile.length()) {
	    int r=matrixFile.readInt();
	    int c= matrixFile.readInt();
	    vals[c][r]=matrixFile.readDouble();
	}
	}
	catch (IOException e) {throw new RuntimeException(e);}
	
	return new MatrixDoubleArray<C,R>(new ArrayList<C>(cIdx.keySet()),new ArrayList<R>(rIdx.keySet()),vals);
    }

    @Override
    protected void finalize() throws Throwable {
	matrixFile.close();
	f.delete();
	super.finalize();
    }

}
