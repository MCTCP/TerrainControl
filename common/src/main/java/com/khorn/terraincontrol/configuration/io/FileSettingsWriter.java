package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileSettingsWriter implements SettingsWriter
{
    /**
     * Writes this configuration settings to a file. It will use the file
     * returned by {@link ConfigFile#getFile()}. If writing fails, the error
     * is logged using {@link ConfigFile#logIOError(IOException)}.
     *
     * @param config     The configuration to write to disk.
     * @param configMode The configuration mode. If this is set to
     * WriteDisable, this method does nothing.
     */
    public static final void writeToFile(ConfigFile config, ConfigMode configMode)
    {
        writeToFile(config, config.getFile(), configMode);
    }

    /**
     * Writes the configuration settings to the given file. If writing fails,
     * the error is logged using {@link ConfigFile#logIOError(IOException)}.
     *
     * @param config     The configuration to write to disk.
     * @param file       The file to write to.
     * @param configMode The configuration mode. If this is set to
     * WriteDisable, this method does nothing.
     */
    public static final void writeToFile(ConfigFile config, File file, ConfigMode configMode)
    {
        if (configMode == ConfigMode.WriteDisable)
        {
            return;
        }

        try
        {
            SettingsWriter writer = new FileSettingsWriter(file);
            config.write(writer, configMode);
        } catch (IOException e)
        {
            config.logIOError(e);
        }
    }

    private final File file;
    private boolean writeComments;
    private BufferedWriter writer;

    public FileSettingsWriter(ConfigFile configFile)
    {
        this.file = configFile.getFile();
    }

    public FileSettingsWriter(File file)
    {
        this.file = file;
    }

    @Override
    public void bigTitle(String title) throws IOException
    {
        checkState();
        writer.newLine();
        writer.write("#######################################################################");
        writer.newLine();
        writer.write("# +-----------------------------------------------------------------+ #");
        writer.newLine();
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
        writer.write("# |" + builder.toString() + "| #");
        writer.newLine();
        writer.write("# +-----------------------------------------------------------------+ #");
        writer.newLine();
        writer.write("#######################################################################");
        writer.newLine();
        writer.newLine();
    }

    private void checkState() throws IllegalStateException
    {
        if (writer == null)
        {
            throw new IllegalStateException("Not started writing yet");
        }
    }

    @Override
    public void close()
    {
        if (writer == null)
        {
            // Can happen if file opening failed, ignore
            return;
        }

        try
        {
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            TerrainControl.log(LogMarker.WARN, "Failed to close file {} ({})",
                    file.getAbsolutePath(), e.getMessage());
        }
        writer = null;
    }

    @Override
    public void comment(String comment) throws IOException
    {
        checkState();
        if (!this.writeComments)
            return;
        if (comment.length() > 0)
            writer.write("# " + comment);
        writer.newLine();
    }

    @Override
    public void function(ConfigFunction<?> function) throws IOException
    {
        checkState();
        writer.write(function.write());
        writer.newLine();
    }

    @Override
    public File getFile()
    {
        return file;
    }

    @Override
    public void open() throws IOException
    {
        file.getParentFile().mkdirs();
        writer = new BufferedWriter(new FileWriter(file));
    }

    @Override
    public void setConfigMode(ConfigMode configMode)
    {
        if (configMode == ConfigMode.WriteAll)
        {
            this.writeComments = true;
        } else if (configMode == ConfigMode.WriteWithoutComments)
        {
            this.writeComments = false;
        } else
        {
            throw new IllegalArgumentException("Invalid config mode: " + configMode);
        }
    }

    @Override
    public <T> void setting(Setting<T> setting, T value) throws IOException
    {
        checkState();
        writer.write(setting.getName() + ": " + setting.write(value));
        writer.newLine();
        writer.newLine();
    }

    @Override
    public void smallTitle(String title) throws IOException
    {
        checkState();
        int titleLength = title.length();
        StringBuilder rowBuilder = new StringBuilder(titleLength + 4);
        for (int i = 0; i < titleLength + 4; i++)
        {
            rowBuilder.append('#');
        }
        writer.write(rowBuilder.toString());
        writer.newLine();
        writer.write("# " + title + " #");
        writer.newLine();
        writer.write(rowBuilder.toString());
        writer.newLine();
        writer.newLine();
    }

}
