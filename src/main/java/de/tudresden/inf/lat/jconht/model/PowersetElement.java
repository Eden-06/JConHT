package de.tudresden.inf.lat.jconht.model;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */




// wird eigentlich nicht mehr verwendet und kann weg
public class PowersetElement implements Iterator<PowersetElement>, Iterable<PowersetElement> {

    private LinkedList<OWLClass> input;
    private Set<OWLClass> subset;
    int currentIndex;

    public PowersetElement(LinkedList<OWLClass> input, int index) {
        this.input = input;
        update(index);
    }

    private void update(int currentIndex) {
        this.currentIndex = currentIndex;
        String binaryWord = Integer.toBinaryString(currentIndex);
        while (binaryWord.length() < input.size()) {
            binaryWord = "0" + binaryWord;
        }
        subset = new HashSet<>();
        for (int j = 0; j < input.size(); j++) {
            if (binaryWord.charAt(j) == '0') {
                subset.add(input.get(j));
            }
        }
    }

    @Override
    public boolean hasNext() {

        return currentIndex < Math.pow(2, input.size());
    }

    @Override
    public PowersetElement next() {

        update(currentIndex + 1);
        return new PowersetElement(input,currentIndex - 1);
    }

    public Set<OWLClass> getSubset() {

        return subset;
    }

    @Override
    public String toString() {

        return Integer.toBinaryString(currentIndex) + ": " + subset;
    }

    @Override
    public Iterator<PowersetElement> iterator() {

        return this;
    }


    private static Stream<PowersetElement> powersetStream(Stream<OWLClass> input) {

        return StreamSupport.stream(
                new PowersetElement(input.collect(Collectors.toCollection(LinkedList::new)), 0).spliterator(),
                false);
    }

    public static Stream<Set<OWLClass>> powerset(Stream<OWLClass> inputStream) {
        return powersetStream(inputStream).map(PowersetElement::getSubset);
    }

}
