package de.tudresden.inf.lat.jconht.model;

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

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private PrefixManager rosiPrefix;

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

        String inputDir = new File("input").getAbsolutePath();
        File cromMapperTestOntologyFile = new File("input/CROMMapperTest/MapperTest.owl");
        //TODO hier auch wieder die Frage, wie man die Ontology richtig lädt
        rawOntology = manager.loadOntology(IRI.create(cromMapperTestOntologyFile));

        numberOfAnonymousMetaConcepts = 0;
        numberOfAnonymousIndividuals = 0;

        naturalTypes = dataFactory.getOWLClass("NaturalTypes", rosiPrefix);
        roleTypes = dataFactory.getOWLClass("RoleTypes", rosiPrefix);
        roleGroups = dataFactory.getOWLClass("RoleGroups", rosiPrefix);
        compartmentTypes = dataFactory.getOWLClass("CompartmentTypes", rosiPrefix);
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
    public void testRawOntologyIsConsistent() throws Exception {
        System.out.println("Executing testRawOntologyIsConsistent: ");

        assertFalse(isInconsistent());
    }

    @Test
    public void testNaturalIsObject() throws Exception {
        System.out.println("Executing testNaturalIsObject: ");

        assertTrue(isInconsistent(getMetaTypeAssertion(naturalTypes, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testCompartmentIsMeta() throws Exception {
        System.out.println("Executing testCompartmentIsMeta: ");

        assertTrue(isInconsistent(getGlobalObjectTypeAssertion(compartmentTypes, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testNaturalsAndRolesAreDisjoint() throws Exception {
        System.out.println("Executing testNaturalsAndRolesAreDisjoint: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(naturalTypes, individual),
                getGlobalObjectTypeAssertion(roleTypes, individual)));
    }

    @Test
    public void testPlaysIsObject() throws Exception {
        System.out.println("Executing testPlaysIsObject: ");

        assertTrue(isInconsistent(Stream.of(
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        plays,
                        dataFactory.getOWLAnonymousIndividual(),
                        dataFactory.getOWLAnonymousIndividual()))));
    }

    @Test
    public void testEveryRoleIsPlayed() throws Exception {
        System.out.println("Executing testEveryRoleIsPlayed: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleTypes, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMaxCardinality(0, dataFactory.getOWLObjectInverseOf(plays)),
                        individual)));
    }

    @Test
    public void testEveryRoleGroupIsPlayed() throws Exception {
        System.out.println("Executing testEveryRoleGroupIsPlayed: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleGroups, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMaxCardinality(0, dataFactory.getOWLObjectInverseOf(plays)),
                        individual)));
    }

    @Test
    public void testNoRolePlaysAnything() throws Exception {
        System.out.println("Executing testNoRolePlaysAnything: ");

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleTypes, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMinCardinality(1, plays),
                        individual)));
    }

    @Test
    public void testOnlyRolesOrRoleGroupsCanBePlayed() throws Exception {
        System.out.println("Executing testOnlyRolesOrRoleGroupsCanBePlayed: ");

        OWLClassExpression roleTypesOrGroups = dataFactory.getOWLObjectUnionOf(roleTypes, roleGroups);

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectSomeValuesFrom(plays, dataFactory.getOWLObjectComplementOf(roleTypesOrGroups)),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testOccurrenceCounterIsNeitherNaturalNorRole() throws Exception {
        System.out.println("Executing testOccurrenceCounterIsNeitherNaturalNorRole: ");

        OWLIndividual occurrenceCounter = dataFactory.getOWLNamedIndividual("occurrenceCounter", rosiPrefix);
        OWLClassExpression natTypeOrRoleTypeOrRoleGroup = dataFactory.getOWLObjectUnionOf(
                dataFactory.getOWLClass("NaturalTypes", rosiPrefix),
                dataFactory.getOWLClass("RoleTypes", rosiPrefix),
                dataFactory.getOWLClass("RoleGroups", rosiPrefix));

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(natTypeOrRoleTypeOrRoleGroup, occurrenceCounter)));
    }


    





    /*
     Tests that must be passed for the CROMMapperTest ontology.
     */

    // Compartments

    @Test
    public void testCompartmentDisjointnessPre() throws Exception {
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
    public void testCompartmentDisjointness() throws Exception {
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
    public void testCompartmentIsNotEmpty() throws Exception {
        System.out.println("Executing testCompartmentIsNotEmpty: ");

        OWLClass ct1 = dataFactory.getOWLClass("EmptyCompartment", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct1, compartment)
        ));
    }


    // Natural type inheritance

    @Test
    public void testNTInheritance1To4CanBeInstantiated() throws Exception {
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
    public void testNTInheritanceNT2AndNotSubType() throws Exception {
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
    public void testNTInheritanceNoNT5() throws Exception {
        System.out.println("Executing testNTInheritanceNoNT5: ");

        OWLClass nt5 = dataFactory.getOWLClass("NTInheritance5", rosiPrefix);

        assertTrue("NaturalType5 has no instances due to multiple inheritance.",
                isInconsistent(getGlobalObjectTypeAssertion(nt5, dataFactory.getOWLAnonymousIndividual())));

    }

    @Test
    public void testNTInheritanceNatMustHaveSomeType() throws Exception {
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
    public void testFillsAllowed() throws Exception {
        System.out.println("Executing testFillsAllowed: ");

        OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass naturalType1 = dataFactory.getOWLClass("NTFiller1", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NTFiller2", rosiPrefix);

        OWLIndividual compartment1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType1 = dataFactory.getOWLClass("FillsTest1", rosiPrefix);
        OWLClass compartmentType2 = dataFactory.getOWLClass("FillsTest2", rosiPrefix);

        OWLClass roleType1 = dataFactory.getOWLClass("RTFills1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType1, compartment1),
                getMetaTypeAssertion(compartmentType2, compartment2),
                getGlobalObjectTypeAssertion(naturalType1, natural1),
                getGlobalObjectTypeAssertion(naturalType2, natural2),
                getNaturalPlaysRoleInCompartmentAssertion(natural1, roleType1, compartment1),
                getNaturalPlaysRoleInCompartmentAssertion(natural2, roleType1, compartment1)));
    }

    @Test
    public void testFillsRelationCheckForbidden() throws Exception {
        System.out.println("Executing testFillsRelationCheckForbidden: ");

        OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass roleType2 = dataFactory.getOWLClass("RTFills2", rosiPrefix);
        OWLClass naturalType1 = dataFactory.getOWLClass("NTFiller1", rosiPrefix);
        OWLClass compartmentType2 = dataFactory.getOWLClass("FillsTest2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType2, compartment2),
                getGlobalObjectTypeAssertion(naturalType1, natural1),
                getNaturalPlaysRoleInCompartmentAssertion(natural1, roleType2, compartment2)));
    }


    // Basic properties about CROM Roles.

    @Test
    public void testCantPlayRoleTypeTwicePre() throws Exception {
        System.out.println("Executing testCantPlayRoleTypeTwicePre: ");

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

    @Test
    public void testCantPlayRoleTypeTwice() throws Exception {
        System.out.println("Executing testCantPlayRoleTypeTwice: ");

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

    @Test
    public void testRoleTypesAreDisjointPre() throws Exception {
        System.out.println("Executing testRoleTypesAreDisjointPre: ");

        OWLClass roleType1 = dataFactory.getOWLClass("RTDisjoint1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTDisjoint2", rosiPrefix);
        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual individual2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getLocalObjectTypeAssertion(roleType1, individual, compartment),
                getLocalObjectTypeAssertion(roleType2, individual2, compartment)));
    }

    @Test
    public void testRoleTypesAreDisjoint() throws Exception {
        System.out.println("Executing testRoleTypesAreDisjoint: ");

        OWLClass roleType1 = dataFactory.getOWLClass("RTDisjoint1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTDisjoint2", rosiPrefix);
        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getLocalObjectTypeAssertion(roleType1, individual, compartment),
                getLocalObjectTypeAssertion(roleType2, individual, compartment)));
    }


    // Tests for relationship type domain and range

    @Test
    public void testRelationshipTypeDomainPre() throws Exception {
        System.out.println("Executing testRelationshipTypeDomainPre: ");

        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType = dataFactory.getOWLClass("RSTDomainRange1", rosiPrefix);
        OWLClass roleType1 = dataFactory.getOWLClass("RTRSTDomain", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTRSTRange", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment),
                getRelationshipAssertion(rst, role1, role2, compartment)
        ));
    }

    @Test
    public void testRelationshipTypeWrongDomain() throws Exception {
        System.out.println("Executing testRelationshipTypeWrongDomain:");

        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType = dataFactory.getOWLClass("RSTDomainRange1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTRSTRange", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getLocalObjectTypeAssertion(roleType2, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment),
                getRelationshipAssertion(rst, role1, role2, compartment)));
    }

    @Test
    public void testRelationshipTypeDifferentDomainRangePre() throws Exception {
        System.out.println("Executing testRelationshipTypeDifferentDomainRangePre:");

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType = dataFactory.getOWLClass("RSTDomainRangeDifferent", rosiPrefix);
        OWLClass roleType = dataFactory.getOWLClass("RTRSTDomRan", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getLocalObjectTypeAssertion(roleType, role1, compartment),
                getLocalObjectTypeAssertion(roleType, role2, compartment)
        ));
    }

    @Test
    public void testRelationshipTypeDifferentDomainRange() throws Exception {
        System.out.println("Executing testRelationshipTypeDifferentDomainRange:");

        OWLClass compartmentType = dataFactory.getOWLClass("RSTDomainRangeDifferent", rosiPrefix);
        OWLClass roleType = dataFactory.getOWLClass("RTRSTDomRan", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTSameDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getLocalObjectTypeAssertion(roleType, role1, compartment),
                getLocalObjectTypeAssertion(roleType, role2, compartment),
                getRelationshipAssertion(rst, role1, role2, compartment)
        ));
    }

    // Tests for the occurrence constraints (for role types)

    @Test
    public void testOccurrenceMaxConstraints1Pre() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints1Pre:");

        OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
        OWLClass rt = dataFactory.getOWLClass("RTOccurrence1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = createNewAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(rt, role1, compartment)
        ));
    }

    @Test
    public void testOccurrenceMaxConstraints1() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints1:");

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

    @Test
    public void testOccurrenceMaxConstraints2Pre() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints2Pre:");

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

    @Test
    public void testOccurrenceMaxConstraints2() throws Exception {
        System.out.println("Executing testOccurrenceMaxConstraints2:");

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

    @Test
    public void testOccurrenceMinConstraintsPre() throws Exception {
        System.out.println("Executing testOccurrenceMinConstraintsPre:");

        OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment)
        ));
    }

    @Test
    public void testOccurrenceMinConstraints() throws Exception {
        System.out.println("Executing testOccurrenceMinConstraints:");

        OWLClass ct = dataFactory.getOWLClass("OccurrenceRTTest1", rosiPrefix);
        OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRTOccurrence2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getObjectIsBottom(rtPlayer, compartment)
        ));
    }

    @Test
    public void testOccurrenceConstraintsNeedFiller1() throws Exception {
        System.out.println("Executing testOccurrenceConstraintsNeedFiller1:");

        OWLClass compartmentType = dataFactory.getOWLClass("OccurrenceRTTest2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getBottomNTIsBottom(),
                getMetaTypeAssertion(compartmentType, compartment)
        ));
    }

    @Test
    public void testOccurrenceConstraintsNeedFiller2() throws Exception {
        System.out.println("Executing testOccurrenceConstraintsNeedFiller2:");

        OWLClass compartmentType = dataFactory.getOWLClass("OccurrenceRTTest3", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getBottomNTIsBottom(),
                getMetaTypeAssertion(compartmentType, compartment)
        ));
    }


    // Tests for the cardinality constraints of a relationship type.

    @Test
    public void testRelationshipTypeCardConstraints1MaxPre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardConstraints1MaxPre:");

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCard2", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTCard1", rosiPrefix);

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

    @Test
    public void testRelationshipTypeCardinalConstraints1Max() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints1Max:");

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLIndividual role5 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCard2", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTCard1", rosiPrefix);

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

    @Test
    public void testRelationshipTypeCardConstraints1MinPre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardConstraints1MinPre:");

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCard1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment)
        ));
    }


    @Test
    public void testRelationshipTypeCardinalConstraints1Min() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints1Min:");

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

    @Test
    public void testRelationshipTypeCardinalConstraints2Pre() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Pre:");

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

    @Test
    public void testRelationshipTypeCardinalConstraints2Max() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Max:");

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLIndividual role5 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCard4", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCard3", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTCard2", rosiPrefix);

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

    @Test
    public void testRelationshipTypeCardinalConstraints2Min() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints2Min:");

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

    @Test
    public void testRelationshipTypeCardinalConstraints3() throws Exception {
        System.out.println("Executing testRelationshipTypeCardinalConstraints3:");

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


    // Tests for  role groups


    @Test
    public void testImplicationPre1() throws Exception {
        System.out.println("Executing testImplicationPre1:");

        OWLClass ct = dataFactory.getOWLClass("RoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTImplication2", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getObjectIsBottom(rt2Player, compartment)
        ));
    }

    @Test
    public void testImplicationPre2() throws Exception {
        System.out.println("Executing testImplicationPre2:");

        OWLClass ct = dataFactory.getOWLClass("RoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTImplication1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
        ));
    }

    @Test
    public void testImplication() throws Exception {
        System.out.println("Executing testImplication:");

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

    @Test
    public void testProhibitionPre1() throws Exception {
        System.out.println("Executing testProhibitionPre1:");

        OWLClass ct = dataFactory.getOWLClass("RoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
        ));
    }

    @Test
    public void testProhibitionPre2() throws Exception {
        System.out.println("Executing testProhibitionPre2:");

        OWLClass ct = dataFactory.getOWLClass("RoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt2, compartment)
        ));
    }

    @Test
    public void testProhibition() throws Exception {
        System.out.println("Executing testProhibition:");

        OWLClass ct = dataFactory.getOWLClass("RoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);
        OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt2, compartment)
        ));
    }

    @Test
    public void testEquivalencePre() throws Exception {
        System.out.println("Executing testEquivalencePre:");

        OWLClass ct = dataFactory.getOWLClass("RoleEquivalence", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTEquivalence1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
        ));
    }

    @Test
    public void testEquivalence1() throws Exception {
        System.out.println("Executing testEquivalence1:");

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

    @Test
    public void testEquivalence2() throws Exception {
        System.out.println("Executing testEquivalence2:");

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

    @Test
    public void testRiehleConstraintsPre() throws Exception {
        System.out.println("Executing testRiehleConstraintsPre:");

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

    @Test
    public void testRoleConstraints() throws Exception {
        System.out.println("Executing testRoleConstraints:");

        OWLClass ct = dataFactory.getOWLClass("ComplexRiehle", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTRiehle1", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
        ));
    }

    @Test
    public void testRoleGroupMinMaxPre() throws Exception {
        System.out.println("Executing testRoleGroupMinMaxPre:");

        OWLClass ct = dataFactory.getOWLClass("RGsMinMaxTest", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RT_A", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, rt1, compartment)
        ));
    }

    @Test
    public void testRoleGroupMin() throws Exception {
        System.out.println("Executing testRoleGroupMin:");

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

    @Test
    public void testRoleGroupMax() throws Exception {
        System.out.println("Executing testRoleGroupMax:");

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


    // Tests for occurrence constraints of role groups

    @Test
    public void testRGOccurrenceMaxConstraints1Pre() throws Exception {
        System.out.println("Executing testRGOccurrenceMaxConstraints1Pre:");

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

    @Test
    public void testRGOccurrenceMaxConstraints1() throws Exception {
        System.out.println("Executing testRGOccurrenceMaxConstraints1:");

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

    @Test
    public void testRGOccurrenceMinConstraintsPre() throws Exception {
        System.out.println("Executing testRGOccurrenceMinConstraintsPre:");

        OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment)
        ));
    }

    @Test
    public void testRGOccurrenceMinConstraints() throws Exception {
        System.out.println("Executing testRGOccurrenceMinConstraints:");

        OWLClass ct = dataFactory.getOWLClass("OccurrenceRGTest", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRT_D", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getObjectIsBottom(rtPlayer, compartment)
        ));
    }


    // Further tests about more complex situations

    @Test
    public void testInconsistentCT1() throws Exception {
        System.out.println("Executing testInconsistentCT1:");

        OWLClass ct = dataFactory.getOWLClass("Inconsistent1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment)
        ));
    }


    // Auxiliary methods used in the tests

    private boolean isInconsistent(Stream<OWLAxiom>... axioms) throws OWLOntologyCreationException {

        Stream<OWLAxiom> axiomsStream = Arrays.stream(axioms)
                .flatMap(Function.identity());

        manager.addAxioms(rawOntology, axiomsStream);

        ContextOntology contextOntology = new ContextOntology(rawOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        boolean inconsistent = !reasoner.isConsistent();

        //contextOntology.clear();
        //reasoner.dispose();

        return inconsistent;
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

        return Arrays.asList(dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(), hasIRI.getIRI()));
    }

    private Collection<OWLAnnotation> getObjectGlobal() {

        return Arrays.asList(dataFactory.getRDFSLabel("objectGlobal"));
    }

}
