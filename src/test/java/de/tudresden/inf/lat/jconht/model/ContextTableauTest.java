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
import static org.junit.Assert.assertTrue;

/**
 * This is a test class for testing the correct dealing with rigid names .
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextTableauTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;

    private OWLClass clsC;
    private OWLClass clsA;
    private OWLClass A_Aa;
    private OWLClass A_notAa;
    private OWLClass A_ASubBottom;
    private OWLIndividual indA;
    private OWLIndividual indC;

    private OWLAxiom axiom_Aa;
    private OWLAxiom axiom_notAa;
    private OWLAxiom axiom_ASubBottom;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        clsC = dataFactory.getOWLClass("cls:C");
        clsA = dataFactory.getOWLClass("cls:A");
        A_Aa = dataFactory.getOWLClass("cls:A_Aa");
        A_notAa = dataFactory.getOWLClass("cls:A_notAa");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indC = dataFactory.getOWLNamedIndividual("ind:c");
        A_ASubBottom = dataFactory.getOWLClass("cls:A_ASubBottom");

        axiom_Aa = dataFactory.getOWLClassAssertionAxiom(clsA, indA, getIsDefinedBy(A_Aa));
        axiom_notAa = dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectComplementOf(clsA), indA, getIsDefinedBy(A_notAa));
        axiom_ASubBottom = dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing(), getIsDefinedBy(A_ASubBottom));

    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }


    @Test
    public void testInconsistentOntologyWithRigidNames() throws Exception {
        // TODO



    }

    // This tests example 7 from the technical report about ALC-ALC.
    @Test
    public void testExample7() throws Exception {

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLSubClassOfAxiom(clsC, A_ASubBottom),
                dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectIntersectionOf(clsC, A_Aa), indC),
                // object level
                axiom_ASubBottom,
                axiom_Aa
        ));

        ContextOntology contextOntology = new ContextOntology(ontology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        //System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());
    }


    @Test
    public void testNegatedOAxioms() throws Exception {
        // (¬[A(a)] ⊓ ¬[¬A(a)])(s)


        OWLOntology ontology1 = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectIntersectionOf(
                                dataFactory.getOWLObjectComplementOf(A_Aa),
                                dataFactory.getOWLObjectComplementOf(A_notAa)),
                        indC),
                // object level
                axiom_Aa,
                axiom_notAa
        ));

        ContextOntology contextOntology = new ContextOntology(ontology1);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());

    }


    @Test
    public void testBranchingWithNegatedAxioms() throws Exception {
        // [¬A(a)] ⊓ [A(a)] ⊑ C, C(s)

        OWLObjectProperty rolR = dataFactory.getOWLObjectProperty("rol:R");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                //
                // meta level
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectIntersectionOf(A_notAa, A_Aa),
                        clsC),
                dataFactory.getOWLClassAssertionAxiom(clsC,indC),
                //dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectComplementOf(A_Aa),indC),
                dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectSomeValuesFrom(rolR,clsC),indC),
                //
                // object level
                axiom_Aa,
                axiom_notAa));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        System.out.println("contextOntology = " + contextOntology);
        System.out.println("---------------------------------------------------------------------");
        System.out.println("reasoner.getTableau().getPermanentDLOntology() = " + reasoner.getTableau().getPermanentDLOntology());
        System.out.println("---------------------------------------------------------------------");

        assertTrue(reasoner.isConsistent());

    }





    // Helper functions

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {

        return Arrays.asList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private Collection<OWLAnnotation> getRigid() {

        return Arrays.asList(dataFactory.getRDFSLabel("rigid"));
    }
}
