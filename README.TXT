#######################################################################
#              Ontology alignment API and implementation              #
#                      01/01/2013, version 4.4                        #
#                  http://alignapi.gforge.inria.fr                    #
#######################################################################

Copyright (C) 2003-2013 INRIA.
Copyright (C) 2004-2005 Universitô de Montrôal.
Copyright (C) 2005 CNR Pisa.
Copyright (C) 2005 Konstantinos A. Nedas.
Copyright (C) 2006 CERT.
Copyright (C) 2006 Seungkeun Lee.
Copyright (C) 2006-2007 Orange R&D.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation; either version 2.1
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

Read the LICENSE.TXT file for the terms of the LGPL license.

WHAT IS ONTOLOGY ALIGNMENT?
---------------------------

See http://alignapi.gforge.inria.fr for the moment.
See also http://www.ontologymatching.org

USING THE ONTOLOGY ALIGNMENT API IMPLEMENTATION
-----------------------------------------------

Building:
---------

$ ant jar
(or ant compileall; ant jar)

Testing
-------

$ ant test

Running:
--------

For a better introduction, see the tutorial at 
      	     		   http://alignapi.gforge.inria.fr/tutorial
or find it as a script in html/tutorial/script.sh

$ setenv CWD `pwd`

$ java -jar lib/procalign.jar --help

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.4 -o examples/rdf/sample.rdf

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter file:examples/rdf/newsample.rdf

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -a examples/rdf/sample.rdf

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o examples/rdf/bibref.rdf

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=subStringDistance -t .4 -o examples/rdf/bibref2.rdf

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/examples/rdf/bibref2.rdf file://$CWD/examples/rdf/bibref.rdf

$ java -jar lib/procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > examples/rdf/matrix.tex

Using with JWNL (Wordnet)
-------------------------

- Wordnet 3.0 should be installed its directory to be put in $WNDIR
- jwnl.jar commons-logging.jar must be in lib.
(file_properties.xml need not anymore to be in current directory)

$ setenv WNDIR ../WordNet-3.0/dict

$ java -jar lib/procalign.jar -Dwndict=$WNDIR file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o examples/rdf/JWNL.rdf

LAST RELEASE
------------

The last release is available from: http://gforge.inria.fr/frs/?group_id=117

SOURCE REPOSITORY
-----------------

See http://gforge.inria.fr/scm/?group_id=117 for SVN Access.

DOCUMENTATION
-------------

The documentation can be found on http://gforge.inria.fr/docman/?group_id=117

FILES
-----

README.TXT	this file
README.AServ	instructions for setting up an Alignment Server
LICENSE.TXT	the terms under which the software is licensed to you.
build.xml	the Ant build file for compiling and testing
classes/	directory for compiling the sources
distrib/	some files for generating new jarfiles
dtd/		contains the ontoalign DTDs (and schemas)
examples/	examples of use of the API and server
html/		contains some documentation in HTML format
			 (includes relnotes.html)
html/tutorial/  tutorial
javadoc/	javadoc API documentation in HTML (not very useful)
lib/		contains align.jar, procalign.jar, alignsvc.jar
			 and other necessary jarfiles
plugins/neon/	Plug-in for the NeOn toolkit
src/		Java sources of ontoalign
		org.semanticweb.owl.align: the API
		fr.inrialpes.exmo.align.impl: basic implementation
		fr.inrialpes.exmo.align.util: utility functions
		fr.inrialpes.exmo.align.cli: command line interface
		fr.inrialpes.exmo.align.ling: WordNet-based implementation
		fr.inrialpes.exmo.align.service: Alignment Service
                fr.inrialpes.exmo.align.parser: Alignment format parsers
                fr.inrialpes.exmo.align.gen: test generators
		fr.inrialpes.exmo.ontowrap: Abstract ontology layer
test/		Unit tests for testng
tools/		Compile-time tools (testng, etc.)
