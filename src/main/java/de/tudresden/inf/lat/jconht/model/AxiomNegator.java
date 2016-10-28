package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to generate the negation of a given OWLAxiom.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegator implements OWLAxiomVisitorEx<OWLAxiom> {

    private OWLDataFactory dataFactory;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory used to generate fresh individuals when necessary
     */
    public AxiomNegator(OWLDataFactory dataFactory) {

        this.dataFactory = dataFactory;
    }

    @Override
    public <T> OWLAxiom doDefault(T object) {

        // This should never happen!
        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + object.getClass());
    }

    @Override
    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {

        // ¬(C(a)) ⟹ (¬C(a))

        return dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression().accept(new ConceptNegator(dataFactory)),
                axiom.getIndividual());
    }

    @Override
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {

        // ¬(C ⊑ D) ⟹ (C ∧ ¬D)(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(axiom.getSubClass(),
                        axiom.getSuperClass().accept(new ConceptNegator(dataFactory))),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLSameIndividualAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }

    @Override
    public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
        // ¬(C ≡ D) ⟹ ((C ∧ ¬D)∨(¬C ∧ D))(x_new)

        if (axiom.operands().count() != 2)
            throw new UnhandledAxiomTypeException("Equivalent class axioms are only allowed with two operands.");

        List<OWLClassExpression> classExpressions = axiom.operands().collect(Collectors.toList());

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        dataFactory.getOWLObjectIntersectionOf(
                                classExpressions.get(0),
                                classExpressions.get(1).accept(new ConceptNegator(dataFactory))),
                        dataFactory.getOWLObjectIntersectionOf(
                                classExpressions.get(0).accept(new ConceptNegator(dataFactory)),
                                classExpressions.get(1))),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }

    @Override
    public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }


}
