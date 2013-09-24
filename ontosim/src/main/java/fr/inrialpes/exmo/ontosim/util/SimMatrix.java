package fr.inrialpes.exmo.ontosim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import fr.inrialpes.exmo.ontosim.entity.EntityLexicalMeasure;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.entity.model.EntityImpl;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SimMatrix {

    /**
     * @param args
     * @throws OntowrapException 
     * @throws java.io.FileNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws OntowrapException, FileNotFoundException {
	
	File f1 = new File(args[0]);
	File f2 = new File(args[1]);
	
	PrintStream out = System.out;// new PrintStream(new File("ev43-eurlex.txt"));
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.skoslite.SKOSLiteOntologyFactory");
	
	OntologyFactory ontoFactory = OntologyFactory.getFactory();
	
	LoadedOntology<?> o1 = ontoFactory.loadOntology(f1.toURI());
	LoadedOntology<?> o2 = ontoFactory.loadOntology(f2.toURI());
	
	EntityLexicalMeasure<?> sim = new EntityLexicalMeasure("en");
	
	Set<?> entites2Temp = o2.getClasses();
	ArrayList<Entity<?>> entities2 = new ArrayList<Entity<?>>(entites2Temp.size());
	
	for (Object o : entites2Temp) {
	    entities2.add(new EntityImpl(o2,o));
	}

	for (Object o : o1.getClasses()) {
	    Entity e1 = new EntityImpl(o1,o);
	    for (Entity e2 : entities2) {
		out.println(e1.getURI()+";"+e2.getURI()+";"+sim.getSim(e1, e2));
	    }
	}
	
	out.close();
	
    }

}
