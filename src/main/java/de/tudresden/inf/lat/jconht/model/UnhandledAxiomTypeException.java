package de.tudresden.inf.lat.jconht.model;

/**
 * This runtime exception is thrown when the <code>AxiomNegator</code>
 * encounters an axiom for which there exists no visit method.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class UnhandledAxiomTypeException extends RuntimeException {

    /**
     * This is the standard constructor.
     *
     * @param message The message associated to the cause of the exception.
     */
    public UnhandledAxiomTypeException(String message) {

        super(message);
    }
}
