package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.lib.gson.Gson;
import com.khorn.terraincontrol.lib.gson.GsonBuilder;
import com.khorn.terraincontrol.lib.gson.reflect.TypeToken;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public abstract class ConfigFile
{
    private BufferedWriter SettingsWriter;
    
    // TODO: This map is populated with lowercase versions as well.
    // TODO: That is a derped approach. Use TreeSet with CASE_INSENSITIVE_ORDER instead.
    protected HashMap<String, String> SettingsCache = new HashMap<String, String>();
    
    // TODO: We should use GSON only instead of just for a few fields.
    public static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

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
                    if (thisLine.trim().equals("")) continue;
                    if (thisLine.startsWith("#") || thisLine.startsWith("<")) continue;
                    if (thisLine.toLowerCase().contains(":"))
                    {
                        String[] splitSettings = thisLine.split(":", 2);
                        if (splitSettings.length == 2)
                        {
                            this.SettingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                            this.SettingsCache.put(splitSettings[0].trim(), splitSettings[1].trim());
                        }
                        else if (splitSettings.length == 1)
                        {
                            this.SettingsCache.put(splitSettings[0].trim().toLowerCase(), "");
                            this.SettingsCache.put(splitSettings[0].trim(), "");
                        }
                    }
                    else
                    {
                        this.SettingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                    }
                    lineNumber++;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();

                if (SettingsReader != null)
                {
                    try
                    {
                        SettingsReader.close();
                    }
                    catch (IOException localIOException1)
                    {
                        localIOException1.printStackTrace();
                    }
                }
            }
            finally
            {
                if (SettingsReader != null)
                {
                    try
                    {
                        SettingsReader.close();
                    }
                    catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
                }
            }
        }
    }

    protected List<WeightedMobSpawnGroup> ReadModSettings(String settingsName, List<WeightedMobSpawnGroup> defaultValue)
    {
        String json = this.SettingsCache.get(settingsName);
        if (json == null) return defaultValue;
        return gson.fromJson(json, new TypeToken<List<WeightedMobSpawnGroup>>(){}.getType());
    }
    
    protected ArrayList<String> ReadModSettings(String settingsName, ArrayList<String> defaultValue)
    {
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
            }
            catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
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
            }
            catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected String ReadModSettings(String settingsName, String defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            return this.SettingsCache.get(settingsName);
        }
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
            }
            catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
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
            }
            catch (NumberFormatException ex)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
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
            }
            catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected boolean ReadModSettings(String settingsName, boolean defaultValue)
    {
        settingsName = settingsName.toLowerCase();
        if (this.SettingsCache.containsKey(settingsName))
        {
            return Boolean.valueOf(this.SettingsCache.get(settingsName));
        }
        return defaultValue;
    }

    protected void WriteSettingsFile(File settingsFile)
    {
        try
        {
            this.SettingsWriter = new BufferedWriter(new FileWriter(settingsFile, false));

            this.WriteConfigSettings();
        }
        catch (IOException e)
        {
            e.printStackTrace();

            if (this.SettingsWriter != null)
            {
                try
                {
                    this.SettingsWriter.close();
                }
                catch (IOException localIOException1)
                {
                    localIOException1.printStackTrace();
                }
            }
        }
        finally
        {
            if (this.SettingsWriter != null)
            {
                try
                {
                    this.SettingsWriter.close();
                }
                catch (IOException localIOException2)
                {
                    localIOException2.printStackTrace();
                }
            }
        }
    }

    protected void WriteValue(String settingsName, ArrayList<String> settingsValue) throws IOException
    {
        String out = "";
        for (String key : settingsValue)
        {
            if (out.equals(""))
                out += key;
            else
                out += "," + key;
        }

        this.SettingsWriter.write(settingsName + ":" + out);
        this.SettingsWriter.newLine();
    }
    
    protected void WriteValue(String settingsName, List<WeightedMobSpawnGroup> settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + gson.toJson(settingsValue));
        this.SettingsWriter.newLine();
    }

    
    protected void WriteValue(String settingsName, int settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteValue(String settingsName, double settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteValue(String settingsName, float settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Float.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteValue(String settingsName, boolean settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteValue(String settingsName, String settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + settingsValue);
        this.SettingsWriter.newLine();
    }

    protected void WriteValue(String settingsName) throws IOException
    {
        this.SettingsWriter.write(settingsName);
        this.SettingsWriter.newLine();
    }

    protected void WriteColorValue(String settingsName, int RGB) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":0x" + Integer.toHexString((0xFFFFFF & RGB) | 0x1000000).substring(1));
        this.SettingsWriter.newLine();
    }

    protected void WriteTitle(String title) throws IOException
    {
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("#######################################################################");
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("# +-----------------------------------------------------------------+ #");
        this.SettingsWriter.newLine();
        title = "  " + title + "  ";
        boolean flag = true;
        while (title.length() < 65)
        {
            if (flag)
                title = " " + title;
            else
                title = title + " ";
            flag = !flag;
        }
        this.SettingsWriter.write("# |" + title + "| #");
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("# +-----------------------------------------------------------------+ #");
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("#######################################################################");
        this.SettingsWriter.newLine();
        this.SettingsWriter.newLine();
    }

    protected void WriteComment(String comment) throws IOException
    {
        this.SettingsWriter.write("# " + comment);
        this.SettingsWriter.newLine();
    }

    protected void WriteNewLine() throws IOException
    {
        this.SettingsWriter.newLine();
    }

    protected abstract void WriteConfigSettings() throws IOException;

    protected abstract void ReadConfigSettings();

    protected abstract void CorrectSettings();

    protected abstract void RenameOldSettings();

    protected int CheckValue(int value, int min, int max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected double CheckValue(double value, double min, double max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected float CheckValue(float value, float min, float max)
    {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }
    protected float CheckValue(float value, float min, float max, float minValue)
    {
        value = CheckValue(value, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

    protected int CheckValue(int value, int min, int max, int minValue)
    {
        value = CheckValue(value, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

    protected ArrayList<String> CheckValue(ArrayList<String> biomes, ArrayList<String> customBiomes)
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
}