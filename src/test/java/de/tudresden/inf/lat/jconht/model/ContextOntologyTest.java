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
 * Created by boehme on 28/10/16.
 */
public class ContextOntologyTest {

    private OWLOntologyManager manager;
    private OWLDataFactory df;
    private OWLOntology rootOntology;
    private ContextOntology contextOntology;
    private OWLClass C;
    private OWLClass A;
    private OWLClass A1;
    private OWLClass A2;
    private OWLClass A3;
    private OWLClass B;
    private OWLIndividual a;
    private OWLIndividual b;
    private OWLIndividual c;
    private OWLAnnotation annotationA1;
    private Set<OWLAnnotation> setAnnotationA1;
    private OWLAnnotation annotationA2;
    private Set<OWLAnnotation> setAnnotationA2;
    private OWLAnnotation annotationA3;
    private Set<OWLAnnotation> setAnnotationA3;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        df = manager.getOWLDataFactory();

        C = df.getOWLClass("cls:C");
        A = df.getOWLClass("cls:A");
        A1 = df.getOWLClass("cls:A1");
        A2 = df.getOWLClass("cls:A2");
        A3 = df.getOWLClass("cls:A3");
        B = df.getOWLClass("cls:B");
        a = df.getOWLNamedIndividual("ind:a");
        b = df.getOWLNamedIndividual("ind:b");
        c = df.getOWLNamedIndividual("ind:c");

        annotationA1 = df.getOWLAnnotation(df.getRDFSIsDefinedBy(), A1.getIRI());
        setAnnotationA1 = new HashSet<>();
        setAnnotationA1.add(annotationA1);
        annotationA2 = df.getOWLAnnotation(df.getRDFSIsDefinedBy(), A2.getIRI());
        setAnnotationA2 = new HashSet<>();
        setAnnotationA2.add(annotationA2);
        annotationA3 = df.getOWLAnnotation(df.getRDFSIsDefinedBy(), A3.getIRI());
        setAnnotationA3 = new HashSet<>();
        setAnnotationA3.add(annotationA3);

        rootOntology = manager.createOntology(Arrays.asList(
                df.getOWLSubClassOfAxiom(C,df.getOWLObjectIntersectionOf(A1, A2)),
                df.getOWLSubClassOfAxiom(A,df.getOWLNothing(), setAnnotationA1),
                df.getOWLClassAssertionAxiom(C,c),
                df.getOWLClassAssertionAxiom(A2,c),
                df.getOWLClassAssertionAxiom(A,a,setAnnotationA2),
                df.getOWLClassAssertionAxiom(B,a,setAnnotationA3)
        ));

        contextOntology = new ContextOntology(rootOntology);
    }

    @After
    public void tearDown() throws Exception {
        df.purge();
        manager.clearOntologies();
    }

    @Test
    public void testMetaOntology() throws Exception {

        OWLOntology ontology = manager.createOntology(Arrays.asList(
                df.getOWLSubClassOfAxiom(C,df.getOWLObjectIntersectionOf(A1,A2)),
                df.getOWLClassAssertionAxiom(C,c),
                df.getOWLClassAssertionAxiom(A2,c)
                ));

        assertEquals("Test for getting the meta ontology:",
                ontology.axioms().collect(Collectors.toSet()),
                contextOntology.getMetaOntology().axioms().collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntology() throws Exception {

        assertEquals("Test for getting the object ontology:",
                manager.createOntology(Arrays.asList(
                        df.getOWLSubClassOfAxiom(A, df.getOWLNothing()),
                        df.getOWLClassAssertionAxiom(A, a)))
                        .axioms().collect(Collectors.toSet()),
                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(A1, A2))).axioms().collect(Collectors.toSet()));


        assertEquals("Test for getting the object ontology:",
                manager.createOntology(Arrays.asList(
                        df.getOWLSubClassOfAxiom(A, df.getOWLNothing()),
                        df.getOWLClassAssertionAxiom(B, a)))
                        .axioms().collect(Collectors.toSet()),
                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(A1, A3))).axioms().collect(Collectors.toSet()));

    }
    
}