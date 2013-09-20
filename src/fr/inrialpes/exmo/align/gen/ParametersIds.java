/*
 * $Id: ParametersIds.java 1785 2012-10-16 11:03:44Z euzenat $
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

/* This program represents the list of all the modifications
   that can be applied to the test generator
 */

package fr.inrialpes.exmo.align.gen;

public class ParametersIds {
    public static String ADD_CLASSES        = "addClasses";                     //adds random classes
    public static String REMOVE_CLASSES     = "removeClasses";                  //removes random classes
    public static String REMOVE_PROPERTIES  = "removeProperties";               //removes random properties
    public static String REMOVE_COMMENTS    = "removeComments";                 //removes random comments
    public static String LEVEL_FLATTENED    = "levelFlattened";                 //flattens a level
    public static String ADD_PROPERTIES     = "addProperties";                  //adds random propeties
    public static String REMOVE_CLASSESLEVEL= "removeClassLevel";                  //remove classes from level
    public static String ADD_CLASSESLEVEL   = "addClassLevel";                //add c classes beginning from level l -> the value of this parameters should be:
                                                                                //beginning_level.number_of_classes_to_add
    public static String RENAME_PROPERTIES  = "renameProperties";               //renames properties
    public static String RENAME_CLASSES     = "renameClasses";                  //renames classes
    public static String RENAME_RESOURCES   = "renameResources";                //renames properties + classes
    public static String REMOVE_RESTRICTIONS= "removeRestrictions";             //removes restrictions
    public static String REMOVE_INDIVIDUALS = "removeIndividuals";              //removes individuals
    public static String NO_HIERARCHY       = "noHierarchy";                    //no hierarchy
    public static String RENAME_METHOD      = "renameMethod";                    //no hierarchy
    public static String RENAME_SYNONYM     = "synonym";                    //no hierarchy
    public static String RENAME_TRANSLATE   = "translate";                    //no hierarchy
    public static String RENAME_RANDOM      = "random";                    //no hierarchy
}

