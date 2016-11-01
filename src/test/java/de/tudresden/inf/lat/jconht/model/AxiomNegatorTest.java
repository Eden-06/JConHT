package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.junit.Assert.assertEquals;

/**
 * This is a test class for <code>AxiomNegator</code>.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegatorTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private AxiomNegator axiomNegator;

    private OWLClass clsA;
    private OWLClass clsB;
    private OWLClassExpression negClsA;
    private OWLClassExpression negClsB;
    private OWLIndividual indA;
    private OWLIndividual indB;
    private OWLObjectProperty roleR;
    private OWLObjectProperty roleS;

    private OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom;
    private OWLNegativeObjectPropertyAssertionAxiom negativeObjectPropertyAssertionAxiom;
    private OWLEquivalentClassesAxiom equivalentClassesAxiom;
    private OWLSubClassOfAxiom subClassOfAxiom;
    private OWLClassAssertionAxiom classAssertionAxiom;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        axiomNegator = new AxiomNegator(dataFactory);

        clsA = dataFactory.getOWLClass("cls:A");
        clsB = dataFactory.getOWLClass("cls:B");
        negClsA = clsA.getObjectComplementOf();
        negClsB = clsB.getObjectComplementOf();
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indB = dataFactory.getOWLNamedIndividual("ind:b");
        roleR = dataFactory.getOWLObjectProperty("rol:r");
        roleS = dataFactory.getOWLObjectProperty("rol:s");

        objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(roleR, indA, indB);
        negativeObjectPropertyAssertionAxiom = dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(roleR, indA, indB);
        equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(clsA, clsB);
        subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(clsA, clsB);
        classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(clsA, indA);
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
    }

    @Test
    public void testOWLSubclassOf() throws Exception {

        // TODO
    }

    @Test
    public void testOWLNegativeObjectPropertyAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                objectPropertyAssertionAxiom,
                negativeObjectPropertyAssertionAxiom.accept(axiomNegator));

    }

    @Test
    public void testOWLObjectPropertyAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                negativeObjectPropertyAssertionAxiom,
                objectPropertyAssertionAxiom.accept(axiomNegator));

    }

    @Test
    public void testOWLEquivalentClassesAxiom() throws Exception {

        assertEquals("Negation of OWLEquivalentClassesAxiom",
                dataFactory.getOWLObjectUnionOf(
                        dataFactory.getOWLObjectIntersectionOf(clsA, negClsB),
                        dataFactory.getOWLObjectIntersectionOf(negClsA, clsB)),
                ((OWLClassAssertionAxiom) equivalentClassesAxiom.accept(axiomNegator)).getClassExpression());

    }

    @Test
    public void testOWLSubclassOfAxiom() throws Exception {

        assertEquals("Negation of OWLSubclassOfAxiom",
                dataFactory.getOWLObjectIntersectionOf(clsA, negClsB),
                ((OWLClassAssertionAxiom) subClassOfAxiom.accept(axiomNegator)).getClassExpression());
    }

    @Test
    public void testOWLClassAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLClassAssertionAxiom",
                dataFactory.getOWLClassAssertionAxiom(negClsA, indA),
                classAssertionAxiom.accept(axiomNegator));
    }
}