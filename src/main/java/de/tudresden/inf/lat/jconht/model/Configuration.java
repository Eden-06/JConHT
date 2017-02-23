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

    public Configuration() {
        useUNA = true;
        debugOutput = false;
    }

    public Configuration(boolean useUNA, boolean debugOutput) {
        this.useUNA = useUNA;
        this.debugOutput = debugOutput;
    }

    public boolean useUNA() {
        return useUNA;
    }

    public boolean debugOutput() {
        return debugOutput;
    }
}
