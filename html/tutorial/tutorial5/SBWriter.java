package example.ws.matcher

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SBWriter extends PrintWriter {

       StringBuffer sb;
	
	public SBWriter(String string) throws FileNotFoundException {
		super(string);
	}
	
	public SBWriter(BufferedWriter bufferedWriter, boolean b) throws FileNotFoundException  {
		super(bufferedWriter, b);
		this.sb = new StringBuffer();
		
	}	
	
	public void print(String s) {
		sb.append(s);
	}

	public String toString() {
		return this.sb.toString();
	}
}
