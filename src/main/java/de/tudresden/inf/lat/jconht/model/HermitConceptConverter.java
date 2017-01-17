package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import static de.tudresden.inf.lat.jconht.model.ConceptConverterNormal.toOWLClassExpression;
import static de.tudresden.inf.lat.jconht.model.ConceptConverterNormal.toOWLObjectPropertyExpression;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class HermitConceptConverter implements HermitObjectVisitor<OWLClassExpression> {

    // TODO Tests

    private OWLDataFactory dataFactory;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory used to generate the OWLClassExpression
     */
    public HermitConceptConverter(OWLDataFactory dataFactory) {

        this.dataFactory = dataFactory;
    }

    @Override
    public <T> OWLClassExpression doDefault(T object) {

        // This should never happen!
        throw new UnhandledHermiTObjectException("Unknown HermiT concept type in HermitConceptConverter: " + object.getClass());
    }

    @Override
    public OWLClass visit(AtomicConcept concept) {

        return dataFactory.getOWLClass(IRI.create(concept.getIRI()));
    }

    @Override
    public OWLClassExpression visit(AtomicNegationConcept concept) {

        return dataFactory.getOWLObjectComplementOf(dataFactory.getOWLClass(
                IRI.create(concept.getNegatedAtomicConcept().getIRI())));
    }

    @Override
    public OWLClassExpression visit(AtLeastConcept concept) {

        return dataFactory.getOWLObjectMinCardinality(
                concept.getNumber(),
                toOWLObjectPropertyExpression(concept.getOnRole(), dataFactory),
                toOWLClassExpression(concept.getToConcept(), dataFactory));
    }

    @Override
    public OWLClassExpression visit(AtLeast concept) {

        return dataFactory.getOWLObjectMinCardinality(
                concept.getNumber(),
                toOWLObjectPropertyExpression(concept.getOnRole(), dataFactory));
    }

}
