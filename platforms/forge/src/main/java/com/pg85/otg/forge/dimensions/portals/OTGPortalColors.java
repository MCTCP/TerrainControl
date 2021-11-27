package com.pg85.otg.forge.dimensions.portals;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.fmllegacy.RegistryObject;

public class OTGPortalColors
{
	public static final String portalColorBeige = "beige";
	public static final String portalColorBlack = "black";
	public static final String portalColorBlue = "blue";	
	public static final String portalColorCrystalBlue = "crystalblue";
	public static final String portalColorDarkBlue = "darkblue";
	public static final String portalColorDarkGreen = "darkgreen";
	public static final String portalColorDarkRed = "darkred";
	public static final String portalColorEmerald = "emerald";
	public static final String portalColorFlame = "flame";
	public static final String portalColorGold = "gold";
	public static final String portalColorGreen = "green";
	public static final String portalColorGrey = "grey";
	public static final String portalColorLightBlue = "lightblue";
	public static final String portalColorLightGreen = "lightgreen";
	public static final String portalColorOrange = "orange";
	public static final String portalColorPink = "pink";
	public static final String portalColorRed = "red";
	public static final String portalColorWhite = "white";
	public static final String portalColorYellow = "yellow";
	public static final String portalColorDefault = "default";
	
	public static final String blockPortalOTGBeigeName = "portalotg_beige";
	public static final String blockPortalOTGBlackName = "portalotg_black";
	public static final String blockPortalOTGBlueName = "portalotg_blue";	
	public static final String blockPortalOTGCrystalBlueName = "portalotg_crystalblue";
	public static final String blockPortalOTGDarkBlueName = "portalotg_darkblue";
	public static final String blockPortalOTGDarkGreenName = "portalotg_darkgreen";
	public static final String blockPortalOTGDarkRedName = "portalotg_darkred";
	public static final String blockPortalOTGEmeraldName = "portalotg_emerald";
	public static final String blockPortalOTGFlameName = "portalotg_flame";
	public static final String blockPortalOTGGoldName = "portalotg_gold";
	public static final String blockPortalOTGGreenName = "portalotg_green";
	public static final String blockPortalOTGGreyName = "portalotg_grey";	
	public static final String blockPortalOTGLightBlueName = "portalotg_lightblue";
	public static final String blockPortalOTGLightGreenName = "portalotg_lightgreen";
	public static final String blockPortalOTGOrangeName = "portalotg_orange";
	public static final String blockPortalOTGPinkName = "portalotg_pink";
	public static final String blockPortalOTGRedName = "portalotg_red";
	public static final String blockPortalOTGWhiteName = "portalotg_white";
	public static final String blockPortalOTGYellowName = "portalotg_yellow";
	public static final String blockPortalOTGDefaultName = "portalotg";	
	
	private static final List<String> portalColors = Arrays.asList(
		portalColorDefault,
		portalColorBeige,
		portalColorBlack,
		portalColorBlue,
		portalColorCrystalBlue,
		portalColorDarkBlue,
		portalColorDarkGreen,
		portalColorDarkRed,
		portalColorEmerald,
		portalColorFlame,
		portalColorGold,
		portalColorGreen,
		portalColorGrey,
		portalColorLightBlue,
		portalColorLightGreen,
		portalColorOrange,
		portalColorPink,
		portalColorRed,
		portalColorWhite,
		portalColorYellow
	);
		
	public static RegistryObject<OTGPortalBlock> getPortalBlockByColor(String color)
	{
		if(color == null)
		{
			return OTGPortalBlocks.blockPortalOTG;
		}
		switch(color.trim().toLowerCase())
		{
			case portalColorBeige:
				return OTGPortalBlocks.blockPortalOTGBeige;
			case portalColorBlack:
				return OTGPortalBlocks.blockPortalOTGBlack;
			case portalColorBlue:
				return OTGPortalBlocks.blockPortalOTGBlue;
			case portalColorCrystalBlue:
				return OTGPortalBlocks.blockPortalOTGCrystalBlue;
			case portalColorDarkBlue:
				return OTGPortalBlocks.blockPortalOTGDarkBlue;
			case portalColorDarkGreen:
				return OTGPortalBlocks.blockPortalOTGDarkGreen;
			case portalColorDarkRed:
				return OTGPortalBlocks.blockPortalOTGDarkRed;
			case portalColorEmerald:
				return OTGPortalBlocks.blockPortalOTGEmerald;
			case portalColorFlame:
				return OTGPortalBlocks.blockPortalOTGFlame;				
			case portalColorGold:
				return OTGPortalBlocks.blockPortalOTGGold;				
			case portalColorGreen:
				return OTGPortalBlocks.blockPortalOTGGreen;
			case portalColorGrey:
				return OTGPortalBlocks.blockPortalOTGGrey;				
			case portalColorLightBlue:
				return OTGPortalBlocks.blockPortalOTGLightBlue;				
			case portalColorLightGreen:
				return OTGPortalBlocks.blockPortalOTGLightGreen;				
			case portalColorOrange:
				return OTGPortalBlocks.blockPortalOTGOrange;				
			case portalColorPink:
				return OTGPortalBlocks.blockPortalOTGPink;				
			case portalColorRed:
				return OTGPortalBlocks.blockPortalOTGRed;				
			case portalColorWhite:
				return OTGPortalBlocks.blockPortalOTGWhite;
			case portalColorYellow:
				return OTGPortalBlocks.blockPortalOTGYellow;				
			case portalColorDefault:
			default:
				return OTGPortalBlocks.blockPortalOTG;
		}
	}

	public static RegistryObject<PoiType> getPortalPOIByColor(String color)
	{
		if(color == null)
		{
			return OTGPortalPois.portalOTG;
		}
		switch(color.trim().toLowerCase())
		{
			case portalColorBeige:
				return OTGPortalPois.portalOTGBeige;
			case portalColorBlack:
				return OTGPortalPois.portalOTGBlack;
			case portalColorBlue:
				return OTGPortalPois.portalOTGBlue;
			case portalColorCrystalBlue:
				return OTGPortalPois.portalOTGCrystalBlue;
			case portalColorDarkBlue:
				return OTGPortalPois.portalOTGDarkBlue;
			case portalColorDarkGreen:
				return OTGPortalPois.portalOTGDarkGreen;
			case portalColorDarkRed:
				return OTGPortalPois.portalOTGDarkRed;
			case portalColorEmerald:
				return OTGPortalPois.portalOTGEmerald;
			case portalColorFlame:
				return OTGPortalPois.portalOTGFlame;				
			case portalColorGold:
				return OTGPortalPois.portalOTGGold;				
			case portalColorGreen:
				return OTGPortalPois.portalOTGGreen;
			case portalColorGrey:
				return OTGPortalPois.portalOTGGrey;				
			case portalColorLightBlue:
				return OTGPortalPois.portalOTGLightBlue;				
			case portalColorLightGreen:
				return OTGPortalPois.portalOTGLightGreen;				
			case portalColorOrange:
				return OTGPortalPois.portalOTGOrange;				
			case portalColorPink:
				return OTGPortalPois.portalOTGPink;				
			case portalColorRed:
				return OTGPortalPois.portalOTGRed;				
			case portalColorWhite:
				return OTGPortalPois.portalOTGWhite;
			case portalColorYellow:
				return OTGPortalPois.portalOTGYellow;				
			case portalColorDefault:
			default:
				return OTGPortalPois.portalOTG;
		}
	}
	
	public static String getNextPortalColor(String currentColor)
	{
		if(currentColor != null && !currentColor.toLowerCase().equals(portalColors.get(portalColors.size() - 1)))
		{
			for(int i = 0; i < portalColors.size(); i++)
			{
				if(portalColors.get(i).equals(currentColor.toLowerCase()))
				{
					return portalColors.get(i + 1);
				}
			}
		}
		return portalColors.get(0);
	}
}
