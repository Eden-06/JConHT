package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Set;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ConceptToDual implements OWLClassExpressionVisitorEx<OWLClassExpression> {

    private OWLDataFactory dataFactory;
    private Set<OWLClass> conceptsToReplace;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory.
     */
    public ConceptToDual(OWLDataFactory dataFactory, Set<OWLClass> conceptsToReplace) {

        this.dataFactory = dataFactory;
        this.conceptsToReplace = conceptsToReplace;
    }

    @Override
    public <T> OWLClassExpression doDefault(T object) {

        // This should never happen!
        throw new UnhandledClassExpressionException("\nUnhandled class expression in ConceptToDual: " + object);
    }

    @Override
    public OWLClassExpression visit(OWLClass owlClass) {
        return conceptsToReplace.contains(owlClass) ?
                dataFactory.getOWLObjectComplementOf(getDualClass(owlClass)) :
                owlClass;
    }

    @Override
    public OWLClassExpression visit(OWLObjectIntersectionOf conjunction) {
        return dataFactory.getOWLObjectIntersectionOf(conjunction.operands()
                .map(conjunct -> conjunct.accept(new ConceptToDual(dataFactory, conceptsToReplace))));
    }

    @Override
    public OWLClassExpression visit(OWLObjectUnionOf disjunction) {
        return dataFactory.getOWLObjectUnionOf(disjunction.operands()
                .map(disjunct -> disjunct.accept(new ConceptToDual(dataFactory, conceptsToReplace))));
    }

    @Override
    public OWLClassExpression visit(OWLObjectComplementOf complement) {
        return dataFactory.getOWLObjectComplementOf(complement.getOperand()
                .accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectSomeValuesFrom existentialRestriction) {
        return dataFactory.getOWLObjectSomeValuesFrom(
                existentialRestriction.getProperty(),
                existentialRestriction.getFiller().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectAllValuesFrom valueRestriction) {
        return dataFactory.getOWLObjectAllValuesFrom(
                valueRestriction.getProperty(),
                valueRestriction.getFiller().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectMinCardinality minCardinality) {
        return dataFactory.getOWLObjectMinCardinality(
                minCardinality.getCardinality(),
                minCardinality.getProperty(),
                minCardinality.getFiller().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectExactCardinality exactCardinality) {
        return dataFactory.getOWLObjectExactCardinality(
                exactCardinality.getCardinality(),
                exactCardinality.getProperty(),
                exactCardinality.getFiller().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectMaxCardinality maxCardinality) {
        return dataFactory.getOWLObjectMaxCardinality(
                maxCardinality.getCardinality(),
                maxCardinality.getProperty(),
                maxCardinality.getFiller().accept(new ConceptToDual(dataFactory, conceptsToReplace)));
    }

    @Override
    public OWLClassExpression visit(OWLObjectOneOf nominal) {
        return nominal;
    }

    // TODO diese Methode gibt's doppelt, das ist so noch nicht schön
    private OWLClass getDualClass(OWLClass owlClass) {
        return dataFactory.getOWLClass(
                owlClass.getIRI().getNamespace(),
                "DUAL." + owlClass.getIRI().getRemainder().orElse(""));
    }
}
