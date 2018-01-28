package com.pg85.otg.forge.dimensions;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderOTG extends WorldProvider
{
	public WorldConfig config = null;

	private WorldConfig GetWorldConfig()
	{
		if(config == null)
		{
			ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(world);
			if(forgeWorld != null)
			{
				config = forgeWorld.getConfigs().getWorldConfig();
			}
		}
		return config;
	}

	public WorldProviderOTG() { }

	// TODO: This method is no longer used. Seems like a nice feature for dimensions though?
    // A message to display to the user when they transfer to this dimension.
	//@Override
    public String getWelcomeMessage()
    {
		WorldConfig worldConfig = GetWorldConfig();
		return worldConfig != null ? worldConfig.welcomeMessage : WorldStandardValues.welcomeMessage.getDefaultValue();
    }

	// TODO: This method is no longer used. Seems like a nice feature for dimensions though?
	// A Message to display to the user when they transfer out of this dismension.
	//@Override
    public String getDepartMessage()
    {
		WorldConfig worldConfig = GetWorldConfig();
		return worldConfig != null ? worldConfig.departMessage : WorldStandardValues.departMessage.getDefaultValue();
    }

	DimensionType dimType = null;
    public DimensionType getDimensionType()
    {
    	if(dimType == null)
    	{
    		dimType = DimensionManager.getProviderType(this.world.provider.getDimension());
    	}

    	// Some mods (like Optifine) crash if the dimensionType returned is not one of the default ones.
    	// We can't use DimensionType.OVERWORLD though or the ChunkProdivderServer.unloadQueuedChunks won't unload this dimension
    	// This seems to be called often so may cause client lag :(.
    	StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    	if(stackTrace.length > 2)
    	{
    		String className = stackTrace[2].getClassName().toLowerCase();
	    	if(className.contains("customcolors"))
	    	{
	    		return DimensionType.OVERWORLD;
	    	}
    	}

        return dimType;
    }

     // Creates a new {@link BiomeProvider} for the WorldProvider, and also sets the values of {@link #hasSkylight} and
     // {@link #hasNoSky} appropriately.
    protected void init()
    {
        // Creates a new world chunk manager for WorldProvider
    	this.hasSkyLight = true;
        this.biomeProvider = OTGPlugin.txWorldType.getBiomeProvider(world);
    }

    @Override
    public net.minecraft.world.gen.IChunkGenerator createChunkGenerator()
    {
    	return OTGPlugin.txWorldType.getChunkGenerator(world, world.getWorldInfo() instanceof DerivedWorldInfo ? ((DerivedWorldInfo)world.getWorldInfo()).delegate.getGeneratorOptions() : world.getWorldInfo().getGeneratorOptions());
    }

    // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
    @Override
    public boolean isSurfaceWorld()
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.isSurfaceWorld : WorldStandardValues.isSurfaceWorld.getDefaultValue();
    }

    // Will check if the x, z position specified is alright to be set as the map spawn point
    @Override
    public boolean canCoordinateBeSpawn(int x, int z)
    {
        return false; // TODO: Make spawn pos detection method? (make sure it doesn't screw up BO3AtSpawn and cartographer in TXChunkGenerator)
    }

    @Override
    public BlockPos getRandomizedSpawnPoint()
    {
    	return super.getRandomizedSpawnPoint();
    }

    // True if the player can respawn in this dimension (true = overworld, false = nether).
    @Override
    public boolean canRespawnHere()
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.canRespawnHere : WorldStandardValues.canRespawnHere.getDefaultValue();
    }

    // Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.
    // @param entity The entity holding the map, playername, or frame-ENTITYID
    // @param x X Position
    // @param y Y Position
	// @param z Z Position
    // @return True to 'spin' the cursor
    @Override
    public boolean shouldMapSpin(String entity, double x, double y, double z)
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.shouldMapSpin : super.shouldMapSpin(entity, x, y, z);
    }

    @Override
    public boolean isDaytime()
    {
    	return super.isDaytime();
    }

    @Override
    public int getHeight()
    {
        return super.getHeight();
    }

    @Override
    public int getActualHeight()
    {
        return super.getActualHeight();
    }

    // Return Vec3D with biome specific fog color
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_)
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null && worldConfig.useCustomFogColor ? new Vec3d(worldConfig.fogColorRed, worldConfig.fogColorGreen, worldConfig.fogColorBlue) : super.getFogColor(p_76562_1_, p_76562_2_);
    }

    // Returns true if the given X,Z coordinate should show environmental fog.
    // TODO: Is this really needed to make fog work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z)
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.doesXZShowFog : WorldStandardValues.doesXZShowFog.getDefaultValue();
    }

    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public double getVoidFogYFactor()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.voidFogYFactor : WorldStandardValues.voidFogYFactor.getDefaultValue();
    }

    // TODO: Is this really needed to make sky colors work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored()
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.isSkyColored : WorldStandardValues.isSkyColored.getDefaultValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks)
    {
    	return super.getSkyColor(cameraEntity, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getCloudColor(float partialTicks)
    {
        return super.getCloudColor(partialTicks);
    }

    // the y level at which clouds are rendered.
    @SideOnly(Side.CLIENT)
    @Override
    public float getCloudHeight()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.cloudHeight : WorldStandardValues.cloudHeight.getDefaultValue();
    }

    @Override
    public int getAverageGroundLevel()
    {
    	WorldConfig worldConfig = GetWorldConfig();
   		return worldConfig != null ? worldConfig.waterLevelMax + 1 : this.world.getSeaLevel() + 1; // Sea level + 1 by default
    }

    // Called to determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used
    // in WorldProviderSurface to prevent spawn chunks from being unloaded.
    @Override
    public boolean canDropChunk(int x, int z)
    {
    	if(this.getDimension() != 0) // Never unload Overworld
    	{
	    	WorldConfig worldConfig = GetWorldConfig();
	        return worldConfig != null ? worldConfig.canDropChunk : WorldStandardValues.canDropChunk.getDefaultValue();
    	} else {
    		return !this.world.isSpawnChunk(x, z) || !this.world.provider.getDimensionType().shouldLoadSpawn();
    	}
    }

    // Creates the light to brightness table
    @Override
    protected void generateLightBrightnessTable()
    {
    	WorldConfig worldConfig = GetWorldConfig();
		if(worldConfig != null && worldConfig.isNightWorld)
    	{
	        for (int i = 0; i <= 15; ++i)
	        {
	            //float f1 = 1.0F - (float)i / 15.0F;
	        	float f1 = 0.0F;
	            this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 0.9F + 0.1F;
	        }
    	} else {
    		super.generateLightBrightnessTable();
    	}
    }


	@Override
    public WorldBorder createWorldBorder()
    {
		return super.createWorldBorder();
    }

    // Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks)
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	if(worldConfig != null && worldConfig.isNightWorld)
    	{
    		return 0.49837038f;
    	} else {
    		return super.calculateCelestialAngle(worldTime, partialTicks);
    	}
    }

    @Override
    public double getHorizon()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.waterLevelMax : this.world.getSeaLevel();
    }

    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk)
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.canDoLightning : WorldStandardValues.canDoLightning.getDefaultValue();
    }

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk)
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.canDoRainSnowIce : WorldStandardValues.canDoRainSnowIce.getDefaultValue();
    }

    // This only affects lillies, glass bottles and a few other unimportant things?
    //@Override
    //public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player, BlockPos pos)
    //{
    	//WorldConfig worldConfig = GetWorldConfig();
    	//return worldConfig != null ? worldConfig.canMineBlock && super.canMineBlock(player, pos) : super.canMineBlock(player, pos);
    //}

    @Override
    public boolean doesWaterVaporize()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.doesWaterVaporize : WorldStandardValues.doesWaterVaporize.getDefaultValue();
    }

    @Override
    public boolean hasSkyLight()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.hasSkyLight : WorldStandardValues.hasSkyLight.getDefaultValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public net.minecraftforge.client.IRenderHandler getSkyRenderer()
    {
    	return super.getSkyRenderer(); // TODO: Replace this with custom sky renderer?
    }

    /**
     * The dimension's movement factor.
     * Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by
     * worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor()
     * Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.
     * @return The movement factor
     */
    @Override
    public double getMovementFactor()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.movementFactor : WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue();
    }

    /**
     * Determines the dimension the player will be respawned in, typically this brings them back to the overworld.
     *
     * @param player The player that is respawning
     * @return The dimension to respawn the player in
     */
    @Override
    public int getRespawnDimension(net.minecraft.entity.player.EntityPlayerMP player)
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? !worldConfig.canRespawnHere ? worldConfig.respawnDimension : super.getRespawnDimension(player) : super.getRespawnDimension(player);
    }

    /**
     * Called when a Player is added to the provider's world.
     */
    public void onPlayerAdded(EntityPlayerMP player)
    {
    	if(getWelcomeMessage() != null && getWelcomeMessage().trim().length() > 0)
    	{
    		player.sendMessage(new TextComponentString(getWelcomeMessage()));
    	}

    	// TODO: Add feature for giving players items via world config + nbt file

		WorldConfig worldConfig = GetWorldConfig();

		boolean itemsRemoved = false;
		String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
		if(itemsToRemoveString != null)
		{
			itemsToRemoveString = itemsToRemoveString.trim();
			if(itemsToRemoveString.length() > 0)
			{
				if(itemsToRemoveString.charAt(0) == '[' && itemsToRemoveString.charAt(itemsToRemoveString.length() - 1) == ']')
				{
					itemsToRemoveString = itemsToRemoveString.substring(1, itemsToRemoveString.length() - 1);
				}

				ArrayList<String> params = new ArrayList<String>();
				int paramStartIndex = 0;
				boolean foundParam = false;
				for(int i = 0; i < itemsToRemoveString.length(); i++)
				{
					if(itemsToRemoveString.charAt(i) == '\"')
					{
						if(!foundParam)
						{
							paramStartIndex = i;
							foundParam = true;
						} else {
							params.add(itemsToRemoveString.substring(paramStartIndex + 1, i));
							foundParam = false;
						}
					}
				}
				for(int i = 0; i < params.size(); i+= 4)
				{
					RemoveItemFromPlayer(player, params.get(i + 0), params.get(i + 1), params.get(i + 2), params.get(i + 3));
	    			//RemoveItemFromPlayer(player, "diamond_sword", "1", "0", "{ench:[{id:16,lvl:5}]}");
	                itemsRemoved = true;
				}
			}
		}

		boolean itemsAdded = false;
		String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
		if(itemsToAddString != null)
		{
			itemsToAddString = itemsToAddString.trim();
			if(itemsToAddString.length() > 0)
			{
				if(itemsToAddString.charAt(0) == '[' && itemsToAddString.charAt(itemsToAddString.length() - 1) == ']')
				{
					itemsToAddString = itemsToAddString.substring(1, itemsToAddString.length() - 1);
				}

				ArrayList<String> params = new ArrayList<String>();
				int paramStartIndex = 0;
				boolean foundParam = false;
				for(int i = 0; i < itemsToAddString.length(); i++)
				{
					if(itemsToAddString.charAt(i) == '\"')
					{
						if(!foundParam)
						{
							paramStartIndex = i;
							foundParam = true;
						} else {
							params.add(itemsToAddString.substring(paramStartIndex + 1, i));
							foundParam = false;
						}
					}
				}
				for(int i = 0; i < params.size(); i+= 4)
				{
					GiveItemToPlayer(player, params.get(i + 0), params.get(i + 1), params.get(i + 2), params.get(i + 3));
	    			//GiveItemToPlayer(player, "diamond_sword", "1", "0", "{ench:[{id:16,lvl:5}]}");
					itemsAdded = true;
				}
			}
		}

    	if(itemsRemoved)
    	{
    		player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
    	}
    	if(itemsAdded)
    	{
    		player.sendMessage(new TextComponentString("Items have been added to your inventory"));
    	}
    }

    /**
     * Called when a Player is removed from the provider's world.
     */
    public void onPlayerRemoved(EntityPlayerMP player)
    {
    	if(getDepartMessage() != null && getDepartMessage().trim().length() > 0)
    	{
    		player.sendMessage(new TextComponentString(getDepartMessage()));
    	}

		WorldConfig worldConfig = GetWorldConfig();

		boolean itemsRemoved = false;
		String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
		if(itemsToRemoveString != null)
		{
			itemsToRemoveString = itemsToRemoveString.trim();
			if(itemsToRemoveString.length() > 0)
			{
				if(itemsToRemoveString.charAt(0) == '[' && itemsToRemoveString.charAt(itemsToRemoveString.length() - 1) == ']')
				{
					itemsToRemoveString = itemsToRemoveString.substring(1, itemsToRemoveString.length() - 1);
				}

				ArrayList<String> params = new ArrayList<String>();
				int paramStartIndex = 0;
				boolean foundParam = false;
				for(int i = 0; i < itemsToRemoveString.length(); i++)
				{
					if(itemsToRemoveString.charAt(i) == '\"')
					{
						if(!foundParam)
						{
							paramStartIndex = i;
							foundParam = true;
						} else {
							params.add(itemsToRemoveString.substring(paramStartIndex + 1, i));
							foundParam = false;
						}
					}
				}
				for(int i = 0; i < params.size(); i+= 4)
				{
					RemoveItemFromPlayer(player, params.get(i + 0), params.get(i + 1), params.get(i + 2), params.get(i + 3));
	    			//RemoveItemFromPlayer(player, "diamond_sword", "1", "0", "{ench:[{id:16,lvl:5}]}");
	                itemsRemoved = true;
				}
			}
		}

		boolean itemsAdded = false;
		String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
		if(itemsToAddString != null)
		{
			itemsToAddString = itemsToAddString.trim();
			if(itemsToAddString.length() > 0)
			{
				if(itemsToAddString.charAt(0) == '[' && itemsToAddString.charAt(itemsToAddString.length() - 1) == ']')
				{
					itemsToAddString = itemsToAddString.substring(1, itemsToAddString.length() - 1);
				}

				ArrayList<String> params = new ArrayList<String>();
				int paramStartIndex = 0;
				boolean foundParam = false;
				for(int i = 0; i < itemsToAddString.length(); i++)
				{
					if(itemsToAddString.charAt(i) == '\"')
					{
						if(!foundParam)
						{
							paramStartIndex = i;
							foundParam = true;
						} else {
							params.add(itemsToAddString.substring(paramStartIndex + 1, i));
							foundParam = false;
						}
					}
				}
				for(int i = 0; i < params.size(); i+= 4)
				{
					GiveItemToPlayer(player, params.get(i + 0), params.get(i + 1), params.get(i + 2), params.get(i + 3));
	    			//GiveItemToPlayer(player, "diamond_sword", "1", "0", "{ench:[{id:16,lvl:5}]}");
					itemsAdded = true;
				}
			}
		}

    	if(itemsRemoved)
    	{
    		player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
    	}
    	if(itemsAdded)
    	{
    		player.sendMessage(new TextComponentString("Items have been added to your inventory"));
    	}
    }

    private void GiveItemToPlayer(EntityPlayerMP entityplayer, String itemName, String amountString, String metaDataString, String nbtDataString)
    {
    	// Taken from CommandGive

        Item item;
		try
		{
			item = CommandBase.getItemByText(null, itemName);
		}
		catch (NumberInvalidException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		int amount;
		try
		{
			amount = CommandBase.parseInt(amountString, 1, item.getItemStackLimit());
		}
		catch (NumberInvalidException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

        int meta = 0;
        if (metaDataString != null && metaDataString.length() > 0)
        {
			try
			{
				meta = CommandBase.parseInt(metaDataString);
			}
			catch (NumberInvalidException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
        }

        ItemStack itemstack = new ItemStack(item, amount, meta);

        if (nbtDataString != null && nbtDataString.length() > 0)
        {
            try
            {
                itemstack.setTagCompound(JsonToNBT.getTagFromJson(nbtDataString));
            }
            catch (NBTException nbtexception)
            {
                try
                {
					throw new CommandException("commands.give.tagError", new Object[] {nbtexception.getMessage()});
				}
                catch (CommandException e)
                {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }

        boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);

        if (flag)
        {
            entityplayer.world.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.inventoryContainer.detectAndSendChanges();
        }

        if (flag && itemstack.isEmpty())
        {
            itemstack.setCount(1);
            EntityItem entityitem1 = entityplayer.dropItem(itemstack, false);

            if (entityitem1 != null)
            {
                entityitem1.makeFakeItem();
            }
        } else {
            EntityItem entityitem = entityplayer.dropItem(itemstack, false);

            if (entityitem != null)
            {
                entityitem.setNoPickupDelay();
                entityitem.setOwner(entityplayer.getName());
            }
        }
    }

    private void RemoveItemFromPlayer(EntityPlayerMP entityplayer, String itemName, String amountString, String metaDataString, String nbtDataString)
    {
    	// Taken from CommandGive

        Item item;
		try
		{
			item = CommandBase.getItemByText(null, itemName);
		}
		catch (NumberInvalidException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		int amount;
		try
		{
			amount = CommandBase.parseInt(amountString, 1, item.getItemStackLimit());
		}
		catch (NumberInvalidException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

        int meta = 0;
        if (metaDataString != null && metaDataString.length() > 0)
        {
			try
			{
				meta = CommandBase.parseInt(metaDataString);
			}
			catch (NumberInvalidException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
        }

        NBTTagCompound nbtTagCompound = null;

        if (nbtDataString != null && nbtDataString.length() > 0)
        {
            try
            {
            	nbtTagCompound = JsonToNBT.getTagFromJson(nbtDataString);
            }
            catch (NBTException nbtexception)
            {
                try
                {
					throw new CommandException("commands.give.tagError", new Object[] {nbtexception.getMessage()});
				}
                catch (CommandException e)
                {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }

        int amountRemoved = clearMatchingItems(entityplayer.inventory, item, meta, amount, nbtTagCompound);
        if(amount == -1 || amountRemoved < amount)
        {
        	amountRemoved += clearMatchingItemsEnderChest(entityplayer.getInventoryEnderChest(), item, meta, amount, nbtTagCompound);
        }
    }

    public int clearMatchingItems(InventoryPlayer _this, @Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT)
    {
        int i = 0;

        for (int j = 0; j < _this.getSizeInventory(); ++j)
        {
            ItemStack itemstack = _this.getStackInSlot(j);

            if(itemstack.getItem() instanceof ItemShulkerBox)
            {
            	if(itemstack.getTagCompound() != null && itemstack.getTagCompound().getCompoundTag("BlockEntityTag") != null)
            	{
	        		NBTTagCompound BlockEntityTag = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");
	        		NBTTagList itemsTag = BlockEntityTag.getTagList("Items", 10);
	        		if(itemsTag != null)
	        		{
		        		for(int l = 0; l < itemsTag.tagCount(); l++)
		        		{
		        			NBTTagCompound item = itemsTag.getCompoundTagAt(l);
		        			String id = item.getString("id");
		        			int count = itemstack.getCount();
		        			NBTTagCompound nbtTag = item.getCompoundTag("tag");
		        			if(
								id.equals(itemIn.getRegistryName().toString()) &&
								nbtTag.equals(itemNBT)
							)
		        			{
		        				if(count - removeCount <= 0)
		        				{
		        					itemsTag.removeTag(l);
		        					i += count;
		        				} else {
		        					itemstack.setCount(count - removeCount);
		        					return removeCount;
	        					}
		        			}
		        		}
	        		}
            	}
            }

            if (
        		!itemstack.isEmpty() &&
        		(
    				itemIn == null ||
    				itemstack.getItem() == itemIn
				) && (
					metadataIn <= -1 ||
					itemstack.getMetadata() == metadataIn
				) && (
					itemNBT == null ||
					NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true)
				)
			)
            {
                int k = removeCount <= 0 ? itemstack.getCount() : Math.min(removeCount - i, itemstack.getCount());
                i += k;

                if (removeCount != 0)
                {
                    itemstack.shrink(k);

                    if (itemstack.isEmpty())
                    {
                    	_this.setInventorySlotContents(j, ItemStack.EMPTY);
                    }

                    if (removeCount > 0 && i >= removeCount)
                    {
                        return i;
                    }
                }
            }
        }

        if (!_this.getItemStack().isEmpty())
        {
            if (itemIn != null && _this.getItemStack().getItem() != itemIn)
            {
                return i;
            }

            if (metadataIn > -1 && _this.getItemStack().getMetadata() != metadataIn)
            {
                return i;
            }

            if (itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, _this.getItemStack().getTagCompound(), true))
            {
                return i;
            }

            int l = removeCount <= 0 ? _this.getItemStack().getCount() : Math.min(removeCount - i, _this.getItemStack().getCount());
            i += l;

            if (removeCount != 0)
            {
            	_this.getItemStack().shrink(l);

                if (_this.getItemStack().isEmpty())
                {
                	_this.setItemStack(ItemStack.EMPTY);
                }

                if (removeCount > 0 && i >= removeCount)
                {
                    return i;
                }
            }
        }

        return i;
    }

    /**
     * Removes matching items from the inventory.
     * @param itemIn The item to match, null ignores.
     * @param metadataIn The metadata to match, -1 ignores.
     * @param removeCount The number of items to remove. If less than 1, removes all matching items.
     * @param itemNBT The NBT data to match, null ignores.
     * @return The number of items removed from the inventory.
     */
    public int clearMatchingItemsEnderChest(InventoryEnderChest inventory, @Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT)
    {
        int i = 0;

        for (int j = 0; j < inventory.getSizeInventory(); ++j)
        {
            ItemStack itemstack = inventory.getStackInSlot(j);

            if(itemstack.getItem() instanceof ItemShulkerBox)
            {
            	if(itemstack.getTagCompound() != null && itemstack.getTagCompound().getCompoundTag("BlockEntityTag") != null)
            	{
	        		NBTTagCompound BlockEntityTag = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");
	        		NBTTagList itemsTag = BlockEntityTag.getTagList("Items", 10);
	        		if(itemsTag != null)
	        		{
		        		for(int l = 0; l < itemsTag.tagCount(); l++)
		        		{
		        			NBTTagCompound item = itemsTag.getCompoundTagAt(l);
		        			String id = item.getString("id");
		        			int count = itemstack.getCount();
		        			NBTTagCompound nbtTag = item.getCompoundTag("tag");
		        			if(
								id.equals(itemIn.getRegistryName().toString()) &&
								nbtTag.equals(itemNBT)
							)
		        			{
		        				if(count - removeCount <= 0)
		        				{
		        					itemsTag.removeTag(l);
		        					i += count;
		        				} else {
		        					itemstack.setCount(count - removeCount);
		        					return removeCount;
	        					}
		        			}
		        		}
	        		}
            	}
            }

            if (!itemstack.isEmpty() && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true)))
            {
                int k = removeCount <= 0 ? itemstack.getCount() : Math.min(removeCount - i, itemstack.getCount());
                i += k;

                if (removeCount != 0)
                {
                    itemstack.shrink(k);

                    if (itemstack.isEmpty())
                    {
                    	inventory.setInventorySlotContents(j, ItemStack.EMPTY);
                    }

                    if (removeCount > 0 && i >= removeCount)
                    {
                        return i;
                    }
                }
            }
        }

        return i;
    }
}