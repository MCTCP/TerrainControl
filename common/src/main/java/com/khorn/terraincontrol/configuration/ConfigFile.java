package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.util.MultiTypedSetting;
import com.khorn.terraincontrol.util.MultiTypedSetting.SettingsType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

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
                TerrainControl.printStackTrace(Level.SEVERE, e);

                if (settingsReader != null)
                {
                    try
                    {
                        settingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        TerrainControl.printStackTrace(Level.SEVERE, localIOException1);
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
                        TerrainControl.printStackTrace(Level.SEVERE, localIOException2);
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
        TerrainControl.log(Level.FINEST, "Setting:`{0}` was not found \nin `{1}`.", new Object[]
        {
            settingsName, (this.file == null ? this.name + " Biome" : this.file.getName())
        });
    }

    protected void logSettingValueInvalid(String settingsName)
    {
        TerrainControl.log(Level.WARNING, getSettingValueInvalidError(settingsName));
    }

    protected void logSettingValueInvalid(String settingsName, Exception e)
    {
        TerrainControl.log(Level.WARNING, e.getClass().getSimpleName() + " :: " + getSettingValueInvalidError(settingsName));
    }

    private String getSettingValueInvalidError(String settingsName)
    {
        return "Value of " + settingsName + ": `" + this.settingsCache.get(settingsName) + "' in " + this.file.getName() + " is not valid.";
    }

    protected void logFileNotFound(File logFile)
    {
        String logName = logFile.getName();
        TerrainControl.log(Level.CONFIG, "File not found: {0} in {1}", new Object[]
        {
            logName, logFile.getAbsolutePath()
        });
    }

    // -------------------------------------------- //
    // ReadModSettings
    // -------------------------------------------- //
    protected List<WeightedMobSpawnGroup> readModSettings(MultiTypedSetting setting, List<WeightedMobSpawnGroup> defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            String json = this.settingsCache.get(settingsName);
            if (json == null)
                return defaultValue;
            return WeightedMobSpawnGroup.fromJson(json);
        }

        logSettingNotFound(settingsName);

        return defaultValue;
    }

    protected HashSet<Integer> readModSettings(MultiTypedSetting setting, HashSet<Integer> defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            HashSet<Integer> out = new HashSet<Integer>();
            if (this.settingsCache.get(settingsName).trim().isEmpty() || this.settingsCache.get(settingsName).equals("None"))
            {
                return out;
            }
            for (String string : this.settingsCache.get(settingsName).split(","))
            {
                out.add(Integer.parseInt(string));
            }
            return out;
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected ArrayList<String> readModSettings(MultiTypedSetting setting, ArrayList<String> defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            ArrayList<String> out = new ArrayList<String>();
            if (this.settingsCache.get(settingsName).trim().isEmpty() || this.settingsCache.get(settingsName).equals("None"))
            {
                return out;
            }
            Collections.addAll(out, this.settingsCache.get(settingsName).split(","));
            return out;
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected int readModSettings(MultiTypedSetting setting, int defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                return Integer.valueOf(this.settingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected long readModSettings(MultiTypedSetting setting, long defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            String value = settingsCache.get(settingsName);
            if (value.isEmpty())
            {
                return 0;
            }
            try
            {
                return Long.parseLong(value);
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected String readModSettings(MultiTypedSetting setting, String defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            return this.settingsCache.get(settingsName);
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected double readModSettings(MultiTypedSetting setting, double defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                return Double.valueOf(this.settingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected int readModSettingsColor(MultiTypedSetting setting, String defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        Color color = Color.decode(defaultValue);
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                color = Color.decode(this.settingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        } else
            logSettingNotFound(settingsName);
        return color.getRGB() & 0xFFFFFF;
    }

    protected float readModSettings(MultiTypedSetting setting, float defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                return Float.valueOf(this.settingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected boolean readModSettings(MultiTypedSetting setting, boolean defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            return Boolean.valueOf(this.settingsCache.get(settingsName));
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected Enum<?> readModSettings(MultiTypedSetting setting, Enum<?> defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {

            Class<?> enumClass = defaultValue.getDeclaringClass();
            String value = this.settingsCache.get(settingsName);

            if (enumClass.isEnum())
            {

                Object[] enumValues = enumClass.getEnumConstants();
                for (Object enumValue : enumValues)
                {
                    String enumName = ((Enum<?>) enumValue).name();
                    if (enumName.toLowerCase().equals(value) || enumName.equals(value))
                        return (Enum<?>) enumValue;
                }
                logSettingValueInvalid(settingsName);

            }

        }
        logSettingNotFound(settingsName);
        return defaultValue;

    }
    
    protected LocalMaterialData readModSettings(MultiTypedSetting setting, DefaultMaterial defaultValue)
    {
        String settingsName = setting.name().toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                return TerrainControl.readMaterial(this.settingsCache.get(settingsName));
            } catch (InvalidConfigException e)
            {
                logSettingValueInvalid(settingsName);
            }
        }
        logSettingNotFound(settingsName);
        return TerrainControl.toLocalMaterialData(defaultValue, 0);
    }

    @SuppressWarnings("unchecked")
    protected <T> T readSettings(MultiTypedSetting value)
    {
        Object obj = null;

        switch (value.getReturnType())
        {
            case String:
                obj = readModSettings(value, value.stringValue());
                break;
            case Boolean:
                obj = readModSettings(value, value.booleanValue());
                break;
            case Int:
                obj = readModSettings(value, value.intValue());
                break;
            case IntSet:
                obj = readModSettings(value, value.intSetValue());
                break;
            case Long:
                obj = readModSettings(value, value.longValue());
                break;
            case Enum:
                obj = readModSettings(value, value.enumValue());
                break;
            case Double:
                obj = readModSettings(value, value.doubleValue());
                break;
            case Float:
                obj = readModSettings(value, value.floatValue());
                break;
            case StringArray:
                obj = readModSettings(value, value.stringArrayListValue());
                break;
            case Color:
                obj = readModSettingsColor(value, value.stringValue());
                break;
            case Material:
                obj = readModSettings(value, value.materialValue());
                break;
            /*
             * This prevents NPE if you happen to add a new type to
             * TCSettings and cascade the change but forget to add it here
             */
            default:
                throw new EnumConstantNotPresentException(SettingsType.class, value.getReturnType().name());
        }

        return (T) obj;

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
            TerrainControl.log(Level.SEVERE, "{0}:: {1}", new Object[]
            {
                "localIOExceptionE: ", localIOExceptionE.getStackTrace().toString()
            });

            if (this.settingsWriter != null)
            {
                try
                {
                    this.settingsWriter.close();
                } catch (IOException localIOException1)
                {
                    TerrainControl.log(Level.SEVERE, "{0}:: {1}", new Object[]
                    {
                        "localIOException1: ", localIOException1.getStackTrace().toString()
                    });
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
                    TerrainControl.log(Level.SEVERE, "{0}:: {1}", new Object[]
                    {
                        "localIOException2: ", localIOException2.getStackTrace().toString()
                    });
                }
            }
        }
    }

    protected void writeValue(MultiTypedSetting setting, ArrayList<String> settingsValue) throws IOException
    {
        String out = "";
        for (String key : settingsValue)
        {
            if (out.isEmpty())
                out += key;
            else
                out += "," + key;
        }

        this.settingsWriter.write(setting.name() + ": " + out);
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, HashSet<Integer> settingsValue) throws IOException
    {
        String out = "";
        for (Integer key : settingsValue)
        {
            if (out.isEmpty())
                out += key;
            else
                out += "," + key;
        }

        this.settingsWriter.write(setting.name() + ": " + out);
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, List<WeightedMobSpawnGroup> settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + WeightedMobSpawnGroup.toJson(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, int settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + Integer.toString(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, double settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + Double.toString(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, float settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + Float.toString(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, boolean settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + Boolean.toString(settingsValue));
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, String settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + settingsValue);
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeValue(MultiTypedSetting setting, LocalMaterialData settingsValue) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": " + settingsValue);
        this.settingsWriter.newLine();
        this.settingsWriter.newLine();
    }

    protected void writeFunction(ConfigFunction<?> function) throws IOException
    {
        this.settingsWriter.write(function.write());
        this.settingsWriter.newLine();
    }

    protected void writeColorValue(MultiTypedSetting setting, int RGB) throws IOException
    {
        this.settingsWriter.write(setting.name() + ": 0x" + Integer.toHexString((0xFFFFFF & RGB) | 0x1000000).substring(1));
        this.settingsWriter.newLine();
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
    protected void renameOldSetting(String oldValue, MultiTypedSetting newValue)
    {
        if (this.settingsCache.containsKey(oldValue.toLowerCase()))
        {
            this.settingsCache.put(newValue.name().toLowerCase(), this.settingsCache.get(oldValue.toLowerCase()));
        }
    }

    protected int applyBounds(int value, int min, int max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected double applyBounds(double value, double min, double max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected float applyBounds(float value, float min, float max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected float applyBounds(float value, float min, float max, float minValue)
    {
        value = applyBounds(value, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

    protected int applyBounds(int value, int min, int max, int minValue)
    {
        value = applyBounds(value, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

    protected ArrayList<String> filterBiomes(ArrayList<String> biomes, Set<String> customBiomes)
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

    protected static String readStringFromStream(DataInputStream stream) throws IOException
    {
        byte[] chars = new byte[stream.readShort()];
        if (stream.read(chars, 0, chars.length) != chars.length)
            throw new EOFException();

        return new String(chars);
    }

}
