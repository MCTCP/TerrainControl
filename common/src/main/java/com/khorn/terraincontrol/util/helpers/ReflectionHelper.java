package com.khorn.terraincontrol.util.helpers;

import java.lang.reflect.Field;

/**
 * Implementations of Terrain Control can use methods in this class to make
 * using reflection easier. Reflection should be avoided where possible, but
 * when dealing with Minecraft there's sometimes no other option.
 */
public final class ReflectionHelper
{

    private ReflectionHelper()
    {
        // No instances
    }

    /**
     * Sets the field on the given object of the given field type to the given
     * value. For example, {@code setValueInFieldOfType(structure,
     * boolean.class, true)} will set the boolean of the structure object to
     * true.
     *
     * @param on        The object to change a field on.
     * @param fieldType The type of the field that must be changed.
     * @param newValue  The value to set the field to.
     * @throws NoSuchFieldError When no field of that type exists, or when two
     *                          or more fields of that type exist.
     */
    public static <T> void setValueInFieldOfType(Object on, Class<? super T> fieldType, T newValue)
    {
        Field field = getOnlyFieldDefOfType(on.getClass(), fieldType);
        try
        {
            field.setAccessible(true);
            field.set(on, newValue);
        } catch (IllegalAccessException e)
        {
            // Cannot happen, we just made the field accessible
            throw new AssertionError(e);
        }
    }

    /**
     * Gets the value of the field in the class with the given type.
     *
     * @param on        The object to retrieve a value from.
     * @param fieldType The field type.
     * @param <T>       The same field type.
     * @return The value.
     * @throws NoSuchFieldError When no field of the given type exists in the
     *                          class, or when two or more field of that type
     *                          exist.
     */
    public static <T> T getValueInFieldOfType(Object on, Class<T> fieldType)
    {
        Field field = getOnlyFieldDefOfType(on.getClass(), fieldType);
        try
        {
            field.setAccessible(true);
            // We can't use fieldType.cast(...) instead of an unchecked cast to
            // T: the cast method does not work for primitive types (i.e.
            // int.class), as field.get always returns a boxed type
            @SuppressWarnings("unchecked")
            T fieldValue = (T) field.get(on);
            return fieldValue;
        } catch (IllegalAccessException e)
        {
            // Cannot happen, we just made the field accessible
            throw new AssertionError(e);
        }
    }

    private static Field getOnlyFieldDefOfType(Class<?> searchClass, Class<?> fieldType)
    {
        // As getDeclaredFields() only returns fields declared in the class,
        // we also need to search parent classes
        Class<?> onClass = searchClass;
        Field result = null;
        while (onClass != null)
        {
            for (Field field : onClass.getDeclaredFields())
            {
                if (!field.getType().equals(fieldType))
                {
                    continue;
                }

                if (result != null)
                {
                    throw new NoSuchFieldError("Two fields of type " + fieldType + " in " + onClass + ": " + field.getName() + " and " + result.getName());
                }

                result = field;
            }

            if (result != null)
            {
                // Found single field in class, stop searching
                break;
            }

            // Not yet found, continue search in super class
            onClass = onClass.getSuperclass();
        }

        if (result == null)
        {
            throw new NoSuchFieldError("Found no field of type " + fieldType + " in " + searchClass);
        }

        return result;
    }
}
