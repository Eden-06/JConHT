package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * This class describes is used to generate the negation of a given OWLAxiom.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegator {

    private final OWLAxiom owlAxiom;
    private OWLDataFactory dataFactory;

    /**
     * This is the standard constructor.
     *
     * @param owlAxiom_ the input (unnegated) axiom
     * @param dataFactory_ the OWL data factory used to generate fresh individuals when necessary
     */
    public AxiomNegator(OWLAxiom owlAxiom_, OWLDataFactory dataFactory_) {
        owlAxiom = owlAxiom_;
        dataFactory = dataFactory_;
    }

    /**
     * This method returns the negated version of the axiom. How exactly the negation looks like depends on the axiom
     * type.
     *
     * @return the negated axiom
     */
    protected OWLAxiom getNegation() {
        // TODO actually return the negated axiom for all AxiomTypes
        // TODO lieber AxiomNegator als Interface mit einzelnen Subklassen für alle AxiomTzpes oder hier mit switch??
        switch (owlAxiom.getAxiomType().getIndex()) {
//        case 0: // Declaration
//        case 1: // Equivalent Classes
            // TODO bei einer negiert GCI muss ein fresh individual eingeführt werden. Wo bekommen wir das her?
        case 2: // Subclass Of
            // ¬(C ⊑ D) ⟹ (C ∧ ¬D)(x_new)
            OWLSubClassOfAxiom axiomAsSubClassOfAxiom = (OWLSubClassOfAxiom) owlAxiom;
            Stream<OWLClassExpression> conjuncts = Stream.of(axiomAsSubClassOfAxiom.getSubClass(),
                    axiomAsSubClassOfAxiom.getSuperClass().getObjectComplementOf());
            return new OWLClassAssertionAxiomImpl(dataFactory.getOWLAnonymousIndividual(),
                    new OWLObjectIntersectionOfImpl(conjuncts),
                    Collections.emptySet());
//        case 3: // Disjoint Classes
//        case 4: // Disjoint Union
            // TODO Warum geht das hier nicht, wenn man oben .getIndex() weglässt
            // case AxiomType.CLASS_ASSERTION: // Class Assertion
            case 5: // Class Assertion
                // ¬(C(a)) ⟹ (¬C(a))
                OWLClassAssertionAxiom axiomAsClassAssertion = (OWLClassAssertionAxiom) owlAxiom;
                return new OWLClassAssertionAxiomImpl(axiomAsClassAssertion.getIndividual(),
                        axiomAsClassAssertion.getClassExpression().getObjectComplementOf(),
                        Collections.emptySet());
//          case 6: // Same Individual
//          case 7: // Different Individuals
//          case 8: // Object Property Assertion
//          case 9: // Negative Object Property Assertion
            default:
                // TODO das muss hier noch mal ordentlich gemacht werden
                try {
                    throw new Exception("Unknown axiom type! No negated  axiom is returned.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return owlAxiom;
        }


    }
}
