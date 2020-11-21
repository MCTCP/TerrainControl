package com.pg85.otg.util.interfaces;

import java.nio.file.Path;
import java.util.List;

import com.pg85.otg.logging.ILogger;

public interface ICustomStructureGen
{
	List<IStructuredCustomObject> getObjects(String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, ICustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
	Double getObjectChance(int i);
	String getObjectName(int i);
	boolean isEmpty();
}
