package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;

/**
 * Created by boehme on 23/09/16.
 */
public class ContextReasoner extends Reasoner {

    public ContextReasoner(Configuration configuration, OWLOntology rootOntology) {
        super(configuration, rootOntology);

        this.m_tableau = new ContextTableau(this.getTableau(), configuration);
    }

}
