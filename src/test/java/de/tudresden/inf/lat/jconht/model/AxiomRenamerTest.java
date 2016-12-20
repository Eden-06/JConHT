package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is a test class for <code>AxiomRenamer</code>.
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomRenamerTest {

    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private ReasonerFactory reasonerFactory;
    private AxiomRenamer axiomRenamer;

    private OWLClass clsA;
    private OWLClass clsB;
    private OWLClass clsC;
    private OWLClass clsD;
    private OWLIndividual indA;
    private OWLIndividual indB;
    private OWLObjectProperty roleR;
    private OWLObjectProperty roleS;

    @Before
    public void setUp() throws Exception {

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        clsA = dataFactory.getOWLClass("cls:A");
        clsB = dataFactory.getOWLClass("cls:B");
        clsC = dataFactory.getOWLClass("cls:C");
        clsD = dataFactory.getOWLClass("cls:D");
        indA = dataFactory.getOWLNamedIndividual("ind:a");
        indB = dataFactory.getOWLNamedIndividual("ind:b");
        roleR = dataFactory.getOWLObjectProperty("rol:r");
        roleS = dataFactory.getOWLObjectProperty("rol:s");
    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    @Test
    public void test1() throws Exception {

        //Todo test not finished yet

        OWLAxiom ax1 = dataFactory.getOWLSubClassOfAxiom(clsA, clsB);
        OWLAxiom ax2 = dataFactory.getOWLSubClassOfAxiom(clsA, clsC);
        OWLAxiom ax3 = dataFactory.getOWLSubClassOfAxiom(clsB, clsC);
        OWLAxiom ax4 = dataFactory.getOWLSubClassOfAxiom(
                dataFactory.getOWLObjectSomeValuesFrom(roleR, clsC),
                dataFactory.getOWLObjectAllValuesFrom(roleR, clsC));
        OWLAxiom ax5 = dataFactory.getOWLSubClassOfAxiom(clsA,
                dataFactory.getOWLObjectSomeValuesFrom(roleS,dataFactory.getOWLThing()));

        OWLOntology ontology = manager.createOntology(Stream.of(ax1, ax2, ax3, ax4, ax5));

        ontology.axioms().forEach(System.out::println);
        System.out.println("\n");

        AxiomRenamer renamer = new AxiomRenamer(ontology);
        Set<OWLEntity> flexibleNames = new HashSet<>(Arrays.asList(clsC,roleR));
        renamer.rename(flexibleNames, 3);

        ontology.axioms().forEach(System.out::println);

    }
}
