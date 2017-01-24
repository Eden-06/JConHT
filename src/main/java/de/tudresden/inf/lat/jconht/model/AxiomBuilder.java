package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomBuilder {

    private final static String regexIRICompliant = "((^[A-Za-z0-9]+)|(^[⊥⊤]))";
    private OWLDataFactory dataFactory;
    private String rolePrefix;
    private String individualPrefix;
    private String conceptPrefix;

    public AxiomBuilder(OWLDataFactory dataFactory, String rolePrefix, String individualPrefix, String conceptPrefix) {
        this.dataFactory = dataFactory;
        this.rolePrefix = rolePrefix;
        this.individualPrefix = individualPrefix;
        this.conceptPrefix = conceptPrefix;
    }

    public AxiomBuilder(OWLDataFactory dataFactory) {

        this(dataFactory, "rol:", "ind:", "cls:");
    }


    public OWLAxiom stringToOWLAxiom(String string) {

        // This part is about GCIs
        int indexSubseteq = string.indexOf('⊑');
        if (indexSubseteq != -1) {
            return dataFactory.getOWLSubClassOfAxiom(
                    this.stringToConcept(string.substring(0, indexSubseteq)),
                    this.stringToConcept(string.substring(indexSubseteq + 1)));
        }

        // Then it must be some assertion axiom
        if (string.matches(".*\\(.*\\)")) {
            int indexClose = string.lastIndexOf(')');
            int indexOpen = indexOfOpeningParenthesis(string).orElseThrow(
                    () -> new AxiomBuilderException("\nAssertion Axiom must have openening '(': "
                            + string));
            String[] indString = string.substring(indexOpen + 1, indexClose).trim().split(",");
            if (indString.length == 1) {
                // Concept assertion
                if (!indString[0].trim().matches(regexIRICompliant)) {
                    throw new AxiomBuilderException(
                            "\nThis string is not allowed as IRI for the individual: " + indString[0]);
                }
                return dataFactory.getOWLClassAssertionAxiom(
                        stringToConcept(string.substring(0, indexOpen)),
                        dataFactory.getOWLNamedIndividual(individualPrefix + indString[0].trim()));
            } else if (indString.length == 2) {
                // Role assertion
                if (!indString[0].trim().matches(regexIRICompliant)) {
                    throw new AxiomBuilderException(
                            "\nThis string is not allowed as IRI for the individual: " + indString[0]);
                }
                if (!indString[1].trim().matches(regexIRICompliant)) {
                    throw new AxiomBuilderException(
                            "\nThis string is not allowed as IRI for the individual: " + indString[1]);
                }
                return dataFactory.getOWLObjectPropertyAssertionAxiom(
                        stringToRole(string.substring(0, indexOpen)),
                        dataFactory.getOWLNamedIndividual(individualPrefix + indString[0].trim()),
                        dataFactory.getOWLNamedIndividual(individualPrefix + indString[1].trim()));
            } else {
                throw new AxiomBuilderException(
                        "\nAn assertion axiom must have one or two individuals: " +
                                Arrays.toString(indString)
                );
            }
        }

        throw new AxiomBuilderException("\nUnhandled axiom type: " + string);
    }

    public OWLClassExpression stringToConcept(String string) {

        return new OWLStringExpression(string).getClassExpression();
    }

    public OWLObjectPropertyExpression stringToRole(String string) {

        string = string.trim();

        if (string.matches("\\(.*\\)")) {
            return stringToRole(string.substring(1, string.length() - 1));
        }

        if (!string.matches(regexIRICompliant + "(\\^-1)?"))
            throw new AxiomBuilderException("\nIllegal character in role construction.\n" +
                    "A role name can consist of [a-zA-Z0-9]. Inverse roles are denoted by ^-1 at the end: " +
                    string);

        switch (string) {
            case "⊤":
                return dataFactory.getOWLTopObjectProperty();
            case "⊥":
                return dataFactory.getOWLBottomObjectProperty();
            case "⊤^-1":
                return dataFactory.getOWLObjectInverseOf(
                        dataFactory.getOWLTopObjectProperty());
            case "⊥^-1":
                return dataFactory.getOWLObjectInverseOf(
                        dataFactory.getOWLBottomObjectProperty());
            default:
                if (string.endsWith("^-1")) {
                    return dataFactory.getOWLObjectInverseOf(
                            dataFactory.getOWLObjectProperty(
                                    rolePrefix + string.substring(0, string.indexOf('^'))));
                } else {
                    return dataFactory.getOWLObjectProperty(rolePrefix + string);
                }
        }

    }

    private int countMatches(String str, String c) {
        return str.length() - str.replace(c, "").length();
    }

    /**
     * Given a string that contains '(', this method returns the index of the corresponding ')'.
     * If several '(' exist, it takes the first. If none exists, it returns Optional.empty().
     *
     * @param str input string.
     * @return index of closing parenthesis.
     */
    private Optional<Integer> indexOfClosingParenthesis(String str) {

        int startIndex = str.indexOf("(");

        if (startIndex == -1) {
            return Optional.empty();
        }

        int openOnes = 0;
        for (int i = startIndex; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                openOnes++;
            } else if (str.charAt(i) == ')') {
                openOnes--;
            }
            if (openOnes == 0) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /**
     * Given a string that contains ')', this method returns the index of the corresponding '('.
     * If several ')' exist, it takes the last. If none exists, it returns Optional.empty().
     *
     * @param str input string.
     * @return index of opening parenthesis.
     */
    private Optional<Integer> indexOfOpeningParenthesis(String str) {

        int startIndex = str.lastIndexOf(")");

        if (startIndex == -1) {
            return Optional.empty();
        }

        int openOnes = 0;
        for (int i = startIndex; i >= 0; i--) {
            if (str.charAt(i) == ')') {
                openOnes++;
            } else if (str.charAt(i) == '(') {
                openOnes--;
            }
            if (openOnes == 0) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private enum OWLStringExpressionType {
        ATOMIC("ATOMIC"),
        CONJUNCTION("CONJUNCTION"),
        DISJUNCTION("DISJUNCTION"),
        NEGATION("NEGATION"),
        PARENTHESIS("PARENTHESIS"),
        EXISTENTIAL("EXISTENTIAL RESTRICTION"),
        VALUE("VALUE RESTRICTION"),
        UNKNOWN("UNKNOWN");

        private String name;

        OWLStringExpressionType(String str) {
            name = str;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class OWLStringExpression {

        String string;
        Optional<OWLStringExpression> left;
        Optional<OWLStringExpression> right;
        OWLStringExpressionType type;


        public OWLStringExpression(String input) {
            this.string = input.trim();
            this.left = Optional.empty();
            this.right = Optional.empty();
            this.type = OWLStringExpressionType.UNKNOWN;

            if (countMatches(string, "(") != countMatches(string, ")")) {
                throw new AxiomBuilderException("Unbalanced parenthesis in: " + string);
            }

            if (string.startsWith("(") && (indexOfClosingParenthesis(string).orElse(-1) == string.length() - 1)) {
                type = OWLStringExpressionType.PARENTHESIS;
                return;
            }

            String matcher = string;
            int indexOpen = matcher.indexOf("(");
            while (indexOpen != -1) {
                int indexClose = indexOfClosingParenthesis(matcher).orElse(-1);
                String replacer = matcher.substring(indexOpen, indexClose + 1);
                matcher = matcher.replace(replacer, String.join("", Collections.nCopies(replacer.length(), "-")));
                indexOpen = matcher.indexOf("(");
            }

            if (matcher.indexOf('⊓') != -1) {
                type = OWLStringExpressionType.CONJUNCTION;
                int i = matcher.indexOf('⊓');
                left = Optional.of(new OWLStringExpression(string.substring(0, i)));
                right = Optional.of(new OWLStringExpression(string.substring(i + 1)));
                return;
            }

            if (matcher.indexOf('⊔') != -1) {
                type = OWLStringExpressionType.DISJUNCTION;
                int i = matcher.indexOf('⊔');
                left = Optional.of(new OWLStringExpression(string.substring(0, i)));
                right = Optional.of(new OWLStringExpression(string.substring(i + 1)));
                return;
            }

            if (string.startsWith("∃")) {
                type = OWLStringExpressionType.EXISTENTIAL;
                return;
            }

            if (string.startsWith("∀")) {
                type = OWLStringExpressionType.VALUE;
                return;
            }

            if (string.startsWith("¬")) {
                type = OWLStringExpressionType.NEGATION;
                left = Optional.of(new OWLStringExpression(string.substring(1)));
                return;
            }

            if (string.matches(regexIRICompliant)) {
                type = OWLStringExpressionType.ATOMIC;
                return;
            }


        }

        public OWLClassExpression getClassExpression() {

            if (type == OWLStringExpressionType.ATOMIC) {
                if (!string.matches(regexIRICompliant)) {
                    throw new AxiomBuilderException("Unhandled atomic concept string:" + string);
                }
                if (string.equals("⊤")) {
                    return dataFactory.getOWLThing();
                }
                if (string.equals("⊥")) {
                    return dataFactory.getOWLNothing();
                }
                return dataFactory.getOWLClass(IRI.create(conceptPrefix + string));
            }

            if (type == OWLStringExpressionType.CONJUNCTION) {
                return dataFactory.getOWLObjectIntersectionOf(
                        left.get().getClassExpression(),
                        right.get().getClassExpression());
            }

            if (type == OWLStringExpressionType.DISJUNCTION) {
                return dataFactory.getOWLObjectUnionOf(
                        left.get().getClassExpression(),
                        right.get().getClassExpression());
            }

            if (type == OWLStringExpressionType.NEGATION) {
                return dataFactory.getOWLObjectComplementOf(
                        left.get().getClassExpression());
            }

            if (type == OWLStringExpressionType.EXISTENTIAL) {
                int indexDot = string.indexOf('.');
                if (indexDot == -1)
                    throw new AxiomBuilderException("Existential restriction must have a dot: " + string);
                return dataFactory.getOWLObjectSomeValuesFrom(
                        stringToRole(string.substring(1, indexDot)),
                        stringToConcept(string.substring(indexDot + 1)));
            }

            if (type == OWLStringExpressionType.VALUE) {
                int indexDot = string.indexOf('.');
                if (indexDot == -1)
                    throw new AxiomBuilderException("Value restriction must have a dot: " + string);
                return dataFactory.getOWLObjectAllValuesFrom(
                        stringToRole(string.substring(1, indexDot)),
                        stringToConcept(string.substring(indexDot + 1)));
            }

            if (type == OWLStringExpressionType.PARENTHESIS) {
                return stringToConcept(string.substring(1, string.length() - 1));
            }

            throw new AxiomBuilderException("Unhandled OWLStringExpressionType\n"
                    + "in getClassExpression(): " + type + " in " + string);
        }

        @Override
        public String toString() {
            return type.toString() + ": " + string;
        }
    }

}

