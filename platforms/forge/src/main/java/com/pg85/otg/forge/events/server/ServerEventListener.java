package com.pg85.otg.forge.events.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.customobjects.bo3.bo3function.EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.SpawnerFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.generator.Cartographer;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.forge.util.WorldHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

public class ServerEventListener
{	
    /** The 17x17 area around the player where mobs and particles can spawn */
    private HashMap<ChunkCoordinate, Boolean> eligibleChunksForSpawning = new HashMap<ChunkCoordinate, Boolean>();

    int currentTimeInSeconds = 0;
    int lastSpawnedTimeInSeconds = 0;

    /**
     * adds all chunks within the spawn radius of the players to eligibleChunksForSpawning. pars: the world,
     * hostileCreatures, passiveCreatures. returns number of eligible chunks.
     */
    public void findChunksForSpawning(ForgeWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs)
    {
    	WorldServer worldServer = (WorldServer)world.getWorld();

        if (!spawnHostileMobs && !spawnPeacefulMobs) // TODO: Spawn particles even when mobs are disabled?
        {
            return;
        } else {

            this.eligibleChunksForSpawning.clear();
            for (int i = 0; i < worldServer.playerEntities.size(); ++i)
            {
                EntityPlayer entityplayer = (EntityPlayer)worldServer.playerEntities.get(i);
                int playerPosChunkX = MathHelper.floor(entityplayer.posX / 16.0D);
                int playerPosChunkZ = MathHelper.floor(entityplayer.posZ / 16.0D);
                byte radius = 8;

                for (int l = -radius; l <= radius; ++l)
                {
                    for (int i1 = -radius; i1 <= radius; ++i1)
                    {
                        boolean flag3 = l == -radius || l == radius || i1 == -radius || i1 == radius;
                        ChunkCoordinate chunkcoordintpair = ChunkCoordinate.fromChunkCoords(l + playerPosChunkX, i1 + playerPosChunkZ);

                        if (!flag3) // If this chunk is not on the edge of the search area (?)
                        {
                            this.eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(false));
                        }
                    }
                }
            }

    		Stack<Object[]> playerCoords = new Stack<Object[]>();
    		for (int a = 0; a < worldServer.playerEntities.size(); ++a)
    		{
    			EntityPlayer player = (EntityPlayer)worldServer.playerEntities.get(a);
    			playerCoords.add(new Object[] { player.posX, player.posY, player.posZ, player });
    		}

			double maxDistToClosestPlayer = 33.0d;
			double maxDistToClosestPlayerSq = maxDistToClosestPlayer * maxDistToClosestPlayer;

	        for (int x = 0; x < worldServer.loadedEntityList.size(); x++)
	        {
	        	Entity entity = ((Entity)worldServer.loadedEntityList.get(x));
	            NBTTagCompound entityData = entity.getEntityData();

	            if(entityData.hasKey("OTG"))
	            {
	            	// Don't de-spawn if there is a player near the entity
        			boolean playerInRange = false;
        			for (int a = 0; a < playerCoords.size(); ++a)
                    {
        		        float f = (float)((Double)playerCoords.get(a)[0] - entity.posX);
        		        float f1 = (float)((Double)playerCoords.get(a)[1] - entity.posY);
        		        float f2 = (float)((Double)playerCoords.get(a)[2] - entity.posZ);
        		        double distance = f * f + f1 * f1 + f2 * f2;

                    	if(distance < maxDistToClosestPlayerSq)
                    	{
                    		playerInRange = true;
                    		break;
                    	}
                    }
        			if(playerInRange)
        			{
        				continue;
        			}

	            	int despawnTimer = entityData.getInteger("OTGDT"); // OTG Despawn Timer
	            	if(despawnTimer <= 1)
	            	{
	            		// Time's up, despawn
	            		entity.setDead();

	            	} else {
	            		entityData.setInteger("OTGDT", despawnTimer - 1); // OTG Despawn Timer
	            	}
	            }
	        }

            Random random = new Random();

            // OTG mob spawning
    		Stack<SpawnerFunction> spawnerDataSortedByDistance = new Stack<SpawnerFunction>();
    		Stack<Object[]> spawnerDatasWithDistance = new Stack<Object[]>();

            for (ChunkCoordinate chunkcoordintpair1 : this.eligibleChunksForSpawning.keySet())
            {
                if (!this.eligibleChunksForSpawning.get(chunkcoordintpair1))
                {
                	ArrayList<SpawnerFunction> spawnerDataForOTG = world.GetWorldSession().getSpawnersForChunk(ChunkCoordinate.fromChunkCoords(chunkcoordintpair1.getChunkX(), chunkcoordintpair1.getChunkZ()));

                	if(spawnerDataForOTG != null && spawnerDataForOTG.size() > 0)
                	{
                		for(SpawnerFunction spawnerData : spawnerDataForOTG)
                		{
                			double distToClosestPlayer = maxDistToClosestPlayerSq;

                			for (int a = 0; a < playerCoords.size(); ++a)
                            {
                		        float f = (float)((Double)playerCoords.get(a)[0] - spawnerData.x);
                		        float f1 = (float)((Double)playerCoords.get(a)[1] - spawnerData.y);
                		        float f2 = (float)((Double)playerCoords.get(a)[2] - spawnerData.z);
                		        double distance = f * f + f1 * f1 + f2 * f2;

                            	if(distance < distToClosestPlayer)
                            	{
                            		distToClosestPlayer = distance;
                            	}
                            }

                            if(distToClosestPlayer > 0 && distToClosestPlayer < maxDistToClosestPlayerSq)
                            {
		                    	if(spawnerDatasWithDistance.size() == 0)
		                    	{
		                    		spawnerDatasWithDistance.add(new Object[] { distToClosestPlayer, spawnerData });
		                    	} else {
    		                    	for(int r = 0; r < spawnerDatasWithDistance.size(); r++)
    		                    	{
    		                    		if(distToClosestPlayer < (Double)spawnerDatasWithDistance.get(r)[0])
    		                    		{
    		                    			spawnerDatasWithDistance.add(r, new Object[] { distToClosestPlayer, spawnerData });
    		                    			break;
    		                    		}
    		                    		else if(r == spawnerDatasWithDistance.size() - 1)
    		                    		{
    		                    			spawnerDatasWithDistance.add(new Object[] { distToClosestPlayer, spawnerData });
    		                    			break;
    		                    		}
    		                    	}
		                    	}
                            }
                		}
                	}
                }
            }

            for(Object[] spawnerDataWithDistance : spawnerDatasWithDistance)
            {
            	spawnerDataSortedByDistance.add((SpawnerFunction)spawnerDataWithDistance[1]);
            }

    		for(SpawnerFunction spawnerData : spawnerDataSortedByDistance)
        	{
				String mobTypeName = spawnerData.mobName;
				int groupSize = spawnerData.groupSize;
				int interval = spawnerData.interval;

				int spawnChance = spawnerData.spawnChance;
				int max = spawnerData.maxCount;
				int despawnTime = spawnerData.despawnTime;
				int rngRoll = random.nextInt(100);

				if((currentTimeInSeconds - spawnerData.intervalOffset) % interval == 0 || spawnerData.firstSpawn)// && spawnChance > rngRoll)
				{
					if(spawnerData.firstSpawn)
					{
						spawnerData.intervalOffset = currentTimeInSeconds;
					}
					spawnerData.firstSpawn = false;

                    Class<? extends Entity> entityClass = MobSpawnGroupHelper.toMinecraftClass(mobTypeName);                    
                    if(entityClass == null)
                    {
                    	if(OTG.getPluginConfig().SpawnLog)
                    	{
                    		OTG.log(LogMarker.WARN, "Could not find entity: " + mobTypeName);
                    	}
                    	continue;
                    }

                    ResourceLocation resourceLocation = MobSpawnGroupHelper.resourceLocationFromMinecraftClass(entityClass);
                    
                    Entity entity = null;
                    NBTTagCompound nbttagcompound = null;

                    if(spawnerData.getMetaData() == null)
                    {
                        try
                        {
                            entity = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { worldServer });
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                            continue;
                        }
                    } else {
                        nbttagcompound = new NBTTagCompound();

                        try
                        {
                            NBTBase nbtbase = JsonToNBT.getTagFromJson(spawnerData.getMetaData());

                            if (!(nbtbase instanceof NBTTagCompound))
                            {
                            	throw new RuntimeException(); // Not a valid tag
                            }

                            nbttagcompound = (NBTTagCompound)nbtbase;
                        }
                        catch (NBTException nbtexception)
                        {
                        	if(OTG.getPluginConfig().SpawnLog)
                        	{
                        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in SpawnerFunction: " + spawnerData.getMetaData() + ". Skipping mob.");
                        	}
                        	continue;
                        }

                        nbttagcompound.setString("id", resourceLocation.toString());
                        entity = EntityList.createEntityFromNBT(nbttagcompound, worldServer);
                    }

                    if(entity == null)
                    {
                    	throw new RuntimeException();
                    }

			        int worldMobCount = 0;
			        int mobCountRadius = 32;
			        for (int x = 0; x < worldServer.loadedEntityList.size(); x++)
			        {
			        	Entity entity1 = ((Entity)worldServer.loadedEntityList.get(x));
			            if (entity1.getClass() == entityClass && entity1.getEntityData().hasKey("OTG"))
			            {
			            	if(
		            			entity1.posX >= spawnerData.x - mobCountRadius &&
		            			entity1.posX <= spawnerData.x + mobCountRadius &&
            					entity1.posY >= spawnerData.y - mobCountRadius &&
								entity1.posY <= spawnerData.y + mobCountRadius &&
		            			entity1.posZ >= spawnerData.z - mobCountRadius &&
		            			entity1.posZ <= spawnerData.z + mobCountRadius
	            			)
			            	{
				            	worldMobCount++;
			            	}
			            }
			        }

			        if(worldMobCount >= max)
					{
			        	continue;
					}

                    int j1 = spawnerData.x;
                    int k1 = spawnerData.y;
                    int l1 = spawnerData.z;

                    float x = (float)j1 + 0.5F;
                    float y = (float)k1;
                    float z = (float)l1 + 0.5F;

					float yaw = spawnerData.yaw;
					float pitch = spawnerData.pitch;

					entity.getEntityData().setBoolean("OTG", true);

                    if(despawnTime > 0)
                    {
                    	entity.getEntityData().setInteger("OTGDT", despawnTime - 1); // OTG Despawn time
                    }

                    if(entity instanceof EntityLiving)
                    {
						double velocityY = spawnerData.velocityYSet ? spawnerData.velocityY : 0;
						double velocityX = spawnerData.velocityXSet ? spawnerData.velocityX : Math.random() * 0.2 - 0.1;
						double velocityZ = spawnerData.velocityZSet ? spawnerData.velocityZ : Math.random() * 0.2 - 0.1;

                        entity.setLocationAndAngles((double)x, (double)y, (double)z, yaw, pitch);
                        entity.addVelocity(velocityX, velocityY, velocityZ);

                        Result canSpawn = ForgeEventFactory.canEntitySpawn((EntityLiving) entity, worldServer, x, y, z);

                        boolean entityCanSpawnHere = false;

                        if(canSpawn == Result.DEFAULT)
                        {
                            int ia = MathHelper.floor(entity.posX);
                            int ja = MathHelper.floor(entity.getEntityBoundingBox().minY);
                            int ka = MathHelper.floor(entity.posZ);

                        	boolean b1 = entity.world.checkNoEntityCollision(entity.getEntityBoundingBox());
                        	boolean b2 = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty();
                        	boolean b3 = !entity.world.containsAnyLiquid(entity.getEntityBoundingBox());

                        	boolean b5 = entity instanceof EntityCreature ? ((EntityCreature)entity).getBlockPathWeight(new BlockPos(ia, ja, ka)) >= 0.0F : true;

                        	entityCanSpawnHere = b1 && b2 && b3 && b5;
                        }

                        if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entityCanSpawnHere))
                        {
                        	for(int r = 0; r < groupSize; r++)
                        	{
	        					if(worldMobCount >= max)
        						{
	        						break;
        						}

                    			rngRoll = random.nextInt(100);
                    			if(spawnChance <= rngRoll)
                    			{
                    				continue;
                    			}

                        		if(r != 0)
                        		{
                        			if(spawnerData.getMetaData() == null)
                        			{
                                        try
                                        {
                                        	entity = (EntityLiving) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { worldServer });
                                        }
                                        catch (Exception exception)
                                        {
                                            exception.printStackTrace();
                                            break;
                                        }
                        			} else {
                                    	entity = EntityList.createEntityFromNBT(nbttagcompound, worldServer);
                                    }

                                    velocityY = spawnerData.velocityYSet ? spawnerData.velocityY : 0;
	        						velocityX = spawnerData.velocityXSet ? spawnerData.velocityX : Math.random() * 0.2 - 0.1;
	        						velocityZ = spawnerData.velocityZSet ? spawnerData.velocityZ : Math.random() * 0.2 - 0.1;

                                    entity.setLocationAndAngles((double)x, (double)y, (double)z, yaw, pitch);
                                    entity.addVelocity(velocityX, velocityY, velocityZ);
                                    entity.getEntityData().setBoolean("OTG", true);
                                    entity.getEntityData().setInteger("OTGDT", despawnTime); // OTG Despawn time
                        		}

                                if (spawnerData.getMetaData() == null)
                                {
                                	((EntityLiving) entity).onInitialSpawn(worldServer.getDifficultyForLocation(new BlockPos(x, y, z)),(IEntityLivingData)null);
                                }
                                worldServer.spawnEntity(entity);

                                if (nbttagcompound != null)
                                {
                                    Entity entity2 = entity;

                                    for (NBTTagCompound nbttagcompound1 = nbttagcompound; entity2 != null && nbttagcompound1.hasKey("Riding", 10); nbttagcompound1 = nbttagcompound1.getCompoundTag("Riding"))
                                    {
                                        Entity entity1 = EntityList.createEntityFromNBT(nbttagcompound1.getCompoundTag("Riding"), worldServer);

                                        if (entity1 != null)
                                        {
                                            entity1.setLocationAndAngles(x, y, z, entity1.rotationYaw, entity1.rotationPitch);
                                            worldServer.spawnEntity(entity1);
                                            entity2.startRiding(entity1);
                                        }

                                        entity2 = entity1;
                                    }
                                }

								worldMobCount++;
                        	}
                        }
                    } else {

						double velocityY = spawnerData.velocityYSet ? spawnerData.velocityY : 0.1;
						double velocityX = spawnerData.velocityXSet ? spawnerData.velocityX : Math.random() * 0.2 - 0.1;
						double velocityZ = spawnerData.velocityZSet ? spawnerData.velocityZ : Math.random() * 0.2 - 0.1;

                        entity.setLocationAndAngles((double)x, (double)y, (double)z, yaw, pitch);
                        if(!(entity instanceof EntityHanging))
                        {
                        	entity.addVelocity(velocityX, velocityY, velocityZ);
                        }

                        boolean entityCanSpawnHere = false;

                        int ia = MathHelper.floor(((Entity)entity).posX);
                        int ja = MathHelper.floor(((Entity)entity).getEntityBoundingBox().minY);
                        int ka = MathHelper.floor(((Entity)entity).posZ);

                    	boolean b1 = entity.world.checkNoEntityCollision(entity.getEntityBoundingBox());
                    	boolean b2 = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty();
                    	boolean b3 = !entity.world.containsAnyLiquid(entity.getEntityBoundingBox());

                    	boolean b5 = entity instanceof EntityLiving ? ((EntityCreature)entity).getBlockPathWeight(new BlockPos(ia, ja, ka)) >= 0.0F : true;

                    	entityCanSpawnHere = b1 && b2 && b3 && b5;

                        if (entityCanSpawnHere)
                        {
                        	for(int r = 0; r < groupSize; r++)
                        	{
	        					if(worldMobCount >= max)
        						{
	        						break;
        						}

                    			rngRoll = random.nextInt(100);
                    			if(spawnChance <= rngRoll)
                    			{
                    				continue;
                    			}

                        		if(r != 0)
                        		{
                        			if(spawnerData.getMetaData() == null)
                        			{
                                        try
                                        {
                                        	entity = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { worldServer });
                                        }
                                        catch (Exception exception)
                                        {
                                            exception.printStackTrace();
                                            break;
                                        }
                        			} else {
                                    	entity = EntityList.createEntityFromNBT(nbttagcompound, worldServer);
                                    }

	        						velocityX = spawnerData.velocityXSet ? spawnerData.velocityX : Math.random() * 0.2 - 0.1;
	        						velocityY = spawnerData.velocityYSet ? spawnerData.velocityY : 0.1;
	        						velocityZ = spawnerData.velocityZSet ? spawnerData.velocityZ : Math.random() * 0.2 - 0.1;

                                    entity.setLocationAndAngles((double)x, (double)y, (double)z, yaw, pitch);
                                    entity.addVelocity(velocityX, velocityY, velocityZ);
                                    entity.getEntityData().setBoolean("OTG", true);
                                    entity.getEntityData().setInteger("OTGDT", despawnTime); // OTG Despawn time
                        		}

                                worldServer.spawnEntity(entity);

								worldMobCount++;
                        	}
                        }
                    }
                }
            }

			for (int a = 0; a < playerCoords.size(); ++a)
            {
				EntityPlayer player = (EntityPlayer)playerCoords.get(a)[3];
				ArrayList<ParticleFunction> particleDataForOTGPerPlayer = new ArrayList<ParticleFunction>();

                for (ChunkCoordinate chunkcoordintpair1 : this.eligibleChunksForSpawning.keySet())
                {
                    if (!this.eligibleChunksForSpawning.get(chunkcoordintpair1))
                    {
                    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkcoordintpair1.getChunkX(), chunkcoordintpair1.getChunkZ());
                    	ArrayList<ParticleFunction> particleDataForOTG = world.GetWorldSession().getParticlesForChunk(chunkCoord);

                    	if(particleDataForOTG != null && particleDataForOTG.size() > 0)
                    	{
                    		for(ParticleFunction particleData : particleDataForOTG)
                    		{
                		        float f = (float)((Double)playerCoords.get(a)[0] - particleData.x);
                		        float f1 = (float)((Double)playerCoords.get(a)[1] - particleData.y);
                		        float f2 = (float)((Double)playerCoords.get(a)[2] - particleData.z);
                		        double distance = f * f + f1 * f1 + f2 * f2;

                                if(distance > 0 && distance < maxDistToClosestPlayerSq)
                                {
                                	if(!worldServer.getBlockState(new BlockPos(particleData.x, particleData.y, particleData.z)).getMaterial().isSolid())
                                	{
                                		particleDataForOTGPerPlayer.add(particleData);
                                	} else {
                                		world.GetWorldSession().removeParticles(chunkCoord, particleData);
                                	}
                                }
            				}
                    	}
                    }
                }
        		if(particleDataForOTGPerPlayer.size() > 0)
        		{
        			ServerPacketManager.SendParticlesPacket(particleDataForOTGPerPlayer, (EntityPlayerMP) player);
        		}
        	}
        }
    }

	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			((ForgeEngine)OTG.getEngine()).ProcessPregeneratorTick();

			if(((ForgeEngine)OTG.getEngine()).getOverWorld() != null) // If overworld is null then the overworld is not an OTG world
			{
				boolean cartographerEnabled = ((ForgeEngine)OTG.getEngine()).getCartographerEnabled();

				if(cartographerEnabled)
				{
					Cartographer.UpdateWorldMap();
				}
			}

			// When players are above or below the y threshold teleport them to the dimension above or below this one (configured via worldconfig)
			TeleportPlayers();
		}

		if(event.phase == Phase.END)
		{
			// Particles and Spawners
        	currentTimeInSeconds = (int)Math.ceil(System.currentTimeMillis() / 1000L);
        	if(currentTimeInSeconds != lastSpawnedTimeInSeconds) // If the server is lagging and suddenly processes multiple ticks in a row only do 1 spawning cycle.
        	{
	    		lastSpawnedTimeInSeconds = currentTimeInSeconds;
				for(LocalWorld forgeWorld : ((ForgeEngine)OTG.getEngine()).getWorldLoader().getAllLoadedWorlds())
				{
					findChunksForSpawning((ForgeWorld)forgeWorld, true, true);
				}
    		}

        	// ModData
	        List<IMCMessage> messages = FMLInterModComms.fetchRuntimeMessages(OTGPlugin.instance);
	        if (messages.size() > 0)
	        {
	        	for(IMCMessage imcMessage : messages)
	        	{
	                // Checks to see if the message has a specific key.
	                if (imcMessage.key.equalsIgnoreCase("GetModData"))
	                {
	                    // Checks to see if a message is a string
	                    if (imcMessage.isStringMessage())
	                    {

	                    	String[] paramString = imcMessage.getStringValue().split(",");
	                    	// Get BO3 ModData with worldname and chunkcoord from imcMessage.getStringValue() "New World,-51,10"
	                    	if(paramString.length == 3)
	                    	{
		                    	String worldName = paramString[0];

		                    	ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(worldName);
		                    	if(forgeWorld == null)
		                    	{
		                    		forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(worldName);
		                    	}

		                    	int chunkX;
		                    	int chunkZ;
		                    	try
		                    	{
			                    	chunkX = Integer.parseInt(paramString[1]);
			                    	chunkZ = Integer.parseInt(paramString[2]);
		                    	} catch(NumberFormatException ex)
		                    	{
		                    		OTG.log(LogMarker.WARN, "The mod " + imcMessage.getSender() + " has sent the following message: " + imcMessage.key + ", however the parameters were invalid: " + imcMessage.getStringValue() + ". Should be: MyWorldName,MyChunkX,MyChunkZ");
		                    		return;
		                    	}

                    			// Return modData to sender.
                        		String messageString = "";
		                    	HashMap<String, ArrayList<ModDataFunction>> modDataInChunk = forgeWorld.GetWorldSession().getModDataForChunk(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
		                    	if(modDataInChunk != null)
		                    	{
			                    	for(Entry<String, ArrayList<ModDataFunction>> modNameAndData : modDataInChunk.entrySet())
			                    	{
			                    		if(modNameAndData.getKey().equalsIgnoreCase(imcMessage.getSender()))
			                    		{
			                    			for(ModDataFunction modData : modNameAndData.getValue())
			                    			{
			                    				messageString += "[" + modData.x + "," + modData.y + "," + modData.z + "," + modData.modData + "]";
			                    			}
			                    		}
			                    	}
			                    	FMLInterModComms.sendRuntimeMessage(OTGPlugin.instance, imcMessage.getSender(), "ModData", "[" + "[" + worldName + "," + chunkX + "," + chunkZ + "]" + (messageString.length() > 0 ? messageString : "[]") + "]");
		                    	} else {
		                    		FMLInterModComms.sendRuntimeMessage(OTGPlugin.instance, imcMessage.getSender(), "ModData", "[" + "[" + worldName + "," + chunkX + "," + chunkZ + "]]");
		                    	}
	                    	} else {
	            	        	OTG.log(LogMarker.WARN, "The mod " + imcMessage.getSender() + " has sent the following message: " + imcMessage.key + ", however the parameters were invalid: " + imcMessage.getStringValue() + ". Should be: MyWorldName,MyChunkX,MyChunkZ");
	                    	}
	                    }
	                }
    	        	if (imcMessage.key.equalsIgnoreCase("ModData"))
    	        	{
	    	        	String[] paramString = imcMessage.getStringValue().replace("[[", "").replace("]]", "").split("\\]\\[");
	    	        	String[] chunkCoordString = paramString[0].split(",");
	    	        	String worldName = chunkCoordString[0];
	    	        	if(paramString.length < 2)
	    	        	{
	    	        		// Chunk hasn't been populated yet.
	    	        	} else {
		    	        	for(int i = 1; i < paramString.length; i++)
		    	        	{
		    	        		if(paramString[i].length() > 0)
		    	        		{
			    	        		String[] modDataString = paramString[i].split(",");
				    	        	int modDataBlockX = Integer.parseInt(modDataString[0]);
				    	        	int modDataBlockY = Integer.parseInt(modDataString[1]);
				    	        	int modDataBlockZ = Integer.parseInt(modDataString[2]);
				    	        	String modDataText = modDataString[3];

				    	        	// Do something with the modData
		    						ForgeWorld world = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(worldName);
		    						if(world == null)
		    						{
		    							world = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(worldName);
		    						}

		    						if(world == null)
		    						{
		    							OTG.log(LogMarker.FATAL, "Error: Failed to load LocalWorld for world \"" + worldName + "\"");
		    							throw new RuntimeException("Error: Failed to load LocalWorld for world \"" + worldName + "\"");
		    						}
		    						if(world.getConfigs() == null)
		    						{
		    							OTG.log(LogMarker.FATAL, "Error: Failed to load world settings for world \"" + worldName + "\"");
		    							throw new RuntimeException("Error: Failed to load world settings for world \"" + worldName + "\"");
		    						}
		    						if(world.getConfigs().getWorldConfig() == null)
		    						{
		    							OTG.log(LogMarker.FATAL, "Error: Failed to load worldConfig for world \"" + worldName + "\"");
		    							throw new RuntimeException("Error: Failed to load worldConfig for world \"" + worldName + "\"");
		    						}

		    						String[] paramString2 = modDataText.split("\\/");

		    						if(paramString2.length > 1)
		    						{
		    							if(paramString2[0].equals("mob"))
		    							{
		    								try
		    								{
		    									EntityFunction entityFunc = new EntityFunction();
		    									entityFunc.x = modDataBlockX;
		    									entityFunc.y = modDataBlockY;
		    									entityFunc.z = modDataBlockZ;

		    									entityFunc.mobName = paramString2[1];
		    									entityFunc.groupSize = paramString2.length > 2 ? Integer.parseInt(paramString2[2]) : 1;
		    									entityFunc.nameTagOrNBTFileName = paramString2.length > 5 ? paramString2[5] : null;
		    									entityFunc.originalNameTagOrNBTFileName = entityFunc.nameTagOrNBTFileName;

		    									world.SpawnEntity(entityFunc);
		    								}
		    								catch(NumberFormatException ex)
		    								{
		    									if(OTG.getPluginConfig().SpawnLog)
		    									{
		    										OTG.log(LogMarker.WARN, "Error in ModData: " + modDataText + " parameter count was not a number");
		    									}
		    								}
		    							}
		    							else if(paramString2[0].equals("block"))
		    							{
		    								try {
												LocalMaterialData material = OTG.readMaterial(paramString2[1]);
			    								world.setBlock(modDataBlockX, modDataBlockY, modDataBlockZ, material, null, true);
											}
		    								catch (InvalidConfigException e)
		    								{
		    									if(OTG.getPluginConfig().SpawnLog)
		    									{
													OTG.log(LogMarker.WARN, "Error in ModData: " + modDataText + " parameter material was not a valid material");
													e.printStackTrace();
		    									}
											}
		    							}
		    						}
		    	        		} else {
		    	        			// There was no modData in this chunk.
		    	        		}
		    	        	}
	    	        	}
    	        	}
    	        	//OTG.log(LogMarker.INFO, "The mod " + imcMessage.getSender() + " has sent the following key: " + imcMessage.key + " and message: " + imcMessage.getStringValue());
	        	}
	        }
		}
	}

	private void TeleportPlayers()
	{
		MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		for(WorldServer worldServer : mcServer.worlds)
		{
	    	if(
    			(
    				(
						worldServer.getWorldInfo() instanceof DerivedWorldInfo &&
    					((DerivedWorldInfo)worldServer.getWorldInfo()).delegate.getGeneratorOptions().equals("OpenTerrainGenerator")
    				) || (
    						worldServer.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator")
    				)
    			)
    		)
			{
				ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>(worldServer.playerEntities);
				for(EntityPlayer player : players)
				{
					tryTeleportPlayer(player);
				}
			}
		}
	}

	private void tryTeleportPlayer(EntityPlayer player)
	{
		ForgeWorld playerWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(player.world);
		DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(WorldHelper.getName(player.world));
		
		// DimensionBelow
		if(playerWorld != null && dimConfig.Settings.DimensionBelow != null && dimConfig.Settings.DimensionBelow.trim().length() > 0)
		{
			if(player.getPosition().getY() < dimConfig.Settings.DimensionBelowHeight)
			{
				ForgeWorld destinationWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(dimConfig.Settings.DimensionBelow);
				if(destinationWorld == null)
				{
					destinationWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(dimConfig.Settings.DimensionBelow);
				}

				if(destinationWorld != null) // Dimension does not exist
				{
					if(destinationWorld == playerWorld)
					{
						player.world.setBlockToAir(new BlockPos(player.getPosition().getX(), 254, player.getPosition().getZ()));
						player.world.setBlockToAir(new BlockPos(player.getPosition().getX(), 255, player.getPosition().getZ()));
						player.setPositionAndUpdate(player.getPosition().getX(), 254, player.getPosition().getZ());
					} else {
						TeleportPlayerToDimension(playerWorld.getWorld().provider.getDimension(), destinationWorld.getWorld().provider.getDimension(), player);
						return;
					}
				}
			}
		}

		// DimensionAbove
		if(playerWorld != null && dimConfig.Settings.DimensionAbove != null && dimConfig.Settings.DimensionAbove.trim().length() > 0)
		{
			if(player.getPosition().getY() > dimConfig.Settings.DimensionAboveHeight)
			{
				ForgeWorld destinationWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(dimConfig.Settings.DimensionAbove);
				if(destinationWorld == null)
				{
					destinationWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(dimConfig.Settings.DimensionAbove);
				}

				if(destinationWorld != null) // Dimension does not exist
				{
					if(destinationWorld != playerWorld)
					{
						TeleportPlayerToDimension(playerWorld.getWorld().provider.getDimension(), destinationWorld.getWorld().provider.getDimension(), player);
					}
				}
			}
		}
	}

    private void TeleportPlayerToDimension(int originDimension, int newDimension, EntityPlayer e)
    {
    	boolean cartographerEnabled = ((ForgeEngine)OTG.getEngine()).getCartographerEnabled();

		if(e instanceof EntityPlayerMP)
		{
			OTGTeleporter.changeDimension(newDimension, (EntityPlayerMP)e, false, false);
		}

    	// If coming from main world then update Cartographer map at last player position (should remove head+banner from Cartographer map)
		if(originDimension == 0 && cartographerEnabled)
		{
			//LocalWorld localWorld = OTG.getEngine().getWorld(world.getWorldInfo().getWorldName());
			//if(localWorld != null)
			{
				Cartographer.CreateBlockWorldMapAtSpawn(ChunkCoordinate.fromBlockCoords(e.getPosition().getX(), e.getPosition().getZ()), true);
			}
		}
    }
}