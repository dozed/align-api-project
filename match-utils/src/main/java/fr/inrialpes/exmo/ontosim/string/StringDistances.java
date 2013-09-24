/*
 * $Id: StringDistances.java 126 2012-07-17 07:30:13Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2007, 2009
 * Except for the Levenshtein class whose copyright is not claimed to
 * our knowledge.
 * Copyright (C) University of Montréal, 2004-2005 for the tokenizer
 * This program was originaly part of the Alignment API implementation
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

/*
 * This class implements various string distances that can be used on
 * various kind of strings.
 *
 * This includes:
 * - equality
 * - subStringDistance
 * - pSubStringDistance [not implemented yet]
 * - lowenhein (edit) distance
 * - n-gram distance
 * - CERTH ISWC-2005 SMOA (calling org.ivml.alimo.ISub)
 * 
 * All of these are implemented as distances normalized by the
 * largest possible distance between two such strings.
 * They return doubles
 *
 * @author Jérôme Euzenat
 * @version $Id: StringDistances.java 126 2012-07-17 07:30:13Z euzenat $ 
 */

package fr.inrialpes.exmo.ontosim.string;

import java.util.Vector;

import org.ivml.alimo.ISub;

public class StringDistances {

    /*
     * subStringDistance
     * computes substring distance:
     * = 1 - (2 | length of longest common substring | / |s1|+|s2|)
     * return: 0 if both string are equal, 1 otherwise
     */
    public static double subStringDistance (String s1, String s2) {
	if (s1 == null || s2 == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	
	int l1 = s1.length(); // length of s
	int l2 = s2.length(); // length of t
		
	if ((l1 == 0) && ( l2 == 0 )) return 0.;
	if ((l1 == 0) || ( l2 == 0 )) return 1.;

	//int max = Math.min( l1, l2 ); // the maximal length of a subs
	int best = 0; // the best subs length so far
	    
	int i = 0; // iterates through s1
	int j = 0; // iterates through s2

	for( i=0; (i < l1) && (l1-i > best); i++ ){
	    j = 0;
	    while( l2-j > best ){
		int k = i;
		for( ; (j < l2 )
			 && (s1.charAt(k) != s2.charAt(j) ); j++) {};
		if ( j != l2 ) {// we have found a starting point
		    for( j++, k++; (j < l2) && (k < l1) && (s1.charAt(k) == s2.charAt(j)); j++, k++);
		    best = Math.max( best, k-i );
		}
	    }
	}
	return (1.0 - ((double)2*best / (double)(l1+l2)));
    }

    /* pSubStringDistance:
     * find all the common substrings (up to length 3)
     * sum their size / s1+s2
     */

    /*
     * equalDistance
     * return: 0 if both string are equal, 1 otherwise
     */
    public static double equalDistance (String s, String t) {
	if (s == null || t == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	if ( s.equals(t) ) { return 0.;} else {return 1.;}
    }

    /*
     * hammingDistance
     * return: the proportion of positions on which two strings differ
     */
    public static double hammingDistance (String s, String t) {
	if (s == null || t == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	int l1 = s.length();
	int l2 = t.length();
	int min = Math.min(l1, l2);
	int max = Math.max(l1, l2);

	int score = max;
	for( int i=0; i < min ; i++ )
	    if ( s.charAt(i) == t.charAt(i) ) score--;
	return (double)score/(double)max ;
    }

    /*
     * jaroMeasure as a dissimilarity (identical have 0.)
     * return: 
     * Original algorithm by Jérôme Euzenat.
     * It traverses both strings at the same time
     * finding the first match on s, then looking for the first on t
     * (and deciding if there is transposition or not on the fly)
     * before comming back to "s", etc.
     * This is certainly minimal in lines of code if not optimal
     */
    public static double jaroMeasure (String s, String t) {
	if (s == null || t == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	int l1 = s.length(); // length of s
	int l2 = t.length(); // length of t
	int span = Math.min(l1, l2)/2; // vicinity of search
	int i = 0; // index on s
	int j = 0; // index on t
	int comps = 0; // nb of char in s, close in t
	int compt = 0; // nb of char in t, close in s
	int transp = 0; // nb of char NOT transposed
	             // i.e., nb of char in s appearing in the same order in t
	char lastchars = '\0'; // last matched char in s
	while( i < l1 || j < l2 ){
	    if ( ( j < l2 && comps > compt ) || i > l1 ){
		// find a new match in compt
		for ( int k = Math.max(0,j-span); k < Math.min(l1,j+span); k++){
		    if ( t.charAt(j) == s.charAt(k) ){
			compt++;
			if ( t.charAt(j) == lastchars ) transp++;
			k = l1;
		    }
		}
		j++;
	    } else if ( i == l1 ) { // end of s
		lastchars = '\0'; // avoid matching with it
		i = l1+1; // so we will go to the previous clause now
	    } else { // comps
		for ( int k = Math.max(0,i-span); k < Math.min(l2,i+span); k++){
		    if ( t.charAt(k) == s.charAt(i) ){
			comps++;
			lastchars = s.charAt(i);
			k = l2;
		    }
		}
		i++;
	    }
	}
	if ( comps == 0. ) return 1.;
	else return 1.0 - ((double)comps/l1 + (double)compt/l2 + (double)transp/comps)/3.;
    }

    /*
     * jaroWinklerMeasure
     * return: 
     */
    public static double jaroWinklerMeasure (String s, String t) {
	int PREFIX = 4;
	double jaro = jaroMeasure( s, t );
	//int P = Math.max( PREFIX );// length or larger prefix
	return jaro + (double)PREFIX*(1 - jaro)/10;
    }

    /*
     * ngrammDistance
     * In fact 3-gramm distance
     * return: the ratio between the number of common n-grams over the
     *         total number of n-gramms in both strings. 
     */
    public static double ngramDistance(String s, String t) {
	int n = 3; // tri-grams for the moment
	if (s == null || t == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	int l1 = s.length()-n+1;
	int l2 = t.length()-n+1;
	int found = 0;
	for( int i=0; i < l1 ; i++ ){
	    for( int j=0; j < l2; j++){
		int k = 0;
		for( ; ( k < n ) && ( s.charAt(i+k) == t.charAt(j+k) ); k++);
		if ( k == n ) found++;
	    }
	}
	return 1.0 - (2*((double)found)/((double)(l1+l2)));
    }

    public static double levenshteinDistance (String s, String t) {
	return needlemanWunschDistance( s, t, 1 );
    }

    public static double needlemanWunsch2Distance (String s, String t) {
	return needlemanWunschDistance( s, t, 2 );
    }

    /* Pointer was provided in Todd Hugues (Lockheed)
       Taken from http://www.merriampark.com/ldjava.htm
       Initial algorithm by Michael Gilleland
       Integrated in Apache Jakarta Commons
       Improved by Chas Emerick
       This algorithm should be taken appart of this file and reset in the
       context of a proper package name with an acceptable license terms.
       Hopefully, Jakarta Commons will provide this.
       Modified by Jérôme Euzenat for returning normalized distance.
       Modified again by Jérôme Euzenat for computing Needleman-Wunsch.
       Bugfix 2009-08-10 by Gerard de Melo fixing Needleman-Wunsch initialization.
    */
    public static double needlemanWunschDistance (String s, String t, int gap) {
	if (s == null || t == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
		
	/* The difference between this impl. and the previous is that, rather 
	  than creating and retaining a matrix of size s.length()+1 by 
	  t.length()+1,
	  we maintain two single-dimensional arrays of length s.length()+1.
	  The first, d, is the 'current working' distance array that maintains
	  the newest distance cost counts as we iterate through the characters
	  of String s.  Each time we increment the index of String t we are 
	  comparing, d is copied to p, the second int[]. Doing so allows us
	  to retain the previous cost counts as required by the algorithm
	  (taking the minimum of the cost count to the left, up one, and
	  diagonally up and to the left of the current cost count being
	  calculated).
	  (Note that the arrays aren't really copied anymore, just switched...
	  this is clearly much better than cloning an array or doing a
	  System.arraycopy() each time  through the outer loop.)
	  
	  Effectively, the difference between the two implementations is this
	  one does not cause an out of memory condition when calculating the LD
	  over two very large strings.  		
	*/		
		
	int n = s.length(); // length of s
	int m = t.length(); // length of t
		
	if (n == 0) return 1.;
	else if (m == 0) return 1.;

	int p[] = new int[n+1]; //'previous' cost array, horizontally
	int d[] = new int[n+1]; // cost array, horizontally
	int _d[]; //placeholder to assist in swapping p and d

	// indexes into strings s and t
	int i; // iterates through s
	int j; // iterates through t

	char t_j; // jth character of t

	int cost; // cost

	for (i = 0; i<=n; i++) p[i] = i * gap;
	
	for (j = 1; j<=m; j++) {
	    t_j = t.charAt(j-1);
	    d[0] = j * gap;
	    
	    for (i=1; i<=n; i++) {
		cost = s.charAt(i-1)==t_j ? 0 : 1;
		// minimum of cell to the left+1, to the top+1,
		// diagonally left and up +cost				
		d[i] = Math.min(Math.min(d[i-1]+gap, p[i]+gap),  p[i-1]+cost);  
	    }
	    
	    // copy current distance counts to 'previous row' distance counts
	    _d = p;
	    p = d;
	    d = _d;
	} 

	// our last action in the above loop was to switch d and p, so p now 
	// actually has the most recent cost counts
	//System.err.println(s+" x "+t+" = "+p[n]);
	//return p[n];
	//return (double)p[n] / (double)Math.max( n, m );
	int min = Math.min( n, m );
	int diff = Math.max( n, m ) - min;
	return (double)p[n] / (double)(min + diff*gap);
    }

    /*
     * smoaDistance
     * A specialized distance for ontology matching identifiers
     * Calls the string matching method proposed in the paper
     * "A String Metric For Ontology Alignment", published in ISWC 2005 
     * It is implemented in a separate class provided by the authors and
     * available with this package
     * returns commonality - dissimilarity + winklerImprovement;
     */
    public static double smoaDistance (String s1, String s2) {
	if (s1 == null || s2 == null) {
	    //throw new IllegalArgumentException("Strings must not be null");
	    return 1.;
	}
	ISub metrics = new ISub();

	return 1.0 - metrics.score( s1, s2 );
    }
	
    /**
     * @param s a String
     * @return s without included quotations between ' or "
     */
    public static String stripQuotations( String s ) {
	int sLength = s.length();
	String result = "";
	int sStart = 0;
	int sEnd = sStart;
	while ( sStart < sLength ) {
	    while ( sEnd < sLength  && s.charAt(sStart) != '\"' ) sEnd++;
	    if ( sEnd < sLength ) result += s.substring(sStart, sEnd);
	    while ( sEnd < sLength  && s.charAt(sStart) != '\"' ) sEnd++;
	    sStart = sEnd;
	}
	return result;
    }

    /**
     * JE//: This should return a BagOfWords
     * the new tokenizer
     * first looks for non-alphanumeric chars in the string
     * if any, they will be taken as the only delimiters
     * otherwise the standard naming convention will be assumed:
     * words start with a capital letter
     * substring of capital letters will be seen as a whole
     * if it is a suffix
     * otherwise the last letter will be taken as the new token
     * start
     *
     * Would be useful to parameterise with stop words as well
     */
    public static Vector<String> tokenize( String s ) {
	String str1 = s;
	int sLength = s.length();
	Vector<String> vTokens = new Vector<String>();
		
	// 1. detect possible delimiters
	// starts on the first character of the string
	int tkStart = 0;
	int tkEnd = 0;
		
	// looks for the first delimiter
	// while (tkStart < sLength  && isAlpha (str1.charAt(tkStart))) {
	while (tkStart < sLength  && isAlphaNum (str1.charAt(tkStart))) {
	    tkStart++;
	}
		
	// if there is one then the tokens will be the
	// substrings between delimiters
	if (tkStart < sLength){
			
	    // reset start and look for the first token
	    tkStart = 0;
	    
	    // ignore leading separators
	    // while (tkStart < sLength && ! isAlpha (str1.charAt(tkStart))) {
	    while (tkStart < sLength  && ! isAlphaNum (str1.charAt(tkStart))) {
		tkStart++;
	    }

	    tkEnd = tkStart;

	    while (tkStart < sLength) {

		// consumption of the Alpha/Num token
		if (isAlpha (str1.charAt(tkEnd))) {
		    while (tkEnd < sLength  && isAlpha (str1.charAt(tkEnd))) {
			tkEnd++;
		    }
		} else {
		    while (tkEnd < sLength  && isNum (str1.charAt(tkEnd))) {
			tkEnd++;
		    }					
		}
		
		// consumption of the Num token
		vTokens.add(str1.substring(tkStart, tkEnd));
		
		// ignoring intermediate delimiters
		while (tkEnd < sLength  && !isAlphaNum (str1.charAt(tkEnd))) {
		    tkEnd++;
		}			
		tkStart=tkEnd;
	    }			
	} else { // else the standard naming convention will be used
	    // start at the beginning of the string
	    tkStart = 0;			
	    tkEnd = tkStart;

	    while (tkStart < sLength) {

		// the beginning of a token
		if (isAlpha (str1.charAt(tkEnd))){
					
		    if (isAlphaCap (str1.charAt(tkEnd))){
						
			// This starts with a Cap
			// IS THIS an Abbreviaton ???
			// lets see how maqny Caps
			while (tkEnd < sLength  && isAlphaCap (str1.charAt(tkEnd))) {
			    tkEnd++;
			}
			
			// The pointer is at:
			// a) string end: make a token and go on
			// b) number: make a token and go on
			// c) a small letter:
			// if there are at least 3 Caps,
			// separate them up to the second last one and move the
			// tkStart to tkEnd-1
			// otherwise
			// go on

			if (tkEnd == sLength || isNum (str1.charAt(tkEnd))) {
			    vTokens.add(str1.substring(tkStart, tkEnd));
			    tkStart=tkEnd;
			} else {
			    // small letter
			    if (tkEnd - tkStart > 2) {
				// If at least 3
				vTokens.add(str1.substring(tkStart, tkEnd-1));
				tkStart=tkEnd-1;
			    }
			}
			// if (isAlphaSmall (str1.charAt(tkEnd))){}
		    } else {
			// it is a small letter that follows a number : go on
			// relaxed
			while (tkEnd < sLength  && isAlphaSmall (str1.charAt(tkEnd))) {
			    tkEnd++;
			}
			vTokens.add(str1.substring(tkStart, tkEnd));
			tkStart=tkEnd;
		    }
		} else {
		    // Here is the numerical token processing
		    while (tkEnd < sLength  && isNum (str1.charAt(tkEnd))) {
			tkEnd++;
		    }
		    vTokens.add(str1.substring(tkStart, tkEnd));
		    tkStart=tkEnd;			
		}
	    }	
	}
	// PV: Debug 
	//System.err.println("Tokens = "+ vTokens.toString());		
	return vTokens;
    }
    
    public static boolean isAlphaNum(char c) {
	return isAlpha(c) || isNum(c);
    }
    
    public static boolean isAlpha(char c) {
	return isAlphaCap(c) || isAlphaSmall(c);
    }
    
    public static boolean isAlphaCap(char c) {
	return (c >= 'A') && (c <= 'Z');
    }

    public static boolean isAlphaSmall(char c) {
	return (c >= 'a') && (c <= 'z');
    }
    
    public static boolean isNum(char c) {
	return (c >= '0') && (c <= '9');
    }

}
