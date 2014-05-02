package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.surface.MesaSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.NullSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.SimpleSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.SurfaceGenerator;
import com.khorn.terraincontrol.util.helpers.StringHelper;

/**
 * Setting that handles the {@link SurfaceGenerator}.
 *
 */
class SurfaceGeneratorSetting extends Setting<SurfaceGenerator>
{

    SurfaceGeneratorSetting(String name)
    {
        super(name);
    }

    @Override
    public SurfaceGenerator getDefaultValue()
    {
        return new NullSurfaceGenerator();
    }

    @Override
    public SurfaceGenerator read(String string) throws InvalidConfigException
    {
        if (string.length() > 0)
        {
            SurfaceGenerator mesa = MesaSurfaceGenerator.getFor(string);
            if (mesa != null)
            {
                return mesa;
            }
            String[] parts = StringHelper.readCommaSeperatedString(string);
            return new SimpleSurfaceGenerator(parts);
        }
        return new NullSurfaceGenerator();
    }

}
