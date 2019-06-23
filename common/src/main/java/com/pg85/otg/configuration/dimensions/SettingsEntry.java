package com.pg85.otg.configuration.dimensions;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.exception.InvalidConfigException;

public class SettingsEntry
{
	public String[] DimensionPortalMaterials = new String[] { "DIRT" }; // WorldStandardValues.DIMENSION_PORTAL_MATERIALS.getDefaultValue(); // TODO: Fetch from worldstandardvalues
	public ArrayList<LocalMaterialData> GetDimensionPortalMaterials()
	{
        ArrayList<LocalMaterialData> portalMaterials = new ArrayList<LocalMaterialData>();                
    	for(String mat : this.DimensionPortalMaterials)
    	{
    		LocalMaterialData forgeMat;
			try {
				forgeMat = OTG.readMaterial(mat);
				portalMaterials.add(forgeMat);
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
    	}
    	return portalMaterials;
	}	
	
	public String DimensionBelow = WorldStandardValues.DIMENSIONBELOW.getDefaultValue();
	public String DimensionAbove = WorldStandardValues.DIMENSIONABOVE.getDefaultValue();
	public int DimensionBelowHeight = WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue();
	public int DimensionAboveHeight = WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue();
	public boolean TeleportToSpawnOnly = WorldStandardValues.teleportToSpawnOnly.getDefaultValue();
	public String WelcomeMessage = WorldStandardValues.welcomeMessage.getDefaultValue();
	public String DepartMessage = WorldStandardValues.departMessage.getDefaultValue();
	public boolean HasSkyLight = WorldStandardValues.hasSkyLight.getDefaultValue();
	public boolean IsSurfaceWorld = WorldStandardValues.isSurfaceWorld.getDefaultValue();
	public boolean CanRespawnHere = WorldStandardValues.canRespawnHere.getDefaultValue();
	public boolean DoesWaterVaporize = WorldStandardValues.doesWaterVaporize.getDefaultValue();
	public boolean DoesXZShowFog = WorldStandardValues.doesXZShowFog.getDefaultValue();
	public boolean UseCustomFogColor = WorldStandardValues.useCustomFogColor.getDefaultValue();
	public double FogColorRed = WorldStandardValues.fogColorRed.getDefaultValue();
	public double FogColorGreen = WorldStandardValues.fogColorGreen.getDefaultValue();
	public double FogColorBlue = WorldStandardValues.fogColorBlue.getDefaultValue();
	public boolean IsSkyColored = WorldStandardValues.isSkyColored.getDefaultValue();
	public int CloudHeight = WorldStandardValues.cloudHeight.getDefaultValue();
	public boolean CanDoLightning = WorldStandardValues.canDoLightning.getDefaultValue();
	public boolean CanDoRainSnowIce = WorldStandardValues.canDoRainSnowIce.getDefaultValue();
	public boolean IsNightWorld = WorldStandardValues.isNightWorld.getDefaultValue();
	public double VoidFogYFactor = WorldStandardValues.voidFogYFactor.getDefaultValue();
	public double GravityFactor = WorldStandardValues.gravityFactor.getDefaultValue();
	public boolean ShouldMapSpin = WorldStandardValues.shouldMapSpin.getDefaultValue();
	public boolean CanDropChunk = WorldStandardValues.canDropChunk.getDefaultValue();
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
		clone.TeleportToSpawnOnly = this.TeleportToSpawnOnly;
		clone.UseCustomFogColor = this.UseCustomFogColor;
		clone.VoidFogYFactor = this.VoidFogYFactor;
		clone.WelcomeMessage = this.WelcomeMessage;
		
		return clone;
	}
}