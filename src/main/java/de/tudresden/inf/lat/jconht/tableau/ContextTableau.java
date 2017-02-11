package de.tudresden.inf.lat.jconht.tableau;

import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.model.Type;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.AsOWLClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasClassesInSignature;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.tudresden.inf.lat.jconht.model.TupleTableEntries.*;


/**
 * This class describes a tableau that can deal with contexts.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextTableau extends Tableau {

    private static final boolean debugOutput = false;

    private final Predicate<OWLClass> classIsAbstractedMetaConcept;
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
        this.reasonerFactory = new ReasonerFactory();

        classIsAbstractedMetaConcept =
                owlClass -> contextOntology.outerAbstractedMetaConcepts().anyMatch(metaConcept -> metaConcept.equals(owlClass));
    }


//    @Override
//    public void clear() {
//        super.clear();
//        contextOntology.clear();
//    }

    @Override
    protected boolean runCalculus() {

        // First run Hypertableau algorithm on meta ontology
        if (super.runCalculus()) {

            // Possibly a model for meta level is found.
            if (debugOutput) {
                System.out.println(contextOntology);
                System.out.println("meta ontology is consistent, following context model is found:");
                binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                        .forEach(System.out::println);
                ternaryTupleTableEntries(getExtensionManager()).forEach(System.out::println);
            }

            if (contextOntology.containsRigidNames()) {
                isAdmissibleWithRigid().ifPresent(clashSet -> getExtensionManager().setClash(clashSet));
            } else {
                isAdmissibleWithoutRigid().ifPresent(clashSet -> getExtensionManager().setClash(clashSet));
            }

            if (getExtensionManager().containsClash()) {
                // Perform actual backtracking.
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
     * This method returns the set of OWLClasses that are abstracted meta concepts and must hold for a given node.
     *
     * @param node A node.
     * @return The set of positive abstracted meta concepts as {@code Stream<OWLClass>}.
     */
    private Stream<OWLClass> positiveMetaConceptsOfNode(Node node) {
        // TODO oder lieber streams zurückgeben?
        return binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getClassExpression)
                .filter(AsOWLClass::isOWLClass)
                .map(AsOWLClass::asOWLClass)
                .filter(classIsAbstractedMetaConcept);
    }

    @Deprecated
    private Set<OWLClass> getPositiveMetaConceptsOfNode(Node node) {
        // TODO oder lieber streams zurückgeben?
        return binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getClassExpression)
                .filter(AsOWLClass::isOWLClass)
                .map(AsOWLClass::asOWLClass)
                .filter(classIsAbstractedMetaConcept)
                .collect(Collectors.toSet());
    }

    /**
     * This method returns the set of OWLClasses that are abstracted meta concepts and must NOT hold for a given node.
     *
     * @param node A node.
     * @return The set of negative abstracted meta concepts as {@code Stream<OWLClass>}.
     */
    private Stream<OWLClass> negativeMetaConceptsOfNode(Node node) {
        // TODO Test mit negativem metakonzept cls:C, dass kein outerAbstract ist
        return binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getClassExpression)
                .filter(owlClassExpression ->
                        owlClassExpression.getClassExpressionType().equals(ClassExpressionType.OBJECT_COMPLEMENT_OF))
                .flatMap(HasClassesInSignature::classesInSignature);
    }

    @Deprecated
    private Set<OWLClass> getNegativeMetaConceptsOfNode(Node node) {
        return binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getClassExpression)
                .filter(owlClassExpression ->
                        owlClassExpression.getClassExpressionType().equals(ClassExpressionType.OBJECT_COMPLEMENT_OF))
                .flatMap(HasClassesInSignature::classesInSignature)
                .collect(Collectors.toSet());
    }

    private Type typeOfNode(Node node) {

        return new Type(
                positiveMetaConceptsOfNode(node).collect(Collectors.toSet()),
                negativeMetaConceptsOfNode(node).collect(Collectors.toSet()));
    }

    /**
     * This class checks for ontologies without rigid names whether the induced object types are admissible.
     * If so, an empty optional is returned; if not, a dependency set, used for backtracking, is returned.
     *
     * @return Optional.empty() if admissible, a dependency set otherwise.
     */
    private Optional<DependencySet> isAdmissibleWithoutRigid() {

        // Iterate over all tableau nodes and check whether one of them is not inner consistent.
        Optional<Node> clashNode = tableauNodes()
                .filter(node -> {
                    OWLReasoner reasoner = reasonerFactory.createReasoner(
                            contextOntology.getObjectOntology(Arrays.asList(typeOfNode(node))));
                    boolean result = !reasoner.isConsistent();

                    if (debugOutput && result) {
                        System.out.println(String.join("", Collections.nCopies(100, "-")));
                        System.out.println("--- object ontology for node " + node + " is consistent, following object model is found:");
                        binaryTupleTableEntries(((Reasoner) reasoner).getTableau().getExtensionManager(),
                                contextOntology.getDataFactory())
                                .forEach(System.out::println);
                        ternaryTupleTableEntries(((Reasoner) reasoner).getTableau().getExtensionManager())
                                .forEach(System.out::println);
                        System.out.println(String.join("", Collections.nCopies(100, "-")));
                    }

                    return result;
                })
                .findFirst();

        // Check whether a clash occurred, and return dependency set if so.
        if (clashNode.isPresent()) {

            // Obtain the dependency set of last entry that speaks about clashNode.
            DependencySet clashSet =
                    binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                            // Consider only entries that speak about clashNode.
                            .filter(entry -> entry.getNode().equals(clashNode.get()))
                            // Take the last entry, which is present!
                            .reduce((entry1, entry2) -> entry2)
                            .get()
                            .getDependencySet();

            if (debugOutput) {
                System.out.println("Node " + clashNode.get() + " is not admissible. Returning dependency set: " +
                        clashSet);
            }

            return Optional.of(clashSet);

        } else {
            return Optional.empty();
        }
    }

    // TODO not implemented yet!!!
    private Optional<DependencySet> isAdmissibleWithRigid() {

        //tableauNodes().map(node -> typeOfNode(node)).

        return isAdmissibleWithoutRigid();
    }

    /**
     * @return A stream of HermiT's tableau nodes.
     */
    private Stream<Node> tableauNodes() {

        return StreamSupport.stream(new NodeIterator().spliterator(), false);
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
}
