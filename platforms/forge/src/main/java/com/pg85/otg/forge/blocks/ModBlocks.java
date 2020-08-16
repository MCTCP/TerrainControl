package com.pg85.otg.forge.blocks;

import java.util.ArrayList;
import net.minecraft.block.Block;

public class ModBlocks
{
	public static final ArrayList<Block> Blocks = new ArrayList<Block>();
	public static final BlockPortalOTG BlockPortalOTG = new BlockPortalOTG("portalotg");
	public static final BlockPortalOTG BlockPortalOTGBlack = new BlockPortalOTG("portalotg_black");
	public static final BlockPortalOTG BlockPortalOTGBlue = new BlockPortalOTG("portalotg_blue");
	public static final BlockPortalOTG BlockPortalOTGGold = new BlockPortalOTG("portalotg_gold");
	public static final BlockPortalOTG BlockPortalOTGGreen = new BlockPortalOTG("portalotg_green");
	public static final BlockPortalOTG BlockPortalOTGLightBlue = new BlockPortalOTG("portalotg_lightblue");
	public static final BlockPortalOTG BlockPortalOTGLightGreen = new BlockPortalOTG("portalotg_lightgreen");
	public static final BlockPortalOTG BlockPortalOTGOrange = new BlockPortalOTG("portalotg_orange");
	public static final BlockPortalOTG BlockPortalOTGPink = new BlockPortalOTG("portalotg_pink");
	public static final BlockPortalOTG BlockPortalOTGRed = new BlockPortalOTG("portalotg_red");
	public static final BlockPortalOTG BlockPortalOTGWhite = new BlockPortalOTG("portalotg_white");
	
	public static BlockPortalOTG getPortalBlockByColor(String color)
	{
		if(color == null)
		{
			return BlockPortalOTG;
		}
		switch(color.trim().toLowerCase())
		{
			case "black":
				return BlockPortalOTGBlack;
			case "blue":
				return BlockPortalOTGBlue;				
			case "gold":
				return BlockPortalOTGGold;				
			case "green":
				return BlockPortalOTGGreen;				
			case "lightblue":
				return BlockPortalOTGLightBlue;				
			case "lightgreen":
				return BlockPortalOTGLightGreen;				
			case "orange":
				return BlockPortalOTGOrange;				
			case "pink":
				return BlockPortalOTGPink;				
			case "red":
				return BlockPortalOTGRed;				
			case "white":
				return BlockPortalOTGWhite;
			case "default":
			default:
				return BlockPortalOTG;
		}
	}
}
