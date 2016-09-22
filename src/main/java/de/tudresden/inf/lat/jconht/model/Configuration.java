package de.tudresden.inf.lat.jconht.model;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class Configuration {

    private boolean useUNA;
    private int debugOutput;
    private boolean useDualization;
    private boolean useRepletion;

    /**
     * Constructor that sets all boolean fields accordingly.
     * @param useUNA if <code>true</code>, the unique name assumption is used.
     * @param debugOutput higher number means more verbose debug output. 0 means none at all.
     * @param useDualization if <code>true</code>, then the dualized axioms are added to the context ontology.
     * @param useRepletion if <code>true</code>, then the repletion axioms for the context ontology are added.
     */
    public Configuration(boolean useUNA,
                         int debugOutput,
                         boolean useDualization,
                         boolean useRepletion) {
        this.useUNA = useUNA;
        this.debugOutput = debugOutput;
        this.useDualization = useDualization;
        this.useRepletion = useRepletion;
    }

    /**
     * Default constructor, that uses the UNA, produces no debug output, adds repletion axioms, but not dualized axioms.
     */
    public Configuration() {
        this(true, 0, false, true);
    }

    /**
     * Simplified constructor that sets the default values for <code>useUNA</code>, <code>useDualization</code> and
     * <code>useRepletion</code>.
     * @param debugOutput if <code>true</code>, then debug output is produced at several places.
     */
    public Configuration(int debugOutput) {
        this(true, debugOutput, false, true);
    }



    public boolean useUNA() {
        return useUNA;
    }

    public int debugOutput() {
        return debugOutput;
    }

    public boolean useDualization() {
        return useDualization;
    }

    public boolean useRepletion() {
        return useRepletion;
    }
}
