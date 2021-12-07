package com.pg85.otg.core.objectcreator;

import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Extractor
{
	public static List<BlockFunction<?>> getBlockFunctions
		(ObjectType type, Corner min, Corner max, Corner center, LocalWorldGenRegion localWorld,
		 LocalNBTHelper nbtHelper, boolean leaveIllegalLeaves, String objectName, File objectFolder,
		 Set<LocalMaterialData> filter)
	{
		File nbtFolder = new File(objectFolder, objectName);
		ArrayList<BlockFunction<?>> blocks = new ArrayList<>();

		for (int x = min.x; x <= max.x; x++)
		{
			for (int z = min.z; z <= max.z; z++)
			{
				for (int y = min.y; y <= max.y; y++)
				{
					LocalMaterialData materialData = localWorld.getMaterial(x, y, z);

					LocalMaterialData finalMaterialData = materialData;
					if (materialData == null
						// Returns true if any item in the filter set matches the found material
						|| filter.stream().anyMatch(item -> item.isMaterial(finalMaterialData))
						|| materialData.isMaterial(LocalMaterials.STRUCTURE_VOID)
						|| materialData.isMaterial(LocalMaterials.STRUCTURE_BLOCK))
					{
						continue;
					}

					if (materialData.isLeaves())
					{
						materialData = materialData.legalOrPersistentLeaves(leaveIllegalLeaves);
					}

					BlockFunction<?> block = switch (type)
						{
							case BO3 -> new BO3BlockFunction();
							case BO4 -> new BO4BlockFunction();
							case BO2 -> throw new RuntimeException("Tried to make BlockFunctions for a BO2");
						};
					block.material = materialData;
					block.nbt = null;
					block.nbtName = "";
					block.x = x - center.x;
					block.y = (short) (y - center.y);
					block.z = z - center.z;

					NamedBinaryTag nbt = nbtHelper.getNBTFromLocation(localWorld, x, y, z);
					if (nbt != null)
					{
						try
						{
							if (!nbtFolder.exists()) nbtFolder.mkdirs();
							String tileName = getTileEntityName(nbt);
							String nbtName = objectName + "/" + tileName + "_" + block.x + "_" + block.y + "_" + block.z + ".nbt";
							block.nbt = nbt;
							block.nbtName = nbtName;
							File nbtFile = new File(objectFolder, nbtName);
							nbtFile.delete(); // Make sure there is no leftover file here from before
							nbtFile.createNewFile(); // Make the new file
							FileOutputStream stream = new FileOutputStream(nbtFile);
							nbt.writeTo(stream); // Write the new file to disk
							stream.flush();
							stream.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					blocks.add(block);
				}
			}
		}
		return blocks;
	}

	public static String getTileEntityName(NamedBinaryTag tag)
	{
		NamedBinaryTag idTag = tag.getTag("id");
		if (idTag != null)
		{
			String name = (String) idTag.getValue();

			return name.replace("minecraft:", "").replace(':', '_');
		}
		return "Unknown";
	}
}
