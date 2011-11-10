package com.Khorn.TerrainControl.Configuration;

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
                int lineNumber = 0;
                while ((thisLine = SettingsReader.readLine()) != null)
                {
                    if (thisLine.trim().equals(""))
                        continue;
                    if (thisLine.startsWith("#") || thisLine.startsWith("<"))
                        continue;
                    if (thisLine.toLowerCase().contains(":"))
                    {
                        String[] splitSettings = thisLine.split(":");
                        if (splitSettings.length == 2)
                        {
                            this.SettingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                            this.SettingsCache.put(splitSettings[0].trim(), splitSettings[1].trim());
                        }
                    } else
                        this.SettingsCache.put(thisLine.trim(), Integer.toString(lineNumber));
                    lineNumber++;


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
        settingsName = settingsName.toLowerCase();
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
        settingsName = settingsName.toLowerCase();
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
            } catch (NumberFormatException e)
            {
                System.out.println("TerrainControl: " + settingsName + " had wrong value");
            }
        }
        return defaultValue;
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
