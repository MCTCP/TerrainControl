package com.pg85.otg.forge.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BO3Creator {

	private String name;
	private boolean includeAir = false;
	private boolean includeTiles = false;

	public BO3Creator(String name) {
		this.name = name;
	}

	public boolean create(Region selection, World world, String blockName, boolean branch) {
		int tileEntityCount = 1;

		File tileEntitiesFolder = new File(OTG.getEngine().getGlobalObjectsDirectory(), name);

		if (includeTiles) {
			tileEntitiesFolder.mkdirs();
		}

		Vector start = selection.getMinimumPoint();
		Vector end = selection.getMaximumPoint();

		LocalMaterialData centerBlock = null;
		if (!blockName.isEmpty()) {
			try {
				centerBlock = OTG.getEngine().readMaterial(blockName);
			} catch (InvalidConfigException e1) {
				centerBlock = null;
			}
		}

		int width = selection.getWidth();
		int length = selection.getLength();
		int height = selection.getHeight();

		int widthMin = width;
		int heightMin = height;
		int lengthMin = length;
		int widthMax = Integer.MIN_VALUE;
		int heightMax = Integer.MIN_VALUE;
		int lengthMax = Integer.MIN_VALUE;

		boolean centerBlockFound = false;
		int centerPointX = 0;
		int centerPointY = 0;
		int centerPointZ = 0;

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					IBlockState block = world.getBlockState(
							new BlockPos(x + start.getBlockX(), y + start.getBlockY(), z + start.getBlockZ()));

					ForgeMaterialData data = ForgeMaterialData.ofMinecraftBlockState(block);

					if (centerBlock != null && centerBlock.equals(data)) {
						centerPointX = x + start.getBlockX();
						centerPointY = y + start.getBlockY();
						centerPointZ = z + start.getBlockZ();
						centerBlockFound = true;
					}

					if (x < widthMin) {
						widthMin = x;
					}
					if (y < heightMin) {
						heightMin = y;
					}
					if (z < lengthMin) {
						lengthMin = z;
					}

					if (x > widthMax) {
						widthMax = x;
					}
					if (y > heightMax) {
						heightMax = y;
					}
					if (z > lengthMax) {
						lengthMax = z;
					}

				}
			}
		}

		if (centerBlock == null || !centerBlockFound) {
			centerPointX = (int) Math.floor((start.getBlockX() + end.getBlockX()) / 2d);
			centerPointY = start.getBlockY();
			centerPointZ = (int) Math.floor((start.getBlockZ() + end.getBlockZ()) / 2d);
		}

		Map<ChunkCoordinate, List<BO3BlockFunction>> blocksPerChunkArr = new HashMap<ChunkCoordinate, List<BO3BlockFunction>>();

		for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
			for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
				for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {

					List<BO3BlockFunction> blocksInChunk = new ArrayList<BO3BlockFunction>();
					ChunkCoordinate chunkCoordinates;

					if (branch) {
						chunkCoordinates = ChunkCoordinate.fromChunkCoords(
								(int) Math.floor((x - start.getBlockX()) / 16),
								(int) Math.floor((z - start.getBlockZ()) / 16));
					} else {
						chunkCoordinates = ChunkCoordinate.fromChunkCoords(0, 0);
					}

					if (blocksPerChunkArr.get(chunkCoordinates) == null) {
						blocksPerChunkArr.put(chunkCoordinates, blocksInChunk);
					} else {
						blocksInChunk = blocksPerChunkArr.get(chunkCoordinates);
					}

					IBlockState block = world.getBlockState(new BlockPos(x, y, z));

					ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(block);

					if (includeAir || !material.isMaterial(DefaultMaterial.AIR)) {
						BO3BlockFunction blockFunction;

						if (branch) {
							blockFunction = (BO3BlockFunction) CustomObjectConfigFunction.create(null,
									BO3BlockFunction.class,
									x - ((chunkCoordinates.getBlockX() * 16) + start.getBlockX()) - 8, y - centerPointY,
									z - ((chunkCoordinates.getChunkZ() * 16) + start.getBlockZ()) - 7, material);
						} else {
							blockFunction = (BO3BlockFunction) CustomObjectConfigFunction.create(null,
									BO3BlockFunction.class, x - centerPointX, y - centerPointY, z - centerPointZ,
									material);
						}

						if (includeTiles) {
							// Look for tile entities
							NamedBinaryTag tag = NBTHelper.getMetadata(world, x, y, z);
							if (tag != null) {
								String tileEntityName;

								if (branch) {
									tileEntityName = tileEntityCount + "-" + getTileEntityName(tag) + "C"
											+ chunkCoordinates.getBlockX() + "R" + chunkCoordinates.getBlockZ()
											+ ".nbt";
								} else {
									tileEntityName = tileEntityCount + "-" + getTileEntityName(tag) + ".nbt";
								}

								File tileEntityFile = new File(tileEntitiesFolder, tileEntityName);

								tileEntityCount++;
								try {
									tileEntityFile.createNewFile();
									FileOutputStream fos = new FileOutputStream(tileEntityFile);
									tag.writeTo(fos);
									fos.flush();
									fos.close();
									blockFunction.metaDataTag = tag;
									blockFunction.metaDataName = name + "/" + tileEntityName;
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}

						}
						blocksInChunk.add(blockFunction);
					}
				}
			}
		}

		BO3 bo3 = null;
		boolean isStartBO3 = true;

		for (int x1 = 0; x1 <= Math.abs(widthMin - widthMax); x1++) {
			for (int z1 = 0; z1 <= Math.abs(lengthMin - lengthMax); z1++) {
				List<BO3BlockFunction> blocks = blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1, z1));

				if (blocks == null || blocks.isEmpty())
					continue;

				for (int i = 0; i < 1; i++) {

					List<BO3BranchFunction> branches = new ArrayList<BO3BranchFunction>();

					if (branch) {

						if (isStartBO3) {
							branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null,
									BO3BranchFunction.class, 0, 0, 0, (name + "C0R0"), "NORTH", 100));
						}

						if (!isStartBO3 && blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1 + 1, z1)) != null) {
							branches.add(
									(BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class,
											16, 0, 0, (name + "C" + (x1 + 1) + "R" + z1), "NORTH", 100));
						}

						if (!isStartBO3 && x1 == 0) {
							if (blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1, z1 + 1)) != null) {
								branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null,
										BO3BranchFunction.class, 0, 0, 16, (name + "C" + x1 + "R" + (z1 + 1)), "NORTH",
										100));
							}
						}
					}

					if (isStartBO3) {
						bo3 = new BO3(name, new File(OTG.getEngine().getGlobalObjectsDirectory(), name + ".bo3"));
					} else {
						bo3 = new BO3(name, new File(OTG.getEngine().getGlobalObjectsDirectory(),
								name + "C" + x1 + "R" + z1 + ".bo3"));
					}

					bo3.onEnable();

					if (!isStartBO3 || !branch)
						bo3.getSettings().extractBlocks(blocks);

					if (!branches.isEmpty())
						bo3.getSettings().setBranches(branches);

					bo3.getSettings().rotateBlocksAndChecks();

					OTG.getCustomObjectManager().registerGlobalObject(bo3);

					bo3.getSettings().settingsMode = ConfigMode.WriteAll;

					try {
						SettingsWriterOTGPlus writer = new FileSettingsWriterOTGPlus(bo3.getSettings().getFile());
						bo3.getSettings().write(writer, ConfigMode.WriteAll);
					} catch (IOException ex) {
						OTG.log(LogMarker.ERROR, "Failed to write to file {}", bo3.getSettings().getFile());
						OTG.printStackTrace(LogMarker.ERROR, ex);
						return false;
					}

					if (isStartBO3) {
						isStartBO3 = false;
						if (branch) {
							i--;
						}
					}
				}

			}
		}
		return true;

	}

	private String getTileEntityName(NamedBinaryTag tag) {
		NamedBinaryTag idTag = tag.getTag("id");
		if (idTag != null) {
			String name = (String) idTag.getValue();

			return name.replace("minecraft:", "").replace(':', '_');
		}
		return "Unknown";
	}

	public void includeAir(boolean include) {
		this.includeAir = include;
	}

	public void includeTiles(boolean include) {
		this.includeTiles = include;
	}

}
