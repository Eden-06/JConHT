package de.tudresden.inf.lat.jconht;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        System.out.println("Welcome to JConHT!");

        String inputDir = new File("input").getAbsolutePath();
        IRI iri1 = IRI.create("file://" + inputDir + "/" + "hermit-testonto-1.owl");
        IRI iri2 = IRI.create("file://" + inputDir + "/" + "pizza.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {

            OWLOntology onto = manager.loadOntology(iri1);
            Reasoner reasoner = new Reasoner(new Configuration(), onto);
            Tableau tableau = reasoner.getTableau();
            boolean result = tableau.isSatisfiable(true, true, null, null, null, null, null, ReasoningTaskDescription.isABoxSatisfiable());

            System.out.println(result);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }


        System.out.println(inputDir);
    }
}
