package de.tudresden.inf.lat.jconht.test;

import de.tudresden.inf.lat.jconht.model.Powerset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.tudresden.inf.lat.jconht.model.Powerset.powerset;

/**
 * This class ...
 *
 * @author Stephan Böhme
 * @author Marcel Lippmann
 */
public class PowersetTest {

    private OWLDataFactory dataFactory;

    @Before
    public void setUp() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

    }

    @After
    public void tearDown() throws Exception {


    }


    @Test
    public void testPowerset() throws Exception {

        OWLClass meta1 = dataFactory.getOWLClass("cls:meta1");
        OWLClass meta2 = dataFactory.getOWLClass("cls:meta2");
        OWLClass meta3 = dataFactory.getOWLClass("cls:meta3");
        OWLClass meta4 = dataFactory.getOWLClass("cls:meta4");
        OWLClass meta5 = dataFactory.getOWLClass("cls:meta5");
        LinkedList<OWLClass> owlClassList = Stream.of(meta1, meta2, meta3, meta4, meta5)
                .collect(Collectors.toCollection(LinkedList::new));
        powerset(owlClassList).forEach(System.out::println);


        LinkedList<Integer> intList = IntStream.range(1, 4).boxed().collect(Collectors.toCollection(LinkedList::new));
        Stream<Powerset> ps = Powerset.powersetStream(intList);
        ps.forEach(System.out::println);
        powerset(intList).forEach(System.out::println);


        LinkedList<Integer> intList2 = IntStream.range(1, 26).boxed().collect(Collectors.toCollection(LinkedList::new));
        System.out.println(powerset(intList2).count());
    }


    @Test
    public void tmptest() throws Exception {
        System.out.println("Executing tmptest:");

        Set<Integer> set = Stream.of(1, 2, 3).collect(Collectors.toSet());

//        long num = 12;
//
//        System.out.println("num = " + num);
//
//        System.out.println(Long.toBinaryString(num));

        long counter = 0;
        while (true) {
            for (int i = 0; i < set.size(); i++) {
                if ((counter & (1L << i)) != 0) {
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }
            System.out.println();
            counter++;
            if (counter == 20)
                break;


        }


    }
}