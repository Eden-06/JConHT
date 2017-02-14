package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ConceptToDualTest {

    private OWLDataFactory dataFactory;
    private OWLClass clsA;
    private OWLClass clsB;
    private OWLClass clsC;
    private Set<OWLClass> setA;

    @Before
    public void setUp() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        clsA = dataFactory.getOWLClass("cls:A");
        clsB = dataFactory.getOWLClass("cls:B");
        clsC = dataFactory.getOWLClass("cls:C");
        setA = Stream.of(clsA).collect(Collectors.toSet());

    }

    @After
    public void tearDown() throws Exception {


    }


    @Test
    public void testConceptToDual() throws Exception {
        System.out.println("Executing testConceptToDual:");

        assertEquals("ConceptToDual",
                clsA.accept(new ConceptToDual(dataFactory, setA)),
                dataFactory.getOWLObjectComplementOf(
                        dataFactory.getOWLClass("cls:DUAL.A")));

    }


    @Test
    public void testConceptUnchanged() throws Exception {
        System.out.println("Executing testConceptToDual:");

        assertEquals("ConceptToDual",
                clsB.accept(new ConceptToDual(dataFactory, setA)),
                clsB);

    }

    @Test
    public void testComplexConcept() throws Exception {
        System.out.println("Executing testComplexConcept:");

        OWLClassExpression concept = dataFactory.getOWLObjectIntersectionOf(
                dataFactory.getOWLClass("cls:A"),
                dataFactory.getOWLClass("cls:B"),
                dataFactory.getOWLClass("cls:C"));

        System.out.println(concept.accept(new ConceptToDual(dataFactory, setA)));

    }

    // TODO MinCardinality with and without ClassExpression

    // TODO Hier fehlen noch viele Tests.
}