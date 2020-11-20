package com.pg85.otg.config.dimensions;

import java.util.ArrayList;

import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

public class SettingsEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice)
	public String[] DimensionPortalMaterials = new String[] { "DIRT" }; // WorldStandardValues.DIMENSION_PORTAL_MATERIALS.getDefaultValue(); // TODO: Fetch from worldstandardvalues
	public String PortalColor = WorldStandardValues.PORTAL_COLOR.getDefaultValue(null);
	public String PortalParticleType = WorldStandardValues.PORTAL_PARTICLE_TYPE.getDefaultValue(null); 
	public String PortalMobType = WorldStandardValues.PORTAL_MOB_TYPE.getDefaultValue(null);
	public int PortalMobSpawnChance = WorldStandardValues.PORTAL_MOB_SPAWN_CHANCE.getDefaultValue(null);
	
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
	
	public boolean IsOTGPlus = WorldStandardValues.IS_OTG_PLUS.getDefaultValue(null);
	public String DimensionBelow = WorldStandardValues.DIMENSIONBELOW.getDefaultValue(null);
	public String DimensionAbove = WorldStandardValues.DIMENSIONABOVE.getDefaultValue(null);
	public int DimensionBelowHeight = WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue(null);
	public int DimensionAboveHeight = WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue(null);
	public boolean TeleportToSpawnOnly = WorldStandardValues.TeleportToSpawnOnly.getDefaultValue(null);
	public String WelcomeMessage = WorldStandardValues.WelcomeMessage.getDefaultValue(null);
	public String DepartMessage = WorldStandardValues.DepartMessage.getDefaultValue(null);
	public boolean HasSkyLight = WorldStandardValues.HasSkyLight.getDefaultValue(null);
	public boolean IsSurfaceWorld = WorldStandardValues.IsSurfaceWorld.getDefaultValue(null);
	public boolean CanRespawnHere = WorldStandardValues.CanRespawnHere.getDefaultValue(null);
	public boolean DoesWaterVaporize = WorldStandardValues.DoesWaterVaporize.getDefaultValue(null);
	public boolean DoesXZShowFog = WorldStandardValues.DoesXZShowFog.getDefaultValue(null);
	public boolean UseCustomFogColor = WorldStandardValues.UseCustomFogColor.getDefaultValue(null);
	public double FogColorRed = WorldStandardValues.FogColorRed.getDefaultValue(null);
	public double FogColorGreen = WorldStandardValues.FogColorGreen.getDefaultValue(null);
	public double FogColorBlue = WorldStandardValues.FogColorBlue.getDefaultValue(null);
	public boolean IsSkyColored = WorldStandardValues.IsSkyColored.getDefaultValue(null);
	public int CloudHeight = WorldStandardValues.CloudHeight.getDefaultValue(null);
	public boolean CanDoLightning = WorldStandardValues.CanDoLightning.getDefaultValue(null);
	public boolean CanDoRainSnowIce = WorldStandardValues.CanDoRainSnowIce.getDefaultValue(null);
	public boolean IsNightWorld = WorldStandardValues.IsNightWorld.getDefaultValue(null);
	public double VoidFogYFactor = WorldStandardValues.VoidFogYFactor.getDefaultValue(null);
	public double GravityFactor = WorldStandardValues.GravityFactor.getDefaultValue(null);
	public boolean ShouldMapSpin = WorldStandardValues.ShouldMapSpin.getDefaultValue(null);
	public boolean CanDropChunk = WorldStandardValues.CanDropChunk.getDefaultValue(null);
	public int RespawnDimension = WorldStandardValues.RESPAWN_DIMENSION.getDefaultValue(null);
	public int MovementFactor = WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue(null);
	public String ItemsToAddOnJoinDimension = WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue(null);
	public String ItemsToRemoveOnJoinDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue(null);
	public String ItemsToAddOnLeaveDimension = WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue(null);
	public String ItemsToRemoveOnLeaveDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue(null);
	public String ItemsToAddOnRespawn = WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue(null);
	public boolean SpawnPointSet = WorldStandardValues.SPAWN_POINT_SET.getDefaultValue(null);
	public int SpawnPointX = WorldStandardValues.SPAWN_POINT_X.getDefaultValue(null);
	public int SpawnPointY = WorldStandardValues.SPAWN_POINT_Y.getDefaultValue(null);
	public int SpawnPointZ = WorldStandardValues.SPAWN_POINT_Z.getDefaultValue(null);
	public boolean PlayersCanBreakBlocks = WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS.getDefaultValue(null);
	public boolean ExplosionsCanBreakBlocks = WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS.getDefaultValue(null);
	public boolean PlayersCanPlaceBlocks = WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS.getDefaultValue(null);
	
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
