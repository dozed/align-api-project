#!/bin/csh

#####################
# Preparation

#mkdir alignapi
#cd alignapi
#unzip align*.zip
#java -jar lib/procalign.jar --help
#cd html/tutorial
cd ..
setenv CWD `pwd`
cd tutorial2
setenv WNDIR ../../../../WordNet-2.0/dict

/bin/rm results/*

#####################
# Embedding

javac -classpath ../../../lib/align.jar:../../../lib/procalign.jar -d results Skeleton.java

java -cp ../../../lib/Procalign.jar:results Skeleton file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl

javac -classpath ../../../lib/align.jar:../../../lib/procalign.jar -d results MyApp.java

java -cp ../../../lib/Procalign.jar:results MyApp file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl > results/MyApp.owl
