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

    private long currentIndex;
    private LinkedList<T> input;
    private Set<T> subset;

    private Powerset(LinkedList<T> input, long index) {
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

    private void update(long currentIndex) {
        this.currentIndex = currentIndex;
        subset = new HashSet<>();
        for (int j = 0; j < input.size(); j++) {
            if ((currentIndex & (1L << j)) != 0) {
                subset.add(input.get(j));
            }
        }
    }

    @Override
    public boolean hasNext() {
        
        return ((currentIndex & (1L << input.size())) == 0);
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

        String binaryWord = Long.toBinaryString(currentIndex);
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
