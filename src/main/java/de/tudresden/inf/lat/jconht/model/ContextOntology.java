package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class describes a context ontology.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class ContextOntology {

    private final OWLOntologyManager ontologyManager;
    private OWLOntology metaOntology;
    private Map<OWLClassExpression, OWLAxiom> objectAxiomsMap;

    /**
     * This is the standard constructor.
     *
     * @param rootOntology The correctly annotated root ontology.
     * @TODO: Specify what this actually means.
     */
    public ContextOntology(OWLOntology rootOntology) {

        ontologyManager = rootOntology.getOWLOntologyManager();
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

            IRI metaIRI = IRI.create(rootOntology.getOntologyID().getOntologyIRI().orElse(IRI.create("")) + "_meta");
            OWLOntologyID metaOntologyID = new OWLOntologyID(Optional.of(metaIRI), Optional.empty());
            // Create the change that will set our version IRI
            SetOntologyID setOntologyID = new SetOntologyID(metaOntology, metaOntologyID);
            // Apply the change
            ontologyManager.applyChange(setOntologyID);

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


        // Create negated axioms for negated keys and add them to object axioms map
        Map<OWLClassExpression, OWLAxiom> negatedObjectAxiomsMap = new HashMap<>();
        objectAxiomsMap.forEach((metaClass,axiom) -> {
            AxiomNegator axiomNegator = new AxiomNegator(axiom);
            negatedObjectAxiomsMap.put(metaClass.getObjectComplementOf(), axiomNegator.getNegation());
        });

        objectAxiomsMap.putAll(negatedObjectAxiomsMap);
    }
    

    /**
     * This method returns the meta ontology.
     * <p>
     *
     * @return The meta ontology.
     * @TODO: Probably this class could implement <code>OWLOntology</code> and
     * could delegate all functionality to the meta ontology field.  This
     * would make this method obsolete.
     */
    public OWLOntology getMetaOntology() {

        return metaOntology;
    }

    /**
     * This method returns an object ontology associated to the given set of meta classes.
     *
     * @param metaClasses A set of meta classes.
     * @return The associated object ontology.
     */
    public OWLOntology getObjectOntology(Set<OWLClass> metaClasses) {

        OWLOntology objectOntology = null;

        try {

            objectOntology = ontologyManager.createOntology(
                    objectAxiomsMap.entrySet().stream()
                            .filter(entry -> metaClasses.contains(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toSet())
            );

        } catch (OWLOntologyCreationException e) {

            e.printStackTrace();
        }

        return objectOntology;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Meta Ontology:\n");
        stringBuilder.append("Meta Ontology IRI: ");
        metaOntology.getOntologyID().getOntologyIRI()
                .ifPresent(iri -> stringBuilder.append(iri).append("\n"));
        metaOntology.axioms()
                .forEach(axiom -> stringBuilder.append(axiom).append("\n"));
        stringBuilder.append("\n");

        stringBuilder.append("Hash map:\n");
        objectAxiomsMap.forEach(
                (key, value) -> stringBuilder.append(key).append(" -> ").append(value).append("\n"));
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}