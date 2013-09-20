#!/bin/sh

echo 1st
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter wine.xml > wine2.xml
echo 2nd
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter wine2.xml > wine3.xml
echo diff
diff wine2.xml wine3.xml

