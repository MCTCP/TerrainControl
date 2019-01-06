package com.pg85.otg.forge.network.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.WorldLoader;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.gui.OTGGuiDimensionList;
import com.pg85.otg.forge.gui.OTGGuiPresetList;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.client.packets.CreateDeleteDimensionPacket;
import com.pg85.otg.forge.network.client.packets.UpdateDimensionSettingsPacket;
import com.pg85.otg.forge.network.client.packets.TeleportPlayerPacket;
import com.pg85.otg.network.ClientConfigProvider;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class ClientPacketManager
{    
	// Client to server
	
	public static void SendCreateDimensionPacket(DimensionConfig dimensionConfig)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	CreateDeleteDimensionPacket.WriteCreatePacketToStream(dimensionConfig, stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
        	PacketDispatcher.sendToServer(new CreateDeleteDimensionPacket(nettyBuffer));
		}        
	}

	public static void SendUpdateDimensionSettingsPacket(DimensionConfig dimConfig, boolean isOverWorld)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	UpdateDimensionSettingsPacket.WriteToStream(stream, dimConfig, isOverWorld);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
        	PacketDispatcher.sendToServer(new UpdateDimensionSettingsPacket(nettyBuffer));
		}	
	}
	
	public static void SendDeleteDimensionPacket(String dimensionName)
	{
		ArrayList<LocalWorld> worlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
		
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	CreateDeleteDimensionPacket.WriteDeletePacketToStream(dimensionName, stream);        
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
        	PacketDispatcher.sendToServer(new CreateDeleteDimensionPacket(nettyBuffer));
		}
	}
	
	public static void SendTeleportPlayerPacket(String dimensionName)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	TeleportPlayerPacket.WriteToStream(dimensionName, stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
        	PacketDispatcher.sendToServer(new TeleportPlayerPacket(nettyBuffer));
		}
	}

	public static void RegisterClientWorlds(DataInputStream wrappedStream, WorldLoader worldLoader) throws IOException
	{	
		OTG.SetDimensionsConfig(DimensionsConfig.FromYamlString(ConfigFile.readStringFromStream(wrappedStream)));
		
		ForgeEngine.presets.clear();
		int presetCount = wrappedStream.readInt();
		for(int i = 0; i < presetCount; i++)
		{
			DimensionConfigGui dimConfig = DimensionConfigGui.FromYamlString(ConfigFile.readStringFromStream(wrappedStream));
			ForgeEngine.presets.put(dimConfig.PresetName, dimConfig);
		}
		
		int worldCount = wrappedStream.readInt();
		
		HashMap<Integer, String> dimsToRemove = OTGDimensionManager.GetAllOTGDimensions(); // TODO: use String[] instead?
		boolean isSinglePlayer = Minecraft.getMinecraft().isSingleplayer();
		
		for(int i = 0; i < worldCount; i++)
		{
			// Don't create dimensions on client, only server needs them.
			
			boolean worldIsLoaded = wrappedStream.readBoolean();
			int dimensionId = wrappedStream.readInt();
	
			Integer integerToRemove = 0;
			boolean bFound = false;
			for(Entry<Integer, String> dimToRemove : dimsToRemove.entrySet())
			{
				if(dimToRemove.getKey().intValue() == dimensionId)
				{
					bFound = true;
					integerToRemove = dimToRemove.getKey();
				}
			}
			if(bFound)
			{
				dimsToRemove.remove(integerToRemove);
			}
	
			String worldName = ConfigFile.readStringFromStream(wrappedStream);
			
			// TODO: For integratedServer worlds are shared between client and server? World only needs to be created for MP client, not SP client?			
			if(dimensionId != 0 && !DimensionManager.isDimensionRegistered(dimensionId))
			{
				if(dimensionId != 0)
	    		{
	    			OTGDimensionManager.registerDimension(dimensionId, DimensionType.register(worldName, "OTG", dimensionId, OTGWorldProvider.class, false));
	    		}
	
				ForgeWorld world = new ForgeWorld(worldName);
				world.isLoadedOnServer = worldIsLoaded;
				world.clientDimensionId = dimensionId;
	            ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world, isSinglePlayer);
	            
	            world.provideClientConfigs(configs);
	            worldLoader.LoadClientWorldFromPacket(world);
			} else {
	
				// World already exists, read the data from the stream but don't create a world.
				new ClientConfigProvider(wrappedStream, new ForgeWorld(worldName), isSinglePlayer);
			}
		}
	
		for(Entry<Integer, String> removedDim : dimsToRemove.entrySet())
		{
			// This dimension has been deleted on the server, remove it
			ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(removedDim.getValue());
			if(forgeWorld == null)
			{
				forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(removedDim.getValue()); // This can happen because the client considers all worlds loaded when it receives them from the server.
			}
	
			OTGDimensionManager.DeleteDimension(removedDim.getKey(), forgeWorld, Minecraft.getMinecraft().player.getServer(), false);
		}
		
		if(
			Minecraft.getMinecraft().currentScreen != null
			&& (
				Minecraft.getMinecraft().currentScreen instanceof OTGGuiDimensionList ||
				Minecraft.getMinecraft().currentScreen instanceof OTGGuiPresetList
			)
		)
		{
			Minecraft.getMinecraft().displayGuiScreen(new OTGGuiDimensionList(null));
		}
	}
}
