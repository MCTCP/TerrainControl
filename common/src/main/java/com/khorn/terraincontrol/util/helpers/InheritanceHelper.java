package com.khorn.terraincontrol.util.helpers;

public class InheritanceHelper
{

    private InheritanceHelper()
    {
    }

    public static String evaluate(String childValue, String parentValue)
    {
        return childValue.replaceAll("(?i)inherited", parentValue);
    }

}
