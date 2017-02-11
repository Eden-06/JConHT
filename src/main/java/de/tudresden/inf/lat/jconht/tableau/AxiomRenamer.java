package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This class is used to rename OWL axioms in an ontology. This is needed when handling rigid names.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomRenamer {
    private OWLOntologyManager manager;
    private OWLOntology ontologyToChange;
    private Function<OWLEntity, Stream<OWLAxiom>> axiomsContainingThatEntity;

    public AxiomRenamer(OWLOntology ontologyToChange) {
        this.ontologyToChange = ontologyToChange;
        this.manager = ontologyToChange.getOWLOntologyManager();
        axiomsContainingThatEntity = name -> ontologyToChange.axioms().filter(axiom -> axiom.containsEntityInSignature(name));
    }


    //todo hier direkt Streams zu nehmen geht nicht, oder? Wäre es sinnvoller? Schwierig, da ich flexible names mehrmals brauche
    public void rename(Set<OWLEntity> flexibleNames, int index) {


            try {
                // This ontology is a subset of ontologyToChange that contains all axioms with any flexible name
                OWLOntology ontology = manager.createOntology(flexibleNames.stream()
                        .flatMap(axiomsContainingThatEntity));

                OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));

                flexibleNames.forEach(name ->
                        manager.applyChanges(renamer.changeIRI(name, IRI.create(name.getIRI().getIRIString() + "_" + index))));

                manager.addAxioms(ontologyToChange, ontology.axioms());

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }


        // Remove the original axioms containing flexible names from the ontology
        flexibleNames.forEach(name ->
                manager.removeAxioms(
                        ontologyToChange,
                        axiomsContainingThatEntity.apply(name)));


    }
}

//todo Im Constructor von OWLEntityRenamer braucht man eine Menge von Ontologien, kann man das einfacher/besser erzeugen?




