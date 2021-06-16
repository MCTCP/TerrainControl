package com.pg85.otg.gen.surface;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IMaterialReader;

/**
 * Setting that handles the {@link SurfaceGenerator}.
 *
 */
public class SurfaceGeneratorSetting extends Setting<SurfaceGenerator>
{
	public static final Setting<SurfaceGenerator> SURFACE_AND_GROUND_CONTROL = surfaceGeneratorSetting("SurfaceAndGroundControl");	

	private SurfaceGeneratorSetting(String name)
	{
		super(name);
	}

	@Override
	public SurfaceGenerator getDefaultValue(IMaterialReader materialReader)
	{
		return new SimpleSurfaceGenerator();
	}

	@Override
	public SurfaceGenerator read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (string.length() > 0)
		{
			SurfaceGenerator mesa = MesaSurfaceGenerator.getFor(string);
			if (mesa != null)
			{
				return mesa;
			}

			SurfaceGenerator iceberg = IcebergSurfaceGenerator.getFor(string);
			if (iceberg != null)
			{
				return iceberg;
			}

			String[] parts = StringHelper.readCommaSeperatedString(string);
			return new MultipleLayersSurfaceGenerator(parts, materialReader);
		}
		return new SimpleSurfaceGenerator();
	}
	
	/**
	 * Creates a setting that represents a {@link SurfaceGenerator}.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	private static final Setting<SurfaceGenerator> surfaceGeneratorSetting(String name)
	{
		return new SurfaceGeneratorSetting(name);
	}
}
