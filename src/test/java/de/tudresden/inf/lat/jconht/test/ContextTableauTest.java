package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.AxiomBuilder;
import de.tudresden.inf.lat.jconht.model.Configuration;
import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import de.tudresden.inf.lat.jconht.tableau.ContextTableau;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Collections;
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
    private AxiomBuilder builder;

    private OWLClass clsC;
    private OWLClass clsA;
    private OWLClass clsB;
    private OWLClass A_Aa;
    private OWLClass A_notAa;
    private OWLClass A_ASubBottom;
    private OWLClass thing;
    private OWLIndividual indA;
    private OWLIndividual indC;
    private OWLObjectProperty rolR;

    private OWLAxiom axiom_Aa;
    private OWLAxiom axiom_notAa;
    private OWLAxiom axiom_ASubBottom;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        builder = new AxiomBuilder(dataFactory);

        clsC = dataFactory.getOWLClass("cls:C");
        clsA = dataFactory.getOWLClass("cls:A");
        clsB = dataFactory.getOWLClass("cls:B");
        A_Aa = dataFactory.getOWLClass("cls:A_Aa");
        A_notAa = dataFactory.getOWLClass("cls:A_notAa");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indC = dataFactory.getOWLNamedIndividual("ind:c");
        A_ASubBottom = dataFactory.getOWLClass("cls:A_ASubBottom");
        rolR = dataFactory.getOWLObjectProperty("rol:R");
        thing = dataFactory.getOWLThing();

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

        ContextOntology contextOntology = new ContextOntology(ontology, new Configuration(true, true));
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        //System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());
    }


    @Test
    public void testNegatedObjAxioms1() throws Exception {
        // (¬[A(a)] ⊓ ¬[¬A(a)])(s)


        OWLOntology rootOntology = manager.createOntology(Stream.of(
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

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

//        System.out.println("contextOntology = " + contextOntology);
//        System.out.println("---------------------------------------------------------------------");
//        System.out.println("reasoner.getTableau().getPermanentDLOntology() = " + reasoner.getTableau().getPermanentDLOntology());
//        System.out.println("---------------------------------------------------------------------");

        assertFalse(reasoner.isConsistent());

    }

    @Test
    public void testNegatedObjAxioms2() throws Exception {
        System.out.println("Executing testNegatedAxioms2:");

        // ¬C(s),  ⊤ ⊑ C ⊔ ¬[A(a)],  ⊤ ⊑ C ⊔ ¬[¬A(a)]
        OWLOntology rootOntology = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectComplementOf(clsC), indC),
                dataFactory.getOWLSubClassOfAxiom(
                        thing,
                        dataFactory.getOWLObjectUnionOf(
                                clsC,
                                dataFactory.getOWLObjectComplementOf(A_Aa))),
                dataFactory.getOWLSubClassOfAxiom(
                        thing,
                        dataFactory.getOWLObjectUnionOf(
                                clsC,
                                dataFactory.getOWLObjectComplementOf(A_notAa))),
                // object level
                axiom_Aa,
                axiom_notAa
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

//        System.out.println("contextOntology = " + contextOntology);
//        System.out.println("---------------------------------------------------------------------");
//        System.out.println("reasoner.getTableau().getPermanentDLOntology() = " + reasoner.getTableau().getPermanentDLOntology());
//        System.out.println("---------------------------------------------------------------------");

        assertFalse(reasoner.isConsistent());

    }

    @Test
    public void testNegatedObjAxiomsWithDuals() throws Exception {
        System.out.println("Executing testNegatedObjAxiomsWithDuals:");

        // ¬C(s), [¬A(a)] ⊑ C, ¬C ⊑ [A ⊑ ⊥]
        OWLOntology rootOntology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("¬C(c)"),
                builder.stringToOWLAxiom("A_notAa ⊑ C"),
                builder.stringToOWLAxiom("¬C ⊑ A_ASubBottom"),
                // object level
                axiom_Aa,
                axiom_notAa,
                axiom_ASubBottom
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

//        System.out.println("contextOntology = " + contextOntology);
//        System.out.println("---------------------------------------------------------------------");
//        System.out.println("reasoner.getTableau().getPermanentDLOntology() = " + reasoner.getTableau().getPermanentDLOntology());
//        System.out.println("---------------------------------------------------------------------");

        assertFalse(reasoner.isConsistent());

    }

    @Test
    public void testBranchingWithNegatedAxioms() throws Exception {
        // [¬A(a)] ⊓ [A(a)] ⊑ C, C(c), (∃r.C)(c)

        OWLObjectProperty rolR = dataFactory.getOWLObjectProperty("rol:R");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                //
                // meta level
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectIntersectionOf(A_notAa, A_Aa),
                        clsC),
                dataFactory.getOWLClassAssertionAxiom(clsC, indC),
                dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectSomeValuesFrom(rolR, clsC), indC),
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

    @Test
    public void testObjectOntologyConsistencyRecursion() throws Exception {

        OWLClass meta1 = dataFactory.getOWLClass("cls:meta1");
        OWLClass meta2 = dataFactory.getOWLClass("cls:meta2");
        OWLClass meta3 = dataFactory.getOWLClass("cls:meta3");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                dataFactory.getOWLClassAssertionAxiom(thing, indA, getIsDefinedBy(meta1)),
                dataFactory.getOWLClassAssertionAxiom(thing, indA, getIsDefinedBy(meta2)),
                dataFactory.getOWLClassAssertionAxiom(thing, indA, getIsDefinedBy(meta3))
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertTrue(reasoner.isConsistent());

        reasoner.dispose();

    }

    @Test
    public void testModelWithSeveralNodes() throws Exception {
        System.out.println("Executing testModelWithSeveralNodes:");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                dataFactory.getOWLSubClassOfAxiom(clsA,
                        dataFactory.getOWLObjectMinCardinality(2, rolR, clsB)),
                dataFactory.getOWLSubClassOfAxiom(clsB,
                        dataFactory.getOWLObjectMinCardinality(2, rolR, clsC)),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA)
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertTrue(reasoner.isConsistent());

    }

    @Test
    public void testRigidNames() throws Exception {
        System.out.println("Executing testRigidNames:");
        // TODO not done yet!

        OWLAxiom axiom = isRigid(clsA);
        System.out.println(axiom);
    }

    @Test
    public void testListModels() throws Exception {
        System.out.println("Executing testListModels:");

//        ContextReasoner reasoner = new ContextReasoner(new ContextOntology(manager.createOntology(Arrays.asList(
//                builder.stringToOWLAxiom("A(a)")
//                ,builder.stringToOWLAxiom("A ⊑ B1 ⊔ B2")
//                ,builder.stringToOWLAxiom("A ⊑ C1 ⊔ C2")
////                ,builder.stringToOWLAxiom("C1 ⊓ B2 ⊑ ⊥")
////                ,builder.stringToOWLAxiom("A ⊑ ⊥")
//                )), new Configuration(true,true)));
//
//        ((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
//        System.out.println("---------------------------");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                dataFactory.getOWLSubClassOfAxiom(clsC, A_ASubBottom),
                dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectIntersectionOf(clsC, A_Aa), indC),
                // object level
                axiom_ASubBottom,
                axiom_Aa
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, new Configuration(true, true));
        ContextReasoner reasoner2 = new ContextReasoner(contextOntology);
        ((ContextTableau) reasoner2.getTableau()).consistentInterpretations().forEach(System.out::println);

        System.out.println("---------------------------");

        ((ContextTableau) reasoner2.getTableau()).listModels().forEach(System.out::println);


    }

    // Helper functions

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {

        return Collections.singletonList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private OWLAnnotationAssertionAxiom isRigid(HasIRI hasIRI) {

        return dataFactory.getOWLAnnotationAssertionAxiom(
                hasIRI.getIRI(),
                dataFactory.getRDFSLabel("rigid"));
    }
}
