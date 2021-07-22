package com.pg85.otg.forge.dimensions.portals;

import com.pg85.otg.constants.Constants;

import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class OTGPortalPois
{
	public static final DeferredRegister<PointOfInterestType> poi = DeferredRegister.create(ForgeRegistries.POI_TYPES, Constants.MOD_ID_SHORT);

	public static final RegistryObject<PointOfInterestType> portalOTGBeige = poi.register(OTGPortalColors.blockPortalOTGBeigeName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGBeigeName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGBeige.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGBlack = poi.register(OTGPortalColors.blockPortalOTGBlackName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGBlackName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGBlack.get()), 0, 1));	
	public static final RegistryObject<PointOfInterestType> portalOTGBlue = poi.register(OTGPortalColors.blockPortalOTGBlueName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGBlueName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGBlue.get()), 0, 1));	
	public static final RegistryObject<PointOfInterestType> portalOTGCrystalBlue = poi.register(OTGPortalColors.blockPortalOTGCrystalBlueName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGCrystalBlueName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGCrystalBlue.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGDarkBlue = poi.register(OTGPortalColors.blockPortalOTGDarkBlueName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGDarkBlueName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkBlue.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGDarkGreen = poi.register(OTGPortalColors.blockPortalOTGDarkGreenName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGDarkGreenName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkGreen.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGDarkRed = poi.register(OTGPortalColors.blockPortalOTGDarkRedName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGDarkRedName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkRed.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGEmerald = poi.register(OTGPortalColors.blockPortalOTGEmeraldName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGEmeraldName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGEmerald.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGFlame = poi.register(OTGPortalColors.blockPortalOTGFlameName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGFlameName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGFlame.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGGold = poi.register(OTGPortalColors.blockPortalOTGGoldName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGGoldName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGGold.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGGreen = poi.register(OTGPortalColors.blockPortalOTGGreenName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGGreenName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGGreen.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGGrey = poi.register(OTGPortalColors.blockPortalOTGGreyName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGGreyName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGGrey.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGLightBlue = poi.register(OTGPortalColors.blockPortalOTGLightBlueName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGLightBlueName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGLightBlue.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGLightGreen = poi.register(OTGPortalColors.blockPortalOTGLightGreenName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGLightGreenName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGLightGreen.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGOrange = poi.register(OTGPortalColors.blockPortalOTGOrangeName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGOrangeName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGOrange.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGPink = poi.register(OTGPortalColors.blockPortalOTGPinkName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGPinkName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGPink.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGRed = poi.register(OTGPortalColors.blockPortalOTGRedName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGRedName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGRed.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGWhite = poi.register(OTGPortalColors.blockPortalOTGWhiteName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGWhiteName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGWhite.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTGYellow = poi.register(OTGPortalColors.blockPortalOTGYellowName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGYellowName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTGYellow.get()), 0, 1));
	public static final RegistryObject<PointOfInterestType> portalOTG = poi.register(OTGPortalColors.blockPortalOTGDefaultName, () -> new PointOfInterestType(OTGPortalColors.blockPortalOTGDefaultName, PointOfInterestType.getBlockStates(OTGPortalBlocks.blockPortalOTG.get()), 0, 1));
}
