package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.tableau.Tableau;


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
                configuration.parameters);
    }
}
