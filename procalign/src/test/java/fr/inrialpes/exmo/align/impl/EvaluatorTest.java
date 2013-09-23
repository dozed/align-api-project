package fr.inrialpes.exmo.align.impl;

import fr.inrialpes.exmo.align.impl.eval.SemPRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.testng.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: stefan
 * Date: 20.09.13
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */
public class EvaluatorTest {

    @Test
    public void testEvaluation() throws AlignmentException, IOException {
        AlignmentParser aparser = new AlignmentParser(0);

        Alignment a1 = aparser.parse("file:/home/stefan/Code/diplom-code/ldif-geo/data/align-reegle-ref.rdf");
        Alignment a2 = aparser.parse("file:/home/stefan/Code/diplom-code/ldif-geo/matching-code.rdf");

        SemPRecEvaluator e1 = new SemPRecEvaluator(a1, a2);
        e1.eval(System.getProperties());

        PrintWriter writer = new PrintWriter(System.out);
        e1.write(writer);
        writer.flush();
        writer.close();
    }

}
