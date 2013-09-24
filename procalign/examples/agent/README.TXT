
This example is a JADE based GUI client for the Alignment Server

Compiling
---------

$ javac -classpath ../../lib/jade/jade.jar *.java 

$ jar cvf JADEFIPAASRequesterAgent.jar *.class

Launching
---------

Beware, the server must be launched before:
$ java -jar ../../lib/alignsvc.jar -H -A &

$ java -cp .:../../lib/jade/jade.jar:JADEFIPAASRequesterAgent.jar jade.Boot -container -host localhost -port 8888 Control:JADEFIPAASRequesterAgent

