package de.tudresden.inf.lat.jconht.model;

import de.tudresden.inf.lat.jconht.tableau.AxiomRenamer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private OWLAxiom ax1;
    private OWLAxiom ax2;
    private OWLAxiom ax3;
    private OWLAxiom ax4;
    private OWLAxiom ax5;

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

        ax1 = dataFactory.getOWLSubClassOfAxiom(clsA, clsB);
        ax2 = dataFactory.getOWLSubClassOfAxiom(clsA, clsC);
        ax3 = dataFactory.getOWLSubClassOfAxiom(clsB, clsC);
        ax4 = dataFactory.getOWLSubClassOfAxiom(
                dataFactory.getOWLObjectSomeValuesFrom(roleR, clsC),
                dataFactory.getOWLObjectAllValuesFrom(roleR, clsC));
        ax5 = dataFactory.getOWLSubClassOfAxiom(clsA,
                dataFactory.getOWLObjectSomeValuesFrom(roleS, dataFactory.getOWLThing()));

    }

    @After
    public void tearDown() throws Exception {

        dataFactory.purge();
        manager.clearOntologies();
    }

    @Test
    public void test1() throws Exception {

        OWLOntology ontology1 = manager.createOntology(Stream.of(ax1, ax2, ax3, ax4, ax5));

        AxiomRenamer renamer = new AxiomRenamer(ontology1);
        Set<OWLEntity> flexibleNames = new HashSet<>(Arrays.asList(clsC, roleR));
        renamer.rename(flexibleNames, 3);

        OWLOntology ontology2 = manager.createOntology(Stream.of(
                ax1,
                dataFactory.getOWLSubClassOfAxiom(clsA, dataFactory.getOWLClass("cls:C_3")),
                dataFactory.getOWLSubClassOfAxiom(clsB, dataFactory.getOWLClass("cls:C_3")),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLObjectSomeValuesFrom(
                                dataFactory.getOWLObjectProperty("rol:r_3"),
                                dataFactory.getOWLClass("cls:C_3")),
                        dataFactory.getOWLObjectAllValuesFrom(
                                dataFactory.getOWLObjectProperty("rol:r_3"),
                                dataFactory.getOWLClass("cls:C_3"))),
                ax5));

        assertEquals(ontology1.axioms().collect(Collectors.toSet()),
                ontology2.axioms().collect(Collectors.toSet()));

    }

    @Test
    public void testNoOriginalNamesInResult() throws Exception {

        OWLOntology ontology = manager.createOntology(Stream.of(ax1, ax2, ax3, ax4, ax5));
        AxiomRenamer renamer = new AxiomRenamer(ontology);
        Set<OWLEntity> flexibleNames = new HashSet<>(Arrays.asList(clsC, roleR));
        renamer.rename(flexibleNames, 3);

        assertTrue("After renaming the original concepts cannot appear in ontology",
                ontology.signature().noneMatch(owlEntity -> flexibleNames.contains(owlEntity)));

    }

    //todo Angenommen in original onto ist C ⊑ D, beide non-rigid (und es gibt mehrere Meta-Welten).
    // Dann muss es C1 ⊑ D1, C2 ⊑ D2, usw geben, aber nicht C1 ⊑ D2. Kann man das irgendwie testen? Ist
    // ja nur syntaktisch.
}
