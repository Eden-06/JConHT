package de.tudresden.inf.lat.jconht.model;

/**
 * This runtime exception is thrown when the <code>HermitConceptConverter</code>
 * encounters an HermiT concept for which there exists no visit method.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class UnhandledHermiTObjectException extends RuntimeException {

    /**
     * This is the standard constructor.
     *
     * @param message The message associated to the cause of the exception.
     */
    public UnhandledHermiTObjectException(String message) {

        super(message);
    }
}