package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFunction;

/**
 * Represents a BO3 function - a ConfigFunction with a BO3 as holder. It can be
 * rotated.
 */
public abstract class BO3Function extends ConfigFunction<BO3Config>
{

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

    /**
     * Returns a new BO3Function that is rotated 90 degrees.
     * <p/>
     * Note: the BO3Functons can have a magical link: if you change something on
     * the rotated one, it may also change on the original and vice versa.
     * <p/>
     * @return A new BlockFunction that is rotated 90 degrees.
     */
    public abstract BO3Function rotate();

}
