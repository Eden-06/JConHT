package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.model.*;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public interface HermitObjectVisitor<O> {



    /**
     * Gets the default return value for this visitor. By default, the default
     * is {@code null}
     *
     * @param object
     *        The object that was visited.
     * @param <T>
     *        type visited
     * @return The default return value
     */
    @SuppressWarnings("null")
    default <T> O doDefault(@SuppressWarnings("unused") T object) {
        // no other way to provide a default implementation
        return null;
    }

    // TODO diese Methoden habe ich analog zu OWLAxiomVisitorEx<O> erstellt, brauchen wir die hier?

    default O visit(Concept concept) {
        return doDefault(concept);
    }

    default O visit(AtomicConcept concept) {
        return doDefault(concept);
    }

    default O visit(AtomicNegationConcept concept) {
        return doDefault(concept);
    }

    default O visit(AtLeastConcept concept) {
        return doDefault(concept);
    }

    default O visit(AtLeast concept) {
        return doDefault(concept);
    }

    default O visit(Role role) {
        return doDefault(role);
    }

    default O visit(AtomicRole role) {
        return doDefault(role);
    }

    default O visit(InverseRole role) {
        return doDefault(role);
    }


}
