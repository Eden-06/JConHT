package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This class is used to generate the negation of a given OWLAxiom.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegator implements OWLAxiomVisitorEx<OWLAxiom> {

    private OWLDataFactory dataFactory;
    private ConceptNegator conceptNegator;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory used to generate fresh individuals when necessary
     */
    public AxiomNegator(OWLDataFactory dataFactory) {

        this.dataFactory = dataFactory;
        this.conceptNegator = new ConceptNegator(dataFactory);
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
                dataFactory.getOWLObjectIntersectionOf(
                        axiom.getSubClass(),
                        axiom.getSuperClass().accept(conceptNegator)),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {

        // ¬(¬r(a, b)) ⟹ r(a, b)

        return dataFactory.getOWLObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {

        // ¬(DisjointUnion(C, D, E)) ⟹ ((C ⊓ D) ⊔ (C ⊓ E) ⊔ (D ⊓ E))(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        axiom.asPairwiseAxioms().stream()
                                .map(a -> a.operands()
                                        .reduce((c, d) -> dataFactory.getOWLObjectIntersectionOf(c, d))
                                        // This get() does not fail, because of asPairwiseAxioms().
                                        .get())),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {

        // ¬(Dom(r) = C) ⟹ (∃r.⊤ ⊓ ¬C)(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(
                        dataFactory.getOWLObjectSomeValuesFrom(
                                axiom.getProperty(),
                                dataFactory.getOWLThing()),
                        axiom.getDomain().accept(conceptNegator)),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {

        // ¬(Ran(r) = C) ⟹ (∃r.¬C)(x_new)

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectSomeValuesFrom(
                        axiom.getProperty(),
                        axiom.getRange().accept(conceptNegator)),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {

        // ¬(r(a, b)) ⟹ ¬r(a, b)

        return dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(
                axiom.getProperty(), axiom.getSubject(), axiom.getObject());
    }

    @Override
    public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {

        // ¬(DisjointUnionOf(A, B, C, D)) ⟹ ((A ⊓ ¬B ⊓ ¬C ⊓ ¬D) ⊔ (B ⊓ C) ⊔ (B ⊓ D) ⊔ (C ⊓ D))(x_new)

        OWLClassExpression firstDisjunct = dataFactory.getOWLObjectIntersectionOf(
                Stream.concat(Stream.of(axiom.getOWLClass()),
                        axiom.classExpressions()
                                .map(c -> c.accept(conceptNegator))));

        Stream<OWLClassExpression> remainingDisjuncts =
                axiom.getOWLDisjointClassesAxiom().asPairwiseAxioms().stream()
                        .map(a -> a.operands()
                                .reduce((c, d) -> dataFactory.getOWLObjectIntersectionOf(c, d))
                                // This get() does not fail, because of asPairwiseAxioms().
                                .get());

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        Stream.concat(Stream.of(firstDisjunct), remainingDisjuncts)),
                dataFactory.getOWLAnonymousIndividual());
    }

    @Override
    public OWLAxiom visit(OWLClassAssertionAxiom axiom) {

        // ¬(C(a)) ⟹ ¬C(a)

        return dataFactory.getOWLClassAssertionAxiom(
                axiom.getClassExpression().accept(conceptNegator),
                axiom.getIndividual());
    }

    @Override
    public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {

        // ¬(C ≡ D ≡ E) ⟹ ((C ⊓ ¬D) ⊔ (¬C ⊓ D) ⊔ (D ⊓ ¬E) ⊔ (¬D ⊓ E) ⊔ (C ⊓ ¬E) ⊔ (¬C ⊓ E))(x_new)

        // This function maps C ≡ D to [(C ⊓ ¬D), (¬C ⊓ D)].
        Function<OWLEquivalentClassesAxiom, Stream<Optional<OWLClassExpression>>> mapper =
                a -> Stream.of(
                        a.operands().reduce((c, d) ->
                                dataFactory.getOWLObjectIntersectionOf(c, d.accept(conceptNegator))),
                        a.operands().reduce((c, d) ->
                                dataFactory.getOWLObjectIntersectionOf(c.accept(conceptNegator), d)));

        return dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectUnionOf(
                        axiom.asPairwiseAxioms().stream()
                                .flatMap(mapper)
                                // This get() does not fail, because of asPairwiseAxioms().
                                .map(Optional::get)),
                dataFactory.getOWLAnonymousIndividual());
    }
}
