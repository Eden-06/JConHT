package de.tudresden.inf.lat.jconht.model;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class Configuration {

    private boolean useUNA;
    private boolean debugOutput;
    private boolean useDualization;
    private boolean useRepletion;

    public Configuration() {
        useUNA = true;
        debugOutput = false;
        useDualization = false;
        useRepletion = true;
    }

    public Configuration(boolean useUNA,
                         boolean debugOutput,
                         boolean useDualization,
                         boolean useRepletion) {
        this.useUNA = useUNA;
        this.debugOutput = debugOutput;
        this.useDualization = useDualization;
        this.useRepletion = useRepletion;
    }

    public boolean useUNA() {
        return useUNA;
    }

    public boolean debugOutput() {
        return debugOutput;
    }

    public boolean useDualization() {
        return useDualization;
    }

    public boolean useRepletion() {
        return useRepletion;
    }
}
