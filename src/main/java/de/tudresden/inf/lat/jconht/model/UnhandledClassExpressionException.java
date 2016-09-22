package de.tudresden.inf.lat.jconht.model;

/**
 * This runtime exception is thrown when the <code>ConceptNegator</code>
 * encounters a class expression for which there exists no visit method.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class UnhandledClassExpressionException extends RuntimeException {

    /**
     * This is the standard constructor.
     *
     * @param message The message associated to the cause of the exception.
     */
    public UnhandledClassExpressionException(String message) {

        super(message);
    }
}
