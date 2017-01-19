package de.tudresden.inf.lat.jconht.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */


// wird eigentlich nicht mehr verwendet und kann weg
public class Powerset<T> implements Iterator<Powerset>, Iterable<Powerset> {

    private int currentIndex;
    private LinkedList<T> input;
    private Set<T> subset;

    private Powerset(LinkedList<T> input, int index) {
        this.input = input;
        update(index);
    }

    public static <T> Stream<Powerset> powersetStream(LinkedList<T> input) {

        return StreamSupport.stream(
                new Powerset<>(input, 0).spliterator(),
                false);
    }

    public static <T> Stream<Set<T>> powerset(LinkedList<T> input) {
        return powersetStream(input).map(Powerset::getSubset);
    }

    private void update(int currentIndex) {
        this.currentIndex = currentIndex;
        String binaryWord = Integer.toBinaryString(currentIndex);
        while (binaryWord.length() < input.size()) {
            binaryWord = "0" + binaryWord;
        }
        subset = new HashSet<>();
        for (int j = 0; j < input.size(); j++) {
            if (binaryWord.charAt(j) == '1') {
                subset.add(input.get(j));
            }
        }
    }

    @Override
    public boolean hasNext() {

        return currentIndex < Math.pow(2, input.size());
    }

    @Override
    public Powerset next() {

        update(currentIndex + 1);
        return new Powerset<>(input, currentIndex - 1);
    }

    private Set<T> getSubset() {

        return subset;
    }

    @Override
    public String toString() {

        String binaryWord = Integer.toBinaryString(currentIndex);
        while (binaryWord.length() < input.size()) {
            binaryWord = "0" + binaryWord;
        }

        return currentIndex + " -> " + binaryWord + ": " + subset;
    }

    @Override
    public Iterator<Powerset> iterator() {

        return this;
    }

}
