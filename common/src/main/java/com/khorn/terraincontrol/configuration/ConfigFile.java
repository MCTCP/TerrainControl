package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.*;
import java.util.*;

public abstract class ConfigFile
{

    private BufferedWriter settingsWriter;
    public final String name;

    public File file;
    public boolean readSuccess;

    /**
     * True if the file does not exist yet on disk, false otherwise. Used to
     * provide backwards compatible default settings.
     */
    protected final boolean isNewConfig;

    /**
     * Creates a new configuration file.
     * <p/>
     * @param name Name of the thing that is being read,
     *             like Plains or MyBO3. May not be null.
     * @param file The file of the configuration. If this config
     *             needs to read or written, this shouldn't be null.
     *             Otherwise, if it's manually read from the network, this
     *             can be null. The {@link #readSettingsFile()}
     *             and {@link #writeSettingsFile(boolean)} methods must not be
     *             used, otherwise they will throw a RuntimeException.
     */
    protected ConfigFile(String name, File file) throws IllegalArgumentException
    {
        this.name = name;
        this.file = file;
        this.isNewConfig = (file == null || !file.exists());

        if (name == null)
        {
            throw new IllegalArgumentException("Name may not be null");
        }
    }

    /**
     * Stores all the settings. Settings like Name:Value or Name=Value are
     * stored as name, Value and settings like Function(a, b, c) are stored
     * as function(a, b, c), lineNumber. Because this is a linked hashmap,
     * you're guaranteed that the lines will be read in order when iterating
     * over this map.
     */
    protected Map<String, String> settingsCache = new LinkedHashMap<String, String>();
    private boolean writeComments;

    protected void readSettingsFile() throws RuntimeException
    {
        readSettingsFile(true);
    }

    protected void readSettingsFile(boolean verbose) throws RuntimeException
    {
        this.readSuccess = false;
        if (file == null)
            throw new RuntimeException("Constructor called with null file.");

        BufferedReader settingsReader = null;

        if (file.exists())
        {
            try
            {
                settingsReader = new BufferedReader(new FileReader(file));
                String thisLine;
                int lineNumber = 0;
                while ((thisLine = settingsReader.readLine()) != null)
                {
                    lineNumber++;

                    if (thisLine.trim().isEmpty())
                    {
                        // Empty line, ignore
                    } else if (thisLine.startsWith("#") || thisLine.startsWith("<"))
                    {
                        // Comment, ignore
                    } else if (thisLine.contains(":") || thisLine.toLowerCase().contains("("))
                    {
                        // Setting or resource
                        if (thisLine.contains("(") && (!thisLine.contains(":") || thisLine.indexOf('(') < thisLine.indexOf(':')))
                        {
                            // ( is first, so it's a resource
                            this.settingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                        } else
                        {
                            // : is first, so it's a setting
                            String[] splitSettings = thisLine.split(":", 2);
                            this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                        }
                    } else if (thisLine.contains("="))
                    {
                        // Setting (old style), split it and add it
                        String[] splitSettings = thisLine.split("=", 2);
                        this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                    } else
                    {
                        // Unknown, just add it
                        this.settingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                    }
                }
                this.readSuccess = true;
            } catch (IOException e)
            {
                TerrainControl.printStackTrace(LogMarker.FATAL, e);

                if (settingsReader != null)
                {
                    try
                    {
                        settingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        TerrainControl.printStackTrace(LogMarker.FATAL, localIOException1);
                    }
                }
            } finally
            {
                if (settingsReader != null)
                {
                    try
                    {
                        settingsReader.close();
                    } catch (IOException localIOException2)
                    {
                        TerrainControl.printStackTrace(LogMarker.FATAL, localIOException2);
                    }
                }
            }
        } else if (verbose)
        {
            logFileNotFound(file);
        }
    }

    // -------------------------------------------- //
    // LOG STUFF
    // -------------------------------------------- //
    protected void logSettingNotFound(String settingsName)
    {
        TerrainControl.log(LogMarker.TRACE, "Setting:`{}` was not found in `{}`.",settingsName,
                (this.file == null ? this.name + " Biome" : this.file.getName()));
    }

    protected void logSettingValueInvalid(String settingsName)
    {
        TerrainControl.log(LogMarker.WARN, getSettingValueInvalidError(settingsName));
    }

    protected void logSettingValueInvalid(String settingsName, Exception e)
    {
        TerrainControl.log(LogMarker.WARN, "{} :: {}", (Object) e.getClass().getSimpleName(), getSettingValueInvalidError(settingsName));
    }

    private String getSettingValueInvalidError(String settingsName)
    {
        return "Value of " + settingsName + ": `" + this.settingsCache.get(settingsName) + "' in " + this.file.getName() + " is not valid.";
    }

    protected void logFileNotFound(File logFile)
    {
        String logName = logFile.getName();
        TerrainControl.log(LogMarker.DEBUG, "File not found: {} in {}", new Object[] {logName, logFile.getAbsolutePath()});
    }


    /**
     * Reads a setting. If the setting has an invalid value,
     * a message is logged and the default value is returned.
     * @param setting The setting to read.
     * @return The value of the setting.
     */
    protected <T> T readSettings(Setting<T> setting)
    {
        return readSettings(setting, setting.getDefaultValue());
    }

    /**
     * Reads a setting. This method allows you to provide another default
     * value. If the setting has an invalid value, a message is logged and
     * the default value is returned.
     * @param setting      The setting to read.
     * @param defaultValue Default value for the setting.
     * @return The value of the setting.
     */
    protected <T> T readSettings(Setting<T> setting, T defaultValue)
    {
        String settingsName = setting.getName().toLowerCase();
        String value = settingsCache.get(settingsName);
        if (value == null)
        {
            logSettingNotFound(setting.getName());
            return defaultValue;
        }
        try
        {
            return setting.read(value);
        } catch (InvalidConfigException e)
        {
            logSettingValueInvalid(setting.getName(), e);
            return defaultValue;
        }
    }

    public void writeSettingsFile(boolean comments)
    {
        if (file == null)
            throw new RuntimeException("Constructor called with null file.");

        this.writeComments = comments;
        try
        {
            this.settingsWriter = new BufferedWriter(new FileWriter(file, false));

            this.writeConfigSettings();
        } catch (IOException localIOExceptionE)
        {
            TerrainControl.log(LogMarker.FATAL, "{}:: {}", new Object[] {"localIOExceptionE: ",
                    localIOExceptionE.getStackTrace().toString()});

            if (this.settingsWriter != null)
            {
                try
                {
                    this.settingsWriter.close();
                } catch (IOException localIOException1)
                {
                    TerrainControl.log(LogMarker.FATAL, "{}:: {}", new Object[] {"localIOException1: ",
                            localIOException1.getStackTrace().toString()});
                }
            }
        } finally
        {
            if (this.settingsWriter != null)
            {
                try
                {
                    this.settingsWriter.close();
                } catch (IOException localIOException2)
                {
                    TerrainControl.log(LogMarker.FATAL, "{}:: {}", new Object[] {"localIOException2: ",
                            localIOException2.getStackTrace().toString()});
                }
            }
        }
    }

    /**
     * Writes given setting to the config file.
     * @param setting       The setting to write.
     * @param settingsValue The value of the setting to write.
     * @throws IOException When an IO error occurs.
     */
    protected <T> void writeValue(Setting<T> setting, T settingsValue)
            throws IOException
    {
        this.settingsWriter.write(setting.getName() + ": " + setting.write(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    /**
     * Writes the given config function to the config file.
     * @param function The function to write.
     * @throws IOException When an IO error occurs.
     */
    protected void writeFunction(ConfigFunction<?> function) throws IOException
    {
        this.settingsWriter.write(function.write());
        this.settingsWriter.newLine();
    }

    protected void writeBigTitle(String title) throws IOException
    {
        this.settingsWriter.newLine();
        this.settingsWriter.write("#######################################################################");
        this.settingsWriter.newLine();
        this.settingsWriter.write("# +-----------------------------------------------------------------+ #");
        this.settingsWriter.newLine();
        StringBuilder builder = new StringBuilder(title);
        builder.insert(0, ' ');
        builder.append(' ');
        boolean flag = true;
        while (builder.length() < 65)
        {
            if (flag)
                builder.insert(0, ' ');
            else
                builder.append(' ');
            flag = !flag;
        }
        this.settingsWriter.write("# |" + builder.toString() + "| #");
        this.settingsWriter.newLine();
        this.settingsWriter.write("# +-----------------------------------------------------------------+ #");
        this.settingsWriter.newLine();
        this.settingsWriter.write("#######################################################################");
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeSmallTitle(String title) throws IOException
    {
        int titleLength = title.length();
        StringBuilder rowBuilder = new StringBuilder(titleLength + 4);
        for (int i = 0; i < titleLength + 4; i++)
        {
            rowBuilder.append('#');
        }
        this.settingsWriter.write(rowBuilder.toString());
        this.settingsWriter.newLine();
        this.settingsWriter.write("# " + title + " #");
        this.settingsWriter.newLine();
        this.settingsWriter.write(rowBuilder.toString());
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeComment(String comment) throws IOException
    {
        if (!this.writeComments)
            return;
        if (comment.length() > 0)
            this.settingsWriter.write("# " + comment);
        this.settingsWriter.newLine();
    }

    protected abstract void writeConfigSettings() throws IOException;

    protected abstract void readConfigSettings();

    protected abstract void correctSettings();

    protected abstract void renameOldSettings();

    /**
     * Renames an old setting. If the old setting isn't found, this does
     * nothing.
     *
     * @param oldValue Name of the old setting.
     * @param newValue The new setting.
     */
    protected void renameOldSetting(String oldValue, com.khorn.terraincontrol.configuration.settingType.Setting<?> newValue)
    {
        if (this.settingsCache.containsKey(oldValue.toLowerCase()))
        {
            this.settingsCache.put(newValue.getName().toLowerCase(), this.settingsCache.get(oldValue.toLowerCase()));
        }
    }

    /**
     * Silently corrects the given number so that it is higher than the
     * minimum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param minimumValue The minimum value.
     * @return The corrected value.
     */
    protected int higherThan(int currentValue, int minimumValue)
    {
        if (currentValue <= minimumValue) {
            return minimumValue + 1;
        }
        return currentValue;
    }

    /**
     * Silently corrects the given number so that it is higher than or equal
     * to the minimum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param minimumValue The minimum value.
     * @return The corrected value.
     */
    protected double higherThan(double currentValue, double minimumValue)
    {
        if (currentValue < minimumValue) {
            return minimumValue;
        }
        return currentValue;
    }

    /**
     * Silently corrects the given number so that it is lower than or equal
     * to the maximum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param maximumValue The maximum value.
     * @return The corrected value.
     */
    protected int lowerThanOrEqualTo(int currentValue, int maximumValue)
    {
        if (currentValue > maximumValue) {
            return maximumValue;
        }
        return currentValue;
    }

    protected ArrayList<String> filterBiomes(List<String> biomes, Set<String> customBiomes)
    {
        ArrayList<String> output = new ArrayList<String>();

        for (String key : biomes)
        {
            key = key.trim();
            if (customBiomes.contains(key))
            {
                output.add(key);
                continue;
            }

            if (DefaultBiome.Contain(key))
                output.add(key);

        }
        return output;
    }

    protected static void writeStringToStream(DataOutputStream stream, String value) throws IOException
    {
        byte[] bytes = value.getBytes();
        stream.writeShort(bytes.length);
        stream.write(bytes);
    }

    public static String readStringFromStream(DataInputStream stream) throws IOException
    {
        byte[] chars = new byte[stream.readShort()];
        if (stream.read(chars, 0, chars.length) != chars.length)
            throw new EOFException();

        return new String(chars);
    }

}
