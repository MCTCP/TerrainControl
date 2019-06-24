package com.pg85.otg.forge.events;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.network.server.ServerPacketManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerTracker
{
    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
    	// Only do this for MP, to clear configs / registries
    	// For SP the server shuts down after the client disconnects, so we can't unregister biome yet(?)
    	if(!event.getManager().isLocalChannel())
    	{
	    	OTG.setDimensionsConfig(null);
	    	((ForgeEngine)OTG.getEngine()).UnloadAndUnregisterAllWorlds();
    	}
    }
    
    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
    	// Only do this for MP, to clear any configs / registries that may 
    	// still be present, although there technically shouldn't be any!
    	if(!event.getManager().isLocalChannel())
    	{
	    	OTG.setDimensionsConfig(null);
	    	((ForgeEngine)OTG.getEngine()).UnloadAndUnregisterAllWorlds();
    	}
    }
	
    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
   		ServerPacketManager.SendPacketsOnConnect(event);
    }

	@SubscribeEvent
	public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player.world.provider instanceof OTGWorldProvider)
		{
	    	if(((OTGWorldProvider)event.player.world.provider).getWelcomeMessage() != null && ((OTGWorldProvider)event.player.world.provider).getWelcomeMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((OTGWorldProvider)event.player.world.provider).getWelcomeMessage()));
	    	}

	    	// TODO: Add feature for giving players items via world config + nbt file

	    	DimensionConfig dimConfig = ((OTGWorldProvider)event.player.world.provider).GetDimensionConfig();

			String itemsToRemoveString = dimConfig != null ? dimConfig.Settings.ItemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = dimConfig != null ? dimConfig.Settings.ItemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsAdded = GiveItemsToPlayer(itemsToAddString, event.player);

	    	if(itemsRemoved)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
	    	}
	    	if(itemsAdded)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been added to your inventory"));
	    	}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(event.player.world.provider instanceof OTGWorldProvider)
		{
	    	if(((OTGWorldProvider)event.player.world.provider).getDepartMessage() != null && ((OTGWorldProvider)event.player.world.provider).getDepartMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((OTGWorldProvider)event.player.world.provider).getDepartMessage()));
	    	}

			DimensionConfig dimConfig = ((OTGWorldProvider)event.player.world.provider).GetDimensionConfig();

			String itemsToRemoveString = dimConfig != null ? dimConfig.Settings.ItemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = dimConfig != null ? dimConfig.Settings.ItemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
			boolean itemsAdded = GiveItemsToPlayer(itemsToAddString, event.player);

	    	if(itemsRemoved)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
	    	}
	    	if(itemsAdded)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been added to your inventory"));
	    	}
		}
	}

	@SubscribeEvent
	public void playerRespawned(PlayerEvent.PlayerRespawnEvent event)
	{
		if(event.player.world.provider instanceof OTGWorldProvider)
		{
	    	// TODO: Add feature for giving players items via world config + nbt file

			DimensionConfig dimConfig = ((OTGWorldProvider)event.player.world.provider).GetDimensionConfig();

			String itemsToAddString = dimConfig != null ? dimConfig.Settings.ItemsToAddOnRespawn : WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue();
			boolean itemsAdded = GiveItemsToPlayer(itemsToAddString, event.player);

	    	if(itemsAdded)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been added to your inventory"));
	    	}
		}
	}

	@SubscribeEvent
	public void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		WorldServer fromWorld = DimensionManager.getWorld(event.fromDim);

		if(fromWorld != null)
		{
			if(fromWorld.provider instanceof OTGWorldProvider)
			{
		    	if(((OTGWorldProvider)fromWorld.provider).getDepartMessage() != null && ((OTGWorldProvider)fromWorld.provider).getDepartMessage().trim().length() > 0)
		    	{
		    		event.player.sendMessage(new TextComponentString(((OTGWorldProvider)fromWorld.provider).getDepartMessage()));
		    	}

		    	DimensionConfig dimConfig = ((OTGWorldProvider)fromWorld.provider).GetDimensionConfig();

				String itemsToRemoveString = dimConfig != null ? dimConfig.Settings.ItemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
				boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

				String itemsToAddString = dimConfig != null ? dimConfig.Settings.ItemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
				boolean itemsAdded = GiveItemsToPlayer(itemsToAddString, event.player);

		    	if(itemsRemoved)
		    	{
		    		event.player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
		    	}
		    	if(itemsAdded)
		    	{
		    		event.player.sendMessage(new TextComponentString("Items have been added to your inventory"));
		    	}
			}
		}

		WorldServer toWorld = DimensionManager.getWorld(event.toDim);
		if(toWorld.provider instanceof OTGWorldProvider)
		{
	    	if(((OTGWorldProvider)toWorld.provider).getWelcomeMessage() != null && ((OTGWorldProvider)toWorld.provider).getWelcomeMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((OTGWorldProvider)toWorld.provider).getWelcomeMessage()));
	    	}

	    	// TODO: Add feature for giving players items via world config + nbt file

	    	DimensionConfig dimConfig = ((OTGWorldProvider)toWorld.provider).GetDimensionConfig();

			String itemsToRemoveString = dimConfig != null ? dimConfig.Settings.ItemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = dimConfig != null ? dimConfig.Settings.ItemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsAdded = GiveItemsToPlayer(itemsToAddString, event.player);

	    	if(itemsRemoved)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been removed from your inventory"));
	    	}
	    	if(itemsAdded)
	    	{
	    		event.player.sendMessage(new TextComponentString("Items have been added to your inventory"));
	    	}
		}
	}

    boolean GiveItemsToPlayer(String itemsToAddString, EntityPlayer player)
    {
    	boolean itemsAdded = false;
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
		return itemsAdded;
    }

    void GiveItemToPlayer(EntityPlayer entityplayer, String itemName, String amountString, String metaDataString, String nbtDataString)
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

    boolean RemoveItemsFromPlayer(String itemsToRemoveString, EntityPlayer player)
    {
		boolean itemsRemoved = false;
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
		return itemsRemoved;
    }

    void RemoveItemFromPlayer(EntityPlayer entityplayer, String itemName, String amountString, String metaDataString, String nbtDataString)
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
			amount = CommandBase.parseInt(amountString, -1, item.getItemStackLimit());
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

    int clearMatchingItems(InventoryPlayer _this, @Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT)
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
    int clearMatchingItemsEnderChest(InventoryEnderChest inventory, @Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT)
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
    
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event)
	{
		if(event.getEntity().getEntityWorld() != null)
		{
			if(OTG.getDimensionsConfig() == null)
			{
				// Can happen for Forge clients connecting to a bukkit server
				return;
			}
			
			// ForgeWorld can be null for vanilla overworld / other mods dims
			ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(event.getEntity().getEntityWorld());
			if(forgeWorld != null)
			{
				DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName());
				
				// Calculate the new distance based on gravity
				double baseGravity = 0.08D;				
				double gravityFactor = 1d / (baseGravity / dimConfig.Settings.GravityFactor);
	
				// MC subtracts the default fall damage threshhold (3) from the distance
				double baseThreshHold = 3D;
				double newThreshold = baseThreshHold * (1d / gravityFactor);
				double newDistance = ((event.getDistance() + 3) * gravityFactor) - newThreshold; 
				
				event.setDamageMultiplier((float)gravityFactor);
				event.setDistance((float)newDistance);
				event.setResult(Result.ALLOW);
			}
		}
	}
}
