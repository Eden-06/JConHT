package de.tudresden.inf.lat.jconht;

import de.tudresden.inf.lat.jconht.model.Configuration;
import de.tudresden.inf.lat.jconht.model.ContextOntology;
import de.tudresden.inf.lat.jconht.tableau.ContextReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormatFactory;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Arrays;

public class Main {

    // TODO refactor code of whole project, and tests accordingly

    // TODO ordentliche clear und dispose Methoden wo notwendig

    // TODO Taking care of proper handling of meta and object role boxes
    // meta RBoxAxioms müssen nicht gesondert behandelt werden, das sind einfach unannotierte OWLAxioms
    // obj RBoxAxioms müssen global gelten, müssen also mit "objectGlobal" annotiert sein
    // RBoxAxioms mit "definedBy" sind verboten! Das sollte vielleicht überprüft werden.

    public static void main(String[] args) {

        boolean verbose = Arrays.asList(args).contains("-v");

        if (verbose) {
            System.out.println("Welcome to JConHT!");
        }

        File file = new File("input/hermit-testonto-2.owl");
        if (args.length != 0) {
            file = new File(args[0]);
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        Configuration confWithoutDebug = new Configuration(true, false, false, false);
        Configuration confWithDebug = new Configuration(true, true, false, false);
        if (verbose) {
            System.out.println("Loading " + file);
            System.out.println();
        }
        try {

            //TODO oder hier lieber manager.loadOntologyFromOntologyDocument(file) ?
            //OWLOntology rootOntology = manager.loadOntology(IRI.create(file));

            FileDocumentSource fileDocumentSource = new FileDocumentSource(file, new ManchesterSyntaxDocumentFormatFactory().createFormat());
            OWLOntology rootOntology = manager.loadOntologyFromOntologyDocument(fileDocumentSource);

            ContextOntology contextOntology = verbose ?
                    new ContextOntology(rootOntology, confWithDebug) :
                    new ContextOntology(rootOntology, confWithoutDebug);
            if (verbose) {
                System.out.println(contextOntology);
                System.out.println("---------------------------------------------");
            }

            ContextReasoner reasoner = new ContextReasoner(contextOntology);
            if (verbose) {
                System.out.println("reasoner.getDLOntology().getDLClauses() = ");
                reasoner.getDLOntology().getDLClauses().forEach(System.out::println);
                System.out.println("reasoner.getDLOntology().getPositiveFacts() = " + reasoner.getDLOntology().getPositiveFacts());
                System.out.println("reasoner.getDLOntology().getNegativeFacts() = " + reasoner.getDLOntology().getNegativeFacts());
                System.out.println("---------------------------------------------");
            }

            boolean result = reasoner.isConsistent();

            System.out.println(result);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}

