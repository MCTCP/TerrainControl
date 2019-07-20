package com.pg85.otg.forge.dimensions;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class OTGTeleporter
{
	// Items

    public static Entity changeDimension(int dimensionIn, Entity _this)
    {
    	ITeleporter teleporter = _this.getServer().getWorld(dimensionIn).getDefaultTeleporter();
        if (!_this.world.isRemote && !_this.isDead)
        {
            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(_this, dimensionIn)) return null;
            _this.world.profiler.startSection("changeDimension");
            MinecraftServer minecraftserver = _this.getServer();
            int i = _this.dimension;
            WorldServer worldserver = minecraftserver.getWorld(i);                        
            WorldServer worldserver1 = net.minecraftforge.common.DimensionManager.getWorld(dimensionIn);
            if(worldserver1 == null)
            {
            	OTGDimensionManager.initDimension(dimensionIn);
            }
            worldserver1 = net.minecraftforge.common.DimensionManager.getWorld(dimensionIn);            
            _this.dimension = dimensionIn;
            
            if (i == 1 && dimensionIn == 1 && teleporter.isVanilla())
            {
                worldserver1 = minecraftserver.getWorld(0);
                _this.dimension = 0;
            }

            _this.world.removeEntity(_this);
            _this.isDead = false;
            _this.world.profiler.startSection("reposition");

            BlockPos blockpos;

            if (dimensionIn == 1 && teleporter.isVanilla())
            {
                blockpos = worldserver1.getSpawnCoordinate();
            } else {
                double moveFactor = worldserver.provider.getMovementFactor() / worldserver1.provider.getMovementFactor();
                double d0 = MathHelper.clamp(_this.posX * moveFactor, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
                double d1 = MathHelper.clamp(_this.posZ * moveFactor, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);

                d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
                d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);
                float f = _this.rotationYaw;
                _this.setLocationAndAngles(d0, _this.posY, d1, 90.0F, 0.0F);
                teleporter.placeEntity(worldserver1, _this, f);
                blockpos = new BlockPos(_this);
            }            
            
            worldserver.updateEntityWithOptionalForce(_this, false);
            _this.world.profiler.endStartSection("reloading");
            Entity entity = EntityList.newEntity(_this.getClass(), worldserver1);

            if (entity != null)
            {
                copyDataFromOld(_this, entity);

                if (i == 1 && dimensionIn == 1 && teleporter.isVanilla())
                {
                    BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
                    entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
                } else {
                    entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw, entity.rotationPitch);
                }                
                
                boolean flag = entity.forceSpawn;
                entity.forceSpawn = true;

                worldserver1.spawnEntity(entity);

                entity.forceSpawn = flag;
                worldserver1.updateEntityWithOptionalForce(entity, false);
            }

            _this.isDead = true;
            _this.world.profiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            _this.world.profiler.endSection();
            return entity;
        } else {
            return null;
        }
    }

    private static void copyDataFromOld(Entity entityIn, Entity _this)
    {       
        NBTTagCompound nbttagcompound = entityIn.writeToNBT(new NBTTagCompound());
        nbttagcompound.removeTag("Dimension");
        _this.readFromNBT(nbttagcompound);
        _this.timeUntilPortal = entityIn.timeUntilPortal;
        _this.lastPortalPos = entityIn.lastPortalPos;
        _this.lastPortalVec = entityIn.lastPortalVec;
        _this.teleportDirection = entityIn.teleportDirection;
    }

	// Players

	public static Entity changeDimension(int dimensionIn, EntityPlayerMP _this, boolean createPortal, boolean placeOnHighestBlock)
    {
		ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dimensionIn);
		
		if(forgeWorld == null && ((ForgeEngine)OTG.getEngine()).getUnloadedWorldByDimId(dimensionIn) != null)
		{		
			OTGDimensionManager.initDimension(dimensionIn);
			
			forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dimensionIn);
	
			DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(dimensionIn == 0 ? "overworld" : forgeWorld.getName());
			
			// TODO: Fix teleporttospawnonly when making portals
			if(dimConfig.Settings.TeleportToSpawnOnly)
			{
				BlockPos forgeWorldSpawnPoint = forgeWorld.getSpawnPoint();
				_this.setLocationAndAngles(forgeWorldSpawnPoint.getX(), forgeWorld.getHighestBlockYAt(forgeWorldSpawnPoint.getX(), forgeWorldSpawnPoint.getZ()), forgeWorldSpawnPoint.getZ(), 0, 0);
				placeOnHighestBlock = true;
			} //else {
				// Find suitable spawn location
				//_this.setLocationAndAngles(_this.getPosition().getX(), forgeWorld.getHighestBlockYAt(_this.getPosition().getX(), _this.getPosition().getZ(), true, true, false, false), _this.getPosition().getZ(), 0, 0);
			//}
		}
       
		// Entity.setPortal has updated the entity's lastPortalVec incorrectly, 
		// because Blocks.Portal.createPatternHelper can't detect OTG portals, 
		// detect portals and update lastPortalVec again.
		
        if (!_this.world.isRemote)
        {
            BlockPattern.PatternHelper blockpattern$patternhelper = OTGBlockPortal.createPatternHelper(_this.world, _this.lastPortalPos != null ? _this.lastPortalPos : _this.getPosition());
            double d0 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getFrontTopLeft().getZ() : (double)blockpattern$patternhelper.getFrontTopLeft().getX();
            double d1 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? _this.posZ : _this.posX;
            d1 = Math.abs(MathHelper.pct(d1 - (double)(blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double)blockpattern$patternhelper.getWidth()));
            double d2 = MathHelper.pct(_this.posY - 1.0D, (double)blockpattern$patternhelper.getFrontTopLeft().getY(), (double)(blockpattern$patternhelper.getFrontTopLeft().getY() - blockpattern$patternhelper.getHeight()));
            _this.lastPortalVec = new Vec3d(d1, d2, 0.0D);
            _this.teleportDirection = blockpattern$patternhelper.getForwards();
        }
        
        // We're overriding the flow from EntityPlayerMP.changeDimension from here on.
        // We have to because the normal flow can't detect OTG portals and tp's players/entities
        // to the wrong coordinates when going through a portal.
		
		Teleporter teleporter = _this.getServer().getWorld(dimensionIn).getDefaultTeleporter();
        _this.invulnerableDimensionChange = true;

        if (_this.dimension == 0 && dimensionIn == -1)
        {
        	_this.enteredNetherPosition = new Vec3d(_this.posX, _this.posY, _this.posZ);
        }
        else if (_this.dimension != -1 && dimensionIn != 0)
        {
        	_this.enteredNetherPosition = null;
        }

        if (_this.dimension == 1 && dimensionIn == 1 && teleporter.isVanilla())
        {
        	_this.world.removeEntity(_this);

            if (!_this.queuedEndExit)
            {
            	_this.queuedEndExit = true;
            	_this.connection.sendPacket(new SPacketChangeGameState(4, _this.seenCredits ? 0.0F : 1.0F));
            	_this.seenCredits = true;
            }
            ServerPacketManager.sendParticlesPacket(null, _this); // Clear particles
            return _this;
        } else {
            if (_this.dimension == 0 && dimensionIn == 1)
            {
                dimensionIn = 1;
            }

            //_this.mcServer.getPlayerList().transferPlayerToDimension(_this, dimensionIn, teleporter);
            transferPlayerToDimension(_this, dimensionIn, teleporter, _this.getServer().getPlayerList(), createPortal, false);
            _this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            _this.lastExperience = -1;
            _this.lastHealth = -1.0F;
            _this.lastFoodLevel = -1;
            ServerPacketManager.sendParticlesPacket(null, _this); // Clear particles            
            return _this;
        }		

		/*
		 * TODO: Re-use this for forcing gamemode / flying per dimension?
		if(((ForgeEngine)OTG.getEngine()).getCartographerEnabled() && dimensionIn == Cartographer.CartographerDimension)
		{
			_this.capabilities.allowEdit = false;
			_this.capabilities.allowFlying = true;
			_this.sendPlayerAbilities();
		} else {
			_this.capabilities.allowEdit = true;
			_this.capabilities.allowFlying = _this.capabilities.isCreativeMode;
			_this.sendPlayerAbilities();
		}
	    */
    }

    private static void transferPlayerToDimension(EntityPlayerMP player, int dimensionIn, net.minecraft.world.Teleporter teleporter, PlayerList _this, boolean createPortal, boolean placeOnHighestBlock)
    {
        int i = player.dimension;
        WorldServer worldserver = _this.getServerInstance().getWorld(player.dimension);
        player.dimension = dimensionIn;
        WorldServer worldserver1 = _this.getServerInstance().getWorld(player.dimension);
        player.connection.sendPacket(new SPacketRespawn(player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
        _this.updatePermissionLevel(player);
        worldserver.removeEntityDangerously(player);
        player.isDead = false;
        transferEntityToWorld(player, i, worldserver, worldserver1, teleporter, createPortal, placeOnHighestBlock);
        _this.preparePlayer(player, worldserver);
        player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        player.interactionManager.setWorld(worldserver1);
        player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
        _this.updateTimeAndWeatherForPlayer(player, worldserver1);
        _this.syncPlayerInventory(player);

        for (PotionEffect potioneffect : player.getActivePotionEffects())
        {
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        // Fix MC-88179: on non-death SPacketRespawn, also resend attributes
        net.minecraft.entity.ai.attributes.AttributeMap attributemap = (net.minecraft.entity.ai.attributes.AttributeMap) player.getAttributeMap();
        java.util.Collection<net.minecraft.entity.ai.attributes.IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
        if (!watchedAttribs.isEmpty()) player.connection.sendPacket(new net.minecraft.network.play.server.SPacketEntityProperties(player.getEntityId(), watchedAttribs));
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, i, dimensionIn);
    }

    private static void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, net.minecraft.world.Teleporter teleporter, boolean createPortal, boolean placeOnHighestBlock)
    {
    	double entityPosY = entityIn.getPosition().getY();
        double moveFactor = oldWorldIn.provider.getMovementFactor() / toWorldIn.provider.getMovementFactor();
        double d0 = MathHelper.clamp(entityIn.posX * moveFactor, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
        double d1 = MathHelper.clamp(entityIn.posZ * moveFactor, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
        //double d2 = 8.0D;
        float f = entityIn.rotationYaw;
        oldWorldIn.profiler.startSection("moving");

        if (entityIn.dimension == 1 && teleporter.isVanilla())
        {
            BlockPos blockpos;

            if (lastDimension == 1)
            {
                blockpos = toWorldIn.getSpawnPoint();
            } else {
                blockpos = toWorldIn.getSpawnCoordinate();
            }

            d0 = (double)blockpos.getX();
            entityIn.posY = (double)blockpos.getY();
            d1 = (double)blockpos.getZ();
            entityIn.setLocationAndAngles(d0, entityIn.posY, d1, 90.0F, 0.0F);

            if (entityIn.isEntityAlive())
            {
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }        
        
        oldWorldIn.profiler.endSection();

        if (lastDimension != 1 || !teleporter.isVanilla())
        {
            oldWorldIn.profiler.startSection("placing");
            d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
            d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);

            if (entityIn.isEntityAlive())
            {
            	entityIn.setLocationAndAngles(d0, entityIn.getPosition().getY(), d1, entityIn.rotationYaw, entityIn.rotationPitch);
            	oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            	
                ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(DimensionManager.getWorld(lastDimension));

                ArrayList<LocalMaterialData> portalMaterials = new ArrayList<LocalMaterialData>();
                portalMaterials.add(MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 0));
                if(lastDimension == 0 && (ForgeWorld)((ForgeEngine)OTG.getEngine()).getOverWorld() == null) // This is a vanilla overworld
                {
                	portalMaterials = OTG.getDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
                }
                else if(forgeWorld != null)
                {
                	portalMaterials = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
                }
                
                if(createPortal)
                {
                	placeInPortal((ForgeMaterialData)portalMaterials.get(0), toWorldIn, entityIn, f, teleporter);
                } else {
            		if(entityPosY < 0) // Falling down into another dimension
            		{
            			toWorldIn.setBlockToAir(new BlockPos(d0, 254, d1));
        				toWorldIn.setBlockToAir(new BlockPos(d0, 255, d1));
            			entityIn.setLocationAndAngles(d0 + 0.5, 255, d1 + 0.5, entityIn.rotationYaw, entityIn.rotationPitch);
            			((EntityPlayerMP)entityIn).connection.setPlayerLocation(d0 + 0.5, 254, d1 + 0.5, entityIn.rotationYaw, entityIn.rotationPitch);
            		}
            		else if(entityPosY > 255) // Climbing up to another dimension
            		{
            			toWorldIn.setBlockState(new BlockPos(d0, 0, d1), Blocks.GRASS.getDefaultState());
        				toWorldIn.setBlockToAir(new BlockPos(d0, 1, d1));
        				toWorldIn.setBlockToAir(new BlockPos(d0, 2, d1));
            			entityIn.setLocationAndAngles(d0 + 0.5, 1, d1 + 0.5, entityIn.rotationYaw, entityIn.rotationPitch);
            			((EntityPlayerMP)entityIn).connection.setPlayerLocation(d0 + 0.5, 1, d1 + 0.5, entityIn.rotationYaw, entityIn.rotationPitch);
            		} else { // Using /otg tp or teleport button
            			if(toWorldIn.provider.getDimension() != -1)
            			{
            				boolean bFound = false;
                			int highestBlock = toWorldIn.getHeight((int)Math.ceil(entityIn.posX), (int)Math.ceil(entityIn.posZ));
            				if(highestBlock <= 0)
                			{
                				int radius = 64;
                				for(int r = 0; r < radius; r++)
                				{
                					int searchX = (int)Math.floor(entityIn.posX + r);
                					int searchZ = (int)Math.floor(entityIn.posZ + r);
                					for(int i = 1; i < 128; i++)
	                				{
	                					if(
                							toWorldIn.isSideSolid(new BlockPos(searchX, i - 1, searchZ), EnumFacing.UP) &&
                							toWorldIn.isAirBlock(new BlockPos(searchX, i, searchZ)) &&
                							toWorldIn.isAirBlock(new BlockPos(searchX, i + 1, searchZ))
            							)
	                					{
	                						highestBlock = i - 1;
	        	                			bFound = true;
	        	                			break;
	                					}
	                				}
	                				if(bFound)
	                				{
	                					break;
	                				}
                				}
                				if(!bFound)
	                			{
	                				highestBlock = (int)Math.floor(entityIn.posY);
	                			}
                			}
                			entityIn.setLocationAndAngles(entityIn.posX, highestBlock + 1, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
                			((EntityPlayerMP)entityIn).connection.setPlayerLocation(entityIn.posX, highestBlock + 1, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
            			} else {
            				int radius = 64;
            				boolean bFound = false;
            				for(int r = 0; r < radius; r++)
            				{
            					int searchX = (int)Math.floor(entityIn.posX + r);
            					int searchZ = (int)Math.floor(entityIn.posZ + r);
            					for(int i = 1; i < 128; i++)
                				{
                					if(
            							toWorldIn.isSideSolid(new BlockPos(searchX, i - 1, searchZ), EnumFacing.UP) &&
            							toWorldIn.isAirBlock(new BlockPos(searchX, i, searchZ)) &&
            							toWorldIn.isAirBlock(new BlockPos(searchX, i + 1, searchZ))
        							)
                					{
        	                			entityIn.setLocationAndAngles(searchX, i, searchZ, entityIn.rotationYaw, entityIn.rotationPitch);
        	                			((EntityPlayerMP)entityIn).connection.setPlayerLocation(searchX, i, searchZ, entityIn.rotationYaw, entityIn.rotationPitch);
        	                			bFound = true;
        	                			break;
                					}
                				}
                				if(bFound)
                				{
                					break;
                				}
            				}
                			if(!bFound)
                			{
                    			toWorldIn.setBlockState(new BlockPos(d0, 64, d1), Blocks.NETHERRACK.getDefaultState());
                				toWorldIn.setBlockToAir(new BlockPos(d0, 65, d1));
                				toWorldIn.setBlockToAir(new BlockPos(d0, 66, d1));
                				entityIn.setLocationAndAngles(entityIn.posX, 65, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
                				((EntityPlayerMP)entityIn).connection.setPlayerLocation(entityIn.posX, 65, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
                			}
            			}
            		}
                }

                toWorldIn.spawnEntity(entityIn);
                toWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }

            oldWorldIn.profiler.endSection();
        }

        entityIn.setWorld(toWorldIn);
        
        if(placeOnHighestBlock)
        {
			entityIn.setLocationAndAngles(entityIn.posX, entityIn.getEntityWorld().getHeight((int)entityIn.posX, (int)entityIn.posZ), entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
			((EntityPlayerMP)entityIn).connection.setPlayerLocation(entityIn.posX, entityIn.getEntityWorld().getHeight((int)entityIn.posX, (int)entityIn.posZ), entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
			toWorldIn.updateEntityWithOptionalForce(entityIn, false);
        }
    }

	static Long2ObjectMap<Teleporter.PortalPosition> getPortals(int dimensionId)
	{
		Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = null;

		MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
    	WorldServer worldserver1 = mcServer.getWorld(dimensionId);
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
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return destinationCoordinateCache;
	}

    private static boolean placeInExistingPortal(WorldServer destinationWorld, Entity entityIn, float rotationYaw, Teleporter _this)
    {
    	//int i = 128;
        double d0 = -1.0D;
        //int j = MathHelper.floor(entityIn.posX);
        //int k = MathHelper.floor(entityIn.posZ);
        //boolean flag = true;
        BlockPos blockpos = BlockPos.ORIGIN;
        //long l = ChunkPos.asLong(j, k);

        BlockPos blockpos3 = new BlockPos(entityIn);

        for (int i1 = -128; i1 <= 128; ++i1)
        {
            BlockPos blockpos2;

            for (int j1 = -128; j1 <= 128; ++j1)
            {
                for (BlockPos blockpos1 = blockpos3.add(i1, destinationWorld.getActualHeight() - 1 - blockpos3.getY(), j1); blockpos1.getY() >= 0; blockpos1 = blockpos2)
                {
                    blockpos2 = blockpos1.down();

                    if (destinationWorld.getBlockState(blockpos1).getBlock() == Blocks.PORTAL)
                    {
                        for (blockpos2 = blockpos1.down(); destinationWorld.getBlockState(blockpos2).getBlock() == Blocks.PORTAL; blockpos2 = blockpos2.down())
                        {
                            blockpos1 = blockpos2;
                        }

                        double d1 = blockpos1.distanceSq(blockpos3);

                        if (d0 < 0.0D || d1 < d0)
                        {
                            BlockPattern.PatternHelper blockpattern$patternhelper = OTGBlockPortal.createPatternHelper(entityIn.getEntityWorld(), destinationWorld, blockpos1);
                            if(blockpattern$patternhelper.getHeight() >= 3 && blockpattern$patternhelper.getWidth() >= 2)
                            {
                                d0 = d1;
                                blockpos = blockpos1;
                            }        			                               
                        }
                    }
                }
            }
        }

        if (d0 >= 0.0D)
        {
            double d5 = (double)blockpos.getX() + 0.5D;
            double d7 = (double)blockpos.getZ() + 0.5D;
            BlockPattern.PatternHelper blockpattern$patternhelper = OTGBlockPortal.createPatternHelper(entityIn.getEntityWorld(), destinationWorld, blockpos);
            boolean flag1 = blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;            
                      
            double d2 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getFrontTopLeft().getZ() : (double)blockpattern$patternhelper.getFrontTopLeft().getX();
            double d6 = (double)(blockpattern$patternhelper.getFrontTopLeft().getY() + 1) - entityIn.getLastPortalVec().y * (double)blockpattern$patternhelper.getHeight();

            if (flag1)
            {
                ++d2;
            }

            if (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X)
            {
                d7 = d2 + (1.0D - entityIn.getLastPortalVec().x) * (double)blockpattern$patternhelper.getWidth() * (double)blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
            } else {
                d5 = d2 + (1.0D - entityIn.getLastPortalVec().x) * (double)blockpattern$patternhelper.getWidth() * (double)blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
            }

            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;

            if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection())
            {
                f = 1.0F;
                f1 = 1.0F;
            }
            else if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection().getOpposite())
            {
                f = -1.0F;
                f1 = -1.0F;
            }
            else if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection().rotateY())
            {
                f2 = 1.0F;
                f3 = -1.0F;
            } else {
                f2 = -1.0F;
                f3 = 1.0F;
            }

            double d3 = entityIn.motionX;
            double d4 = entityIn.motionZ;
            entityIn.motionX = d3 * (double)f + d4 * (double)f3;
            entityIn.motionZ = d3 * (double)f2 + d4 * (double)f1;
            entityIn.rotationYaw = rotationYaw - (float)(entityIn.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (float)(blockpattern$patternhelper.getForwards().getHorizontalIndex() * 90);

            if (entityIn instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP)entityIn).connection.setPlayerLocation(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
            } else {
                entityIn.setLocationAndAngles(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
            }

            return true;
        } else {
            return false;
        }
    }

    private static void placeInPortal(ForgeMaterialData portalMaterial, WorldServer destinationWorld, Entity entityIn, float rotationYaw, Teleporter _this)
    {
        if (destinationWorld.provider.getDimensionType().getId() != 1) // If not End
        {
            if (!placeInExistingPortal(destinationWorld, entityIn, rotationYaw, _this))
            {
            	makePortal(portalMaterial, destinationWorld, entityIn, _this);
            	placeInExistingPortal(destinationWorld, entityIn, rotationYaw, _this);
            }
        } else {
    		throw new RuntimeException("This shouldn't happen. Please contact team OTG about this crash."); // TODO: Does this ever happen?
        }
    }

    private static boolean makePortal(ForgeMaterialData portalMaterial, WorldServer destinationWorld, Entity entityIn, Teleporter _this)
    {
        double d0 = -1.0D;
        int j = MathHelper.floor(entityIn.posX);
        int k = MathHelper.floor(entityIn.posY);
        int l = MathHelper.floor(entityIn.posZ);
        int i1 = j;
        int j1 = k;
        int k1 = l;
        int l1 = 0;
        int i2 = new Random().nextInt(4);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j2 = j - 16; j2 <= j + 16; ++j2)
        {
            double d1 = (double)j2 + 0.5D - entityIn.posX;

            for (int l2 = l - 16; l2 <= l + 16; ++l2)
            {
                double d2 = (double)l2 + 0.5D - entityIn.posZ;
                label146:

                for (int j3 = destinationWorld.getActualHeight() - 1; j3 >= 0; --j3)
                {
                    if (destinationWorld.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3, l2)))
                    {
                        while (j3 > 0 && destinationWorld.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3 - 1, l2)))
                        {
                            --j3;
                        }

                        for (int k3 = i2; k3 < i2 + 4; ++k3)
                        {
                            int l3 = k3 % 2;
                            int i4 = 1 - l3;

                            if (k3 % 4 >= 2)
                            {
                                l3 = -l3;
                                i4 = -i4;
                            }

                            for (int j4 = 0; j4 < 3; ++j4)
                            {
                                for (int k4 = 0; k4 < 4; ++k4)
                                {
                                    for (int l4 = -1; l4 < 4; ++l4)
                                    {
                                        int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
                                        int j5 = j3 + l4;
                                        int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
                                        blockpos$mutableblockpos.setPos(i5, j5, k5);

                                        if (l4 < 0 && !destinationWorld.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid() || l4 >= 0 && !destinationWorld.isAirBlock(blockpos$mutableblockpos))
                                        {
                                            continue label146;
                                        }
                                    }
                                }
                            }

                            double d5 = (double)j3 + 0.5D - entityIn.posY;
                            double d7 = d1 * d1 + d5 * d5 + d2 * d2;

                            if (d0 < 0.0D || d7 < d0)
                            {
                                d0 = d7;
                                i1 = j2;
                                j1 = j3;
                                k1 = l2;
                                l1 = k3 % 4;
                            }
                        }
                    }
                }
            }
        }

        if (d0 < 0.0D)
        {
            for (int l5 = j - 16; l5 <= j + 16; ++l5)
            {
                double d3 = (double)l5 + 0.5D - entityIn.posX;

                for (int j6 = l - 16; j6 <= l + 16; ++j6)
                {
                    double d4 = (double)j6 + 0.5D - entityIn.posZ;
                    label567:

                    for (int i7 = destinationWorld.getActualHeight() - 1; i7 >= 0; --i7)
                    {
                        if (destinationWorld.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7, j6)))
                        {
                            while (i7 > 0 && destinationWorld.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7 - 1, j6)))
                            {
                                --i7;
                            }

                            for (int k7 = i2; k7 < i2 + 2; ++k7)
                            {
                                int j8 = k7 % 2;
                                int j9 = 1 - j8;

                                for (int j10 = 0; j10 < 4; ++j10)
                                {
                                    for (int j11 = -1; j11 < 4; ++j11)
                                    {
                                        int j12 = l5 + (j10 - 1) * j8;
                                        int i13 = i7 + j11;
                                        int j13 = j6 + (j10 - 1) * j9;
                                        blockpos$mutableblockpos.setPos(j12, i13, j13);

                                        if (j11 < 0 && !destinationWorld.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid() || j11 >= 0 && !destinationWorld.isAirBlock(blockpos$mutableblockpos))
                                        {
                                            continue label567;
                                        }
                                    }
                                }

                                double d6 = (double)i7 + 0.5D - entityIn.posY;
                                double d8 = d3 * d3 + d6 * d6 + d4 * d4;

                                if (d0 < 0.0D || d8 < d0)
                                {
                                    d0 = d8;
                                    i1 = l5;
                                    j1 = i7;
                                    k1 = j6;
                                    l1 = k7 % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int i6 = i1;
        int k2 = j1;
        int k6 = k1;
        int l6 = l1 % 2;
        int i3 = 1 - l6;

        if (l1 % 4 >= 2)
        {
            l6 = -l6;
            i3 = -i3;
        }

        if (d0 < 0.0D)
        {
            j1 = MathHelper.clamp(j1, 70, destinationWorld.getActualHeight() - 10);
            k2 = j1;

            for (int j7 = -1; j7 <= 1; ++j7)
            {
                for (int l7 = 1; l7 < 3; ++l7)
                {
                    for (int k8 = -1; k8 < 3; ++k8)
                    {
                        int k9 = i6 + (l7 - 1) * l6 + j7 * i3;
                        int k10 = k2 + k8;
                        int k11 = k6 + (l7 - 1) * i3 - j7 * l6;
                        boolean flag = k8 < 0;
                        destinationWorld.setBlockState(new BlockPos(k9, k10, k11), flag ? portalMaterial.internalBlock() : Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        IBlockState iblockstate = Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, l6 == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);

        for (int i8 = 0; i8 < 4; ++i8)
        {
            for (int l8 = 0; l8 < 4; ++l8)
            {
                for (int l9 = -1; l9 < 4; ++l9)
                {
                    int l10 = i6 + (l8 - 1) * l6;
                    int l11 = k2 + l9;
                    int k12 = k6 + (l8 - 1) * i3;
                    boolean flag1 = l8 == 0 || l8 == 3 || l9 == -1 || l9 == 3;
                    destinationWorld.setBlockState(new BlockPos(l10, l11, k12), flag1 ? portalMaterial.internalBlock() : iblockstate, 2);
                }
            }
        }

        return true;
    }
}
