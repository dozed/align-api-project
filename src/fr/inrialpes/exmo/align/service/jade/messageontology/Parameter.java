package fr.inrialpes.exmo.align.service.jade.messageontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Paramameter
* @author ontology bean generator
* @version 2007/03/19, 17:12:29
*/
public class Parameter implements Concept {

    private static final long serialVersionUID = 330;
   /**
* Protege name: value
   */
   private String value;
   public void setValue(String value) { 
    this.value=value;
   }
   public String getValue() {
     return this.value;
   }

   /**
* Protege name: name
   */
   private String name;
   public void setName(String value) { 
    this.name=value;
   }
   public String getName() {
     return this.name;
   }

}
