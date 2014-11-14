package io.herd.base;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class CompositeIterableTest {

    private static class A {
        List<B> bs = new ArrayList<B>();
    }

    private static class B {

    }

    public static Iterator<B> getBs(A a) {
        return a.bs.iterator();
    }

    @Test
    public void testEmptyList() {

        List<A> aList = new ArrayList<A>();
        int count = 0;
        for(B b : Iterables.composite(aList, CompositeIterableTest::getBs)) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    public void testEmptyBList() {

        List<A> aList = new ArrayList<A>();
        aList.add(new A());
        int count = 0;
        for(B b : Iterables.composite(aList, CompositeIterableTest::getBs)) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    public void testSingleElementList() {

        A a = new A();
        a.bs.add(new B());
        List<A> aList = new ArrayList<A>();
        aList.add(a);
        int count = 0;
        for(B b : Iterables.composite(aList, CompositeIterableTest::getBs)) {
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void test2ElementList() {

        A a = new A();
        a.bs.add(new B());
        a.bs.add(new B());
        List<A> aList = new ArrayList<A>();
        aList.add(a);
        int count = 0;
        for(B b : Iterables.composite(aList, CompositeIterableTest::getBs)) {
            count++;
        }
        assertEquals(2, count);
    }
}
