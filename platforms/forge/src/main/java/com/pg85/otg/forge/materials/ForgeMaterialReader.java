package com.pg85.otg.forge.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

public class ForgeMaterialReader implements IMaterialReader
{
	@Override
	public LocalMaterialData readMaterial(String string) throws InvalidConfigException
	{
		return ForgeMaterials.readMaterial(string);
	}
}
