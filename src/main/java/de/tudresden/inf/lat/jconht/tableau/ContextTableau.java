package de.tudresden.inf.lat.jconht.tableau;

import de.tudresden.inf.lat.jconht.model.ConceptConverterNormal;
import de.tudresden.inf.lat.jconht.model.ContextOntology;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * This class describes a tableau that can deal with contexts.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextTableau extends Tableau {

    private ContextOntology contextOntology;
    private OWLReasonerFactory reasonerFactory;

    /**
     * This is the standard constructor.
     * <p>
     * It basically copies the information from a given tableau instance to itself.
     *
     * @param tableau       A given tableau instance.
     * @param configuration The configuration used for creating the given tableau instance.
     */
    public ContextTableau(Tableau tableau, ContextOntology contextOntology, Configuration configuration) {

        super(tableau.getInterruptFlag(),
                tableau.getTableauMonitor(),
                tableau.getExistentialsExpansionStrategy(),
                configuration.useDisjunctionLearning,
                tableau.getPermanentDLOntology(),
                tableau.getAdditionalDLOntology(),
                tableau.getParameters());

        this.contextOntology = contextOntology;
        // TODO wozu brauchten wir die ReasonerFactory?
        this.reasonerFactory = new ReasonerFactory();
    }

    @Override
    protected boolean runCalculus() {

        // First run Hypertableau algorithm on meta ontology
        if (super.runCalculus()) {

            // Possibly a model for meta level is found.

            //System.out.println(contextOntology);

            // Debug output
            System.out.println("meta ontology is consistent, following context model is found:");
            binaryTupleTableEntries().forEach(System.out::println);

            // Iterate over all tableau nodes and check whether one of them is not inner consistent.
            Optional<Node> clashNode = getTableauNodes()
                    .filter(this::isNodeInnerInconsistent)
                    .findFirst();

            // Check whether a clash occurred, and initiate backtracking of HermiT if so.
            if (clashNode.isPresent()) {

                // Obtain last entry that speaks about clashNode.
                BinaryTupleTableEntry lastEntryOfClashNode = binaryTupleTableEntries()
                        // Consider only entries that speak about clashNode.
                        .filter(entry -> entry.getNode().equals(clashNode.get()))
                        // Take the last entry, which is present!
                        .reduce((entry1, entry2) -> entry2)
                        .get();

                // Tell HermiT to backtrack to the dependency set associated with clashNode.
                getExtensionManager().setClash(lastEntryOfClashNode.getDependencySet());

                // Perform actual backtracking.
                System.out.println("Doing backtracking because of node " + clashNode.get() + " with " +
                        lastEntryOfClashNode.getDependencySet());
                return runCalculus();

            } else {

                // All nodes are inner consistent.
                return true;
            }

        } else {

            // The meta-ontology is inconsistent, giving up.
            return false;
        }
    }

    /**
     * This method is used to check whether a given tableau node is inner consistent, i.e. the associated object
     * ontology is consistent.
     *
     * @param node A tableau node.
     * @return <code>true</code> iff <code>node</code> is not inner consistent.
     */
    private boolean isNodeInnerInconsistent(Node node) {

        OWLReasoner reasoner = reasonerFactory.createReasoner(
                contextOntology.getObjectOntology(getClassesOfNode(node)));

        boolean result = !reasoner.isConsistent();

        String str;
        Prefixes prefix = new Prefixes();
        prefix.declarePrefix("rosi:","http://www.rosi-project.org/ontologies#");
        prefix.declarePrefix("int:","internal:");
        prefix.declarePrefix("intNom:","internal:nom#http://www.rosi-project.org/ontologies#");
        prefix.declareSemanticWebPrefixes();

        if (!result) {

            System.out.println(String.join("", Collections.nCopies(100, "-")));
            System.out.println("--- object ontology for node " + node + " is consistent, following object model is found:");
            ExtensionTable binTable = ((Reasoner) reasoner).getTableau().getExtensionManager().getBinaryExtensionTable();
            ExtensionTable terTable = ((Reasoner) reasoner).getTableau().getExtensionManager().getTernaryExtensionTable();

            for (Node node1 = ((Reasoner) reasoner).getTableau().getFirstTableauNode(); node1 != null; node1 = node1.getNextTableauNode()) {
                for (int i=0; binTable.getTupleObject(i,1) != null; i++) {
                    if (binTable.getTupleObject(i,1) == node1) {
                        str = ((Concept) binTable.getTupleObject(i,0)).toString(prefix);
                        System.out.println(node1 + "   " + str);
                    }

                }
                System.out.println();
            }
            for (int i=0; terTable.getTupleObject(i,1) != null; i++) {
                str = ((DLPredicate) terTable.getTupleObject(i,0)).toString(prefix);
                System.out.println(terTable.getTupleObject(i,1) + "   " + str + "   " + terTable.getTupleObject(i,2));
            }
            System.out.println(String.join("", Collections.nCopies(100, "-")));
        }

        reasoner.dispose();

        return result;
    }

    /**
     * This method returns the set of OWLClassExpressions associated to a given node.
     *
     * @param node A node.
     * @return The set of associated OWLClassExpressions.
     */
    private Set<OWLClass> getClassesOfNode(Node node) {

        Set<OWLClass> result = binaryTupleTableEntries()
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getOWLClassIfAtomicConcept)
                .collect(Collectors.toSet());
        System.out.println("result = " + result);
        return result;
    }

    /**
     * @return A stream of HermiT’s extension manager’s binary tuple table.
     */
    private Stream<BinaryTupleTableEntry> binaryTupleTableEntries() {

        return StreamSupport.stream(new BinaryTupleTableEntry(0).spliterator(), false);
    }

    /**
     * This class encapsulates an entry in HermiT’s extension manager’s binary tuple table.
     */
    private class BinaryTupleTableEntry implements Iterator<BinaryTupleTableEntry>, Iterable<BinaryTupleTableEntry> {

        private int tupleIndex;
        private Concept concept;
        private Node node;
        private DependencySet dependencySet;

        /**
         * This is the standard constructor.
         *
         * @param tupleIndex The tuple index of the entry.
         */
        public BinaryTupleTableEntry(int tupleIndex) {

            update(tupleIndex);
        }

        /**
         * This function sets the entry to the one of a given index.
         *
         * @param tupleIndex A tuple index.
         */
        private void update(int tupleIndex) {

            this.tupleIndex = tupleIndex;

            ExtensionTable extensionTable = getExtensionManager().getBinaryExtensionTable();

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

            OWLClassExpression classExpression = null;
            OWLDataFactory dataFactory = contextOntology.getDataFactory();

            // return concept.accept(new HermitConceptConverter(dataFactory));

            classExpression = ConceptConverterNormal.toOWLClassExpression(concept,dataFactory);

            return classExpression;
        }

        /**
         *  This method retrieves the concept of an BinaryTupleTableEntry as an OWLClass if the concept is an
         *  AtomicConcept, otherwise it returns <code>null</code>.
         * @return
         */
        public OWLClass getOWLClassIfAtomicConcept() {

            OWLClass owlClass = null;
            OWLDataFactory dataFactory = contextOntology.getDataFactory();

            // The following code is necessary because of legacy HermiT code.
            if (concept instanceof AtomicConcept) {
                owlClass = dataFactory.getOWLClass(
                        IRI.create(((AtomicConcept) concept).getIRI()));
            }

            return owlClass;
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

            return getNode() + "\t" + getClassExpression() + "\t" + getDependencySet();
        }

        @Override
        public BinaryTupleTableEntry next() {

            update(tupleIndex + 1);
            return new BinaryTupleTableEntry(tupleIndex - 1);
        }
    }

    /**
     * This class realises an iterator for HermiT’s tableau nodes.
     */
    private class NodeIterator implements Iterator<Node>, Iterable<Node> {

        private Node node;

        /**
         * The standard constructor initialising the internal state with the first tableau node.
         */
        public NodeIterator() {

            node = getFirstTableauNode();
        }

        @Override
        public boolean hasNext() {

            return node != null;
        }

        @Override
        public Node next() {

            Node result = node;
            node = node.getNextTableauNode();
            return result;
        }

        @Override
        public Iterator<Node> iterator() {

            return this;
        }
    }

    /**
     * @return A stream of HermiT's tableau nodes.
     */
    private Stream<Node> getTableauNodes() {

        return StreamSupport.stream(new NodeIterator().spliterator(), false);
    }
}
