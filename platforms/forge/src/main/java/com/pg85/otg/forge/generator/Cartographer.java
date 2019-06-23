package com.pg85.otg.forge.generator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.BlockSkull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.NamedBinaryTag;
import com.pg85.otg.util.NamedBinaryTag.Type;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGTeleporter;

public class Cartographer
{
	// TODO: Use instance instead of static methods

	public static int CartographerDimension = 0;

	static long lastUpdateTime = System.currentTimeMillis();
	public static void UpdateWorldMap()
	{
		WorldServer worldServer = DimensionManager.getWorld(0);
		if(((ForgeEngine)OTG.getEngine()).getCartographerEnabled() && worldServer.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator"))
		{
			if(System.currentTimeMillis() - lastUpdateTime > 1000) // Once per second
			{
				destinationCoordinateCache = null;

				ForgeWorld world = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(worldServer);
				ForgeWorld cartographerWorld = (ForgeWorld)OTG.getWorld("DIM-Cartographer");
	    		if(cartographerWorld == null)
	    		{
	    			OTGDimensionManager.initDimension(Cartographer.CartographerDimension);
	    			cartographerWorld = (ForgeWorld)OTG.getWorld("DIM-Cartographer");
	    		}
	    		if(cartographerWorld == null)
	    		{
	    			throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
	    		}

				if(world == null || cartographerWorld == null || cartographerWorld == world)
				{
					return; // May be unloading / shutting down
				}

				lastUpdateTime = System.currentTimeMillis();

		    	Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = getPortals();
				for(Teleporter.PortalPosition portalPos : destinationCoordinateCache.values())
				{
					// Prevent the portal from being unloaded from the list of portals.
					// TODO: Make sure this doesn't cause problems, portal locations will never be unloaded
					// TODO: Save TC portals seperately from normal portals so they don't get unloaded and aren't seen as nether portals by MC.
					portalPos.lastUpdateTime = worldServer.getTotalWorldTime();
				}

				BlockPos spawnPoint = world.getSpawnPoint();
		    	BlockPos cartographerSpawnPoint = cartographerWorld.getSpawnPoint();
		    	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());

		    	HashMap<ChunkCoordinate,ArrayList<Entity>> entitiesPerBlock = new HashMap<ChunkCoordinate, ArrayList<Entity>>();
		    	for(Entity entity : cartographerWorld.getWorld().getEntities(Entity.class, EntitySelectors.NOT_SPECTATING))
		    	{
		    		if(
	    				//entity.dimension == 0 &&
	    				!(entity instanceof EntityEnderPearl) &&
	    				(
							entity instanceof EntityAreaEffectCloud ||
		    				entity instanceof EntityItem ||
		    				entity instanceof EntityLiving ||
		    				entity instanceof EntityArrow ||
		    				entity instanceof EntityFireball ||
		    				entity instanceof EntityBoat ||
		    				entity instanceof EntityMinecart ||
		    				entity instanceof EntityFireworkRocket ||
		    				entity instanceof EntityThrowable ||
		    				entity instanceof EntityTNTPrimed ||
		    				entity instanceof EntityShulkerBullet ||
		    				entity instanceof EntityXPOrb
	    				)
					)
		    		{
			    		ChunkCoordinate chunkCoord1 = ChunkCoordinate.fromChunkCoords(entity.getPosition().getX() - cartographerSpawnPoint.getX(), entity.getPosition().getZ() - cartographerSpawnPoint.getZ());

			    		if(entitiesPerBlock.containsKey(chunkCoord1))
			    		{
			    			entitiesPerBlock.get(chunkCoord1).add(entity);
			    		} else {
			    			ArrayList<Entity> items = new ArrayList<Entity>();
			    			items.add(entity);
			    			entitiesPerBlock.put(chunkCoord1, items);
			    		}
		    		}
		    	}

				ArrayList<ChunkCoordinate> chunksDone = new ArrayList<ChunkCoordinate>();

				// Check all chunks around spawn for items to teleport
		        int minDist = world.getWorld().getMinecraftServer().getPlayerList().getViewDistance();
				for(int x = -minDist; x < minDist; x++)
				{
					for(int z = -minDist; z < minDist; z++)
					{
						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(x, z);
						if(!chunksDone.contains(chunkCoord))
						{
							ArrayList<Entity> entities = entitiesPerBlock.get(chunkCoord);
			        		chunksDone.add(chunkCoord);
			        		if(entities != null)
			        		{
		        				TeleportEntityFromMap(cartographerWorld, entities, true);
			        		}
						}
					}
				}

				// Check all chunks around portals for items to teleport
				minDist = 2; // Radius around portals can be tp'd to even when no player is near
				for(Teleporter.PortalPosition portalPos : destinationCoordinateCache.values())
				{
					ChunkCoordinate portalChunk = ChunkCoordinate.fromBlockCoords(portalPos.getX() - spawnPoint.getX(), portalPos.getZ() - spawnPoint.getZ());

					for(int x = portalChunk.getChunkX() - minDist; x < portalChunk.getChunkX() + minDist; x++)
					{
						for(int z = portalChunk.getChunkZ() - minDist; z < portalChunk.getChunkZ() + minDist; z++)
						{
							ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(x, z);
							if(!chunksDone.contains(chunkCoord))
							{
								ArrayList<Entity> entities = entitiesPerBlock.get(chunkCoord);
				        		chunksDone.add(chunkCoord);
				        		if(entities != null)
				        		{
			        				TeleportEntityFromMap(cartographerWorld, entities, true);
				        		}
							}
						}
					}
				}

				// Update all chunks around players
				//destinationCoordinateCache = getPortals();
				int viewDist = world.getWorld().getMinecraftServer().getPlayerList().getViewDistance();
				ArrayList<ChunkCoordinate> chunksDonePlayers = new ArrayList<ChunkCoordinate>();
		    	for(EntityPlayer player : world.getWorld().playerEntities)
		    	{
		    		if(player.dimension == 0)
		    		{
		    			ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)Math.floor(player.posX) - spawnPoint.getX(), (int)Math.floor(player.posZ) - spawnPoint.getZ());

						for(int x = playerChunk.getChunkX() - viewDist; x < playerChunk.getChunkX() + viewDist; x++)
						{
							for(int z = playerChunk.getChunkZ() - viewDist; z < playerChunk.getChunkZ() + viewDist; z++)
							{
								ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(x, z);
			        			if(!chunksDonePlayers.contains(chunkCoord))
			        			{
									if(!chunksDone.contains(chunkCoord))
									{
										ArrayList<Entity> entities = entitiesPerBlock.get(chunkCoord);
						        		chunksDone.add(chunkCoord);
						        		if(entities != null)
						        		{
					        				TeleportEntityFromMap(cartographerWorld, entities, true);
						        		}
									}
			        				chunksDonePlayers.add(chunkCoord);
									ChunkCoordinate realChunkCoord = ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + spawnChunk.getChunkX(), chunkCoord.getChunkZ() + spawnChunk.getChunkZ());
			        				if(
		        						worldServer.getChunkProvider().chunkExists(realChunkCoord.getChunkX(), realChunkCoord.getChunkZ()) &&
		        						worldServer.getChunkProvider().provideChunk(realChunkCoord.getChunkX(), realChunkCoord.getChunkZ()).isTerrainPopulated()
	        						)
			        				{
			        					CreateBlockWorldMapAtSpawn(realChunkCoord, false);
			        				}
			        			}
							}
						}
		    		}
		    	}
		    	destinationCoordinateCache = null;
			}
		}
	}

	public static boolean TeleportPlayerFromMap(EntityPlayer player)
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		entities.add(player);

		ForgeWorld cartographerWorld = (ForgeWorld)OTG.getWorld("DIM-Cartographer");
		if(cartographerWorld == null)
		{
			OTGDimensionManager.initDimension(Cartographer.CartographerDimension);
			cartographerWorld = (ForgeWorld)OTG.getWorld("DIM-Cartographer");
		}
		if(cartographerWorld == null)
		{
			throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
		}

		return TeleportEntityFromMap(cartographerWorld, entities, false);
	}

	static Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = null;
	private static Long2ObjectMap<Teleporter.PortalPosition> getPortals()
	{
		if(destinationCoordinateCache == null)
		{
	    	MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
	    	WorldServer worldserver1 = mcServer.getWorld(0);
			try {
				Field[] fields = worldserver1.getDefaultTeleporter().getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(Long2ObjectMap.class))
					{
						field.setAccessible(true);
						destinationCoordinateCache = (Long2ObjectMap<Teleporter.PortalPosition>) field.get(worldserver1.getDefaultTeleporter());
				        break;
					}
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return destinationCoordinateCache;
	}

	private static boolean TeleportEntityFromMap(ForgeWorld cartographerworld, ArrayList<Entity> entities, boolean excludePlayers)
	{
		Entity entity = entities.get(0);
		// Check existing portals, players and spawn

		WorldServer overWorldServer = DimensionManager.getWorld(0);
		if(!(overWorldServer.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator")))
		{
			return false;
		}
		ForgeWorld overWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(overWorldServer);

		BlockPos spawnPoint = overWorld.getSpawnPoint();
		ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
		BlockPos cartographerSpawnPoint = cartographerworld.getSpawnPoint();

		ChunkCoordinate destinationChunk = ChunkCoordinate.fromChunkCoords((int)Math.floor(entity.posX) - cartographerSpawnPoint.getX() + spawnChunk.getChunkX(), (int)Math.floor(entity.posZ) - cartographerSpawnPoint.getZ() + spawnChunk.getChunkZ());
    	int newX = destinationChunk.getBlockXCenter();
    	int newZ = destinationChunk.getBlockZCenter();

        // Check if destination is close enough to overworld spawn point to be able to teleport
		int minDistSq = overWorld.getWorld().getMinecraftServer().getPlayerList().getViewDistance() * overWorld.getWorld().getMinecraftServer().getPlayerList().getViewDistance();
        int xDiff = (int) (destinationChunk.getChunkX() - spawnChunk.getChunkX());
        int zDiff = (int) (destinationChunk.getChunkZ() - spawnChunk.getChunkZ());
        float distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

        if(distInChunksSq <= minDistSq)
        {
    		for(Entity entityToTeleport : entities)
    		{
    			if(!((entityToTeleport instanceof EntityPlayer) && !entityToTeleport.velocityChanged) || (entityToTeleport instanceof EntityPlayer && !excludePlayers))
    			{
    	        	int newY = overWorld.getHighestBlockYAt(newX, newZ);
    				entity.setPositionAndUpdate(newX, newY, newZ);
    				if(entity instanceof EntityPlayerMP)
    				{
    					OTGTeleporter.changeDimension(0, (EntityPlayerMP)entity);
					} else {
						OTGTeleporter.changeDimension(0, entity);
    				}
    			}
    		}
			return true;
        }

        // Check if destination is close enough to a portal to be able to teleport
		boolean portalInRange = false;
		boolean portalInChunk = false;

		Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = getPortals();

		minDistSq = 2 * 2; // Radius around portals can be tp'd to even when no player is near
		for(Teleporter.PortalPosition portalPos : destinationCoordinateCache.values())
		{
			ChunkCoordinate portalChunk = ChunkCoordinate.fromBlockCoords(portalPos.getX(), portalPos.getZ());

            xDiff = destinationChunk.getChunkX() - portalChunk.getChunkX();
            zDiff = destinationChunk.getChunkZ() - portalChunk.getChunkZ();
            distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

            if(portalInRange || distInChunksSq <= minDistSq)
            {
            	portalInRange = true;
            	break;
            }

    		if(portalChunk.equals(destinationChunk))
    		{
    			portalInChunk = true;
    			break;
    		}
		}

		if(portalInRange || portalInChunk)
		{
	    	int newY = overWorld.getHighestBlockYAt(newX, newZ);
			entity.setPositionAndUpdate(newX, newY, newZ);
			if(entity instanceof EntityPlayerMP)
			{
				OTGTeleporter.changeDimension(0, (EntityPlayerMP)entity);
			} else {
				OTGTeleporter.changeDimension(0, entity);
			}
			return true;
		}

    	minDistSq = overWorld.getWorld().getMinecraftServer().getPlayerList().getViewDistance() * overWorld.getWorld().getMinecraftServer().getPlayerList().getViewDistance();
    	boolean playerInRange = false;
    	ArrayList<EntityPlayer> playersInChunk = new ArrayList<EntityPlayer>();
    	for(EntityPlayer player : overWorld.getWorld().playerEntities)
    	{
    		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)player.posX, (int)player.posZ);

            xDiff = destinationChunk.getChunkX() - playerChunk.getChunkX();
            zDiff = destinationChunk.getChunkZ() - playerChunk.getChunkZ();
            distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

            if(playerInRange || distInChunksSq <= minDistSq)
            {
            	playerInRange = true;
            	break;
            }

    		if(playerChunk.equals(destinationChunk))
    		{
    	    	playersInChunk.add(player);
    	    	break;
    		}
    	}

		if(portalInRange || portalInChunk)
		{
	    	int newY = overWorld.getHighestBlockYAt(newX, newZ);
			entity.setPositionAndUpdate(newX, newY, newZ);
			if(entity instanceof EntityPlayerMP)
			{
				OTGTeleporter.changeDimension(0, (EntityPlayerMP)entity);
			} else {
				OTGTeleporter.changeDimension(0, entity);
			}
			return true;
		}
		return false;
	}

	/**
	 * Spawns a miniature of the known world at 1/16 scale (so one block per chunk) at spawn, made of
	 * colored clay, wool and glass. Work in progress, will add more features for spawning location,
	 * updating blocks, console commands etc.
	 */
    public static void CreateBlockWorldMapAtSpawn(ChunkCoordinate chunkCoord, boolean unloading)
    {
		WorldServer worldServer = DimensionManager.getWorld(0);
		if(worldServer.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator"))
		{
			ForgeWorld world = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(worldServer);

			ForgeWorld cartographerWorld = (ForgeWorld) OTG.getWorld("DIM-Cartographer");
    		if(cartographerWorld == null)
    		{
    			OTGDimensionManager.initDimension(Cartographer.CartographerDimension);
    			cartographerWorld = (ForgeWorld)OTG.getWorld("DIM-Cartographer");
    		}
    		if(cartographerWorld == null)
    		{
    			throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
    		}

	    	if(world == null || cartographerWorld == null || world == cartographerWorld)
	    	{
	    		return; // May be unloading / shutting down ? TODO: Is this really acceptable? Load worlds?
	    	}

	    	// NOTE: Don't use Glowstone, it's laggy!

	    	DefaultMaterial replaceByMaterial = DefaultMaterial.STAINED_CLAY; // Glass = 95, Clay = 159, Wool = 35

	    	// One chunk in the overworld is one block in the Cartographer dimension, calculate x and z.
	    	BlockPos spawnPoint =  world.getSpawnPoint();
	    	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
	    	BlockPos spawnPointCartographer =  cartographerWorld.getSpawnPoint();

	    	int newX = spawnPointCartographer.getX() + (chunkCoord.getChunkX() - spawnChunk.getChunkX());
	    	int newZ = spawnPointCartographer.getZ() + (chunkCoord.getChunkZ() - spawnChunk.getChunkZ());

	    	// Draw a map of the world at spawn using blocks
	    	int highestBlockY = world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());
	    	LocalMaterialData material = null;
	    	LocalMaterialData topMaterial = null;

	    	// Apply 1/16 to height and get Y, take into account height of spawn location
	    	int baseHeight = spawnPoint.getY();

	    	int minY = (baseHeight - (int) Math.floor(spawnPoint.getY() / 16d));
	    	int maxY = (baseHeight + (int) Math.floor((255 - spawnPoint.getY()) / 16d));

	    	// Find the appropriate blocks to place on the map for the given material.
	    	// If a block cannot be found (because the material is unknown or has no replace-to block)
	    	// try lower blocks until a block is found or the world depth is reached.
	    	while(material == null && highestBlockY >= 0)
	    	{
	    		//if(highestBlockY < minY || highestBlockY > maxY) // Don't count the Cartographer itself
	    		{
		    		LocalMaterialData materialToReplace = world.getMaterial(chunkCoord.getBlockXCenter(), highestBlockY, chunkCoord.getBlockZCenter(), true);

		    		// For trees/water/lava/ice/fire/wood the top block is wool or glass, for everything else it's clay
		        	DefaultMaterial replaceByMaterialTop = replaceByMaterial;
		        	if(materialToReplace != null)
		        	{
		    	    	DefaultMaterial defaultMaterialToReplace = materialToReplace.toDefaultMaterial();
		    	    	if(
		    				defaultMaterialToReplace.equals(DefaultMaterial.WATER) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.STATIONARY_WATER) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.LAVA) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.STATIONARY_LAVA) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.MAGMA) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.ICE) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.FIRE)
		    			)
		    	    	{
		    	    		replaceByMaterialTop = DefaultMaterial.STAINED_GLASS;
		    	    	}
		    	    	if(
		    				defaultMaterialToReplace.equals(DefaultMaterial.LEAVES) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.LEAVES_2) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.LOG) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.LOG_2) ||
		    	    		defaultMaterialToReplace.equals(DefaultMaterial.WOOD) ||
		    				defaultMaterialToReplace.equals(DefaultMaterial.WOOD_DOUBLE_STEP) ||
							defaultMaterialToReplace.equals(DefaultMaterial.WOOD_STEP) ||
							defaultMaterialToReplace.equals(DefaultMaterial.SPRUCE_WOOD_STAIRS) ||
							defaultMaterialToReplace.equals(DefaultMaterial.BIRCH_WOOD_STAIRS) ||
							defaultMaterialToReplace.equals(DefaultMaterial.JUNGLE_WOOD_STAIRS) ||
							defaultMaterialToReplace.equals(DefaultMaterial.ACACIA_STAIRS) ||
							defaultMaterialToReplace.equals(DefaultMaterial.DARK_OAK_STAIRS)
		    			)
		    	    	{
		    	    		replaceByMaterialTop = DefaultMaterial.WOOL;
		    	    	}
		        	}

		    		topMaterial = GetReplaceByMaterial(materialToReplace, replaceByMaterialTop);
		    		material = GetReplaceByMaterial(materialToReplace, replaceByMaterial);
		    		if(material != null)
		    		{
			    		if(topMaterial == null)
			    		{
			    			topMaterial = GetReplaceByMaterial(materialToReplace, replaceByMaterialTop);
			    		}
			    		break;
		    		}
	    		}
	    		highestBlockY--;
	    	}

	    	// Couldn't find a block, use black.
	    	if(highestBlockY < 0)
	    	{
	    		highestBlockY = world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter()) - 1;
	    		material = OTG.toLocalMaterialData(replaceByMaterial, 15);
	    	}

	    	int heightDiff = (int) Math.floor(((highestBlockY + 1) - spawnPoint.getY()) / 16d);


	    	int viewDistSq = world.getWorld().getMinecraftServer().getPlayerList().getViewDistance() * world.getWorld().getMinecraftServer().getPlayerList().getViewDistance();

	        int xDiff = spawnChunk.getChunkX() - chunkCoord.getChunkX();
	        int zDiff = spawnChunk.getChunkZ() - chunkCoord.getChunkZ();
	        float distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

			boolean spawnInRange = distInChunksSq <= viewDistSq;
			boolean portalInRange = false;
			boolean portalInChunk = false;

	    	Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = getPortals();

			int minDistSq = 2 * 2; // Radius around portals can be tp'd to even when no player is near
			for(Teleporter.PortalPosition portalPos : destinationCoordinateCache.values())
			{
				ChunkCoordinate portalChunk = ChunkCoordinate.fromBlockCoords(portalPos.getX(), portalPos.getZ());

	            xDiff = portalChunk.getChunkX() - chunkCoord.getChunkX();
	            zDiff = portalChunk.getChunkZ() - chunkCoord.getChunkZ();
	            distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

	            if(portalInRange || distInChunksSq <= minDistSq)
	            {
	            	portalInRange = true;
	            }

	    		if(portalChunk.equals(chunkCoord))
	    		{
	    			portalInChunk = true;
	    			break;
	    		}
			}

	    	boolean playerInRange = false;
	    	ArrayList<EntityPlayer> playersInChunk = new ArrayList<EntityPlayer>();
	    	for(EntityPlayer player : world.getWorld().playerEntities)
	    	{
	    		if(player.dimension == 0)
	    		{
		    		ChunkCoordinate playerChunk = ChunkCoordinate.fromChunkCoords(player.chunkCoordX, player.chunkCoordZ);

		            xDiff = playerChunk.getChunkX() - chunkCoord.getChunkX();
		            zDiff = playerChunk.getChunkZ() - chunkCoord.getChunkZ();
		            distInChunksSq = (xDiff * xDiff + zDiff * zDiff);

		            if(playerInRange || distInChunksSq <= viewDistSq)
		            {
		            	playerInRange = true;
		            }

		    		if(playerChunk.equals(chunkCoord))
		    		{
		    	    	playersInChunk.add(player);
		    		}
	    		}
	    	}

	    	// Set top block
	    	int newY = baseHeight + heightDiff - minY;
	    	cartographerWorld.setBlock(newX, newY, newZ, topMaterial, null, true);

	    	// Set blocks above
	    	while(newY < maxY - minY)
	    	{
	    		newY++;
	    		if(playerInRange || portalInRange)
	    		{
		    		if(!cartographerWorld.isNullOrAir(newX, newY, newZ, true))
		    		{
		    			cartographerWorld.setBlock(newX, newY, newZ, OTG.toLocalMaterialData(DefaultMaterial.AIR, 0), null, true);
		    		}
	    		}
	    	}

	    	newY = baseHeight + heightDiff - minY;
	    	// Set lower blocks
	    	while(newY > 0)
	    	{
	    		newY--;
	   			cartographerWorld.setBlock(newX, newY, newZ, material, null, true);
	    	}

	    	// Banner colors
	    	// 0 = Black
	    	// 1 = Brown
	    	// 2 = Green
	    	// 3 = Red
	    	// 4 = Blue
	    	// 5 = Purple
	    	// 6 = Cyan
	    	// 7 = Light Gray
	    	// 8 = Dark Gray
	    	// 9 = Pink
	    	// 10 = Lime
	    	// 11 = Yellow
	    	// 12 = Light Blue
	    	// 13 = Magenta
	    	// 14 = Orange
	    	// 15 = White
	    	newY = baseHeight + heightDiff + 1 - minY;
	    	// Put a banner at spawn
	    	if(chunkCoord.equals(spawnChunk))
	    	{
	            NamedBinaryTag tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_Int, "Base", 15) });
	    		cartographerWorld.setBlock(newX, newY, newZ, OTG.toLocalMaterialData(DefaultMaterial.STANDING_BANNER, 0), tag, true);
	    		//cartographerWorld.setBlock(spawnChunk.getBlockX() + chunkCoord.getChunkX(), newY - 1, spawnChunk.getBlockZ() + chunkCoord.getChunkZ(), OTG.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0));
			}
	    	// Put a banner at each portal
	   		else if(portalInChunk)
			{
	   			NamedBinaryTag tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_Int, "Base", 13) });
	    		cartographerWorld.setBlock(newX, newY, newZ, OTG.toLocalMaterialData(DefaultMaterial.STANDING_BANNER, 0), tag, true);
	    		//cartographerWorld.setBlock(destPosX, newY - 1, destPosZ, OTG.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0));
			}
	    	// Animals TODO: Animal textures on custom player head cause lots of lag, find a solution
	    	/*
	   		else if(!unloading && playerInRange && playersInChunk.size() == 0)
	   		{
		    	ArrayList<EntityLiving> mobsInChunk = new ArrayList<EntityLiving>();
		    	for(EntityLiving mob : world.getWorld().getEntities(EntityLiving.class, EntitySelectors.IS_ALIVE))
		    	{
		    		if(mob.dimension == 0 && (mob instanceof EntityAnimal || mob instanceof EntityVillager || mob instanceof EntityWaterMob))
		    		{
		        		ChunkCoordinate entityChunk = ChunkCoordinate.fromChunkCoords(mob.chunkCoordX, mob.chunkCoordZ);

		        		if(entityChunk.equals(chunkCoord))
		        		{
		        			mobsInChunk.add(mob);
		        			break;
		        		}
		    		}
		    	}
		    	if(mobsInChunk.size() > 0)
		    	{
			    	int playerSignHeightOffset = 1;

		    		LocalMaterialData playerHead = OTG.toLocalMaterialData(DefaultMaterial.SKULL, 3);
		    		//String playerHeadName = mobsInChunk.get(0).getName();

		    		//NamedBinaryTag tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_String, "ExtraType", playerHeadName), new NamedBinaryTag(Type.TAG_String, "SkullOwner", ""), new NamedBinaryTag(Type.TAG_Byte, "SkullType", (byte)3) });

					BlockPos pos = new BlockPos(destPosX, baseHeight + heightDiff + playerSignHeightOffset - minY, spawnChunk.getBlockZCenter() + chunkCoord.getChunkZ());

					cartographerWorld.setBlock(pos.getX(), pos.getY(), pos.getZ(), playerHead);
					//world.attachMetadata(pos.getX(), pos.getY(), pos.getZ(), tag);
					world.getWorld().setBlockState(pos, Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 11);

		            int rotation = (MathHelper.floor_double((double)((mobsInChunk.get(0).rotationYaw - 180) * 16.0F / 360.0F) + 0.5D) & 15);
		            TileEntity tileentity = world.getWorld().getTileEntity(pos);
		            TileEntitySkull tileentityskull = (TileEntitySkull)tileentity;
		            tileentityskull.setSkullRotation(rotation);
		    	}
	    	}
	    	*/
	    	// Fog of war
	   		else if((unloading && !portalInRange && !spawnInRange) || (!playerInRange && !portalInRange && !spawnInRange ))
	    	{
	    		cartographerWorld.setBlock(newX, newY, newZ, OTG.toLocalMaterialData(DefaultMaterial.STAINED_GLASS, 15), null, true);
	    	}

	    	// Place player heads
	   		if(!unloading && playersInChunk.size() > 0)
	    	{
		    	int playerSignHeightOffset = 0;

		    	for(EntityPlayer player : playersInChunk)
		    	{
		    		playerSignHeightOffset++;

		    		newY = baseHeight + heightDiff + playerSignHeightOffset - minY;
		    		if(newY + 2 < maxY - minY && newY >= 0)
		    		{
			    		LocalMaterialData playerHead = OTG.toLocalMaterialData(DefaultMaterial.SKULL, 3);
			    		String playerHeadName = "MHF_Blaze";//player.getName();

			    		NamedBinaryTag tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_String, "SkullOwner", playerHeadName), new NamedBinaryTag(Type.TAG_String, "ExtraType", ""), new NamedBinaryTag(Type.TAG_Byte, "SkullType", (byte)3) });

						BlockPos pos = new BlockPos(newX, newY, newZ);

						cartographerWorld.setBlock(pos.getX(), pos.getY(), pos.getZ(), playerHead, tag, true);
						cartographerWorld.getWorld().setBlockState(pos, Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 11);

		                int rotation = (MathHelper.floor((double)((player.rotationYaw - 180) * 16.0F / 360.0F) + 0.5D) & 15);
		                TileEntity tileentity = cartographerWorld.getWorld().getTileEntity(pos);
		                TileEntitySkull tileentityskull = (TileEntitySkull)tileentity;
		                tileentityskull.setSkullRotation(rotation);
		    		}
		    	}

		    	// Put banner on top of player heads

	            int rotation = (MathHelper.floor((double)(playersInChunk.get(0).rotationYaw * 16.0F / 360.0F) + 0.5D) & 15);
	            NamedBinaryTag tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_Int, "Base", chunkCoord.equals(spawnChunk) ? 15 : portalInChunk ? 13 : 10) });
		    	cartographerWorld.setBlock(newX, baseHeight + heightDiff + playerSignHeightOffset + 1 - minY, newZ, OTG.toLocalMaterialData(DefaultMaterial.STANDING_BANNER, rotation), tag, true);

	    		//playerSignHeightOffset = 2;

		    	//for(int i = 0; i < playersInChunk.size(); i += 4)
		    	//{
					//LocalMaterialData playerSign = OTG.toLocalMaterialData(DefaultMaterial.SIGN_POST, 0);

					//String playerName1 = playersInChunk.get(i).getName();
					//String playerName2 = playersInChunk.size() > i + 1 ? playersInChunk.get(i + 1).getName() : "";
					//String playerName3 = playersInChunk.size() > i + 2 ? playersInChunk.get(i + 2).getName() : "";
					//String playerName4 = playersInChunk.size() > i + 3 ? playersInChunk.get(i + 3).getName() : "";

					//tag = new NamedBinaryTag(Type.TAG_List, "BlockEntityTag", new NamedBinaryTag[] { new NamedBinaryTag(Type.TAG_String, "id", "Sign"), new NamedBinaryTag(Type.TAG_String, "Text1", playerName1), new NamedBinaryTag(Type.TAG_String, "Text2", playerName2), new NamedBinaryTag(Type.TAG_String, "Text3", playerName3), new NamedBinaryTag(Type.TAG_String, "Text4", playerName4) });

					//cartographerWorld.setBlock(destPosX, baseHeight + heightDiff + playerSignHeightOffset - minY, destPosZ, playerSign);
					//cartographerWorld.attachMetadata(destPosX, baseHeight + heightDiff + playerSignHeightOffset - minY, destPosZ, tag);
		    		//playerSignHeightOffset++;
		    	//}
	    	}
		}
    }

    /**
     * Gets the block to place on the map based on the given material.
     */
    private static LocalMaterialData GetReplaceByMaterial(LocalMaterialData materialToReplace, DefaultMaterial replaceByMaterial)
    {
		DefaultMaterial[] TransparentBlocks = {
		};

		DefaultMaterial[] WhiteBlocks = {
    		DefaultMaterial.SNOW,
    		DefaultMaterial.SNOW_BLOCK,
    	    DefaultMaterial.QUARTZ_BLOCK,
    	    DefaultMaterial.QUARTZ_STAIRS,
		};

		DefaultMaterial[] OrangeBlocks = {
			DefaultMaterial.FIRE,
    		DefaultMaterial.SAND,
    	    DefaultMaterial.RED_SANDSTONE,
    	    DefaultMaterial.HARD_CLAY,
    	    DefaultMaterial.RED_SANDSTONE_STAIRS
		};

	    DefaultMaterial[] MagentaBlocks = {
    	    DefaultMaterial.PURPUR_BLOCK,
    	    DefaultMaterial.PURPUR_PILLAR,
    	    DefaultMaterial.PURPUR_STAIRS,
    	    DefaultMaterial.PURPUR_DOUBLE_SLAB,
    	    DefaultMaterial.PURPUR_SLAB
	    };

	    DefaultMaterial[] LightBlueBlocks = {
	    		DefaultMaterial.PACKED_ICE
	    };

		DefaultMaterial[] YellowBlocks = {
    		DefaultMaterial.SAND,
    		DefaultMaterial.SANDSTONE,
    		DefaultMaterial.SANDSTONE_STAIRS
		};

		DefaultMaterial[] LimeBlocks = {
			DefaultMaterial.LEAVES,
			DefaultMaterial.LEAVES_2
		};

		DefaultMaterial[] PinkBlocks = {
    		DefaultMaterial.NETHERRACK,
    		DefaultMaterial.MYCEL
		};

		DefaultMaterial[] GrayBlocks = {
    		DefaultMaterial.COBBLESTONE,
    		DefaultMaterial.COBBLESTONE_STAIRS,
    		DefaultMaterial.MOSSY_COBBLESTONE,
    	    DefaultMaterial.STONE_SLAB2,
    	    DefaultMaterial.DOUBLE_STONE_SLAB2,
    	    DefaultMaterial.STEP,
    		DefaultMaterial.DOUBLE_STEP,
    		DefaultMaterial.BRICK,
    	    DefaultMaterial.BRICK_STAIRS,
    		DefaultMaterial.SMOOTH_BRICK,
    	    DefaultMaterial.SMOOTH_STAIRS,
		};

		DefaultMaterial[] LightGrayBlocks = {
    		DefaultMaterial.STONE,
    		DefaultMaterial.GRAVEL,
    	    DefaultMaterial.CLAY,
    	    DefaultMaterial.EMERALD_ORE,
    	    DefaultMaterial.EMERALD_BLOCK,
    	    DefaultMaterial.REDSTONE_ORE,
    	    DefaultMaterial.GLOWING_REDSTONE_ORE,
    	    DefaultMaterial.DIAMOND_ORE,
    	    DefaultMaterial.DIAMOND_BLOCK,
    		DefaultMaterial.GOLD_ORE,
    	    DefaultMaterial.IRON_ORE,
    	    DefaultMaterial.COAL_ORE,
    	    DefaultMaterial.COAL_BLOCK,
    	    DefaultMaterial.LAPIS_ORE,
    	    DefaultMaterial.LAPIS_BLOCK,
    	    DefaultMaterial.GOLD_BLOCK,
    	    DefaultMaterial.IRON_BLOCK,
    	    DefaultMaterial.REDSTONE_BLOCK,
    	    DefaultMaterial.QUARTZ_ORE,
		};

		DefaultMaterial[] CyanBlocks = {
    		DefaultMaterial.FROSTED_ICE,
    		DefaultMaterial.ICE
		};

		DefaultMaterial[] PurpleBlocks = {
    	    DefaultMaterial.NETHER_BRICK,
    	    DefaultMaterial.NETHER_BRICK_STAIRS
		};

	    DefaultMaterial[] BlueBlocks = {
    	    DefaultMaterial.WATER,
    	    DefaultMaterial.STATIONARY_WATER
	    };

	    DefaultMaterial[] BrownBlocks = {
    		DefaultMaterial.DIRT,
    		DefaultMaterial.WOOD,
    	    DefaultMaterial.WOOD_DOUBLE_STEP,
    	    DefaultMaterial.WOOD_STEP,
    	    DefaultMaterial.SPRUCE_WOOD_STAIRS,
    	    DefaultMaterial.BIRCH_WOOD_STAIRS,
    	    DefaultMaterial.JUNGLE_WOOD_STAIRS,
    	    DefaultMaterial.ACACIA_STAIRS,
    	    DefaultMaterial.DARK_OAK_STAIRS,
    		DefaultMaterial.LOG,
    		DefaultMaterial.LOG_2,
    		DefaultMaterial.SOIL,
    		DefaultMaterial.SOUL_SAND,
    	    DefaultMaterial.HUGE_MUSHROOM_1,
    	    DefaultMaterial.HUGE_MUSHROOM_2
	    };

	    DefaultMaterial[] GreenBlocks = {
    		DefaultMaterial.GRASS,
    		DefaultMaterial.GRASS_PATH,
	    };

		DefaultMaterial[] RedBlocks = {
    	    DefaultMaterial.LAVA,
    	    DefaultMaterial.STATIONARY_LAVA,
    	    DefaultMaterial.MAGMA
		};

	    DefaultMaterial[] BlackBlocks = {
    		DefaultMaterial.BEDROCK,
    		DefaultMaterial.OBSIDIAN
	    };

		DefaultMaterial[] ColoredBlocks = {
    	    DefaultMaterial.STAINED_CLAY
		};

	    // Ignored
	    /*
		DefaultMaterial.GLASS
		DefaultMaterial.WOOL,
		DefaultMaterial.AIR,

	    DefaultMaterial.SPONGE
	    DefaultMaterial.POWERED_RAIL
	    DefaultMaterial.DETECTOR_RAIL
	    DefaultMaterial.PISTON_STICKY_BASE
	    DefaultMaterial.WEB
	    DefaultMaterial.DEAD_BUSH
	    DefaultMaterial.PISTON_BASE
	    DefaultMaterial.BED_BLOCK
	    DefaultMaterial.NOTE_BLOCK
	    DefaultMaterial.DISPENSER
	    DefaultMaterial.PISTON_EXTENSION
	    DefaultMaterial.PISTON_MOVING_PIECE
	    DefaultMaterial.BROWN_MUSHROOM
		DefaultMaterial.RED_ROSE
		DefaultMaterial.YELLOW_FLOWER
		DefaultMaterial.LONG_GRASS
		DefaultMaterial.SAPLING
		DefaultMaterial.RED_MUSHROOM
	    DefaultMaterial.TNT
	    DefaultMaterial.BOOKSHELF
	    DefaultMaterial.TORCH
	    DefaultMaterial.MOB_SPAWNER
	    DefaultMaterial.WOOD_STAIRS
	    DefaultMaterial.CHEST
	    DefaultMaterial.REDSTONE_WIRE
	    DefaultMaterial.WORKBENCH
	    DefaultMaterial.CROPS
	    DefaultMaterial.FURNACE
	    DefaultMaterial.BURNING_FURNACE
	    DefaultMaterial.SIGN_POST
	    DefaultMaterial.WOODEN_DOOR
	    DefaultMaterial.LADDER
	    DefaultMaterial.RAILS
	    DefaultMaterial.WALL_SIGN
	    DefaultMaterial.LEVER
	    DefaultMaterial.STONE_PLATE
	    DefaultMaterial.IRON_DOOR_BLOCK
	    DefaultMaterial.WOOD_PLATE
	    DefaultMaterial.REDSTONE_TORCH_OFF
	    DefaultMaterial.REDSTONE_TORCH_ON
	    DefaultMaterial.STONE_BUTTON
	    DefaultMaterial.CACTUS
	    DefaultMaterial.SUGAR_CANE_BLOCK
	    DefaultMaterial.JUKEBOX
	    DefaultMaterial.FENCE
	    DefaultMaterial.PUMPKIN
	    DefaultMaterial.GLOWSTONE
	    DefaultMaterial.PORTAL
	    DefaultMaterial.JACK_O_LANTERN
	    DefaultMaterial.CAKE_BLOCK
	    DefaultMaterial.DIODE_BLOCK_OFF
	    DefaultMaterial.DIODE_BLOCK_ON
	    DefaultMaterial.STAINED_GLASS
	    DefaultMaterial.TRAP_DOOR
	    DefaultMaterial.MONSTER_EGGS
	    DefaultMaterial.IRON_FENCE
	    DefaultMaterial.THIN_GLASS
	    DefaultMaterial.MELON_BLOCK
	    DefaultMaterial.PUMPKIN_STEM
	    DefaultMaterial.MELON_STEM
	    DefaultMaterial.VINE
	    DefaultMaterial.FENCE_GATE
	    DefaultMaterial.WATER_LILY
	    DefaultMaterial.NETHER_FENCE
	    DefaultMaterial.COCOA
	    DefaultMaterial.NETHER_WARTS
	    DefaultMaterial.ENCHANTMENT_TABLE
	    DefaultMaterial.BREWING_STAND
	    DefaultMaterial.CAULDRON
	    DefaultMaterial.ENDER_PORTAL
	    DefaultMaterial.ENDER_PORTAL_FRAME
	    DefaultMaterial.ENDER_STONE
	    DefaultMaterial.DRAGON_EGG
	    DefaultMaterial.REDSTONE_LAMP_OFF
	    DefaultMaterial.REDSTONE_LAMP_ON
	    DefaultMaterial.ENDER_CHEST
	    DefaultMaterial.TRIPWIRE_HOOK
	    DefaultMaterial.TRIPWIRE
	    DefaultMaterial.COMMAND
	    DefaultMaterial.BEACON
	    DefaultMaterial.COBBLE_WALL
	    DefaultMaterial.FLOWER_POT
	    DefaultMaterial.CARROT
	    DefaultMaterial.POTATO
	    DefaultMaterial.WOOD_BUTTON
	    DefaultMaterial.SKULL
	    DefaultMaterial.ANVIL
	    DefaultMaterial.TRAPPED_CHEST
	    DefaultMaterial.GOLD_PLATE
	    DefaultMaterial.IRON_PLATE
	    DefaultMaterial.REDSTONE_COMPARATOR_OFF
	    DefaultMaterial.REDSTONE_COMPARATOR_ON
	    DefaultMaterial.DAYLIGHT_DETECTOR
	    DefaultMaterial.HOPPER
	    DefaultMaterial.ACTIVATOR_RAIL
	    DefaultMaterial.DROPPER
	    DefaultMaterial.STAINED_GLASS_PANE
	    DefaultMaterial.SLIME_BLOCK
	    DefaultMaterial.BARRIER
	    DefaultMaterial.IRON_TRAPDOOR
	    DefaultMaterial.PRISMARINE
	    DefaultMaterial.SEA_LANTERN
	    DefaultMaterial.HAY_BLOCK(170),
	    DefaultMaterial.CARPET
	    DefaultMaterial.DOUBLE_PLANT
	    DefaultMaterial.STANDING_BANNER
	    DefaultMaterial.WALL_BANNER
	    DefaultMaterial.DAYLIGHT_DETECTOR_INVERTED
	    DefaultMaterial.SPRUCE_FENCE_GATE
	    DefaultMaterial.BIRCH_FENCE_GATE
	    DefaultMaterial.JUNGLE_FENCE_GATE
	    DefaultMaterial.DARK_OAK_FENCE_GATE
	    DefaultMaterial.ACACIA_FENCE_GATE
	    DefaultMaterial.SPRUCE_FENCE
	    DefaultMaterial.BIRCH_FENCE
	    DefaultMaterial.JUNGLE_FENCE
	    DefaultMaterial.DARK_OAK_FENCE
	    DefaultMaterial.ACACIA_FENCE
	    DefaultMaterial.SPRUCE_DOOR
	    DefaultMaterial.BIRCH_DOOR
	    DefaultMaterial.JUNGLE_DOOR
	    DefaultMaterial.ACACIA_DOOR
	    DefaultMaterial.DARK_OAK_DOOR
	    DefaultMaterial.END_ROD
	    DefaultMaterial.CHORUS_PLANT
	    DefaultMaterial.CHORUS_FLOWER
	    DefaultMaterial.END_BRICKS
	    DefaultMaterial.BEETROOT_BLOCK
	    DefaultMaterial.END_GATEWAY
	    DefaultMaterial.COMMAND_REPEATING
	    DefaultMaterial.COMMAND_CHAIN
	    DefaultMaterial.NETHER_WART_BLOCK
	    DefaultMaterial.RED_NETHER_BRICK
	    DefaultMaterial.BONE_BLOCK
	    DefaultMaterial.STRUCTURE_VOID
	    DefaultMaterial.STRUCTURE_BLOCK
	    */

    	for(DefaultMaterial replacematerial : TransparentBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(DefaultMaterial.GLASS, 0);
    		}
    	}
    	for(DefaultMaterial replacematerial : WhiteBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 0);
    		}
    	}
    	for(DefaultMaterial replacematerial : OrangeBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			if(replacematerial.equals(DefaultMaterial.SAND))
    			{
    				if(materialToReplace.getBlockData() != 1) { continue; }
    			}
    			return OTG.toLocalMaterialData(replaceByMaterial, 1);
    		}
    	}
    	for(DefaultMaterial replacematerial : MagentaBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 2);
    		}
    	}
    	for(DefaultMaterial replacematerial : LightBlueBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 3);
    		}
    	}
    	for(DefaultMaterial replacematerial : YellowBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 4);
    		}
    	}
    	for(DefaultMaterial replacematerial : LimeBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			if(replaceByMaterial == DefaultMaterial.WOOL) // Use green glass instead of lime for spawning trees on top of clay
    			{
    				continue;
    			}

    			return OTG.toLocalMaterialData(replaceByMaterial, 5);
    		}
    	}
    	for(DefaultMaterial replacematerial : PinkBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 6);
    		}
    	}
    	for(DefaultMaterial replacematerial : GrayBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 7);
    		}
    	}
    	for(DefaultMaterial replacematerial : LightGrayBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 8);
    		}
    	}
    	for(DefaultMaterial replacematerial : CyanBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 9);
    		}
    	}
    	for(DefaultMaterial replacematerial : PurpleBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 10);
    		}
    	}
    	for(DefaultMaterial replacematerial : BlueBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 11);
    		}
    	}
    	for(DefaultMaterial replacematerial : BrownBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 12);
    		}
    	}
    	for(DefaultMaterial replacematerial : GreenBlocks)
    	{
    		if(
				replacematerial.equals(materialToReplace.toDefaultMaterial()) ||
    			(replaceByMaterial == DefaultMaterial.WOOL && (materialToReplace.toDefaultMaterial().equals(DefaultMaterial.LEAVES) || materialToReplace.toDefaultMaterial().equals(DefaultMaterial.LEAVES_2))) // Use green glass instead of lime for spawning trees on top of clay
			)
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 13);
    		}
    	}
    	for(DefaultMaterial replacematerial : RedBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 14);
    		}
    	}
    	for(DefaultMaterial replacematerial : BlackBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, 15);
    		}
    	}
    	for(DefaultMaterial replacematerial : ColoredBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			return OTG.toLocalMaterialData(replaceByMaterial, materialToReplace.getBlockData());
    		}
    	}

    	return null;
    }

	public static void CreateCartographerDimension()
	{
    	if(((ForgeEngine)OTG.getEngine()).getCartographerEnabled() && !OTGDimensionManager.isDimensionNameRegistered("DIM-Cartographer"))
    	{
    		CartographerDimension = OTGDimensionManager.createDimension("DIM-Cartographer", false, true, false);

    		ForgeWorld world = (ForgeWorld) OTG.getWorld("DIM-Cartographer");

			world.getWorld().getGameRules().setOrCreateGameRule("doEntityDrops", "false");
			world.getWorld().getGameRules().setOrCreateGameRule("doFireTick", "false");
			world.getWorld().getGameRules().setOrCreateGameRule("doMobSpawning", "false");
			world.getWorld().getGameRules().setOrCreateGameRule("doTileDrops", "false");
			world.getWorld().getGameRules().setOrCreateGameRule("keepInventory", "true");
			world.getWorld().getGameRules().setOrCreateGameRule("randomTickSpeed", "0");
			world.getWorld().getGameRules().setOrCreateGameRule("spawnRadius", "0");
			world.getWorld().getGameRules().setOrCreateGameRule("doWeatherCycle", "false");
			world.getWorld().getGameRules().setOrCreateGameRule("falldamage", "false");
    	}
	}
}
