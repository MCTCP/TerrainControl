package com.pg85.otg.forge.dimensions.portals;

import com.pg85.otg.constants.Constants;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class OTGPortalBlocks
{
	public static final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID_SHORT);

	public static final RegistryObject<OTGPortalBlock> blockPortalOTGBeige = blocks.register(OTGPortalColors.blockPortalOTGBeigeName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorBeige));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGBlack = blocks.register(OTGPortalColors.blockPortalOTGBlackName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorBlack));	
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGBlue = blocks.register(OTGPortalColors.blockPortalOTGBlueName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorBlue));	
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGCrystalBlue = blocks.register(OTGPortalColors.blockPortalOTGCrystalBlueName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorCrystalBlue));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGDarkBlue = blocks.register(OTGPortalColors.blockPortalOTGDarkBlueName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorDarkBlue));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGDarkGreen = blocks.register(OTGPortalColors.blockPortalOTGDarkGreenName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorDarkGreen));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGDarkRed = blocks.register(OTGPortalColors.blockPortalOTGDarkRedName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorDarkRed));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGEmerald = blocks.register(OTGPortalColors.blockPortalOTGEmeraldName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorEmerald));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGFlame = blocks.register(OTGPortalColors.blockPortalOTGFlameName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorFlame));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGGold = blocks.register(OTGPortalColors.blockPortalOTGGoldName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorGold));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGGreen = blocks.register(OTGPortalColors.blockPortalOTGGreenName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorGreen));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGGrey = blocks.register(OTGPortalColors.blockPortalOTGGreyName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorGrey));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGLightBlue = blocks.register(OTGPortalColors.blockPortalOTGLightBlueName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorLightBlue));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGLightGreen = blocks.register(OTGPortalColors.blockPortalOTGLightGreenName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorLightGreen));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGOrange = blocks.register(OTGPortalColors.blockPortalOTGOrangeName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorOrange));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGPink = blocks.register(OTGPortalColors.blockPortalOTGPinkName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorPink));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGRed = blocks.register(OTGPortalColors.blockPortalOTGRedName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorRed));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGWhite = blocks.register(OTGPortalColors.blockPortalOTGWhiteName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorWhite));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTGYellow = blocks.register(OTGPortalColors.blockPortalOTGYellowName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorYellow));
	public static final RegistryObject<OTGPortalBlock> blockPortalOTG = blocks.register(OTGPortalColors.blockPortalOTGDefaultName, () -> new OTGPortalBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), OTGPortalColors.portalColorDefault));
}
