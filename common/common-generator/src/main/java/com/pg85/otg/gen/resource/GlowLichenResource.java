package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

import java.util.ArrayList;
import java.util.List;

public class GlowLichenResource extends BiomeResourceBase
{
	public int minX;
	public int maxX;
	public int countMin;
	public int countMax;
	public int nearbyAttempts;
	public boolean canPlaceOnFloor;
	public boolean canPlaceOnCeiling;
	public boolean canPlaceOnWall;
	public float chanceOfSpreading;
	public List<LocalMaterialBase> canBePlacedOn;
	public GlowLichenResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		nearbyAttempts = readInt(args.get(0), 0, 1000);
		canPlaceOnFloor = args.get(1).equalsIgnoreCase("true");
		canPlaceOnCeiling = args.get(2).equalsIgnoreCase("true");
		canPlaceOnWall = args.get(3).equalsIgnoreCase("true");
		chanceOfSpreading = (float) readDouble(args.get(4), 0.0, 1.0);
		minX = readInt(args.get(5), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT);
		maxX = readInt(args.get(6), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT);
		countMin = readInt(args.get(7), 0, 1000);
		countMax = readInt(args.get(8), 0, 1000);

		canBePlacedOn = new ArrayList<>();
		for (int i = 9; i < args.size(); i++)
		{
			String input = args.get(i);
			LocalMaterialTag tag = materialReader.readTag(input);
			if(tag != null)
			{
				canBePlacedOn.add(tag);
			} else {
				LocalMaterialData material = materialReader.readMaterial(input);
				if(material == null)
				{
					throw new InvalidConfigException("Invalid entry for glow lichen source block, material \"" + input + "\" could not be found.");
				}
				canBePlacedOn.add(material);
			}
		}
		if (canBePlacedOn.isEmpty()) canBePlacedOn.add(materialReader.readTag("otg:stone"));
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder("GlowLichen(" + nearbyAttempts + "," + canPlaceOnWall + "," + canPlaceOnCeiling + "," + canPlaceOnWall+ "," + chanceOfSpreading);
		s.append(",").append(minX).append(",").append(maxX).append(",").append(countMin).append(",").append(countMax);
		for (LocalMaterialBase localMaterialBase : canBePlacedOn)
		{
			s.append(",");
			s.append(localMaterialBase.toString());
		}
		s.append(")");
		return s.toString();
	}
}
