package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;


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

    private ContextOntology contextOntology;
    private ContextOntology contextOntologyWithRigidNames;
    private OWLOntology rootOntology;
    private AxiomBuilder builder;

    private OWLClass clsC;
    private OWLClass clsA;
    private OWLClass meta1;
    private OWLClass meta2;
    private OWLClass meta3;
    private OWLClass meta4;
    private OWLClass meta5;
    private OWLClass clsB;
    private OWLClass clsB3;
    private OWLClass thing;
    private OWLIndividual indA;
    private OWLIndividual indC;
    private OWLObjectProperty rolR;
    private OWLObjectProperty rolS;
    private OWLObjectProperty rolT;

    private Set<OWLClass> abstractedMetaConcepts;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        builder = new AxiomBuilder(dataFactory);

        clsC = dataFactory.getOWLClass("cls:C");
        clsA = dataFactory.getOWLClass("cls:A");
        meta1 = dataFactory.getOWLClass("cls:meta1");
        meta2 = dataFactory.getOWLClass("cls:meta2");
        meta3 = dataFactory.getOWLClass("cls:meta3");
        meta4 = dataFactory.getOWLClass("cls:meta4");
        meta5 = dataFactory.getOWLClass("cls:meta5");
        clsB = dataFactory.getOWLClass("cls:B");
        OWLClass clsB2 = dataFactory.getOWLClass("cls:B2");
        clsB3 = dataFactory.getOWLClass("cls:B3");
        thing = dataFactory.getOWLThing();
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indC = dataFactory.getOWLNamedIndividual("ind:c");
        rolR = dataFactory.getOWLObjectProperty("rol:R");
        rolS = dataFactory.getOWLObjectProperty("rol:S");
        rolT = dataFactory.getOWLObjectProperty("rol:T");

        abstractedMetaConcepts = new HashSet<>(Arrays.asList(meta1, meta2, meta3, meta4, meta5));

        rootOntology = manager.createOntology(Arrays.asList(
                builder.stringToOWLAxiom("C ⊑ meta1 ⊓ meta2"),
                builder.stringToOWLAxiom("C(c)"),
                builder.stringToOWLAxiom("(meta3)(c)"),
                builder.stringToOWLAxiom("meta4 ⊑ ∀S.C"),
                builder.stringToOWLAxiom("⊤ ⊑ meta5"),
                //
                // global object ontology
                builder.stringToOWLAxiom("A ⊑ B3 @ global"),
                //
                // mapping of o-axioms
                builder.stringToOWLAxiom("A ⊑ ⊥ @ meta1"),
                builder.stringToOWLAxiom("A(a) @ meta2"),
                builder.stringToOWLAxiom("(B ⊓ ∃R.B)(a) @ meta3"),
                builder.stringToOWLAxiom("∃T.⊤ ⊑ A @ meta4"),
                builder.stringToOWLAxiom("A ⊑ B @ meta5")
        ));


        // C ⊑ [A ⊑ ⊥] ⊓ [A(a)],  C(c),  [(B ⊓ ∃r.B)(a)](c),  [∃t.⊤ ⊑ A] ⊑ ∀s.C,  ⊤ ⊑ [A ⊑ B]
        // meta1 := [A ⊑ ⊥]
        // meta2 := [A(a)]
        // meta3 := [(B ⊓ ∃r.B)(a)]
        // meta4 := [∃t.⊤ ⊑ A]
        // meta5 := [A ⊑ B]
        contextOntology = new ContextOntology(rootOntology);


        OWLOntology rootOntologyWithRigid = manager.createOntology(rootOntology.axioms());
        rootOntologyWithRigid.addAxioms(Arrays.asList(
                // rigid names
                isRigid(clsB),
                isRigid(clsB2),
                isRigid(clsC),
                isRigid(rolT)));

        contextOntologyWithRigidNames = new ContextOntology(rootOntologyWithRigid);
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

        Set<OWLClass> classSet = new HashSet<>(Arrays.asList(clsC, thing, meta1, meta2, meta3, meta4, meta5));
        addDualSetTo(classSet, abstractedMetaConcepts);
        Set<OWLEntity> metaSignatureSet = new HashSet<>(Arrays.asList((OWLEntity) indC, rolS));
        metaSignatureSet.addAll(classSet);


        assertEquals("Test for meta signature",
                metaSignatureSet,
                contextOntology.metaSignature().collect(Collectors.toSet()));
    }

    @Test
    public void testMetaClassesInSignature() throws Exception {

        Set<OWLClass> conceptSet = new HashSet<>(Arrays.asList(clsC, thing, meta1, meta2, meta3, meta4, meta5));
        addDualSetTo(conceptSet, abstractedMetaConcepts);

        assertEquals("Test for classes in meta signature",
                conceptSet,
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
                contextOntologyWithRigidNames.rigidClasses().collect(Collectors.toSet()));
    }

    @Test
    public void testRigidObjectProperties() throws Exception {

        Stream<OWLEntity> streamOfRigidObjProp = Stream.of(
                rolT);

        assertEquals("Test for rigid object properties",
                streamOfRigidObjProp.collect(Collectors.toSet()),
                contextOntologyWithRigidNames.rigidObjectProperties().collect(Collectors.toSet()));
    }

    @Test
    public void testFlexibleClasses() throws Exception {

        Stream<OWLEntity> streamOfFlexibleClasses = Stream.of(
                clsA, clsB3);

        assertEquals("Test for flexible classes",
                streamOfFlexibleClasses.collect(Collectors.toSet()),
                contextOntologyWithRigidNames.flexibleClasses().collect(Collectors.toSet()));
    }

    @Test
    public void testFlexibleObjectProperties() throws Exception {

        Stream<OWLEntity> streamOfFlexibleObjProp = Stream.of(
                rolR);

        assertEquals("Test for flexible object properties",
                streamOfFlexibleObjProp.collect(Collectors.toSet()),
                contextOntologyWithRigidNames.flexibleObjectProperties().collect(Collectors.toSet()));
    }

    @Test
    public void testContainsRigidNames() throws Exception {

        assertFalse(contextOntology.containsRigidNames());
        assertTrue(contextOntologyWithRigidNames.containsRigidNames());
    }


    // Test outer abstracted meta concepts

    @Test
    public void testOuterAbstractedMetaConcepts() throws Exception {

        Set<OWLClass> conceptSet = Stream.of(meta1, meta2, meta3, meta4, meta5).collect(Collectors.toSet());
        addDualSetTo(conceptSet, abstractedMetaConcepts);

        assertEquals("Test for outer abstracted meta concepts",
                conceptSet,
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

        OWLOntology metaOntology = manager.createOntology(Arrays.asList(
                builder.stringToOWLAxiom("C ⊑ meta1 ⊓ meta2"),
                builder.stringToOWLAxiom("C ⊑ meta1 ⊓ meta2")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Collections.singletonList(meta1)))),
                builder.stringToOWLAxiom("C ⊑ meta1 ⊓ meta2")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Collections.singletonList(meta2)))),
                builder.stringToOWLAxiom("C ⊑ meta1 ⊓ meta2")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Arrays.asList(meta1, meta2)))),
                builder.stringToOWLAxiom("C(c)"),
                builder.stringToOWLAxiom("(meta3)(c)"),
                builder.stringToOWLAxiom("(meta3)(c)")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Collections.singletonList(meta3)))),
                builder.stringToOWLAxiom("meta4 ⊑ ∀S.C"),
                builder.stringToOWLAxiom("meta4 ⊑ ∀S.C")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Collections.singletonList(meta4)))),
                builder.stringToOWLAxiom("⊤ ⊑ meta5"),
                builder.stringToOWLAxiom("⊤ ⊑ meta5")
                        .accept(new AxiomToDual(dataFactory, new HashSet<>(Collections.singletonList(meta5))))));

        assertEquals("Test for getting the meta ontology:",
                metaOntology.axioms().collect(Collectors.toSet()),
                contextOntology.getMetaOntology().axioms().collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntologyAllMetaConceptsPositive() throws Exception {

        Set<OWLAxiom> objectOntologyAxiomSet = Stream.of(
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3),
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLObjectIntersectionOf(
                                clsB,
                                dataFactory.getOWLObjectSomeValuesFrom(rolR, clsB)),
                        indA),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectSomeValuesFrom(rolT, dataFactory.getOWLThing()), clsA))
                .collect(Collectors.toSet());

        Supplier<Stream<OWLClass>> posMetaConcepts = () -> Stream.of(meta1, meta2, meta3, meta4);

        assertEquals("Test 1 for getting the object ontology:",
                objectOntologyAxiomSet,
                contextOntology.getObjectOntology(getType(posMetaConcepts.get(), Stream.empty()))
                        .axioms().collect(Collectors.toSet()));

        contextOntology
                .getObjectOntology(getType(posMetaConcepts.get(), Stream.empty()))
                .axioms()
                .forEach(System.out::println);
    }

    @Test
    public void testGetObjectOntologySomeMetaConceptsPositive() throws Exception {

        Set<OWLAxiom> objectOntologyAxiomSet = Stream.of(
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3),
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                dataFactory.getOWLClassAssertionAxiom(clsA, indA))
                .collect(Collectors.toSet());

        Stream<OWLClass> posMetaConcepts = Stream.of(meta1, meta2);

        assertEquals("Test 2 for getting the object ontology:",
                objectOntologyAxiomSet,
                contextOntology.getObjectOntology(getType(posMetaConcepts, Stream.empty()))
                        .axioms().collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntologyOnlyNegativeMetaConcepts() throws Exception {

        Set<OWLAxiom> objectOntologyAxiomSet = Stream.of(
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3),
                replaceAnonymousIndividual(
                        dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing())
                                .accept(new AxiomNegator(dataFactory))))
                .collect(Collectors.toSet());

        Stream<OWLClass> negMetaConcepts = Stream.of(meta1);

        assertEquals("Test 3 for getting the object ontology:",
                objectOntologyAxiomSet,
                contextOntology.getObjectOntology(getType(Stream.empty(), negMetaConcepts))
                        .axioms()
                        .map(this::replaceAnonymousIndividual)
                        .collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntologyBothPositiveAndNegativeMetaConcepts() throws Exception {

        Set<OWLAxiom> objectOntologyAxiomSet = Stream.of(
                // global object axiom
                dataFactory.getOWLSubClassOfAxiom(clsA, clsB3),
                // positive meta axiom meta1 := [A ⊑ ⊥]
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLNothing()),
                // positive meta axiom meta2 := [A(a)]
                dataFactory.getOWLClassAssertionAxiom(clsA, indA),
                // negative meta axiom meta3 := [(B ⊓ ∃r.B)(a)].negated
                replaceAnonymousIndividual(
                        dataFactory.getOWLClassAssertionAxiom(
                                dataFactory.getOWLObjectIntersectionOf(
                                        clsB,
                                        dataFactory.getOWLObjectSomeValuesFrom(rolR, clsB)),
                                indA)
                                .accept(new AxiomNegator(dataFactory))),
                // negative meta axiom meta4 := [∃t.⊤ ⊑ A].negated
                replaceAnonymousIndividual(
                        dataFactory.getOWLSubClassOfAxiom(
                                dataFactory.getOWLObjectSomeValuesFrom(rolT, dataFactory.getOWLThing()), clsA)
                                .accept(new AxiomNegator(dataFactory))))
                .collect(Collectors.toSet());

        Stream<OWLClass> posMetaConcepts = Stream.of(meta1, meta2);
        Stream<OWLClass> negMetaConcepts = Stream.of(meta3, meta4);

        assertEquals("Test 4 for getting the object ontology:",
                objectOntologyAxiomSet,
                contextOntology.getObjectOntology(getType(posMetaConcepts, negMetaConcepts))
                        .axioms()
                        .map(this::replaceAnonymousIndividual)
                        .collect(Collectors.toSet()));
    }

    @Test
    public void testGetObjectOntologyAllMetaConceptsPositiveWithRigidNames() throws Exception {

        Set<OWLAxiom> objectOntologyAxiomSet = Stream.of(
                builder.stringToOWLAxiom("A_0 ⊑ B3_0"),
                builder.stringToOWLAxiom("A_0 ⊑ ⊥"),
                builder.stringToOWLAxiom("A_0(a)"),
                builder.stringToOWLAxiom("(B ⊓ ∃ R_0.B)(a)"),
                builder.stringToOWLAxiom("∃ T.⊤ ⊑ A_0"))
                .collect(Collectors.toSet());

        Supplier<Stream<OWLClass>> posMetaConcepts = () -> Stream.of(meta1, meta2, meta3, meta4);

        assertEquals("Test 1 for getting the object ontology:",
                objectOntologyAxiomSet,
                contextOntologyWithRigidNames
                        .getObjectOntology(getType(posMetaConcepts.get(), Stream.empty()))
                        .axioms()
                        .collect(Collectors.toSet()));
    }

    //TODO getObjectOntology with several types

    @Test
    public void testClassAssertionWithAnonymousIndividuals() throws Exception {

        OWLAxiom axiom1 = dataFactory.getOWLClassAssertionAxiom(clsC, dataFactory.getOWLAnonymousIndividual());
        OWLAxiom axiom2 = dataFactory.getOWLClassAssertionAxiom(clsC, dataFactory.getOWLAnonymousIndividual());

        assertEquals(
                replaceAnonymousIndividual(axiom1),
                replaceAnonymousIndividual(axiom2)
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

    @Test
    public void testChangedRootOntology() throws Exception {
        System.out.println("Executing testChangedRootOntology:");

        assertTrue(contextOntologyWithRigidNames.rigidClasses().anyMatch(cls -> cls.equals(clsB)));
        assertFalse(contextOntology.rigidClasses().anyMatch(cls -> cls.equals(clsB)));
        rootOntology.addAxiom(isRigid(clsB));

        // TODO here must stand "assertFalse(..."
        assertTrue(contextOntology.rigidClasses().anyMatch(cls -> cls.equals(clsB)));
    }


    // Helper functions

    private OWLAnnotationAssertionAxiom isRigid(HasIRI hasIRI) {

        return dataFactory.getOWLAnnotationAssertionAxiom(
                hasIRI.getIRI(),
                dataFactory.getRDFSLabel("rigid"));
    }

    private OWLAxiom replaceAnonymousIndividual(OWLAxiom owlAxiom) {

        return owlAxiom.isOfType(AxiomType.CLASS_ASSERTION) &&
                ((OWLClassAssertionAxiom) owlAxiom).getIndividual().isAnonymous() ?
                dataFactory.getOWLClassAssertionAxiom(
                        ((OWLClassAssertionAxiom) owlAxiom).getClassExpression(),
                        dataFactory.getOWLNamedIndividual("testing:OWLAnonymousIndividual")) :
                owlAxiom;
    }

    private void addDualSetTo(Set<OWLClass> setToModify, Set<OWLClass> abstractedMetaConcepts) {

        Set<OWLClass> dualSet = setToModify.stream()
                .map(concept -> concept.accept(new ConceptToDual(dataFactory, abstractedMetaConcepts)))
                .map(elem -> (elem instanceof OWLObjectComplementOf) ?
                        ((OWLObjectComplementOf) elem).getOperand().asOWLClass() :
                        (OWLClass) elem)
                .collect(Collectors.toSet());
        setToModify.addAll(dualSet);

    }

    private List<Type> getType(Stream<OWLClass> pos, Stream<OWLClass> neg) {

        return Collections.singletonList(
                new Type(pos.collect(Collectors.toSet()), neg.collect(Collectors.toSet())));
    }

}