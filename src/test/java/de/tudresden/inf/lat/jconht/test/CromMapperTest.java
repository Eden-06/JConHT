package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.AxiomBuilder;
import de.tudresden.inf.lat.jconht.model.Configuration;
import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This is a test class for testing the correctness of the ontology generator in FRaMED.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class CromMapperTest {

    private final boolean nominalMapping = true;

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private PrefixManager rosiPrefix;
    private Configuration confWithDebug;
    private Configuration confWithoutDebug;
    private AxiomBuilder builder;

    private OWLOntology rawOntology;

    private int numberOfAnonymousMetaConcepts;
    private int numberOfAnonymousIndividuals;

    private OWLClass naturalTypes;
    private OWLClass roleTypes;
    private OWLClass roleGroups;
    private OWLClass compartmentTypes;
    private OWLClass nothing;

    private OWLObjectProperty plays;

    private long startTime;

    private String neededTime() {
        double time = (System.nanoTime() - startTime) / 1000000000.;
        return String.format("%.3f", time);
    }


    @Before
    public void setUp() throws Exception {
        System.out.println("Setting up.");
        startTime = System.nanoTime();
        //Thread.sleep(5000); // Needed for VisualVM Profiler
        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        rosiPrefix = new DefaultPrefixManager("http://www.rosi-project.org/ontologies#");
        builder = new AxiomBuilder(dataFactory,
                "http://www.rosi-project.org/ontologies#",
                "http://www.rosi-project.org/ontologies#",
                "http://www.rosi-project.org/ontologies#");

        //String inputDir = new File("input").getAbsolutePath();
        File cromMapperTestOntologyFile = new File("input/CROMMapperTest/MapperTest.owl");
        //TODO again the question how to correctly load an ontology
        rawOntology = manager.loadOntology(IRI.create(cromMapperTestOntologyFile));
        confWithDebug = new Configuration(true, 2, false, false);
        confWithoutDebug = new Configuration(true, 0, false, false);

        numberOfAnonymousMetaConcepts = 0;
        numberOfAnonymousIndividuals = 0;

        naturalTypes = dataFactory.getOWLClass("NaturalType", rosiPrefix);
        roleTypes = dataFactory.getOWLClass("RoleType", rosiPrefix);
        roleGroups = dataFactory.getOWLClass("RoleGroup", rosiPrefix);
        compartmentTypes = dataFactory.getOWLClass("CompartmentType", rosiPrefix);
        nothing = dataFactory.getOWLNothing();

        plays = dataFactory.getOWLObjectProperty("plays", rosiPrefix);

        System.out.println(neededTime() + "s");
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();

        System.out.println(neededTime() + "s");
    }


    /*
     General tests that must pass for every generated ontology.
     */
    @Test
    public void test01_RawOntologyIsConsistent() throws Exception {
        System.out.println("Executing testRawOntologyIsConsistent: ");

        ContextOntology contextOntology = new ContextOntology(rawOntology, confWithoutDebug);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        System.out.println(contextOntology.getStatistics());

        assertTrue(reasoner.isConsistent());
    }

    @Test
    public void test02_NaturalIsObject() throws Exception {
        System.out.println("Executing testNaturalIsObject: ");

        assertTrue(isInconsistent(Stream.of(
                builder.stringToOWLAxiom("NaturalType(x)"))));
    }

    @Test
    public void test03_CompartmentIsMeta() throws Exception {
        System.out.println("Executing testCompartmentIsMeta: ");

        assertTrue(isInconsistent(Stream.of(
                builder.stringToOWLAxiom("CompartmentType(c) @ global")
        )));

    }

    @Test
    public void test04_NaturalsAndRolesAreDisjoint() throws Exception {
        System.out.println("Executing testNaturalsAndRolesAreDisjoint: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(naturalTypes, individual),
                getGlobalObjectTypeAssertion(roleTypes, individual)));
    }

    @Test
    public void test05_PlaysIsObject() throws Exception {
        System.out.println("Executing testPlaysIsObject: ");

        assertTrue(isInconsistent(Stream.of(
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        plays,
                        dataFactory.getOWLAnonymousIndividual(),
                        dataFactory.getOWLAnonymousIndividual()))));
    }

    @Test
    public void test06_EveryRoleIsPlayed() throws Exception {
        System.out.println("Executing testEveryRoleIsPlayed: ");

        if (nominalMapping) {

        } else {

            OWLIndividual individual = createNewAnonymousIndividual();
            // TODO warum klappt das nicht mit dataFactory.getOWLAnonymousIndividual() ?

            assertTrue(isInconsistent(//confWithDebug,
                    getGlobalObjectTypeAssertion(roleTypes, individual),
                    getGlobalObjectTypeAssertion(
                            dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectSomeValuesFrom(
                                    dataFactory.getOWLObjectInverseOf(plays), dataFactory.getOWLThing())),
                            individual)
            ));
        }
    }

    @Test
    public void test07_EveryRoleGroupIsPlayed() throws Exception {
        System.out.println("Executing testEveryRoleGroupIsPlayed: ");

        if (!nominalMapping) {
            OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

            assertTrue(isInconsistent(
                    getGlobalObjectTypeAssertion(roleGroups, individual),
                    getGlobalObjectTypeAssertion(
                            dataFactory.getOWLObjectMaxCardinality(0, dataFactory.getOWLObjectInverseOf(plays)),
                            individual)));
        }
    }

    @Test
    public void test08_NoRolePlaysAnything() throws Exception {
        System.out.println("Executing testNoRolePlaysAnything: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleTypes, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMinCardinality(1, plays),
                        individual)));
    }

    @Test
    public void test09_OnlyRolesOrRoleGroupsCanBePlayed() throws Exception {
        System.out.println("Executing testOnlyRolesOrRoleGroupsCanBePlayed: ");

        assertTrue(isInconsistent(Stream.of(
                builder.stringToOWLAxiom("(∃ plays.¬(RoleType ⊔ RoleGroup))(x) @ global"))));
    }

    @Test
    public void test10_OccurrenceCounterIsNeitherNaturalNorRole() throws Exception {
        System.out.println("Executing testOccurrenceCounterIsNeitherNaturalNorRole: ");

        if (!nominalMapping) {

            OWLIndividual occurrenceCounter = dataFactory.getOWLNamedIndividual("occurrenceCounter", rosiPrefix);
            OWLClassExpression natTypeOrRoleTypeOrRoleGroup = dataFactory.getOWLObjectUnionOf(
                    naturalTypes, roleTypes, roleGroups, compartmentTypes);

            assertTrue(isInconsistent(
                    getGlobalObjectTypeAssertion(natTypeOrRoleTypeOrRoleGroup, occurrenceCounter)));
        }
    }






    /*
     Tests that must be passed for the CROMMapperTest ontology.
     */

    // Compartments

    @Test
    public void test11_CompartmentDisjointnessPre() throws Exception {
        System.out.println("Executing testCompartmentDisjointnessPre: ");

        OWLClass ct1 = dataFactory.getOWLClass("CTDisjoint1", rosiPrefix);
        OWLClass ct2 = dataFactory.getOWLClass("CTDisjoint2", rosiPrefix);
        OWLIndividual compartment1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment2 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct1, compartment1),
                getMetaTypeAssertion(ct2, compartment2)
        ));
    }

    @Test
    public void test12_CompartmentDisjointness() throws Exception {
        System.out.println("Executing testCompartmentDisjointness: ");

        OWLClass ct1 = dataFactory.getOWLClass("CTDisjoint1", rosiPrefix);
        OWLClass ct2 = dataFactory.getOWLClass("CTDisjoint2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct1, compartment),
                getMetaTypeAssertion(ct2, compartment)
        ));
    }

    @Test
    public void test13_CompartmentIsNotEmpty() throws Exception {
        System.out.println("Executing testCompartmentIsNotEmpty: ");

        OWLClass ct1 = dataFactory.getOWLClass("EmptyCompartment", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct1, compartment)
        ));
    }


    // Natural type inheritance

    @Test
    public void test14_NTInheritance1To4CanBeInstantiated() throws Exception {
        System.out.println("Executing testNTInheritance1To4CanBeInstantiated: ");

        OWLClass nt1 = dataFactory.getOWLClass("NTInheritance1", rosiPrefix);
        OWLClass nt2 = dataFactory.getOWLClass("NTInheritance2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NTInheritance3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NTInheritance4", rosiPrefix);

        assertFalse("NaturalTypes 1–4 can be instantiated.",
                isInconsistent(
                        getGlobalObjectTypeAssertion(nt1, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt2, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt3, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt4, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void test15_NTInheritanceNT2AndNotSubType() throws Exception {
        System.out.println("Executing testNTInheritanceNT2AndNotSubType: ");

        OWLClass nt2 = dataFactory.getOWLClass("NTInheritance2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NTInheritance3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NTInheritance4", rosiPrefix);

        assertFalse("A natural can be in NT1 without being in its subtype.",
                isInconsistent(getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectIntersectionOf(
                                nt2,
                                dataFactory.getOWLObjectComplementOf(nt3),
                                dataFactory.getOWLObjectComplementOf(nt4)),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void test16_NTInheritanceNoNT5() throws Exception {
        System.out.println("Executing testNTInheritanceNoNT5: ");

        OWLClass nt5 = dataFactory.getOWLClass("NTInheritance5", rosiPrefix);

        assertTrue("NaturalType5 has no instances due to multiple inheritance.",
                isInconsistent(getGlobalObjectTypeAssertion(nt5, dataFactory.getOWLAnonymousIndividual())));

    }

    @Test
    public void test17_NTInheritanceNatMustHaveSomeType() throws Exception {
        System.out.println("Executing testNTInheritanceNatMustHaveSomeType: ");

        OWLClassExpression allTopLevelNT = dataFactory.getOWLObjectUnionOf(
                dataFactory.getOWLClass("NTInheritance1", rosiPrefix),
                dataFactory.getOWLClass("NTInheritance2", rosiPrefix),
                dataFactory.getOWLClass("NTFiller1", rosiPrefix),
                dataFactory.getOWLClass("NTFiller2", rosiPrefix),
                dataFactory.getOWLClass("BottomNT", rosiPrefix),
                dataFactory.getOWLClass("DefaultNT", rosiPrefix)
        );

        assertTrue("A natural must have a type",
                isInconsistent(getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectIntersectionOf(
                                naturalTypes,
                                dataFactory.getOWLObjectComplementOf(allTopLevelNT)),
                        dataFactory.getOWLAnonymousIndividual())));
    }


    // Fills relation

    @Test
    public void test18_Fills1() throws Exception {
        System.out.println("Executing testFills1: ");

        if (nominalMapping) {
            assertFalse(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("NTFiller1(n1)      @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RTFills1) @ meta2"),
                    builder.stringToOWLAxiom("NTFiller2(n2)      @ meta3"),
                    builder.stringToOWLAxiom("plays(n2,RTFills1) @ meta4"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)"),
                    builder.stringToOWLAxiom("(meta3 ⊓ meta4)(c)")
            )));
        } else {
            assertFalse(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("NTFiller1(n1) @ meta1"),
                    builder.stringToOWLAxiom("RTFills1(r1)  @ meta2"),
                    builder.stringToOWLAxiom("plays(n1,r1)  @ meta3"),
                    builder.stringToOWLAxiom("NTFiller2(n2) @ meta4"),
                    builder.stringToOWLAxiom("RTFills1(r2)  @ meta5"),
                    builder.stringToOWLAxiom("plays(n2,r2)  @ meta6"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)"),
                    builder.stringToOWLAxiom("(meta4 ⊓ meta5 ⊓ meta6)(c)")
            )));
        }
    }

    @Test
    public void test19_Fills2() throws Exception {
        System.out.println("Executing testFills2: ");

        if (nominalMapping) {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest2(c)"),
                    builder.stringToOWLAxiom("NTFiller1(n1)      @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RTFills2) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest2(c)"),
                    builder.stringToOWLAxiom("NTFiller1(n1) @ meta1"),
                    builder.stringToOWLAxiom("RTFills2(r1)  @ meta2"),
                    builder.stringToOWLAxiom("plays(n1,r1)  @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        }

    }

    @Test
    public void test20_Fills3() throws Exception {
        System.out.println("Executing testFills3: ");

        if (nominalMapping) {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("DefaultNT(n1) @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RTFills1)  @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("DefaultNT(n1) @ meta1"),
                    builder.stringToOWLAxiom("RTFills1(r1)  @ meta2"),
                    builder.stringToOWLAxiom("plays(n1,r1)  @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        }

    }

    @Test
    public void test21_Fills4() throws Exception {
        System.out.println("Executing testFills4: ");

        if (nominalMapping) {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("plays(n,RTRiehle1)  @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("FillsTest1(c)"),
                    builder.stringToOWLAxiom("RTRiehle1(r)  @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        }
    }


    // Basic properties about CROM Roles.

    @Test
    public void test22_CantPlayRoleTypeTwicePre() throws Exception {
        System.out.println("Executing testCantPlayRoleTypeTwicePre: ");

        if (!nominalMapping) {
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCantBePlayedTwice", rosiPrefix);
            OWLClass naturalType = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
            OWLClass compartmentType = dataFactory.getOWLClass("RolesCantBePlayedTwice", rosiPrefix);

            assertFalse("One role should be playable!", isInconsistent(
                    getGlobalObjectTypeAssertion(naturalType, natural),
                    getGlobalObjectTypeAssertion(roleType1, role1),
                    getMetaTypeAssertion(compartmentType, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)));
        }
    }

    @Test
    public void test23_CantPlayRoleTypeTwice() throws Exception {
        System.out.println("Executing testCantPlayRoleTypeTwice: ");

        if (!nominalMapping) {
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCantBePlayedTwice", rosiPrefix);
            OWLClass naturalType = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
            OWLClass compartmentType = dataFactory.getOWLClass("RolesCantBePlayedTwice", rosiPrefix);

            assertTrue("Two roles should not be playable!", isInconsistent(
                    getGlobalObjectTypeAssertion(naturalType, natural),
                    getGlobalObjectTypeAssertion(roleType1, role1),
                    getMetaTypeAssertion(compartmentType, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                    getGlobalObjectTypeAssertion(roleType1, role2),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment),
                    getObjectDifferentIndividualAssertion(role1, role2)));
        }
    }

    @Test
    public void test24_RoleTypesAreDisjointPre() throws Exception {
        System.out.println("Executing testRoleTypesAreDisjointPre: ");

        if (nominalMapping) {

        } else {
            OWLClass roleType1 = dataFactory.getOWLClass("RTDisjoint1", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTDisjoint2", rosiPrefix);
            OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual individual2 = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertFalse(isInconsistent(
                    getLocalObjectTypeAssertion(roleType1, individual, compartment),
                    getLocalObjectTypeAssertion(roleType2, individual2, compartment)));
        }
    }

    @Test
    public void test25_RoleTypesAreDisjoint() throws Exception {
        System.out.println("Executing testRoleTypesAreDisjoint: ");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RTDisjoint1 = RTDisjoint2 @ global")
            )));
        } else {
            OWLClass roleType1 = dataFactory.getOWLClass("RTDisjoint1", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTDisjoint2", rosiPrefix);
            OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertTrue(isInconsistent(
                    getLocalObjectTypeAssertion(roleType1, individual, compartment),
                    getLocalObjectTypeAssertion(roleType2, individual, compartment)));
        }
    }


    // Tests for relationship type domain and range

    @Test
    public void test26_RelationshipTypeDomainPre() throws Exception {
        System.out.println("Executing testRelationshipTypeDomainPre: ");

        if (nominalMapping) {
            assertFalse(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("RSTDomainRange1(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTRSTDomain)            @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTRSTRange)             @ meta2"),
                    builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(n1,n2) @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        } else {
            assertFalse(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("RSTDomainRange1(c)"),
                    builder.stringToOWLAxiom("RTRSTDomain(r1)                   @ meta1"),
                    builder.stringToOWLAxiom("RTRSTRange(r2)                    @ meta2"),
                    builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(r1,r2)  @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        }
    }

    @Test
    public void test27_RelationshipTypeWrongDomain() throws Exception {
        System.out.println("Executing testRelationshipTypeWrongDomain:");

        if (nominalMapping) {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("RSTDomainRange1(c)"),
                    builder.stringToOWLAxiom("∀plays.¬{RTRSTDomain}(n1)        @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTRSTRange)             @ meta2"),
                    builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(n1,n2) @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        } else {
            assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                    builder.stringToOWLAxiom("RSTDomainRange1(c)"),
                    builder.stringToOWLAxiom("RTRSTRange(r1)                    @ meta1"),
                    builder.stringToOWLAxiom("RTRSTRange(r2)                    @ meta2"),
                    builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(r1,r2)  @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        }
    }

    @Test
    public void test28_RSTInOtherCTPre() throws Exception {
        System.out.println("Executing testRSTInOtherCTPre:");

        assertFalse(isInconsistent(confWithoutDebug, Stream.of(
                builder.stringToOWLAxiom("RSTDomainRange1(c)"),
                builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(a,b) @ meta1"),
                builder.stringToOWLAxiom("meta1(c)")
        )));
    }

    @Test
    public void test29_RSTInOtherCT() throws Exception {
        System.out.println("Executing testRSTInOtherCT:");

        assertTrue(isInconsistent(confWithoutDebug, Stream.of(
                builder.stringToOWLAxiom("ComplexRiehle(c)"),
                builder.stringToOWLAxiom("RSTDomainRange1.RSTDomRan(a,b) @ meta1"),
                builder.stringToOWLAxiom("meta1(c)")
        )));
    }

    // Tests for the occurrence constraints (for role types)

    @Test
    public void test30_OccurrenceMaxConstraints1Pre() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints1Pre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTOccurrence1) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLClass rt = dataFactory.getOWLClass("RTOccurrence1", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(rt, role1, compartment)
            ));
        }
    }

    @Test
    public void test31_OccurrenceMaxConstraints1() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints1:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTOccurrence1) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTOccurrence1) @ meta2"),
                    builder.stringToOWLAxiom("n1≠n2 @ global"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLClass rt = dataFactory.getOWLClass("RTOccurrence1", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(rt, role1, compartment),
                    getLocalObjectTypeAssertion(rt, role2, compartment),
                    getObjectDifferentIndividualAssertion(role1, role2)
            ));
        }
    }

    @Test
    public void test32_OccurrenceMaxConstraints2Pre() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints2Pre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTOccurrence2) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTOccurrence2) @ meta2"),
                    builder.stringToOWLAxiom("plays(n3,RTOccurrence2) @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)"),
                    builder.stringToOWLAxiom("≠(n1,n2,n3) @ global")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLClass rt = dataFactory.getOWLClass("RTOccurrence2", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLIndividual role3 = createNewAnonymousIndividual();

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(rt, role1, compartment),
                    getLocalObjectTypeAssertion(rt, role2, compartment),
                    getLocalObjectTypeAssertion(rt, role3, compartment),
                    getObjectDifferentIndividualAssertion(role1, role2, role3)
            ));
        }
    }

    @Test
    public void test33_OccurrenceMaxConstraints2() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints2:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTOccurrence2) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTOccurrence2) @ meta2"),
                    builder.stringToOWLAxiom("plays(n3,RTOccurrence2) @ meta3"),
                    builder.stringToOWLAxiom("plays(n4,RTOccurrence2) @ meta4"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3 ⊓ meta4)(c)"),
                    builder.stringToOWLAxiom("≠(n1,n2,n3,n4) @ global")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLClass rt = dataFactory.getOWLClass("RTOccurrence2", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLIndividual role3 = createNewAnonymousIndividual();
            OWLIndividual role4 = createNewAnonymousIndividual();

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(rt, role1, compartment),
                    getLocalObjectTypeAssertion(rt, role2, compartment),
                    getLocalObjectTypeAssertion(rt, role3, compartment),
                    getLocalObjectTypeAssertion(rt, role4, compartment),
                    getObjectDifferentIndividualAssertion(role1, role2, role3, role4)
            ));
        }
    }

    @Test
    public void test34_OccurrenceMinConstraintsPre() throws Exception {
        System.out.println("Executing testOccurrenceMinConstraintsPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment)
            ));
        }
    }

    @Test
    public void test35_OccurrenceMinConstraints() throws Exception {
        System.out.println("Executing testOccurrenceMinConstraints:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest1(c)"),
                    builder.stringToOWLAxiom("(∃ plays.{RTOccurrence2}) ⊑ ⊥ @ global")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
            OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRTOccurrence2", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getObjectIsBottom(rtPlayer, compartment)
            ));
        }
    }

    @Test
    public void test36_OccurrenceConstraintsNeedFiller1() throws Exception {
        System.out.println("Executing testOccurrenceConstraintsNeedFiller1:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest2(c)"),
                    builder.stringToOWLAxiom("BottomNT ⊑ ⊥ @ global")
            )));
        } else {
            OWLClass compartmentType = dataFactory.getOWLClass("OccurrenceRTTest2", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertFalse(isInconsistent(//new Configuration(true, true ,false, true),
                    getBottomNTIsBottom(),
                    getMetaTypeAssertion(compartmentType, compartment)
            ));
        }
    }

    @Test
    public void test37_OccurrenceConstraintsNeedFiller2() throws Exception {
        System.out.println("Executing testOccurrenceConstraintsNeedFiller2:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRTTest3(c)"),
                    builder.stringToOWLAxiom("BottomNT ⊑ ⊥ @ global")
            )));
        } else {
            OWLClass compartmentType = dataFactory.getOWLClass("OccurrenceRTTest3", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertTrue(isInconsistent(
                    getBottomNTIsBottom(),
                    getMetaTypeAssertion(compartmentType, compartment)
            ));
        }
    }


    // Tests for the cardinality constraints of a relationship type.

    @Test
    public void test38_RelationshipTypeCardConstraints1MaxPre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardConstraints1MaxPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(confWithDebug,
                    Stream.of(
                            builder.stringToOWLAxiom("CardinalConstraints(c)"),
                            builder.stringToOWLAxiom("plays(n1,RTCard1) @ meta1"),
                            builder.stringToOWLAxiom("plays(n2,RTCard2) @ meta2"),
                            builder.stringToOWLAxiom("plays(n3,RTCard2) @ meta3"),
                            builder.stringToOWLAxiom("plays(n4,RTCard2) @ meta4"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n2) @ meta5"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n3) @ meta6"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n4) @ meta7"),
                            builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3 ⊓ meta4 ⊓ meta5)(c)"),
                            builder.stringToOWLAxiom("(meta6 ⊓ meta7)(c)"),
                            builder.stringToOWLAxiom("≠(n2,n3,n4) @ global")
                    )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLIndividual role3 = createNewAnonymousIndividual();
            OWLIndividual role4 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTCard2", rosiPrefix);
            OWLObjectProperty rst = dataFactory.getOWLObjectProperty("CardinalConstraints.RSTCard1", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getLocalObjectTypeAssertion(roleType2, role2, compartment),
                    getLocalObjectTypeAssertion(roleType2, role3, compartment),
                    getLocalObjectTypeAssertion(roleType2, role4, compartment),
                    getRelationshipAssertion(rst, role1, role2, compartment),
                    getRelationshipAssertion(rst, role1, role3, compartment),
                    getRelationshipAssertion(rst, role1, role4, compartment),
                    getObjectDifferentIndividualAssertion(role2, role3, role4)
            ));
        }
    }

    @Test
    public void test39_RelationshipTypeCardinalConstraints1Max() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints1Max:");

        if (nominalMapping) {
            assertTrue(isInconsistent(confWithDebug, Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard1) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTCard2) @ meta2"),
                    builder.stringToOWLAxiom("plays(n3,RTCard2) @ meta3"),
                    builder.stringToOWLAxiom("plays(n4,RTCard2) @ meta4"),
                    builder.stringToOWLAxiom("plays(n5,RTCard2) @ meta5"),
                    builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n2) @ meta6"),
                    builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n3) @ meta7"),
                    builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n4) @ meta8"),
                    builder.stringToOWLAxiom("CardinalConstraints.RSTCard1(n1,n5) @ meta9"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3 ⊓ meta4 ⊓ meta5)(c)"),
                    builder.stringToOWLAxiom("(meta6 ⊓ meta7 ⊓ meta8 ⊓ meta9)(c)"),
                    builder.stringToOWLAxiom("≠(n2,n3,n4,n5) @ global")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLIndividual role3 = createNewAnonymousIndividual();
            OWLIndividual role4 = createNewAnonymousIndividual();
            OWLIndividual role5 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTCard2", rosiPrefix);
            OWLObjectProperty rst = dataFactory.getOWLObjectProperty("CardinalConstraints.RSTCard1", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getLocalObjectTypeAssertion(roleType2, role2, compartment),
                    getLocalObjectTypeAssertion(roleType2, role3, compartment),
                    getLocalObjectTypeAssertion(roleType2, role4, compartment),
                    getLocalObjectTypeAssertion(roleType2, role5, compartment),
                    getRelationshipAssertion(rst, role1, role2, compartment),
                    getRelationshipAssertion(rst, role1, role3, compartment),
                    getRelationshipAssertion(rst, role1, role4, compartment),
                    getRelationshipAssertion(rst, role1, role5, compartment),
                    getObjectDifferentIndividualAssertion(role2, role3, role4, role5)
            ));
        }
    }

    @Test
    public void test40_RelationshipTypeCardConstraints1MinPre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardConstraints1MinPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard1) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment)
            ));
        }
    }


    @Test
    public void test41_RelationshipTypeCardinalConstraints1Min() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints1Min:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard1) @ meta1"),
                    builder.stringToOWLAxiom("∃plays.{RTCard2} ⊑ ⊥ @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTCard2", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getObjectIsBottom(rt2Player, compartment)
            ));
        }
    }

    @Test
    public void test42_RelationshipTypeCardinalConstraints2Pre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Pre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard3) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTCard4) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard4", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTCard3", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getLocalObjectTypeAssertion(roleType2, role2, compartment)
            ));
        }
    }

    @Test
    public void test43_RelationshipTypeCardinalConstraints2Max() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Max:");

        if (nominalMapping) {
            assertTrue(isInconsistent(//confWithDebug,
                    Stream.of(
                            builder.stringToOWLAxiom("CardinalConstraints(c)"),
                            builder.stringToOWLAxiom("plays(n1,RTCard4) @ meta1"),
                            builder.stringToOWLAxiom("plays(n2,RTCard3) @ meta2"),
                            builder.stringToOWLAxiom("plays(n3,RTCard3) @ meta3"),
                            builder.stringToOWLAxiom("plays(n4,RTCard3) @ meta4"),
                            builder.stringToOWLAxiom("plays(n5,RTCard3) @ meta5"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard2(n2,n1) @ meta6"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard2(n3,n1) @ meta7"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard2(n4,n1) @ meta8"),
                            builder.stringToOWLAxiom("CardinalConstraints.RSTCard2(n5,n1) @ meta9"),
                            builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3 ⊓ meta4 ⊓ meta5)(c)"),
                            builder.stringToOWLAxiom("(meta6 ⊓ meta7 ⊓ meta8 ⊓ meta9)(c)"),
                            builder.stringToOWLAxiom("≠(n2,n3,n4,n5) @ global"),
                            builder.stringToOWLAxiom("TEST1(n1) @ global"),
                            builder.stringToOWLAxiom("TEST2(n2) @ global"),
                            builder.stringToOWLAxiom("TEST3(n3) @ global"),
                            builder.stringToOWLAxiom("TEST4(n4) @ global"),
                            builder.stringToOWLAxiom("TEST5(n5) @ global")
                    )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLIndividual role3 = createNewAnonymousIndividual();
            OWLIndividual role4 = createNewAnonymousIndividual();
            OWLIndividual role5 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard4", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTCard3", rosiPrefix);
            OWLObjectProperty rst = dataFactory.getOWLObjectProperty("CardinalConstraints.RSTCard2", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getLocalObjectTypeAssertion(roleType2, role2, compartment),
                    getLocalObjectTypeAssertion(roleType2, role3, compartment),
                    getLocalObjectTypeAssertion(roleType2, role4, compartment),
                    getLocalObjectTypeAssertion(roleType2, role5, compartment),
                    getRelationshipAssertion(rst, role2, role1, compartment),
                    getRelationshipAssertion(rst, role3, role1, compartment),
                    getRelationshipAssertion(rst, role4, role1, compartment),
                    getRelationshipAssertion(rst, role5, role1, compartment),
                    getObjectDifferentIndividualAssertion(role2, role3, role4, role5)
            ));
        }
    }

    @Test
    public void test44_RelationshipTypeCardinalConstraints2Min() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Min:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard4) @ meta1"),
                    builder.stringToOWLAxiom("∃plays.{RTCard3} ⊑ ⊥ @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CTCardConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard4", rosiPrefix);
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTCard3", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getObjectIsBottom(rt2Player, compartment)
            ));
        }
    }

    @Test
    public void test45_RelationshipTypeCardinalConstraints3() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints3:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("CardinalConstraints(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTCard5) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTCard6) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
            OWLIndividual role1 = createNewAnonymousIndividual();
            OWLIndividual role2 = createNewAnonymousIndividual();
            OWLClass roleType1 = dataFactory.getOWLClass("RTCard5", rosiPrefix);
            OWLClass roleType2 = dataFactory.getOWLClass("RTCard6", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getLocalObjectTypeAssertion(roleType1, role1, compartment),
                    getLocalObjectTypeAssertion(roleType2, role2, compartment)
            ));
        }
    }


    // Tests for role groups


    @Test
    public void test46_ImplicationPre1() throws Exception {
        System.out.println("Executing testImplicationPre1:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleImplication(c)"),
                    builder.stringToOWLAxiom("∃plays.{RTImplication2} ⊑ ⊥ @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleImplication", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTImplication2", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getObjectIsBottom(rt2Player, compartment)
            ));
        }
    }

    @Test
    public void test47_ImplicationPre2() throws Exception {
        System.out.println("Executing testImplicationPre2:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleImplication(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTImplication1) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleImplication", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTImplication1", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
            ));
        }
    }

    @Test
    public void test48_Implication() throws Exception {
        System.out.println("Executing testImplication:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleImplication(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTImplication1) @ meta1"),
                    builder.stringToOWLAxiom("∃plays.{RTImplication2} ⊑ ⊥ @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleImplication", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTImplication1", rosiPrefix);
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTImplication2", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                    getObjectIsBottom(rt2Player, compartment)
            ));
        }
    }

    @Test
    public void test49_ProhibitionPre() throws Exception {
        System.out.println("Executing testProhibitionPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleProhibition(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTProhibition1) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTProhibition2) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleProhibition", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);
            OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);

            assertFalse(isInconsistent(//confWithDebug,
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural1, rt1, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural2, rt2, compartment)
            ));
        }
    }

    @Test
    public void test50_Prohibition() throws Exception {
        System.out.println("Executing testProhibition:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleProhibition(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTProhibition1) @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RTProhibition2) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleProhibition", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);
            OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);

            assertTrue(isInconsistent(confWithDebug,
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt2, compartment)
            ));
        }
    }

    @Test
    public void test51_EquivalencePre() throws Exception {
        System.out.println("Executing testEquivalencePre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleEquivalence(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTEquivalence1) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RoleEquivalence", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTEquivalence1", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
            ));
        }
    }

    @Test
    public void test52_Equivalence1() throws Exception {
        System.out.println("Executing testEquivalence1:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleEquivalence(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTEquivalence1) @ meta1"),
                    builder.stringToOWLAxiom("∃plays.{RTEquivalence2} ⊑ ⊥ @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("CTRoleEquivalence", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTEquivalence1", rosiPrefix);
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTEquivalence2", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                    getObjectIsBottom(rt2Player, compartment)
            ));
        }
    }

    @Test
    public void test53_Equivalence2() throws Exception {
        System.out.println("Executing testEquivalence2:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RoleEquivalence(c)"),
                    builder.stringToOWLAxiom("∃plays.{RTEquivalence1} ⊑ ⊥ @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RTEquivalence2) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("CTRoleEquivalence", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt2 = dataFactory.getOWLClass("RTEquivalence2", rosiPrefix);
            OWLClass rt1Player = dataFactory.getOWLClass("PlaysRTEquivalence1", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt2, compartment),
                    getObjectIsBottom(rt1Player, compartment)
            ));
        }
    }

    @Test
    public void test54_RiehleConstraintsPre() throws Exception {
        System.out.println("Executing testRiehleConstraintsPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("ComplexRiehle(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTRiehle2) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RTRiehle3) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("ComplexRiehle", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt2 = dataFactory.getOWLClass("RTRiehle2", rosiPrefix);
            OWLClass rt3 = dataFactory.getOWLClass("RTRiehle3", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural1, rt2, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural2, rt3, compartment)
            ));
        }
    }

    @Test
    public void test55_RoleConstraints() throws Exception {
        System.out.println("Executing testRoleConstraints:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("ComplexRiehle(c)"),
                    builder.stringToOWLAxiom("plays(n1,RTRiehle1) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("ComplexRiehle", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RTRiehle1", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
            ));
        }
    }

    @Test
    public void test56_RoleGroupMinMaxPre() throws Exception {
        System.out.println("Executing testRoleGroupMinMaxPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RGsMinMaxTest(c)"),
                    builder.stringToOWLAxiom("plays(n1,RT_A) @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RGsMinMaxTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RT_A", rosiPrefix);

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
            ));
        }
    }

    @Test
    public void test57_RoleGroupMin() throws Exception {
        System.out.println("Executing testRoleGroupMin:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RGsMinMaxTest(c)"),
                    builder.stringToOWLAxiom("plays(n1,RT_A) @ meta1"),
                    builder.stringToOWLAxiom("∃plays.{RT_B} ⊑ ⊥ @ meta2"),
                    builder.stringToOWLAxiom("∃plays.{RT_C} ⊑ ⊥ @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RGsMinMaxTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RT_A", rosiPrefix);
            OWLClass rt2Player = dataFactory.getOWLClass("PlaysRT_B", rosiPrefix);
            OWLClass rt3Player = dataFactory.getOWLClass("PlaysRT_C", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                    getObjectIsBottom(rt2Player, compartment),
                    getObjectIsBottom(rt3Player, compartment)
            ));
        }
    }

    @Test
    public void test58_RoleGroupMax() throws Exception {
        System.out.println("Executing testRoleGroupMax:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("RGsMinMaxTest(c)"),
                    builder.stringToOWLAxiom("plays(n1,RT_A) @ meta1"),
                    builder.stringToOWLAxiom("plays(n1,RT_B) @ meta2"),
                    builder.stringToOWLAxiom("plays(n1,RT_C) @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("RGsMinMaxTetst", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt1 = dataFactory.getOWLClass("RT_A", rosiPrefix);
            OWLClass rt2 = dataFactory.getOWLClass("RT_B", rosiPrefix);
            OWLClass rt3 = dataFactory.getOWLClass("RT_C", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt2, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural, rt3, compartment)
            ));
        }
    }


    // Tests for occurrence constraints of role groups

    @Test
    public void test59_RGOccurrenceMaxConstraints1Pre() throws Exception {
        System.out.println("Executing testRGOccurrenceMaxConstraints1Pre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRGTest(c)"),
                    builder.stringToOWLAxiom("plays(n1,RT_D) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RT_D) @ meta2"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2)(c)"),
                    builder.stringToOWLAxiom("≠(n1,n2) @ global")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt = dataFactory.getOWLClass("RT_D", rosiPrefix);
            OWLIndividual natural1 = createNewAnonymousIndividual();
            OWLIndividual natural2 = createNewAnonymousIndividual();

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural1, rt, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural2, rt, compartment),
                    getObjectDifferentIndividualAssertion(natural1, natural2)
            ));
        }
    }

    @Test
    public void test60_RGOccurrenceMaxConstraints1() throws Exception {
        System.out.println("Executing testRGOccurrenceMaxConstraints1:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRGTest(c)"),
                    builder.stringToOWLAxiom("plays(n1,RT_D) @ meta1"),
                    builder.stringToOWLAxiom("plays(n2,RT_D) @ meta2"),
                    builder.stringToOWLAxiom("plays(n3,RT_D) @ meta3"),
                    builder.stringToOWLAxiom("(meta1 ⊓ meta2 ⊓ meta3)(c)"),
                    builder.stringToOWLAxiom("≠(n1,n2,n3) @ global")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass rt = dataFactory.getOWLClass("RT_D", rosiPrefix);
            OWLIndividual natural1 = createNewAnonymousIndividual();
            OWLIndividual natural2 = createNewAnonymousIndividual();
            OWLIndividual natural3 = createNewAnonymousIndividual();

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural1, rt, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural2, rt, compartment),
                    getNaturalPlaysRoleInCompartmentAssertion(natural3, rt, compartment),
                    getObjectDifferentIndividualAssertion(natural1, natural2, natural3)
            ));
        }
    }

    @Test
    public void test61_RGOccurrenceMinConstraintsPre() throws Exception {
        System.out.println("Executing testRGOccurrenceMinConstraintsPre:");

        if (nominalMapping) {
            assertFalse(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRGTest(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertFalse(isInconsistent(
                    getMetaTypeAssertion(ct, compartment)
            ));
        }
    }

    @Test
    public void test62_RGOccurrenceMinConstraints() throws Exception {
        System.out.println("Executing testRGOccurrenceMinConstraints:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("OccurrenceRGTest(c)"),
                    builder.stringToOWLAxiom("∃plays.{RT_D} ⊑ ⊥ @ meta1"),
                    builder.stringToOWLAxiom("meta1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
            OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRT_D", rosiPrefix);

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment),
                    getObjectIsBottom(rtPlayer, compartment)
            ));
        }
    }


    // Further tests about more complex situations

    @Test
    public void test63_InconsistentCT1() throws Exception {
        System.out.println("Executing testInconsistentCT1:");

        if (nominalMapping) {
            assertTrue(isInconsistent(Stream.of(
                    builder.stringToOWLAxiom("Inconsistent1(c)")
            )));
        } else {
            OWLClass ct = dataFactory.getOWLClass("Inconsistent1", rosiPrefix);
            OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

            assertTrue(isInconsistent(
                    getMetaTypeAssertion(ct, compartment)
            ));
        }
    }


    // Auxiliary methods used in the tests

    @SafeVarargs
    private final boolean isInconsistent(Stream<OWLAxiom>... axioms) throws OWLOntologyCreationException {

        return isInconsistent(confWithoutDebug, axioms);
    }

    @SafeVarargs
    private final boolean isInconsistent(Configuration config, Stream<OWLAxiom>... axioms) throws OWLOntologyCreationException {

        Stream<OWLAxiom> axiomsStream = Arrays.stream(axioms)
                .flatMap(Function.identity());

        manager.addAxioms(rawOntology, axiomsStream);

        ContextOntology contextOntology = new ContextOntology(rawOntology, config);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        return !reasoner.isConsistent();
    }

    private Stream<OWLAxiom> getMetaTypeAssertion(OWLClassExpression metaType,
                                                  OWLIndividual individual) {

        return Stream.of(dataFactory.getOWLClassAssertionAxiom(metaType, individual));
    }

    private Stream<OWLAxiom> getGlobalObjectTypeAssertion(OWLClassExpression objectType,
                                                          OWLIndividual individual) {

        return Stream.of(dataFactory.getOWLClassAssertionAxiom(objectType, individual, getObjectGlobal()));
    }

    private Stream<OWLAxiom> getLocalObjectTypeAssertion(OWLClassExpression objectType,
                                                         OWLIndividual objectIndividual,
                                                         OWLIndividual metaIndividual) {

        OWLClass metaA = getAnonymousMetaConcept();

        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaA, metaIndividual),
                dataFactory.getOWLClassAssertionAxiom(objectType, objectIndividual, getIsDefinedBy(metaA)));
    }

    private Stream<OWLAxiom> getObjectDifferentIndividualAssertion(OWLIndividual... individuals) {

        return Stream.of(dataFactory.getOWLDifferentIndividualsAxiom(
                Arrays.stream(individuals).collect(Collectors.toSet()),
                getObjectGlobal()));
    }


    private Stream<OWLAxiom> getObjectIsBottom(OWLClass objectClass, OWLIndividual metaIndividual) {

        OWLClass metaA = getAnonymousMetaConcept();

        return Stream.of(
                dataFactory.getOWLSubClassOfAxiom(objectClass, nothing, getIsDefinedBy(metaA)),
                dataFactory.getOWLClassAssertionAxiom(metaA, metaIndividual));
    }

    private Stream<OWLAxiom> getNaturalPlaysRoleInCompartmentAssertion(OWLIndividual natural,
                                                                       OWLClass roleType,
                                                                       OWLIndividual compartment) {

        OWLClass metaA = getAnonymousMetaConcept();
        OWLClass metaB = getAnonymousMetaConcept();
        OWLIndividual role = dataFactory.getOWLAnonymousIndividual();

        // (metaA ⊓ metaB)(compartment) ∧ [metaA -> (roleType)(r)] ∧ [metaB -> plays(natural, r)]
        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaA, compartment),
                dataFactory.getOWLClassAssertionAxiom(metaB, compartment),
                dataFactory.getOWLClassAssertionAxiom(roleType, role, getIsDefinedBy(metaA)),
                dataFactory.getOWLObjectPropertyAssertionAxiom(plays, natural, role, getIsDefinedBy(metaB)));
    }

    private Stream<OWLAxiom> getNaturalPlaysRoleInCompartmentAssertion(OWLIndividual natural,
                                                                       OWLIndividual role,
                                                                       OWLIndividual compartment) {

        OWLClass metaA = getAnonymousMetaConcept();

        // (metaA)(compartment) ∧ [metaA -> plays(natural, role)]
        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaA, compartment),
                dataFactory.getOWLObjectPropertyAssertionAxiom(plays, natural, role, getIsDefinedBy(metaA)));
    }

    private Stream<OWLAxiom> getRelationshipAssertion(OWLObjectProperty relationshipType,
                                                      OWLIndividual role1,
                                                      OWLIndividual role2,
                                                      OWLIndividual compartment) {

        OWLClass metaConcept = getAnonymousMetaConcept();

        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaConcept, compartment),
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        relationshipType,
                        role1,
                        role2,
                        getIsDefinedBy(metaConcept)));
    }

    private Stream<OWLAxiom> getBottomNTIsBottom() {

        OWLClass bottomNT = dataFactory.getOWLClass("BottomNT", rosiPrefix);

        return Stream.of(dataFactory.getOWLSubClassOfAxiom(bottomNT, nothing, getObjectGlobal()));
    }

    private OWLClass getAnonymousMetaConcept() {

        numberOfAnonymousMetaConcepts++;

        return dataFactory.getOWLClass("___genMetaConcept-" + numberOfAnonymousMetaConcepts, rosiPrefix);
    }

    private OWLIndividual createNewAnonymousIndividual() {

        numberOfAnonymousIndividuals++;

        return dataFactory.getOWLNamedIndividual("genIndividual-" + numberOfAnonymousIndividuals, rosiPrefix);
    }

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {

        return Collections.singletonList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private Collection<OWLAnnotation> getObjectGlobal() {

        return Collections.singletonList(dataFactory.getRDFSLabel("objectGlobal"));
    }

}
