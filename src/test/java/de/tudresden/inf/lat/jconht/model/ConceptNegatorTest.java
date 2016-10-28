package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.junit.Assert.assertEquals;

/**
 * This is a test class for <code>ConceptNegator</code>.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ConceptNegatorTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private ConceptNegator conceptNegator;

    private OWLClass owlThing;
    private OWLClass owlNothing;
    private OWLClass owlClass;

    private OWLObjectComplementOf complementOf;

    private OWLObjectIntersectionOf intersectionOf;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        conceptNegator = new ConceptNegator(dataFactory);

        owlThing = dataFactory.getOWLThing();
        owlNothing = dataFactory.getOWLNothing();
        owlClass = dataFactory.getOWLClass("cls:A");

        complementOf = dataFactory.getOWLObjectComplementOf(owlClass);

        intersectionOf = dataFactory.getOWLObjectIntersectionOf(owlClass, owlClass);

    }

    @After
    public void tearDown() throws Exception {
        dataFactory.purge();
    }

    @Test
    public void testOWLThing() throws Exception {

        assertEquals("Negation of OWLThing is OWLNothing",
                owlNothing,
                owlThing.accept(conceptNegator));
    }

    @Test
    public void testOWLNothing() throws Exception {

        assertEquals("Negation of OWLNothing is OWLThing",
                owlThing,
                owlNothing.accept(conceptNegator));
    }

    @Test
    public void testOWLClass() throws Exception {

        assertEquals("Negation of OWLClass",
                owlClass.getObjectComplementOf(),
                owlClass.accept(conceptNegator));
    }

    @Test
    public void testOWLObjectComplementOf() throws Exception {

        assertEquals("Negation of OWLObjectComplementOf",
                owlClass,
                complementOf.accept(conceptNegator));
    }

    @Test
    public void testOWLObjectIntersectionOf() throws Exception {

        assertEquals("Negation of OWLObjectIntersectionOf",
                intersectionOf.getObjectComplementOf(),
                intersectionOf.accept(conceptNegator));
    }
}