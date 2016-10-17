package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

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

}
