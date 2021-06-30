package com.pg85.otg.interfaces;

import java.nio.file.Path;
import java.util.List;

public interface ICustomStructureGen
{
	List<IStructuredCustomObject> getObjects(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
	Double getObjectChance(int i);
	String getObjectName(int i);
	boolean isEmpty();
}
