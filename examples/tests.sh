#!/bin/csh
# This is a serie of tests made for the presentation of the API.
# All these tests can be automatically run
# This file is obsolete and is superseeded by our tests (Version 3.6)

echo "This file is obsolete and is superseeded by our tests"

# Context
echo "Cleaning up."
setenv CWD `pwd`
setenv WNDIR ../../WordNet-3.0/dict

# Clean up
/bin/rm -rf aligns/

mkdir aligns
/bin/cp ../dtd/align.dtd aligns/align.dtd
/bin/cp ../file_properties.xml .

# Display parameters
echo "Basic..."
java -jar ../lib/procalign.jar

# Simple basic example
java -jar ../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -o aligns/sample.owl

java -jar ../lib/procalign.jar file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment -t .6 -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

# Test a number of methods
echo "Aligning..."
java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.NameEqAlignment -o aligns/NameEq.owl

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -o aligns/EditDistName.owl

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment -o aligns/SubsDistName.owl

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment -o aligns/StrucSubsDist.owl

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment -o aligns/StrucSubsDist4.owl -t .4

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment -o aligns/StrucSubsDist7.owl -t .7

#java -jar ../lib/procalign.jar -Dwndict=$WNDIR file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o aligns/JWNL.owl

# Evaluate their performances
echo "Comparing..."

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/NameEq.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/EditDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/SubsDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist4.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist7.owl

# Other evaluations
echo "Comparing again..."

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/NameEq.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/EditDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/SubsDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist4.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.SymMeanEvaluator file://$CWD/rdf/bibref.owl file://$CWD/aligns/StrucSubsDist7.owl
# Pipelining
echo "Pipelining..."

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.PropSubsDistAlignment -o aligns/PropSubsDist.owl 

java -jar ../lib/procalign.jar file://$CWD/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.ClassStructAlignment -a aligns/PropSubsDist.owl -o aligns/Piped.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/aligns/StrucSubsDist.owl file://$CWD/aligns/Piped.owl

# Rendering
echo "Rendering..."

java -jar ../lib/procalign.jar file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor -t 0.4

java -jar ../lib/procalign.jar file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor -t 0.4

java -jar ../lib/procalign.jar file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor -t 0.4

java -jar ../lib/procalign.jar file://$CWD/rdf/onto1.owl file://$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.COWLMappingRendererVisitor -t 0.4

# Output to html
echo "HTML output..."

# This should be best done with JAVA XSLT if it exists than xsltproc
echo '<html><head></head><body>' > aligns/index.html
xsltproc ../html/form-align.xsl rdf/bibref.owl > aligns/bibref.html
echo '<a href="bibref.html">Reference</a>' >> aligns/index.html
foreach i (`ls aligns/*.owl | sed "s:aligns/::" | sed "s:\.owl::"`)
	echo '<a href="'$i'.html">'$i'</a>' >> aligns/index.html
	xsltproc ../html/form-align.xsl aligns/$i.owl > aligns/$i.html
end
echo '</body></html>' >> aligns/index.html
