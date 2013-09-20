/*
 * $Id: Apply.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) INRIA, 2010, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.edoal;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * Apply is the application of a function to arguments.
 * </p>
 * <p>
 * In the specification a {@code op} is a local transformaion
 * function/service. The {@code args} specifies the parameters needed for the
 * function to compute the transformaion.
 * </p>
 *
 * @version $Id: Apply.java 1710 2012-03-23 19:53:25Z euzenat $
 */

public class Apply implements ValueExpression {

    /** Holds the operation to apply */
    private URI operation;

    private List<ValueExpression> arguments;

    /**
     * Constructs an object with the given value.
     * 
     * @param op
     *            the URI of the operation to apply.
     * @param args
     *            its list of argumenst
     * @throws NullPointerException
     *             if the value is {@code null}
     */
    public Apply( final URI op, final List<ValueExpression> args ) {
	if ( op == null) {
	    throw new NullPointerException("The operation must not be null");
	}
	operation = op;
	arguments = args;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }
    public TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public URI getOperation() {
	return operation;
    }

    public List<ValueExpression> getArguments() {
	return arguments;
    }

    public int hashCode() {
	return 5*operation.hashCode() + 13*arguments.hashCode();
    }

    public boolean equals( final Object o ) {
	if ( o == this ) return true;
	if ( !(o instanceof Apply) ) return false;
	Apply a = (Apply)o;
	return ( operation.equals(a.getOperation()) && 
		 arguments.equals(a.getArguments()) );
    }
    /*
    public Object clone() {
	return super.clone();
    }
    */

    /**
     * <p>
     * Returns a short description about this object. <b>The format of the
     * returned string is undocumentd and subject to change.</b>
     * </p>
     * <p>
     * An example return string could be: {@code 15}
     * </p>
     */
    //public String toString() {
    //	return value;
    //}
}
