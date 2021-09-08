package com.pg85.otg.paper.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.pg85.otg.util.gen.JigsawStructureData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.paper.biome.OTGBiomeProvider;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.paper.gen.OTGPaperChunkGen;
import com.pg85.otg.paper.gen.PaperChunkBuffer;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.world.level.block.Blocks;

public class MapCommand extends BaseCommand
{
	private static final List<String> TYPES = new ArrayList<>(Arrays.asList("biomes", "terrain"));
	
	public MapCommand() {
		super("map");
		this.helpMessage = "Generates an image of the biome or terrain layout.";
		this.usage = "/otg map <biomes/terrain> [width] [height] [threads]";
		this.detailedHelp = new String[] { 
				"<biomes/terrain>: The type of map to create.",
				" - biomes: Creates an image using the color specified in each biome's config file.",
				" - terrain: Creates an image using the colours of the blocks shaded to show the altitude of the terrain.",
				"[width]: Image width in pixels.",
				"[height]: Image height in pixels.",
				"[threads]: The number of threads to use while rendering the image."
				
			};
	}
	
	public boolean execute(CommandSender sender, String[] args)
	{
		if (args.length == 0) {
			return true;
			// TODO show usage
		}
		if (args[0].equalsIgnoreCase("biomes"))
		{
			return mapBiomes(sender, args);
		} else if (args[0].equalsIgnoreCase("terrain"))
		{
			return mapTerrain(sender, args);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return StringUtil.copyPartialMatches(args[1], TYPES, new ArrayList<>());
	}
	
	private boolean mapBiomes (CommandSender sender, String[] args)
	{
		CraftWorld world;
		Player player;
		int size = 2048;
		int offsetX = 0;
		int offsetZ = 0;
		String name = "";
		for (int i = 1; i < args.length-1; i++)
		{
			if (args[i].equalsIgnoreCase("-s"))
				size = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-ox"))
				offsetX = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-oz"))
				offsetZ = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-n"))
				name = args[i+1];
		}
		if (sender instanceof Player)
		{
			player = (Player) sender;
			world = (CraftWorld) player.getWorld();
			if (offsetX == 0 && offsetZ == 0)
			{
				offsetX += player.getLocation().getBlockX();
				offsetZ += player.getLocation().getBlockZ();
			}
		} else {
			sender.sendMessage("Only in-game for now");
			return true;
		}
		if (!(world.getHandle().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			sender.sendMessage("This is not an OTG world");
			return true;
		}

		ICachedBiomeProvider provider = ((OTGNoiseChunkGenerator)world.getHandle().getChunkSource().getGenerator()).getCachedBiomeProvider();

		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int noiseX = 0; noiseX < img.getHeight(); noiseX++)
		{
			for (int noiseZ = 0; noiseZ < img.getWidth(); noiseZ++)
			{
				// TODO: Fetch biome data per chunk, not per column, probably very slow atm.
				// TODO: Forge doesn't use an offset, always starts at 0,0?
				img.setRGB(noiseX, noiseZ, provider.getNoiseBiomeConfig(noiseX + offsetX, noiseZ + offsetZ, true).getBiomeColor());
			}
			if (noiseX % progressUpdate == 0)
			{
				sender.sendMessage((((double) noiseX / img.getHeight()) * 100) + "% Done mapping");
			}
		}

		String fileName = player.getWorld().getName()+" "+name+" biomes.png";
		sender.sendMessage("Finished mapping! The resulting image is located at " + fileName + ".");
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}
	
	@SuppressWarnings("resource")
	private boolean mapTerrain (CommandSender sender, String[] args)
	{
		CraftWorld world;
		Player player;
		int size = 2048;
		int offsetX = 0;
		int offsetZ = 0;
		String name = "";
		for (int i = 1; i < args.length-1; i++)
		{
			if (args[i].equalsIgnoreCase("-s"))
				size = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-ox"))
				offsetX = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-oz"))
				offsetZ = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-n"))
				name = args[i+1];
		}
		if (sender instanceof Player)
		{
			player = (Player) sender;
			world = (CraftWorld) player.getWorld();
			if (offsetX == 0 && offsetZ == 0)
			{
				offsetX += player.getLocation().getBlockX();
				offsetZ += player.getLocation().getBlockZ();
			}
		} else {
			sender.sendMessage("Only in-game for now");
			return true;
		}
		if (!(world.getHandle().getChunkSource().getGenerator().getBiomeSource() instanceof OTGBiomeProvider))
		{
			sender.sendMessage("This is not an OTG world");
			return true;
		}
	
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		int progressUpdate = img.getHeight() / 8;
		HighestBlockInfo highestBlockInfo;
		int min = 0;
		int max = 255;
		int range = max - min;
		int distance;
		float relativeDistance;
		int shadePercentage;
		int rgbColor;
		int progress = 0;
		for (int chunkX = 0; chunkX < (int)Math.ceil(img.getWidth() / 16f); chunkX++)
		{
			for (int chunkZ = 0; chunkZ < (int)Math.ceil(img.getHeight() / 16f); chunkZ++)
			{
//				PaperChunkBuffer chunk = ((OTGPaperChunkGen)world.getHandle().generator).generator.getChunkWithoutLoadingOrCaching(world.getHandle().getRandom(), ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
				// TODO: make caching work again
				PaperChunkBuffer chunk = new PaperChunkBuffer(new ProtoChunk(new ChunkPos(chunkX, chunkZ), null, world.getHandle(), world.getHandle()));
				ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
				ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

				((OTGPaperChunkGen)world.getHandle().generator).generator.internalGenerator.populateNoise(256, new Random(/*TODO seed!*/), chunk, chunk.getChunkCoordinate(), structures, junctions);

				for(int internalX = 0; internalX < Constants.CHUNK_SIZE; internalX++)
				{
					for(int internalZ = 0; internalZ < Constants.CHUNK_SIZE; internalZ++)
					{
						if(
							chunkX * Constants.CHUNK_SIZE + internalX < img.getWidth() &&
							chunkZ * Constants.CHUNK_SIZE + internalZ < img.getHeight()
						)
						{
							highestBlockInfo = getHighestBlockInfoInUnloadedChunk(chunk, internalX, internalZ);
			
							// Color depth relative to waterlevel
							//int worldHeight = 255;
							//int worldWaterLevel = 63;
							//int min = worldWaterLevel - worldHeight;
							//int max = worldWaterLevel + worldHeight;
							// Color depth relative to 0-255						
							distance = -min + highestBlockInfo.y;
							relativeDistance = (float)distance / (float)range;
							shadePercentage = (int)Math.floor(relativeDistance * 2 * 100);
							rgbColor = shadeColor(highestBlockInfo.material.internalBlock().getBlock().defaultMaterialColor().col, shadePercentage);
							img.setRGB(chunkX * Constants.CHUNK_SIZE + internalX, chunkZ * Constants.CHUNK_SIZE + internalZ, rgbColor);						
						}
					}
				}
				progress++;
				if (progress % progressUpdate == 0)
				{
					sender.sendMessage((((double) chunkX / (int)Math.ceil(img.getWidth() / 16f)) * 100) + "% Done mapping");
				}				
			}
		}

		String fileName = player.getWorld().getName()+" " + name + " terrain.png";
		sender.sendMessage("Finished mapping! The resulting image is located at " + fileName + ".");
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}

	private static int shadeColor(int rgbColor, int percent)
	{
		int red = (rgbColor >> 16) & 0xFF;
		int green = (rgbColor >> 8) & 0xFF;
		int blue = rgbColor & 0xFF;
		
		red = red * percent / 100;
		red = Math.min(red, 255);
		green = green * percent / 100;
		green = Math.min(green, 255);
		blue = blue * percent / 100;
		blue = Math.min(blue, 255);

		return 65536 * red + 256 * green + blue;
	}

	private HighestBlockInfo getHighestBlockInfoInUnloadedChunk(PaperChunkBuffer chunk, int internalX, int internalZ)
	{
		// TODO: Just use heightmaps?
		BlockState blockInChunk;
		for (int y = chunk.getHighestBlockForColumn(internalX, internalZ); y >= 0; y--)
		{
			blockInChunk = chunk.getChunk().getType(internalX, y, internalZ);
			if (blockInChunk.getBlock() != Blocks.AIR)
			{
				return new HighestBlockInfo((PaperMaterialData) PaperMaterialData.ofBlockData(blockInChunk), y);
			}
		}
		return new HighestBlockInfo((PaperMaterialData) LocalMaterials.AIR, 63);
	}

	public record HighestBlockInfo(PaperMaterialData material, int y) {}
}
