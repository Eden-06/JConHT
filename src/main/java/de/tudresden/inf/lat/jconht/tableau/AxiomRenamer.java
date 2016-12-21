package de.tudresden.inf.lat.jconht.tableau;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public AxiomRenamer(OWLOntology ontologyToChange) {
        this.ontologyToChange = ontologyToChange;
        this.manager = ontologyToChange.getOWLOntologyManager();
    }



    //todo hier direkt Streams zu nehmen geht nicht, oder? Wäre es sinnvoller?
    public void rename(Set<OWLEntity> flexibleNames, int numberOfNewNames) {

        IntStream.rangeClosed(1, numberOfNewNames).parallel().forEach(i -> {
            try {
                OWLOntology ontology = manager.createOntology(flexibleNames.stream().flatMap(name -> ontologyToChange.axioms().filter(axiom -> axiom.containsEntityInSignature(name))));
                OWLEntityRenamer renamer = new OWLEntityRenamer(
                        ontology.getOWLOntologyManager(),
                        //todo hier brauch man eine Menge von Ontologien, kann man das einfacher erzeugen?
                        Stream.of(ontology).collect(Collectors.toSet()));
                flexibleNames.forEach(name -> manager.applyChanges(renamer.changeIRI(name, IRI.create(name.getIRI().getIRIString() + "_" + i))));
                manager.addAxioms(ontologyToChange, ontology.axioms());
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        });

        flexibleNames.forEach(name -> manager.removeAxioms(ontologyToChange, ontologyToChange.axioms().filter(axiom -> axiom.containsEntityInSignature(name))));


    }
}



