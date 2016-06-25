package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.io.RawSettingValue.ValueType;
import com.khorn.terraincontrol.configuration.settingType.Setting;

import java.util.Collection;
import java.util.List;

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
public interface SettingsMap
{

    /**
     * Adds a {@link ConfigFunction} to this settings map, so that it can be read back
     * using {@link #getConfigFunctions(Object, boolean)}.
     *
     * @param functions The function to add.
     */
    void addConfigFunctions(Collection<? extends ConfigFunction<?>> functions);

    /**
     * Gets all ConfigFunctions in this configuration. If this reader doesn't
     * support ConfigFunctions, an empty list will be returned.
     *
     * @param <T> The type of the config functions.
     * @param holder      The holder of all config functions.
     * @param useFallback True if the {@link #setFallback(SettingsMap)
     * fallback reader} must be used, false otherwise.
     * @return The config functions.
     */
    <T> List<ConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback);

    /**
     * Gets the name of this config file. For worlds, this is the world name,
     * for biomes this is the biome name, etc.
     *
     * @return The name of this config file.
     */
    String getName();

    /**
     * Gets all settings in this config file in an unparsed state.
     *
     * @return The raw settings.
     */
    Collection<RawSettingValue> getRawSettings();

    /**
     * Reads a setting. If the setting does not exist, the default value for the
     * setting is returned.
     * @param <S>     Type of the value of the setting.
     * @param setting The setting to read.
     * @return The setting value.
     */
    <S> S getSetting(Setting<S> setting);

    /**
     * Reads a setting. This method allows you to provide another default
     * value. If the setting has an invalid value, a message is logged and
     * the default value is returned.
     * @param <S>          Type of the value of the setting.
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
     * Adds a setting to this map, overwriting existing settings with the same name.
     * @param <S>      Type of the value of the setting.
     * @param setting  The setting to set.
     * @param value    The value of the setting.
     * @param comments Comments for the setting. Each comment is written on its
     *                 own line.
     */
    <S> void putSetting(Setting<S> setting, S value, String... comments);

    /**
     * Adds a raw setting to this map. If this setting has a type of
     * {@link ValueType#PLAIN_SETTING}, it will overwrite existing settings with
     * the same name. 
     * @param value Raw value for the setting.
     */
    void addRawSetting(RawSettingValue value);

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
    void setFallback(SettingsMap reader);

    /**
     * Adds a big title to the config file. New setting added after this title
     * will be placed below this title.
     * @param title    The title text.
     * @param comments Comments directly after the title.
     */
    void smallTitle(String title, String... comments);

    /**
     * Adds a small title to the config file. New setting added after this title
     * will be placed below this title.
     * @param title    The title text.
     * @param comments Comments directly after the title.
     */
    void bigTitle(String title, String... comments);

}
