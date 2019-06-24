package com.pg85.otg.forge.network.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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

	public static void SendUpdateDimensionSettingsPacket(ArrayList<DimensionConfig> dimConfigs, boolean isOverWorldIncluded)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	UpdateDimensionSettingsPacket.WriteToStream(stream, dimConfigs, isOverWorldIncluded);
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
		DimensionsConfig dimsConfig = DimensionsConfig.fromYamlString(ConfigFile.readStringFromStream(wrappedStream)); 
		OTG.setDimensionsConfig(dimsConfig);
		
		ForgeEngine.presets.clear();
		int presetCount = wrappedStream.readInt();
		for(int i = 0; i < presetCount; i++)
		{
			DimensionConfigGui dimConfig = DimensionConfigGui.fromYamlString(ConfigFile.readStringFromStream(wrappedStream));
			ForgeEngine.presets.put(dimConfig.PresetName, dimConfig);
		}
		
		int worldCount = wrappedStream.readInt();
		
		HashMap<Integer, String> dimsToRemove = OTGDimensionManager.GetAllOTGDimensions(); // TODO: use String[] instead?
		boolean isSinglePlayer = Minecraft.getMinecraft().isSingleplayer();
		
		for(int i = 0; i < worldCount; i++)
		{
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
			
			// Overworld can be null for MP clients
			if(!DimensionManager.isDimensionRegistered(dimensionId) || (dimensionId == 0 && ((ForgeEngine)OTG.getEngine()).getOverWorld() == null))
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
			// Overworld isn't sent by the server if it's a vanilla world, so it mistakenly ends up in removedDims.
			if(removedDim.getKey() != 0)
			{
				// This dimension has been deleted on the server, remove it
				ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(removedDim.getValue());
				if(forgeWorld == null)
				{
					forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(removedDim.getValue()); // This can happen because the client considers all worlds loaded when it receives them from the server.
				}				
				OTGDimensionManager.DeleteDimension(removedDim.getKey(), forgeWorld, Minecraft.getMinecraft().player.getServer(), false);
			}
		}
		
		if(
			Minecraft.getMinecraft().currentScreen != null
			&& (
				Minecraft.getMinecraft().currentScreen instanceof OTGGuiDimensionList ||
				Minecraft.getMinecraft().currentScreen instanceof OTGGuiPresetList
			)
		)
		{
			if(Minecraft.getMinecraft().currentScreen instanceof OTGGuiDimensionList)
			{
				int previouslySelectedIndex = ((OTGGuiDimensionList)Minecraft.getMinecraft().currentScreen).selectedDimensionIndex;
				boolean isMainMenu = ((OTGGuiDimensionList)Minecraft.getMinecraft().currentScreen).dimensionSettingsList.mainMenu;
				boolean isGameRulesMenu = ((OTGGuiDimensionList)Minecraft.getMinecraft().currentScreen).dimensionSettingsList.gameRulesMenu;
				boolean isAdvancedSettingsMenu = ((OTGGuiDimensionList)Minecraft.getMinecraft().currentScreen).dimensionSettingsList.advancedSettingsMenu;
				float lastScrollPos = ((OTGGuiDimensionList)Minecraft.getMinecraft().currentScreen).dimensionSettingsList.getAmountScrolledFloat();
				Minecraft.getMinecraft().displayGuiScreen(new OTGGuiDimensionList(previouslySelectedIndex, isMainMenu, isGameRulesMenu, isAdvancedSettingsMenu, lastScrollPos));
			} else {
				Minecraft.getMinecraft().displayGuiScreen(new OTGGuiDimensionList(null));
			}
		}
	}
}
