package de.tudresden.inf.lat.jconht.model;

import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
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
    private ReasonerFactory reasonerFactory;
    private PrefixManager rosiPrefix;
    private OWLOntology rawOntology;

    private int unusedMetaConceptIndex;
    private int unusedIndividualIndex;

    private OWLClass naturalTypes;
    private OWLClass roleTypes;
    private OWLClass compartmentTypes;

    private OWLObjectProperty plays;


    @Before
    public void setUp() throws Exception {


        String inputDir = new File("input").getAbsolutePath();
        IRI CromMapperTestOntologyIRI = IRI.create("file://" + inputDir + "/" + "CROMMapperTestOntology.owl");
        manager = OWLManager.createOWLOntologyManager();
        rawOntology = manager.loadOntology(CromMapperTestOntologyIRI);
        dataFactory = manager.getOWLDataFactory();
        reasonerFactory = new ReasonerFactory();
        rosiPrefix = new DefaultPrefixManager("http://www.rosi-project.org/ontologies#");
        unusedMetaConceptIndex = 1;
        unusedIndividualIndex = 1;

        naturalTypes = dataFactory.getOWLClass("NaturalTypes", rosiPrefix);
        roleTypes = dataFactory.getOWLClass("RoleTypes", rosiPrefix);
        compartmentTypes = dataFactory.getOWLClass("CompartmentTypes", rosiPrefix);
        plays = dataFactory.getOWLObjectProperty("plays", rosiPrefix);

    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.removeOntology(rawOntology);
        manager.clearOntologies();
    }

    @Test
    public void testCantPlayRoleTypeTwice() throws Exception {

        OWLIndividual natural = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual context = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual role1 = getAnonymousIndividual();
        OWLIndividual role2 = getAnonymousIndividual();
        OWLClass roleType1 = dataFactory.getOWLClass("RoleType1", rosiPrefix);


        assertTrue(isInconsistent(
                addObjectTypeAssertion(naturalTypes, natural),
                addObjectTypeAssertion(roleType1, role1),
                addObjectTypeAssertion(roleType1, role2),
                addMetaTypeAssertion(compartmentTypes, context),
                addNaturalPlaysRoleInCompartmentAssertion(natural, role1, context),
                addNaturalPlaysRoleInCompartmentAssertion(natural, role2, context),
                addObjectDifferentIndividualAssertion(role1,role2)
        ));
    }

    @Test
    public void testNaturalsAndRolesAreDisjoint() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                addObjectTypeAssertion(naturalTypes, individual),
                addObjectTypeAssertion(roleTypes, individual)
        ));

    }

    @Test
    public void testNaturalIsObject() throws Exception {

        assertTrue(isInconsistent(addMetaTypeAssertion(naturalTypes,dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testCompartmentIsMeta() throws Exception {

        assertTrue(isInconsistent(addObjectTypeAssertion(compartmentTypes, dataFactory.getOWLAnonymousIndividual())));
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
                addObjectTypeAssertion(
                        dataFactory.getOWLObjectMaxCardinality(0,dataFactory.getOWLObjectInverseOf(plays)),
                        individual),
                addObjectTypeAssertion(roleTypes,individual)
        ));
    }

    @Test
    public void testNoRolePlaysAnything() throws Exception {

        OWLIndividual individual = dataFactory.getOWLAnonymousIndividual();

        assertTrue(isInconsistent(
                addObjectTypeAssertion(roleTypes,individual),
                addObjectTypeAssertion(dataFactory.getOWLObjectSomeValuesFrom(plays,dataFactory.getOWLThing()),individual)
        ));
    }

    @Test
    public void testOnlyRolesCanBePlayed() throws Exception {

        assertTrue(isInconsistent(
                addObjectTypeAssertion(
                        dataFactory.getOWLObjectSomeValuesFrom(plays,dataFactory.getOWLObjectComplementOf(roleTypes)),
                        dataFactory.getOWLAnonymousIndividual())));
    }

    @Test
    public void testNaturalTypeInheritance() throws Exception {

        OWLClass nt1 = dataFactory.getOWLClass("NaturalType1", rosiPrefix);
        OWLClass nt2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass nt3 = dataFactory.getOWLClass("NaturalType3", rosiPrefix);
        OWLClass nt4 = dataFactory.getOWLClass("NaturalType4", rosiPrefix);

        // TODO Was könnte hier schief gehen, was sollte hier getestet werden?

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
                addMetaTypeAssertion(compartmentType1,compartment),
                addObjectTypeAssertion(naturalType1,natural1),
                addObjectTypeAssertion(naturalType2,natural2),
                addNaturalPlaysRoleInCompartmentAssertion(natural1,roleType1,compartment),
                addNaturalPlaysRoleInCompartmentAssertion(natural2,roleType1,compartment)
        ));
    }

    @Test
    public void testFillsRelationCheckForbidden() throws Exception {

        OWLIndividual natural2 = dataFactory.getOWLAnonymousIndividual();
        OWLIndividual compartment = dataFactory.getOWLAnonymousIndividual();
        OWLClass roleType2 = dataFactory.getOWLClass("RoleType2", rosiPrefix);
        OWLClass naturalType2 = dataFactory.getOWLClass("NaturalType2", rosiPrefix);
        OWLClass compartmentType1 = dataFactory.getOWLClass("CompartmentType1", rosiPrefix);
        
        assertTrue(isInconsistent(
                addMetaTypeAssertion(compartmentType1,compartment),
                addObjectTypeAssertion(naturalType2,natural2),
                addNaturalPlaysRoleInCompartmentAssertion(natural2,roleType2,compartment)
        ));
    }

    @Test
    public void testRoleTypesAreDisjoint() throws Exception {

        // TODO still todo

    }

    private boolean isInconsistent(Stream<OWLAxiom>... axioms) throws OWLOntologyCreationException {

        manager.addAxioms(rawOntology, Arrays.stream(axioms).flatMap(Function.identity()));

        ContextOntology contextOntology = new ContextOntology(rawOntology);
        ContextReasoner reasoner = new ContextReasoner(contextOntology);

        System.out.println(contextOntology);
        System.out.println("---------------------------------------------");

        boolean inconsistent = !reasoner.isConsistent();

        reasoner.dispose();

        return inconsistent;
    }

    private Stream<OWLAxiom> addMetaTypeAssertion(OWLClassExpression metaType, OWLIndividual individual) {
        return Stream.of(dataFactory.getOWLClassAssertionAxiom(metaType, individual));
    }

    private Stream<OWLAxiom> addObjectTypeAssertion(OWLClassExpression objectType, OWLIndividual individual) {
        return Stream.of(dataFactory.getOWLClassAssertionAxiom(objectType, individual, getObjectGlobal()));
    }

    private Stream<OWLAxiom> addObjectDifferentIndividualAssertion(OWLIndividual individual1, OWLIndividual individual2) {
        return Stream.of(dataFactory.getOWLDifferentIndividualsAxiom(individual1,individual2,getObjectGlobal()));
    }

    private Stream<OWLAxiom> addNaturalPlaysRoleInCompartmentAssertion(OWLIndividual natural, OWLClass roleType, OWLIndividual compartment) {

        OWLClass metaConceptA = getAnonymousMetaConcept();
        OWLClass metaConceptB = getAnonymousMetaConcept();
        OWLIndividual role = dataFactory.getOWLAnonymousIndividual();

        // (metaA ⊓ metaB)(compartment) ∧ [metaA -> (roleType)(r)] ∧ [metaB -> plays(natural,r)]
        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaConceptA, compartment),
                dataFactory.getOWLClassAssertionAxiom(metaConceptB, compartment),
                dataFactory.getOWLClassAssertionAxiom(roleType, role, getIsDefinedBy(metaConceptA)),
                dataFactory.getOWLObjectPropertyAssertionAxiom(plays, natural, role, getIsDefinedBy(metaConceptB)));
    }

    private Stream<OWLAxiom> addNaturalPlaysRoleInCompartmentAssertion(OWLIndividual natural, OWLIndividual role, OWLIndividual compartment) {

        OWLClass metaConceptA = getAnonymousMetaConcept();

        // (metaA)(compartment) ∧ [metaA -> plays(natural,role)]
        return Stream.of(
                dataFactory.getOWLClassAssertionAxiom(metaConceptA, compartment),
                dataFactory.getOWLObjectPropertyAssertionAxiom(plays, natural, role, getIsDefinedBy(metaConceptA)));
    }

    private OWLClass getAnonymousMetaConcept() {
        return dataFactory.getOWLClass("genMetaConcept-" + unusedMetaConceptIndex++, rosiPrefix);
    }

    private OWLIndividual getAnonymousIndividual() {
        return dataFactory.getOWLNamedIndividual("genIndividual-" + unusedIndividualIndex++, rosiPrefix);
    }

    private Collection<OWLAnnotation> getIsDefinedBy(HasIRI hasIRI) {
        return getIsDefinedBy(hasIRI.getIRI());
    }

    private Collection<OWLAnnotation> getIsDefinedBy(IRI iri) {
        return Stream.of(
                dataFactory.getOWLAnnotation(
                        dataFactory.getRDFSIsDefinedBy(),
                        iri))
                .collect(Collectors.toSet());
    }

    private Collection<OWLAnnotation> getObjectGlobal() {
        return Stream.of(dataFactory.getRDFSLabel("objectGlobal"))
                .collect(Collectors.toSet());
    }

}
