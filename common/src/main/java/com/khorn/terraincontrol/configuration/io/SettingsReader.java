package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.settingType.Setting;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

/**
 * This interface allows you to retrieve settings.
 *
 * <p>Null values are not permitted anywhere (parameters, return values) in
 * this class, unless noted otherwise.
 *
 * <p>All data manipulations methods must not actually save any of the data.
 * This interface is purely intended for reading. Data manipulation is
 * supported so that old configs can get rewritten in memory to a newer style,
 * before being read.
 *
 */
public interface SettingsReader
{

    /**
     * Adds a ConfigFunction to this reader, so that it can be read back
     * using {@link #getConfigFunctions(Object)}. If this reader doesn't
     * support ConfigFunctions, this method does nothing.
     *
     * @param <T> The type of the function. Must match the type used in {@link #getConfigFunctions(Object)}.
     * @param function The function to add.
     */
    <T> void addConfigFunction(ConfigFunction<T> function);

    /**
     * Gets all ConfigFunctions in this configuration. If this reader doesn't
     * support ConfigFunctions, an empty list will be returned.
     *
     * @param <T> The type of the config functions.
     * @param holder      The holder of all config functions.
     * @param useFallback True if the {@link #setFallbackReader(SettingsReader)
     * fallback reader} must be used, false otherwise.
     * @return The config functions.
     */
    <T> List<ConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback);

    /**
     * Gets the file this reader if reading from. Will be null if this reader
     * doesn't read from a file.
     *
     * @return The file, or null if not reading from a file.
     */
    File getFile();

    /**
     * Gets the name of this config file. For worlds, this is the world name,
     * for biomes this is the biome name, etc.
     *
     * @return The name of this config file.
     */
    String getName();

    /**
     * Gets all the raw settings in the object, for when some special parsing
     * is needed.
     *
     * <p>In general, this method should be avoided. The settings and config
     * functions system should already fit most use cases. The names of all
     * settings should be known beforehand.
     *
     * <p>One use case of this method is to parse some kind of legacy config
     * file with dynamic keys, like a BO2 file.
     *
     * <p>Implementations are allowed to return an empty collection, even
     * when there are settings present. Note that legacy config files may not
     * get parsed correctly then.
     */
    Iterable<Entry<String, String>> getRawSettings();

    /**
     * Reads a setting. This method allows you to provide another default
     * value. If the setting has an invalid value, a message is logged and
     * the default value is returned.
     * @param setting      The setting to read.
     * @param defaultValue Default value for the setting.
     * @return The value of the setting.
     */
    <S> S getSetting(Setting<S> setting, S defaultValue);

    /**
     * Gets whether the reader has a value of the given setting. If this
     * method returns false, trying to get the setting will return the default
     * value.
     * @param setting The setting to check for.
     * @return The setting.
     */
    boolean hasSetting(Setting<?> setting);

    /**
     * Gets whether the config file is newly created.
     *
     * <p>This is used for backwards compatibility of settings files. When a
     * new setting is introduced, and its default value doesn't match the old
     * behavior, the default value must be changed to match the old
     * behavior for old configs.
     *
     * <p>An example of this is SurfaceAndGroundControl, that should not
     * create stone on the surface of existing Extreme Hills biomes, but
     * should create stone on new Extreme Hills biomes.
     * @return True if this
     */
    boolean isNewConfig();

    /**
     * Forces a setting to have another value when reading. Optional
     * operation, may have an empty implementation.
     * @param setting The setting to set.
     * @param value   The value of the setting.
     */
    <S> void putSetting(Setting<S> setting, S value);

    /**
     * Renames an old setting. If the old setting isn't found, this does
     * nothing.
     *
     * @param oldName Name of the old setting.
     * @param newSetting The new setting.
     */
    void renameOldSetting(String oldName, Setting<?> newSetting);

    /**
     * When {@link #getSetting(Setting, Object)} can't read a setting, it will
     * ask this reader to provide the setting value instead. This essentially
     * makes the given reader a fallback.
     *
     * @param reader The reader to fall back.
     */
    void setFallbackReader(SettingsReader reader);
}
