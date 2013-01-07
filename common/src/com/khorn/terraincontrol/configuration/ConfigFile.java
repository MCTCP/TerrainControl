package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public abstract class ConfigFile
{
    private BufferedWriter settingsWriter;

    /**
     * Stores all the settings. Settings like Name:Value or Name=Value are stored as name, Value and settings like Function(a, b, c) are stored as function(a, b, c), lineNumber
     */
    protected Map<String, String> SettingsCache = new HashMap<String, String>();

    private boolean WriteComments;

    protected void ReadSettingsFile(File f)
    {
        BufferedReader SettingsReader = null;

        if (f.exists())
        {
            try
            {
                SettingsReader = new BufferedReader(new FileReader(f));
                String thisLine;
                int lineNumber = 0;
                while ((thisLine = SettingsReader.readLine()) != null)
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
                        if(thisLine.contains("(") && (!thisLine.contains(":") || thisLine.indexOf('(') < thisLine.indexOf(":")) )
                        {
                            // ( is first, so it's a resource
                            this.SettingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                        } else
                        {
                            // : is first, so it's a setting
                            String[] splitSettings = thisLine.split(":", 2);
                            this.SettingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                        }
                    } else if (thisLine.contains("="))
                    {
                        // Setting (old style), split it and add it
                        String[] splitSettings = thisLine.split("=", 2);
                        this.SettingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                    } else
                    {
                        // Unknown, just add it
                        this.SettingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();

                if (SettingsReader != null)
                {
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        localIOException1.printStackTrace();
                    }
                }
            } finally
            {
                if (SettingsReader != null)
                {
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
                }
            }
        } else
            System.out.println("TerrainControl: Can not load " + f.getName());
    }

    // -------------------------------------------- //
    // SAY STUFF
    // -------------------------------------------- //

    protected boolean sayNotFoundEnabled()
    {
        return false;
    }

    protected void sayNotFound(String settingsName)
    {
        if (this.sayNotFoundEnabled())
        {
            System.out.println("TerrainControl: value " + settingsName + " not found.");
        }
    }

    protected void sayHadWrongValue(String settingsName)
    {
        System.out.println("TerrainControl: " + settingsName + " had wrong value");
    }

    // -------------------------------------------- //
    // ReadModSettings
    // -------------------------------------------- //

    protected List<WeightedMobSpawnGroup> ReadModSettings(String settingsName, List<WeightedMobSpawnGroup> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            String json = this.SettingsCache.get(settingsName);
            if (json == null)
                return defaultValue;
            return WeightedMobSpawnGroup.fromJson(json);
        }

        sayNotFound(settingsName);

        return defaultValue;
    }

    protected ArrayList<String> ReadModSettings(String settingsName, ArrayList<String> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            ArrayList<String> out = new ArrayList<String>();
            if (this.SettingsCache.get(settingsName).trim().equals("") || this.SettingsCache.get(settingsName).equals("None"))
            {
                return out;
            }
            Collections.addAll(out, this.SettingsCache.get(settingsName).split(","));
            return out;
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected int ReadModSettings(String settingsName, int defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Integer.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                sayHadWrongValue(settingsName);
            }
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected byte ReadModSettings(String settingsName, byte defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Byte.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                sayHadWrongValue(settingsName);
            }
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected String ReadModSettings(String settingsName, String defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            return this.SettingsCache.get(settingsName);
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected double ReadModSettings(String settingsName, double defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Double.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                sayHadWrongValue(settingsName);
            }
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected int ReadModSettingsColor(String settingsName, String defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        Color color = Color.decode(defaultValue);
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                color = Color.decode(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException ex)
            {
                sayHadWrongValue(settingsName);
            }
        } else
            sayNotFound(settingsName);
        return color.getRGB() & 0xFFFFFF;
    }

    protected float ReadModSettings(String settingsName, float defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Float.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                sayHadWrongValue(settingsName);
            }
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected boolean ReadModSettings(String settingsName, boolean defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            return Boolean.valueOf(this.SettingsCache.get(settingsName));
        }
        sayNotFound(settingsName);
        return defaultValue;
    }

    protected Enum<?> ReadModSettings(String settingsName, Enum<?> defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {

            Class<?> enumClass = defaultValue.getDeclaringClass();
            String value = this.SettingsCache.get(settingsName);

            if (enumClass.isEnum())
            {

                Object[] enumValues = enumClass.getEnumConstants();
                for (Object enumValue : enumValues)
                {
                    String enumName = ((Enum<?>) enumValue).name();
                    if (enumName.toLowerCase().equals(value) || enumName.equals(value))
                        return (Enum<?>) enumValue;
                }
                sayHadWrongValue(settingsName);

            }

        }
        sayNotFound(settingsName);
        return defaultValue;


    }

    @SuppressWarnings("unchecked")
    protected <T> T ReadSettings(TCSetting value)
    {
        Object obj = null;

        switch (value.getReturnType())
        {
            case String:
                obj = ReadModSettings(value.name(), value.stringValue());
                break;
            case Boolean:
                obj = ReadModSettings(value.name(), value.booleanValue());
                break;
            case Int:
                obj = ReadModSettings(value.name(), value.intValue());
                break;
            case Enum:
                obj = ReadModSettings(value.name(), value.enumValue());
                break;
            case Double:
                obj = ReadModSettings(value.name(), value.doubleValue());
                break;
            case Float:
                obj = ReadModSettings(value.name(), value.floatValue());
                break;
            case StringArray:
                obj = ReadModSettings(value.name(), value.stringArrayListValue());
                break;
            case Color:
                obj = ReadModSettingsColor(value.name(), value.stringValue());
                break;
        }


        return (T) obj;

    }


    public void WriteSettingsFile(File settingsFile, boolean comments)
    {
        this.WriteComments = comments;
        try
        {
            this.settingsWriter = new BufferedWriter(new FileWriter(settingsFile, false));

            this.WriteConfigSettings();
        } catch (IOException e)
        {
            e.printStackTrace();

            if (this.settingsWriter != null)
            {
                try
                {
                    this.settingsWriter.close();
                } catch (IOException localIOException1)
                {
                    localIOException1.printStackTrace();
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
                    localIOException2.printStackTrace();
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

    protected void writeValue(String settingsName, List<WeightedMobSpawnGroup> settingsValue) throws IOException
    {
        this.settingsWriter.write(settingsName + ": " + WeightedMobSpawnGroup.toJson(settingsValue));
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
        if (!this.WriteComments)
            return;
        if(comment.length() > 0)
            this.settingsWriter.write("# " + comment);
        this.settingsWriter.newLine();
    }

    protected void writeNewLine() throws IOException
    {
        this.settingsWriter.newLine();
    }

    protected abstract void WriteConfigSettings() throws IOException;

    protected abstract void ReadConfigSettings();

    protected abstract void CorrectSettings();

    protected abstract void RenameOldSettings();

    /**
     * Renames an old setting. If the old setting isn't found, this does
     * nothing.
     *
     * @param oldValue Name of the old setting.
     * @param newValue The new setting.
     */
    protected void renameOldSetting(String oldValue, TCDefaultValues newValue)
    {
        if (this.SettingsCache.containsKey(oldValue.toLowerCase()))
        {
            this.SettingsCache.put(newValue.name().toLowerCase(), this.SettingsCache.get(oldValue.toLowerCase()));
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

    protected static void WriteStringToStream(DataOutputStream stream, String value) throws IOException
    {
        byte[] bytes = value.getBytes();
        stream.writeShort(bytes.length);
        stream.write(bytes);
    }

    protected static String ReadStringFromStream(DataInputStream stream) throws IOException
    {
        byte[] chars = new byte[stream.readShort()];
        if (stream.read(chars, 0, chars.length) != chars.length)
            throw new EOFException();

        return new String(chars);
    }

    // Public access modifier, so that WeightedMobSpawnGroup can use it
    public static String[] ReadComplexString(String line)
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