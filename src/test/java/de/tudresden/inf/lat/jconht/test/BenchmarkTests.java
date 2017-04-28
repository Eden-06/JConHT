package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.AxiomToDual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class BenchmarkTests {

    private OWLDataFactory dataFactory;

    @Before
    public void setUp() throws Exception {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

    }

    @After
    public void tearDown() throws Exception {


    }


    @Test
    public void testSubclassAxiom() throws Exception {
        System.out.println("Executing testSubclassAxiom:");

        OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(
                dataFactory.getOWLClass("cls:C"),
                dataFactory.getOWLClass("cls:D"));


        System.out.println(axiom);


        System.out.println(axiom.accept(new AxiomToDual(dataFactory,
                Stream.of(dataFactory.getOWLClass("cls:C")).collect(Collectors.toSet()))));

    }

    // TODO noch viele Tests!!!!!
}