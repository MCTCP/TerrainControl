package com.pg85.otg.forge.events;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nullable;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigToNetworkSender;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.DimensionData;
import com.pg85.otg.forge.dimensions.OTGDimensionInfo;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.WorldProviderOTG;
import com.pg85.otg.forge.network.DimensionSyncPacket;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.ParticlesPacket;
import com.pg85.otg.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerTracker
{
    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
		ByteBuf nettyBuffer = createWorldAndBiomeConfigsPacket();
		if(nettyBuffer != null)
		{
			PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer), event.getManager());
	    	// Reset particles in case the player just switched worlds.
	    	PacketDispatcher.sendTo(new ParticlesPacket(), event.getManager());
		} else {
			OTG.log(LogMarker.WARN, "Could not find an OTG overworld, OTG is disabled. It is currently not possible to use OTG with a non-OTG overworld. To enable OTG make sure that level-type=OTG is configured in the server.properties file.");
		}
    }

    // Used when creating / deleting dimensions
    public static void SendAllWorldAndBiomeConfigsToAllPlayers(MinecraftServer server)
    {
		ByteBuf nettyBuffer = createWorldAndBiomeConfigsPacket();
		if(nettyBuffer != null)
		{
	    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
	    	{
	        	PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer), (EntityPlayerMP) player);
	    	}
		}
    }

    private static ByteBuf createWorldAndBiomeConfigsPacket()
    {
        // Make sure worlds are sent in the correct order.

		OTGDimensionInfo otgDimData = OTGDimensionManager.GetOrderedDimensionData();

        // Serialize it
        ByteBuf nettyBuffer = Unpooled.buffer();
        //PacketBuffer mojangBuffer = new PacketBuffer(nettyBuffer);
        DataOutput stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	stream.writeInt(PluginStandardValues.ProtocolVersion);
        	stream.writeInt(0); // 0 == Normal packet
        	stream.writeInt(otgDimData.orderedDimensions.size() + 1); // Number of worlds in this packet

    		// Send worldconfig and biomeconfigs for each world.

			LocalWorld localWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();

			if(localWorld == null)
			{
				// This is not an OTG world.
				((ByteBufOutputStream)stream).close();
				return null;
			}

			// Overworld (dim 0)
	        try
	        {
	        	stream.writeInt(0);
	            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false);
	        }
	        catch (IOException e)
	        {
	            OTG.printStackTrace(LogMarker.FATAL, e);
	        }

    		for(int i = 0; i <= otgDimData.highestOrder; i++)
    		{
    			if(otgDimData.orderedDimensions.containsKey(i))
    			{
    				DimensionData dimData = otgDimData.orderedDimensions.get(i);
    				localWorld = OTG.getWorld(dimData.dimensionName);
    				if(localWorld == null)
    				{
    					localWorld = OTG.getUnloadedWorld(dimData.dimensionName);
    				}

    		        try
    		        {
    		        	stream.writeInt(dimData.dimensionId);
    		            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false); // TODO: localWorld is null after /otg dim -c
    		        }
    		        catch (IOException e)
    		        {
    		            OTG.printStackTrace(LogMarker.FATAL, e);
    		        }
    			}
    		}
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		}

        return nettyBuffer;
    }

	@SubscribeEvent
	public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player.world.provider instanceof WorldProviderOTG)
		{
	    	if(((WorldProviderOTG)event.player.world.provider).getWelcomeMessage() != null && ((WorldProviderOTG)event.player.world.provider).getWelcomeMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((WorldProviderOTG)event.player.world.provider).getWelcomeMessage()));
	    	}

	    	// TODO: Add feature for giving players items via world config + nbt file

			WorldConfig worldConfig = ((WorldProviderOTG)event.player.world.provider).GetWorldConfig();

			String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
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
		if(event.player.world.provider instanceof WorldProviderOTG)
		{
	    	if(((WorldProviderOTG)event.player.world.provider).getDepartMessage() != null && ((WorldProviderOTG)event.player.world.provider).getDepartMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((WorldProviderOTG)event.player.world.provider).getDepartMessage()));
	    	}

			WorldConfig worldConfig = ((WorldProviderOTG)event.player.world.provider).GetWorldConfig();

			String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
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
		if(event.player.world.provider instanceof WorldProviderOTG)
		{
	    	// TODO: Add feature for giving players items via world config + nbt file

			WorldConfig worldConfig = ((WorldProviderOTG)event.player.world.provider).GetWorldConfig();

			String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnRespawn : WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue();
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
			if(fromWorld.provider instanceof WorldProviderOTG)
			{
		    	if(((WorldProviderOTG)fromWorld.provider).getDepartMessage() != null && ((WorldProviderOTG)fromWorld.provider).getDepartMessage().trim().length() > 0)
		    	{
		    		event.player.sendMessage(new TextComponentString(((WorldProviderOTG)fromWorld.provider).getDepartMessage()));
		    	}

				WorldConfig worldConfig = ((WorldProviderOTG)fromWorld.provider).GetWorldConfig();

				String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
				boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

				String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
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
		if(toWorld.provider instanceof WorldProviderOTG)
		{
	    	if(((WorldProviderOTG)toWorld.provider).getWelcomeMessage() != null && ((WorldProviderOTG)toWorld.provider).getWelcomeMessage().trim().length() > 0)
	    	{
	    		event.player.sendMessage(new TextComponentString(((WorldProviderOTG)toWorld.provider).getWelcomeMessage()));
	    	}

	    	// TODO: Add feature for giving players items via world config + nbt file

			WorldConfig worldConfig = ((WorldProviderOTG)toWorld.provider).GetWorldConfig();

			String itemsToRemoveString = worldConfig != null ? worldConfig.itemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
			boolean itemsRemoved = RemoveItemsFromPlayer(itemsToRemoveString, event.player);

			String itemsToAddString = worldConfig != null ? worldConfig.itemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
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
}
