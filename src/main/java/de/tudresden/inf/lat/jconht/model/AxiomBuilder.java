package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class AxiomBuilder {

    private final static String regexConceptIRICompliant = "((^[A-Za-z0-9_.]+)|(^[⊥⊤]))";
    private final static String regexRoleIRICompliant = "((^[A-Za-z0-9_]+)|(^[⊥⊤]))(\\^-1)?";
    private final static String regexIndividualIRICompliant = "[A-Za-z0-9]+";
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

        string = string.trim();

        Set<OWLAnnotation> annotations = new HashSet<>();
        // Annotated axioms
        int indexAt = string.indexOf('@');
        if (indexAt != -1) {
            annotations.add(stringToOWLAnnotation(string.substring(indexAt + 1)));
            string = string.substring(0, indexAt).trim();
        }

        // This part is about GCIs
        int indexSubseteq = string.indexOf('⊑');
        if (indexSubseteq != -1) {
            return dataFactory.getOWLSubClassOfAxiom(
                    this.stringToConcept(string.substring(0, indexSubseteq)),
                    this.stringToConcept(string.substring(indexSubseteq + 1)),
                    annotations);
        }

        // Then it must be some assertion axiom
        if (string.matches(".*\\(.*\\)")) {
            int indexClose = string.lastIndexOf(')');
            int indexOpen = indexOfMatchingParenthesis(string, ')', '(', false).orElseThrow(
                    () -> new AxiomBuilderException("\nAssertion Axiom must have matching openening '(': "
                    ));
            String[] indString = string.substring(indexOpen + 1, indexClose).trim().split(",");
            if (indString.length == 1) {
                // Concept assertion
                return dataFactory.getOWLClassAssertionAxiom(
                        stringToConcept(string.substring(0, indexOpen)),
                        stringToIndividual(indString[0]),
                        annotations);
            } else if (indString.length == 2) {
                // Role assertion
                return dataFactory.getOWLObjectPropertyAssertionAxiom(
                        stringToRole(string.substring(0, indexOpen)),
                        stringToIndividual(indString[0]),
                        stringToIndividual(indString[1]),
                        annotations);
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

    public OWLIndividual stringToIndividual(String string) {

        if (string.trim().matches(regexIndividualIRICompliant)) {
            return dataFactory.getOWLNamedIndividual(individualPrefix, string.trim());
        } else {
            throw new AxiomBuilderException(
                    "\nThis string is not allowed as IRI for an individual: " + string);
        }
    }

    public OWLObjectPropertyExpression stringToRole(String string) {

        string = string.trim();

        if (string.matches("\\(.*\\)")) {
            return stringToRole(string.substring(1, string.length() - 1));
        }

        if (!string.matches(regexRoleIRICompliant))
            throw new AxiomBuilderException(
                    "\nThis string is not allowed as IRI for a role: " + string +
                            "\nAllowed are [a-zA-Z0-9]. Inverse roles are denoted by ^-1 at the end\n");

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

    public OWLAnnotation stringToOWLAnnotation(String string) {

        OWLLiteral objectGlobal = dataFactory.getOWLLiteral("objectGlobal");

        switch (string.trim()) {
            case "global":
                return dataFactory.getRDFSLabel(objectGlobal);
            default:
                if (!stringToConcept(string.trim()).isOWLClass()) {
                    throw new AxiomBuilderException("Unsupported annotation: " + string);
                }
                return dataFactory.getOWLAnnotation(dataFactory.getRDFSIsDefinedBy(),
                        stringToConcept(string.trim()).asOWLClass().getIRI());

        }

    }

    private int countMatches(String str, String c) {
        return str.length() - str.replace(c, "").length();
    }

    /**
     * Given a string that contains parentheses, this method returns the index of the corresponding other parenthesis.
     * If several  exist, it takes the first. If none exists, it returns Optional.empty().
     *
     * @param str               input string.
     * @param firstParenthesis  the opening parenthesis, could also be ')' if {@code forward} is false.
     * @param secondParenthesis the corresponding closing parenthesis.
     * @param forward           true, if search should go from start of string to the end.
     * @return index of corresponding parenthesis.
     */
    private Optional<Integer> indexOfMatchingParenthesis(String str,
                                                         char firstParenthesis,
                                                         char secondParenthesis,
                                                         boolean forward) {

        int startIndex = forward ?
                str.indexOf(firstParenthesis) :
                str.lastIndexOf(firstParenthesis);

        if (startIndex == -1) {
            return Optional.empty();
        }

        Predicate<Integer> finished = forward ?
                i -> i < str.length() :
                i -> i >= 0;

        int openOnes = 0;
        for (int i = startIndex; finished.test(i); i = i + (forward ? 1 : -1)) {
            if (str.charAt(i) == firstParenthesis) {
                openOnes++;
            } else if (str.charAt(i) == secondParenthesis) {
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
        NOMINAL("NOMINAL"),
        MINCARD("MINCARD"),
        MAXCARD("MAXCARD"),
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
            string = input.trim();
            left = Optional.empty();
            right = Optional.empty();

            if (countMatches(string, "(") != countMatches(string, ")")) {
                throw new AxiomBuilderException("\nUnbalanced parenthesis (...) in: " + string);
            }

            if (countMatches(string, "{") != countMatches(string, "}")) {
                throw new AxiomBuilderException("\nUnbalanced parenthesis {...} in: " + string);
            }

            if (string.startsWith("(") &&
                    (indexOfMatchingParenthesis(string, '(', ')', true).orElse(-1) == string.length() - 1)) {
                type = OWLStringExpressionType.PARENTHESIS;
                return;
            }

            if (string.startsWith("{") &&
                    (indexOfMatchingParenthesis(string, '{', '}', true).orElse(-1) == string.length() - 1)) {
                type = OWLStringExpressionType.NOMINAL;
                return;
            }

            String matcher = string;
            int indexOpen = matcher.indexOf("(");
            while (indexOpen != -1) {
                int indexClose = indexOfMatchingParenthesis(matcher, '(', ')', true).orElse(-1);
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

            if (string.startsWith("≥")) {
                type = OWLStringExpressionType.MINCARD;
                return;
            }

            if (string.startsWith("≤")) {
                type = OWLStringExpressionType.MAXCARD;
                return;
            }

            if (string.startsWith("¬")) {
                type = OWLStringExpressionType.NEGATION;
                left = Optional.of(new OWLStringExpression(string.substring(1)));
                return;
            }


            type = OWLStringExpressionType.ATOMIC;


        }

        public OWLClassExpression getClassExpression() {

            if (type == OWLStringExpressionType.ATOMIC) {
                if (!string.matches(regexConceptIRICompliant)) {
                    throw new AxiomBuilderException(
                            "\nThis string is not allowed as IRI for an atomic concept: " + string);
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

            if (type == OWLStringExpressionType.MINCARD || type == OWLStringExpressionType.MAXCARD) {
                int indexDot = string.indexOf('.');
                if (indexDot == -1)
                    throw new AxiomBuilderException("Cardinality restriction must have a dot: " + string);
                Matcher matcher = Pattern.compile("\\d+").matcher(string);
                if (!matcher.find())
                    throw new AxiomBuilderException("Cardinality restriction must have a number:" + string);
                //int n = Integer.valueOf(matcher.group());
                int indexLastDigit = matcher.end();
                if (indexLastDigit > indexDot)
                    throw new AxiomBuilderException("The first number in cardinality restriction must be before the dot:" + string);
                int n;
                try {
                    n = Integer.parseInt(string.substring(1,indexLastDigit).trim());
                } catch (NumberFormatException e) {
                    throw new AxiomBuilderException("NumberFormatException in:" + string);
                }

                if (type == OWLStringExpressionType.MINCARD) {
                    return dataFactory.getOWLObjectMinCardinality(n,
                            stringToRole(string.substring(indexLastDigit, indexDot)),
                            stringToConcept(string.substring(indexDot + 1)));
                } else {
                    return dataFactory.getOWLObjectMaxCardinality(n,
                            stringToRole(string.substring(indexLastDigit, indexDot)),
                            stringToConcept(string.substring(indexDot + 1)));
                }
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

            if (type == OWLStringExpressionType.NOMINAL) {
                return dataFactory.getOWLObjectOneOf(stringToIndividual(
                        string.substring(1, string.length() - 1)));
            }

            throw new AxiomBuilderException(
                    "\nUnhandled OWLStringExpressionType in getClassExpression(): " + type + " in " + string);
        }

        @Override
        public String toString() {
            return type.toString() + ": " + string;
        }
    }

}

