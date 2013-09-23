/*
 * $Id: NullStream.java 563 2008-01-15 15:09:04Z euzenat $
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.util;

import java.io.OutputStream;

/*
 * My most vacuous class ever.
 * The goal of this class is to simulate /dev/null by doing nothing on output.
 * It is used as:
 * System.setErr( new PrintStream( new NullStream() ) );
 * It just does nothing!
 */
public class NullStream extends OutputStream {

    public void close() {};
    public void flush() {};
    public void write(byte[] b) {};
    public void write(byte[] b, int off, int len) {};
    public void write(int i) {};
}


