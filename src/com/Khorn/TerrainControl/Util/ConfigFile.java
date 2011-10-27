package com.Khorn.TerrainControl.Util;

import java.io.*;
import java.util.HashMap;

public abstract class ConfigFile
{

    private BufferedWriter SettingsWriter;
    protected HashMap<String, String> SettingsCache = new HashMap<String, String>();


    protected void ReadSettingsFile(File f)
    {
        BufferedReader SettingsReader = null;

        if (f.exists())
        {

            try
            {
                SettingsReader = new BufferedReader(new FileReader(f));
                String thisLine;
                while ((thisLine = SettingsReader.readLine()) != null)
                {
                    if (thisLine.toLowerCase().contains(":"))
                    {
                        String[] splitSettings = thisLine.split(":");
                        if (splitSettings.length == 2)
                            this.SettingsCache.put(splitSettings[0].trim(), splitSettings[1].trim());
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();

                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        localIOException1.printStackTrace();
                    }
            } finally
            {
                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
            }
        }
    }

    protected int ReadModSettings(String settingsName, int defaultValue)
    {
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Integer.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected byte ReadModSettings(String settingsName, byte defaultValue)
    {
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Byte.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected String ReadModSettings(String settingsName, String defaultValue)
    {
        if (this.SettingsCache.containsKey(settingsName))
        {
            return this.SettingsCache.get(settingsName);
        }
        return defaultValue;
    }

    protected double ReadModSettings(String settingsName, double defaultValue)
    {
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Double.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected float ReadModSettings(String settingsName, float defaultValue)
    {
        if (this.SettingsCache.containsKey(settingsName))
        {
            try
            {
                return Float.valueOf(this.SettingsCache.get(settingsName));
            } catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
    }

    protected boolean ReadModSettings(String settingsName, boolean defaultValue)
    {
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
        } catch (IOException e)
        {
            e.printStackTrace();

            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException1)
                {
                    localIOException1.printStackTrace();
                }
        } finally
        {
            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException2)
                {
                    localIOException2.printStackTrace();
                }
        }
    }

    protected void WriteModSettings(String settingsName, int settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteModSettings(String settingsName, double settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteModSettings(String settingsName, float settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Float.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteModSettings(String settingsName, boolean settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    protected void WriteModSettings(String settingsName, String settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + settingsValue);
        this.SettingsWriter.newLine();
    }

    protected void WriteModTitleSettings(String title) throws IOException
    {
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("<" + title + ">");
        this.SettingsWriter.newLine();
    }

    protected abstract void WriteConfigSettings() throws IOException;

    protected abstract void ReadConfigSettings();

    protected abstract void CorrectSettings();

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

    protected int CheckValue(int value, int min, int max, int minValue)
    {
        value = CheckValue(value, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

}
