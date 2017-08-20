package com.pg85.otg.configuration.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.generator.surface.MultipleLayersSurfaceGenerator;
import com.pg85.otg.generator.surface.SimpleSurfaceGenerator;
import com.pg85.otg.generator.surface.SurfaceGenerator;
import com.pg85.otg.util.helpers.StringHelper;

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
        return new SimpleSurfaceGenerator();
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
            return new MultipleLayersSurfaceGenerator(parts);
        }
        return new SimpleSurfaceGenerator();
    }

}
