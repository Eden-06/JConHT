package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Set;
import java.util.stream.Stream;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class Type {

    private Set<OWLClass> positiveConcepts;
    private Set<OWLClass> negativeConcepts;

    public Type(Set<OWLClass> positiveConcepts, Set<OWLClass> negativeConcepts) {
        this.positiveConcepts = positiveConcepts;
        this.negativeConcepts = negativeConcepts;
    }

    public Stream<OWLClass> positiveConcepts() {
        return positiveConcepts.stream();
    }

    public Stream<OWLClass> negativeConcepts() {
        return negativeConcepts.stream();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("pos: ").append(positiveConcepts).append('\n');
        builder.append("neg: ").append(negativeConcepts).append('\n');
        return builder.toString();
    }
}
