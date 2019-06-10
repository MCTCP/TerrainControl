package com.pg85.otg.forge.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Int;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.settingType.DoubleSetting;
import com.pg85.otg.configuration.settingType.IntSetting;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.ForgeWorldSession;
import com.pg85.otg.forge.generator.Pregenerator;
import com.pg85.otg.forge.network.client.ClientPacketManager;
import com.pg85.otg.util.helpers.StringHelper;

@SideOnly(Side.CLIENT)
public class OTGGuiDimensionSettingsList extends OTGGuiListExtended
{
	public enum ValueType
	{
		Bool,
		Int,
		Double,
		String
	}
	
	public class SettingEntry<T>
	{
		public String name;
		public ValueType valueType;
		public T value;
		public T defaultValue;
		public T minValue;
		public T maxValue;
		public boolean newWorldOnly;
		public boolean overWorldOnly;

		public SettingEntry(String name, T value, T defaultValue, boolean newWorldOnly)
		{
			this(name, value, defaultValue, null, null, newWorldOnly, false);
		}
		
		public SettingEntry(String name, T value, T defaultValue, boolean newWorldOnly, boolean overWorldOnly)
		{
			this(name, value, defaultValue, null, null, newWorldOnly, overWorldOnly);
		}
		
		public SettingEntry(String name, T value, T defaultValue, T minValue, T maxValue, boolean newWorldOnly)
		{
			this(name, value, defaultValue, minValue, maxValue, newWorldOnly, false);
		}
		
		public SettingEntry(String name, T value, T defaultValue, T minValue, T maxValue, boolean newWorldOnly, boolean overWorldOnly)
		{
			this.name = name;
			this.value = value;
			this.defaultValue = defaultValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.newWorldOnly = newWorldOnly;
			this.overWorldOnly = overWorldOnly;
				
			if(value == null)
			{
				valueType = ValueType.String;
			}							
			else if(value instanceof Integer)
			{
				valueType = ValueType.Int;
			}
			else if(value instanceof String)
			{
				valueType = ValueType.String;
			}
			else if(value instanceof Double)
			{
				valueType = ValueType.Double;
			}
			else if(value instanceof Boolean)
			{
				valueType = ValueType.Bool;
			} else {
				throw new RuntimeException("This should not happen, please contact team OTG about this crash.");
			}
		}
		
		public String getValueString()
		{
			if(this.valueType == ValueType.Double)
			{
				DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
				df.setMaximumFractionDigits(340); // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
				return df.format((Double)this.value);
			}
			
			String output = 
				this.value == null ? "" :
				this.valueType == ValueType.Bool ? ((Boolean)this.value ? "On" : "Off") : 
				this.valueType == ValueType.Int ? (Integer)this.value + "" : 
				(String)this.value;
			
            return output != null ? output : "";     				
		}
	}
	
    public final OTGGuiDimensionList controlsScreen;
    private final Minecraft mc;
    private final ArrayList<OTGGuiListExtended.IGuiListEntry> listEntries;
    private int maxListLabelWidth;

    public OTGGuiDimensionSettingsList(OTGGuiDimensionList controls, int top, int height, int left, int width, Minecraft mcIn)
    {
        super(mcIn, left, width, height, top, height, 24);
        this.controlsScreen = controls;
        this.mc = mcIn;
        this.listEntries = new ArrayList<OTGGuiListExtended.IGuiListEntry>();

        refreshData(true, false, false);
    }
    
    public void Resize(int topIn, int height, int left, int width)
    {
        this.width = width;
        this.height = height;
        this.top = topIn;
        this.bottom = height;
        this.left = left;
        this.right = left + width;
    }
    
    public void refreshData()
    {
    	refreshData(this.mainMenu, this.gameRulesMenu, this.advancedSettingsMenu);
    }

    public boolean mainMenu = true;
    public boolean gameRulesMenu = false;
    public boolean advancedSettingsMenu = false;
	public boolean showingPregeneratorStatus;
    public void refreshData(boolean mainMenu, boolean gameRulesMenu, boolean advancedSettingsMenu)
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
        	listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
        	listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
    		listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Creating world..."));
        }
        else if(this.controlsScreen.selectedDimension.PresetName == null) // If preset is null then this is a vanilla overworld
    	{
    		listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("OTG settings"));
    		listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("Portal materials", StringHelper.join(dimConfig.Settings.DimensionPortalMaterials, ", "), "DIRT", false), this)); // TODO: Fetch default value from worldstandarvalues
	        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
	        
	        // If world is not null then were ingame
	        // Don't show teleport button if we're in this world
	        if(this.mc.world != null && this.mc.world.provider.getDimension() != 0) 
	        {
	        	listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Teleport"));
	        }
    	} else {
	        // If a modder has added default values for this preset then use those, otherwise use the worldconfig
	        // If this.mc.world is not null then we're ingame
	        DimensionsConfig defaultConfigs = DimensionsConfig.getModPackConfig(this.mc.world != null ? OTG.GetDimensionsConfig().Overworld.PresetName : this.controlsScreen.previousMenu.selectedPreset.getSecond().PresetName);
	        DimensionConfig defaultConfig = defaultConfigs != null ? defaultConfigs.GetDimensionConfig(this.controlsScreen.selectedDimension.PresetName) : null;        
	        if(defaultConfig == null)
	        {
	        	// Get the default values from the world config, stored in presets
        		defaultConfig = new DimensionConfig(ForgeEngine.presets.get(this.controlsScreen.selectedDimension.PresetName));
	        }
	        if(mainMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = this.mc.world == null || !this.mc.isSingleplayer() ? "Cancel" : "Back to game"; // If world is not null then we're ingame
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("World settings"));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("Preset", dimConfig.PresetName, defaultConfig != null ? defaultConfig.PresetName : null, true), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("Seed", dimConfig.Seed, "", true), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("Game type", dimConfig.GameType, "Survival", true, true), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("Bonus chest", dimConfig.BonusChest, false, true, true), this, !dimConfig.GameType.equals("Hardcore")));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("Allow cheats", dimConfig.AllowCheats, false, true, true), this, !dimConfig.GameType.equals("Hardcore")));
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("OTG settings"));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("Pregenerator radius", dimConfig.PregeneratorRadiusInChunks, defaultConfig != null ? defaultConfig.PregeneratorRadiusInChunks : WorldStandardValues.PREGENERATION_RADIUS.getDefaultValue(), ((IntSetting)WorldStandardValues.PREGENERATION_RADIUS).getMinValue(), ((IntSetting)WorldStandardValues.PREGENERATION_RADIUS).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("World border radius", dimConfig.WorldBorderRadiusInChunks, defaultConfig != null ? defaultConfig.WorldBorderRadiusInChunks : WorldStandardValues.WORLD_BORDER_RADIUS.getDefaultValue(), ((IntSetting)WorldStandardValues.WORLD_BORDER_RADIUS).getMinValue(), ((IntSetting)WorldStandardValues.WORLD_BORDER_RADIUS).getMaxValue(), true), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("Portal materials", StringHelper.join(dimConfig.Settings.DimensionPortalMaterials, ", "), defaultConfig != null ? StringHelper.join(defaultConfig.Settings.DimensionPortalMaterials, ", ") : "DIRT", false), this)); // TODO: Fetch default value from worldstandarvalues
		        
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
					if(forgeWorld != null && forgeWorld.GetWorldSession() != null) 
					{
						Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
			
						if(pregenerator.getPregeneratorIsRunning() && !pregenerator.preGeneratorProgressStatus.equals("Done"))
				    	{	
							this.showingPregeneratorStatus = true;
					        listEntries.add(new OTGGuiDimensionSettingsList.PregeneratorSettingsEntry(this));
					        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
					        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
				    	}
					}
		        }
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
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
		        		listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Teleport"));		        		 
		        	}		        	
		        }
		        listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Game rules"));
		        listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Advanced settings"));
	        }
	        else if(gameRulesMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = "Back";
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Game rules"));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("CommandBlockOutput", dimConfig.GameRules.CommandBlockOutput, defaultConfig != null ? defaultConfig.GameRules.CommandBlockOutput : WorldStandardValues.commandBlockOutput.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DisableElytraMovementCheck", dimConfig.GameRules.DisableElytraMovementCheck, defaultConfig != null ? defaultConfig.GameRules.DisableElytraMovementCheck : WorldStandardValues.disableElytraMovementCheck.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoDaylightCycle", dimConfig.GameRules.DoDaylightCycle, defaultConfig != null ? defaultConfig.GameRules.DoDaylightCycle : WorldStandardValues.doDaylightCycle.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoEntityDrops", dimConfig.GameRules.DoEntityDrops, defaultConfig != null ? defaultConfig.GameRules.DoEntityDrops : WorldStandardValues.doEntityDrops.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoFireTick", dimConfig.GameRules.DoFireTick, defaultConfig != null ? defaultConfig.GameRules.DoFireTick : WorldStandardValues.doFireTick.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoLimitedCrafting", dimConfig.GameRules.DoLimitedCrafting, defaultConfig != null ? defaultConfig.GameRules.DoLimitedCrafting : WorldStandardValues.doLimitedCrafting.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoMobLoot", dimConfig.GameRules.DoMobLoot, defaultConfig != null ? defaultConfig.GameRules.DoMobLoot : WorldStandardValues.doMobLoot.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoMobSpawning", dimConfig.GameRules.DoMobSpawning, defaultConfig != null ? defaultConfig.GameRules.DoMobSpawning : WorldStandardValues.doMobSpawning.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoTileDrops", dimConfig.GameRules.DoTileDrops, defaultConfig != null ? defaultConfig.GameRules.DoTileDrops : WorldStandardValues.doTileDrops.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoWeatherCycle", dimConfig.GameRules.DoWeatherCycle, defaultConfig != null ? defaultConfig.GameRules.DoWeatherCycle : WorldStandardValues.doWeatherCycle.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("GameLoopFunction", dimConfig.GameRules.GameLoopFunction, defaultConfig != null ? defaultConfig.GameRules.GameLoopFunction : WorldStandardValues.gameLoopFunction.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("KeepInventory", dimConfig.GameRules.KeepInventory, defaultConfig != null ? defaultConfig.GameRules.KeepInventory : WorldStandardValues.keepInventory.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("LogAdminCommands", dimConfig.GameRules.LogAdminCommands, defaultConfig != null ? defaultConfig.GameRules.LogAdminCommands : WorldStandardValues.logAdminCommands.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("MaxCommandChainLength", dimConfig.GameRules.MaxCommandChainLength , defaultConfig != null ? defaultConfig.GameRules.MaxCommandChainLength : WorldStandardValues.maxCommandChainLength.getDefaultValue(), ((IntSetting)WorldStandardValues.maxCommandChainLength).getMinValue(), ((IntSetting)WorldStandardValues.maxCommandChainLength).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("MaxEntityCramming", dimConfig.GameRules.MaxEntityCramming, defaultConfig != null ? defaultConfig.GameRules.MaxEntityCramming : WorldStandardValues.maxEntityCramming.getDefaultValue(), ((IntSetting)WorldStandardValues.maxEntityCramming).getMinValue(), ((IntSetting)WorldStandardValues.maxEntityCramming).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("MobGriefing", dimConfig.GameRules.MobGriefing, defaultConfig != null ? defaultConfig.GameRules.MobGriefing : WorldStandardValues.mobGriefing.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("NaturalRegeneration", dimConfig.GameRules.NaturalRegeneration, defaultConfig != null ? defaultConfig.GameRules.NaturalRegeneration : WorldStandardValues.naturalRegeneration.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("RandomTickSpeed", dimConfig.GameRules.RandomTickSpeed, defaultConfig != null ? defaultConfig.GameRules.RandomTickSpeed : WorldStandardValues.randomTickSpeed.getDefaultValue(), ((IntSetting)WorldStandardValues.randomTickSpeed).getMinValue(), ((IntSetting)WorldStandardValues.randomTickSpeed).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("ReducedDebugInfo", dimConfig.GameRules.ReducedDebugInfo, defaultConfig != null ? defaultConfig.GameRules.ReducedDebugInfo : WorldStandardValues.reducedDebugInfo.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("SendCommandFeedback", dimConfig.GameRules.SendCommandFeedback, defaultConfig != null ? defaultConfig.GameRules.SendCommandFeedback : WorldStandardValues.sendCommandFeedback.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("ShowDeathMessages", dimConfig.GameRules.ShowDeathMessages, defaultConfig != null ? defaultConfig.GameRules.ShowDeathMessages : WorldStandardValues.showDeathMessages.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("SpawnRadius", dimConfig.GameRules.SpawnRadius, defaultConfig != null ? defaultConfig.GameRules.SpawnRadius : WorldStandardValues.spawnRadius.getDefaultValue(), ((IntSetting)WorldStandardValues.spawnRadius).getMinValue(), ((IntSetting)WorldStandardValues.spawnRadius).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("SpectatorsGenerateChunks", dimConfig.GameRules.SpectatorsGenerateChunks, defaultConfig != null ? defaultConfig.GameRules.SpectatorsGenerateChunks : WorldStandardValues.spectatorsGenerateChunks.getDefaultValue(), false), this));
		        
		        // If world isn't null then we're ingame
		        if(this.mc.world != null)
		        {
		        	if(this.mc.isSingleplayer())
		        	{
		        		listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("* Close the OTG menu to apply game rules *"));
		        	}
		        	listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("* Don't use /gamerule, it's overworld only *"));
		        }
	
		        listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Back"));
	        }
	        else if(advancedSettingsMenu)
	        {
	        	this.controlsScreen.btnCancel.displayString = "Back";              	
	        	        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Blocks"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("PlayersCanBreakBlocks", dimConfig.Settings.PlayersCanBreakBlocks, defaultConfig != null ? defaultConfig.Settings.PlayersCanBreakBlocks : WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS.getDefaultValue(), null, null, false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("PlayersCanPlaceBlocks", dimConfig.Settings.PlayersCanPlaceBlocks, defaultConfig != null ? defaultConfig.Settings.PlayersCanPlaceBlocks : WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS.getDefaultValue(), null, null, false), this));        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("ExplosionsCanBreakBlocks", dimConfig.Settings.ExplosionsCanBreakBlocks, defaultConfig != null ? defaultConfig.Settings.ExplosionsCanBreakBlocks : WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS.getDefaultValue(), false), this));
	                
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Spawning"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("CanRespawnHere", dimConfig.Settings.CanRespawnHere, defaultConfig != null ? defaultConfig.Settings.CanRespawnHere : WorldStandardValues.canRespawnHere.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("RespawnDimension", dimConfig.Settings.RespawnDimension, defaultConfig != null ? defaultConfig.Settings.RespawnDimension : WorldStandardValues.RESPAWN_DIMENSION.getDefaultValue(), ((IntSetting)WorldStandardValues.RESPAWN_DIMENSION).getMinValue(), ((IntSetting)WorldStandardValues.RESPAWN_DIMENSION).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("SpawnPointSet", dimConfig.Settings.SpawnPointSet, defaultConfig != null ? defaultConfig.Settings.SpawnPointSet : WorldStandardValues.SPAWN_POINT_SET.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("SpawnPointX", dimConfig.Settings.SpawnPointX, defaultConfig != null ? defaultConfig.Settings.SpawnPointX : WorldStandardValues.SPAWN_POINT_X.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_X).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_X).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("SpawnPointY", dimConfig.Settings.SpawnPointY, defaultConfig != null ? defaultConfig.Settings.SpawnPointY : WorldStandardValues.SPAWN_POINT_Y.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Y).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Y).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("SpawnPointZ", dimConfig.Settings.SpawnPointZ, defaultConfig != null ? defaultConfig.Settings.SpawnPointZ : WorldStandardValues.SPAWN_POINT_Z.getDefaultValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Z).getMinValue(), ((IntSetting)WorldStandardValues.SPAWN_POINT_Z).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("TeleportToSpawnOnly", dimConfig.Settings.TeleportToSpawnOnly, defaultConfig != null ? defaultConfig.Settings.TeleportToSpawnOnly : WorldStandardValues.teleportToSpawnOnly.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("WelcomeMessage", dimConfig.Settings.WelcomeMessage, defaultConfig != null ? defaultConfig.Settings.WelcomeMessage : WorldStandardValues.welcomeMessage.getDefaultValue(), false), this));	        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("DepartMessage", dimConfig.Settings.DepartMessage, defaultConfig != null ? defaultConfig.Settings.DepartMessage : WorldStandardValues.departMessage.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("ItemsToAddOnJoinDimension", dimConfig.Settings.ItemsToAddOnJoinDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnJoinDimension : WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("ItemsToAddOnLeaveDimension", dimConfig.Settings.ItemsToAddOnLeaveDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnLeaveDimension : WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("ItemsToAddOnRespawn", dimConfig.Settings.ItemsToAddOnRespawn, defaultConfig != null ? defaultConfig.Settings.ItemsToAddOnRespawn : WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("ItemsToRemoveOnJoinDimension", dimConfig.Settings.ItemsToRemoveOnJoinDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToRemoveOnJoinDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("ItemsToRemoveOnLeaveDimension", dimConfig.Settings.ItemsToRemoveOnLeaveDimension, defaultConfig != null ? defaultConfig.Settings.ItemsToRemoveOnLeaveDimension : WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue(), false), this));                
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Lighting"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("HasSkyLight", dimConfig.Settings.HasSkyLight, defaultConfig != null ? defaultConfig.Settings.HasSkyLight : WorldStandardValues.hasSkyLight.getDefaultValue(), true), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("AlwaysNight", dimConfig.Settings.IsNightWorld, defaultConfig != null ? defaultConfig.Settings.IsNightWorld : WorldStandardValues.isNightWorld.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("IsSkyColored", dimConfig.Settings.IsSkyColored, defaultConfig != null ? defaultConfig.Settings.IsSkyColored : WorldStandardValues.isSkyColored.getDefaultValue(), false), this));
	
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Weather and fog"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("CanDoLightning", dimConfig.Settings.CanDoLightning, defaultConfig != null ? defaultConfig.Settings.CanDoLightning : WorldStandardValues.canDoLightning.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("CanDoRainSnowIce", dimConfig.Settings.CanDoRainSnowIce, defaultConfig != null ? defaultConfig.Settings.CanDoRainSnowIce : WorldStandardValues.canDoRainSnowIce.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("CloudHeight", dimConfig.Settings.CloudHeight, defaultConfig != null ? defaultConfig.Settings.CloudHeight : WorldStandardValues.cloudHeight.getDefaultValue(), ((IntSetting)WorldStandardValues.cloudHeight).getMinValue(), ((IntSetting)WorldStandardValues.cloudHeight).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoesXZShowFog", dimConfig.Settings.DoesXZShowFog, defaultConfig != null ? defaultConfig.Settings.DoesXZShowFog : WorldStandardValues.doesXZShowFog.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Double>("FogColorBlue", dimConfig.Settings.FogColorBlue, defaultConfig != null ? defaultConfig.Settings.FogColorBlue : WorldStandardValues.fogColorBlue.getDefaultValue(), ((DoubleSetting)WorldStandardValues.fogColorBlue).getMinValue(), ((DoubleSetting)WorldStandardValues.fogColorBlue).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Double>("FogColorGreen", dimConfig.Settings.FogColorGreen, defaultConfig != null ? defaultConfig.Settings.FogColorGreen : WorldStandardValues.fogColorGreen.getDefaultValue(), ((DoubleSetting)WorldStandardValues.fogColorGreen).getMinValue(), ((DoubleSetting)WorldStandardValues.fogColorGreen).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Double>("FogColorRed", dimConfig.Settings.FogColorRed, defaultConfig != null ? defaultConfig.Settings.FogColorRed : WorldStandardValues.fogColorRed.getDefaultValue(), ((DoubleSetting)WorldStandardValues.fogColorRed).getMinValue(), ((DoubleSetting)WorldStandardValues.fogColorRed).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("UseCustomFogColor", dimConfig.Settings.UseCustomFogColor, defaultConfig != null ? defaultConfig.Settings.UseCustomFogColor : WorldStandardValues.useCustomFogColor.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Double>("VoidFogYFactor", dimConfig.Settings.VoidFogYFactor, defaultConfig != null ? defaultConfig.Settings.VoidFogYFactor : WorldStandardValues.voidFogYFactor.getDefaultValue(), ((DoubleSetting)WorldStandardValues.voidFogYFactor).getMinValue(), ((DoubleSetting)WorldStandardValues.voidFogYFactor).getMaxValue(), false), this));	        
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Miscellaneous"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("CanUnload", dimConfig.Settings.CanDropChunk, defaultConfig != null ? defaultConfig.Settings.CanDropChunk : WorldStandardValues.canDropChunk.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("IsSurfaceWorld", dimConfig.Settings.IsSurfaceWorld, defaultConfig != null ? defaultConfig.Settings.IsSurfaceWorld : WorldStandardValues.isSurfaceWorld.getDefaultValue(), true), this));                
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("DoesWaterVaporize", dimConfig.Settings.DoesWaterVaporize, defaultConfig != null ? defaultConfig.Settings.DoesWaterVaporize : WorldStandardValues.doesWaterVaporize.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Double>("GravityFactor", dimConfig.Settings.GravityFactor, defaultConfig != null ? defaultConfig.Settings.GravityFactor : WorldStandardValues.gravityFactor.getDefaultValue(), ((DoubleSetting)WorldStandardValues.gravityFactor).getMinValue(), ((DoubleSetting)WorldStandardValues.gravityFactor).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("MovementFactor", dimConfig.Settings.MovementFactor, defaultConfig != null ? defaultConfig.Settings.MovementFactor : WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue(), ((IntSetting)WorldStandardValues.MOVEMENT_FACTOR).getMinValue(), ((IntSetting)WorldStandardValues.MOVEMENT_FACTOR).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Boolean>("ShouldMapSpin", dimConfig.Settings.ShouldMapSpin, defaultConfig != null ? defaultConfig.Settings.ShouldMapSpin : WorldStandardValues.shouldMapSpin.getDefaultValue(), false), this));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry("Dimension above / below"));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("DimensionAbove", dimConfig.Settings.DimensionAbove, defaultConfig != null ? defaultConfig.Settings.DimensionAbove : WorldStandardValues.DIMENSIONABOVE.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("DimensionAboveHeight", dimConfig.Settings.DimensionAboveHeight, defaultConfig != null ? defaultConfig.Settings.DimensionAboveHeight : WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue(), ((IntSetting)WorldStandardValues.DIMENSIONABOVEHEIGHT).getMinValue(), ((IntSetting)WorldStandardValues.DIMENSIONABOVEHEIGHT).getMaxValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<String>("DimensionBelow", dimConfig.Settings.DimensionBelow, defaultConfig != null ? defaultConfig.Settings.DimensionBelow : WorldStandardValues.DIMENSIONBELOW.getDefaultValue(), false), this));
		        listEntries.add(new OTGGuiDimensionSettingsList.KeyEntry(new SettingEntry<Integer>("DimensionBelowHeight", dimConfig.Settings.DimensionBelowHeight, defaultConfig != null ? defaultConfig.Settings.DimensionBelowHeight : WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue(), ((IntSetting)WorldStandardValues.DIMENSIONBELOWHEIGHT).getMinValue(), ((IntSetting)WorldStandardValues.DIMENSIONBELOWHEIGHT).getMaxValue(), false), this));
		        
		        listEntries.add(new OTGGuiDimensionSettingsList.CategoryEntry(""));
		        listEntries.add(new OTGGuiDimensionSettingsList.ButtonEntry(this, "Back"));
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
    
    public void ApplySettings()
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
    	this.controlsScreen.CompareSettingsToOriginal();
    }
    
    protected int getSize()
    {
        return this.listEntries.size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public OTGGuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return this.listEntries.get(index);
    }
    
    public ArrayList<OTGGuiListExtended.IGuiListEntry> getAllListEntries()
    {
        return this.listEntries;
    }

    @SideOnly(Side.CLIENT)
    public class PregeneratorSettingsEntry implements OTGGuiListExtended.IGuiListEntry
    {
        private final OTGGuiDimensionSettingsList parent;
    	
        public PregeneratorSettingsEntry(OTGGuiDimensionSettingsList parent)
        {
            this.parent = parent;
        }
        
        private Pregenerator pregenerator = null;
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
        	if(this.pregenerator == null)
        	{
				ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
				if(forgeWorld == null)
				{
					forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimension.PresetName);
				}
				pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
        	}

			ArrayList<String> lines = new ArrayList<String>();
			
			lines.add("");
			lines.add("Pre-generating " + (pregenerator.progressScreenWorldSizeInBlocks > 0 ? pregenerator.progressScreenWorldSizeInBlocks + "x" + pregenerator.progressScreenWorldSizeInBlocks  + " blocks" : ""));
			lines.add("Progress: " + pregenerator.preGeneratorProgress + "%");
			lines.add("Chunks: " + pregenerator.preGeneratorProgressStatus);
			lines.add("Elapsed: " + pregenerator.progressScreenElapsedTime);
			lines.add("Estimated: " + pregenerator.progressScreenEstimatedTime);

			if(Minecraft.getMinecraft().isSingleplayer())
			{
				long i = Runtime.getRuntime().maxMemory();
		        long j = Runtime.getRuntime().totalMemory();
		        long k = Runtime.getRuntime().freeMemory();
		        long l = j - k;
		        lines.add("Memory: " + Long.valueOf(BytesToMb(l)) + "/" +  Long.valueOf(BytesToMb(i)) + " MB");
			} else {
				lines.add("Memory: " + pregenerator.progressScreenServerUsedMbs + "/" +  pregenerator.progressScreenServerTotalMbs + " MB");
			}

	        int linespacing = 11;
	        
	        for(int a = 0; a < lines.size(); a++)
	        {
	        	OTGGuiDimensionSettingsList.this.mc.fontRenderer.drawString(
        			lines.get(a), 
	    			x + 6,  			
	    			y + slotHeight - OTGGuiDimensionSettingsList.this.mc.fontRenderer.FONT_HEIGHT - 5 + (a * linespacing), 
	    			16777215
				);
	        }
        }

        private long BytesToMb(long bytes)
        {
            return bytes / 1024L / 1024L;
        }
    	
        public void keyTyped(char typedChar, int keyCode)
        {
        }
        
        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }

		@Override
		public String getLabelText() {
			return null;
		}

		@Override
		public String getDisplayText() {
			return null;
		}   	
    }
    
    @SideOnly(Side.CLIENT)
    public class CategoryEntry implements OTGGuiListExtended.IGuiListEntry
    {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name)
        {
            this.labelText = name;
            this.labelWidth = OTGGuiDimensionSettingsList.this.mc.fontRenderer.getStringWidth(this.labelText);
        }
        
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
        	OTGGuiDimensionSettingsList.this.mc.fontRenderer.drawString(
    			this.labelText, 
    			x + (300 / 2) - (labelWidth / 2),  			
    			y + slotHeight - OTGGuiDimensionSettingsList.this.mc.fontRenderer.FONT_HEIGHT - 5, 
    			16777215
			);
        }

        public void keyTyped(char typedChar, int keyCode)
        {
        }
        
        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }
        
        public String getLabelText()
        {
        	return this.labelText;
        }
        
        public String getDisplayText()
        {
        	return this.labelText;
        }
    }
    
    @SideOnly(Side.CLIENT)
    public class ButtonEntry implements OTGGuiListExtended.IGuiListEntry
    {
        private final String labelText;
        private final GuiButton btnSettingsEntry;
        private final OTGGuiDimensionSettingsList parent;

        public ButtonEntry(OTGGuiDimensionSettingsList parent, String name)
        {
        	this.parent = parent;
            this.labelText = name != null ? name : "";
            this.btnSettingsEntry = new GuiButton(0, 0, 0, 120, 20, this.labelText);
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            this.btnSettingsEntry.x = x + (300 / 2) - (120 / 2);
            this.btnSettingsEntry.y = y;
            this.btnSettingsEntry.displayString = this.labelText;
        	this.btnSettingsEntry.drawButton(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY, partialTicks);
        }
        
        public void keyTyped(char typedChar, int keyCode)
        {
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
        	if(this.btnSettingsEntry.mousePressed(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY))
        	{
        		// TODO: Make this prettier
        		if(labelText.equals("Game rules"))
        		{
        			// Open game rules menu
        			OTGGuiDimensionSettingsList.this.refreshData(false, true, false);
        			OTGGuiDimensionSettingsList.this.scrollBy(Int.MinValue());
        		}
        		else if(labelText.equals("Advanced settings"))
        		{
        			// Open Advanced settings menu
        			OTGGuiDimensionSettingsList.this.refreshData(false, false, true);
        			OTGGuiDimensionSettingsList.this.scrollBy(Int.MinValue());
        		}
        		else if(labelText.equals("Teleport"))
        		{
        			ClientPacketManager.SendTeleportPlayerPacket(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : parent.controlsScreen.selectedDimension.PresetName);
        			this.parent.mc.displayGuiScreen(null);
        		}
        		else if(labelText.equals("Back"))
        		{
        			// Return to dimension settings menu
        			OTGGuiDimensionSettingsList.this.refreshData(true, false, false);
        			OTGGuiDimensionSettingsList.this.scrollBy(Int.MinValue());
        		}
        		
                return true;
        	}
            
            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            this.btnSettingsEntry.mouseReleased(x, y);
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }
        
        public String getLabelText()
        {
        	return this.labelText;
        }
        
        public String getDisplayText()
        {
        	return this.labelText;
        }
    }

    @SideOnly(Side.CLIENT)
    public class KeyEntry implements OTGGuiListExtended.IGuiListEntry
    {
        /** The keybinding specified for this KeyEntry */
        private final SettingEntry settingEntry;
        /** The localized key description for this KeyEntry */
        private final String keyDesc;
        private final GuiButton btnSettingEntry;
        private final GuiTextField txtSettingsEntry;
        private final GuiButton btnReset;
        private final OTGGuiDimensionSettingsList parent;

        private KeyEntry(SettingEntry settingEntry, OTGGuiDimensionSettingsList parent, boolean isEnabled)
        {
        	this(settingEntry, parent);
        	this.btnSettingEntry.enabled = isEnabled;
        }
        
        private KeyEntry(SettingEntry settingEntry, OTGGuiDimensionSettingsList parent)
        {
            this.settingEntry = settingEntry;
        	this.parent = parent;
            this.keyDesc = this.settingEntry.name;
            this.btnSettingEntry = new GuiButton(0, 0, 0, 95, 20, this.settingEntry.getValueString());
            this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            this.txtSettingsEntry = new GuiTextField(0, OTGGuiDimensionSettingsList.this.mc.fontRenderer, 0, 0, 90, 20);
            this.txtSettingsEntry.setMaxStringLength(Integer.MAX_VALUE);
            this.txtSettingsEntry.setText(this.settingEntry.getValueString());            
            this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
        	boolean gameIsRunning = mc.world != null;
        	boolean newWorldOnly = this.settingEntry.newWorldOnly;
        	boolean overWorldOnly = this.settingEntry.overWorldOnly;
        	boolean isOverWorld = this.parent.controlsScreen.selectedDimensionIndex == 0;
        	boolean isNewConfig = this.parent.controlsScreen.selectedDimension.isNewConfig;
        	boolean showButtons = !(!isOverWorld && overWorldOnly) && !(gameIsRunning && !isNewConfig && newWorldOnly) && (!isOverWorld || !this.settingEntry.name.equals("Preset"));
        	
        	int marginleft = x;
            OTGGuiDimensionSettingsList.this.mc.fontRenderer.drawString(this.keyDesc, marginleft + 6, y + slotHeight / 2 - OTGGuiDimensionSettingsList.this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
            this.btnReset.x = marginleft + 240;
            this.btnReset.y = y;
            this.btnReset.enabled = showButtons &&
        		(
					(
						this.settingEntry.value == null && 
						this.settingEntry.defaultValue != null
					) || (
						this.settingEntry.value != null && 
						(
							(
								this.settingEntry.valueType == ValueType.Bool && 
								this.settingEntry.value != this.settingEntry.defaultValue
							) || (
								this.settingEntry.valueType != ValueType.Bool &&
								!this.settingEntry.value.equals(this.settingEntry.defaultValue)
							)
						)
					)
				);
            this.btnReset.drawButton(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY, partialTicks);
            this.btnSettingEntry.x = marginleft + 135;
            this.btnSettingEntry.y = y;
            // Don't re-enable buttons that were disabled by other settings, like AllowCheats and BonusChest.
            this.btnSettingEntry.enabled = this.btnSettingEntry.enabled && showButtons;  
            if(!isOverWorld && overWorldOnly)
            {
            	this.btnSettingEntry.displayString = "Overworld";
            }
            
            this.txtSettingsEntry.x = marginleft + 137;
            this.txtSettingsEntry.y = y;
            this.txtSettingsEntry.setEnabled(showButtons);

            if(this.settingEntry.name.equals("Game type") || this.settingEntry.name.equals("Preset") || this.settingEntry.valueType == ValueType.Bool)
            {
            	this.btnSettingEntry.drawButton(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY, partialTicks);
            } else {
            	this.txtSettingsEntry.drawTextBox();
            }
        }
        
        public void keyTyped(char typedChar, int keyCode)
        {
            if(
        		this.settingEntry.valueType != ValueType.Bool && 
        		!this.settingEntry.name.equals("Game type") && // GameType is a string presented as a button 
        		!this.settingEntry.name.equals("Preset") // Preset is a string presented as a button
    		)
            {
            	if(this.txtSettingsEntry.isFocused() || keyCode == 28 || keyCode == 83) // 28 + 83 is enter 
            	{
            		boolean bIsFunctionkey = false;
            		if(
						GuiScreen.isKeyComboCtrlA(keyCode) ||
						GuiScreen.isKeyComboCtrlC(keyCode) ||
						GuiScreen.isKeyComboCtrlV(keyCode) ||
						GuiScreen.isKeyComboCtrlX(keyCode) ||
						keyCode == 14 || // backspace
						keyCode == 199 ||
						keyCode == 203 || // left
						keyCode == 205 || // right
						keyCode == 207 ||
						keyCode == 211 ||
						keyCode == 28 || // 28 + 83 is enter 
						keyCode == 83
                    )
            		{
            			bIsFunctionkey = true;
        			}
            		
            		if(keyCode == 28 || keyCode == 83) // 28 + 83 is enter
            		{
            			this.txtSettingsEntry.setFocused(false);
            		}
            		
            		// Key code 14 is backspace
            		if(this.settingEntry.valueType == ValueType.String)
            		{
            			if(keyCode != 28 && keyCode != 83)
            			{
            				this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
            			}
        				if(keyCode == 28 || keyCode == 83)
        				{
	    	            	this.settingEntry.value = this.txtSettingsEntry.getText();
	    	            	this.txtSettingsEntry.setText(this.settingEntry.getValueString());
	    	            	ApplySettings();
        				}
            		}
            		else if(this.settingEntry.valueType == ValueType.Int)
            		{
            			if(Character.isDigit(typedChar) || (typedChar == '-' && this.txtSettingsEntry.getCursorPosition() == 0) || bIsFunctionkey)
            			{
            				if(keyCode != 28 && keyCode != 83)
            				{
            					this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
            				}
            				if(keyCode == 28 || keyCode == 83)
            				{
            					int integer = 0;
            					try
            					{
            						integer = Integer.parseInt(txtSettingsEntry.getText().trim());
            					}
            					catch(NumberFormatException ex)
            					{
            						integer = (int)this.settingEntry.value;
            					}
            					if(integer > (int)this.settingEntry.maxValue)
            					{
            						integer = (int)this.settingEntry.maxValue;
            					}
            					if(integer < (int)this.settingEntry.minValue)
            					{
            						integer = (int)this.settingEntry.minValue;
            					}
            					
	            				if(this.settingEntry.name.equals("Pregenerator radius"))
	            				{
	            					int radius = integer;
	            					if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
	            					{
		            					ForgeWorld forgeWorld = null;
	            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
		            					if(forgeWorld == null)
		            					{
		            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
		            					}
		            					// ForgeWorld can be null in SP world creation menu.
		            					if(Minecraft.getMinecraft().isSingleplayer() && forgeWorld != null && forgeWorld.GetWorldSession() != null && forgeWorld.GetWorldSession().getPregenerationRadius() != radius)
		            					{
			            					forgeWorld.GetWorldSession().setPregenerationRadius(radius);
			            					radius = forgeWorld.GetWorldSession().getPregenerationRadius();
			                            	this.parent.controlsScreen.selectedDimension.PregeneratorRadiusInChunks = radius;				                            	
		            					}
	            					}
		            				this.settingEntry.value = radius;
		            				this.txtSettingsEntry.setText(radius + "");
	            				} else {	            					
		            				this.settingEntry.value = integer;
	            				}
	            				this.txtSettingsEntry.setText(this.settingEntry.getValueString());
	            				
	            				ApplySettings();	            			
            				}
            			}
            		}
            		else if(this.settingEntry.valueType == ValueType.Double)
            		{
            			if(bIsFunctionkey || Character.isDigit(typedChar) || (typedChar == '-' && this.txtSettingsEntry.getCursorPosition() == 0) || (typedChar == '.' && !this.txtSettingsEntry.getText().contains(".")))
            			{
            				if(keyCode != 28 && keyCode != 83)
            				{
            					this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
            				}
            				if(keyCode == 28 || keyCode == 83)
            				{
            					double db = 0;
            					try
            					{
            						db = Double.parseDouble(txtSettingsEntry.getText().trim());
            					}
            					catch(NumberFormatException ex)
            					{
            						db = (int)this.settingEntry.value;
            					}
            					if(db > (double)this.settingEntry.maxValue)
            					{
            						db = (double)this.settingEntry.maxValue;
            					}
            					if(db < (double)this.settingEntry.minValue)
            					{
            						db = (double)this.settingEntry.minValue;
            					}            					
	            				this.settingEntry.value = db;
	            				this.txtSettingsEntry.setText(this.settingEntry.getValueString());
	            				ApplySettings();
            				}
            			}
            		}
            	}
            }
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         * Also called whenever the mouse is pressed on the parent screen, to make text boxes lose focus
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
        	if(this.settingEntry.name.equals("Game type"))
        	{
            	if(this.btnSettingEntry.mousePressed(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY))
            	{
	                OTGGuiDimensionSettingsList.this.controlsScreen.buttonId = this.settingEntry;
                	if(((String)this.settingEntry.value).toUpperCase().equals(GameType.SURVIVAL.toString()))
                	{
		                this.settingEntry.value = "Hardcore";
        				this.btnSettingEntry.displayString = this.settingEntry.getValueString();
	            		for(IGuiListEntry entry : this.parent.getAllListEntries())
	            		{
	            			if(
            					entry.getLabelText().equals("Allow cheats") ||
            					entry.getLabelText().equals("Bonus chest")
        					)
            				{
	            				((KeyEntry)entry).btnSettingEntry.enabled = false;
	            				((KeyEntry)entry).settingEntry.value = false;
	            				((KeyEntry)entry).btnSettingEntry.displayString = ((KeyEntry)entry).settingEntry.getValueString();
	            			}
	            		}
                	}
                	else if(((String)this.settingEntry.value).toUpperCase().equals(GameType.CREATIVE.toString()))
                	{
                		this.settingEntry.value = "Survival";
                		this.btnSettingEntry.displayString = this.settingEntry.getValueString();
	            		for(IGuiListEntry entry : this.parent.getAllListEntries())
	            		{
	            			if(
            					entry.getLabelText().equals("Allow cheats") ||
            					entry.getLabelText().equals("Bonus chest")
        					)
            				{
	            				((KeyEntry)entry).btnSettingEntry.enabled = true;
	            			}
	            		}
                	}
	                else if(((String)this.settingEntry.value).toUpperCase().equals("HARDCORE"))
	                {
	                	this.settingEntry.value = "Creative";
	                	this.btnSettingEntry.displayString = this.settingEntry.getValueString();
	            		for(IGuiListEntry entry : this.parent.getAllListEntries())
	            		{
	            			if(
            					entry.getLabelText().equals("Allow cheats") ||
            					entry.getLabelText().equals("Bonus chest")
        					)
            				{
	            				((KeyEntry)entry).btnSettingEntry.enabled = true;
	            			}
	            		}	                	
                	}
					ApplySettings();
	                return true;
            	}
        	}
        	else if(this.settingEntry.name.equals("Preset"))
        	{
            	if(this.btnSettingEntry.mousePressed(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY))
            	{
	                OTGGuiDimensionSettingsList.this.controlsScreen.buttonId = this.settingEntry;
	                
	                // Show choose preset menu
	                OTGGuiDimensionSettingsList.this.controlsScreen.selectingPresetForDimension = true;
	                OTGGuiDimensionSettingsList.this.controlsScreen.selectPresetForDimensionMenu = new OTGGuiPresetList(OTGGuiDimensionSettingsList.this.controlsScreen, true);
	                OTGGuiDimensionSettingsList.this.mc.displayGuiScreen(OTGGuiDimensionSettingsList.this.controlsScreen.selectPresetForDimensionMenu);
	                
	                return true;
            	}
        	}
        	else if (this.settingEntry.valueType == ValueType.Bool)
            {
            	if(this.btnSettingEntry.mousePressed(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY))
            	{
	                OTGGuiDimensionSettingsList.this.controlsScreen.buttonId = this.settingEntry;
                	if(((boolean)this.settingEntry.value))
                	{
		                this.settingEntry.value = false;
                	} else {
		                this.settingEntry.value = true;
                	}
	                this.btnSettingEntry.displayString = this.settingEntry.getValueString();
					ApplySettings();
	                return true;
            	}
            }
            else if (this.settingEntry.valueType != ValueType.Bool)
            {
            	if(this.txtSettingsEntry.mouseClicked(mouseX, mouseY,0))
            	{
	                OTGGuiDimensionSettingsList.this.controlsScreen.buttonId = this.settingEntry;                
	                return true;
            	} else {
            		// If textbox for an int/double loses foxus and is empty then set default value
	                if(this.txtSettingsEntry.getText().length() == 0 && (this.settingEntry.valueType == ValueType.Int || this.settingEntry.valueType == ValueType.Double))
	                {
		                this.settingEntry.value = this.settingEntry.defaultValue;
    					ApplySettings();
	                }
            	}
            }
            
            if (this.btnReset.mousePressed(OTGGuiDimensionSettingsList.this.mc, mouseX, mouseY))
            {
            	this.settingEntry.value = this.settingEntry.defaultValue;
            	if(this.settingEntry.name.equals("Pregenerator radius"))
        		{
					int radius = (int)this.settingEntry.value;
            		if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
            		{
    					ForgeWorld forgeWorld = null;
    					forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
    					if(forgeWorld == null)
    					{
    						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
    					}
    					// ForgeWorld can be null for SP world creation menu
    					if(Minecraft.getMinecraft().isSingleplayer() && forgeWorld != null && forgeWorld.GetWorldSession() != null && forgeWorld.GetWorldSession().getPregenerationRadius() != radius)
    					{
        					forgeWorld.GetWorldSession().setPregenerationRadius(radius);
        					radius = forgeWorld.GetWorldSession().getPregenerationRadius();
                        	this.parent.controlsScreen.selectedDimension.PregeneratorRadiusInChunks = radius;				                            	
    					}
            		}
    				this.settingEntry.value = radius;
    				this.txtSettingsEntry.setText(radius + "");
        		}
            	
            	if(this.settingEntry.name.equals("Game type"))
            	{
                	this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            		for(IGuiListEntry entry : this.parent.getAllListEntries())
            		{
            			if(
        					entry.getLabelText().equals("Allow cheats") ||
        					entry.getLabelText().equals("Bonus chest")
    					)
        				{
                        	if(((String)this.settingEntry.value).equals("Hardcore"))
                        	{
	            				((KeyEntry)entry).btnSettingEntry.enabled = false;
	            				((KeyEntry)entry).settingEntry.value = false;
                        	} else {
	            				((KeyEntry)entry).btnSettingEntry.enabled = true;
                        	}
                        	((KeyEntry)entry).btnSettingEntry.displayString = ((KeyEntry)entry).settingEntry.getValueString();
            			}
            		}
            	} else {
            		if(this.settingEntry.valueType.equals(ValueType.Bool))
            		{
	            		this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            		} else {
            			this.txtSettingsEntry.setText(this.settingEntry.getValueString());
            		}
            	}
                
				ApplySettings();
                return true;
            }
            
            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            this.btnSettingEntry.mouseReleased(x, y);
            this.btnReset.mouseReleased(x, y);
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }
        
        public String getLabelText()
        {
        	return this.settingEntry.name;
        }
        
        public String getDisplayText()
        {
        	return this.settingEntry.getValueString();
        }
    }
}