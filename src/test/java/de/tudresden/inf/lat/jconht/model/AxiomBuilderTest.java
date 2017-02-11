package de.tudresden.inf.lat.jconht.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomBuilderTest {

    private OWLDataFactory dataFactory;
    private AxiomBuilder builder;

    @Before
    public void setUp() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        builder = new AxiomBuilder(dataFactory);
    }

    @After
    public void tearDown() throws Exception {

    }



    @Test
    public void testValidSyntaxRoles() throws Exception {
        System.out.println("Executing testValidSyntaxRoles:");

        assertTrue("S",validRoleSyntax("S"));
        assertTrue("⊤",validRoleSyntax("⊤"));
        assertTrue("⊤^-1",validRoleSyntax("⊤^-1"));
        assertTrue("S^-1",validRoleSyntax("S^-1"));
        assertTrue("(S^-1)",validRoleSyntax("(S^-1)"));
        assertTrue("ROLE",validRoleSyntax("ROLE"));
        assertFalse("S_asd",validRoleSyntax("S_asd"));
        assertFalse("(S)^-1",validRoleSyntax("(S)^-1"));
        assertFalse("((S)",validRoleSyntax("((S)"));

    }

    @Test
    public void testStringToRole() {
        System.out.println("Executing testStringToRole:");

        assertEquals("R",
                builder.stringToRole("R"),
                dataFactory.getOWLObjectProperty("rol:R"));
        assertEquals("((R))",
                builder.stringToRole("((R))"),
                dataFactory.getOWLObjectProperty("rol:R"));
        assertEquals("R^-1",
                builder.stringToRole("R^-1"),
                dataFactory.getOWLObjectInverseOf(
                        dataFactory.getOWLObjectProperty("rol:R")));

        assertEquals("⊥",
                builder.stringToRole("⊥"),
                dataFactory.getOWLBottomObjectProperty());
        assertEquals("⊤",
                builder.stringToRole("⊤"),
                dataFactory.getOWLTopObjectProperty());
        assertEquals("⊥^-1",
                builder.stringToRole("⊥^-1"),
                dataFactory.getOWLObjectInverseOf(
                        dataFactory.getOWLBottomObjectProperty()));
        assertEquals("⊤^-1",
                builder.stringToRole("⊤^-1"),
                dataFactory.getOWLObjectInverseOf(
                        dataFactory.getOWLTopObjectProperty()));

    }

    @Test
    public void testValidSyntaxConcepts() throws Exception {
        System.out.println("Executing testValidSyntaxRoles:");

        assertTrue("C",validConceptSyntax("C"));
        assertTrue("langerString",validConceptSyntax("langerString"));
        assertTrue("⊤",validConceptSyntax("⊤"));
        assertTrue("C ⊓ ⊤",validConceptSyntax("C ⊓ ⊤"));

        assertFalse("⊥S",validConceptSyntax("⊥S"));
        assertFalse("S⊥",validConceptSyntax("S⊥"));
        assertFalse("langer:String",validConceptSyntax("langer:String"));
        assertFalse("⊥⊥",validConceptSyntax("⊥⊥"));
        assertFalse("C ⊓ () ⊓ ⊤",validConceptSyntax("C ⊓ () ⊓ ⊤"));
        assertFalse("(S)^-1",validConceptSyntax("(S)^-1"));
        assertFalse("((S)",validConceptSyntax("((S)"));

        assertTrue("{a}",validConceptSyntax("{a}"));

        assertFalse("C ⊓ D @ global", validConceptSyntax("C ⊓ D @ global"));

    }

    @Test
    public void testStringToConcept() {
        System.out.println("Executing testStringToConcept:");

        assertEquals("C",
                builder.stringToConcept("C"),
                dataFactory.getOWLClass("cls:C"));
        assertEquals("⊥",
                builder.stringToConcept("⊥"),
                dataFactory.getOWLNothing());
        assertEquals("⊤",
                builder.stringToConcept("⊤"),
                dataFactory.getOWLThing());
        assertEquals("C ⊓ D",
                builder.stringToConcept("C ⊓ D"),
                dataFactory.getOWLObjectIntersectionOf(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLClass("cls:D")));
        assertEquals("C ⊔ D",
                builder.stringToConcept("C ⊔ D"),
                dataFactory.getOWLObjectUnionOf(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLClass("cls:D")));
        assertEquals("∀S.C",
                builder.stringToConcept("∀S.C"),
                dataFactory.getOWLObjectAllValuesFrom(
                        dataFactory.getOWLObjectProperty("rol:S"),
                        dataFactory.getOWLClass("cls:C")));
        assertEquals("∀ S . C",
                builder.stringToConcept("∀ S . C"),
                dataFactory.getOWLObjectAllValuesFrom(
                        dataFactory.getOWLObjectProperty("rol:S"),
                        dataFactory.getOWLClass("cls:C")));
        assertEquals("(∀ (S) . (C))",
                builder.stringToConcept("(∀ (S) . (C))"),
                dataFactory.getOWLObjectAllValuesFrom(
                        dataFactory.getOWLObjectProperty("rol:S"),
                        dataFactory.getOWLClass("cls:C")));
        assertEquals("∃S.C",
                builder.stringToConcept("∃S.C"),
                dataFactory.getOWLObjectSomeValuesFrom(
                        dataFactory.getOWLObjectProperty("rol:S"),
                        dataFactory.getOWLClass("cls:C")));
        assertEquals("¬C",
                builder.stringToConcept("¬C"),
                dataFactory.getOWLObjectComplementOf(
                        dataFactory.getOWLClass("cls:C")));
        assertEquals("¬⊤",
                builder.stringToConcept("¬⊤"),
                dataFactory.getOWLObjectComplementOf(
                        dataFactory.getOWLThing()));
        assertEquals("∃ R^-1 . ¬D",
                builder.stringToConcept("∃ R^-1 . ¬D"),
                dataFactory.getOWLObjectSomeValuesFrom(
                        dataFactory.getOWLObjectInverseOf(
                                dataFactory.getOWLObjectProperty("rol:R")),
                        dataFactory.getOWLObjectComplementOf(
                                dataFactory.getOWLClass("cls:D"))));

        assertEquals("∃ (R).(C ⊓ (D)) ⊓ D",
                builder.stringToConcept("∃ (R).(C ⊓ (D)) ⊓ D"),
                dataFactory.getOWLObjectIntersectionOf(
                        dataFactory.getOWLObjectSomeValuesFrom(
                                dataFactory.getOWLObjectProperty("rol:R"),
                                dataFactory.getOWLObjectIntersectionOf(
                                        dataFactory.getOWLClass("cls:C"),
                                        dataFactory.getOWLClass("cls:D"))),
                        dataFactory.getOWLClass("cls:D")));

        assertEquals("¬((A ⊓ B) ⊔  ¬(C ⊓ D))",
                builder.stringToConcept("¬((A ⊓ B) ⊔  ¬(C ⊓ D))"),
                dataFactory.getOWLObjectComplementOf(
                        dataFactory.getOWLObjectUnionOf(
                                dataFactory.getOWLObjectIntersectionOf(
                                        dataFactory.getOWLClass("cls:A"),
                                        dataFactory.getOWLClass("cls:B")),
                                dataFactory.getOWLObjectComplementOf(
                                        dataFactory.getOWLObjectIntersectionOf(
                                                dataFactory.getOWLClass("cls:C"),
                                                dataFactory.getOWLClass("cls:D"))))));

        assertEquals("∃ plays^-1.{counter}",
                builder.stringToConcept("∃ plays^-1.{counter}"),
                dataFactory.getOWLObjectSomeValuesFrom(
                        dataFactory.getOWLObjectInverseOf(
                                dataFactory.getOWLObjectProperty("rol:plays")),
                        dataFactory.getOWLObjectOneOf(
                                dataFactory.getOWLNamedIndividual("ind:counter"))));
    }

    @Test
    public void testValidSyntaxAxioms() throws Exception {
        System.out.println("Executing testValidSyntaxRoles:");

        assertTrue("C(a)",validAxiomSyntax("C(a)"));
        assertFalse("C()",validAxiomSyntax("C()"));
        assertFalse("C(⊥)",validAxiomSyntax("C(⊥)"));
        assertFalse("((C   )(a)", validAxiomSyntax("((C   )(a)"));

        assertTrue("r(a,b)", validAxiomSyntax("r(a,b)"));
        assertFalse("r(a,b,c)", validAxiomSyntax("r(a,b,c)"));

        assertTrue("⊥(a)",validAxiomSyntax("⊥(a)"));
        assertTrue("⊥(a,b)",validAxiomSyntax("⊥(a,b)"));

        assertTrue("C ⊑ D", validAxiomSyntax("C ⊑ D"));
        assertFalse("C ⊑ D ⊑ E", validAxiomSyntax("C ⊑ D ⊑ E"));
        assertFalse("C ⊑ A(a)", validAxiomSyntax("C ⊑ A(a)"));
        assertTrue("C ⊑ A @ global", validAxiomSyntax("C ⊑ A @ global"));
        assertTrue("C ⊑ A @ (meta1)", validAxiomSyntax("C ⊑ A @ (meta1)"));
        assertFalse("C ⊑ A @ (A ⊓ B)", validAxiomSyntax("C ⊑ A @ (A ⊓ B)"));

    }


    @Test
    public void testStringToOWLAxiom() throws Exception {
        System.out.println("Executing testStringToOWLAxiom:");

        assertEquals("C ⊑ D",
                builder.stringToOWLAxiom("C ⊑ D"),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLClass("cls:D")));

        assertEquals("r(a,b)",
                builder.stringToOWLAxiom("r(a,b)"),
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        dataFactory.getOWLObjectProperty("rol:r"),
                        dataFactory.getOWLNamedIndividual("ind:a"),
                        dataFactory.getOWLNamedIndividual("ind:b")));

        assertEquals("(r^-1)(a,b)",
                builder.stringToOWLAxiom("(r^-1)(a,b)"),
                dataFactory.getOWLObjectPropertyAssertionAxiom(
                        dataFactory.getOWLObjectInverseOf(
                                dataFactory.getOWLObjectProperty("rol:r")),
                        dataFactory.getOWLNamedIndividual("ind:a"),
                        dataFactory.getOWLNamedIndividual("ind:b")));

        assertEquals("C(a)",
                builder.stringToOWLAxiom("C(a)"),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLNamedIndividual("ind:a")));

        assertEquals("(    C)(a)",
                builder.stringToOWLAxiom("C(a)"),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLNamedIndividual("ind:a")));
    }


    @Test
    public void testGlobalObjectAxiom() throws Exception {
        System.out.println("Executing testGlobalObjectAxiom:");

        assertEquals("C ⊑ D @ global",
                builder.stringToOWLAxiom("C ⊑ D @ global"),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLClass("cls:C"),
                        dataFactory.getOWLClass("cls:D"),
                        new HashSet<>(Arrays.asList(dataFactory.getRDFSLabel("objectGlobal")))));
    }

    @Test
    public void testObjectAxiom() throws Exception {
        System.out.println("Executing testObjectAxiom:");

        assertEquals("A ⊑ ⊥ @ meta1",
                builder.stringToOWLAxiom("A ⊑ ⊥ @ meta1"),
                dataFactory.getOWLSubClassOfAxiom(
                        dataFactory.getOWLClass("cls:A"),
                        dataFactory.getOWLNothing(),
                        new HashSet<>(Arrays.asList(dataFactory.getOWLAnnotation(
                                dataFactory.getRDFSIsDefinedBy(),
                                IRI.create("cls:meta1"))))));

        assertEquals("A(a) @ meta2",
                builder.stringToOWLAxiom("A(a) @ meta2"),
                dataFactory.getOWLClassAssertionAxiom(
                        dataFactory.getOWLClass("cls:A"),
                        dataFactory.getOWLNamedIndividual("ind:a"),
                        new HashSet<>(Arrays.asList(dataFactory.getOWLAnnotation(
                                dataFactory.getRDFSIsDefinedBy(),
                                IRI.create("cls:meta2"))))));
    }

    private boolean validRoleSyntax(String string) {
        try {
            builder.stringToRole(string);
        } catch (AxiomBuilderException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    private boolean validConceptSyntax(String string) {
        try {
            builder.stringToConcept(string);
        } catch (AxiomBuilderException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    private boolean validAxiomSyntax(String string) {
        try {
            builder.stringToOWLAxiom(string);
        } catch (AxiomBuilderException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }






}


































