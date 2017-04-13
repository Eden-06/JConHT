package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.AxiomBuilder;
import de.tudresden.inf.lat.jconht.model.Configuration;
import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import de.tudresden.inf.lat.jconht.tableau.ContextTableau;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

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
    private AxiomBuilder rosiBuilder;
    private Configuration confWithDebug;
    private Configuration confWithoutDebug;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        builder = new AxiomBuilder(dataFactory);
        rosiBuilder = new AxiomBuilder(dataFactory,
                "http://www.rosi-project.org/ontologies#",
                "ind:",
                "http://www.rosi-project.org/ontologies#");
        confWithDebug = new Configuration(true);
        confWithoutDebug = new Configuration(false);

    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    // This tests example 7 from the technical report about ALC-ALC.
    @Test
    public void testExample7() throws Exception {

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("C ⊑ meta1"),
                builder.stringToOWLAxiom("(C ⊓ meta2)(c)"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A ⊑ ⊥ @ meta1"),
                builder.stringToOWLAxiom("A(a) @ meta2")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        //System.out.println("contextOntology = " + contextOntology);

        assertFalse(reasoner.isConsistent());
    }


    @Test
    public void testNegatedObjAxioms1() throws Exception {
        // (¬[A(a)] ⊓ ¬[¬A(a)])(s)

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("(¬meta1 ⊓ ¬meta2)(c)"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("¬A(a) @ meta2")
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
                builder.stringToOWLAxiom("¬C(s)"),
                builder.stringToOWLAxiom("⊤ ⊑ C ⊔ ¬meta1"),
                builder.stringToOWLAxiom("⊤ ⊑ C ⊔ ¬meta2"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("¬A(a) @ meta2")
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
                builder.stringToOWLAxiom("meta1 ⊑ C"),
                builder.stringToOWLAxiom("¬C ⊑ meta2"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("¬A(a) @ meta1"),
                builder.stringToOWLAxiom("A ⊑ ⊥ @ meta2")
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

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("meta1 ⊓ meta2 ⊑ C"),
                builder.stringToOWLAxiom("C(c)"),
                builder.stringToOWLAxiom("(∃R.C)(c)"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("¬A(a) @ meta2")
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

//        System.out.println("contextOntology = " + contextOntology);
//        System.out.println("---------------------------------------------------------------------");
//        System.out.println("reasoner.getTableau().getPermanentDLOntology() = " + reasoner.getTableau().getPermanentDLOntology());
//        System.out.println("---------------------------------------------------------------------");

        assertTrue(reasoner.isConsistent());

    }

    @Test
    public void testObjectOntologyConsistencyRecursion() throws Exception {

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("⊤(a) @ meta1"),
                builder.stringToOWLAxiom("⊤(a) @ meta2"),
                builder.stringToOWLAxiom("⊤(a) @ meta3")
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology, new Configuration(true));
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertTrue(reasoner.isConsistent());

        reasoner.dispose();

    }

    @Test
    public void testModelWithSeveralNodes() throws Exception {
        System.out.println("Executing testModelWithSeveralNodes:");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("A ⊑ ≥ 2R.B"),
                builder.stringToOWLAxiom("B ⊑ ≥ 2R.C"),
                builder.stringToOWLAxiom("A(a)")
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertTrue(reasoner.isConsistent());

    }

    @Test
    public void testInconsistentOntologyForRigidNamesPre() throws Exception {
        System.out.println("Executing testInconsistentOntologyForRigidNamesPre:");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("meta1(c)"),
                builder.stringToOWLAxiom("meta1 ⊑ ∃R.meta2"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("¬A(a) @ meta2")
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertTrue(reasoner.isConsistent());
    }

    @Test
    public void testInconsistentOntologyForRigidNames() throws Exception {
        System.out.println("Executing testInconsistentOntologyForRigidNames:");

        OWLOntology rootOntology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("meta1(c)"),
                builder.stringToOWLAxiom("meta1 ⊑ ∃R.meta2"),
                // mapping of o-axioms
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("¬A(a) @ meta2"),
                // Rigidity axioms
                isRigid(builder.stringToConcept("A").asOWLClass())
        ));

        ContextOntology contextOntology = new ContextOntology(rootOntology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        assertFalse(reasoner.isConsistent());
    }

    @Test
    public void testConsistentInterpretations1() throws Exception {
        System.out.println("Executing testConsistentInterpretations1:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("A(a)"),
                builder.stringToOWLAxiom("A ⊑ B1 ⊔ B2 ⊔ B3"),
                builder.stringToOWLAxiom("A ⊑ C1 ⊔ C2")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        //((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(), 6);
    }

    @Test
    public void testConsistentInterpretations2() throws Exception {
        System.out.println("Executing testConsistentInterpretations2:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("A(a)"),
                builder.stringToOWLAxiom("A ⊑ B1 ⊔ B2 ⊔ B3"),
                builder.stringToOWLAxiom("A ⊑ C1 ⊔ C2"),
                builder.stringToOWLAxiom("A ⊑ ⊥")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        ((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(), 0);
    }

    @Test
    public void testConsistentInterpretations3() throws Exception {
        System.out.println("Executing testConsistentInterpretations3:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("A(a)"),
                builder.stringToOWLAxiom("A ⊑ B1 ⊔ B2 ⊔ B3"),
                builder.stringToOWLAxiom("A ⊑ C1 ⊔ C2 ⊔ C3"),
                builder.stringToOWLAxiom("A ⊑ D1 ⊔ D2 ⊔ D3"),
                builder.stringToOWLAxiom("C1 ⊓ B2 ⊑ ⊥"),
                builder.stringToOWLAxiom("C3 ⊓ B1 ⊓ D3 ⊑ ⊥")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        ((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(), 23);
    }

    @Test
    public void testConsistentInterpretations4() throws Exception {
        System.out.println("Executing testConsistentInterpretations4:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("C ⊑ meta1"),
                builder.stringToOWLAxiom("(C ⊓ meta2)(c)"),
                // object level
                builder.stringToOWLAxiom("A ⊑ ⊥ @ meta1"),
                builder.stringToOWLAxiom("A(a) @ meta2")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        //System.out.println(reasoner.isConsistent());
        //((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(), 1);
    }

    @Test
    public void testConsistentInterpretations5() throws Exception {
        System.out.println("Executing testConsistentInterpretations5:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                // meta level
                builder.stringToOWLAxiom("A(a)"),
                builder.stringToOWLAxiom("A ⊑ ∃ R.B ⊔ ∃ S.C")
                //builder.stringToOWLAxiom("B ⊑ ⊥")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        System.out.println(reasoner.isConsistent());
        ((ContextTableau) reasoner.getTableau()).consistentInterpretations().forEach(System.out::println);
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(), 2);
    }

    @Test
    public void testMetaConceptsOfNode() throws Exception {
        System.out.println("Executing testMetaConceptsOfNode:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("C(c)"),
                builder.stringToOWLAxiom("meta1(c)"),
                builder.stringToOWLAxiom("¬D(c)"),
                builder.stringToOWLAxiom("¬meta2(c)"),
                //
                builder.stringToOWLAxiom("A(a) @ meta1"),
                builder.stringToOWLAxiom("B(a) @ meta2")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        reasoner.isConsistent();
        ContextTableau contextTableau = (ContextTableau) reasoner.getTableau();
        Node node = contextTableau.getFirstTableauNode();
        if (contextOntology.getConfiguration().debugOutput()) {
            contextTableau.positiveMetaConceptsOfNode(node).forEach(System.out::println);
            System.out.println();
            contextTableau.negativeMetaConceptsOfNode(node).forEach(System.out::println);
        }

        assertEquals("Test positive meta concepts:",
                contextTableau.positiveMetaConceptsOfNode(node).collect(Collectors.toSet()),
                Stream.of(
                        dataFactory.getOWLClass("cls:meta1"),
                        dataFactory.getOWLClass("cls:DUAL.meta2")
                ).collect(Collectors.toSet()));

        assertEquals("Test negative meta concepts:",
                contextTableau.negativeMetaConceptsOfNode(node).collect(Collectors.toSet()),
                Stream.of(
                        dataFactory.getOWLClass("cls:meta2")
                ).collect(Collectors.toSet()));

    }

    @Test
    public void testMergedNodes() throws Exception {
        System.out.println("Executing testMergedNodes:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("A(a)"),
                builder.stringToOWLAxiom("B(b)"),
                builder.stringToOWLAxiom("C(c)"),
                builder.stringToOWLAxiom("R(a,b)"),
                builder.stringToOWLAxiom("R(a,c)"),
                builder.stringToOWLAxiom("A ⊑ ≤ 1 R.⊤")));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        reasoner.isConsistent();
    }

    @Test
    public void testMergeNodes2() throws Exception {
        System.out.println("Executing testMergeNodes2:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("(≥2count.B)(a) @ global"),
                builder.stringToOWLAxiom("B ⊑ ∃ plays^-1.A @ global"),
                builder.stringToOWLAxiom("A ⊑ ≤1plays.B @ global"),
                builder.stringToOWLAxiom("∃ plays.B ⊑ ≥1plays.(C) @ global"),
                builder.stringToOWLAxiom("A ⊑ ≤1plays.C @ global"),
                builder.stringToOWLAxiom("C ⊑ ∃ count^-1 .{a} @ global"),
                builder.stringToOWLAxiom("(≤1count.C)(a) @ global"),


                builder.stringToOWLAxiom("C ⊑ ≤1plays^-1.A @ global"),
                builder.stringToOWLAxiom("C ⊑ ≥1plays^-1.A @ global")
//
//
//                builder.stringToOWLAxiom("(A⊓B)⊔(A⊓C)⊔(A⊓D)⊔(B⊓C) ⊑ ⊥ @ global"),
//                builder.stringToOWLAxiom("¬(A⊔B⊔C)(a) @ global"),
//                builder.stringToOWLAxiom("∃plays.⊤ ⊑ A @ global"),
//                builder.stringToOWLAxiom("∃count.⊤ ⊑ {a} @ global"),
//                //
//                builder.stringToOWLAxiom("A ⊑ ≤1plays.B @ global"),
//                builder.stringToOWLAxiom("A ⊑ ≤1plays.C @ global"),
//                //
//                builder.stringToOWLAxiom("C ⊑ ∃ plays^-1.A @ global"),
//                //
//                builder.stringToOWLAxiom("∃ plays.B ⊑ ≤1plays.(C) @ global"),
//                builder.stringToOWLAxiom("∃ plays.B ⊑ ≥1plays.(C) @ global"),
//                builder.stringToOWLAxiom("B ⊑ ∃ count^-1 .{a} @ global"),
//                builder.stringToOWLAxiom("C ⊑ ∃ count^-1 .{a} @ global"),
//                //
//                builder.stringToOWLAxiom("(≤1count.C)(a) @ global")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        reasoner.isConsistent();
    }

    @Test
    public void testBankExamplePre() throws Exception {
        System.out.println("Executing testBankExamplePre:");

        assertTrue(checkBankExample(true, Stream.of()));

    }

    @Test
    public void testBankExampleWithBank() throws Exception {
        System.out.println("Executing testBankExampleWithBank:");

        assertTrue(checkBankExample(true, Stream.of(
                rosiBuilder.stringToOWLAxiom("Bank(c)")
        )));
    }

    @Test
    public void testBankExampleWithTransaction() throws Exception {
        System.out.println("Executing testBankExampleWithTransaction:");

        assertTrue(checkBankExample(true, Stream.of(
                rosiBuilder.stringToOWLAxiom("Transaction(c)")
        )));
    }

    @Test
    public void testBankExampleWithTransactionInconsistent1() throws Exception {
        System.out.println("Executing testBankExampleWithTransactionInconsistent1:");

        assertFalse(checkBankExample(false, Stream.of(
                rosiBuilder.stringToOWLAxiom("Transaction(c)"),
                rosiBuilder.stringToOWLAxiom("Transaction ⊑ meta1"),
                rosiBuilder.stringToOWLAxiom("Source ⊑ ⊥ @ meta1")
        )));
    }

    @Test
    public void testBankExampleWithTransactionInconsistent2() throws Exception {
        System.out.println("Executing testBankExampleWithTransactionInconsistent2:");

        assertFalse(checkBankExample(false, Stream.of(
                rosiBuilder.stringToOWLAxiom("Transaction(c)"),
                rosiBuilder.stringToOWLAxiom("Transaction ⊑ meta1"),
                rosiBuilder.stringToOWLAxiom("Target ⊑ ⊥ @ meta1")
        )));
    }

    @Test
    public void testBankExampleWithTransactionInconsistent3() throws Exception {
        System.out.println("Executing testBankExampleWithTransactionInconsistent3:");

        assertFalse(checkBankExample(false, Stream.of(
                rosiBuilder.stringToOWLAxiom("Transaction(c)"),
                rosiBuilder.stringToOWLAxiom("Transaction ⊑ meta1 ⊓ meta2 ⊓ meta3"),
                rosiBuilder.stringToOWLAxiom("Target(t1) @ meta1"),
                rosiBuilder.stringToOWLAxiom("Target(t2) @ meta2"),
                rosiBuilder.stringToOWLAxiom("t1≠t2 @ meta3")
        )));
    }

    @Test
    public void testEquivalenceAxiom() throws Exception {
        System.out.println("Executing testEquivalenceAxiom:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("A ≡ ¬B @ global"),
                builder.stringToOWLAxiom("A(a) @ global"),
                builder.stringToOWLAxiom("¬B(b) @ global")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        reasoner.isConsistent();

    }

    @Test
    public void testForAllAxiom() throws Exception {
        System.out.println("Executing testForAllAxiom:");

        OWLOntology ontology = manager.createOntology(Stream.of(
                builder.stringToOWLAxiom("¬A(a) @ global"),
                builder.stringToOWLAxiom("∀R.¬C ⊑ A @ global")
        ));

        ContextOntology contextOntology = new ContextOntology(ontology, confWithDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        reasoner.isConsistent();

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

    private boolean checkBankExample(boolean debug, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException {
        File file = new File("input/Bank.owl");
        OWLOntology rootOntology = manager.loadOntologyFromOntologyDocument(file);
        manager.addAxioms(rootOntology, axioms);
        ContextOntology contextOntology = new ContextOntology(
                rootOntology,
                new Configuration(true, debug, false, false));
        ContextReasoner reasoner = new ContextReasoner(contextOntology);
        return reasoner.isConsistent();
    }
}
