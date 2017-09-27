package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class TupleTableEntries {



    /**
     * @return A stream of HermiT’s extension manager’s binary tuple table.
     */
    public static Stream<BinaryTupleTableEntry> binaryTupleTableEntries(ExtensionManager extensionManager,
                                                                        OWLDataFactory dataFactory) {

        return StreamSupport.stream(new BinaryTupleTableEntry(0, extensionManager, dataFactory).spliterator(), false);
    }

    /**
     * @return A stream of HermiT’s extension manager’s ternary tuple table.
     */
    public static Stream<TernaryTupleTableEntry> ternaryTupleTableEntries(ExtensionManager extensionManager) {

        return StreamSupport.stream(new TernaryTupleTableEntry(0, extensionManager).spliterator(), false);
    }

    /**
     * This class encapsulates an entry in HermiT’s extension manager’s binary tuple table.
     */
    public static class BinaryTupleTableEntry
            implements Iterator<BinaryTupleTableEntry>, Iterable<BinaryTupleTableEntry> {

        private int tupleIndex;
        private Concept concept;
        private Node node;
        private DependencySet dependencySet;
        private ExtensionManager extensionManager;
        private OWLDataFactory dataFactory;

        /**
         * This is the standard constructor.
         *
         * @param tupleIndex The tuple index of the entry.
         */
        public BinaryTupleTableEntry(int tupleIndex, ExtensionManager extensionManager, OWLDataFactory dataFactory) {
            this.extensionManager = extensionManager;
            this.dataFactory = dataFactory;
            update(tupleIndex);
        }

        /**
         * This function sets the entry to the one of a given index.
         *
         * @param tupleIndex A tuple index.
         */
        private void update(int tupleIndex) {

            this.tupleIndex = tupleIndex;

            ExtensionTable extensionTable = extensionManager.getBinaryExtensionTable();

            // The following code is necessary because of legacy HermiT code.
            this.concept = (Concept) extensionTable.getTupleObject(tupleIndex, 0);
            this.node = (Node) extensionTable.getTupleObject(tupleIndex, 1);
            this.dependencySet = extensionTable.getDependencySet(tupleIndex);
        }

        /**
         * @return The tuple index of the entry.
         */

        public int getTupleIndex() {

            return tupleIndex;
        }

        /**
         * @return The concept of the entry.
         */
        public Concept getConcept() {

            return concept;
        }

        /**
         * @return The node of the entry.
         */
        public Node getNode() {

            return node;
        }

        /**
         * @return The dependency set of the entry.
         */
        public DependencySet getDependencySet() {

            return dependencySet;
        }


        public OWLClassExpression getClassExpression() {

            //return concept.accept(new HermitConceptConverter(contextOntology.getDataFactory()));
            return ConceptConverterNormal.toOWLClassExpression(concept, dataFactory);
        }

        @Override
        public boolean hasNext() {

            return concept != null;
        }

        @Override
        public Iterator<BinaryTupleTableEntry> iterator() {

            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            switch (getNode().getNodeType()) {
                case NAMED_NODE:
                    builder.append("Named\t");
                    break;
                case NI_NODE:
                    builder.append("NI   \t");
                    break;
                case GRAPH_NODE:
                    builder.append("Graph\t");
                    break;
                case TREE_NODE:
                    builder.append("Tree \t");
                    break;
                case CONCRETE_NODE:
                    builder.append("Concrete ");
                    break;
                case ROOT_CONSTANT_NODE:
                    builder.append("Root \t");
            }
            builder.append(getNode());
            if (getNode().isMerged()) {
                builder.append("->").append(getNode().getMergedInto());
            } else {
                builder.append("    ");
            }
            if (getNode().isBlocked()) {
                builder.append("||").append(getNode().getBlocker());
            } else {
                builder.append("   ");
            }
            builder.append('\t');
            builder.append(getDependencySet());
            if (getDependencySet().toString().length()<8) {
                builder.append('\t');
            }
            builder.append('\t');
            builder.append(getClassExpression());

            return builder.toString();
        }

        @Override
        public BinaryTupleTableEntry next() {

            update(tupleIndex + 1);
            return new BinaryTupleTableEntry(tupleIndex - 1, extensionManager, dataFactory);
        }
    }

    /**
     * This class encapsulates an entry in HermiT’s extension manager’s ternary tuple table.
     */
    public static class TernaryTupleTableEntry
            implements Iterator<TernaryTupleTableEntry>, Iterable<TernaryTupleTableEntry> {

        private int tupleIndex;
        private Optional<Role> role;
        private Optional<Inequality> inequality;
        private Node nodeFrom;
        private Node nodeTo;
        private DependencySet dependencySet;
        private ExtensionManager extensionManager;

        /**
         * This is the standard constructor.
         *
         * @param tupleIndex The tuple index of the entry.
         */
        public TernaryTupleTableEntry(int tupleIndex, ExtensionManager extensionManager) {
            this.extensionManager = extensionManager;
            update(tupleIndex);
        }

        /**
         * This function sets the entry to the one of a given index.
         *
         * @param tupleIndex A tuple index.
         */
        private void update(int tupleIndex) {

            this.tupleIndex = tupleIndex;

            ExtensionTable extensionTable = extensionManager.getTernaryExtensionTable();

            // The following code is necessary because of legacy HermiT code.
            if (extensionTable.getTupleObject(tupleIndex, 0) instanceof Role) {
                role = Optional.of((Role) extensionTable.getTupleObject(tupleIndex, 0));
                inequality = Optional.empty();
            } else if (extensionTable.getTupleObject(tupleIndex, 0) instanceof Inequality) {
                role = Optional.empty();
                inequality = Optional.of((Inequality) extensionTable.getTupleObject(tupleIndex, 0));
            } else {
                role = null;
                inequality = null;
            }
            nodeFrom = (Node) extensionTable.getTupleObject(tupleIndex, 1);
            nodeTo = (Node) extensionTable.getTupleObject(tupleIndex, 2);
            dependencySet = extensionTable.getDependencySet(tupleIndex);
        }

        /**
         * @return The tuple index of the entry.
         */
        public int getTupleIndex() {

            return tupleIndex;
        }

        /**
         * @return The role of the entry.
         */
        public Optional<Role> getRole() {

            return role;
        }

        /**
         * @return The inequality of the entry.
         */
        public Optional<Inequality> getInequality() {

            return inequality;
        }

        /**
         * @return The first node of the entry.
         */
        public Node getNodeTo() {

            return nodeTo;
        }

        /**
         * @return The second node of the entry.
         */
        public Node getNodeFrom() {

            return nodeFrom;
        }

        /**
         * @return The dependency set of the entry.
         */
        public DependencySet getDependencySet() {

            return dependencySet;
        }


        @Override
        public boolean hasNext() {

            return role != null;
        }

        @Override
        public Iterator<TernaryTupleTableEntry> iterator() {

            return this;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();

            builder.append(getNodeFrom());
            builder.append('\t');
            if (getRole().isPresent()) {
                //noinspection OptionalGetWithoutIsPresent
                builder.append(getRole().get());
                builder.append('\t');
            } else if (getInequality().isPresent()) {
                builder.append("!=");
                builder.append('\t');
            }
            builder.append(getNodeTo());
            builder.append('\t');
            builder.append(getDependencySet());

            return builder.toString();
        }

        @Override
        public TernaryTupleTableEntry next() {

            update(tupleIndex + 1);
            return new TernaryTupleTableEntry(tupleIndex - 1, extensionManager);
        }
    }


}
