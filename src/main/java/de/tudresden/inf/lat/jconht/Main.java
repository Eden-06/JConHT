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


        if (args.length == 0) {
            System.out.println("Usage: JConHT.jar [OPTIONS] INPUT");
            System.out.println("OPTIONS");
            System.out.println("\t-v\t\tVerbose output");
            System.out.println("\t-vv\t\tMore verbose output");
            System.out.println("INPUT");
            System.out.println("\tthe file location of the .owl file that should be processed");
            System.out.println("EXIT CODES");
            System.out.println("\t0\t\tontology is consistent");
            System.out.println("\t13\t\tOWLOntologyCreationException catched, e.g. when INPUT is not found");
            System.out.println("\t42\t\tontology is inconsistent");
            System.out.println("\t127\t\tOutOfMemoryError catched");
            return;
        }


        int verbose = Arrays.stream(args).filter(arg -> arg.matches("-v*")).findFirst().orElse("-").length() - 1;


        if (verbose > 0) {
            System.out.println("Welcome to JConHT!");
        }

        File file = new File("input/hermit-testonto-2.owl");
        if (args.length != 0) {
            file = new File(args[0]);
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        Configuration conf = new Configuration(true, verbose, false, false);
        if (verbose > 0) {
            System.out.print("Loading " + file + " ... ");
        }
        try {

            //TODO oder hier lieber manager.loadOntologyFromOntologyDocument(file) ?
            //OWLOntology rootOntology = manager.loadOntology(IRI.create(file));

            FileDocumentSource fileDocumentSource = new FileDocumentSource(file, new ManchesterSyntaxDocumentFormatFactory().createFormat());
            OWLOntology rootOntology = manager.loadOntologyFromOntologyDocument(fileDocumentSource);

            if (verbose > 0) {
                System.out.println("done");
                System.out.println();
            }


            ContextOntology contextOntology = new ContextOntology(rootOntology, conf);
            if (verbose > 0) {
                System.out.println(contextOntology.getStatistics());
                System.out.println();
                if (verbose > 1) {
                    System.out.println(contextOntology);
                    System.out.println("---------------------------------------------");
                }
            }

            ContextReasoner reasoner = new ContextReasoner(contextOntology);
            if (verbose > 1) {
                System.out.println("reasoner.getDLOntology().getDLClauses() = ");
                reasoner.getDLOntology().getDLClauses().forEach(System.out::println);
                System.out.println("reasoner.getDLOntology().getPositiveFacts() = " + reasoner.getDLOntology().getPositiveFacts());
                System.out.println("reasoner.getDLOntology().getNegativeFacts() = " + reasoner.getDLOntology().getNegativeFacts());
                System.out.println("---------------------------------------------");
            }

            boolean result = reasoner.isConsistent();

            if (verbose > 0) {
                System.out.println("The context ontology is " + (result ? "" : "not ") + "consistent");
            }

            System.exit(result ? 0 : 42);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(13);
        //} catch (java.io.FileNotFoundException e) {
        //    System.err.println("File not found:" + file);
        } catch (OutOfMemoryError e) {
            System.exit(127);
        }
    }
}

