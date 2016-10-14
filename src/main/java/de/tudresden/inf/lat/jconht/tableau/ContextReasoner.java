package de.tudresden.inf.lat.jconht.tableau;

import de.tudresden.inf.lat.jconht.model.ContextOntology;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;

/**
 * This class describes a reasoner that can deal with contexts.
 * <p>
 * For that, it uses a specialised version of a tableau.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextReasoner extends Reasoner {

    /**
     * This is the standard constructor, which calls the super constructor and then
     * adjusts the tableau used by the reasoner.
     * <p>
     * Hence, <code>getTableau()</code> actually returns an instance of
     * <code>ContextTableau</code>.
     *
     * @param configuration The configuration used to create the reasoner.
     * @param rootOntology  The root ontology for the reasoner.
     */
    public ContextReasoner(Configuration configuration, ContextOntology rootOntology) {

        super(configuration, rootOntology.getMetaOntology());

        m_tableau = new ContextTableau(this.getTableau(), configuration);
    }

}
