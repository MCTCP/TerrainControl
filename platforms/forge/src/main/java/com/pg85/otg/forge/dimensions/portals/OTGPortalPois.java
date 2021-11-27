package com.pg85.otg.forge.dimensions.portals;

import com.pg85.otg.constants.Constants;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

// Points of interest, used to track OTG portals.
public class OTGPortalPois
{
	public static final DeferredRegister<PoiType> poi = DeferredRegister.create(ForgeRegistries.POI_TYPES, Constants.MOD_ID_SHORT);

	public static final RegistryObject<PoiType> portalOTGBeige = poi.register(OTGPortalColors.blockPortalOTGBeigeName, () -> new PoiType(OTGPortalColors.blockPortalOTGBeigeName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGBeige.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGBlack = poi.register(OTGPortalColors.blockPortalOTGBlackName, () -> new PoiType(OTGPortalColors.blockPortalOTGBlackName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGBlack.get()), 0, 1));	
	public static final RegistryObject<PoiType> portalOTGBlue = poi.register(OTGPortalColors.blockPortalOTGBlueName, () -> new PoiType(OTGPortalColors.blockPortalOTGBlueName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGBlue.get()), 0, 1));	
	public static final RegistryObject<PoiType> portalOTGCrystalBlue = poi.register(OTGPortalColors.blockPortalOTGCrystalBlueName, () -> new PoiType(OTGPortalColors.blockPortalOTGCrystalBlueName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGCrystalBlue.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGDarkBlue = poi.register(OTGPortalColors.blockPortalOTGDarkBlueName, () -> new PoiType(OTGPortalColors.blockPortalOTGDarkBlueName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkBlue.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGDarkGreen = poi.register(OTGPortalColors.blockPortalOTGDarkGreenName, () -> new PoiType(OTGPortalColors.blockPortalOTGDarkGreenName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkGreen.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGDarkRed = poi.register(OTGPortalColors.blockPortalOTGDarkRedName, () -> new PoiType(OTGPortalColors.blockPortalOTGDarkRedName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGDarkRed.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGEmerald = poi.register(OTGPortalColors.blockPortalOTGEmeraldName, () -> new PoiType(OTGPortalColors.blockPortalOTGEmeraldName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGEmerald.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGFlame = poi.register(OTGPortalColors.blockPortalOTGFlameName, () -> new PoiType(OTGPortalColors.blockPortalOTGFlameName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGFlame.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGGold = poi.register(OTGPortalColors.blockPortalOTGGoldName, () -> new PoiType(OTGPortalColors.blockPortalOTGGoldName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGGold.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGGreen = poi.register(OTGPortalColors.blockPortalOTGGreenName, () -> new PoiType(OTGPortalColors.blockPortalOTGGreenName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGGreen.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGGrey = poi.register(OTGPortalColors.blockPortalOTGGreyName, () -> new PoiType(OTGPortalColors.blockPortalOTGGreyName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGGrey.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGLightBlue = poi.register(OTGPortalColors.blockPortalOTGLightBlueName, () -> new PoiType(OTGPortalColors.blockPortalOTGLightBlueName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGLightBlue.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGLightGreen = poi.register(OTGPortalColors.blockPortalOTGLightGreenName, () -> new PoiType(OTGPortalColors.blockPortalOTGLightGreenName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGLightGreen.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGOrange = poi.register(OTGPortalColors.blockPortalOTGOrangeName, () -> new PoiType(OTGPortalColors.blockPortalOTGOrangeName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGOrange.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGPink = poi.register(OTGPortalColors.blockPortalOTGPinkName, () -> new PoiType(OTGPortalColors.blockPortalOTGPinkName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGPink.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGRed = poi.register(OTGPortalColors.blockPortalOTGRedName, () -> new PoiType(OTGPortalColors.blockPortalOTGRedName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGRed.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGWhite = poi.register(OTGPortalColors.blockPortalOTGWhiteName, () -> new PoiType(OTGPortalColors.blockPortalOTGWhiteName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGWhite.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTGYellow = poi.register(OTGPortalColors.blockPortalOTGYellowName, () -> new PoiType(OTGPortalColors.blockPortalOTGYellowName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTGYellow.get()), 0, 1));
	public static final RegistryObject<PoiType> portalOTG = poi.register(OTGPortalColors.blockPortalOTGDefaultName, () -> new PoiType(OTGPortalColors.blockPortalOTGDefaultName, PoiType.getBlockStates(OTGPortalBlocks.blockPortalOTG.get()), 0, 1));
}
