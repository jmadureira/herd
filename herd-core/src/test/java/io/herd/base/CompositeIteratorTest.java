package io.herd.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class CompositeIteratorTest {

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
        Iterator<B> it = Iterators.composite(aList.iterator(), CompositeIteratorTest::getBs);
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyBList() {

        List<A> aList = new ArrayList<A>();
        aList.add(new A());
        Iterator<B> it = Iterators.composite(aList.iterator(), CompositeIteratorTest::getBs);
        assertFalse(it.hasNext());
    }

    @Test
    public void testSingleElementList() {

        A a = new A();
        a.bs.add(new B());
        List<A> aList = new ArrayList<A>();
        aList.add(a);
        Iterator<B> it = Iterators.composite(aList.iterator(), CompositeIteratorTest::getBs);
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void test2ElementList() {

        A a = new A();
        a.bs.add(new B());
        a.bs.add(new B());
        List<A> aList = new ArrayList<A>();
        aList.add(a);
        Iterator<B> it = Iterators.composite(aList.iterator(), CompositeIteratorTest::getBs);
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }
}
