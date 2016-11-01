package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Set;
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
    public OWLAxiom visit(OWLSubClassOfAxiom axiom) {

        // ¬(C ⊑ D) ⟹ (C ⊓ ¬D)(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(axiom.getSubClass(),
                        axiom.getSuperClass().accept(new ConceptNegator(dataFactory))),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(
                        dataFactory.getOWLObjectMinCardinality(1, axiom.getProperty()),
                        axiom.getDomain().accept(new ConceptNegator(dataFactory))),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectMinCardinality(1,
                        axiom.getProperty(),
                        axiom.getRange().accept(new ConceptNegator(dataFactory))),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {

        return dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {

        // ¬(A DisjointUnionOf B,C,D) ⟹ ((A ⊓ ¬B ⊓ ¬C ⊓ ¬D) ⊔ (B ⊓ C) ⊔ (B ⊓ D) ⊔ (C ⊓ D))(x_new)

        Set conjuncts = axiom.classExpressions()
                .map(ce -> ce.accept(new ConceptNegator(dataFactory)))
                .collect(Collectors.toSet());
        conjuncts.add(axiom.getOWLClass());

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        dataFactory.getOWLObjectIntersectionOf(conjuncts),
                        dataFactory.getOWLObjectUnionOf(
                        axiom.getOWLDisjointClassesAxiom().asPairwiseAxioms().stream()
                            .map(a -> a.operands()
                                    .reduce((c, d) -> dataFactory.getOWLObjectIntersectionOf(c, d))
                                    .get()))),
                dataFactory.getOWLAnonymousIndividual()
        );
    }

    @Override
    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {

        // ¬(C(a)) ⟹ (¬C(a))

        return dataFactory.getOWLClassAssertionAxiom(axiom.getClassExpression().accept(new ConceptNegator(dataFactory)),
                axiom.getIndividual());
    }

    @Override
    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {

        // ¬(C ≡ D) ⟹ ((C ⊓ ¬D) ⊔ (¬C ⊓ D))(x_new)
        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        axiom.asPairwiseAxioms().stream()
                                .map(a -> a.operands()
                                        .reduce((c, d) -> dataFactory.getOWLObjectUnionOf(
                                                dataFactory.getOWLObjectIntersectionOf(
                                                        c,
                                                        d.accept(new ConceptNegator(dataFactory))),
                                                dataFactory.getOWLObjectIntersectionOf(
                                                        c.accept(new ConceptNegator(dataFactory)),
                                                        d)))
                                        // This get() does not fail.
                                        .get())),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {

        // ¬(DisjointUnion(C,D,E) ⟹ ((C ⊓ D) ⊔ (C ⊓ E) ⊔ (D ⊓ E))(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        axiom.asPairwiseAxioms().stream()
                                .map(a -> a.operands()
                                        .reduce((c, d) -> dataFactory.getOWLObjectIntersectionOf(c, d))
                                        // This get() does not fail.
                                        .get())),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLSameIndividualAxiom axiom) {

        throw new UnhandledAxiomTypeException("Unknown axiom type in AxiomNegator: " + axiom.getAxiomType());
    }
}
