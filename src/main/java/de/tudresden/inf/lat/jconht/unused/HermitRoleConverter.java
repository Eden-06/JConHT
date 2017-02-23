package de.tudresden.inf.lat.jconht.unused;

import de.tudresden.inf.lat.jconht.model.UnhandledHermiTObjectException;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class HermitRoleConverter implements HermitObjectVisitor<OWLObjectPropertyExpression> {

    // TODO Tests

    private OWLDataFactory dataFactory;

    /**
     * This is the standard constructor.
     *
     * @param dataFactory the OWL data factory used to generate the OWLClassExpression
     */
    public HermitRoleConverter(OWLDataFactory dataFactory) {

        this.dataFactory = dataFactory;
    }

    @Override
    public <T> OWLObjectPropertyExpression doDefault(T object) {

        // This should never happen!
        throw new UnhandledHermiTObjectException("Unknown HermiT role type in HermitRoleConverter: " + object.getClass());
    }

    @Override
    public OWLObjectProperty visit(AtomicRole role) {

        return dataFactory.getOWLObjectProperty(IRI.create(role.getIRI()));
    }

    @Override
    public OWLObjectPropertyExpression visit(InverseRole role) {

        return dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(
                IRI.create(role.getInverseOf().getIRI())));
    }


}
