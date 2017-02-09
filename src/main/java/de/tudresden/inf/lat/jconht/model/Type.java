package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.tableau.Node;
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
    private Node node;

    public Type(Set<OWLClass> positiveConcepts, Set<OWLClass> negativeConcepts, Node node) {
        this.positiveConcepts = positiveConcepts;
        this.negativeConcepts = negativeConcepts;
        this.node = node;

    }

    public Stream<OWLClass> positiveConcepts() {
        return positiveConcepts.stream();
    }

    public Stream<OWLClass> negativeConcepts() {
        return negativeConcepts.stream();
    }

    public Node getNode() {
        return node;
    }
}
