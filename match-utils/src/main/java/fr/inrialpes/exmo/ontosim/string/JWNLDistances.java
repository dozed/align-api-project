/*
 * $Id: JWNLDistances.java 111 2011-05-27 09:18:55Z jdavid $
 *
 * Copyright (C) University of Montr�al, 2004-2005
 * Copyright (C) INRIA, 2004-2005, 2007-2011
 * This program was originaly part of the Alignment API implementation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.ontosim.string;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.WeakHashMap;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import fr.inrialpes.exmo.ontosim.OntoSimException;

/**
 * Compute a string distance using the JWNL API (WordNet API)
 * and sometime Lucene
 * @author Jerome Pierson, David Loup, Petko Valtchev, Jerome Euzenat
 * @version $Id: JWNLDistances.java 111 2011-05-27 09:18:55Z jdavid $
 *
 */

public class JWNLDistances {

    public static final double NOUN_WEIGHT = 0.60;
    public static final double ADJ_WEIGHT  = 0.25;
    public static final double VERB_WEIGHT = 0.15;
    private static final double MINIMUM_DISTANCE = 0.05;

    private static Dictionary dictionary = null;

    // Uses standard English stopWords because this is wordnet (it is possible to change this)
    @SuppressWarnings("unchecked") // Lucene no 1.5 ?
    private static Set<String> stopWords;

    // Results tables
    double[][]                 nounsResults;
    double[][]                 verbsResults;
    double[][]                 adjectivesResults;

    // Weights tables (masks)
    double[][]                 nounsMasks;
    double[][]                 verbsMasks;
    double[][]                 adjectivesMasks;
    
    // tokens depending on their nature
    // PG: These are now global variables.
    private Hashtable nouns1        = new Hashtable();
    private Hashtable adjectives1   = new Hashtable();
    private Hashtable verbs1        = new Hashtable();
    private Hashtable nouns2        = new Hashtable();
    private Hashtable adjectives2   = new Hashtable();
    private Hashtable verbs2        = new Hashtable();
    
    @SuppressWarnings("unchecked") // ENGLISH_STOP_WORDS_SET declared as Set
    public JWNLDistances() throws OntoSimException {
	if ( stopWords == null ) {
	    stopWords = (Set<String>)StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	}
    }

    /**
     * Initialize the JWNL API. Must be done one time before computing distance
     * Need to configure the file_properties.xml located in the current
     * directory
     */
    public void Initialize() throws OntoSimException {
	Initialize( (String)null, (String)null );
    }

    public void Initialize( String wordnetdir, String wordnetversion ) throws OntoSimException {
	if ( !JWNL.isInitialized() ) {
	    InputStream pptySource = null;
	    if ( wordnetdir == null ) {
		try {
		    pptySource = new FileInputStream( "./file_properties.xml" );
		} catch ( FileNotFoundException e ) {
		    throw new OntoSimException( "Cannot find WordNet dictionary: use -Dwndict or file_property.xml" );
		}
	    } else {
		String properties = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		properties += "<jwnl_properties language=\"en\">";
		properties += "  <resource class=\"PrincetonResource\"/>";
		properties += "  <version publisher=\"Princeton\" number=\""+wordnetversion+"\" language=\"en\"/>";
		properties += "  <dictionary class=\"net.didion.jwnl.dictionary.FileBackedDictionary\">";
		properties += "     <param name=\"dictionary_element_factory\" value=\"net.didion.jwnl.princeton.data.PrincetonWN17FileDictionaryElementFactory\"/>";
		properties += "     <param name=\"file_manager\" value=\"net.didion.jwnl.dictionary.file_manager.FileManagerImpl\">";
		properties += "       <param name=\"file_type\" value=\"net.didion.jwnl.princeton.file.PrincetonRandomAccessDictionaryFile\"/>";
		properties += "       <param name=\"dictionary_path\" value=\""+wordnetdir+"\"/>";
		properties += "     </param>";
		properties += "  </dictionary>";
		properties += "</jwnl_properties>";
		// Sorry but this initialize wants to read a stream
		pptySource = new ByteArrayInputStream( properties.getBytes() );
	    }

	    // Initialize
	    try {
		JWNL.initialize( pptySource );
	    } catch ( JWNLException e ) {
		throw new OntoSimException( "Cannot initialize JWNL (WordNet)", e );
	    }
	    dictionary = Dictionary.getInstance();
	}
    }

    /**
     * Provides the oportunity to cache pretreatments in measures which require them
     * Using this requires to set up initPreCache() before using the cache and please
     * cleanPreCache() after. This can only improve the performances.
     */

    protected WeakHashMap cache;

    public void initPreCache() {
	cache = new WeakHashMap<String,Object>();
    }

    public void cleanPreCache() { cache = null; }

    /**
     * Reads a file containing one stopword per line
     * Returns these stop words as a set of strings
     * Set the defaults stopWords with this list
     */
    public Set<String> loadStopWordsFromFile( String filename ) throws IOException, FileNotFoundException {
	stopWords = new HashSet<String>();
	StreamTokenizer st = new StreamTokenizer( new FileReader( filename ));
	st.eolIsSignificant( true );
	st.ordinaryChar(' ');
	st.ordinaryChar('-');
	boolean eof = false;
	String str = "";
	while ( !eof ) {
	    int tk = st.nextToken();
	    switch (tk) {
	    case StreamTokenizer.TT_EOF : eof = true; break;
	    case StreamTokenizer.TT_EOL : stopWords.add( str ); str = ""; break;
		//case StreamTokenizer.TT_NUMBER : str += st.nval.toString(); break;
	    case StreamTokenizer.TT_WORD : str += st.sval; break;
	    }
	}
	return stopWords;
    }

    /**
     * Compute a basic distance between 2 strings using WordNet synonym.
     * @param s1
     * @param s2
     * @return Distance between s1 & s2 (return 1 if s2 is a synonym of s1, else
     *         return a BasicStringDistance between s1 & s2)
     */
    public double basicSynonymDistance( String s1, String s2 ) {
        double Dist = 0.0;
        double Dists1s2;
        int j, k = 0;
        int synonymNb = 0;
        int besti = 0;
	int bestj = 0;
        double DistTab[];
        IndexWord index = null;
        Synset Syno[] = null;

        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        Dists1s2 = StringDistances.subStringDistance(s1,s2);

        try {
            // Lookup for first string
            index = dictionary.lookupIndexWord(POS.NOUN,s1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        // if found in the dictionary
        if (index != null) {
            try {
                // get the groups of synonyms for each sense
                Syno = index.getSenses();
            } catch (JWNLException e) {
                e.printStackTrace();
            }
            // number of senses for the word s1
            synonymNb = index.getSenseCount();
            DistTab = new double[synonymNb];
            // for each sense
            for (k = 0; k < synonymNb; k++) {
                // for each synonym of this sense
                for (j = 0; j < Syno[k].getWordsSize(); j++) {
                    Dist = StringDistances.subStringDistance(Syno[k].getWord(j)
                            .getLemma(),
                        s2);
                    if (Dist < Dists1s2) {
                        Dists1s2 = Dist;
                        besti = k;
                        bestj = j;
                    }
                }
            }
        }

        return Dists1s2;
    }

    /**
     * Retrieve all WordNet senses of a term
     * @param term
     * @return the set of senses of term
     */

    @SuppressWarnings("unchecked") // WordNet non-1.5
    Set<Synset> getAllSenses( String term ) throws OntoSimException {
	Set<Synset> res = new HashSet<Synset>();
	IndexWordSet iws = null;
	try {
	    iws = dictionary.lookupAllIndexWords( term );
	} catch ( JWNLException ex ) {
	    throw new OntoSimException( "Wordnet exception", ex );
        }
	if ( iws != null ) {
	    // not iterable...
	    for ( IndexWord idx : (Collection<IndexWord>)iws.getIndexWordCollection() ) {
		Synset Syno[] = null;
		try {
		    // get the synsets for each sense
		    Syno = idx.getSenses();
		} catch ( JWNLException jwnlex ) {
		    throw new OntoSimException( "Wordnet exception", jwnlex );
		    //jwnlex.printStackTrace();
		}
		// number of senses for the word s1
		int synonymNb = idx.getSenseCount();
		// for each sense
		for ( int k = 0; k < synonymNb; k++ ) {
		    res.add( Syno[k] );
		}
	    }
	}
	return res;
    }

    /**
     * Cache method for synsets
     */
    @SuppressWarnings("unchecked") // Only one cache... my bad
    protected Set<Synset> computeSynsets( String s ) throws OntoSimException {
	String term = s.toLowerCase();
	if ( cache != null && cache.containsKey( term ) ) {
	    return (Set<Synset>)cache.get( term );
	} else {
	    Set<Synset> sense = getAllSenses( term );
	    if ( cache != null ) cache.put( term, sense );
	    return sense;
	}
    }

    /**
     * Compute the proportion of common synset between two words
     * @param s1 a String
     * @param s2 a String
     * @return the proportion of common synonyms shared by both terms
     */
    public double cosynonymySimilarity( String s1, String s2 ) throws OntoSimException {
	Set<Synset> sense1 = computeSynsets( s1 );
	Set<Synset> sense2 = computeSynsets( s2 );
        // if found in the dictionary
        if ( sense1 != null && sense2 != null ) {
	    //System.err.print( "Success : "+s1+" / "+s2 );
	    int union = sense1.size();
	    int inter = 0;
	    // For all senses of s2
	    for ( Synset s : sense2 ){
		if ( sense1.contains( s ) ) {
		    inter++;
		} else {
		    union++;
		}
	    }
	    if ( union == 0 ) return 0.;
	    //System.err.println( "= "+inter+" / "+union );
	    return ((double)inter)/((double)union);
        } else {
	    //System.err.println( "Failure : "+s1+" / "+s2 );
	    return 1. - StringDistances.equalDistance(s1.toLowerCase(),s2.toLowerCase()); 
	}
    }

    /**
     * Evaluate if two terms can be synonym
     * @param s1 a String
     * @param s2 a String
     * @return 1 if strings are equal or s2 is a synonym of s1
     *
     * Note this is asymmetric: it is assumed that WordNet is symmetric
     */

    public double basicSynonymySimilarity( String s1, String s2 ) throws OntoSimException {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

	if ( s1.equals( s2 ) ) return 1.;
	else {
	    Set<Synset> sense1 = computeSynsets( s1 );
	    if ( sense1 != null ) {
		for ( Synset s : sense1 ){
		    if ( s.containsWord( s2 ) ) return 1.;
		}
	    }
	    return 0.; 
	}
    }

    /**
     * Compute the overlap between all glosses of two strings
     * @param s1 a String
     * @param s2 a String
     * @return a measure of overlap of their glosses in WordNet based on the following treatments:
     * - take gloss for all senses and add the term name;
     * - suppress quotations ('...');
     * - suppress empty words (or, and, the, a, an, for, of, etc.);
     * [- suppress technical vocabulary, e.g., 'term';]
     * [- suppress empty phrases, e.g., 'usually including';]
     * - keep categories, e.g., law;
     * - stem words.
     * The results are sets (not bags, so there is no repetition) of words and compared with:
     * | g(t1) \cap g(t2) | / | g(t1) \cup g(t2) |
     *
     */
    public double basicGlossOverlap( String s1, String s2 ) throws OntoSimException {
	Collection<String> st1 = computeGlossValue( s1 );
	Collection<String> st2 = computeGlossValue( s2 );
	// Compute measure
	if ( st1 == null || st2 == null ) {
	    // JE: no maybe a simple string distance anyway
	    // but why this one?
	    return 1. - StringDistances.subStringDistance(s1,s2); 
	}
	int common = 0;
	for ( String s : st1 ) {
	    if ( st2.contains( s ) ) common++;
	}
	return (double)common/(double)(st1.size()+st2.size()-common);
    }

    /**
     * Cache method for glosses
     */
    @SuppressWarnings("unchecked") // Only one cache... my bad
    protected Collection<String> computeGlossValue( String s ) throws OntoSimException {
	// Strange to uppercase...
	String term = s.toLowerCase();
	if ( cache != null && cache.containsKey( term ) ) {
	    return (Collection<String>)cache.get( term );
	} else {
	    Collection<String> st = null;
	    // Collect gloss
	    String gloss = term;
	    // if found in the dictionary
	    gloss += " "+getGlossForLabel( term );
	    try {
		// Clean-up gloss
		// Tokenize gloss
		st = tokenizeGloss( gloss );
	    } catch ( IOException ioex ) {
		ioex.printStackTrace(); // should never occur
	    }
	    // This uses our home-made tokenizer (rather use Lucene maybe)
	    //OLD: st = StringDistances.tokenize( gloss );
	    if ( cache != null ) cache.put( term, st );
	    return st;
	}
    }

    /**
     * Takes a gloss-like string (text) and returns it tokenized.
     * with:
     * - stopwords
     * - lower case
     * - porter stemmer
     */
    protected Set<String> tokenizeGloss( String s ) throws IOException {
	Set<String> result = new HashSet<String>();
	// I am affraid that I am reimplementing the StandardAnalizer...
	TokenStream ts = new PorterStemFilter(
				new StopFilter( true, 
                                       new LowerCaseTokenizer( 
                                              new StringReader( s ) ), stopWords, true ));
	TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
	while ( ts.incrementToken() ) {
	    result.add( termAtt.term() );
	}
	return result;
    }

    protected String getGlossForLabel( String s ) {
	String norm = splitStringForWordNet( s );
	String text = norm+"."; // add the label as gloss
	String result = getGlossForLabel1( norm );
	// Could also be done systematically
	if ( "".equals( result ) ) {
	    try {
		for ( String subterm : tokenizeGloss( norm ) ){
		    text += getGlossForLabel1( subterm );
		}
	    } catch (IOException ioex) {}
	} else {
	    text += result;
	}
	return text;
    }

    /**
     * Fetches all the glosses from wordnet for the given term and concatenate them (without quotations).
     */
    protected String getGlossForLabel1( String s ) {
	String text = "";
	for( Synset ss : getAllSenses( s ) ){
	    text += " "+ss.getGloss().replaceAll("\\\"[^\"]*\\\"", "")+".";
	}
	return text;
    }

    /**
     * Retains only strings made of lowercase/uppercase characters
     * Suppress numbers
     * Split strings when they contain LowercaseUppercase "/" ":" "_" "\" "+" "." "*" "&"
     * But not "-" or "@" taken into account by Lucene
     */
    protected String splitStringForWordNet( String s ) {
	String result ="";
	int last = 0;
	int pos = 0;
	int len = s.length();
	while ( pos < len ) { // case of 0
	    char c = s.charAt( pos );
	    if ( c == '/' || c == ':' || c == '_' || c == '\\' || c == '+' || c == '&' || c == '.' || c == '-' ) {
		if ( pos > 0 ) result += s.substring( last, pos-1 )+" ";
		last = pos;
	    } else if ( c >= '0' && c <= '9' ) {
		if ( pos > 0 ) result += s.substring( last, pos );
		last = pos+1;
	    } else if ( pos != 0 && c >= 'A' && c <= 'Z' && s.charAt( pos-1 ) >= 'a' && s.charAt( pos-1 ) <= 'z' ) {
		result += s.substring( last, pos )+" ";
		last = pos;
	    }
	    pos++;
	}
	result += s.substring( last, pos );
	//System.err.println( "* "+result+" *" );
	return result;
    }

    /**
     * Compute the Wu-Palmer similarity defined by
     * score = 2*depth(lcs(s1,s2)) / (depth(s1) + depth(s2))
     * @param s1
     * @param s2
     * @return the Wu-Palmer similarity
     * The algorithm returns the best Wu-Palmer similarity among the pairs
     * of synsets corresponding to s1 and s2
     *
     * Assumption: JE**1: root is when no hypernyms exists...
     *
     * Sketch:
     * 1) full depth-first search from s1 with record shortest path distance from s1 and depth
     * 2) depth-first search from s2 until reached lcs with record the best Wu-Palmer
     *
     * NOTE: The first phase (on s1) is a preprocessing step.
     * In the case when the user want to compute a whole Wu-Palmer matrix,
     * this step is made |s2| times: it may be worth caching this step
     */
    public double wuPalmerSimilarity( String s1, String s2 ) throws OntoSimException {
	// For each encountered node, record:
	// [0] how far is it from s1
	// [1] how far is it from s2
	// [2] how far is it from a root (depth)
	Hashtable<Synset,int[]> depth = new Hashtable<Synset,int[]>();

	// Strange to uppercase...
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
	if ( s1.equals( s2 ) ) return 1.;

	Set<Synset> sense1 = computeSynsets( s1 );
	Set<Synset> sense2 = computeSynsets( s2 );
	if ( sense1 == null || sense2 == null ) return 0.;

	// Traverse the graph from s1 and collect distance
	Stack<Synset> queue = new Stack<Synset>();
	for ( Synset s : sense1 ) { // Stack s ith 0
	    int[] v = new int[3]; v[0]=0; v[1]=-1; v[2]=-1;//{ 0, -1, -1 };
	    depth.put( s, v );
	    queue.push( s );
	}
	// Traversal from s1 (marking the distance from start)
	// (introducing distance from top)
	Stack<Synset> passed = new Stack<Synset>();
	while ( !queue.empty() ) { // Stack non empty
	    //System.err.println("QUEUE: "+queue);
	    //System.err.println("PASSED: "+passed);
	    Synset curnode = queue.pop(); // Unstack
	    int[] curval = depth.get( curnode );
	    int curdepth = curval[0]; // Retrieve depth
	    //System.err.println(">> ["+curdepth+"] "+curnode);
	    try {
		PointerTarget[] hyps = curnode.getTargets( PointerType.HYPERNYM );
		if ( hyps.length == 0 ) { // JE**1: Hitting a root
		    //System.err.println("  == ROOT");
		    int level = 0;
		    curval[2] = level;
		    // Mark second queue
		    boolean firstmark = false; 
		    for ( int i = passed.size()-1; i >= 0; i-- ){
			Synset current = passed.get(i);
			if ( !firstmark ) passed.pop(); // unstack until first mark
			if ( current != null ) {
			    level++;
			    //System.err.println("  <== ("+level+") "+current);
			    int[] val = depth.get( current ); // record depth
			    if ( val[2] == -1 || val[2] > level ) val[2] = level;
			} else { firstmark = true; } // end of poping after first mark
		    }
		} else {
		    passed.push( curnode ); // stack me
		    for ( PointerTarget s : hyps ) {
			if ( s instanceof Synset ){
			    Synset current = (Synset)s;
			    int[] val = depth.get( current );
			    //System.err.println("  -> "+current);
			    if ( val == null ){ // not encounted yet
				int[] v = new int[3]; v[0]=curdepth+1; v[1]=-1; v[2]=-1;
				//int[] v = { curdepth+1, -1, -1 };
				depth.put( current, v );
				queue.push( current );
				passed.push( (Synset)null );
				//System.err.println("  - pushed(1) "+v[0]);
			    } else if ( val[0] > curdepth+1 ) { // updating shortpath
				val[0] = curdepth+1;
				queue.push( current );
				passed.push( (Synset)null );
				//System.err.println("  - pushed(2) "+val[0]);
			    } else { // We must unstack here
				//System.err.println("  == MEET");
				int level = val[0];
				// Mark second queue
				for ( int i = passed.size()-1; i >= 0; i-- ){
				    Synset n = passed.get(i);
				    if ( n != null ) {
					level++;
					//System.err.println("  <== ("+level+") "+n);
					int[] v = depth.get( n ); // record depth
					if ( v[2] == -1 || v[2] > level ) v[2] = level;
				    }
				}
			    }
			}
		    }
		    // Either unstack the last mark or s if nothing has been put in queue
		    passed.pop();
		}
	    } catch ( JWNLException ex ) {}
	}

	// Traverse the graph from s2 and collect distance
	double bestvalue = 0.;
	for ( Synset s : sense2 ) { // Stack s ith 0
	    queue.push( s );
	    int[] val = depth.get( s );
	    if ( val == null ) {
		int[] v = new int[3]; v[0]=-1; v[1]=0; v[2]=-1;
		depth.put( s, v );
	    } else {
		val[1] = 0;
		//System.err.println(val[0]+"/"+val[1]+"/"+val[2]);
		//System.err.println( s );
		double newvalue = (double)(2*val[2])/(double)(val[0]+2*val[2]);
		if ( newvalue > bestvalue ) {
		    bestvalue = newvalue;
		}
	    }
	}
	while ( !queue.empty() ) { // Stack non empty
	    Synset s = queue.pop(); // Unstack
	    int i = (depth.get( s ))[1]; // Retrieve depth
	    try {
		for ( PointerTarget h : s.getTargets( PointerType.HYPERNYM ) ) {
		    if ( h instanceof Synset ){
			Synset current = (Synset)h;
			int [] level = depth.get( current );
			if ( level == null ){ // not encounted yet
			    //if ( bestvalue == -1 || i < bestvalue ) { // modest branch and bound
			        int[] v = new int[3]; v[0]=-1; v[1]=i+1; v[2]=-1;
				//int[] v = { -1, i+1, -1 };
				depth.put( current, v );
				queue.push( current );
				//}
			} else if ( level[0] != -1 ){ // This is a least common subsumer
			    level[1] = i+1;
			    //System.err.println(level[0]+"/"+level[1]+"/"+level[2]);
			    //System.err.println( current );
			    double newvalue = (double)(2*level[2])/(double)(level[0]+i+1+2*level[2]);
			    if ( newvalue > bestvalue ){
				bestvalue = newvalue;
			    }
			} else if ( level[1] > i+1 ){
			    level[1] = i+1;
			    queue.push( current );
			}
		    }
		}
	    } catch ( JWNLException ex ) {}
	}

	return bestvalue;
    }


    /**
     * This is an elaborate similarity based on WordNet
     * It is assumed to assess the similarity based on a decomposition and parsing of the strings.
     * 
     */
    public double computeSimilarity( String s1, String s2 ) {
        double sim = 0.0;
        double dists1s2;
        IndexWord index = null;

        dists1s2 = StringDistances.subStringDistance(s1, s2);
        if (dists1s2 < MINIMUM_DISTANCE) return (1 - dists1s2);
        
        if ( s1.equals(s2) || s1.toLowerCase().equals(s2.toLowerCase())) {
            return 1;
        } else {
            if (s1.equals(s1.toUpperCase()) || s1.equals(s1.toLowerCase())) {
                try {
                    // Lookup for first string
                    index = dictionary.lookupIndexWord(POS.NOUN, s1);
                    if (index == null) {
                        index = dictionary.lookupIndexWord(POS.ADJECTIVE, s1);
                    }
                    if (index == null) {
                        index = dictionary.lookupIndexWord(POS.VERB, s1);
		    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }
                // if not found in the dictionary
                if ( index == null ) return (1 - dists1s2);
                else sim = compareComponentNames(s1, s2);
            }
            else sim = compareComponentNames(s1, s2);
        }
        // return sim;
        return Math.max(sim, 1 - dists1s2);
    }
    
    public double compareComponentNames(String s1, String s2) {
        Vector s1Tokens;
        Vector s2Tokens;
        IndexWord indexNoun1, indexNoun2;
        IndexWord indexAdj1, indexAdj2;
        IndexWord indexVerb1, indexVerb2;
        Iterator pIt, gIt;
        Vector vg, vp;
        String token1, token2;
        double simAsAdj, simAsNoun, simAsVerb;
        double maxSim;
        double[][] simMatrix;
        int i, j;
        
        s1Tokens = StringDistances.tokenize(s1);
        s2Tokens = StringDistances.tokenize(s2);

        // tokens storage

        vg = (s1Tokens.size() >= s2Tokens.size()) ? s1Tokens : s2Tokens;
        vp = (s1Tokens.size() >= s2Tokens.size()) ? s2Tokens : s1Tokens;

        // TODO: Don't forget to switch comments.
        // Initializes the tokens hashtables.
        /*this.nouns1        = new Hashtable();
        this.adjectives1   = new Hashtable();
        this.verbs1        = new Hashtable();
        this.nouns2        = new Hashtable();
        this.adjectives2   = new Hashtable();
        this.verbs2        = new Hashtable();
        */
        
        simMatrix = new double[vg.size()][vp.size()];
        
        i = 0;
        gIt = vg.iterator();
        try {
            while (gIt.hasNext()) {
                token1 = (String) gIt.next();
                
                indexNoun1 = dictionary.lookupIndexWord(POS.NOUN, token1);
                indexAdj1 = dictionary.lookupIndexWord(POS.ADJECTIVE, token1);
                indexVerb1 = dictionary.lookupIndexWord(POS.VERB, token1);

                j = 0;
                pIt = vp.iterator();
                while (pIt.hasNext()) {
                    token2 = (String) pIt.next();
                    
                    indexNoun2 = dictionary.lookupIndexWord(POS.NOUN, token2);
                    indexAdj2 = dictionary.lookupIndexWord(POS.ADJECTIVE, token2);
                    indexVerb2 = dictionary.lookupIndexWord(POS.VERB, token2);
                    
                    simAsAdj = this.computeTokenSimilarity(indexAdj1, indexAdj2);
                    maxSim = simAsAdj;
                    simAsNoun = this.computeTokenSimilarity(indexNoun1, indexNoun2);
                    maxSim = (simAsNoun > maxSim) ? simAsNoun : maxSim;
                    simAsVerb = this.computeTokenSimilarity(indexVerb1, indexVerb2);
                    maxSim = (simAsVerb > maxSim) ? simAsVerb : maxSim;
                    
                    simMatrix[i][j] = maxSim;
                    j++;
                }
                i++;
            }
        }
        catch (JWNLException ex) {
            ex.printStackTrace();
        }
        
        return bestMatch(simMatrix);
        
    }

    public double computeTokenSimilarity(IndexWord index1, IndexWord index2) {
        // the max number of common concepts between the two tokens
        double maxCommon = 0;

        // the two lists giving the best match
        PointerTargetNodeList best1 = new PointerTargetNodeList();
        PointerTargetNodeList best2 = new PointerTargetNodeList();

        // the two lists currently compared
        PointerTargetNodeList ptnl1 = new PointerTargetNodeList();
        PointerTargetNodeList ptnl2 = new PointerTargetNodeList();

        if (index1 != null && index2 != null) {
            // The two tokens exist in WordNet, we find the "depth"
            try {
                // Best match between current lists
                int maxBetweenLists = 0;

                // Synsets for each token
                Synset[] Syno1 = index1.getSenses();
                Synset[] Syno2 = index2.getSenses();
                for (int i = 0; i < index1.getSenseCount(); i++) {

                    Synset synset1 = Syno1[i];
                    for (int k = 0; k < index2.getSenseCount(); k++) {

                        Synset synset2 = Syno2[k];

                        List hypernymList1 = PointerUtils.getInstance()
                                .getHypernymTree(synset1).toList();
                        List hypernymList2 = PointerUtils.getInstance()
                                .getHypernymTree(synset2).toList();

                        Iterator list1It = hypernymList1.iterator();
                        // browse lists
                        while (list1It.hasNext()) {
                            ptnl1 = (PointerTargetNodeList) list1It.next();
                            Iterator list2It = hypernymList2.iterator();
                            while (list2It.hasNext()) {
                                ptnl2 = (PointerTargetNodeList) list2It.next();

                                int cc = getCommonConcepts(ptnl1, ptnl2);
                                if (cc > maxBetweenLists) {
                                    maxBetweenLists = cc;
                                    best1 = ptnl1;
                                    best2 = ptnl2;
                                }
                            }
                        }
                        if (maxBetweenLists > maxCommon) {
                            maxCommon = maxBetweenLists;
                        }
                    }
                }
                // System.err.println("common = " + maxCommon);
                // System.err.println("value = "
                // + ((2 * maxCommon) / (best1.size() + best2.size())));
                // if (best1 != null) best1.print();
                // if (best2 != null) best2.print();
                if (best1.isEmpty() && best2.isEmpty())
                    return 0;
                return (2 * maxCommon / (best1.size() + best2.size()));
            }
            catch (JWNLException je) {
                je.printStackTrace();
                System.exit(-1);
            }
        }
        return 0;
    }

    public double findMatchForAdj(IndexWord index1, IndexWord index2) {
        // the max number of common concepts between the two tokens
        double value = 0;

        if (index1 != null && index2 != null) {
            // The two tokens existe in WordNet, we find the "depth"
            try {
                // Synsets for each token
                Synset[] Syno1 = index1.getSenses();
                Synset[] Syno2 = index2.getSenses();
                for (int i = 0; i < index1.getSenseCount(); i++) {

                    Synset synset1 = Syno1[i];
                    for (int k = 0; k < index2.getSenseCount(); k++) {

                        Synset synset2 = Syno2[k];

                        PointerTargetNodeList adjSynonymList = 
                            PointerUtils.getInstance().getSynonyms(synset1);

                        Iterator listIt = adjSynonymList.iterator();
                        // browse lists
                        while (listIt.hasNext()) {
                            PointerTargetNode ptn = (PointerTargetNode) listIt.next();
                            if (ptn.getSynset() == synset2) {
                                value = 1;
                            }
                        }
                    }
                }
                // System.err.println("value = " + value);
                return value;
            }
            catch (JWNLException je) {
                je.printStackTrace();
                System.exit(-1);
            }
        }
        return 0;
    }

    /**
     * @param word
     */
    public void lookUpWord(String word, Hashtable<String,IndexWord> nouns, Hashtable<String,IndexWord> adjectives,
            Hashtable<String,IndexWord> verbs) {
        IndexWord index = null;
        
        try {
            // Lookup for word in adjectives
            index = dictionary.lookupIndexWord(POS.ADJECTIVE, word);
            if (index != null) adjectives.put(word, index);
            // Lookup for word in nouns
            index = dictionary.lookupIndexWord(POS.NOUN, word);
            if (index != null) nouns.put(word, index);
            // Lookup for word in verbs
            index = dictionary.lookupIndexWord(POS.VERB, word);
            if (index != null) verbs.put(word, index);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
    public void display(Synset syn) {
        String str = "";
        for (int s = 0; s < syn.getWordsSize(); s++) {
            str += syn.getWord(s);
        }
        // System.err.println(str);
    }

    public int getCommonConcepts(PointerTargetNodeList list1,
        PointerTargetNodeList list2) {
        int cc = 0;
        int i = 1;
        while (i <= Math.min(list1.size(),
            list2.size()) && ((PointerTargetNode) list1.get(list1.size() - i))
                .getSynset() == ((PointerTargetNode) list2
                .get(list2.size() - i)).getSynset()) {
            cc++;
            i++;
        }
        return cc;

    }
    
    private double bestMatch(double matrix[][]) {
        int nbrLines = matrix.length;
        if (nbrLines == 0) return 0;
        int nbrColumns = matrix[0].length;
        double sim = 0.;
        int minSize = (nbrLines >= nbrColumns) ? nbrColumns : nbrLines;
        if (minSize == 0) return 0;
        for (int k = 0; k < minSize; k++) {
            double max_val = 0;
            int max_i = 0;
            int max_j = 0;
            for (int i = 0; i < nbrLines; i++) {
                for (int j = 0; j < nbrColumns; j++) {
                    if (max_val < matrix[i][j]) {
                        max_val = matrix[i][j];
                        /* mods
                        if (matrix[i][j] > 0.3)
                            max_val = matrix[i][j];
                        else
                            max_val = matrix[i][j] * mask[i][j];
                        end mods */
                        max_val = matrix[i][j];
                        max_i = i;
                        max_j = j;
                    }
                }
            }
            for (int i = 0; i < nbrLines; i++) {
                matrix[i][max_j] = 0;
            }
            for (int j = 0; j < nbrColumns; j++) {
                matrix[max_i][j] = 0;
            }
            sim += max_val;
        }
        return sim / (double)(nbrLines + nbrColumns - minSize);
    }

    /**
     * @param token A token.
     * @param n The number of the ontology (typically 1 or 2).
     * @return the number of occurences of the token in the hashtables
     * nouns, adjectives and verbs.
     */
    public int getNumberOfOccurences(String token, int n) {
        switch (n) {
            case 1:
                return getNumberOfOccurences(token,
                    this.nouns1,
                    this.adjectives1,
                    this.verbs1);
            case 2:
                return getNumberOfOccurences(token,
                    this.nouns2,
                    this.adjectives2,
                    this.verbs2);
            default:
                return 0;
        }
    }
    
    // Find the number of occurences of a words in different categories
    public int getNumberOfOccurences(String token, Hashtable nouns,
        Hashtable adj, Hashtable verbs) {
        int nb = 0;
        if (nouns.containsKey(token)) nb++;
        if (adj.containsKey(token)) nb++;
        if (verbs.containsKey(token)) nb++;
        return nb;
    }

    public void displayMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.println("[" + matrix[i][j]
                    + "]");
            }
        }
    }

    public void fillWithOnes(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = 1;
            }
        }
    }

    /* Getters */
    public double[][] getAdjectivesResults() {
        return adjectivesResults;
    }

    public double[][] getNounsResults() {
        return nounsResults;
    }

    public double[][] getVerbsResults() {
        return verbsResults;
    }

}
