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

        File file = new File("input/hermit-testonto-2.owl");
        if (args.length != 0) {
            switch (args[0]) {
                case "framed":
                    file = new File("input/Bank.owl");
                    break;
                default:
                    file = new File("input/hermit-testonto-" + args[0] + ".owl");
            }
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {

            //TODO oder hier lieber manager.loadOntologyFromOntologyDocument(file) ?
            OWLOntology onto = manager.loadOntology(IRI.create(file));
            
            ContextOntology contextOntology = new ContextOntology(onto);
            System.out.println(contextOntology);
            System.out.println("---------------------------------------------");

//            Debugger debugger = new Debugger(Prefixes.STANDARD_PREFIXES,true);
//            confWithoutTableauMonitor.monitor = debugger;
            ContextReasoner reasoner = new ContextReasoner(contextOntology);
            System.out.println("reasoner.getDLOntology().getDLClauses() = ");
            reasoner.getDLOntology().getDLClauses().stream().forEach(System.out::println);
            System.out.println("reasoner.getDLOntology().getPositiveFacts() = " + reasoner.getDLOntology().getPositiveFacts());
            System.out.println("reasoner.getDLOntology().getNegativeFacts() = " + reasoner.getDLOntology().getNegativeFacts());
            System.out.println("---------------------------------------------");
            boolean result = reasoner.isConsistent();

            System.out.println(result);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}

