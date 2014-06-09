package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.configuration.ConfigFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InheritanceHelper
{

    private InheritanceHelper()
    {
    }

    /**
     * Creates a new list that contains all of the functions in the child list
     * and all of the functions in the parent list <i>that have no analogous
     * equivalent</i> in the child list.
     *
     * <p>Both lists may not contain null elements.
     *
     * @param childList  The child list.
     * @param parentList The parent list.
     * @param <T>        Holder of the config functions.
     * @param <C>        Type implementing the config functions.
     * @return The merged list.
     */
    public static final <T, C extends ConfigFunction<T>> List<C> mergeLists(
            Collection<? extends C> childList, Collection<? extends C> parentList)
    {
        List<C> returnList = new ArrayList<C>(childList);
        for (C parentFunction : parentList)
        {
            if (!hasAnalogousFunction(parentFunction, childList))
            {
                returnList.add(parentFunction);
            }
        }
        return returnList;
    }

    /**
     * Checks if the given list contains a function that is analogous to the
     * given function.
     *
     * @param function The function to check. May not be null.
     * @param list     The list to check. May not contain null elements.
     * @param <T>      Holder of the config functions.
     * @param <C>      Type implementing the config functions.
     * @return True if the list contains an analogous function, false
     * otherwise.
     */
    private static final <T, C extends ConfigFunction<T>> boolean hasAnalogousFunction(C function, Collection<? extends C> list)
    {
        for (C toCheck : list)
        {
            if (function.isAnalogousTo(toCheck))
            {
                return true;
            }
        }
        return false;
    }

}
