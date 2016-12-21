package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.SimpleRenderer;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
    private final OWLDataFactory dataFactory;
    private final OWLOntology rootOntology;
    private final OWLAnnotationProperty isDefinedBy;
    private final OWLAnnotationProperty label;
    private final OWLLiteral objectGlobal;
    private final OWLAnnotationValue rigid;
    private final OWLAnnotationValue nonRigid;
    private final Predicate<IRI> iriIsOWLClass;
    private final Predicate<IRI> iriIsOWLObjectProperty;
    private OWLOntology metaOntology;
    private Map<OWLClass, OWLAxiom> objectAxiomsMap;
    //private Set<OWLClass> rigidClasses; // todo müssen wir rigid names wirklich als set speichern? welche Alternativen? siehe 10 Zeilen weiter unten


    //todo prinzipielle Frage: Wenn ich es richtig verstanden habe, dann ist die Situation folgende.
    // Ich kann z.B. die rigiden Konzepte als field "rigidConcepts" speichern, dann wird es einmal berechnet und dann als Set
    // gespeichert, als Stream kann ich es nicht speichern, da dieser nur einmal verwendet werden könnte. Vorteil:
    // muss nur einmal berechnet werden, Nachteil: Speicherbedarf des Sets, selbst wenn ich nie das Feld aufrufe.
    // Alternative: eine Methode "rigidConcepts()", die einen Stream zurückgibt. Vorteil: Kein Speicherbedarf,
    // Nachteil: wird jedes Mal neu berechnet, wenn benötigt.
    //
    // Habe ich es bis hierher soweit richtig verstanden?
    //
    // In Anlehnung an die OWLAPI, wo überall Getter, die Sets zurückgegeben hätten, mit Stream-Methoden ersetzt wurden,
    // schätze ich, ist bei Ontologien eher der Speicherbedarf problematisch. Deshalb würde ich das auch so machen.
    // Was ist deine Meinung dazu?

    /**
     * This is the standard constructor.
     *
     * @param rootOntology The correctly annotated root ontology. TODO: Specify what this actually means.
     */
    public ContextOntology(OWLOntology rootOntology) {

        ontologyManager = rootOntology.getOWLOntologyManager();
        dataFactory = ontologyManager.getOWLDataFactory();
        this.rootOntology = rootOntology;


        // Define OWLAnnotationProperties and -Values, OWLLiterals
        isDefinedBy = dataFactory.getRDFSIsDefinedBy();
        label = dataFactory.getRDFSLabel();
        objectGlobal = dataFactory.getOWLLiteral("objectGlobal");
        rigid = dataFactory.getOWLLiteral("rigid");
        nonRigid = dataFactory.getOWLLiteral("non-rigid");
        iriIsOWLClass =
                iri -> rootOntology.classesInSignature().map(HasIRI::getIRI).anyMatch(iri1 -> iri1.equals(iri));
        iriIsOWLObjectProperty =
                iri -> rootOntology.objectPropertiesInSignature().map(HasIRI::getIRI).anyMatch(iri1 -> iri1.equals(iri));

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


        // Set renderer s.t. IRIs are shortened.
        OWLObjectRenderer renderer = new SimpleRenderer();
        renderer.setShortFormProvider(new QNameShortFormProvider(ontologyManager
                .getOntologyFormat(rootOntology)
                .asPrefixOWLDocumentFormat()
                .getPrefixName2PrefixMap()));
        ToStringRenderer.setRenderer(() -> renderer);


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
    }

    public void clear() {
        ontologyManager.removeOntology(metaOntology);
        //TODO probably more to do here
    }

    public Stream<OWLEntity> metaSignature() {
        return this.getMetaOntology().signature();
    }

    public Stream<OWLClass> metaClassesInSignature() {
        return this.getMetaOntology().classesInSignature();
    }

    public Stream<OWLObjectProperty> metaObjectPropertiesInSignature() {
        return this.getMetaOntology().objectPropertiesInSignature();
    }

    public Stream<OWLEntity> objectSignature() {
        return objectAxiomsMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(HasSignature::signature);
    }

    public Stream<OWLClass> objectClassesInSignature() {
        return objectAxiomsMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(HasClassesInSignature::classesInSignature);
    }

    public Stream<OWLObjectProperty> objectObjectPropertiesInSignature() {
        return objectAxiomsMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(HasObjectPropertiesInSignature::objectPropertiesInSignature);
    }

    // todo das ist 4 mal fast der gleiche Code. Kann man das nicht irgendwie abkürzen? Mit Collectors.groupingBy?
    public Stream<OWLClass> rigidClasses() {
        return rootOntology
                .axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(axiom -> axiom.getAnnotation().getProperty().equals(label))
                .filter(axiom -> axiom.getValue().equals(rigid))
                .map(axiom -> axiom.getSubject().asIRI().get())
                .filter(iriIsOWLClass)
                .map(dataFactory::getOWLClass);
    }

    public Stream<OWLObjectProperty> rigidObjectProperties() {
        return rootOntology
                .axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(axiom -> axiom.getAnnotation().getProperty().equals(label))
                .filter(axiom -> axiom.getValue().equals(rigid))
                .map(axiom -> axiom.getSubject().asIRI().get())
                .filter(iriIsOWLObjectProperty)
                .map(dataFactory::getOWLObjectProperty);
    }

    public Stream<OWLClass> flexibleClasses() {
        return rootOntology
                .axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(axiom -> axiom.getAnnotation().getProperty().equals(label))
                .filter(axiom -> axiom.getValue().equals(nonRigid))
                .map(axiom -> axiom.getSubject().asIRI().get())
                .filter(iriIsOWLClass)
                .map(dataFactory::getOWLClass);
    }

    public Stream<OWLObjectProperty> flexibleObjectProperties() {
        return rootOntology
                .axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(axiom -> axiom.getAnnotation().getProperty().equals(label))
                .filter(axiom -> axiom.getValue().equals(nonRigid))
                .map(axiom -> axiom.getSubject().asIRI().get())
                .filter(iriIsOWLObjectProperty)
                .map(dataFactory::getOWLObjectProperty);
    }

    /**
     * This method checks whether the context ontology contains rigid names.
     *
     * @return
     */
    public Boolean containsRigidNames() {
        return rootOntology
                .axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(axiom -> axiom.getAnnotation().getProperty().equals(label))
                .filter(axiom -> axiom.getValue().equals(rigid))
                .count() != 0;
    }


    /**
     * This method retrieves all meta concepts that identify object axioms
     *
     * @return Stream of OWL classes that identify object axioms.
     */
    public Stream<OWLClass> outerAbstractedMetaConcepts() {
        return objectAxiomsMap.keySet().stream().map(OWLClassExpression::asOWLClass);
    }

    /**
     * This method obtains all global object axioms.
     *
     * @return Stream of OWL axioms that must hold in every context.
     */
    public Stream<OWLAxiom> globalObjectOntology() {
        return rootOntology.axioms()
                // consider only logical axioms
                .filter(owlAxiom -> owlAxiom.isOfType(AxiomType.LOGICAL_AXIOM_TYPES))
                // consider only axioms that are labelled with 'global'
                .filter(owlAxiom -> owlAxiom.annotations(label)
                        .filter(owlAnnotation -> owlAnnotation.getValue().equals(objectGlobal))
                        .count() > 0)
                .map(owlAxiom -> (OWLAxiom) owlAxiom.getAxiomWithoutAnnotations());
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
    public OWLOntology getObjectOntology(Set<OWLClass> metaClasses) {

        OWLOntology objectOntology = null;

        try {

            //create the object ontology
            objectOntology = ontologyManager.createOntology(
                    Stream.concat(
                            globalObjectOntology(),
                            objectAxiomsMap.entrySet().stream()
                                    .map(entry -> metaClasses.contains(entry.getKey()) ?
                                            entry.getValue() :
                                            entry.getValue().accept(new AxiomNegator(dataFactory)))));

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
        builder.append("Meta signature: ");
        builder.append(this.metaClassesInSignature().collect(Collectors.toSet()));
        builder.append("\n");
        builder.append("Meta concepts that identify object axioms:\n");
        builder.append(outerAbstractedMetaConcepts()
                .map(OWLClass::toString)
                .collect(Collectors.joining("\n")));
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
        builder.append("\n");

        // Obtain global object-axioms.
        builder.append("Global object-axioms:\n");
        builder.append(globalObjectOntology()
                .sorted()
                .map(OWLAxiom::toString)
                .collect(Collectors.joining("\n")));
        builder.append("\n");

        // Are there rigid names?
        builder.append("Rigid Names: ");
        builder.append(containsRigidNames());
        builder.append("\nRigid Concept Names: ");
        builder.append(rigidClasses().collect(Collectors.toSet()));
        builder.append("\n\n");

        return builder.toString();
    }
}
