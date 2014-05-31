package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.ReplacedBlocksMatrix;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.generator.surface.SurfaceGenerator;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;

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
     * @see BooleanSetting
     */
    protected static final Setting<Boolean> booleanSetting(String name, boolean defaultValue)
    {
        return new BooleanSetting(name, defaultValue);
    }

    /**
     * @see ColorSetting
     */
    protected static final Setting<Integer> colorSetting(String name, String defaultValue)
    {
        return new ColorSetting(name, defaultValue);
    }

    /**
     * @see DoubleSetting
     */
    protected static final Setting<Double> doubleSetting(String name, double defaultValue, double min, double max)
    {
        return new DoubleSetting(name, defaultValue, min, max);
    }

    /**
     * @see EnumSetting
     */
    protected static final <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue)
    {
        return new EnumSetting<T>(name, defaultValue);
    }

    /**
     * @see FloatSetting
     */
    protected static final Setting<Float> floatSetting(String name, float defaultValue, float min, float max)
    {
        return new FloatSetting(name, defaultValue, min, max);
    }

    /**
     * @see IntSetting
     */
    protected static final Setting<Integer> intSetting(String name, int defaultValue, int min, int max)
    {
        return new IntSetting(name, defaultValue, min, max);
    }

    /**
     * @see LongSetting
     */
    protected static final Setting<Long> longSetting(String name, long defaultValue, long min, long max)
    {
        return new LongSetting(name, defaultValue, min, max);
    }

    /**
     * @see MaterialSetSetting
     */
    protected static final Setting<MaterialSet> materialSetSetting(String name, DefaultMaterial... defaultValues)
    {
        return new MaterialSetSetting(name, defaultValues);
    }

    /**
     * Warning: you will get an AssertionError later on (during config
     * reading) if you pass invalid materials.
     * {@link Settings#materialSetSetting(String, DefaultMaterial...)} is the
     * suggested alternative.
     * @see MaterialSetSetting
     * 
     */
    protected static final Setting<MaterialSet> materialSetSetting(String name, String... defaultValues)
    {
        return new MaterialSetSetting(name, defaultValues);
    }

    /**
     * @see MaterialSetting
     */
    protected static final Setting<LocalMaterialData> materialSetting(String name, DefaultMaterial defaultValue)
    {
        return new MaterialSetting(name, defaultValue);
    }

    /**
     * @see MobGroupListSetting
     */
    protected static final Setting<List<WeightedMobSpawnGroup>> mobGroupListSetting(String name)
    {
        return new MobGroupListSetting(name);
    }
    
    /**
     * @see ReplacedBlocksSetting
     */
    protected static final Setting<ReplacedBlocksMatrix> replacedBlocksSetting(String name)
    {
        return new ReplacedBlocksSetting(name);
    }

    /**
     * @see StringSetting
     */
    protected static final Setting<String> stringSetting(String name, String defaultValue)
    {
        return new StringSetting(name, defaultValue);
    }
    
    /**
     * @see StringListSetting
     */
    protected static final Setting<List<String>> stringListSetting(String name, String... defaultValues)
    {
        return new StringListSetting(name, defaultValues);
    }
    
    /**
     * @see SurfaceGeneratorSetting
     */
    protected static final Setting<SurfaceGenerator> surfaceGeneratorSetting(String name)
    {
        return new SurfaceGeneratorSetting(name);
    }
}
