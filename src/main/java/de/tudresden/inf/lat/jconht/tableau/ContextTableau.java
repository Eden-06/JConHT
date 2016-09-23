package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.InterruptFlag;
import org.semanticweb.HermiT.tableau.Tableau;

import java.util.Map;

/**
 * Created by boehme on 23/09/16.
 */
public class ContextTableau  extends Tableau{

    /**
     * @param interruptFlag                 interruptFlag
     * @param tableauMonitor                tableauMonitor
     * @param existentialsExpansionStrategy existentialsExpansionStrategy
     * @param useDisjunctionLearning        useDisjunctionLearning
     * @param permanentDLOntology           permanentDLOntology
     * @param additionalDLOntology          additionalDLOntology
     * @param parameters                    parameters
     */
    public ContextTableau(InterruptFlag interruptFlag, TableauMonitor tableauMonitor, ExistentialExpansionStrategy existentialsExpansionStrategy, boolean useDisjunctionLearning, DLOntology permanentDLOntology, DLOntology additionalDLOntology, Map<String, Object> parameters) {
        super(interruptFlag, tableauMonitor, existentialsExpansionStrategy, useDisjunctionLearning, permanentDLOntology, additionalDLOntology, parameters);
    }
}
