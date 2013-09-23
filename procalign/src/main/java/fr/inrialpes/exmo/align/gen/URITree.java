/*
 * $Id: URITree.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) 2011-2012, INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

/* This program represents the class hierarchy.
   It retains only the URI of the classes .
*/

package fr.inrialpes.exmo.align.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.inrialpes.exmo.align.gen.alt.BasicAlterator;

public class URITree {
    private String URI;                 //the URI of the node
    private ArrayList<URITree> children;//the list of children
    private URITree parent;		//the parent of the node
    int depth;				//the depth of the node
    int maxDepth;			//the max depth of the node
	
    public URITree( String URI ) {
        this.URI = URI;
        children = new ArrayList<URITree>();
        parent = null;
	depth = 0;
	maxDepth = 0;
    }

    //get the URI of the node
    public String getURI () {
        return URI;
    }
	
    //set the URI of the node
    public void setURI( String URI ) {
        this.URI = URI;
    }
	
    //set the depth of the node
    public void setDepth( int depth ) {
        this.depth = depth;
    }
	
    //get the depth of the node
    public int getDepth() {
        return depth;
    }
	
    //return the max depth
    public int getMaxDepth() {
        return maxDepth;
    }
	
    //set the parent of the node
    public void setParent( URITree parent ) {
        this.parent = parent;
    }
	
    //get the parent of the node
    public URITree getParent () {
        return parent;
   }
	
    //returns a child from a specific position
    public URITree getChildAt ( int index ) {
        return children.get( index );
    }
	
    //return the list of children nodes
    public ArrayList<URITree> getChildrenList() {
        return children;
    }
	
    //returns the size of the children
    public int getChildrenSize() {
        return children.size();
    }

    //add the node with the childURI to the parent with the URI parentURI
    public void add( String childURI, String parentURI ) {
        //adds the node to the class hierarchy -> a class might have more than one 
        //superclass we have to add it to each superclass in part, not only to one
        _addChildToNode( this, parentURI, childURI );
    }

    //returns the URITree with the given URI
    public void _addChildToNode( URITree root, String parentURI, String childURI) {
        if ( getURI().equals( parentURI ) ) {				//if the root has the URI as the URI searched
            addChild( root, this, childURI );                                    //then add the child
        } else {
	    for( URITree node : getChildrenList() ) {                              //we start to search recursively
		node._addChildToNode( root, parentURI, childURI );
	    }
	}
    }

    //add a child
    // JE: could have a better interface (and implementation, this
    public void addChild( URITree root, URITree node, String URI ) {
	// If already child, forget it
	for ( URITree n : node.getChildrenList() ) {
            if ( n.getURI().equals( URI ) ) return;
        }
	// If already existing, suppress it 
	// JE: Why at first level only?
	URITree toRemove = null;
	for ( URITree n : root.getChildrenList() ) {
	    if ( n.getURI().equals( URI ) ) {
		toRemove = n;
		break;
	    }
        }
	root.getChildrenList().remove( toRemove );
	// Now, go and create it
        node.addChildToNode( URI );
    }
	
    //add child to a specific node
    public void addChildToNode( String URI ) {
        URITree child = new URITree( URI );                                     //creates a new node
        child.setDepth( getDepth()+1 );                                  //set the depth of the node
        if ( maxDepth < getDepth()+1 )                               //keeps track of the max depth of the hierarchy
            maxDepth = getDepth()+1;
        child.setParent( this );                                                //sets the parent of the node
        getChildrenList().add( child );                                    //adds the node to the parent children list
    }

    //renames the class from the tree after we have renamed the classes
    public void renameTree( Properties alignment ) {
	rename( alignment, (String)alignment.get( "##" ) );
    }

    public void rename( Properties alignment, String ns ) {
	String key = BasicAlterator.getLocalName( getURI() );
	String val = (String)alignment.get( key );
	if ( val != null && !val.equals( key ) ) setURI( ns+val );
	for ( URITree child : getChildrenList() ) {
	    child.rename( alignment, ns );
	}
    }

    //returns the URITree with the given URI
    public URITree searchURITree( String URI ) {
        if ( getURI().equals( URI ) ) return this;                         //if the root has the URI as the URI searched
	for ( URITree node : getChildrenList() ) {                  //we start to search recursively
            URITree ans = node.searchURITree( URI );
            if ( ans != null ) return ans;
        }
        return null;
    }

    //get all the node from a specific level
    public List<URITree> getNodesFromLevel( int level ) {
        List<URITree> nodes = new ArrayList<URITree>();                         //to store the nodes from a specific level
	getNodes( nodes, level );                                               //recursively print all the children URITrees
        return nodes;                                                           //return the list of nodes
    }

    public void getNodes ( List<URITree> nodes, int level) {
        if ( getDepth() == level ) {                                       //if it's on the level that we want, we add it to the hierarchy
	    nodes.add( this );
	} else {
	    for( URITree n : getChildrenList() ) {
		n.getNodes( nodes, level );                      //recursively print all the children URITrees
	    }
	}
    }
	
    //change the depth if the nodes lower the level to node.getDepth()-1
    public void changeDepth( int level ) {
        maxDepth--;
	change( level );
    }

    public void change( int level ) {
        if ( getDepth() > level ) 	{                                       //if it's on the level that we want, we add it to the hierarchy
            setDepth( getDepth()-1 );
        }
	for ( URITree n : getChildrenList() ) {
            n.change( level );
        }
    }
	
    //print the tree
    public void printURITree() {
	print( 0 );
    }

    public void print( int depth ) {
        indent( getDepth() );
        System.err.println( "[" + getURI() + "]" + "->" + getDepth() );
	for( URITree n : getChildrenList() ) {
            n.print( depth+1 );
	}
    }

    protected void indent( int depth ) {
        for ( int i = 0;  i < depth; i++ ) System.out.print( "  " );
    }
		
}


