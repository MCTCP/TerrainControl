package com.pg85.otg.config.io;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * A class for writing a {@link SettingsMap} to a file.
 *
 */
public final class FileSettingsWriter
{
	private final File file;
	private final boolean writeComments;

	private FileSettingsWriter(File file, boolean writeComments)
	{
		this.file = file;
		this.writeComments = writeComments;
	}
	
	/**
	 * Writes the configuration settings to the given file. If writing fails,
	 * the error is logged.
	 *
	 * @param config	 The configuration to write to disk.
	 * @param file		The file to write to.
	 * @param configMode The configuration mode. If this is set to
	 * WriteDisable, this method does nothing.
	 */
	public static final void writeToFile(SettingsMap config, File file, ConfigMode configMode, ILogger logger)
	{
		if (configMode == ConfigMode.WriteDisable)
		{
			return;
		}

		boolean writeComments = configMode != ConfigMode.WriteWithoutComments;
		FileSettingsWriter writer = new FileSettingsWriter(file, writeComments);
		try
		{
			writer.write(config);
		}
		catch (IOException e)
		{
			logger.log(
				LogLevel.ERROR,
				LogCategory.CONFIGS,
				String.format("Failed to write to file " + file + ", error: ",(Object[])e.getStackTrace())
			);
		}
	}

	/**
	 * Writes the settings map to the file.
	 * @param settingsMap The settings map.
	 * @throws IOException If an IO error occurs.
	 */
	private void write(SettingsMap settingsMap) throws IOException
	{
		BufferedWriter writer = null;
		try
		{
			File directory = file.getParentFile();
			if (!directory.exists() && !directory.mkdirs())
			{
				throw new IOException("Could not create directory '" + file.getParentFile() + "'");
			}

			writer = new BufferedWriter(new FileWriter(file));
			for (RawSettingValue entry : settingsMap.getRawSettings())
			{
				writeEntry(writer, entry);
			}
		} finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}

	private void writeEntry(BufferedWriter writer, RawSettingValue value) throws IOException
	{
		switch (value.getType())
		{
			case BIG_TITLE:
				bigTitle(writer, value.getRawValue());
				comments(writer, value.getComments());
				break;
			case BIG_TITLE_2:
				bigTitle2(writer, value.getRawValue());
				comments(writer, value.getComments());
				break;
			case SMALL_TITLE:
				smallTitle(writer, value.getRawValue());
				comments(writer, value.getComments());
				break;
			case PLAIN_SETTING:
				comments(writer, value.getComments());
				writer.write(value.getRawValue());
				writer.newLine();
				writer.newLine();
				break;
			default:
				comments(writer, value.getComments());
				writer.write(value.getRawValue());
				writer.newLine();
				break;
		}
	}

	private void bigTitle(BufferedWriter writer, String title) throws IOException
	{
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
	
	private void bigTitle2(BufferedWriter writer, String title) throws IOException
	{
		writer.newLine();
		writer.write("#######################################################################");
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
		writer.write("#######################################################################");
		writer.newLine();
		writer.newLine();
	}

	private void comments(BufferedWriter writer, Collection<String> comments) throws IOException
	{
		if (!this.writeComments)
			return;

		for (String comment : comments)
		{
			comment(writer, comment);
		}
	}

	private void comment(BufferedWriter writer, String comment) throws IOException
	{
		if (!this.writeComments)
			return;
		if (comment.length() > 0)
			writer.write("# " + comment);
		writer.newLine();
	}

	private void smallTitle(BufferedWriter writer, String title) throws IOException
	{
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
