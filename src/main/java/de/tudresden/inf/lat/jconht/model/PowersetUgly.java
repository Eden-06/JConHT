package de.tudresden.inf.lat.jconht.model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */

// wird eigentlich nicht mehr verwendet und kann weg
public class PowersetUgly {

    public static <T> Stream<Set<T>> powerset(Set<T> input) {

        // TODO hier muss noch ganz viel gemacht werden. Das ist noch so was von gar nicht Java 8.

        Set<Set<T>> powerSet = new HashSet<>();
        powerSet.add(new HashSet<>());

        for (T item : input) {
            Set<Set<T>> newPowerSet = new HashSet<>();

            for (Set<T> subset : powerSet) {
                // Take all what you already have.
                newPowerSet.add(subset);

                // plus the subsets appended with the current item
                Set<T> newSubset = new HashSet<T>(subset);
                newSubset.add(item);
                newPowerSet.add(newSubset);
            }
            powerSet = newPowerSet;

        }

        return powerSet.stream();
    }
}
