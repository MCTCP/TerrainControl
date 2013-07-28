package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.TCSetting.SettingsType;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

public abstract class ConfigFile
{
    private BufferedWriter settingsWriter;

    public final String name;
    public final File file;

    /**
     * Creates a new configuration file.
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

        if (name == null)
        {
            throw new IllegalArgumentException("Name may not be null");
        }
    }

    /**
     * Stores all the settings. Settings like Name:Value or Name=Value are stored as name, Value and settings like Function(a, b, c) are stored as function(a, b, c), lineNumber
     */
    protected Map<String, String> settingsCache = new HashMap<String, String>();

    private boolean writeComments;

    protected void readSettingsFile() throws RuntimeException
    {
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

                    if (thisLine.trim().equals(""))
                    {
                        // Empty line, ignore
                    } else if (thisLine.startsWith("#") || thisLine.startsWith("<"))
                    {
                        // Comment, ignore
                    } else if (thisLine.contains(":") || thisLine.toLowerCase().contains("("))
                    {
                        // Setting or resource
                        if (thisLine.contains("(") && (!thisLine.contains(":") || thisLine.indexOf('(') < thisLine.indexOf(":")))
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
            } catch (IOException e)
            {
                TerrainControl.log(Level.SEVERE, e.getStackTrace().toString());

                if (settingsReader != null)
                {
                    try
                    {
                        settingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        TerrainControl.log(Level.SEVERE, localIOException1.getStackTrace().toString());
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
                        TerrainControl.log(Level.SEVERE, localIOException2.getStackTrace().toString());
                    }
                }
            }
        } else
            logFileNotFound(file);
    }

    // -------------------------------------------- //
    // LOG STUFF
    // -------------------------------------------- //

    protected void logSettingNotFound(String settingsName)
    {
        // Disabled, because it will spam a lot otherwise
        // TerrainControl.log("`"+settingsName + "` in `" + this.file.getName()
        // + "` was not found.");
    }

    protected void logSettingValueInvalid(String settingsName)
    {
        String error = "The value of " + settingsName + " (" + settingsCache.get(settingsName) + ") in file " + file.getName() + " is invalid.";
        TerrainControl.log(Level.WARNING, error);
    }

    protected void logSettingValueInvalid(String settingsName, Exception e)
    {
        String error = "The value of " + settingsName + " (" + settingsCache.get(settingsName) + ") in file " + file.getName() + " is invalid: " + e.getClass().getSimpleName();
        TerrainControl.log(Level.WARNING, error);
    }

    protected void logFileNotFound(File logFile)
    {
        TerrainControl.log("File not found: " + logFile.getName());
    }

    // -------------------------------------------- //
    // ReadModSettings
    // -------------------------------------------- //

    protected List<WeightedMobSpawnGroup> readModSettings(String settingsName, List<WeightedMobSpawnGroup> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected HashSet<Integer> readModSettings(String settingsName, HashSet<Integer> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            HashSet<Integer> out = new HashSet<Integer>();
            if (this.settingsCache.get(settingsName).trim().equals("") || this.settingsCache.get(settingsName).equals("None"))
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

    protected ArrayList<String> readModSettings(String settingsName, ArrayList<String> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            ArrayList<String> out = new ArrayList<String>();
            if (this.settingsCache.get(settingsName).trim().equals("") || this.settingsCache.get(settingsName).equals("None"))
            {
                return out;
            }
            Collections.addAll(out, this.settingsCache.get(settingsName).split(","));
            return out;
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected int readModSettings(String settingsName, int defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected long readModSettings(String settingsName, long defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected byte readModSettings(String settingsName, byte defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            try
            {
                short number = Short.valueOf(this.settingsCache.get(settingsName));
                if (number < 0 || number > 255)
                {
                    throw new NumberFormatException();
                }
                return (byte) number;
            } catch (NumberFormatException e)
            {
                logSettingValueInvalid(settingsName, e);
            }
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected String readModSettings(String settingsName, String defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            return this.settingsCache.get(settingsName);
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected double readModSettings(String settingsName, double defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected int readModSettingsColor(String settingsName, String defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected float readModSettings(String settingsName, float defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    protected boolean readModSettings(String settingsName, boolean defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.settingsCache.containsKey(settingsName))
        {
            return Boolean.valueOf(this.settingsCache.get(settingsName));
        }
        logSettingNotFound(settingsName);
        return defaultValue;
    }

    protected Enum<?> readModSettings(String settingsName, Enum<?> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
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

    @SuppressWarnings("unchecked")
    protected <T> T readSettings(TCSetting value)
    {
        Object obj = null;

        switch (value.getReturnType())
        {
            case String:
                obj = readModSettings(value.name(), value.stringValue());
                break;
            case Boolean:
                obj = readModSettings(value.name(), value.booleanValue());
                break;
            case Int:
                obj = readModSettings(value.name(), value.intValue());
                break;
            case IntSet:
                obj = readModSettings(value.name(), value.intSetValue());
                break;
            case Long:
                obj = readModSettings(value.name(), value.longValue());
                break;
            case Enum:
                obj = readModSettings(value.name(), value.enumValue());
                break;
            case Double:
                obj = readModSettings(value.name(), value.doubleValue());
                break;
            case Float:
                obj = readModSettings(value.name(), value.floatValue());
                break;
            case StringArray:
                obj = readModSettings(value.name(), value.stringArrayListValue());
                break;
            case Color:
                obj = readModSettingsColor(value.name(), value.stringValue());
                break;
            /*
             * This prevents NPE if you happen to add a new type to TCSettings
             * and cascade the change but forget to add it here
             */
            default:
                throw new EnumConstantNotPresentException(SettingsType.class, value.getReturnType().name());
        }

        return (T) obj;

    }

    /**
     * Old write method. Because the file is now stored in the configFile,
     * this parameter is no longer needed.
     * @param settingsFile Previously, the file to write to. 
     *                     Doesn't do anything anymore.
     * @param comments     Whether comments should be written.
     * @deprecated         28 July 2013, use method without file parameter.
     */
    @Deprecated
    public void writeSettingsFile(File settingsFile, boolean comments)
    {
        writeSettingsFile(comments);
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
        } catch (IOException e)
        {
            TerrainControl.log(Level.SEVERE, e.getStackTrace().toString());

            if (this.settingsWriter != null)
            {
                try
                {
                    this.settingsWriter.close();
                } catch (IOException localIOException1)
                {
                    TerrainControl.log(Level.SEVERE, localIOException1.getStackTrace().toString());
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
                    TerrainControl.log(Level.SEVERE, localIOException2.getStackTrace().toString());
                }
            }
        }
    }

    protected void writeValue(String settingsName, ArrayList<String> settingsValue) throws IOException
    {
        String out = "";
        for (String key : settingsValue)
        {
            if (out.equals(""))
                out += key;
            else
                out += "," + key;
        }

        this.settingsWriter.write(settingsName + ":" + out);
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, HashSet<Integer> settingsValue) throws IOException
    {
        String out = "";
        for (Integer key : settingsValue)
        {
            if (out.equals(""))
                out += key;
            else
                out += "," + key;
        }

        this.settingsWriter.write(settingsName + ":" + out);
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, List<WeightedMobSpawnGroup> settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ": " + WeightedMobSpawnGroup.toJson(settingsValue));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, byte settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + (settingsValue & 0xFF));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, int settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, double settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, float settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + Float.toString(settingsValue));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, boolean settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName, String settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ":" + settingsValue);
        this.settingsWriter.newLine();
    }

    protected void writeValue(String settingsName) throws IOException
    {
        this.settingsWriter.write(settingsName);
        this.settingsWriter.newLine();
    }

    protected void writeColorValue(String settingsName, int RGB) throws IOException
    {
        this.settingsWriter.write(settingsName + ":0x" + Integer.toHexString((0xFFFFFF & RGB) | 0x1000000).substring(1));
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

    protected void writeNewLine() throws IOException
    {
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
    protected void renameOldSetting(String oldValue, TCDefaultValues newValue)
    {
        if (this.settingsCache.containsKey(oldValue.toLowerCase()))
        {
            this.settingsCache.put(newValue.name().toLowerCase(), this.settingsCache.get(oldValue.toLowerCase()));
        }
    }

    protected HashSet<Integer> applyBounds(HashSet<Integer> values, int min, int max)
    {
        HashSet<Integer> output = new HashSet<Integer>();
        for (int value : values)
        {
            if (value > max)
                output.add(max);
            else if (value < min)
                output.add(min);
            else
                output.add(value);
        }
        return output;
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

    protected ArrayList<String> filterBiomes(ArrayList<String> biomes, ArrayList<String> customBiomes)
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

    // Public access modifier, so that WeightedMobSpawnGroup can use it
    public static String[] readComplexString(String line)
    {
        ArrayList<String> buffer = new ArrayList<String>();

        int index = 0;
        int lastFound = 0;
        int inBracer = 0;

        for (char c : line.toCharArray())
        {
            if (c == ',' && inBracer == 0)
            {
                buffer.add(line.substring(lastFound, index));
                lastFound = index + 1;
            }

            if (c == '(')
                inBracer++;
            if (c == ')')
                inBracer--;

            index++;
        }
        buffer.add(line.substring(lastFound, index));

        String[] output = new String[0];

        if (inBracer == 0)
            output = buffer.toArray(output);

        return output;

    }
}
