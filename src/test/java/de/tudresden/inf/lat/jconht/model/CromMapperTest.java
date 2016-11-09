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
    private OWLClass compartmentTypes;

    private OWLObjectProperty plays;


    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        rosiPrefix = new DefaultPrefixManager("http://www.rosi-project.org/ontologies#");

        String inputDir = new File("input").getAbsolutePath();
        IRI cromMapperTestOntologyIRI = IRI.create("file://" + inputDir + "/" + "CROMMapperTest.owl");
        rawOntology = manager.loadOntology(cromMapperTestOntologyIRI);

        numberOfAnonymousMetaConcepts = 0;
        numberOfAnonymousIndividuals = 0;

        naturalTypes = dataFactory.getOWLClass("NaturalTypes", rosiPrefix);
        roleTypes = dataFactory.getOWLClass("RoleTypes", rosiPrefix);
        compartmentTypes = dataFactory.getOWLClass("CompartmentTypes", rosiPrefix);

        plays = dataFactory.getOWLObjectProperty("plays", rosiPrefix);
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    @Test
    public void testCantPlayRoleTypeTwice() throws Exception {

        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual context = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);

        assertTrue(isInconsistent(
                getObjectTypeAssertion(naturalTypes, natural),
                getObjectTypeAssertion(roleType1, role1),
                getObjectTypeAssertion(roleType1, role2),
                getMetaTypeAssertion(compartmentTypes, context),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role1, context),
                getNaturalPlaysRoleInCompartmentAssertion(natural, role2, context),
                getObjectDifferentIndividualAssertion(role1, role2)));
    }

    @Test
    public void testNaturalsAndRolesAreDisjoint() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getObjectTypeAssertion(naturalTypes, individual),
                getObjectTypeAssertion(roleTypes, individual)));
    }

    @Test
    public void testNaturalIsObject() throws Exception {

        assertTrue(isInconsistent(getMetaTypeAssertion(naturalTypes, dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testCompartmentIsMeta() throws Exception {

        assertTrue(isInconsistent(getObjectTypeAssertion(compartmentTypes, dataFactory.getOWLAnonymousIndividual())));
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
                getObjectTypeAssertion(roleTypes, individual),
                getObjectTypeAssertion(
                        dataFactory.getOWLObjectMaxCardinality(0, dataFactory.getOWLObjectInverseOf(plays)),
                        individual)));
    }

    @Test
    public void testNoRolePlaysAnything() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getObjectTypeAssertion(roleTypes, individual),
                getObjectTypeAssertion(
                        dataFactory.getOWLObjectMinCardinality(1, dataFactory.getOWLObjectInverseOf(plays)),
                        individual)));
    }

    @Test
    public void testOnlyRolesCanBePlayed() throws Exception {

        assertTrue(isInconsistent(
                getObjectTypeAssertion(
                        dataFactory.getOWLObjectSomeValuesFrom(plays, dataFactory.getOWLObjectComplementOf(roleTypes)),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testNaturalTypeInheritance() throws Exception {

        OWLClass nt1 = dataFactory.getOWLClass("NaturalType1", rosiPrefix);
        OWLClass nt2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NaturalType3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NaturalType4", rosiPrefix);
        OWLClass nt5 = dataFactory.getOWLClass("NaturalType5", rosiPrefix);

        assertFalse("NaturalTypes 1–4 can be instantiated.",
                isInconsistent(
                        getObjectTypeAssertion(nt1, dataFactory.getOWLAnonymousIndividual()),
                        getObjectTypeAssertion(nt2, dataFactory.getOWLAnonymousIndividual()),
                        getObjectTypeAssertion(nt3, dataFactory.getOWLAnonymousIndividual()),
                        getObjectTypeAssertion(nt4, dataFactory.getOWLAnonymousIndividual())));

        assertFalse("A natural can be in NT1 without being in its subtype.",
                isInconsistent(getObjectTypeAssertion(
                        dataFactory.getOWLObjectIntersectionOf(
                                nt1,
                                dataFactory.getOWLObjectComplementOf(nt3),
                                dataFactory.getOWLObjectComplementOf(nt4)),
                        dataFactory.getOWLAnonymousIndividual())));

        assertTrue("NaturalType5 has no instances due to multiple inheritance.",
                isInconsistent(getObjectTypeAssertion(nt5, dataFactory.getOWLAnonymousIndividual())));

        assertTrue("A natural must have a type",
                isInconsistent(getObjectTypeAssertion(
                        dataFactory.getOWLObjectIntersectionOf(
                                naturalTypes,
                                dataFactory.getOWLObjectComplementOf(nt1),
                                dataFactory.getOWLObjectComplementOf(nt2)),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testFillsRelationCheckAllowed() throws Exception {

        OWLIndividual natural1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);
        OWLClass naturalType1 = dataFactory.getOWLClass("NaturalType1", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass compartmentType1 = dataFactory.getOWLClass("CompartmentType1", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType1, compartment),
                getObjectTypeAssertion(naturalType1, natural1),
                getObjectTypeAssertion(naturalType2, natural2),
                getNaturalPlaysRoleInCompartmentAssertion(natural1, roleType1, compartment),
                getNaturalPlaysRoleInCompartmentAssertion(natural2, roleType1, compartment)));
    }

    @Test
    public void testRawOntologyIsConsistent() throws Exception {

        assertFalse(isInconsistent());
    }

    @Test
    public void testFillsRelationCheckForbidden() throws Exception {

        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass compartmentType = dataFactory.getOWLClass("CompartmentType1", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType, compartment),
                getObjectTypeAssertion(naturalType2, natural2),
                getNaturalPlaysRoleInCompartmentAssertion(natural2, roleType2, compartment)));
    }

    @Test
    public void testRoleTypesAreDisjoint() throws Exception {

        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);
        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                getObjectTypeAssertion(roleType1, individual),
                getObjectTypeAssertion(roleType2, individual)));
    }

    @Test
    public void testCardinalityCounterIsNeitherNaturalNorRole() throws Exception {

        OWLIndividual cardinalityCounter = dataFactory.getOWLNamedIndividual("cardinalityCounter", rosiPrefix);
        OWLClassExpression naturalOrRole = dataFactory.getOWLObjectUnionOf(naturalTypes, roleTypes);

        assertTrue(isInconsistent(getObjectTypeAssertion(naturalOrRole, cardinalityCounter)));
    }

    @Test
    public void testRelationshipTypeDomainAndRange1() throws Exception {

        OWLObjectProperty relationshipType1 = dataFactory.getOWLObjectProperty("RelationshipType1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType1 = dataFactory.getOWLClass("CompartmentType1", rosiPrefix);
        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);

        assertFalse(isInconsistent(
                getMetaTypeAssertion(compartmentType1, compartment),
                getObjectTypeAssertion(roleType1, role1),
                getObjectTypeAssertion(roleType2, role2),
                getRelationshipAssertion(relationshipType1, role1, role2, compartment)));
    }

    @Test
    public void testRelationshipTypeDomainAndRange2() throws Exception {

        OWLObjectProperty relationshipType1 = dataFactory.getOWLObjectProperty("RelationshipType1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role2 = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType1 = dataFactory.getOWLClass("CompartmentType1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType1, compartment),
                getObjectTypeAssertion(roleType2, role1),
                getObjectTypeAssertion(roleType2, role2),
                getRelationshipAssertion(relationshipType1, role1, role2, compartment)));
    }

    @Test
    public void testRelationshipTypeDomainAndRange3() throws Exception {

        OWLObjectProperty relationshipType1 = dataFactory.getOWLObjectProperty("RelationshipType1", rosiPrefix);
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass compartmentType2 = dataFactory.getOWLClass("CompartmentType2", rosiPrefix);

        assertTrue(isInconsistent(
                getMetaTypeAssertion(compartmentType2, compartment),
                getRelationshipAssertion(
                        relationshipType1,
                        dataFactory.getOWLAnonymousIndividual(),
                        dataFactory.getOWLAnonymousIndividual(),
                        compartment)));
    }

    @Test
    public void testRelationshipTypeCardinalConstraints() throws Exception {

        // TODO: complete this test.
        OWLIndividual role1 = createNewAnonymousIndividual();
        OWLIndividual role2 = createNewAnonymousIndividual();
        OWLIndividual role3 = createNewAnonymousIndividual();
        OWLIndividual role4 = createNewAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);

        assertFalse(isInconsistent(
                getObjectTypeAssertion(roleType1, role1),
                getObjectTypeAssertion(roleType2, role2),
                getObjectTypeAssertion(roleType2, role3),
                getObjectTypeAssertion(roleType2, role4)));
    }

    private boolean isInconsistent(Stream<OWLAxiom>... axioms) throws OWLOntologyCreationException {

        Stream<OWLAxiom> axiomsStream = Arrays.stream(axioms)
                .flatMap(Function.identity());

        manager.addAxioms(rawOntology, axiomsStream);

        ContextOntology contextOntology = new ContextOntology(rawOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        boolean inconsistent = !reasoner.isConsistent();

        reasoner.dispose();

        return inconsistent;
    }

    private Stream<OWLAxiom> getMetaTypeAssertion(OWLClassExpression metaType, OWLIndividual individual) {

        return Stream.of(dataFactory.getOWLClassAssertionAxiom(metaType, individual));
    }

    private Stream<OWLAxiom> getObjectTypeAssertion(OWLClassExpression objectType, OWLIndividual individual) {

        return Stream.of(dataFactory.getOWLClassAssertionAxiom(objectType, individual, getObjectGlobal()));
    }

    private Stream<OWLAxiom> getObjectDifferentIndividualAssertion(OWLIndividual individual1,
                                                                   OWLIndividual individual2) {

        return Stream.of(dataFactory.getOWLDifferentIndividualsAxiom(individual1, individual2, getObjectGlobal()));
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
