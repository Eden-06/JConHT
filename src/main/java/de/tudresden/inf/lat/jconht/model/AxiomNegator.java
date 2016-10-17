package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

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
        return dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression().getObjectComplementOf(),
                axiom.getIndividual());
    }

    @Override
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {

        // ¬(C ⊑ D) ⟹ (C ∧ ¬D)(x_new)
        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(axiom.getSubClass(),
                        axiom.getSuperClass().getObjectComplementOf()),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLSameIndividualAxiom axiom) {

        // ¬(s ≈ t) ⟹ (s ≉ t)
        // TODO ist nur korrekt, wenn es nur zwei Individuals sind
        return dataFactory.getOWLDifferentIndividualsAxiom(axiom.individuals().collect(Collectors.toSet()));
    }

    @Override
    public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {

        // ¬(s ≉ t) ⟹ (s ≈ t)
        // TODO ist nur korrekt, wenn es nur zwei Individuals sind
        return dataFactory.getOWLSameIndividualAxiom(axiom.individuals().collect(Collectors.toSet()));
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }
}
