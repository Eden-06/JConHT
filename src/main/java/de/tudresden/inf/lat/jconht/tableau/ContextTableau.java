package de.tudresden.inf.lat.jconht.tableau;

import de.tudresden.inf.lat.jconht.model.ContextOntology;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;


/**
 * This class describes a tableau that can deal with contexts.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextTableau extends Tableau {

    private ContextOntology contextOntology;

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
    }

    @Override
    protected boolean runCalculus() {

        if (super.runCalculus()) {

            // Possibly a model is found.
            int i = 0;
            Set<OWLClassExpression> classesOfNode;
            OWLOntology objectOntology;
            ExtensionTable extensionTable = getExtensionManager().getBinaryExtensionTable();
            for (Node node = getFirstTableauNode(); node != null; node = node.getNextTableauNode()) {

                Object o;

                for (i = 0; (o = extensionTable.getTupleObject(i, 0)) != null; ++i) {
                    if (extensionTable.getTupleObject(i, 1).equals(node)) {
                        System.out.println(node.getNodeID() + " " + o + " " + extensionTable.getTupleObject(i, 2));
                    }
                }

                classesOfNode = getClassesOfNode(node);
                System.out.println("Classes of node " + node + ": " + classesOfNode);
                objectOntology = contextOntology.getObjectOntology(classesOfNode);
                Reasoner objectReasoner = new Reasoner(new Configuration(), objectOntology);
                System.out.println(objectReasoner.getDLOntology().getDLClauses());
                System.out.println(objectReasoner.getDLOntology().getPositiveFacts());
                System.out.println(objectReasoner.getDLOntology().getNegativeFacts());
                System.out.println("object ontology is consistent: " + objectReasoner.isConsistent());

                System.out.println();
            }
            System.out.println();

            DependencySet dependencySet = (DependencySet) extensionTable.getTupleObject(i - 1, 2);
            System.out.println(dependencySet);
            getExtensionManager().setClash(dependencySet);

            return runCalculus();

        } else {

            // The meta-ontology is inconsistent, giving up.
            return false;
        }
    }

    /**
     * This method returns the set of OWLClassExpressions associated to a given node.
     *
     * @param node A node.
     * @return The set of associated OWLClassExpressions.
     */
    private Set<OWLClassExpression> getClassesOfNode(Node node) {

        Set<OWLClassExpression> setOfClassExpressions = new HashSet<>();
        OWLDataFactory dataFactory = contextOntology.getDataFactory();
        ExtensionTable extensionTable = getExtensionManager().getBinaryExtensionTable();

        Object o;
        for (int i = 0; (o = extensionTable.getTupleObject(i, 0)) != null; ++i) {
            if (extensionTable.getTupleObject(i, 1).equals(node)) {
                if (o instanceof AtomicConcept)
                    setOfClassExpressions
                            .add(dataFactory.getOWLClass(
                                    IRI.create(((AtomicConcept) o).getIRI())));
                if (o instanceof AtomicNegationConcept)
                    setOfClassExpressions
                            .add(dataFactory.getOWLObjectComplementOf(dataFactory.getOWLClass(
                                    IRI.create(((AtomicNegationConcept) o).getNegatedAtomicConcept().getIRI()))));
            }
        }

        return setOfClassExpressions;
    }

}
