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

    private OWLObjectProperty plays;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        rosiPrefix = new DefaultPrefixManager("http://www.rosi-project.org/ontologies#");

        String inputDir = new File("input").getAbsolutePath();
        IRI cromMapperTestOntologyIRI = IRI.create("file://" + inputDir + "/" + "CROMMapperTestLink.owl");
        rawOntology = manager.loadOntology(cromMapperTestOntologyIRI);

        numberOfAnonymousMetaConcepts = 0;
        numberOfAnonymousIndividuals = 0;

        naturalTypes = dataFactory.getOWLClass("NaturalTypes", rosiPrefix);
        roleTypes = dataFactory.getOWLClass("RoleTypes", rosiPrefix);
        roleGroups = dataFactory.getOWLClass("RoleGroups", rosiPrefix);
        compartmentTypes = dataFactory.getOWLClass("CompartmentTypes", rosiPrefix);

        plays = dataFactory.getOWLObjectProperty("plays", rosiPrefix);
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }





    /*
     General tests that must pass for every generated ontology.
     */

    @Test
    public void testRawOntologyIsConsistent() throws Exception {

        assertFalse(isInconsistent());
    }

    @Test
    public void testNaturalIsObject() throws Exception {

        assertTrue(isInconsistent(getMetaTypeAssertion(naturalTypes, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testCompartmentIsMeta() throws Exception {

        assertTrue(isInconsistent(getGlobalObjectTypeAssertion(compartmentTypes, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testNaturalsAndRolesAreDisjoint() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(naturalTypes, individual),
                getGlobalObjectTypeAssertion(roleTypes, individual)));
    }

    @Test
    public void testPlaysIsObject() throws Exception {

        assertTrue(isInconsistent(Stream.of(
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        plays,
                        dataFactory.getOWLAnonymousIndividual(),
                        dataFactory.getOWLAnonymousIndividual()))));
    }

    @Test
    public void testEveryRoleIsPlayed() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleTypes, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMaxCardinality(0, dataFactory.getOWLObjectInverseOf(plays)),
                        individual)));
    }

    @Test
    public void testNoRolePlaysAnything() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(roleTypes, individual),
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectMinCardinality(1, plays),
                        individual)));
    }

    @Test
    public void testOnlyRolesOrRoleGroupsCanBePlayed() throws Exception {

        OWLClassExpression roleTypesOrGroups = dataFactory.getOWLObjectUnionOf(roleTypes, roleGroups);

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(
                        dataFactory.getOWLObjectSomeValuesFrom(plays, dataFactory.getOWLObjectComplementOf(roleTypesOrGroups)),
                        dataFactory.getOWLAnonymousIndividual())));
    }


    





    /*
     Tests that must be passed for the CROMMapperTest ontology.
     */


    // Compartments

    @Test
    public void testCompartmentDisjointness() throws Exception {

        OWLClass ct1 = dataFactory.getOWLClass("CTDisjoint1", rosiPrefix);
        OWLClass ct2 = dataFactory.getOWLClass("CTDisjoint2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct1, compartment),
                getMetaTypeAssertion(ct2, compartment)
        ));
    }


    // Natural type inheritance

    @Test
    public void testNTInheritance1To4CanBeInstantiated() throws Exception {

        OWLClass nt1 = dataFactory.getOWLClass("NTInheritanceTest1", rosiPrefix);
        OWLClass nt2 = dataFactory.getOWLClass("NTInheritanceTest2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NTInheritanceTest3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NTInheritanceTest4", rosiPrefix);

        assertFalse("NaturalTypes 1–4 can be instantiated.",
                isInconsistent(
                        getGlobalObjectTypeAssertion(nt1, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt2, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt3, dataFactory.getOWLAnonymousIndividual()),
                        getGlobalObjectTypeAssertion(nt4, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testNTInheritanceNT2AndNotSubType() throws Exception {

        OWLClass nt2 = dataFactory.getOWLClass("NTInheritanceTest2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NTInheritanceTest3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NTInheritanceTest4", rosiPrefix);

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

        OWLClass nt5 = dataFactory.getOWLClass("NTInheritanceTest5", rosiPrefix);

        assertTrue("NaturalType5 has no instances due to multiple inheritance.",
                isInconsistent(getGlobalObjectTypeAssertion(nt5, dataFactory.getOWLAnonymousIndividual())));

    }

    @Test
    public void testNTInheritanceNatMustHaveSomeType() throws Exception {

        OWLClassExpression allTopLevelNT = dataFactory.getOWLObjectUnionOf(
                dataFactory.getOWLClass("NTInheritanceTest1", rosiPrefix),
                dataFactory.getOWLClass("NTInheritanceTest2", rosiPrefix),
                dataFactory.getOWLClass("NTCantPlayTwice", rosiPrefix),
                dataFactory.getOWLClass("NTFillsTest1", rosiPrefix),
                dataFactory.getOWLClass("NTFillsTest2", rosiPrefix),
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

        OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass naturalType1 = dataFactory.getOWLClass("NTFillsTest1", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NTFillsTest2", rosiPrefix);

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType1 = dataFactory.getOWLClass("CTFillsTest1", rosiPrefix);

        OWLClass roleType1 = dataFactory.getOWLClass("RTFillsTest1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType1, compartment),
                getGlobalObjectTypeAssertion(naturalType1, natural1),
                getGlobalObjectTypeAssertion(naturalType2, natural2),
                getNaturalPlaysRoleInCompartmentAssertion(natural1, roleType1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural2, roleType1, compartment)));
    }

    @Test
    public void testFillsRelationCheckForbidden() throws Exception {

        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass roleType2 = dataFactory.getOWLClass("RTFillsTest2", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass compartmentType = dataFactory.getOWLClass("CTFillsTest2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getGlobalObjectTypeAssertion(naturalType2, natural2),
                getNaturalPlaysRoleInCompartmentAssertion(natural2, roleType2, compartment)));
    }


    // Basic properties about CROM Roles.

    @Test
    public void testCantPlayRoleTypeTwice() throws Exception {

        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual context = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCantBePlayedTwice", rosiPrefix);
        OWLClass naturalType = dataFactory.getOWLClass("NTCantPlayTwice", rosiPrefix);
        OWLClass compartmentType = dataFactory.getOWLClass("CTRolesCantBePlayedTwice", rosiPrefix);

        assertFalse("One role should be playable!", isInconsistent(
                getGlobalObjectTypeAssertion(naturalType, natural),
                getGlobalObjectTypeAssertion(roleType1, role1),
                getMetaTypeAssertion(compartmentType, context),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, context)));

        assertTrue("Two roles should not be playable!", isInconsistent(
                getGlobalObjectTypeAssertion(roleType1, role2),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, context),
                getObjectDifferentIndividualAssertion(role1, role2)));
    }

    @Test
    public void testRoleTypesAreDisjointPart1() throws Exception {

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
    public void testRoleTypesAreDisjointPart2() throws Exception {

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
    public void testRelationshipTypeDomainRange1() throws Exception {

        OWLClass roleType1 = dataFactory.getOWLClass("RTRSTDom", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTRSTRan", rosiPrefix);
        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual individual2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getLocalObjectTypeAssertion(roleType1, individual, compartment),
                getLocalObjectTypeAssertion(roleType2, individual2, compartment)));

    }

    @Test
    public void testRelationshipTypeDomainRange2() throws Exception {

        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType = dataFactory.getOWLClass("CTRSTDomainRange1", rosiPrefix);
        OWLClass roleType1 = dataFactory.getOWLClass("RTRSTDom", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTRSTRan", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment),
                getRelationshipAssertion(rst, role1, role2, compartment)));
    }

    @Test
    public void testRelationshipTypeDomainRange3() throws Exception {

        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType = dataFactory.getOWLClass("CTRSTDomainRange1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTRSTRan", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getGlobalObjectTypeAssertion(roleType2, role1),
                getGlobalObjectTypeAssertion(roleType2, role2),
                getRelationshipAssertion(rst, role1, role2, compartment)));
    }

    @Test
    public void testRelationshipTypeDomainRange4() throws Exception {

        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTDomRan", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType2 = dataFactory.getOWLClass("CTRSTDomainRange2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType2, compartment),
                getRelationshipAssertion(
                        rst,
                        dataFactory.getOWLAnonymousIndividual(),
                        dataFactory.getOWLAnonymousIndividual(),
                        compartment)));
    }


    // Tests for the occurrence constraints (for role types)

    @Test
    public void testOccurrenceCounterIsNeitherNaturalNorRole() throws Exception {
        OWLIndividual occurrenceCounter = dataFactory.getOWLNamedIndividual("occurrenceCounter", rosiPrefix);
        OWLClassExpression natTypeOrRoleTypeOrRoleGroup = dataFactory.getOWLObjectUnionOf(
                dataFactory.getOWLClass("NaturalTypes", rosiPrefix),
                dataFactory.getOWLClass("RoleTypes", rosiPrefix),
                dataFactory.getOWLClass("RoleGroups", rosiPrefix));

        assertTrue(isInconsistent(
                getGlobalObjectTypeAssertion(natTypeOrRoleTypeOrRoleGroup, occurrenceCounter)));
    }

    @Test
    public void testOccurrenceConstraintsNeedFiller1() throws Exception {

        OWLClass compartmentType = dataFactory.getOWLClass("CTOccurrenceRTs2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment)
        ));
    }

    @Test
    public void testOccurrenceConstraintsNeedFiller2() throws Exception {

        OWLClass compartmentType = dataFactory.getOWLClass("CTOccurrenceRTs3", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment)
        ));
    }

    @Test
    public void testOccurrenceMaxConstraints1Pre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
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

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
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

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
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

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
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

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment)
        ));
    }

    @Test
    public void testOccurrenceMinConstraints() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRTs1", rosiPrefix);
        OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRTOccurrence2", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getObjectIsBottom(rtPlayer, compartment)
        ));
    }


    // Tests for the cardinality constraints of a relationship type.

    @Test
    public void testRelationshipTypeCardinalConstraints1Pre() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCardinality2", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment)
        ));
    }

    @Test
    public void testRelationshipTypeCardinalConstraints1Max() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLIndividual role5 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCardinality2", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTCardinality1", rosiPrefix);

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
    public void testRelationshipTypeCardinalConstraints1Min() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality1", rosiPrefix);
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTCardinality2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getObjectIsBottom(rt2Player, compartment)
                ));
    }

    @Test
    public void testRelationshipTypeCardinalConstraints2Pre() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality4", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCardinality3", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment)
        ));
    }

    @Test
    public void testRelationshipTypeCardinalConstraints2Max() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLIndividual role5 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality4", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCardinality3", rosiPrefix);
        OWLObjectProperty rst = dataFactory.getOWLObjectProperty("RSTCardinality2", rosiPrefix);

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

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality4", rosiPrefix);
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTCardinality3", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getObjectIsBottom(rt2Player, compartment)
        ));
    }

    @Test
    public void testRelationshipTypeCardinalConstraints3() throws Exception {

        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass ct = dataFactory.getOWLClass("CTCardinalConstraints", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RTCardinality5", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RTCardinality6", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(roleType1, role1, compartment),
                getLocalObjectTypeAssertion(roleType2, role2, compartment)
        ));
    }


    // Tests for  role groups

    @Test
    public void testImplicationPre1() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural)
        ));
    }

    @Test
    public void testImplicationPre2() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTImplication2", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getObjectIsBottom(rt2Player, compartment)
        ));
    }

    @Test
    public void testImplicationPre3() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTImplication1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)
        ));
    }

    @Test
    public void testImplication() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleImplication", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTImplication1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTImplication2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                getObjectIsBottom(rt2Player, compartment)
        ));
    }

    @Test
    public void testProhibitionPre1() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)
        ));
    }

    @Test
    public void testProhibitionPre2() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt2, role2, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment)
        ));
    }

    @Test
    public void testProhibition() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleProhibition", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTProhibition1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTProhibition2", rosiPrefix);
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getLocalObjectTypeAssertion(rt2, role2, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment)
        ));
    }

    @Test
    public void testEquivalencePre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleEquivalence", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTEquivalence1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)
        ));
    }

    @Test
    public void testEquivalence1() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleEquivalence", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTEquivalence1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTEquivalence2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                getObjectIsBottom(rt2Player, compartment)
        ));
    }

    @Test
    public void testEquivalence2() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleEquivalence", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTEquivalence2", rosiPrefix);
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1Player = dataFactory.getOWLClass("PlaysRTEquivalence1", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt2, role2, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment),
                getObjectIsBottom(rt1Player, compartment)
        ));
    }

    @Test
    public void testRoleConstraintsPre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleConstraints", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTConstraints2", rosiPrefix);
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt2, role2, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment)
        ));
    }

    @Test
    public void testRoleConstraints() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleConstraints", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTConstraints1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)
        ));
    }

    @Test
    public void testRoleGroupMinMaxPre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleGroupsMinMax", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTRGMinMax1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment)
        ));
    }

    @Test
    public void testRoleGroupMin() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleGroupsMinMax", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTRGMinMax1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2Player = dataFactory.getOWLClass("PlaysRTRGMinMax2", rosiPrefix);
        OWLClass rt3Player = dataFactory.getOWLClass("PlaysRTRGMinMax3", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                getObjectIsBottom(rt2Player, compartment),
                getObjectIsBottom(rt3Player, compartment)
        ));
    }

    @Test
    public void testRoleGroupMax() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTRoleGroupsMinMax", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass defaultNT = dataFactory.getOWLClass("DefaultNT", rosiPrefix);
        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt1 = dataFactory.getOWLClass("RTRGMinMax1", rosiPrefix);
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt2 = dataFactory.getOWLClass("RTRGMinMax2", rosiPrefix);
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt3 = dataFactory.getOWLClass("RTRGMinMax3", rosiPrefix);
        OWLIndividual role3 = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getGlobalObjectTypeAssertion(defaultNT, natural),
                getLocalObjectTypeAssertion(rt1, role1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, compartment),
                getLocalObjectTypeAssertion(rt2, role2, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, compartment),
                getLocalObjectTypeAssertion(rt3, role3, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role3, compartment)
        ));
    }


    // Tests for occurrence constraints of role groups

    @Test
    public void testRGOccurrenceMaxConstraints1Pre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRGs", rosiPrefix);
        OWLClass rt = dataFactory.getOWLClass("RTRG1A", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = createNewAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(rt, role1, compartment)
        ));
    }

    @Test
    public void testRGOccurrenceMaxConstraints1() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRGs1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass rt = dataFactory.getOWLClass("RTRG1A", rosiPrefix);
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getLocalObjectTypeAssertion(rt, role1, compartment),
                getLocalObjectTypeAssertion(rt, role2, compartment),
                getLocalObjectTypeAssertion(rt, role3, compartment),
                getObjectDifferentIndividualAssertion(role1, role2, role3)
        ));
    }

    @Test
    public void testRGOccurrenceMinConstraintsPre() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRGs", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();

        assertFalse(isInconsistent(
                getMetaTypeAssertion(ct, compartment)
        ));
    }

    @Test
    public void testRGOccurrenceMinConstraints() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTOccurrenceRGs", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass rtPlayer = dataFactory.getOWLClass("PlaysRTRG1A", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(ct, compartment),
                getObjectIsBottom(rtPlayer, compartment)
        ));
    }


    // Further tests about more complex situations

    @Test
    public void testInconsistentCT1() throws Exception {

        OWLClass ct = dataFactory.getOWLClass("CTInconsistentConstraints", rosiPrefix);
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

        contextOntology.clear();
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
                dataFactory.getOWLSubClassOfAxiom(objectClass, dataFactory.getOWLNothing(), getIsDefinedBy(metaA)),
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

    private OWLClass getAnonymousMetaConcept() {

        numberOfAnonymousMetaConcepts++;

        return dataFactory.getOWLClass("genMetaConcept-" + numberOfAnonymousMetaConcepts, rosiPrefix);
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
