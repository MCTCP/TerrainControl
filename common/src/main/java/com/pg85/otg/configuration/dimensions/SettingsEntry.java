package com.pg85.otg.configuration.dimensions;

import java.util.ArrayList;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.materials.MaterialHelper;

public class SettingsEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice)
	public String[] DimensionPortalMaterials = new String[] { "DIRT" }; // WorldStandardValues.DIMENSION_PORTAL_MATERIALS.getDefaultValue(); // TODO: Fetch from worldstandardvalues
	public ArrayList<LocalMaterialData> GetDimensionPortalMaterials()
	{
        ArrayList<LocalMaterialData> portalMaterials = new ArrayList<LocalMaterialData>();                
    	for(String mat : this.DimensionPortalMaterials)
    	{
    		LocalMaterialData forgeMat;
			try {
				forgeMat = MaterialHelper.readMaterial(mat);
				portalMaterials.add(forgeMat);
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
    	}
    	return portalMaterials;
	}	
	
	public boolean IsOTGPlus = WorldStandardValues.IS_OTG_PLUS.getDefaultValue();
	public String DimensionBelow = WorldStandardValues.DIMENSIONBELOW.getDefaultValue();
	public String DimensionAbove = WorldStandardValues.DIMENSIONABOVE.getDefaultValue();
	public int DimensionBelowHeight = WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue();
	public int DimensionAboveHeight = WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue();
	public boolean TeleportToSpawnOnly = WorldStandardValues.TeleportToSpawnOnly.getDefaultValue();
	public String WelcomeMessage = WorldStandardValues.WelcomeMessage.getDefaultValue();
	public String DepartMessage = WorldStandardValues.DepartMessage.getDefaultValue();
	public boolean HasSkyLight = WorldStandardValues.HasSkyLight.getDefaultValue();
	public boolean IsSurfaceWorld = WorldStandardValues.IsSurfaceWorld.getDefaultValue();
	public boolean CanRespawnHere = WorldStandardValues.CanRespawnHere.getDefaultValue();
	public boolean DoesWaterVaporize = WorldStandardValues.DoesWaterVaporize.getDefaultValue();
	public boolean DoesXZShowFog = WorldStandardValues.DoesXZShowFog.getDefaultValue();
	public boolean UseCustomFogColor = WorldStandardValues.UseCustomFogColor.getDefaultValue();
	public double FogColorRed = WorldStandardValues.FogColorRed.getDefaultValue();
	public double FogColorGreen = WorldStandardValues.FogColorGreen.getDefaultValue();
	public double FogColorBlue = WorldStandardValues.FogColorBlue.getDefaultValue();
	public boolean IsSkyColored = WorldStandardValues.IsSkyColored.getDefaultValue();
	public int CloudHeight = WorldStandardValues.CloudHeight.getDefaultValue();
	public boolean CanDoLightning = WorldStandardValues.CanDoLightning.getDefaultValue();
	public boolean CanDoRainSnowIce = WorldStandardValues.CanDoRainSnowIce.getDefaultValue();
	public boolean IsNightWorld = WorldStandardValues.IsNightWorld.getDefaultValue();
	public double VoidFogYFactor = WorldStandardValues.VoidFogYFactor.getDefaultValue();
	public double GravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	public boolean ShouldMapSpin = WorldStandardValues.ShouldMapSpin.getDefaultValue();
	public boolean CanDropChunk = WorldStandardValues.CanDropChunk.getDefaultValue();
	public int RespawnDimension = WorldStandardValues.RESPAWN_DIMENSION.getDefaultValue();
	public int MovementFactor = WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue();
	public String ItemsToAddOnJoinDimension = WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION.getDefaultValue();
	public String ItemsToRemoveOnJoinDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION.getDefaultValue();
	public String ItemsToAddOnLeaveDimension = WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION.getDefaultValue();
	public String ItemsToRemoveOnLeaveDimension = WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION.getDefaultValue();
	public String ItemsToAddOnRespawn = WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN.getDefaultValue();
	public boolean SpawnPointSet = WorldStandardValues.SPAWN_POINT_SET.getDefaultValue();
	public int SpawnPointX = WorldStandardValues.SPAWN_POINT_X.getDefaultValue();
	public int SpawnPointY = WorldStandardValues.SPAWN_POINT_Y.getDefaultValue();
	public int SpawnPointZ = WorldStandardValues.SPAWN_POINT_Z.getDefaultValue();
	public boolean PlayersCanBreakBlocks = WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS.getDefaultValue();
	public boolean ExplosionsCanBreakBlocks = WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS.getDefaultValue();
	public boolean PlayersCanPlaceBlocks = WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS.getDefaultValue();
	
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