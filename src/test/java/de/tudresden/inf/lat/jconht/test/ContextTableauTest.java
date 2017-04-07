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

import static org.junit.Assert.assertEquals;
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
    private Configuration confWithDebug;
    private Configuration confWithoutDebug;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        builder = new AxiomBuilder(dataFactory);
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
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(),6);
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
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(),0);
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
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(),23);
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
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(),1);
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
        assertEquals(((ContextTableau) reasoner.getTableau()).consistentInterpretations().count(),2);
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
