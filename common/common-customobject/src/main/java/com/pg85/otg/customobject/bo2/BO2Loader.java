package com.pg85.otg.customobject.bo2;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectLoader;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.logging.ILogger;

import java.io.File;

public class BO2Loader implements CustomObjectLoader
{
	@Override
	public CustomObject loadFromFile(String objectName, File file, ILogger logger)
	{
		return new BO2(new FileSettingsReaderBO4(objectName, file, logger));
	}

	@Override
	public void onShutdown() { }
}
