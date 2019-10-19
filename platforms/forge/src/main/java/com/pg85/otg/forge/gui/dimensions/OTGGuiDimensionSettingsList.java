package com.pg85.otg.forge.gui.dimensions;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.settingType.DoubleSetting;
import com.pg85.otg.configuration.settingType.IntSetting;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.gui.IGuiListEntry;
import com.pg85.otg.forge.pregenerator.Pregenerator;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.util.helpers.StringHelper;

@SideOnly(Side.CLIENT)
public class OTGGuiDimensionSettingsList extends OTGGuiListExtended
{	
	final Minecraft mc;
    private final ArrayList<IGuiListEntry> listEntries;
    private int maxListLabelWidth;

    final OTGGuiDimensionList controlsScreen;
    public boolean mainMenu = true;
    public boolean gameRulesMenu = false;
    public boolean advancedSettingsMenu = false;
	boolean showingPregeneratorStatus;
    
	OTGGuiDimensionSettingsList(OTGGuiDimensionList controls, int top, int height, int left, int width, Minecraft mcIn)
    {
        super(mcIn, left, width, height, top, height, 24);
        this.controlsScreen = controls;
        this.mc = mcIn;
        this.listEntries = new ArrayList<IGuiListEntry>();

        refreshData(true, false, false);
    }
    
    void resize(int topIn, int height, int left, int width)
    {
        this.width = width;
        this.height = height;
        this.top = topIn;
        this.bottom = height;
        this.left = left;
        this.right = left + width;
    }
    
    void refreshData()
    {
    	refreshData(this.mainMenu, this.gameRulesMenu, this.advancedSettingsMenu);
    }

    void refreshData(boolean mainMenu, boolean gameRulesMenu, boolean advancedSettingsMenu)
    {
    	this.showingPregeneratorStatus = false;
    	this.mainMenu = mainMenu;
    	this.gameRulesMenu = gameRulesMenu;
    	this.advancedSettingsMenu = advancedSettingsMenu;

    	// TODO: Use const strings instead of hardcoding the same string in multiple places
    	
    	this.listEntries.clear();
        
    	DimensionConfig dimConfig = this.controlsScreen.selectedDimension;
        
        boolean isBeingCreatedOnServer = false;
        // This dimension config was created via the ingame UI, 
        // but the dimension itself hasn't been created yet (is done when pressing continue/apply)
        if(!dimConfig.isNewConfig) 
        {
            // If we're ingame in MP then find out if the dimension is being created, 
        	// getWorld only fetches loaded worlds. If world isn't null then we're ingame
        	if(this.mc.world != null && !this.mc.isSingleplayer())
        	{            		
        		// For MP get the loaded status from the ForgeWorld, set by a packet from the server.
	            ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(dimConfig.PresetName);
	            isBeingCreatedOnServer = this.controlsScreen.selectedDimensionIndex != 0 && forgeWorld == null;
        	}
        }    	
        if(isBeingCreatedOnServer)
        {
        	listEntries.add(new CategoryEntry(this, ""));
        	listEntries.add(new CategoryEntry(this, ""));
    		listEntries.add(new CategoryEntry(this, "Creating world..."));
        }
        else if(this.controlsScreen.selectedDimension.PresetName == null) // If preset is null then this is a vanilla overworld
    	{
    		listEntries.add(new CategoryEntry(this, "OTG settings"));
    		listEntries.add(new KeyEntry(this, new SettingEntry<String>("Portal materials", StringHelper.join(dimConfig.Settings.DimensionPortalMaterials, ", "), "DIRT", false), this)); // TODO: Fetch default value from worldstandarvalues
	        listEntries.add(new CategoryEntry(this, ""));
	        
	        // If world is not null then were ingame
	        // Don't show teleport button if we're in this world
	        if(this.mc.world != null && this.mc.world.provider.getDimension() != 0) 
	        {
	        	listEntries.add(new ButtonEntry(this, this, "Teleport"));
	        }
    	} else {
	        
    		// If a modder has added default values for this preset then use those, otherwise use the worldconfig
    		DimensionConfig defaultConfig = null;
    		if(this.mc.isSingleplayer())
    		{
		        // If this.mc.world is not null then we're ingame
		        DimensionsConfig defaultConfigs = DimensionsConfig.getModPackConfig(this.mc.world != null ? OTG.getDimensionsConfig().Overworld.PresetName : this.controlsScreen.previousMenu.selectedPreset.getSecond().PresetName);
		        defaultConfig = defaultConfigs != null ? defaultConfigs.getDimensionConfig(this.controlsScreen.selectedDimension.PresetName) : null;        
		        if(defaultConfig == null)
		        {
		        	// Get the default values from the world config, stored in presets
	        		defaultConfig = new DimensionConfig(ForgeEngine.Presets.get(this.controlsScreen.selectedDimension.PresetName));
		        }
    		}
        	// TODO: Use const strings instead of hardcoding the same string in multiple places
	        if(mainMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = this.mc.world == null || !this.mc.isSingleplayer() ? "Cancel" : "Back to game"; // If world is not null then we're ingame
		        listEntries.add(new CategoryEntry(this, "World settings"));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("Preset", dimConfig.PresetName, defaultConfig != null ? defaultConfig.PresetName : null, true), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("Seed", dimConfig.Seed, "", true), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("Game type", dimConfig.GameType, "Survival", true, true), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("Bonus chest", dimConfig.BonusChest, false, true, true), this, !dimConfig.GameType.equals("Hardcore")));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("Allow cheats", dimConfig.AllowCheats, false, true, true), this, !dimConfig.GameType.equals("Hardcore")));
		        listEntries.add(new CategoryEntry(this, "OTG settings"));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("Pregenerator radius", dimConfig.PregeneratorRadiusInChunks, defaultConfig != null ? defaultConfig.PregeneratorRadiusInChunks : WorldStandardValues.PREGENERATION_RADIUS.getDefaultValue(), ((IntSetting)WorldStandardValues.PREGENERATION_RADIUS).getMinValue(), ((IntSetting)WorldStandardValues.PREGENERATION_RADIUS).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("World border radius", dimConfig.WorldBorderRadiusInChunks, defaultConfig != null ? defaultConfig.WorldBorderRadiusInChunks : WorldStandardValues.WORLD_BORDER_RADIUS.getDefaultValue(), ((IntSetting)WorldStandardValues.WORLD_BORDER_RADIUS).getMinValue(), ((IntSetting)WorldStandardValues.WORLD_BORDER_RADIUS).getMaxValue(), true), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("Portal materials", StringHelper.join(dimConfig.Settings.DimensionPortalMaterials, ", "), defaultConfig != null ? StringHelper.join(defaultConfig.Settings.DimensionPortalMaterials, ", ") : "DIRT", false), this)); // TODO: Fetch default value from worldstandarvalues
		        
		        // If world is not null then we're ingame
		        if(this.mc.world != null)
		        {
					ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.controlsScreen.selectedDimension.PresetName);
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.controlsScreen.selectedDimension.PresetName);
					}
					
					// WorldSession can be null on clients when the ForgeWorld has been created but hasn't had a world instance provided yet
					// ForgeWorld can be null for the SP world creation menu
					if(forgeWorld != null && forgeWorld.getWorldSession() != null) 
					{
						Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.getWorldSession()).getPregenerator();
			
						if(pregenerator.getPregeneratorIsRunning() && !pregenerator.preGeneratorProgressStatus.equals("Done"))
				    	{	
							this.showingPregeneratorStatus = true;
					        listEntries.add(new PregeneratorSettingsEntry(this, this));
					        listEntries.add(new CategoryEntry(this, ""));
					        listEntries.add(new CategoryEntry(this, ""));
				    	}
					}
		        }
		        
		        listEntries.add(new CategoryEntry(this, ""));
		        if(this.mc.world != null && !dimConfig.isNewConfig) // If world is not null then were ingame
		        {
		        	// Don't show teleport button for current world
		        	ForgeWorld loadedWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.mc.world);
		        	DimensionConfig selectedDim = this.controlsScreen.selectedDimension;
		        	boolean isCurrentWorld = (
		        		(
	        				this.mc.world.provider.getDimension() == 0 && 
	        				this.controlsScreen.selectedDimensionIndex == 0
        				) ||
		        		(
	        				this.mc.world.provider.getDimension() != 0 &&
		        			selectedDim.PresetName.equals(loadedWorld.getName())
	        			)
        			);
		        	// The player's current dimensions is always loaded
		        	if(loadedWorld == null || !isCurrentWorld)
		        	{
		        		listEntries.add(new ButtonEntry(this, this, "Teleport"));		        		 
		        	}		        	
		        }
		        listEntries.add(new ButtonEntry(this, this, "Game rules"));
		        listEntries.add(new ButtonEntry(this, this, "Advanced settings"));
	        }
	        else if(gameRulesMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = "Back";
		        listEntries.add(new CategoryEntry(this, "Game rules"));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("CommandBlockOutput", dimConfig.GameRules.CommandBlockOutput, defaultConfig != null ? defaultConfig.GameRules.CommandBlockOutput : WorldStandardValues.CommandBlockOutput.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DisableElytraMovementCheck", dimConfig.GameRules.DisableElytraMovementCheck, defaultConfig != null ? defaultConfig.GameRules.DisableElytraMovementCheck : WorldStandardValues.DisableElytraMovementCheck.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoDaylightCycle", dimConfig.GameRules.DoDaylightCycle, defaultConfig != null ? defaultConfig.GameRules.DoDaylightCycle : WorldStandardValues.DoDaylightCycle.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoEntityDrops", dimConfig.GameRules.DoEntityDrops, defaultConfig != null ? defaultConfig.GameRules.DoEntityDrops : WorldStandardValues.DoEntityDrops.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoFireTick", dimConfig.GameRules.DoFireTick, defaultConfig != null ? defaultConfig.GameRules.DoFireTick : WorldStandardValues.DoFireTick.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoLimitedCrafting", dimConfig.GameRules.DoLimitedCrafting, defaultConfig != null ? defaultConfig.GameRules.DoLimitedCrafting : WorldStandardValues.DoLimitedCrafting.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoMobLoot", dimConfig.GameRules.DoMobLoot, defaultConfig != null ? defaultConfig.GameRules.DoMobLoot : WorldStandardValues.DoMobLoot.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoMobSpawning", dimConfig.GameRules.DoMobSpawning, defaultConfig != null ? defaultConfig.GameRules.DoMobSpawning : WorldStandardValues.DoMobSpawning.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoTileDrops", dimConfig.GameRules.DoTileDrops, defaultConfig != null ? defaultConfig.GameRules.DoTileDrops : WorldStandardValues.DoTileDrops.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoWeatherCycle", dimConfig.GameRules.DoWeatherCycle, defaultConfig != null ? defaultConfig.GameRules.DoWeatherCycle : WorldStandardValues.DoWeatherCycle.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("GameLoopFunction", dimConfig.GameRules.GameLoopFunction, defaultConfig != null ? defaultConfig.GameRules.GameLoopFunction : WorldStandardValues.GameLoopFunction.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("KeepInventory", dimConfig.GameRules.KeepInventory, defaultConfig != null ? defaultConfig.GameRules.KeepInventory : WorldStandardValues.KeepInventory.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("LogAdminCommands", dimConfig.GameRules.LogAdminCommands, defaultConfig != null ? defaultConfig.GameRules.LogAdminCommands : WorldStandardValues.LogAdminCommands.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("MaxCommandChainLength", dimConfig.GameRules.MaxCommandChainLength , defaultConfig != null ? defaultConfig.GameRules.MaxCommandChainLength : WorldStandardValues.MaxCommandChainLength.getDefaultValue(), ((IntSetting)WorldStandardValues.MaxCommandChainLength).getMinValue(), ((IntSetting)WorldStandardValues.MaxCommandChainLength).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("MaxEntityCramming", dimConfig.GameRules.MaxEntityCramming, defaultConfig != null ? defaultConfig.GameRules.MaxEntityCramming : WorldStandardValues.MaxEntityCramming.getDefaultValue(), ((IntSetting)WorldStandardValues.MaxEntityCramming).getMinValue(), ((IntSetting)WorldStandardValues.MaxEntityCramming).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("MobGriefing", dimConfig.GameRules.MobGriefing, defaultConfig != null ? defaultConfig.GameRules.MobGriefing : WorldStandardValues.MobGriefing.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("NaturalRegeneration", dimConfig.GameRules.NaturalRegeneration, defaultConfig != null ? defaultConfig.GameRules.NaturalRegeneration : WorldStandardValues.NaturalRegeneration.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("RandomTickSpeed", dimConfig.GameRules.RandomTickSpeed, defaultConfig != null ? defaultConfig.GameRules.RandomTickSpeed : WorldStandardValues.RandomTickSpeed.getDefaultValue(), ((IntSetting)WorldStandardValues.RandomTickSpeed).getMinValue(), ((IntSetting)WorldStandardValues.RandomTickSpeed).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("ReducedDebugInfo", dimConfig.GameRules.ReducedDebugInfo, defaultConfig != null ? defaultConfig.GameRules.ReducedDebugInfo : WorldStandardValues.ReducedDebugInfo.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("SendCommandFeedback", dimConfig.GameRules.SendCommandFeedback, defaultConfig != null ? defaultConfig.GameRules.SendCommandFeedback : WorldStandardValues.SendCommandFeedback.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("ShowDeathMessages", dimConfig.GameRules.ShowDeathMessages, defaultConfig != null ? defaultConfig.GameRules.ShowDeathMessages : WorldStandardValues.ShowDeathMessages.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("SpawnRadius", dimConfig.GameRules.SpawnRadius, defaultConfig != null ? defaultConfig.GameRules.SpawnRadius : WorldStandardValues.SpawnRadius.getDefaultValue(), ((IntSetting)WorldStandardValues.SpawnRadius).getMinValue(), ((IntSetting)WorldStandardValues.SpawnRadius).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("SpectatorsGenerateChunks", dimConfig.GameRules.SpectatorsGenerateChunks, defaultConfig != null ? defaultConfig.GameRules.SpectatorsGenerateChunks : WorldStandardValues.SpectatorsGenerateChunks.getDefaultValue(), false), this));
		        
		        // If world isn't null then we're ingame
		        if(this.mc.world != null)
		        {
		        	if(this.mc.isSingleplayer())
		        	{
		        		listEntries.add(new CategoryEntry(this, "* Close the OTG menu to apply game rules *"));
		        	}
		        	listEntries.add(new CategoryEntry(this, "* Don't use /gamerule, it's overworld only *"));
		        }
	
		        listEntries.add(new ButtonEntry(this, this, "Back"));
	        }
	        else if(advancedSettingsMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = "Back";              	
	        	        
		        listEntries.add(new CategoryEntry(this, "Blocks"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("PlayersCanBreakBlocks", dimConfig.Settings.PlayersCanBreakBlocks, defaultConfig != null ? defaultConfig.Settings.PlayersCanBreakBlocks : WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS.getDefaultValue(), null, null, false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("PlayersCanPlaceBlocks", dimConfig.Settings.PlayersCanPlaceBlocks, defaultConfig != null ? defaultConfig.Settings.PlayersCanPlaceBlocks : WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS.getDefaultValue(), null, null, false), this));        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("ExplosionsCanBreakBlocks", dimConfig.Settings.ExplosionsCanBreakBlocks, defaultConfig != null ? defaultConfig.Settings.ExplosionsCanBreakBlocks : WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS.getDefaultValue(), false), this));
	                
		        listEntries.add(new CategoryEntry(this, "Spawning"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("CanRespawnHere", dimConfig.Settings.CanRespawnHere, defaultConfig != null ? defaultConfig.Settings.CanRespawnHere : WorldStandardValues.CanRespawnHere.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("RespawnDimension", dimConfig.Settings.RespawnDimension, defaultConfig != null ? defaultConfig.Settings.RespawnDimension : WorldStandardValues.RESPAWN_DIMENSION.getDefaultValue(), ((IntSetting)WorldStandardValues.RESPAWN_DIMENSION).getMinValue(), ((IntSetting)WorldStandardValues.RESPAWN_DIMENSION).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("SpawnPointSet", dimConfig.Settings.SpawnPointSet, defaultConfig != null ? defaultConfig.Settings.SpawnPointSet : WorldStandardValues.SPAWN_POINT_SET.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("SpawnPointX", dimConfig.Settings.SpawnPointX, defaultConfig != null ? defaultConfig.Settings.SpawnPointX : WorldStandardValues.SPAWN_POINT_X.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_X).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_X).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("SpawnPointY", dimConfig.Settings.SpawnPointY, defaultConfig != null ? defaultConfig.Settings.SpawnPointY : WorldStandardValues.SPAWN_POINT_Y.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Y).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Y).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("SpawnPointZ", dimConfig.Settings.SpawnPointZ, defaultConfig != null ? defaultConfig.Settings.SpawnPointZ : WorldStandardValues.SPAWN_POINT_Z.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Z).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Z).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("TeleportToSpawnOnly", dimConfig.Settings.TeleportToSpawnOnly, defaultConfig != null ? defaultConfig.Settings.TeleportToSpawnOnly : WorldStandardValues.TeleportToSpawnOnly.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("WelcomeMessage", dimConfig.Settings.WelcomeMessage, defaultConfig != null ? defaultConfig.Settings.WelcomeMessage : WorldStandardValues.WelcomeMessage.getDefaultValue(), false), this));	        
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("DepartMessage", dimConfig.Settings.DepartMessage, defaultConfig != null ? defaultConfig.Settings.DepartMessage : WorldStandardValues.DepartMessage.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("ItemsToAddOnJoinDimension", dimConfig.Settings.ItemsToAddOnJoinDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("ItemsToAddOnLeaveDimension", dimConfig.Settings.ItemsToAddOnLeaveDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("ItemsToAddOnRespawn", dimConfig.Settings.ItemsToAddOnRespawn, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnRespawn : WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("ItemsToRemoveOnJoinDimension", dimConfig.Settings.ItemsToRemoveOnJoinDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("ItemsToRemoveOnLeaveDimension", dimConfig.Settings.ItemsToRemoveOnLeaveDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue(), false), this));                
		        
		        listEntries.add(new CategoryEntry(this, "Lighting"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("HasSkyLight", dimConfig.Settings.HasSkyLight, defaultConfig != null ? defaultConfig.Settings.HasSkyLight : WorldStandardValues.HasSkyLight.getDefaultValue(), true), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("AlwaysNight", dimConfig.Settings.IsNightWorld, defaultConfig != null ? defaultConfig.Settings.IsNightWorld : WorldStandardValues.IsNightWorld.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("IsSkyColored", dimConfig.Settings.IsSkyColored, defaultConfig != null ? defaultConfig.Settings.IsSkyColored : WorldStandardValues.IsSkyColored.getDefaultValue(), false), this));
	
		        listEntries.add(new CategoryEntry(this, "Weather and fog"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("CanDoLightning", dimConfig.Settings.CanDoLightning, defaultConfig != null ? defaultConfig.Settings.CanDoLightning : WorldStandardValues.CanDoLightning.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("CanDoRainSnowIce", dimConfig.Settings.CanDoRainSnowIce, defaultConfig != null ? defaultConfig.Settings.CanDoRainSnowIce : WorldStandardValues.CanDoRainSnowIce.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("CloudHeight", dimConfig.Settings.CloudHeight, defaultConfig != null ? defaultConfig.Settings.CloudHeight : WorldStandardValues.CloudHeight.getDefaultValue(), ((IntSetting)WorldStandardValues.CloudHeight).getMinValue(), ((IntSetting)WorldStandardValues.CloudHeight).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoesXZShowFog", dimConfig.Settings.DoesXZShowFog, defaultConfig != null ? defaultConfig.Settings.DoesXZShowFog : WorldStandardValues.DoesXZShowFog.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Double>("FogColorBlue", dimConfig.Settings.FogColorBlue, defaultConfig != null ? defaultConfig.Settings.FogColorBlue : WorldStandardValues.FogColorBlue.getDefaultValue(), ((DoubleSetting)WorldStandardValues.FogColorBlue).getMinValue(), ((DoubleSetting)WorldStandardValues.FogColorBlue).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Double>("FogColorGreen", dimConfig.Settings.FogColorGreen, defaultConfig != null ? defaultConfig.Settings.FogColorGreen : WorldStandardValues.FogColorGreen.getDefaultValue(), ((DoubleSetting)WorldStandardValues.FogColorGreen).getMinValue(), ((DoubleSetting)WorldStandardValues.FogColorGreen).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Double>("FogColorRed", dimConfig.Settings.FogColorRed, defaultConfig != null ? defaultConfig.Settings.FogColorRed : WorldStandardValues.FogColorRed.getDefaultValue(), ((DoubleSetting)WorldStandardValues.FogColorRed).getMinValue(), ((DoubleSetting)WorldStandardValues.FogColorRed).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("UseCustomFogColor", dimConfig.Settings.UseCustomFogColor, defaultConfig != null ? defaultConfig.Settings.UseCustomFogColor : WorldStandardValues.UseCustomFogColor.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Double>("VoidFogYFactor", dimConfig.Settings.VoidFogYFactor, defaultConfig != null ? defaultConfig.Settings.VoidFogYFactor : WorldStandardValues.VoidFogYFactor.getDefaultValue(), ((DoubleSetting)WorldStandardValues.VoidFogYFactor).getMinValue(), ((DoubleSetting)WorldStandardValues.VoidFogYFactor).getMaxValue(), false), this));	        
		        
		        listEntries.add(new CategoryEntry(this, "Miscellaneous"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("CanUnload", dimConfig.Settings.CanDropChunk, defaultConfig != null ? defaultConfig.Settings.CanDropChunk : WorldStandardValues.CanDropChunk.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("IsSurfaceWorld", dimConfig.Settings.IsSurfaceWorld, defaultConfig != null ? defaultConfig.Settings.IsSurfaceWorld : WorldStandardValues.IsSurfaceWorld.getDefaultValue(), true), this));                
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("DoesWaterVaporize", dimConfig.Settings.DoesWaterVaporize, defaultConfig != null ? defaultConfig.Settings.DoesWaterVaporize : WorldStandardValues.DoesWaterVaporize.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Double>("GravityFactor", dimConfig.Settings.GravityFactor, defaultConfig != null ? defaultConfig.Settings.GravityFactor : WorldStandardValues.GravityFactor.getDefaultValue(), ((DoubleSetting)WorldStandardValues.GravityFactor).getMinValue(), ((DoubleSetting)WorldStandardValues.GravityFactor).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("MovementFactor", dimConfig.Settings.MovementFactor, defaultConfig != null ? defaultConfig.Settings.MovementFactor : WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue(), ((IntSetting)WorldStandardValues.MOVEMENT_FACTOR).getMinValue(), ((IntSetting)WorldStandardValues.MOVEMENT_FACTOR).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Boolean>("ShouldMapSpin", dimConfig.Settings.ShouldMapSpin, defaultConfig != null ? defaultConfig.Settings.ShouldMapSpin : WorldStandardValues.ShouldMapSpin.getDefaultValue(), false), this));
		        
		        listEntries.add(new CategoryEntry(this, "Dimension above / below"));
		        
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("DimensionAbove", dimConfig.Settings.DimensionAbove, defaultConfig != null ? defaultConfig.Settings.DimensionAbove : WorldStandardValues.DIMENSIONABOVE.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("DimensionAboveHeight", dimConfig.Settings.DimensionAboveHeight, defaultConfig != null ? defaultConfig.Settings.DimensionAboveHeight : WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue(), ((IntSetting)WorldStandardValues.DIMENSIONABOVEHEIGHT).getMinValue(), ((IntSetting)WorldStandardValues.DIMENSIONABOVEHEIGHT).getMaxValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<String>("DimensionBelow", dimConfig.Settings.DimensionBelow, defaultConfig != null ? defaultConfig.Settings.DimensionBelow : WorldStandardValues.DIMENSIONBELOW.getDefaultValue(), false), this));
		        listEntries.add(new KeyEntry(this, new SettingEntry<Integer>("DimensionBelowHeight", dimConfig.Settings.DimensionBelowHeight, defaultConfig != null ? defaultConfig.Settings.DimensionBelowHeight : WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue(), ((IntSetting)WorldStandardValues.DIMENSIONBELOWHEIGHT).getMinValue(), ((IntSetting)WorldStandardValues.DIMENSIONBELOWHEIGHT).getMaxValue(), false), this));
		        
		        listEntries.add(new CategoryEntry(this, ""));
		        listEntries.add(new ButtonEntry(this, this, "Back"));
	        }
    	}

        for (IGuiListEntry keybinding : listEntries)
        {
            int j = this.mc.fontRenderer.getStringWidth(keybinding.getDisplayText());

            if (j > this.maxListLabelWidth)
            {
                this.maxListLabelWidth = j;
            }
        }
    }
    
    void applySettings()
    {
        DimensionConfig dimConfig = this.controlsScreen.selectedDimension;
    	    	
    	// TODO: Use const strings instead of hardcoding the same string in multiple places
    	for(IGuiListEntry entry : listEntries)
    	{
    		if(entry instanceof KeyEntry)
    		{
	            if(this.mainMenu)
	            {	            	
	            	switch(((KeyEntry)entry).settingEntry.name)
	            	{
	            		case "Preset":
	            			dimConfig.PresetName = entry.getDisplayText();
	            			break;
	            		case "Seed":
	            			dimConfig.Seed = entry.getDisplayText();
	            			break;
	            		case "Game type":
	            			dimConfig.GameType = entry.getDisplayText();
	            			break;
	            		case "Bonus chest":
	            			dimConfig.BonusChest = entry.getDisplayText().equals("On");
	            			break;
	            		case "Allow cheats":
	            			dimConfig.AllowCheats = entry.getDisplayText().equals("On");
	            			break;
	            		case "Pregenerator radius":
	            			dimConfig.PregeneratorRadiusInChunks = Integer.parseInt(entry.getDisplayText()); 
	            			break;
	            		case "World border radius":
	            			dimConfig.WorldBorderRadiusInChunks = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "Portal materials":
	            			dimConfig.Settings.DimensionPortalMaterials = entry.getDisplayText().replace(" ", "").split(",");
	            			break;
	            	}
	            }
	            else if(this.gameRulesMenu)
	            {
	            	switch(((KeyEntry)entry).settingEntry.name)
	            	{
	            		case "CommandBlockOutput":
	            			dimConfig.GameRules.CommandBlockOutput = entry.getDisplayText().equals("On");
	            			break;
	            		case "DisableElytraMovementCheck":
	            			dimConfig.GameRules.DisableElytraMovementCheck = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoDaylightCycle":
	            			dimConfig.GameRules.DoDaylightCycle = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoEntityDrops":          
	            			dimConfig.GameRules.DoEntityDrops = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoFireTick":
	            			dimConfig.GameRules.DoFireTick = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoLimitedCrafting":   
	            			dimConfig.GameRules.DoLimitedCrafting = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoMobLoot":
	            			dimConfig.GameRules.DoMobLoot = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoMobSpawning":
	            			dimConfig.GameRules.DoMobSpawning = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoTileDrops":
	            			dimConfig.GameRules.DoTileDrops = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoWeatherCycle":
	            			dimConfig.GameRules.DoWeatherCycle = entry.getDisplayText().equals("On");
	            			break;
	            		case "GameLoopFunction":
	            			dimConfig.GameRules.GameLoopFunction = entry.getDisplayText().equals("On");
	            			break;
	            		case "KeepInventory":
	            			dimConfig.GameRules.KeepInventory = entry.getDisplayText().equals("On");
	            			break;
	            		case "LogAdminCommands":
	            			dimConfig.GameRules.LogAdminCommands = entry.getDisplayText().equals("On");
	            			break;
	            		case "MaxCommandChainLength":
	            			dimConfig.GameRules.MaxCommandChainLength = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "MaxEntityCramming":
	            			dimConfig.GameRules.MaxEntityCramming = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "MobGriefing":
	            			dimConfig.GameRules.MobGriefing = entry.getDisplayText().equals("On");
	            			break;
	            		case "NaturalRegeneration":
	            			dimConfig.GameRules.NaturalRegeneration = entry.getDisplayText().equals("On");
	            			break;
	            		case "RandomTickSpeed":
	            			dimConfig.GameRules.RandomTickSpeed = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "ReducedDebugInfo":
	            			dimConfig.GameRules.ReducedDebugInfo = entry.getDisplayText().equals("On");
	            			break;
	            		case "SendCommandFeedback":
	            			dimConfig.GameRules.SendCommandFeedback = entry.getDisplayText().equals("On");
	            			break;
	            		case "ShowDeathMessages":
	            			dimConfig.GameRules.ShowDeathMessages = entry.getDisplayText().equals("On");
	            			break;
	            		case "SpawnRadius":
	            			dimConfig.GameRules.SpawnRadius = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "SpectatorsGenerateChunks":
	            			dimConfig.GameRules.SpectatorsGenerateChunks = entry.getDisplayText().equals("On");
	            			break;            			            			
	            	}
	            }
	            else if(this.advancedSettingsMenu)
	            {
	            	switch(((KeyEntry)entry).settingEntry.name)
	            	{
	            		case "CanDoLightning":
	            			dimConfig.Settings.CanDoLightning = entry.getDisplayText().equals("On");
	            			break;
	            		case "CanDoRainSnowIce":
	            			dimConfig.Settings.CanDoRainSnowIce = entry.getDisplayText().equals("On");
	            			break;
	            		case "CanUnload":
	            			dimConfig.Settings.CanDropChunk = entry.getDisplayText().equals("On");
	            			break;
	            		case "CanRespawnHere":
	            			dimConfig.Settings.CanRespawnHere = entry.getDisplayText().equals("On");
	            			break;
	            		case "CloudHeight":
	            			dimConfig.Settings.CloudHeight = Integer.parseInt(entry.getDisplayText());
	            			break;
	        			case "WelcomeMessage":
	            			dimConfig.Settings.WelcomeMessage = entry.getDisplayText();
	            			break;
	            		case "DepartMessage":
	            			dimConfig.Settings.DepartMessage = entry.getDisplayText();
	            			break;
	            		case "DimensionAbove":
	            			dimConfig.Settings.DimensionAbove = entry.getDisplayText();
	            			break;
	            		case "DimensionAboveHeight":
	            			dimConfig.Settings.DimensionAboveHeight = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "DimensionBelow":
	            			dimConfig.Settings.DimensionBelow = entry.getDisplayText();
	            			break;
	            		case "DimensionBelowHeight":
	            			dimConfig.Settings.DimensionBelowHeight = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "DoesWaterVaporize":
	            			dimConfig.Settings.DoesWaterVaporize = entry.getDisplayText().equals("On");
	            			break;
	            		case "DoesXZShowFog":
	            			dimConfig.Settings.DoesXZShowFog = entry.getDisplayText().equals("On");
	            			break;
	            		case "ExplosionsCanBreakBlocks":
	            			dimConfig.Settings.ExplosionsCanBreakBlocks = entry.getDisplayText().equals("On");
	            			break;
	            		case "FogColorBlue":
	            			dimConfig.Settings.FogColorBlue = Double.parseDouble(entry.getDisplayText());
	            			break;
	            		case "FogColorGreen":
	            			dimConfig.Settings.FogColorGreen = Double.parseDouble(entry.getDisplayText());
	            			break;
	            		case "FogColorRed":
	            			dimConfig.Settings.FogColorRed = Double.parseDouble(entry.getDisplayText());
	            			break;
	            		case "GravityFactor":
	            			dimConfig.Settings.GravityFactor = Double.parseDouble(entry.getDisplayText());
	            			break;
	            		case "HasSkyLight":
	            			dimConfig.Settings.HasSkyLight = entry.getDisplayText().equals("On");
	            			break;
	            		case "AlwaysNight":
	            			dimConfig.Settings.IsNightWorld = entry.getDisplayText().equals("On");
	            			break;
	            		case "IsSkyColored":
	            			dimConfig.Settings.IsSkyColored = entry.getDisplayText().equals("On");
	            			break;
	            		case "IsSurfaceWorld":
	            			dimConfig.Settings.IsSurfaceWorld = entry.getDisplayText().equals("On");
	            			break;
	            		case "ItemsToAddOnJoinDimension":
	            			dimConfig.Settings.ItemsToAddOnJoinDimension = entry.getDisplayText();
	            			break;
	            		case "ItemsToAddOnLeaveDimension":
	            			dimConfig.Settings.ItemsToAddOnLeaveDimension = entry.getDisplayText();
	            			break;
	            		case "ItemsToAddOnRespawn":
	            			dimConfig.Settings.ItemsToAddOnRespawn = entry.getDisplayText();
	            			break;
	            		case "ItemsToRemoveOnJoinDimension":
	            			dimConfig.Settings.ItemsToRemoveOnJoinDimension = entry.getDisplayText();
	            			break;
	            		case "ItemsToRemoveOnLeaveDimension":
	            			dimConfig.Settings.ItemsToRemoveOnLeaveDimension = entry.getDisplayText();
	            			break;
	            		case "MovementFactor":
	            			dimConfig.Settings.MovementFactor = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "PlayersCanBreakBlocks":
	            			dimConfig.Settings.PlayersCanBreakBlocks = entry.getDisplayText().equals("On");
	            			break;
	            		case "PlayersCanPlaceBlocks":
	            			dimConfig.Settings.PlayersCanPlaceBlocks = entry.getDisplayText().equals("On");
	            			break;
	            		case "RespawnDimension":
	            			dimConfig.Settings.RespawnDimension = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "ShouldMapSpin":
	            			dimConfig.Settings.ShouldMapSpin = entry.getDisplayText().equals("On");
	            			break;
	            		case "SpawnPointSet":
	            			dimConfig.Settings.SpawnPointSet = entry.getDisplayText().equals("On");
	            			break;
	            		case "SpawnPointX":
	            			dimConfig.Settings.SpawnPointX = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "SpawnPointY":
	            			dimConfig.Settings.SpawnPointY = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "SpawnPointZ":
	            			dimConfig.Settings.SpawnPointZ = Integer.parseInt(entry.getDisplayText());
	            			break;
	            		case "TeleportToSpawnOnly":
	            			dimConfig.Settings.TeleportToSpawnOnly = entry.getDisplayText().equals("On");
	            			break;
	            		case "UseCustomFogColor":
	            			dimConfig.Settings.UseCustomFogColor = entry.getDisplayText().equals("On");
	            			break;
	            		case "VoidFogYFactor":
	            			dimConfig.Settings.VoidFogYFactor = Double.parseDouble(entry.getDisplayText());
	            			break;
	            	}
	            }
    		}
    	}
    	this.controlsScreen.compareSettingsToOriginal();
    }
    
    protected int getSize()
    {
        return this.listEntries.size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public IGuiListEntry getListEntry(int index)
    {
        return this.listEntries.get(index);
    }
    
    public ArrayList<IGuiListEntry> getAllListEntries()
    {
        return this.listEntries;
    }
}