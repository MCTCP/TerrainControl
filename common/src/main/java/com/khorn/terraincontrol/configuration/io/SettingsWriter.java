package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.settingType.Setting;

import java.io.File;
import java.io.IOException;

/**
 * Used to write settings, usually to a file.
 *
 * <p>A configuration file consists of settings, comments and functions. Those
 * elements are grouped by small and big titles. Comments are always placed
 * just before the setting/function they explain.
 *
 */
public interface SettingsWriter
{
    /**
     * Writes a big title to the configuration file.
     * @param title The title to write.
     * @throws IOException If an IO error occurs.
     * @throws IllegalStateException If this method is called before
     * {@link #open()}.
     */
    void bigTitle(String title) throws IOException, IllegalStateException;

    /**
     * Closes the settings writer.
     * @throws IOException If closing fails.
     */
    void close() throws IOException;

    /**
     * Writes a comment to the configuration file.
     * @param comment The comment to write. May be empty.
     * @throws IOException If an IO error occurs.
     * @throws IllegalStateException If this method is called before
     * {@link #open()}.
     */
    void comment(String comment) throws IOException, IllegalStateException;

    /**
     * Writes the given ConfigFunction to the configuration file.
     * @param function The function to write.
     * @throws IOException If an IO error occurs.
     * @throws IllegalStateException If this method is called before
     * {@link #open()}.
     */
    void function(ConfigFunction<?> function) throws IOException, IllegalStateException;

    /**
     * Gets the file the settings will be written to. Returns null if the
     * settings will not be written to a file.
     * @return The file.
     */
    File getFile();

    /**
     * Opens the settings writer for writing.
     * @throws IOException If the file cannot be opened for writing.
     */
    void open() throws IOException;

    /**
     * Sets the configuration mode. Must be called before {@link #open()} is
     * called.
     * @param configMode The config mode.
     * @throws IllegalStateException If this method is called before
     * {@link #open()}.
     */
    void setConfigMode(ConfigMode configMode) throws IllegalStateException;

    /**
     * Writes given setting to the config file.
     * @param setting       The setting to write.
     * @param settingsValue The value of the setting to write.
     * @throws IOException When an IO error occurs.
     */
    <S> void setting(Setting<S> setting, S value) throws IOException, IllegalStateException;

    /**
     * Writes a small title to the configuration file.
     * @param title The title to write.
     * @throws IOException If an IO error occurs.
     * @throws IllegalStateException If this method is called before
     * {@link #open()}.
     */
    void smallTitle(String title) throws IOException, IllegalStateException;

}
