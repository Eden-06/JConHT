package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;

import java.util.Collections;

/**
 * This class describes is used to generate the negation of a given OWLAxiom.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomNegator {

    private final OWLAxiom owlAxiom;

    /**
     * This is the standard constructor.
     *
     * @param owlAxiom_ the input (unnegated) axiom
     */
    public AxiomNegator(OWLAxiom owlAxiom_) {
        owlAxiom = owlAxiom_;
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
//          case 0: // Declaration
//              System.out.println("AxiomNegator: Declaration found");
//              break;
//          case 1: // Declaration
//              System.out.println("AxiomNegator: Declaration found");
//              break;
//          case 2: // Declaration
//              System.out.println("AxiomNegator: Declaration found");
//              break;
//          case 3: // Declaration
//              System.out.println("AxiomNegator: Declaration found");
//              break;
//          case 4: // Declaration
//              System.out.println("AxiomNegator: Declaration found");
//              break
            // TODO Warum geht das hier nicht, wenn man oben .getIndex() weglässt
            // case AxiomType.CLASS_ASSERTION: // Class Assertion
            case 5: // Class Assertion
                OWLClassAssertionAxiom axiomAsClassAssertion = (OWLClassAssertionAxiom) owlAxiom;
                return new OWLClassAssertionAxiomImpl(axiomAsClassAssertion.getIndividual(),
                        axiomAsClassAssertion.getClassExpression().getObjectComplementOf(),
                        Collections.emptySet());
            default:
                // TODO das muss hier noch mal ordentlich gemacht werden
                try {
                    throw new Exception("Unknown axiom type! The input axiom is returned.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return owlAxiom;
        }


    }
}
