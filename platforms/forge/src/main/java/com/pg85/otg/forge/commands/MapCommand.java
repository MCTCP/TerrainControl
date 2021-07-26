package com.pg85.otg.forge.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.ForgeChunkBuffer;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class MapCommand extends BaseCommand
{
	
	private static final String[] MAP_TYPES = new String[]
	{ "biomes", "terrain" };
	private static final Object queueLock = new Object();
	private static final Object imgLock = new Object();
	
	public MapCommand() {
		this.name = "map";
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
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("map")
			.executes(
				context -> map(context.getSource(), "", 2048, 2048, 0)
				).then(
					Commands.argument("type", StringArgumentType.word()).executes(
							context -> map(context.getSource(), StringArgumentType.getString(context, "type"), 2048, 2048, 0))
							.suggests(this::suggestTypes
					).then(
						Commands.argument("width", IntegerArgumentType.integer(0)).executes(
							(context) -> map(context.getSource(), StringArgumentType.getString(context, "type"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"), 1)
						).then(
							Commands.argument("height", IntegerArgumentType.integer(0)).executes(
								(context) -> map(context.getSource(), StringArgumentType.getString(context, "type"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"), 1)
							).then(
								Commands.argument("threads", IntegerArgumentType.integer(0)).executes(
									(context) -> map(context.getSource(), StringArgumentType.getString(context, "type"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"), IntegerArgumentType.getInteger(context, "threads"))
								)))))
		);
	}
	
	private int map(CommandSource source, String type, int width, int height, int threads)
	{
		switch (type.toLowerCase())
		{
			case "biomes":
				return mapBiomes(source, width, height, threads);
			case "terrain":
				return mapTerrain(source, width, height, threads);
			default:
				source.sendSuccess(new StringTextComponent(getUsage()), false);
				return 0;
		}
	}
	
	private static int mapBiomes(CommandSource source, int width, int height, int threads)
	{
		if (
			!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator) || 
			!(source.getLevel().getChunkSource().generator.getBiomeSource() instanceof OTGBiomeProvider)
		)
		{
			source.sendSuccess(new StringTextComponent("Please run this command in an OTG world."), false);
			return 1;
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		Instant start = Instant.now();
		handleArea(width, height, img, source, (OTGNoiseChunkGenerator)source.getLevel().getChunkSource().generator, true, threads);
		Instant finish = Instant.now();
		Duration duration = Duration.between(start, finish); // Note: This is probably the least helpful time duration helper class I've ever seen ...
		
		String fileName = source.getServer().getWorldData().getLevelName() + " biomes.png";		
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String hours = "" + (duration.toHours() > 9 ? duration.toHours() : "0" + duration.toHours());
		String minutes = "" + (duration.toMinutes() % 60 > 9 ? (duration.toMinutes() % 60) : "0" + (duration.toMinutes() % 60));
		String seconds = "" + (duration.get(ChronoUnit.SECONDS) % 60 > 9 ? (duration.get(ChronoUnit.SECONDS) % 60) : "0" + (duration.get(ChronoUnit.SECONDS) % 60));
		source.sendSuccess(new StringTextComponent("Finished mapping in " + hours + ":" + minutes + ":" + seconds + "! The resulting image is located at " + fileName + "."), true);
		
		return 0;
	}
	
	private static int mapTerrain(CommandSource source, int width, int height, int threads)
	{
		if (
			!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator) || 
			!(source.getLevel().getChunkSource().generator.getBiomeSource() instanceof OTGBiomeProvider)
		)
		{
			source.sendSuccess(new StringTextComponent("Please run this command in an OTG world."), false);
			return 1;
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		Instant start = Instant.now();
		handleArea(width, height, img, source, (OTGNoiseChunkGenerator)source.getLevel().getChunkSource().generator, false, threads);
		Instant finish = Instant.now();
		Duration duration = Duration.between(start, finish);		
		
		String fileName = source.getServer().getWorldData().getLevelName() + " terrain.png";		
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String hours = "" + (duration.toHours() > 9 ? duration.toHours() : "0" + duration.toHours());
		String minutes = "" + (duration.toMinutes() % 60 > 9 ? (duration.toMinutes() % 60) : "0" + (duration.toMinutes() % 60));
		String seconds = "" + (duration.get(ChronoUnit.SECONDS) % 60 > 9 ? (duration.get(ChronoUnit.SECONDS) % 60) : "0" + (duration.get(ChronoUnit.SECONDS) % 60));
		source.sendSuccess(new StringTextComponent("Finished mapping in " + hours + ":" + minutes + ":" + seconds + "! The resulting image is located at " + fileName + "."), true);
		
		return 0;
	}
	
	private static void handleArea(int width, int height, BufferedImage img, CommandSource source, OTGNoiseChunkGenerator generator, boolean mapBiomes, int threads)
	{
		// TODO: Optimise this, List<BlockPos2D> is lazy and handy for having workers pop a task 
		// off a stack until it's empty, ofc it's not efficient or pretty and doesn't scale.
		List<BlockPos2D> coordsToHandle = new ArrayList<BlockPos2D>(width * height);
		if(mapBiomes)
		{
			for (int chunkX = 0; chunkX < (int)Math.ceil(width / 4f); chunkX++)
			{
				for (int chunkZ = 0; chunkZ < (int)Math.ceil(height / 4f); chunkZ++)
				{
					coordsToHandle.add(new BlockPos2D(chunkX, chunkZ));
				}
			}
		} else {
			for (int chunkX = 0; chunkX < (int)Math.ceil(width / 16f); chunkX++)
			{
				for (int chunkZ = 0; chunkZ < (int)Math.ceil(height / 16f); chunkZ++)
				{
					coordsToHandle.add(new BlockPos2D(chunkX, chunkZ));
				}
			}
		}

		CountDownLatch latch = new CountDownLatch(threads);
		MapCommand outer = new MapCommand();
		int totalSize = coordsToHandle.size();
		for(int i = 0; i < threads; i++)
		{
			outer.new Worker(latch, source, generator, img, coordsToHandle, totalSize, mapBiomes, width, height).start();
		}
	
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static int shadeColor(int rgbColor, int percent)
	{
		int red = (rgbColor >> 16) & 0xFF;
		int green = (rgbColor >> 8) & 0xFF;
		int blue = rgbColor & 0xFF;
		
		red = red * percent / 100;
		red = red > 255 ? 255 : red;
		green = green * percent / 100;
		green = green > 255 ? 255 : green;
		blue = blue * percent / 100;
		blue = blue > 255 ? 255 : blue;

		return 65536 * red + 256 * green + blue;
	}
	
	public class Worker implements Runnable
	{
		private Thread runner;		
		private final int totalSize;
		private final List<BlockPos2D> coordsToHandle;
		private final CountDownLatch latch;
		private final OTGNoiseChunkGenerator generator;
		private final CommandSource source;
		private final BufferedImage img;
		private final int progressUpdate;
		private final boolean mapBiomes;
		private final int width;
		private final int height;

		public Worker(CountDownLatch latch, CommandSource source, OTGNoiseChunkGenerator generator, BufferedImage img, List<BlockPos2D> coordsToHandle, int totalSize, boolean mapBiomes, int width, int height)
		{
			this.latch = latch;
			this.generator = generator;
			this.source = source;
			this.img = img;
			this.progressUpdate = (int)Math.ceil(totalSize / 100f);
			this.coordsToHandle = coordsToHandle;
			this.totalSize = totalSize;
			this.mapBiomes = mapBiomes;
			this.width = width;
			this.height = height;
		}

		public void start()
		{
			this.runner = new Thread(this);
			this.runner.start();
		}
		
		@Override
		public void run()
		{
			//set the color
			while(true)
			{
				BlockPos2D coords = null;
				int sizeLeft;
				synchronized(queueLock)
				{
					sizeLeft = this.coordsToHandle.size();
					if(sizeLeft > 0)
					{
						coords = this.coordsToHandle.remove(sizeLeft - 1);
					}
				}
				// Send a progress update to let people know the server isn't dying
				if (sizeLeft % this.progressUpdate == 0)
				{
					this.source.sendSuccess(new StringTextComponent((int)Math.floor(100 - (((double)sizeLeft / this.totalSize) * 100)) + "% Done mapping"), true);
				}
				
				if(coords != null)
				{
					if(this.mapBiomes)
					{
						getBiomePixel(coords);
					} else {
						getTerrainPixel(coords);
					}
				} else {
					this.latch.countDown();
					return;
				}
			}
		}

		private void getBiomePixel(BlockPos2D chunkCoords)
		{
			// mapBiomes uses biome coords, so 1 pixel for every 
			// 4 blocks, not 1 pixel per block like mapTerrain.
			for (int internalX = 0; internalX < Constants.CHUNK_SIZE; internalX++)
			{
				for (int internalZ = 0; internalZ < Constants.CHUNK_SIZE; internalZ++)
				{
					int noiseX = chunkCoords.x * Constants.CHUNK_SIZE + internalX;
					int noiseZ = chunkCoords.z * Constants.CHUNK_SIZE + internalZ;
					if(noiseX < this.width && noiseZ < this.height)
					{
						// TODO: Fetch biome data per chunk, not per column, probably very slow atm.
						int biomeColor = this.generator.getCachedBiomeProvider().getNoiseBiomeConfig(noiseX, noiseZ, true).getBiomeColor();
						synchronized(imgLock)
						{
							this.img.setRGB(noiseX, noiseZ, biomeColor);
						}
					}
				}
			}
		}
		
		private void getTerrainPixel(BlockPos2D chunkCoords)
		{
			ForgeChunkBuffer chunk = this.generator.getChunkWithoutLoadingOrCaching(this.source.getLevel().getRandom(), ChunkCoordinate.fromChunkCoords(chunkCoords.x, chunkCoords.z));
			HighestBlockInfo highestBlockInfo;
			for (int internalX = 0; internalX < 16; internalX++)
			{
				for (int internalZ = 0; internalZ < 16; internalZ++)
				{
					int x = chunkCoords.x * 16 + internalX;
					int z = chunkCoords.z * 16 + internalZ;
					if(x < this.width && z < this.height)
					{
						highestBlockInfo = getHighestBlockInfoInUnloadedChunk(chunk, internalX, internalZ);

						// Color depth relative to waterlevel
						//int worldHeight = 255;
						//int worldWaterLevel = 63;
						//int min = worldWaterLevel - worldHeight;
						//int max = worldWaterLevel + worldHeight;
						// Color depth relative to 0-255
						int min = 0;
						int max = 255;
						int range = max - min;
						int distance = -min + highestBlockInfo.y;
						float relativeDistance = (float)distance / (float)range;
						int shadePercentage = (int)Math.floor(relativeDistance * 2 * 100);
						int rgbColor = shadeColor(highestBlockInfo.material.internalBlock().getBlock().defaultMaterialColor().col, shadePercentage);
						synchronized(imgLock)
						{
							this.img.setRGB(x, z, rgbColor);
						}
					}
				}
			}			
		}

		private HighestBlockInfo getHighestBlockInfoInUnloadedChunk(ForgeChunkBuffer chunk, int internalX, int internalZ)
		{
			// TODO: Just use heightmaps?
			BlockState blockInChunk;
			for (int y = chunk.getHighestBlockForColumn(internalX, internalZ); y >= 0; y--)
			{
				blockInChunk = chunk.getChunk().getBlockState(new BlockPos(internalX, y, internalZ));
				if (blockInChunk != null && blockInChunk.getBlock() != Blocks.AIR)
				{
					return new HighestBlockInfo((ForgeMaterialData)ForgeMaterialData.ofBlockState(blockInChunk), y);					
				}
			}
			return new HighestBlockInfo((ForgeMaterialData)LocalMaterials.AIR, 63);
		}
	}
	
	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(MAP_TYPES, builder);
	}
		
	public class HighestBlockInfo
	{
		public final ForgeMaterialData material;
		public final int y;
		
		public HighestBlockInfo(ForgeMaterialData material, int y)
		{
			this.material = material;
			this.y = y;
		}
	}
}
