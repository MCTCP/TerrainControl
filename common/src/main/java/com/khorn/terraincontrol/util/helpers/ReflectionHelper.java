package com.khorn.terraincontrol.util.helpers;

import java.lang.reflect.Field;

/**
 * Implementations of Terrain Control can use methods in this class to make
 * using reflection easier. Reflection should be avoided where possible, but
 * when dealing with Minecraft there's sometimes no other option.
 *
 */
public final class ReflectionHelper
{

    private ReflectionHelper()
    {
        // No instances
    }

    /**
     * Sets the first field on the given object of the given field type to the
     * given value. For example, {@code setFirstFieldOfType(structure,
     * boolean.class, true)} will set the first boolean of the structure
     * object to true.
     *
     * @param on        The object to change a field on.
     * @param fieldType The type of the field that must be changed.
     * @param newValue  The value to set the field to.
     * @throws RuntimeException When no field of that type exists, or when the
     * field could not be set for whatever reason.
     */
    public static <T> void setFirstFieldOfType(Object on, Class<? super T> fieldType, T newValue)
    {
        // As getDeclaredFields() only returns fields declared in the class,
        // we also need to search parent classes
        Class<?> onClass = on.getClass();
        while (onClass != null)
        {
            for (Field field : onClass.getDeclaredFields())
            {
                if (field.getType().equals(fieldType))
                {
                    try
                    {
                        field.setAccessible(true);
                        field.set(on, newValue);
                        return;
                    } catch (Exception e)
                    {
                        throw new RuntimeException("Could not set field of type " + fieldType + ": " + e.getMessage(), e);
                    }
                }
            }

            onClass = onClass.getSuperclass();
        }
        throw new RuntimeException("No field of type " + fieldType + " on " + on + "(" + on.getClass() + ")");
    }
}
