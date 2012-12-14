package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFunction;

public abstract class BO3Function extends ConfigFunction<BO3>
{
    public Class<BO3> getHolderType() {
        return BO3.class;
    }
}
