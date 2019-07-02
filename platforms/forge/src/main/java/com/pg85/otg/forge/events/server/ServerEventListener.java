package com.pg85.otg.forge.events.server;

import java.io.File;
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
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.bo3.bo3function.EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.SpawnerFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.forge.util.WorldHelper;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.forge.world.OTGWorldType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

public class ServerEventListener
{	
    public static void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
    	// Default settings are not restored on world unload / server quit 
    	// because this was causing problems (unloading dimensions while 
    	// their worlds were still ticking etc).
    	// Unload all world and biomes on server start / connect instead, 
    	// for SP client where data is kept when leaving the game.
 
    	((ForgeEngine)OTG.getEngine()).getWorldLoader().unloadAndUnregisterAllWorlds();
    	ForgeEngine.loadPresets();
    }

    public static void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new OTGCommandHandler());

        World overWorld = DimensionManager.getWorld(0);

        if(overWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator") && !(overWorld.getWorldInfo().getTerrainType() instanceof OTGWorldType))
        {
            ISaveHandler isavehandler = overWorld.getSaveHandler();
            WorldInfo worldInfo = isavehandler.loadWorldInfo();

            if(worldInfo != null)
            {
            	overWorld.getWorldInfo().setTerrainType(OTGPlugin.OtgWorldType);
            	worldInfo.setTerrainType(OTGPlugin.OtgWorldType);
            	isavehandler.saveWorldInfo(worldInfo);
            }
            throw new RuntimeException("OTG has detected that you are loading an OTG world that has been used without OTG installed. OTG has fixed and saved the world data, you can now restart the game and enter the world.");
        }

		if(!overWorld.isRemote) // Server side only
		{
			// This is a vanilla overworld, a new OTG world or a legacy OTG world without a dimensionconfig
		    if(OTG.getDimensionsConfig() == null)
		    {
				// Check if there is a dimensionsConfig saved for this world
				DimensionsConfig dimsConfig = DimensionsConfig.loadFromFile(overWorld.getSaveHandler().getWorldDirectory());
				if(dimsConfig == null)
				{
					// If there is no DimensionsConfig saved for this world, create one
					// LoadCustomDimensionData will add dimensions if any were saved
					dimsConfig = new DimensionsConfig(overWorld.getSaveHandler().getWorldDirectory());
					// If this is a vanilla overworld then we can be sure no dimensions were saved,
					if(!overWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator"))
					{
						// Create a dummy overworld config
						dimsConfig.Overworld = new DimensionConfig();
						// Check if there is a modpack config for vanilla worlds, 
						// since we didn't get a chance to check via the OTG world creation UI
						DimensionsConfig modPackConfig = DimensionsConfig.getModPackConfig(null);
						if(modPackConfig != null)
						{
							dimsConfig.Overworld = modPackConfig.Overworld;
							dimsConfig.Dimensions = modPackConfig.Dimensions;
						}
					}
					dimsConfig.save();
				}
				OTG.setDimensionsConfig(dimsConfig);
			}
	
		    // Load any saved dimensions.
		    OTGDimensionManager.LoadCustomDimensionData();
	
		    for(DimensionConfig dimConfig : OTG.getDimensionsConfig().Dimensions)
		    {
		    	if(!OTGDimensionManager.isDimensionNameRegistered(dimConfig.PresetName))
	    		{
		    		File worldConfigFile = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + "/" + PluginStandardValues.PresetsDirectoryName + "/" + dimConfig.PresetName + "/WorldConfig.ini");
		    		if(!worldConfigFile.exists())
		    		{
		    			OTG.log(LogMarker.WARN, "Could not create dimension \"" + dimConfig.PresetName + "\", OTG preset " + dimConfig.PresetName + " could not be found or does not contain a WorldConfig.ini file.");
		    		} else {
		    			OTG.IsNewWorldBeingCreated = true;
		    			OTGDimensionManager.createDimension(dimConfig.PresetName, false, true, false);
		    			OTG.IsNewWorldBeingCreated = false;
		    		}
	    		}
		    }

		    OTGDimensionManager.SaveDimensionData();
		}
    }
}