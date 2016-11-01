package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


/**
 * This is indA test class for <code>ContextOntology</code>.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextOntologyTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;

    private OWLOntology rootOntology;
    private ContextOntology contextOntology;

    private OWLClass clsC;
    private OWLClass clsA;
    private OWLClass clsA1;
    private OWLClass clsA2;
    private OWLClass clsA3;
    private OWLClass clsB;
    private OWLIndividual indA;
    private OWLIndividual indB;
    private OWLIndividual indC;

    private OWLAnnotation annotationA1;
    private Set<OWLAnnotation> setAnnotationA1;
    private OWLAnnotation annotationA2;
    private Set<OWLAnnotation> setAnnotationA2;
    private OWLAnnotation annotationA3;
    private Set<OWLAnnotation> setAnnotationA3;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        clsC = dataFactory.getOWLClass("cls:C");
        clsA = dataFactory.getOWLClass("cls:A");
        clsA1 = dataFactory.getOWLClass("cls:A1");
        clsA2 = dataFactory.getOWLClass("cls:A2");
        clsA3 = dataFactory.getOWLClass("cls:A3");
        clsB = dataFactory.getOWLClass("cls:B");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indB = dataFactory.getOWLNamedIndividual("ind:b");
        indC = dataFactory.getOWLNamedIndividual("ind:c");

        annotationA1 = dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), clsA1.getIRI());
        setAnnotationA1 = new HashSet<>();
        setAnnotationA1.add(annotationA1);
        annotationA2 = dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), clsA2.getIRI());
        setAnnotationA2 = new HashSet<>();
        setAnnotationA2.add(annotationA2);
        annotationA3 = dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), clsA3.getIRI());
        setAnnotationA3 = new HashSet<>();
        setAnnotationA3.add(annotationA3);

        rootOntology = manager.createOntology(Arrays.asList(
                dataFactory.getOWLSubClassOfAxiom(clsC, dataFactory.getOWLObjectIntersectionOf(clsA1, clsA2)),
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing(), setAnnotationA1),
                dataFactory.getOWLClassAssertionAxiom(clsC, indC),
                dataFactory.getOWLClassAssertionAxiom(clsA2, indC),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA, setAnnotationA2),
                dataFactory.getOWLClassAssertionAxiom(clsB, indA, setAnnotationA3)));

        contextOntology = new ContextOntology(rootOntology);
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    @Test
    public void testMetaOntology() throws Exception {

        OWLOntology ontology = manager.createOntology(Arrays.asList(
                dataFactory.getOWLSubClassOfAxiom(clsC, dataFactory.getOWLObjectIntersectionOf(clsA1, clsA2)),
                dataFactory.getOWLClassAssertionAxiom(clsC, indC),
                dataFactory.getOWLClassAssertionAxiom(clsA2, indC)));

        assertEquals("Test for getting the meta ontology:",
                ontology.axioms().collect(Collectors.toSet()),
                contextOntology.getMetaOntology().axioms().collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntology() throws Exception {

        assertEquals("Test for getting the object ontology:",
                manager.createOntology(Arrays.asList(
                        dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                        dataFactory.getOWLClassAssertionAxiom(clsA, indA)))
                        .axioms().collect(Collectors.toSet()),
                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(clsA1, clsA2))).axioms().collect(Collectors.toSet()));


        assertEquals("Test for getting the object ontology:",
                manager.createOntology(Arrays.asList(
                        dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                        dataFactory.getOWLClassAssertionAxiom(clsB, indA)))
                        .axioms().collect(Collectors.toSet()),
                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(clsA1, clsA3))).axioms().collect(Collectors.toSet()));
    }

}