package com.khorn.terraincontrol.util.helpers;

public class LogicHelper
{

    /**
     * Returns true if both objects are equal. If both objects are null, this
     * method returns true. If one of the objects is null, this method returns
     * false. If no objects are null, this method returns true if
     * <code>o1.equals(o2)</code>.
     * 
     * 
     * The benefit of this method over {@link Object#equals(Object)} is that
     * it won't throw a NullPointerException when the first object is null.
     * 
     * @param o1 The first object.
     * @param o2 The second object.
     * @return True if the objects are equal, false otherwise.
     */
    public static boolean equals(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        }
        return o1.equals(o2);
    }

}
