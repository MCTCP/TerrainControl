package com.pg85.otg.spigot.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

public class SpigotMaterialReader implements IMaterialReader
{
	@Override
	public LocalMaterialData readMaterial (String string) throws InvalidConfigException
	{
		// Might be useful for debugging materials later
		//OTG.log(LogMarker.TRACE, "Trying to read material: " + string);
		//LocalMaterialData tmp = ;
		//OTG.log(LogMarker.TRACE, "Result: " + (tmp == null ? "null" : tmp.toString()));
		return SpigotMaterials.readMaterial(string);
	}

	@Override
	public LocalMaterialTag readTag(String string) throws InvalidConfigException
	{
		return SpigotMaterials.readTag(string);
	}
}
