package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.List;
import java.util.stream.Collectors;

import static de.tudresden.inf.lat.jconht.model.TupleTableEntries.*;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class PreModel {

    private List<BinaryTupleTableEntry> binEntries;
    private List<TernaryTupleTableEntry> terEntries;

    public PreModel(ExtensionManager extensionManager, OWLDataFactory dataFactory) {

        binEntries = binaryTupleTableEntries(extensionManager, dataFactory).collect(Collectors.toList());
        terEntries = ternaryTupleTableEntries(extensionManager).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

//        binEntries.forEach(x -> builder.append(x).append("\n"));
//        terEntries.forEach(x -> builder.append(x).append("\n"));

        binEntries.forEach(x -> builder
                .append(x.getNode())
                .append('\t')
                .append(x.getClassExpression())
                .append("\n"));
        terEntries.forEach(x -> {
            builder.append(x.getNodeFrom());
            builder.append('\t');
            if (x.getRole().isPresent()) {
                builder.append(x.getRole().get());
                builder.append('\t');
            } else if (x.getInequality().isPresent()) {
                builder.append("!=");
                builder.append('\t');
            }
            builder.append(x.getNodeTo());
            builder.append('\n');
        });

        return builder.toString();

    }

    public String toStringWithDependencySet() {
        StringBuilder builder = new StringBuilder();
        binEntries.forEach(x -> builder.append(x).append("\n"));
        terEntries.forEach(x -> builder.append(x).append("\n"));
        return builder.toString();
    }
}
