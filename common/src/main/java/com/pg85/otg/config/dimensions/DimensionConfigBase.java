package com.pg85.otg.config.dimensions;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.config.world.WorldConfig;

// Make sure isNewConfig isn't serialised
@JsonIgnoreProperties(value = { "isNewConfig" })
public abstract class DimensionConfigBase
{
	// isNewConfig isn't serialised
	public boolean isNewConfig = false;
	
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice)
	public String PresetName;
	public int DimensionId;
	public boolean ShowInWorldCreationGUI = true;
	public String Seed = WorldStandardValues.WORLD_SEED.getDefaultValue();
    public String GameType = "Survival";
    public boolean BonusChest = false;
    public boolean AllowCheats = false;	
	public int WorldBorderRadiusInChunks = WorldStandardValues.WORLD_BORDER_RADIUS.getDefaultValue();
	public int PregeneratorRadiusInChunks = WorldStandardValues.PREGENERATION_RADIUS.getDefaultValue();
	public SettingsEntry Settings = new SettingsEntry();
	public GameRulesEntry GameRules = new GameRulesEntry();
	
	public DimensionConfigBase() { }
	
	DimensionConfigBase(String presetName, int dimensionId, boolean showInWorldCreationGUI)
	{
		this.PresetName = presetName;
		this.DimensionId = dimensionId;
		this.ShowInWorldCreationGUI = showInWorldCreationGUI;
	}
	
	DimensionConfigBase(String presetName, int dimensionId, boolean showInWorldCreationGUI, WorldConfig worldConfig)
	{
		this.PresetName = presetName;
		this.DimensionId = dimensionId;
		this.ShowInWorldCreationGUI = showInWorldCreationGUI;
		this.Seed = worldConfig.worldSeed;
		this.WorldBorderRadiusInChunks = worldConfig.worldBorderRadius;
		this.PregeneratorRadiusInChunks = worldConfig.preGenerationRadius;
		
		this.Settings.CanDoLightning = worldConfig.canDoLightning;
		this.Settings.CanDoRainSnowIce = worldConfig.canDoRainSnowIce;
		this.Settings.CanDropChunk = worldConfig.canDropChunk;
		this.Settings.CanRespawnHere = worldConfig.canRespawnHere;
		this.Settings.CloudHeight = worldConfig.cloudHeight;
		this.Settings.DepartMessage = worldConfig.departMessage;
		this.Settings.DimensionAbove = worldConfig.dimensionAbove;
		this.Settings.DimensionAboveHeight = worldConfig.dimensionAboveHeight;
		this.Settings.DimensionBelow = worldConfig.dimensionBelow;
		this.Settings.DimensionBelowHeight = worldConfig.dimensionBelowHeight;
		LocalMaterialData[] portalMats = worldConfig.dimensionPortalMaterials.toArray(new LocalMaterialData[0]);
		ArrayList<String> portalMaterials = new ArrayList<String>();
		for(LocalMaterialData mat : portalMats)
		{
			portalMaterials.add(mat.getName().toUpperCase());
		}
		this.Settings.DimensionPortalMaterials = portalMaterials.toArray(new String[0]);
		this.Settings.PortalColor = worldConfig.portalColor;
		this.Settings.PortalParticleType = worldConfig.portalParticleType; 
		this.Settings.PortalMobType = worldConfig.portalMobType;
		this.Settings.PortalMobSpawnChance = worldConfig.portalMobSpawnChance;		
		this.Settings.DoesWaterVaporize = worldConfig.doesWaterVaporize;
		this.Settings.DoesXZShowFog = worldConfig.doesXZShowFog;
		this.Settings.ExplosionsCanBreakBlocks = worldConfig.explosionsCanBreakBlocks;
		this.Settings.FogColorBlue = worldConfig.fogColorBlue;
		this.Settings.FogColorGreen = worldConfig.fogColorGreen;
		this.Settings.FogColorRed = worldConfig.fogColorRed;
		this.Settings.GravityFactor = worldConfig.gravityFactor;
		this.Settings.HasSkyLight = worldConfig.hasSkyLight;
		this.Settings.IsNightWorld = worldConfig.isNightWorld;
		this.Settings.IsSkyColored = worldConfig.isSkyColored;
		this.Settings.IsSurfaceWorld = worldConfig.isSurfaceWorld;
		this.Settings.ItemsToAddOnJoinDimension = worldConfig.itemsToAddOnJoinDimension;
		this.Settings.ItemsToAddOnLeaveDimension = worldConfig.itemsToAddOnLeaveDimension;
		this.Settings.ItemsToAddOnRespawn = worldConfig.itemsToAddOnRespawn;
		this.Settings.ItemsToRemoveOnJoinDimension = worldConfig.itemsToRemoveOnJoinDimension;
		this.Settings.ItemsToRemoveOnLeaveDimension = worldConfig.itemsToRemoveOnLeaveDimension;
		this.Settings.MovementFactor = worldConfig.movementFactor;
		this.Settings.PlayersCanBreakBlocks = worldConfig.playersCanBreakBlocks;
		this.Settings.PlayersCanPlaceBlocks = worldConfig.playersCanPlaceBlocks;
		this.Settings.RespawnDimension = worldConfig.respawnDimension;
		this.Settings.ShouldMapSpin = worldConfig.shouldMapSpin;
		this.Settings.SpawnPointSet = worldConfig.spawnPointSet;
		this.Settings.SpawnPointX = worldConfig.spawnPointX;
		this.Settings.SpawnPointY = worldConfig.spawnPointY;
		this.Settings.SpawnPointZ = worldConfig.spawnPointZ;
		this.Settings.IsOTGPlus = worldConfig.isOTGPlus;
		this.Settings.TeleportToSpawnOnly = worldConfig.teleportToSpawnOnly;
		this.Settings.UseCustomFogColor = worldConfig.useCustomFogColor;
		this.Settings.VoidFogYFactor = worldConfig.voidFogYFactor;
		this.Settings.WelcomeMessage = worldConfig.welcomeMessage;

		this.GameRules.CommandBlockOutput = Boolean.parseBoolean(worldConfig.commandBlockOutput);
		this.GameRules.DisableElytraMovementCheck = Boolean.parseBoolean(worldConfig.disableElytraMovementCheck);
		this.GameRules.DoDaylightCycle = Boolean.parseBoolean(worldConfig.doDaylightCycle);
		this.GameRules.DoEntityDrops = Boolean.parseBoolean(worldConfig.doEntityDrops);
		this.GameRules.DoFireTick = Boolean.parseBoolean(worldConfig.doFireTick);
		this.GameRules.DoLimitedCrafting = Boolean.parseBoolean(worldConfig.doLimitedCrafting);
		this.GameRules.DoMobLoot = Boolean.parseBoolean(worldConfig.doMobLoot);
		this.GameRules.DoMobSpawning = Boolean.parseBoolean(worldConfig.doMobSpawning);
		this.GameRules.DoTileDrops = Boolean.parseBoolean(worldConfig.doTileDrops);
		this.GameRules.DoWeatherCycle = Boolean.parseBoolean(worldConfig.doWeatherCycle);
		this.GameRules.GameLoopFunction = Boolean.parseBoolean(worldConfig.gameLoopFunction);
		this.GameRules.KeepInventory = Boolean.parseBoolean(worldConfig.keepInventory);
		this.GameRules.LogAdminCommands = Boolean.parseBoolean(worldConfig.logAdminCommands);
		this.GameRules.MaxCommandChainLength = Integer.parseInt(worldConfig.maxCommandChainLength);
		this.GameRules.MaxEntityCramming = Integer.parseInt(worldConfig.maxEntityCramming);
		this.GameRules.MobGriefing = Boolean.parseBoolean(worldConfig.mobGriefing);
		this.GameRules.NaturalRegeneration = Boolean.parseBoolean(worldConfig.naturalRegeneration);
		this.GameRules.RandomTickSpeed = Integer.parseInt(worldConfig.randomTickSpeed);
		this.GameRules.ReducedDebugInfo = Boolean.parseBoolean(worldConfig.reducedDebugInfo);
		this.GameRules.SendCommandFeedback = Boolean.parseBoolean(worldConfig.sendCommandFeedback);
		this.GameRules.ShowDeathMessages = Boolean.parseBoolean(worldConfig.showDeathMessages);
		this.GameRules.SpawnRadius = Integer.parseInt(worldConfig.spawnRadius);
		this.GameRules.SpectatorsGenerateChunks = Boolean.parseBoolean(worldConfig.spectatorsGenerateChunks);
	}	

	public String toYamlString()
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
}