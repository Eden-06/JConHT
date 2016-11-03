package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Set<OWLAxiom> globalObjectOntology;
    private Set<OWLClass> outerAbstractedMetaConcepts;
    private OWLDataFactory dataFactory;

    /**
     * This is the standard constructor.
     *
     * @param rootOntology The correctly annotated root ontology. TODO: Specify what this actually means.
     */
    public ContextOntology(OWLOntology rootOntology) {

        ontologyManager = rootOntology.getOWLOntologyManager();
        dataFactory = ontologyManager.getOWLDataFactory();

        OWLAnnotationProperty isDefinedBy = dataFactory.getRDFSIsDefinedBy();
        OWLAnnotationProperty label = dataFactory.getRDFSLabel();
        OWLLiteral objectGlobal = dataFactory.getOWLLiteral("objectGlobal");

        // Obtain meta ontology
        try {

            metaOntology = ontologyManager.createOntology(rootOntology.axioms()
                    .filter(owlAxiom -> owlAxiom.isOfType(AxiomType.LOGICAL_AXIOM_TYPES))
                    .filter(owlAxiom -> owlAxiom.annotations(isDefinedBy).count() == 0)
                    .filter(owlAxiom -> owlAxiom.annotations(label)
                            .filter(owlAnnotation -> owlAnnotation.getValue().equals(objectGlobal))
                            .count() == 0)
                    .collect(Collectors.toSet()));

            // Create IRI for meta ontology
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
        objectAxiomsMap = rootOntology.axioms()
                .filter(owlAxiom -> owlAxiom.annotations(isDefinedBy).count() > 0)
                .collect(Collectors.toMap(
                        owlAxiom -> dataFactory.getOWLClass(
                                owlAxiom.annotations(isDefinedBy)
                                        .findFirst()
                                        // This get() does not fail, because of the above filter().
                                        .get()
                                        .getValue()
                                        .asIRI()
                                        // This get() does not fail, because values are always IRIs ;-).
                                        .get()),
                        owlAxiom -> owlAxiom.getAxiomWithoutAnnotations()));

        // Retrieve all meta concepts that identify object axioms
        outerAbstractedMetaConcepts = objectAxiomsMap.keySet().stream()
                .map(OWLClassExpression::asOWLClass)
                .collect(Collectors.toSet());

        // Create negated axioms for negated keys and add them to object axioms map
        objectAxiomsMap.putAll(objectAxiomsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().accept(new ConceptNegator(dataFactory)),
                        entry -> entry.getValue().accept(new AxiomNegator(dataFactory)))));

        // Obtain global object axioms
        globalObjectOntology = rootOntology.axioms()
                // consider only logical axioms
                .filter(owlAxiom -> owlAxiom.isOfType(AxiomType.LOGICAL_AXIOM_TYPES))
                // consider only axioms that are labelled with 'global'
                .filter(owlAxiom -> owlAxiom.annotations(label)
                        .filter(owlAnnotation -> owlAnnotation.getValue().equals(objectGlobal))
                        .count() > 0)
                .collect(Collectors.toSet());
    }


    /**
     * This method returns the meta ontology.
     *
     * @return The meta ontology.
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
    public OWLOntology getObjectOntology(Set<OWLClassExpression> metaClasses) {

        OWLOntology objectOntology = null;

        try {

            //create the object ontology
            objectOntology = ontologyManager.createOntology(
                    Stream.concat(
                            globalObjectOntology.stream(),
                            objectAxiomsMap.entrySet().stream()
                                    .filter(entry -> metaClasses.contains(entry.getKey()))
                                    .map(Map.Entry::getValue))
                            .collect(Collectors.toSet()));

        } catch (OWLOntologyCreationException e) {

            e.printStackTrace();
        }

        return objectOntology;
    }


    /**
     * @return The OWL data factory of the ontology manager that was used to create the context.
     */
    public OWLDataFactory getDataFactory() {

        return ontologyManager.getOWLDataFactory();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        // Obtain meta ontology’s IRI.
        builder.append("Meta Ontology IRI: ");
        builder.append(metaOntology.getOntologyID().getOntologyIRI()
                .map(IRI::toString)
                .orElse("no IRI specified"));
        builder.append("\n\n");

        // Obtain meta concepts that abbreviate object axioms.
        builder.append("Meta concepts that identify object axioms:\n");
        builder.append(outerAbstractedMetaConcepts.stream()
                .map(OWLClass::toString)
                .collect(Collectors.joining(", ")));
        builder.append("\n\n");

        // Obtain meta ontology.
        builder.append("Meta Ontology:\n");
        builder.append(metaOntology.axioms()
                .sorted()
                .map(OWLAxiom::toString)
                .collect(Collectors.joining("\n")));
        builder.append("\n\n");

        // Obtain object-axioms map.
        builder.append("Object-axioms map:\n");
        builder.append(objectAxiomsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> Stream.of(entry.getKey().toString(), " -> ", entry.getValue().toString(), "\n"))
                .collect(Collectors.joining()));

        // Obtain global object-axioms.
        builder.append("Global object-axioms:\n");
        builder.append(globalObjectOntology.stream()
                .map(OWLAxiom::toString)
                .collect(Collectors.joining("\n")));
        builder.append("\n\n");

        return builder.toString();
    }
}
