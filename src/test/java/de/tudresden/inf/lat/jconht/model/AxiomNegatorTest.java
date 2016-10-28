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
    private OWLDataFactory df;
    private AxiomNegator axiomNegator;
    private ConceptNegator conceptNegator;

    private OWLClass conceptA;
    private OWLClass conceptB;
    private OWLClassExpression notConceptA;
    private OWLClassExpression notConceptB;
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
        df = manager.getOWLDataFactory();
        axiomNegator = new AxiomNegator(df);
        conceptNegator = new ConceptNegator(df);

        conceptA = df.getOWLClass("cls:A");
        conceptB = df.getOWLClass("cls:B");
        notConceptA = conceptA.accept(conceptNegator);
        notConceptB = conceptB.accept(conceptNegator);
        indA = df.getOWLNamedIndividual("ind:a");
        indB = df.getOWLNamedIndividual("ind:b");
        roleR = df.getOWLObjectProperty("rol:r");
        roleS = df.getOWLObjectProperty("rol:s");

        objectPropertyAssertionAxiom = df.getOWLObjectPropertyAssertionAxiom(roleR, indA, indB);
        negativeObjectPropertyAssertionAxiom = df.getOWLNegativeObjectPropertyAssertionAxiom(roleR, indA, indB);
        equivalentClassesAxiom = df.getOWLEquivalentClassesAxiom(conceptA, conceptB);
        subClassOfAxiom = df.getOWLSubClassOfAxiom(conceptA, conceptB);
        classAssertionAxiom = df.getOWLClassAssertionAxiom(conceptA,indA);
    }

    @After
    public void tearDown() throws Exception {
        df.purge();
    }

    @Test
    public void testOWLSubclassOf() throws Exception {


    }

    @Test
    public void testOWLNegativeObjectPropertyAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                df.getOWLObjectPropertyAssertionAxiom(roleR, indA, indB),
                negativeObjectPropertyAssertionAxiom.accept(axiomNegator));

    }

    @Test
    public void testOWLObjectPropertyAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                df.getOWLNegativeObjectPropertyAssertionAxiom(roleR, indA, indB),
                objectPropertyAssertionAxiom.accept(axiomNegator));

    }

    @Test
    public void testOWLEquivalentClassesAxiom() throws Exception {

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                df.getOWLObjectUnionOf(
                        df.getOWLObjectIntersectionOf(conceptA, notConceptB),
                        df.getOWLObjectIntersectionOf(notConceptA, conceptB)),
                ((OWLClassAssertionAxiom) equivalentClassesAxiom.accept(axiomNegator)).getClassExpression());

    }

    @Test
    public void testOWLSubclassOfAxiom() throws Exception {

        assertEquals("Negation of OWLSubclassOfAxiom",
                df.getOWLObjectIntersectionOf(conceptA, notConceptB),
                ((OWLClassAssertionAxiom) subClassOfAxiom.accept(axiomNegator)).getClassExpression()
        );
    }

    @Test
    public void testOWLClassAssertionAxiom() throws Exception {

        assertEquals("Negation of OWLClassAssertionAxiom",
                df.getOWLClassAssertionAxiom(notConceptA,indA),
                classAssertionAxiom.accept(axiomNegator)
        );
    }
}