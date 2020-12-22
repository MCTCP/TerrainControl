package com.pg85.otg.spigot.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

public class SpigotMaterialReader implements IMaterialReader
{
	@Override
	public LocalMaterialData readMaterial (String string) throws InvalidConfigException
	{
		return SpigotMaterials.readMaterial(string);
	}
}
