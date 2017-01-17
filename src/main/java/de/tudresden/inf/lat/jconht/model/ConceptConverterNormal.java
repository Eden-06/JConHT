package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.*;

/**
 * This class contains the public method <code>toOWLClassExpression</code> which takes a HermiT concept
 * and converts it to an OWL class expression.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ConceptConverterNormal {

    public static OWLClassExpression toOWLClassExpression(Concept concept, OWLDataFactory dataFactory){

        // TODO Tests are missing

        // TODO Hätte ich hier auch irgendwie das Visitor-Pattern verwenden sollen? Falls ja, wie? Wäre dazu ein HermiTConceptVisitor notwendig, den es nicht gibt?
        // Also wildes instanceof und Type-Casting oder die Hermit-Klassen Concept und Role um eine accept-Methode erweitern?

        OWLClassExpression classExpression = null;

        if (concept instanceof AtomicConcept) {
            classExpression = dataFactory.getOWLClass(
                    IRI.create(((AtomicConcept) concept).getIRI()));
        } else if (concept instanceof AtomicNegationConcept) {
            classExpression = dataFactory.getOWLObjectComplementOf(dataFactory.getOWLClass(
                    IRI.create(((AtomicNegationConcept) concept).getNegatedAtomicConcept().getIRI())));
        } else if (concept instanceof AtLeastConcept) {
            classExpression = dataFactory.getOWLObjectMinCardinality(
                    ((AtLeastConcept) concept).getNumber(),
                    toOWLObjectPropertyExpression(((AtLeastConcept) concept).getOnRole(), dataFactory),
                    toOWLClassExpression(((AtLeastConcept) concept).getToConcept(), dataFactory));
        } else if (concept instanceof AtLeast) {
            classExpression = dataFactory.getOWLObjectMinCardinality(
                    ((AtLeast) concept).getNumber(),
                    toOWLObjectPropertyExpression(((AtLeast) concept).getOnRole(), dataFactory));
        }

        return classExpression;
    }

    public static OWLObjectPropertyExpression toOWLObjectPropertyExpression(Role role, OWLDataFactory dataFactory) {

        // TODO Tests are missing

        OWLObjectPropertyExpression objectPropertyExpression = null;

        if (role instanceof AtomicRole) {
            objectPropertyExpression = dataFactory.getOWLObjectProperty(
                    IRI.create(((AtomicRole) role).getIRI()));
        } else if (role instanceof InverseRole) {
            objectPropertyExpression = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(
                    IRI.create(((InverseRole) role).getInverseOf().getIRI())));
        }

        return objectPropertyExpression;
    }
}
