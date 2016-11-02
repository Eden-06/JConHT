package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This is a test class for <code>AxiomNegator</code>.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegatorTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private ReasonerFactory reasonerFactory;
    private AxiomNegator axiomNegator;

    private OWLClass clsA;
    private OWLClass clsB;
    private OWLClass clsC;
    private OWLClass clsD;
    private OWLIndividual indA;
    private OWLIndividual indB;
    private OWLObjectProperty roleR;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        reasonerFactory = new ReasonerFactory();
        axiomNegator = new AxiomNegator(dataFactory);

        clsA = dataFactory.getOWLClass("cls:A");
        clsB = dataFactory.getOWLClass("cls:B");
        clsC = dataFactory.getOWLClass("cls:C");
        clsD = dataFactory.getOWLClass("cls:D");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indB = dataFactory.getOWLNamedIndividual("ind:b");
        roleR = dataFactory.getOWLObjectProperty("rol:r");
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    @Test
    public void testOWLNegativeObjectPropertyAssertionAxiom() throws Exception {

        // Construct r(a, b).
        OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom =
                dataFactory.getOWLObjectPropertyAssertionAxiom(roleR, indA, indB);

        // Construct ¬r(a, b).
        OWLNegativeObjectPropertyAssertionAxiom negativeObjectPropertyAssertionAxiom =
                dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(roleR, indA, indB);

        assertEquals("Negation of OWLNegativeObjectPropertyAssertionAxiom",
                objectPropertyAssertionAxiom,
                negativeObjectPropertyAssertionAxiom.accept(axiomNegator));
    }

    @Test
    public void testOWLObjectPropertyAssertionAxiom() throws Exception {

        // Construct r(a, b).
        OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom =
                dataFactory.getOWLObjectPropertyAssertionAxiom(roleR, indA, indB);

        // Construct ¬r(a, b).
        OWLNegativeObjectPropertyAssertionAxiom negativeObjectPropertyAssertionAxiom =
                dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(roleR, indA, indB);

        assertEquals("Negation of OWLObjectPropertyAssertionAxiom",
                negativeObjectPropertyAssertionAxiom,
                objectPropertyAssertionAxiom.accept(axiomNegator));
    }

    @Test
    public void testOWLClassAssertionAxiom() throws Exception {

        // Construct (A ⊓ B)(a).
        OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(clsA, clsB),
                indA);

        // Get negation of (A ⊓ B)(a).
        OWLAxiom negatedAxiom = axiom.accept(axiomNegator);

        assertTrue("Negated OWLClassAssertionAxiom is again an OWLClassAssertionAxiom",
                negatedAxiom instanceof OWLClassAssertionAxiom);

        assertEquals("Individuals of OWLClassAssertionAxiom and negated OWLClassAssertionAxiom are equal",
                axiom.getIndividual(),
                ((OWLClassAssertionAxiom) negatedAxiom).getIndividual());

        // C is negation of D iff ⊭(C ⊓ D)(x) and ⊨(C ⊔ D)(x)
        assertTrue("Negation of OWLClassExpression: (C ⊓ D)(x) inconsistent",
                isInconsistent(Stream.of(
                        dataFactory.getOWLClassAssertionAxiom(
                                dataFactory.getOWLObjectIntersectionOf(
                                        axiom.getClassExpression(),
                                        ((OWLClassAssertionAxiom) negatedAxiom).getClassExpression()),
                                dataFactory.getOWLAnonymousIndividual()))));

        assertTrue("Negation of OWLClassExpression: ⊨ (C ⊔ D)(x)",
                entailedByEmptyOntology(dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectUnionOf(
                                axiom.getClassExpression(),
                                ((OWLClassAssertionAxiom) negatedAxiom).getClassExpression()),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testOWLClassAssertionAxiomSoundness() throws Exception {

        // Construct (A ⊓ B)(a).
        OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(clsA, clsB),
                indA);

        assertTrue("OWLClassAssertionAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLSubclassOfAxiomSoundness() throws Exception {

        // Construct A ⊑ B.
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(clsA, clsB);

        assertTrue("OWLSubClassOfAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLDisjointClassesAxiomSoundness() throws Exception {

        // Construct DisjointUnion(A, B, C).
        OWLDisjointClassesAxiom axiom = dataFactory.getOWLDisjointClassesAxiom(clsA, clsB, clsC);

        assertTrue("OWLDisjointClassesAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLObjectPropertyDomainAxiomSoundness() throws Exception {

        // Construct Dom(r) = C.
        OWLObjectPropertyDomainAxiom axiom = dataFactory.getOWLObjectPropertyDomainAxiom(roleR, clsA);

        assertTrue("OWLObjectPropertyDomainAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLObjectPropertyRangeAxiomSoundness() throws Exception {

        // Construct Ran(r) = C.
        OWLObjectPropertyRangeAxiom axiom = dataFactory.getOWLObjectPropertyRangeAxiom(roleR, clsA);

        assertTrue("OWLObjectPropertyRangeAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLDisjointUnionAxiomSoundness() throws Exception {

        // Construct DisjointUnionOf(A, B, C, D).
        OWLDisjointUnionAxiom axiom = dataFactory.getOWLDisjointUnionAxiom(clsA, Stream.of(clsB, clsC, clsD));

        assertTrue("OWLDisjointUnionAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    @Test
    public void testOWLEquivalentClassesAxiomSoundness() throws Exception {

        // Construct A ≡ B ≡ C.
        OWLEquivalentClassesAxiom axiom = dataFactory.getOWLEquivalentClassesAxiom(clsA, clsB, clsC);

        assertTrue("OWLEquivalentClassesAxiom is inconsistent with its negation",
                isInconsistent(Stream.of(
                        axiom,
                        axiom.accept(axiomNegator))));
    }

    private boolean isInconsistent(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException {

        OWLOntology ontology = manager.createOntology(axioms);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

        boolean inconsistent = !reasoner.isConsistent();

        reasoner.dispose();
        manager.removeOntology(ontology);

        return inconsistent;
    }

    private boolean entailedByEmptyOntology(OWLAxiom axiom) throws OWLOntologyCreationException {

        OWLOntology ontology = manager.createOntology();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

        boolean entailed = reasoner.isEntailed(axiom);

        reasoner.dispose();
        manager.removeOntology(ontology);

        return entailed;
    }
}