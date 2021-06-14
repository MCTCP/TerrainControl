package com.pg85.otg.forge.commands;

import com.mojang.brigadier.context.CommandContext;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

public class ExportCommand
{
	protected static HashMap<Entity, Region> playerSelectionMap = new HashMap<>();

	public static int execute(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try
		{
			if (!(source.getEntity() instanceof ServerPlayerEntity))
			{
				source.sendSuccess(new StringTextComponent("Only players can execute this command"), false);
				return 0;
			}

			// Extract here; this is kinda complex, would be messy in OTGCommand
			String objectName = "";
			BlockState centerBlockState = null;
			String presetName = null;		
			String templateName = "default";
			boolean overwrite = false, branching = false, includeAir = false;

			try
			{
				objectName = context.getArgument("name", String.class);
				centerBlockState = context.getArgument("center", BlockStateInput.class).getState();
				presetName = context.getArgument("preset", String.class);
				templateName = context.getArgument("template", String.class);
				// Flags as a string - easiest and clearest way I've found of adding multiple boolean flags
				String flags = context.getArgument("flags", String.class);
				overwrite = flags.contains("-o");
				branching = flags.contains("-b");
				includeAir = flags.contains("-a");
			}
			catch (IllegalArgumentException ignored)
			{
			} // We can deal with any of these not being there

			presetName = presetName != null && presetName.equalsIgnoreCase("global") ? null : presetName;
			boolean isGlobal = presetName == null;
			
			if (objectName.equalsIgnoreCase(""))
			{
				source.sendSuccess(new StringTextComponent("Please specify a name for the object"), false);
				return 0;
			}

			Region region = playerSelectionMap.get(source.getEntity());
			if (region == null || region.getLow() == null)
			{
				source.sendSuccess(new StringTextComponent("Please mark two corners with /otg region mark"), false);
				return 0;
			}
			Preset preset = getPresetOrDefault(presetName);
			if (preset == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find preset " + (presetName == null ? "" : presetName)), false);
				return 0;
			}

			Path objectPath = getObjectPath(isGlobal ? null : preset.getPresetFolder());

			if (!overwrite)
			{
				if (new File(objectPath.toFile(), objectName + ".bo3").exists())
				{
					source.sendSuccess(new StringTextComponent("File already exists, run command with flag '-o' to overwrite"), false);
					return 0;
				}
			}

			LocalWorldGenRegion otgRegion = new ForgeWorldGenRegion(
				preset.getFolderName(), preset.getWorldConfig(), source.getLevel(),
				source.getLevel().getChunkSource().getGenerator()
			);
			LocalNBTHelper nbtHelper = new ForgeNBTHelper();
			BOCreator.Corner lowCorner = region.getLow();
			BOCreator.Corner highCorner = region.getHigh();
			BOCreator.Corner center = new BOCreator.Corner((highCorner.x - lowCorner.x) / 2 + lowCorner.x, lowCorner.y, (highCorner.z - lowCorner.z) / 2 + lowCorner.z);

			// Fetch template or default settings
			BO3 template = (BO3) OTG.getEngine().getCustomObjectManager().getObjectLoaders().get("bo3")
				.loadFromFile(templateName, new File(objectPath.toFile(), templateName + ".BO3Template"), OTG.getEngine().getLogger());

			// Initialize the settings
			template.onEnable(preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
			BO3 bo3;

			if (branching)
			{
				try
				{
					bo3 = BO3Creator.createStructure(lowCorner, highCorner, center, objectName, includeAir, objectPath, otgRegion,
						nbtHelper, null, template.getSettings(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
						OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
						OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				}
				catch (Exception e)
				{
					OTG.log(LogMarker.INFO, e.toString());
					for (StackTraceElement s : e.getStackTrace())
					{
						OTG.log(LogMarker.INFO, s.toString());
					}
					return 0;
				}
			} else {
				// Create a new BO3 from our settings
				LocalMaterialData centerBlock = centerBlockState == null ? null : ForgeMaterialData.ofBlockState(centerBlockState);
				bo3 = BO3Creator.create(lowCorner, highCorner, center, centerBlock, objectName, includeAir,
					objectPath, otgRegion, nbtHelper, null, template.getSettings(), preset.getFolderName(),
					OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
					OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
					OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
			}

			// Send feedback, and register the BO3 for immediate use
			if (bo3 != null)
			{
				source.sendSuccess(new StringTextComponent("Successfully created BO3 " + objectName), false);
				if (isGlobal)
				{
					OTG.getEngine().getCustomObjectManager().registerGlobalObject(bo3, bo3.getSettings().getFile());
				} else {
					OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(preset.getFolderName(), bo3.getName().toLowerCase(Locale.ROOT), bo3.getSettings().getFile(), bo3);
				}
			} else {
				source.sendSuccess(new StringTextComponent("Failed to create BO3 " + objectName), false);
			}
		} catch (Exception e) {
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.log(LogMarker.INFO, e.toString());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.log(LogMarker.INFO, s.toString());
			}
		}

		return 0;
	}

	protected static Path getObjectPath(Path presetFolder)
	{
		Path objectPath;
		if (presetFolder == null)
		{
			objectPath = OTG.getEngine().getGlobalObjectsFolder();
		} else {
			objectPath = presetFolder.resolve(Constants.WORLD_OBJECTS_FOLDER);

		}

		if (!objectPath.toFile().exists())
		{
			if (objectPath.resolve("..").resolve("WorldObjects").toFile().exists())
			{
				objectPath = objectPath.resolve("..").resolve("WorldObjects");
			}
		}
		return objectPath;
	}

	protected static Preset getPresetOrDefault(String presetFolderName)
	{
		if (presetFolderName == null)
		{
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		} else {
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(presetFolderName);
		}
	}

	public static int mark(CommandSource source)
	{
		if (!init(source)) return 0;
		playerSelectionMap.get(source.getEntity()).setPos(source.getEntity().blockPosition());
		source.sendSuccess(new StringTextComponent("Position marked"), false);
		return 0;
	}

	public static int clear(CommandSource source)
	{
		if (!init(source)) return 0;
		playerSelectionMap.get(source.getEntity()).clear();
		source.sendSuccess(new StringTextComponent("Position cleared"), false);
		return 0;
	}

	public static int expand(CommandSource source, String direction, Integer value)
	{
		if (!init(source)) return 0;
		Region region = playerSelectionMap.get(source.getEntity());
		if (region.getLow() == null)
		{
			source.sendSuccess(new StringTextComponent("Please mark two positions before modifying or exporting the region"), false);
			return 0;
		}
		switch (direction)
		{
			case "south": // positive Z
				region.setHighCorner(new BOCreator.Corner(region.high.x, region.high.y, region.high.z + value));
				break;
			case "north": // negative Z
				region.setLowCorner(new BOCreator.Corner(region.low.x, region.low.y, region.low.z - value));
				break;
			case "east": // positive X
				region.setHighCorner(new BOCreator.Corner(region.high.x + value, region.high.y, region.high.z));
				break;
			case "west": // negative X
				region.setLowCorner(new BOCreator.Corner(region.low.x - value, region.low.y, region.low.z));
				break;
			case "up": // positive y
				region.setHighCorner(new BOCreator.Corner(region.high.x, region.high.y + value, region.high.z));
				break;
			case "down": // negative y
				region.setLowCorner(new BOCreator.Corner(region.low.x, region.low.y - value, region.low.z));
				break;
			default:
				source.sendSuccess(new StringTextComponent("Unrecognized direction " + direction), false);
				return 0;
		}

		source.sendSuccess(new StringTextComponent("Region modified"), false);
		return 0;
	}

	public static int shrink(CommandSource source, String direction, Integer value)
	{
		if (!init(source)) return 0;

		expand(source, direction, -value);

		return 0;
	}

	private static boolean init(CommandSource source)
	{
		if (!(source.getEntity() instanceof ServerPlayerEntity))
		{
			source.sendSuccess(new StringTextComponent("Only players can execute this command"), false);
			return false;
		}
		if (!playerSelectionMap.containsKey(source.getEntity()))
		{
			playerSelectionMap.put(source.getEntity(), new Region());
		}
		return true;
	}

	protected static Region getRegionFromObject(int x, int y, int z, BO3 bo3)
	{
		ExportCommand.Region region = new ExportCommand.Region();
		BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
		region.setPos(new BlockPos(x + box.getMinX(), y + box.getMinY(), z + box.getMinZ()));
		region.setPos(new BlockPos(
			x + box.getMinX() + box.getWidth(),
			y + box.getMinY() + box.getHeight(),
			z + box.getMinZ() + box.getDepth()));
		return region;
	}

	public static class Region
	{
		private BOCreator.Corner low = null;
		private BOCreator.Corner high = null;
		private final BlockPos[] posArr = new BlockPos[2];

		public Region()
		{
			posArr[0] = null;
			posArr[1] = null;
		}

		public void setPos(BlockPos blockPos)
		{
			if (posArr[0] == null) posArr[0] = blockPos;
			else if (posArr[1] == null)
			{
				posArr[1] = blockPos;
				updateCorners();
			}
			else
			{
				posArr[0] = posArr[1];
				posArr[1] = blockPos;
				updateCorners();
			}
		}

		public void clear()
		{
			posArr[0] = null;
			posArr[1] = null;
			low = null;
			high = null;
		}

		public BOCreator.Corner getLow()
		{
			return low;
		}

		public BOCreator.Corner getHigh()
		{
			return high;
		}

		protected void setLowCorner(BOCreator.Corner newCorner)
		{
			this.low = newCorner;
		}

		protected void setHighCorner(BOCreator.Corner newCorner)
		{
			this.high = newCorner;
		}

		private void updateCorners()
		{
			low = new BOCreator.Corner(
				Math.min(posArr[0].getX(), posArr[1].getX()),
				Math.min(posArr[0].getY(), posArr[1].getY()),
				Math.min(posArr[0].getZ(), posArr[1].getZ())
			);
			high = new BOCreator.Corner(
				Math.max(posArr[0].getX(), posArr[1].getX()),
				Math.max(posArr[0].getY(), posArr[1].getY()),
				Math.max(posArr[0].getZ(), posArr[1].getZ())
			);
		}
	}
}
