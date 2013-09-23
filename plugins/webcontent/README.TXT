#######################################################################
#                    WebContent Alignment plug-in                   #
#######################################################################

(C) INRIA Rhône-Alpes, 2007-2008
Licensed under the LGPL 2.0 or above

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation; either version 2.1
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

Read the LICENSE.TXT file for the terms of the LGPL license.

To build the plug-in:

$ ant war

This produces a webcontent.war to put in the "webapps" directory
of a Tomcat server.

To test the plug-in, a web service client (e.g. JWebServiceClient from http://weblab-project.org/)
can be used to send the following SOAP message :  


<ns2:processArgs xmlns:ns2="http://weblab-project.org/services/ontologyalignment/types">
<usageContext uri="http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl"/>
<onto1 uri="http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl"/>
<onto2 uri="http://alignapi.gforge.inria.fr/tutorial/myOnto.owl"/>
</ns2:processArgs>

The client should expect a response message from the plug-in like this one :

<ns2:processReturn xmlns:ns2="http://weblab-project.org/services/ontologyalignment/types" xmlns:ns3="http://weblab-project.org/services/exception">
<annotation uri="http://aserv.inrialpes.fr:8089/alid/1216393091899/650"/>
</ns2:processReturn>

