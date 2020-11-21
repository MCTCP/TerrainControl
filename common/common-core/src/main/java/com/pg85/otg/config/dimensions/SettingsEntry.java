package com.pg85.otg.config.dimensions;

import java.util.ArrayList;

import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

class SettingsEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice)
	String[] DimensionPortalMaterials = new String[] { "DIRT" }; // WorldStandardValues.DIMENSION_PORTAL_MATERIALS.getDefaultValue(); // TODO: Fetch from worldstandardvalues
	String PortalColor = WorldStandardValues.PORTAL_COLOR.getDefaultValue(null);
	String PortalParticleType = WorldStandardValues.PORTAL_PARTICLE_TYPE.getDefaultValue(null); 
	String PortalMobType = WorldStandardValues.PORTAL_MOB_TYPE.getDefaultValue(null);
	int PortalMobSpawnChance = WorldStandardValues.PORTAL_MOB_SPAWN_CHANCE.getDefaultValue(null);
	
	public ArrayList<LocalMaterialData> GetDimensionPortalMaterials(IMaterialReader materialReader)
	{
        ArrayList<LocalMaterialData> portalMaterials = new ArrayList<LocalMaterialData>();                
    	for(String mat : this.DimensionPortalMaterials)
    	{
    		LocalMaterialData forgeMat;
			try {
				forgeMat = materialReader.readMaterial(mat);
				portalMaterials.add(forgeMat);
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
    	}
    	return portalMaterials;
	}	
	
	boolean IsOTGPlus = WorldStandardValues.IS_OTG_PLUS.getDefaultValue(null);
	String DimensionBelow = WorldStandardValues.DIMENSIONBELOW.getDefaultValue(null);
	String DimensionAbove = WorldStandardValues.DIMENSIONABOVE.getDefaultValue(null);
	int DimensionBelowHeight = WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue(null);
	int DimensionAboveHeight = WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue(null);
	boolean TeleportToSpawnOnly = WorldStandardValues.TeleportToSpawnOnly.getDefaultValue(null);
	String WelcomeMessage = WorldStandardValues.WelcomeMessage.getDefaultValue(null);
	String DepartMessage = WorldStandardValues.DepartMessage.getDefaultValue(null);
	boolean HasSkyLight = WorldStandardValues.HasSkyLight.getDefaultValue(null);
	boolean IsSurfaceWorld = WorldStandardValues.IsSurfaceWorld.getDefaultValue(null);
	boolean CanRespawnHere = WorldStandardValues.CanRespawnHere.getDefaultValue(null);
	boolean DoesWaterVaporize = WorldStandardValues.DoesWaterVaporize.getDefaultValue(null);
	boolean DoesXZShowFog = WorldStandardValues.DoesXZShowFog.getDefaultValue(null);
	boolean UseCustomFogColor = WorldStandardValues.UseCustomFogColor.getDefaultValue(null);
	double FogColorRed = WorldStandardValues.FogColorRed.getDefaultValue(null);
	double FogColorGreen = WorldStandardValues.FogColorGreen.getDefaultValue(null);
	double FogColorBlue = WorldStandardValues.FogColorBlue.getDefaultValue(null);
	boolean IsSkyColored = WorldStandardValues.IsSkyColored.getDefaultValue(null);
	int CloudHeight = WorldStandardValues.CloudHeight.getDefaultValue(null);
	boolean CanDoLightning = WorldStandardValues.CanDoLightning.getDefaultValue(null);
	boolean CanDoRainSnowIce = WorldStandardValues.CanDoRainSnowIce.getDefaultValue(null);
	boolean IsNightWorld = WorldStandardValues.IsNightWorld.getDefaultValue(null);
	double VoidFogYFactor = WorldStandardValues.VoidFogYFactor.getDefaultValue(null);
	double GravityFactor = WorldStandardValues.GravityFactor.getDefaultValue(null);
	boolean ShouldMapSpin = WorldStandardValues.ShouldMapSpin.getDefaultValue(null);
	boolean CanDropChunk = WorldStandardValues.CanDropChunk.getDefaultValue(null);
	int RespawnDimension = WorldStandardValues.RESPAWN_DIMENSION.getDefaultValue(null);
	int MovementFactor = WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue(null);
	String ItemsToAddOnJoinDimension = WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue(null);
	String ItemsToRemoveOnJoinDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue(null);
	String ItemsToAddOnLeaveDimension = WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue(null);
	String ItemsToRemoveOnLeaveDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue(null);
	String ItemsToAddOnRespawn = WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue(null);
	boolean SpawnPointSet = WorldStandardValues.SPAWN_POINT_SET.getDefaultValue(null);
	int SpawnPointX = WorldStandardValues.SPAWN_POINT_X.getDefaultValue(null);
	int SpawnPointY = WorldStandardValues.SPAWN_POINT_Y.getDefaultValue(null);
	int SpawnPointZ = WorldStandardValues.SPAWN_POINT_Z.getDefaultValue(null);
	boolean PlayersCanBreakBlocks = WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS.getDefaultValue(null);
	boolean ExplosionsCanBreakBlocks = WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS.getDefaultValue(null);
	boolean PlayersCanPlaceBlocks = WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS.getDefaultValue(null);
	
	public SettingsEntry clone()
	{
		SettingsEntry clone = new SettingsEntry();
		
		clone.CanDoLightning = this.CanDoLightning;
		clone.CanDoRainSnowIce = this.CanDoRainSnowIce;
		clone.CanDropChunk = this.CanDropChunk;
		clone.CanRespawnHere = this.CanRespawnHere;
		clone.CloudHeight = this.CloudHeight;
		clone.DepartMessage = this.DepartMessage;
		clone.DimensionAbove = this.DimensionAbove;
		clone.DimensionAboveHeight = this.DimensionAboveHeight;
		clone.DimensionBelow = this.DimensionBelow;
		clone.DimensionBelowHeight = this.DimensionBelowHeight;
		clone.DimensionPortalMaterials = this.DimensionPortalMaterials;
		clone.PortalColor = this.PortalColor;
		clone.PortalParticleType = this.PortalParticleType; 
		clone.PortalMobType = this.PortalMobType;
		clone.PortalMobSpawnChance = this.PortalMobSpawnChance;		
		clone.DoesWaterVaporize = this.DoesWaterVaporize;
		clone.DoesXZShowFog = this.DoesXZShowFog;
		clone.ExplosionsCanBreakBlocks = this.ExplosionsCanBreakBlocks;
		clone.FogColorBlue = this.FogColorBlue;
		clone.FogColorGreen = this.FogColorGreen;
		clone.FogColorRed = this.FogColorRed;
		clone.GravityFactor = this.GravityFactor;
		clone.HasSkyLight = this.HasSkyLight;
		clone.IsNightWorld = this.IsNightWorld;
		clone.IsSkyColored = this.IsSkyColored;
		clone.IsSurfaceWorld = this.IsSurfaceWorld;
		clone.ItemsToAddOnJoinDimension = this.ItemsToAddOnJoinDimension;
		clone.ItemsToAddOnLeaveDimension = this.ItemsToAddOnLeaveDimension;
		clone.ItemsToAddOnRespawn = this.ItemsToAddOnRespawn;
		clone.ItemsToRemoveOnJoinDimension = this.ItemsToRemoveOnJoinDimension;
		clone.ItemsToRemoveOnLeaveDimension = this.ItemsToRemoveOnLeaveDimension;
		clone.MovementFactor = this.MovementFactor;
		clone.PlayersCanBreakBlocks = this.PlayersCanBreakBlocks;
		clone.PlayersCanPlaceBlocks = this.PlayersCanPlaceBlocks;
		clone.RespawnDimension = this.RespawnDimension;
		clone.ShouldMapSpin = this.ShouldMapSpin;
		clone.SpawnPointSet = this.SpawnPointSet;
		clone.SpawnPointX = this.SpawnPointX;
		clone.SpawnPointY = this.SpawnPointY;
		clone.SpawnPointZ = this.SpawnPointZ;
		clone.IsOTGPlus = this.IsOTGPlus;
		clone.TeleportToSpawnOnly = this.TeleportToSpawnOnly;
		clone.UseCustomFogColor = this.UseCustomFogColor;
		clone.VoidFogYFactor = this.VoidFogYFactor;
		clone.WelcomeMessage = this.WelcomeMessage;
		
		return clone;
	}
}
