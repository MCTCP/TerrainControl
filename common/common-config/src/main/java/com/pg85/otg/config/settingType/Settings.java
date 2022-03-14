package com.pg85.otg.config.settingType;

import java.util.List;

import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.biome.ReplaceBlocks;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialSet;

/**
 * Acts as a factory for creating settings. Classes holding settings must
 * extends this class and call the appropriate methods to create settings.
 *
 * <p>We might eventually want the class to keep track of all settings
 * created. For now, it just creates instances of the appropriate settings
 * type.
 */
public abstract class Settings
{	
	/**
	 * Creates a setting that can be {@code true} or {@code false}.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<Boolean> booleanSetting(String name, boolean defaultValue)
	{
		return new BooleanSetting(name, defaultValue);
	}

	/**
	 * Creates a setting that represents a RGB color.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<Integer> colorSetting(String name, String defaultValue)
	{
		return new ColorSetting(name, defaultValue);
	}

	/**
	 * Creates a setting that represents double-precision floating point number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	protected static final Setting<Double> doubleSetting(String name, double defaultValue, double min, double max)
	{
		return new DoubleSetting(name, defaultValue, min, max);
	}

	/**
	 * Creates a setting that represents one of the options in the provided enum.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	protected static final <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue)
	{
		return new EnumSetting<T>(name, defaultValue);
	}

	/**
	 * Creates a setting that represents single-precision floating point number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	protected static final Setting<Float> floatSetting(String name, float defaultValue, float min, float max)
	{
		return new FloatSetting(name, defaultValue, min, max);
	}

	/**
	 * Creates a setting that represents a whole number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	protected static final Setting<Integer> intSetting(String name, int defaultValue, int min, int max)
	{
		return new IntSetting(name, defaultValue, min, max);
	}

	protected static final Setting<Rotation> rotationSetting(String name, Rotation defaultValue)
	{
		return new RotationSetting(name, defaultValue);
	}

	/**
	 * Creates a setting that represents a whole number as a {@code long}.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	protected static final Setting<Long> longSetting(String name, long defaultValue, long min, long max)
	{
		return new LongSetting(name, defaultValue, min, max);
	}

	/**
	 * Creates a setting that represents a set of block materials.
	 * Warning: you will get an AssertionError later on (during config
	 * reading) if you provide invalid materials.
	 * {@link Settings#materialSetSetting(String, DefaultMaterial...)} is the
	 * suggested alternative.
	 * @param name		  Name of the setting.
	 * @param defaultValues Default values for the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<MaterialSet> materialSetSetting(String name, String... defaultValues)
	{
		return new MaterialSetSetting(name, defaultValues);
	}
	
	protected static final Setting<ColorSet> colorSetSetting(String name)
	{
		return new ColorSetSetting(name);
	}

	/**
	 * Creates a setting that represents a list of possible mob spawns.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<List<WeightedMobSpawnGroup>> mobGroupListSetting(String name)
	{
		return new MobGroupListSetting(name);
	}

	/**
	 * Creates a setting that represents a {@link ReplaceBlockMatrix}.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<ReplaceBlockMatrix> replacedBlocksSetting(String name)
	{
		return new ReplacedBlocksSetting(name);
	}
	
	/**
	 * Creates a setting that represents a replaceBlocks mappings list.
	 * @param value		 The setting's value as a string.
	 * @return The newly created setting.
	 */
	protected static final Setting<List<ReplaceBlocks>> replaceBlocksListSetting(String value)
	{
		return new ReplaceBlocksListSetting(value);
	}

	/**
	 * Creates a setting that represents a string of text.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<String> stringSetting(String name, String defaultValue)
	{
		return new StringSetting(name, defaultValue);
	}

	/**
	 * Creates a setting that represents a list of text strings.
	 * @param name		  Name of the setting.
	 * @param defaultValues Default values for the setting.
	 * @return The newly created setting.
	 */
	protected static final Setting<List<String>> stringListSetting(String name, String... defaultValues)
	{
		return new StringListSetting(name, defaultValues);
	}
}
