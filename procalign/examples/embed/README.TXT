#!/bin/sh
# Example of embeding alignment in a simple file
# $Id: README.TXT 987 2009-05-27 13:48:33Z euzenat $

javac -cp .:../../lib/procalign.jar MyAlign.java
java -cp .:../../lib/procalign.jar MyAlign
