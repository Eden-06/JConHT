package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class describes a context ontology.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextOntology {

    private OWLOntology metaOntology;

    private Map<OWLClass, OWLAxiom> objectAxiomsMap;

    /**
     * This is the standard constructor.
     *
     * @param rootOntology The correctly annotated root ontology.
     *                     TODO: Specify what this actually means.
     */
    public ContextOntology(OWLOntology rootOntology) {

        OWLOntologyManager ontologyManager = rootOntology.getOWLOntologyManager();
        IRI isDefinedBy = OWLRDFVocabulary.RDFS_IS_DEFINED_BY.getIRI();

        // Obtain meta ontology
        try {

            metaOntology = ontologyManager.createOntology(
                    rootOntology.axioms()
                            .filter(owlAxiom -> !owlAxiom.isOfType(AxiomType.DECLARATION))
                            .filter(owlAxiom -> owlAxiom.annotations(new OWLAnnotationPropertyImpl(isDefinedBy))
                                    .count() == 0)
                            .collect(Collectors.toSet())
            );

        } catch (OWLOntologyCreationException e) {

            e.printStackTrace();
        }


        // Obtain object axioms map
        objectAxiomsMap = new HashMap<>();

        rootOntology.axioms()
                .forEach(owlAxiom -> owlAxiom.annotations(new OWLAnnotationPropertyImpl(isDefinedBy))
                        .findFirst()
                        .ifPresent(
                                owlAnnotation -> objectAxiomsMap.put(new OWLClassImpl((IRI) owlAnnotation.getValue()),
                                        owlAxiom.getAxiomWithoutAnnotations())
                        )
                );
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Meta Ontology:\n");
        metaOntology.axioms()
                .forEach(axiom -> stringBuilder.append(axiom + "\n"));
        stringBuilder.append("\n");

        stringBuilder.append("Hash map:\n");
        objectAxiomsMap.forEach((key, value) -> stringBuilder.append(key + " -> " + value + "\n"));
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
