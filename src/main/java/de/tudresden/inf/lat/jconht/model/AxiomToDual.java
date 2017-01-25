package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomToDual implements OWLAxiomVisitorEx<OWLAxiom> {

    OWLDataFactory dataFactory;
    Set<OWLClass> conceptsToReplace;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory.
     */
    public AxiomToDual(OWLDataFactory dataFactory, Set<OWLClass> conceptsToReplace) {

        this.dataFactory = dataFactory;
        this.conceptsToReplace = conceptsToReplace;
    }

    @Override
    public <T> OWLAxiom doDefault(T object) {

        // This should never happen!
        throw new UnhandledAxiomTypeException("\nUnhandled axiom type in AxiomToDual: " + object);
    }

    @Override
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
        return dataFactory.getOWLSubClassOfAxiom(
                axiom.getSubClass().accept(new ConceptToDual(dataFactory, conceptsToReplace)),
                axiom.getSuperClass().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {
        return dataFactory.getOWLDisjointClassesAxiom(
                axiom.operands().map(operand -> operand.accept(new ConceptToDual(dataFactory, conceptsToReplace))));
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
        return dataFactory.getOWLObjectPropertyDomainAxiom(
                axiom.getProperty(),
                axiom.getDomain().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLAxiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
        return dataFactory.getOWLObjectPropertyRangeAxiom(
                axiom.getProperty(),
                axiom.getRange().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLSubObjectPropertyOfAxiom axiom) {
        return axiom;
    }

    // TODO Was ist mit DisjointUnionAxiom -> das wird schwierig


    @Override
    public OWLAxiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
        return dataFactory.getOWLClassAssertionAxiom(
                axiom.getClassExpression().accept(new ConceptToDual(dataFactory,conceptsToReplace)),
                axiom.getIndividual());
    }

    @Override
    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
        return dataFactory.getOWLEquivalentClassesAxiom(
                axiom.operands().map(operand -> operand.accept(new ConceptToDual(dataFactory, conceptsToReplace))));
    }

    @Override
    public OWLAxiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLSameIndividualAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLSubPropertyChainOfAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLInverseObjectPropertiesAxiom axiom) {
        return axiom;
    }

    @Override
    public OWLAxiom visit(OWLHasKeyAxiom axiom) {
        return dataFactory.getOWLHasKeyAxiom(
                axiom.getClassExpression().accept(new ConceptToDual(dataFactory, conceptsToReplace)),
                axiom.propertyExpressions().collect(Collectors.toSet()));
    }
}
