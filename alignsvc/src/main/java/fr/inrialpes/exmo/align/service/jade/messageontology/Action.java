package fr.inrialpes.exmo.align.service.jade.messageontology;

import jade.content.Predicate;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
* Protege name: Action
* @author ontology bean generator 
* @version 2007/03/19, 17:12:29
*/
public class Action implements Predicate {

    private static final long serialVersionUID = 330;
   /**
* Protege name: hasParameter
   */
   private List<Parameter> hasParameter = new ArrayList<Parameter>();

   public void addHasParameter(Parameter elem) { 
     List<Parameter> oldList = this.hasParameter;
     hasParameter.add(elem);
   }
   public boolean removeHasParameter(Parameter elem) {
     List<Parameter> oldList = this.hasParameter;
     boolean result = hasParameter.remove(elem);
     return result;
   }
   public void clearAllHasParameter() {
     List<Parameter> oldList = this.hasParameter;
     hasParameter.clear();
   }
   public Iterator<Parameter> getAllHasParameter() {return hasParameter.iterator(); }
   public List<Parameter> getHasParameter() {return hasParameter; }
   public void setHasParameter(List<Parameter> l) {hasParameter = l; }
   
   /**
   * Protege name: Result
      */
      private String result;
      public void setResult(String result) { 
       this.result=result;
      }
      public String getResult() {
        return this.result;
      }
   

}
