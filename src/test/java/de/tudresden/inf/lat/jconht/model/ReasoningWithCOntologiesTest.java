package de.tudresden.inf.lat.jconht.model;

import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

/**
 * This is a test class for testing the correct dealing with rigid names .
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ReasoningWithCOntologiesTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }


    @Test
    public void testInconsistentOntologyWithRigidNames() throws Exception {




    }

    // This tests example 7 from the technical report about ALC-ALC.
    @Test
    public void testExample7() throws Exception {

        OWLClass clsC = dataFactory.getOWLClass("cls:C");
        OWLClass clsA = dataFactory.getOWLClass("cls:A");
        OWLClass A_AsubBot = dataFactory.getOWLClass("cls:aux1");
        OWLClass A_Aa = dataFactory.getOWLClass("cls:aux2");
        OWLIndividual indA = dataFactory.getOWLNamedIndividual("ind:a");
        OWLIndividual indC = dataFactory.getOWLNamedIndividual("ind:c");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLSubClassOfAxiom(clsC, A_AsubBot),
                dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectIntersectionOf(clsC, A_Aa), indC),
                // object level
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing(),getIsDefinedBy(A_AsubBot)),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA, getIsDefinedBy(A_Aa))
        ));

        ContextOntology contextOntology = new ContextOntology(ontology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        //System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());
    }


    @Test
    public void testNegatedOAxioms() throws Exception {
        // (¬[A(a)] ⊓ ¬[¬A(a)])(s)

        OWLClass clsA = dataFactory.getOWLClass("cls:A");
        OWLClass A_Aa = dataFactory.getOWLClass("cls:aux1");
        OWLClass A_notAa = dataFactory.getOWLClass("cls:aux2");
        OWLIndividual indA = dataFactory.getOWLNamedIndividual("ind:a");
        OWLIndividual indS = dataFactory.getOWLNamedIndividual("ind:s");

        OWLOntology ontology1 = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectIntersectionOf(
                                dataFactory.getOWLObjectComplementOf(A_Aa),
                                dataFactory.getOWLObjectComplementOf(A_notAa)),
                        indS),
                // object level
                dataFactory.getOWLClassAssertionAxiom(clsA, indA, getIsDefinedBy(A_Aa)),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectComplementOf(clsA), indA, getIsDefinedBy(A_notAa))
        ));

        ContextOntology contextOntology = new ContextOntology(ontology1);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());

    }

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {

        return Arrays.asList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private Collection<OWLAnnotation> getRigid() {

        return Arrays.asList(dataFactory.getRDFSLabel("rigid"));
    }

    private Collection<OWLAnnotation> getFlexible() {

        return Arrays.asList(dataFactory.getRDFSLabel("non-rigid"));
    }
}
