package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * TODO: REWRITE COMPLETELY!
 * <p>
 * <p>
 * This is a test class for <code>ContextOntology</code>.
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
    private OWLClass meta1;
    private OWLClass meta2;
    private OWLClass meta3;
    private OWLClass meta4;
    private OWLClass clsB;
    private OWLClass clsB2;
    private OWLClass clsB3;
    private OWLIndividual indA;
    private OWLIndividual indB;
    private OWLIndividual indC;
    private OWLObjectProperty rolR;
    private OWLObjectProperty rolS;
    private OWLObjectProperty rolT;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        clsC = dataFactory.getOWLClass("cls:C");
        clsA = dataFactory.getOWLClass("cls:A");
        meta1 = dataFactory.getOWLClass("cls:meta1");
        meta2 = dataFactory.getOWLClass("cls:meta2");
        meta3 = dataFactory.getOWLClass("cls:meta3");
        meta4 = dataFactory.getOWLClass("cls:meta4");
        clsB = dataFactory.getOWLClass("cls:B");
        clsB2 = dataFactory.getOWLClass("cls:B2");
        clsB3 = dataFactory.getOWLClass("cls:B3");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indB = dataFactory.getOWLNamedIndividual("ind:b");
        indC = dataFactory.getOWLNamedIndividual("ind:c");
        rolR = dataFactory.getOWLObjectProperty("rol:R");
        rolS = dataFactory.getOWLObjectProperty("rol:S");
        rolT = dataFactory.getOWLObjectProperty("rol:T");

        rootOntology = manager.createOntology(Arrays.asList(
                dataFactory.getOWLSubClassOfAxiom(clsC, dataFactory.getOWLObjectIntersectionOf(meta1, meta2)),
                dataFactory.getOWLClassAssertionAxiom(clsC, indC),
                dataFactory.getOWLClassAssertionAxiom(meta3, indC),
                dataFactory.getOWLSubClassOfAxiom(meta4, dataFactory.getOWLObjectAllValuesFrom(rolS, clsC)),
                //
                // global object ontology
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3, getObjectGlobal()),
                //
                // mapping of o-axioms
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing(), getIsDefinedBy(meta1)),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA, getIsDefinedBy(meta2)),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectIntersectionOf(
                                clsB,
                                dataFactory.getOWLObjectSomeValuesFrom(rolR, clsB)),
                        indA,
                        getIsDefinedBy(meta3)),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectSomeValuesFrom(rolT, dataFactory.getOWLThing()),
                        clsA,
                        getIsDefinedBy(meta4)),
                //
                // rigid names
                isRigid(clsB),
                isRigid(clsB2),
                isRigid(clsC),
                isRigid(rolT)
        ));
        // C ⊑ [A ⊑ ⊥] ⊓ [A(a)]  ∧  C(c)  ∧  [(B ⊓ ∃r.B)(a)](c)  ∧  [∃t.⊤ ⊑ A] ⊑ ∀s.C  ∧  ⊤ ⊑ [A ⊑ B]
        // meta1 := [A ⊑ ⊥]
        // meta2 := [A(a)]
        // meta3 := [(B ⊓ ∃r.B)(a)]
        // meta4 := [∃t.⊤ ⊑ A]
        contextOntology = new ContextOntology(rootOntology);
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }


    // Testing if correct signatures are retrieved

    @Test
    public void testObjectSignature() throws Exception {

        Stream<OWLEntity> objSignature = Stream.of(
                clsA, clsB, dataFactory.getOWLNothing(), dataFactory.getOWLThing(), clsB3,
                (OWLEntity) indA,
                rolR, rolT);

          // Debug output für Problem, dass clsA mehrmals in objSignature steht
//        contextOntology.objectSignature().forEach(System.out::println);
//        System.out.println();
//        rootOntology.signature().forEach(System.out::println);

        assertEquals("Test for object signature",
                objSignature.collect(Collectors.toSet()),
                contextOntology.objectSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testObjectClassesInSignature() throws Exception {

        Stream<OWLClass> streamOfObjectClassesInSignature = Stream.of(
                clsA,
                clsB,
                dataFactory.getOWLNothing(),
                dataFactory.getOWLThing(),
                clsB3);

        assertEquals("Test for classes in object signature",
                streamOfObjectClassesInSignature.collect(Collectors.toSet()),
                contextOntology.classesInObjectSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testObjectObjPropInSignature() throws Exception {

        Stream<OWLObjectProperty> streamOfObjectObjPropInSignature = Stream.of(
                rolR, rolT);

        assertEquals("Test for object properties in object signature",
                streamOfObjectObjPropInSignature.collect(Collectors.toSet()),
                contextOntology.objectPropertiesInObjectSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testMetaSignature() throws Exception {

        Stream<OWLEntity> streamOfMetaSignature = Stream.of(
                clsC, meta1, meta2, meta3, meta4,
                (OWLEntity) indC,
                rolS);

        assertEquals("Test for meta signature",
                streamOfMetaSignature.collect(Collectors.toSet()),
                contextOntology.metaSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testMetaClassesInSignature() throws Exception {

        Stream<OWLEntity> streamOfMetaClassesInSignature = Stream.of(
                clsC, meta1, meta2, meta3, meta4);

        assertEquals("Test for classes in meta signature",
                streamOfMetaClassesInSignature.collect(Collectors.toSet()),
                contextOntology.classesInMetaSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testMetaObjPropInSignature() throws Exception {

        Stream<OWLObjectProperty> streamOfMetaObjPropInSignature = Stream.of(
                rolS
        );

        assertEquals("Test for object properties in meta signature",
                streamOfMetaObjPropInSignature.collect(Collectors.toSet()),
                contextOntology.objectPropertiesInMetaSignature().collect(Collectors.toSet()));
    }


    // Testing if correct rigid names are retrieved

    @Test
    public void testRigidClasses() throws Exception {

        Stream<OWLEntity> streamOfRigidClasses = Stream.of(
                clsB);

        assertEquals("Test for rigid classes",
                streamOfRigidClasses.collect(Collectors.toSet()),
                contextOntology.rigidClasses().collect(Collectors.toSet()));
    }

    @Test
    public void testRigidObjectProperties() throws Exception {

        Stream<OWLEntity> streamOfRigidObjProp = Stream.of(
                rolT);

        assertEquals("Test for rigid object properties",
                streamOfRigidObjProp.collect(Collectors.toSet()),
                contextOntology.rigidObjectProperties().collect(Collectors.toSet()));
    }

    @Test
    public void testFlexibleClasses() throws Exception {

        Stream<OWLEntity> streamOfFlexibleClasses = Stream.of(
                clsA, clsB3);

        assertEquals("Test for flexible classes",
                streamOfFlexibleClasses.collect(Collectors.toSet()),
                contextOntology.flexibleClasses().collect(Collectors.toSet()));
    }

    @Test
    public void testFlexibleObjectProperties() throws Exception {

        Stream<OWLEntity> streamOfFlexibleObjProp = Stream.of(
                rolR);

        assertEquals("Test for flexible object properties",
                streamOfFlexibleObjProp.collect(Collectors.toSet()),
                contextOntology.flexibleObjectProperties().collect(Collectors.toSet()));
    }

    @Test
    public void testContainsRigidNames() throws Exception {

        assertTrue(contextOntology.containsRigidNames());
    }

    @Test
    public void testOuterAbstractedMetaConcepts() throws Exception {

        Stream<OWLEntity> streamOfAbstractedMetaConcept = Stream.of(
                meta1, meta2, meta3, meta4);

        assertEquals("Test for outer abstracted meta concepts",
                streamOfAbstractedMetaConcept.collect(Collectors.toSet()),
                contextOntology.outerAbstractedMetaConcepts().collect(Collectors.toSet()));
    }


    // Tests for retrieval of ontologies

    @Test
    public void testGlobalObjectOntology() throws Exception {

        Stream<OWLAxiom> streamOfGlobalObjectAxioms = Stream.of(
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3)
        );

        assertEquals(
                streamOfGlobalObjectAxioms.collect(Collectors.toSet()),
                contextOntology.globalObjectOntology().collect(Collectors.toSet()));

    }

    @Test
    public void testMetaOntology() throws Exception {
        // TODO

        OWLOntology metaOntology = manager.createOntology(Arrays.asList(
                dataFactory.getOWLSubClassOfAxiom(clsC, dataFactory.getOWLObjectIntersectionOf(meta1, meta2)),
                dataFactory.getOWLClassAssertionAxiom(clsC, indC),
                dataFactory.getOWLClassAssertionAxiom(meta3, indC),
                dataFactory.getOWLSubClassOfAxiom(meta4, dataFactory.getOWLObjectAllValuesFrom(rolS, clsC))));

        assertEquals("Test for getting the meta ontology:",
                metaOntology.axioms().collect(Collectors.toSet()),
                contextOntology.getMetaOntology().axioms().collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntology1() throws Exception {

        OWLOntology objectOntology = manager.createOntology(Arrays.asList(
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3),
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectIntersectionOf(
                                clsB,
                                dataFactory.getOWLObjectSomeValuesFrom(rolR, clsB)),
                        indA),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectSomeValuesFrom(rolT, dataFactory.getOWLThing()), clsA)));

        // TODO macht man das so?
        Set<OWLClass> metaConcepts = new HashSet<>(Arrays.asList(meta1, meta2, meta3, meta4));

        assertEquals("Test 1 for getting the object ontology:",
                objectOntology.axioms().collect(Collectors.toSet()),
                contextOntology.getObjectOntology(metaConcepts.stream(),Stream.of()).axioms().collect(Collectors.toSet()));
    }



    @Test
    public void testGetObjectOntology2() throws Exception {
        //TODO das hier sollte klappen, tut es aber nicht, wie könnte man das testen? Bräuchte ich um ObjOntology richtig zu testen

        OWLAxiom axiom1 = dataFactory.getOWLClassAssertionAxiom(clsC,dataFactory.getOWLAnonymousIndividual());
        OWLAxiom axiom2 = dataFactory.getOWLClassAssertionAxiom(clsC,dataFactory.getOWLAnonymousIndividual());

        axiom1.components().forEach(System.out::println);

        System.out.println(axiom1.equalsIgnoreAnnotations(axiom2));

        assertEquals(
                dataFactory.getOWLClassAssertionAxiom(clsC,dataFactory.getOWLAnonymousIndividual()),
                dataFactory.getOWLClassAssertionAxiom(clsC,dataFactory.getOWLAnonymousIndividual())
        );
    }

//        assertEquals("Test for getting the object ontology:",
//                manager.createOntology(Arrays.asList(
//                        dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
//                        dataFactory.getOWLClassAssertionAxiom(clsA, indA)))
//                        .axioms().collect(Collectors.toSet()),
//                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(meta1, meta2))).axioms().collect(Collectors.toSet()));
//
//
//        assertEquals("Test for getting the object ontology:",
//                manager.createOntology(Arrays.asList(
//                        dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
//                        dataFactory.getOWLClassAssertionAxiom(clsB, indA)))
//                        .axioms().collect(Collectors.toSet()),
//                contextOntology.getObjectOntology(new HashSet<>(Arrays.asList(meta1, meta3))).axioms().collect(Collectors.toSet()));
//    }


    // Helper functions

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {

        return Arrays.asList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private OWLAnnotationAssertionAxiom isRigid(HasIRI hasIRI) {

        return dataFactory.getOWLAnnotationAssertionAxiom(
                hasIRI.getIRI(),
                dataFactory.getRDFSLabel("rigid"));
    }

    private Collection<OWLAnnotation> getObjectGlobal() {

        return Arrays.asList(dataFactory.getRDFSLabel("objectGlobal"));
    }
}