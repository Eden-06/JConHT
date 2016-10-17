package de.tudresden.inf.lat.jconht.model;

/**
 * This runtime exception is thrown when the AxiomNegator encounters an axiom for which there exists no visit method
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class UnhandledAxiomTypeException extends RuntimeException {
    public UnhandledAxiomTypeException(String message) {
        super(message);
    }
}
