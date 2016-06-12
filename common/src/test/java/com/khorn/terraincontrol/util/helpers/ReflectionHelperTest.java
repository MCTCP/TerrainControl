package com.khorn.terraincontrol.util.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rutger on 11-3-2016.
 */
public class ReflectionHelperTest
{
    // Just an easy to follow class hierarchy:
    public static class Animal
    {
        @SuppressWarnings("unused") // Used by reflection
        private boolean hungry = false;
        protected List<Animal> friends = new ArrayList<Animal>();
    }

    public static class Monkey extends Animal
    {
        private String name = "Bokito";
        private boolean isClimbing = true;
    }

    public static class Dog extends Animal
    {
        @SuppressWarnings("unused") // Used by reflection
        private String name = "Pluto";
        @SuppressWarnings("unused") // Used by reflection
        private String owner = "Mickey";
    }

    /**
     * Simplest use case: retrieve a field stored in the class itself.
     */
    @Test
    public void testGet()
    {
        Monkey monkey = new Monkey();
        String name = ReflectionHelper.getValueInFieldOfType(monkey, String.class);
        assertEquals(monkey.name, name);
    }

    /**
     * Tests whether the list (stored in a parent class) is found.
     */
    @Test
    public void testGetInParent()
    {
        Monkey monkey = new Monkey();
        List<?> friends = ReflectionHelper.getValueInFieldOfType(monkey, List.class);
        assertEquals(monkey.friends, friends);
    }

    /**
     * Dog has two fields of type String. We can't known which one should be
     * retrieved.
     */
    @Test(expected = NoSuchFieldError.class)
    public void testAmbiguousGet()
    {
        Dog dog = new Dog();
        ReflectionHelper.getValueInFieldOfType(dog, String.class);
    }

    /**
     * Monkey has two fields of type boolean. In {@link #testAmbiguousGet()},
     * this was a problem. However, in this case one of the booleans in stored
     * in the parent class, so it is clear which field should be retrieved.
     */
    @Test
    public void testActuallyNotAmbiguousGet()
    {
        Monkey monkey = new Monkey();
        boolean isClimbing = ReflectionHelper.getValueInFieldOfType(monkey, boolean.class);
        assertEquals(monkey.isClimbing, isClimbing);
    }

    /**
     * Simple use case: change a field. The same (somewhat problematic) field as
     * in {@link #testActuallyNotAmbiguousGet()} is used.
     */
    @Test
    public void testSet()
    {
        Monkey monkey = new Monkey();

        // First change field the normal way
        monkey.isClimbing = false;
        assertEquals(false, monkey.isClimbing);

        // Then set it back using reflection
        ReflectionHelper.setValueInFieldOfType(monkey, boolean.class, true);
        assertEquals(true, monkey.isClimbing);
    }
}
