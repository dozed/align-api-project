Examples for AlignmentServer web service communication.

# $Id: README.TXT 1593 2011-05-11 15:07:01Z euzenat $

This directory contains an example of connecting to the
Alignment Server through web services.

-------------------------------------
TODO

- beware, wsdl does not make provision for error messages...
- type wsdl??

-------------------------------------
CLIENT INVOKATION

java AlignmentClient [genoptions] keyword [options]
genoptions:
	-d [num]	 debug level
	-r 		 rest interface
	-S 		 servername
	-h		 help

java AlignmentClient wsdl			-> XML
java AlignmentClient find URI URI		-> AURI
java AlignmentClient match [method] URI URI	-> AURI 
     -> CleanUp reception (err messages)
java AlignmentClient trim [method] AURI float	-> AURI
java AlignmentClient invert AURI		-> AURI
java AlignmentClient load URI | <stdin>		-> AURI
     => finir la partie stdin (côté WSAServProfile)
     => it also embeds a <?xml ...>
java AlignmentClient store AURI 		-> AURI
java AlignmentClient retrieve AURI method	-> XML
     => embed a <?xml ...>
     => not always XML
//-java AlignmentClient metadata AURI key	-> XML

java AlignmentClient list alignments		-> XML
java AlignmentClient list methods		-> XML
java AlignmentClient list renderers		-> XML
java AlignmentClient list services		-> XML

----------------------------------
USE WITH THE ALIGNMENT SERVER

$ javac -cp .:../../lib/getopt/getopt.jar:../../lib/procalign.jar AlignmentClient.java

$ java -cp .:../../lib/getopt/getopt.jar AlignmentClient match http://alignapi.gforge.inria.fr/tutorial/myOnto.owl http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl
<SOAP-ENV:Envelope
   xmlns='http://exmo.inrialpes.fr/align/service'
   xml:base='http://exmo.inrialpes.fr/align/service'
   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'
   xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance'
   xmlns:xsd='http://www.w3.org/1999/XMLSchema'>
  <SOAP-ENV:Body>
    <matchResponse>
      <id>12</id>
      <in-reply-to>fr.inrialpes.exmo.align.service.Message@1fa5d1a</in-reply-to>
      <alid>http://aserv.inrialpes.fr:8089/alid/1219842718179/172</alid>
    </matchResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>

----------------------------------
USE WITH A PARTICULAR ALIGNMENT SERVICE

$ java -cp .:../../lib/getopt/getopt.jar AlignmentClient -S http://aserv.inrialpes.fr align http://alignapi.gforge.inria.fr/tutorial/myOnto.owl http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl

----------------------------------
FOR TESTING

$ java -cp .:../../lib/getopt/getopt.jar AlignmentClient list methods  -S http://aserv.inrialpes.fr -r -d 4

or

$ curl -H "Content-Type: text/xml" 'http://aserv.inrialpes.fr/rest/listalignments?'
