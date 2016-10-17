package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;

import java.util.HashSet;
import java.util.Set;


/**
 * This class describes a tableau that can deal with contexts.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextTableau extends Tableau {


    /**
     * This is the standard constructor.
     * <p>
     * It basically copies the information from a given tableau instance to itself.
     *
     * @param tableau       A given tableau instance.
     * @param configuration The configuration used for creating the given tableau instance.
     */
    public ContextTableau(Tableau tableau, Configuration configuration) {

        super(tableau.getInterruptFlag(),
                tableau.getTableauMonitor(),
                tableau.getExistentialsExpansionStrategy(),
                configuration.useDisjunctionLearning,
                tableau.getPermanentDLOntology(),
                tableau.getAdditionalDLOntology(),
                tableau.getParameters());
    }

    @Override
    protected boolean runCalculus() {

        if (super.runCalculus()) {

            // Possibly a model is found.
            int i = 0;
            ExtensionTable extensionTable = getExtensionManager().getBinaryExtensionTable();
            for (Node node = getFirstTableauNode(); node != null; node = node.getNextTableauNode()) {

                Object o;
                for (i = 0; (o = extensionTable.getTupleObject(i, 0)) != null; ++i) {
                    if (extensionTable.getTupleObject(i, 1).equals(node)) {
                        System.out.println(node.getNodeID() + " " + o);
                    }
                }
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
     * This method returns the set of IRIs associated to a given node.
     *
     * @param node A node.
     * @return The set of associated IRIs.
     */
    private Set<IRI> getClassesOfNode(Node node) {

        Set<IRI> associatedIRIs = new HashSet<>();

        ExtensionTable extensionTable = getExtensionManager().getBinaryExtensionTable();

//        Object o;
//        for (int i = 0; (o = extensionTable.getTupleObject(i, 0)) != null; ++i) {
//            if (extensionTable.getTupleObject(i, 1).equals(node)) {
//                Object concept = extensionTable.getTupleObject(i, 0);
//                if (concept instanceof AtomicConcept) {
//                    associatedIRIs.add()
//
//                }
//            }
//        }

        return associatedIRIs;
    }
}
