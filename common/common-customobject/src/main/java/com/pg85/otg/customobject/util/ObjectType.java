package com.pg85.otg.customobject.util;

import java.nio.file.Path;

public enum ObjectType
{
	BO2("BO2", 2),
	BO3("BO3", 3),
	BO4("BO4", 4);

	private final String type;

	private final int version;

	ObjectType(String type, int version)
	{
		this.type = type;
		this.version = version;
	}

	public String getType()
	{
		return type;
	}

	public int getVersion()
	{
		return version;
	}

	public Path getObjectFilePathFromName(String objectName, Path objectFolderPath)
	{
		return objectFolderPath.resolve(objectName + "." + type);
	}

	public boolean filenameIsOfType(String fileName)
	{
		return fileName.matches(".+[Bb][Oo]" + version);
	}

	public String getFileNameForTemplate(String templateName)
	{
		return templateName + "." + type + "Template";
	}
}
