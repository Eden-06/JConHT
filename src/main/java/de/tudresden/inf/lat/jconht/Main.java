package de.tudresden.inf.lat.jconht;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("Welcome to JConHT!");

        String inputDir = new File("input").getAbsolutePath();
        IRI iri1 = IRI.create("file://" + inputDir + "/" + "hermit-testonto-1.owl");
        IRI iri2 = IRI.create("file://" + inputDir + "/" + "pizza.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
            OWLOntology onto = manager.loadOntology(iri2);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        System.out.println(inputDir);
    }
}
