#######################################################################
#                     Alignment API micro edition                     #
#                      13/05/2011, version 4.2                        #
#######################################################################

Copyright (C) 2011 INRIA.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation; either version 2.1
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

Read the LICENSE.TXT file for the terms of the LGPL license.

WHAT IS THIS
------------

This folder contains necessary tool for generating a stripped down
version of the Alignment API so that it can be embedded in a small
platform (the target being the Android platform).

WHAT IS INCLUDED
----------------

microalign.jar = Alignment API + implementation + parser
	       - evaluation - matchers - server - util

Only the libraries related to Jena are used:
Jena (+ icu4j and iri), log4j, xerces (+resolver) and xsdlib (+relaxngDatatype).

OTHER PROFILES
--------------

Of course, you can define other profiles by modifying the build.xml file.

We are considering, introducing a pair of other profiles:
- picoalign: an even smaller package without jena and edoal
- aservlite: a minimal Alignment server version

HOW TO GENERATE
---------------

$ ant compileall              ==> compile the strict necessary
$ ant jar                     ==> generate the microalign.jar library
$ ant zip                     ==> gerenate the malign.zip file with necessary jars
$ ant testindep               ==> recompile the sources in isolation for testing independence
$ ant clean                   ==> restore the initial state of this directory

HOW TO USE
----------

Move the content of the zip file in the lib(s) directory of your Android
project.

The microalign.jar may be used alone if a version of Jena is already embedded.
