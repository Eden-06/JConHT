package de.tudresden.inf.lat.jconht.tableau;

import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.model.PreModel;
import de.tudresden.inf.lat.jconht.model.Type;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.*;
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

    private final Predicate<OWLClass> classIsAbstractedMetaConcept;
    private ContextOntology contextOntology;
    private OWLReasonerFactory reasonerFactory;
    private int debugOutput;

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
        this.debugOutput = contextOntology.getConfiguration().debugOutput();

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

        // Cycle through all consistent meta ABoxes (PreModels). If any of those is admissible, the context ontology is
        // consistent, otherwise if all are inadmissible, the context ontology is inconsistent.
        return consistentInterpretations().filter(this::isAdmissible).findAny().isPresent();
    }


    /**
     * This method returns the set of OWLClasses that are abstracted meta concepts and must hold for a given node.
     *
     * @param node A node.
     * @return The set of positive abstracted meta concepts as {@code Stream<OWLClass>}.
     */
    public Stream<OWLClass> positiveMetaConceptsOfNode(Node node) {
        // TODO oder lieber set zurückgeben?
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
    public Stream<OWLClass> negativeMetaConceptsOfNode(Node node) {
        return binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                .filter(entry -> entry.getNode().equals(node))
                .map(BinaryTupleTableEntry::getClassExpression)
                .filter(owlClassExpression ->
                        owlClassExpression.getClassExpressionType().equals(ClassExpressionType.OBJECT_COMPLEMENT_OF))
                .flatMap(HasClassesInSignature::classesInSignature)
                .filter(classIsAbstractedMetaConcept);
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

        Type type = new Type(
                positiveMetaConceptsOfNode(node).collect(Collectors.toSet()),
                negativeMetaConceptsOfNode(node).collect(Collectors.toSet()));
        if (debugOutput > 1) {
            System.out.println("types for node " + node + ":\n" + type);
        }
        return type;
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
                    OWLReasoner objectReasoner = reasonerFactory.createReasoner(
                            contextOntology.getObjectOntology(Collections.singletonList(typeOfNode(node))));
                    boolean isInconsistent = !objectReasoner.isConsistent();
                    if (debugOutput > 1) {
                        System.out.println("Object ontology for node " + node + ":");
                        objectReasoner.getRootOntology().axioms().forEach(System.out::println);
                    }
                    if (debugOutput > 1 && !isInconsistent) {
                        System.out.println(String.join("", Collections.nCopies(100, "-")));
                        System.out.println("--- object ontology for node " + node + " is consistent, following object model is found:");
                        binaryTupleTableEntries(((Reasoner) objectReasoner).getTableau().getExtensionManager(),
                                contextOntology.getDataFactory())
                                .forEach(System.out::println);
                        ternaryTupleTableEntries(((Reasoner) objectReasoner).getTableau().getExtensionManager())
                                .forEach(System.out::println);
                        System.out.println(String.join("", Collections.nCopies(100, "-")));
                    }

                    return isInconsistent;
                })
                .findFirst();

        // Check whether a clash occurred, and return dependency set if so.
        if (clashNode.isPresent()) {

            // Obtain the last non-empty dependency set of last entry that speaks about clashNode.
            DependencySet clashSet =
                    binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                            // Consider only entries that speak about clashNode.
                            .filter(entry -> entry.getNode().equals(clashNode.get()))
                            // Consider only entries with non-empty dependency sets
                            .filter(entry -> !entry.getDependencySet().isEmpty())
                            // Take the last entry, which is present!
                            .reduce((entry1, entry2) -> entry2)
                            // If no such element is present, simply take the first entry in the binaryTupleTable (note that it has an empty depency set)
                            .orElse(new BinaryTupleTableEntry(0, getExtensionManager(), contextOntology.getDataFactory()))
                            .getDependencySet();

            if (debugOutput > 1) {
                System.out.println("Node " + clashNode.get() + " is not admissible. Returning dependency set: " +
                        clashSet);
            }

            return Optional.of(clashSet);

        } else {
            return Optional.empty();
        }
    }

//    // TODO not implemented yet!!!
//    private Optional<DependencySet> isAdmissibleWithRigid() {
//
//        //tableauNodes().map(node -> typeOfNode(node)).
//
//        return isAdmissibleWithoutRigid();
//    }

    /**
     * This method checks the admissibility of an input ABox. These ABoxes are premodels returned by the hypertableau
     * calculus on meta level. If there are rigid names, a single (larger) object ontology is generated by using the
     * renaming technique and tested for consistency. If no rigid names are present, for each node an object ontology
     * is generated and tested. There, if all nodes are 'ok', the ABox is admissible.
     *
     * @param model A model for the meta ontology.
     * @return <code>true</code> if ABox is admissible.
     */
    private boolean isAdmissible(PreModel model) {

        if (contextOntology.containsRigidNames()) {
            //if (false) {
            // Generate object ontology with renaming and check for admissibility
            OWLReasoner objectReasoner = reasonerFactory.createReasoner(
                    contextOntology.getObjectOntology(tableauNodes()
                            .map(this::typeOfNode)
                            .collect(Collectors.toList())));
            boolean isConsistent = objectReasoner.isConsistent();
            if (debugOutput > 1) {
                System.out.println("Object ontology with renaming:\n");
                objectReasoner.getRootOntology().axioms().forEach(System.out::println);
                System.out.println(String.join("", Collections.nCopies(100, "-")));
                if (isConsistent) {
                    binaryTupleTableEntries(((Reasoner) objectReasoner).getTableau().getExtensionManager(),
                            contextOntology.getDataFactory())
                            .forEach(System.out::println);
                    ternaryTupleTableEntries(((Reasoner) objectReasoner).getTableau().getExtensionManager())
                            .forEach(System.out::println);
                } else {
                    System.out.println("--- object ontology is inconsistent.");
                }
                System.out.println(String.join("", Collections.nCopies(100, "-")));
            }
            return isConsistent;

        } else {
            // No rigid names

            // Iterate over all tableau nodes and check whether one of them is not inner consistent.
            boolean thereExistsNodeThatIsNotInnerConsistent = tableauNodes()
                    // filter if there is any node that is not inner consistent.
                    .filter(node -> {
                        OWLOntology objectOntology = contextOntology.getObjectOntology(Collections.singletonList(typeOfNode(node)));
                        OWLReasoner reasoner = reasonerFactory.createReasoner(objectOntology);
                        boolean isInconsistent = !reasoner.isConsistent();
                        if (debugOutput > 1) {
                            System.out.println("Object ontology for node " + node + ":");
                            reasoner.getRootOntology().axioms().forEach(System.out::println);
                            ((Reasoner) reasoner).getDLOntology().getDLClauses().forEach(System.out::println);
                            System.out.println(String.join("", Collections.nCopies(100, "-")));
                            if (!isInconsistent) {
                                System.out.println("--- object ontology for node " + node
                                        + " is consistent, following object model is found:");
                                binaryTupleTableEntries(((Reasoner) reasoner).getTableau().getExtensionManager(),
                                        contextOntology.getDataFactory())
                                        .forEach(System.out::println);
                                ternaryTupleTableEntries(((Reasoner) reasoner).getTableau().getExtensionManager())
                                        .forEach(System.out::println);
                            } else {
                                System.out.println("--- object ontology for node " + node
                                        + " is inconsistent.");
                            }
                            System.out.println(String.join("", Collections.nCopies(100, "-")));
                        }

                        return isInconsistent;
                    })
                    .findAny().isPresent();

            return !thereExistsNodeThatIsNotInnerConsistent;
        }
    }

    @Deprecated
    public Set<PreModel> listModels() {
        Set<PreModel> models = new HashSet<>();
        Map<Term, Node> termsToNode = new HashMap<>();

        clear();
        m_permanentDLOntology.getPositiveFacts()
                .forEach(atom -> loadPositiveFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
        m_permanentDLOntology.getNegativeFacts()
                .forEach(atom -> loadNegativeFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
        if (m_additionalDLOntology != null) {
            m_additionalDLOntology.getPositiveFacts()
                    .forEach(atom -> loadPositiveFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
            m_additionalDLOntology.getNegativeFacts()
                    .forEach(atom -> loadNegativeFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
        }

        // Ensure that at least one individual exists.
        if (m_firstTableauNode == null) {
            createNewNINode(m_dependencySetFactory.emptySet());
        }
        if (super.runCalculus()) {
            models.add(new PreModel(getExtensionManager(), contextOntology.getDataFactory()));
        }


        Optional<BinaryTupleTableEntry> entry;
        do {
            entry = binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                    .filter(tuple -> !tuple.getDependencySet().isEmpty())
                    .reduce((a, b) -> b);

            if (entry.isPresent()) {
                getExtensionManager().setClash(entry.get().getDependencySet());

                if (super.runCalculus()) {
                    models.add(new PreModel(getExtensionManager(), contextOntology.getDataFactory()));
                }
            }

        } while (entry.isPresent());

        return models;
    }

    /**
     * Wrapper method for Tableau's runCalculus method. Used in ModelIterator.
     *
     * @return true iff ontology is consistent
     */
    private boolean tableauRunCalculus() {
        boolean result = super.runCalculus();

        return result;
    }

    /**
     * @return A stream of Premodels that HermiT calculated.
     */
    public Stream<PreModel> consistentInterpretations() {

        return StreamSupport.stream(new ModelIterator().spliterator(), false);
    }

    /**
     * @return A stream of HermiT's tableau nodes.
     */
    private Stream<Node> tableauNodes() {

        return StreamSupport.stream(new NodeIterator().spliterator(), false);
    }

    /**
     * This class realises an iterator for consistentInterpretations that Hermit calculates.
     */
    private class ModelIterator implements Iterator<PreModel>, Iterable<PreModel> {

        private PreModel model;

        /**
         * The standard constructor initialising the internal state with the first model if the ontology is
         * consistent. The initialisation is taken from isSatisfiable(...) in HermiT's Tableau class.
         */
        public ModelIterator() {
            Map<Term, Node> termsToNode = new HashMap<>();

            clear();
            m_permanentDLOntology.getPositiveFacts()
                    .forEach(atom -> loadPositiveFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
            m_permanentDLOntology.getNegativeFacts()
                    .forEach(atom -> loadNegativeFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
            if (m_additionalDLOntology != null) {
                m_additionalDLOntology.getPositiveFacts()
                        .forEach(atom -> loadPositiveFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
                m_additionalDLOntology.getNegativeFacts()
                        .forEach(atom -> loadNegativeFact(termsToNode, atom, m_dependencySetFactory.emptySet()));
            }

            // Ensure that at least one individual exists.
            if (m_firstTableauNode == null) {
                createNewNINode(m_dependencySetFactory.emptySet());
            }

            model = null;
        }

        @Override
        public boolean hasNext() {
            // Only do backtracking when there already is a model (not in the very first call of hasNext()
            if (model != null) {
                Optional<BinaryTupleTableEntry> entry =
                        binaryTupleTableEntries(getExtensionManager(), contextOntology.getDataFactory())
                                .filter(tuple -> !tuple.getDependencySet().isEmpty())
                                .reduce((a, b) -> b);
                DependencySet clashSet;
                if (entry.isPresent()) {
                    // If there is any node with a non-empty dependency set, use it for backtracking
                    clashSet = entry.get().getDependencySet();
                } else {
                    // Otherwise we cycled through all models and are done -> set clash with empty dependency set
                    clashSet = new BinaryTupleTableEntry(0, getExtensionManager(), contextOntology.getDataFactory())
                            .getDependencySet();
                }
                getExtensionManager().setClash(clashSet);
            }

            if (tableauRunCalculus()) {
                model = new PreModel(getExtensionManager(), contextOntology.getDataFactory());
            } else {
                model = null;
            }

            return model != null;
        }

        @Override
        public PreModel next() {

            if (debugOutput > 1) {
                //System.out.println(contextOntology);
                System.out.println("meta ontology is consistent, following context model is found:");
                System.out.println(model.toStringWithDependencySet());
            }

            return model;
        }

        @Override
        public Iterator<PreModel> iterator() {

            return this;
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
}
