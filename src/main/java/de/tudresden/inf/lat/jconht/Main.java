package de.tudresden.inf.lat.jconht;

import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import org.semanticweb.HermiT.Configuration;
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


        IRI iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-2.owl");
        if (args.length == 0) {
            // default ontology is loaded.
        } else {
            switch (args[0]) {
                case "1":
                    iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-2.owl");
                    break;
                case "2":
                    iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-1.owl");
                    break;
                case "3":
                    iri = IRI.create("file://" + inputDir + "/" + "pizza.owl");
                    break;
                case "4":
                    iri = IRI.create("file://" + inputDir + "/" + "testonto1.owl");
                    break;
                case "5":
                    iri = IRI.create("file://" + inputDir + "/" + "hermit-testonto-2-backtrack.owl");
                    break;
            }
        }


        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {

            OWLOntology onto = manager.loadOntology(iri);
            Configuration confWithouTableauMonitor = new Configuration();
//            Debugger debugger = new Debugger(Prefixes.STANDARD_PREFIXES,true);
//            confWithouTableauMonitor.monitor = debugger;
            ContextReasoner reasoner = new ContextReasoner(confWithouTableauMonitor, onto);
            Tableau tableau = reasoner.getTableau();
            boolean result = tableau.isSatisfiable(true, true, null, null, null, null, null, ReasoningTaskDescription.isABoxSatisfiable());

            System.out.println(result);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }


        System.out.println(inputDir);
    }
}
