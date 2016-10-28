package de.tudresden.inf.lat.jconht;

import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
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


        IRI iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-2.owl");
        if (args.length != 0) {
            switch (args[0]) {
                case "framed":
                    iri = IRI.create("file://" + inputDir + "/" + "Bank.owl");
                    break;
                default:
                    iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-" + args[0] + ".owl");
            }
        }


        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {

            OWLOntology onto = manager.loadOntology(iri);


            ContextOntology contextOntology = new ContextOntology(onto);
            System.out.println(contextOntology);
            System.out.println("---------------------------------------------");

//            Debugger debugger = new Debugger(Prefixes.STANDARD_PREFIXES,true);
//            confWithoutTableauMonitor.monitor = debugger;
            ContextReasoner reasoner = new ContextReasoner(contextOntology);
            boolean result = reasoner.isConsistent();

            System.out.println(result);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}

