package com.pg85.otg.forge.blocks;

import java.util.Arrays;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.forge.blocks.portal.BlockPortalOTG;
import com.pg85.otg.logging.LogMarker;

public class PortalColors
{	
	public static final String PortalColorBeige = "beige";
	public static final String PortalColorBlack = "black";
	public static final String PortalColorBlue = "blue";	
	public static final String PortalColorCrystalBlue = "crystalblue";
	public static final String PortalColorDarkBlue = "darkblue";
	public static final String PortalColorDarkGreen = "darkgreen";
	public static final String PortalColorDarkRed = "darkred";
	public static final String PortalColorEmerald = "emerald";
	public static final String PortalColorFlame = "flame";
	public static final String PortalColorGold = "gold";
	public static final String PortalColorGreen = "green";
	public static final String PortalColorGrey = "grey";
	public static final String PortalColorLightBlue = "lightblue";
	public static final String PortalColorLightGreen = "lightgreen";
	public static final String PortalColorOrange = "orange";
	public static final String PortalColorPink = "pink";
	public static final String PortalColorRed = "red";
	public static final String PortalColorWhite = "white";
	public static final String PortalColorYellow = "yellow";
	public static final String PortalColorDefault = "default";
	
	public static List<String> portalColors = Arrays.asList(
		PortalColorDefault,			
		PortalColorBeige,
		PortalColorBlack,
		PortalColorBlue,
		PortalColorCrystalBlue,
		PortalColorDarkBlue,
		PortalColorDarkGreen,
		PortalColorDarkRed,
		PortalColorEmerald,
		PortalColorFlame,
		PortalColorGold,
		PortalColorGreen,
		PortalColorGrey,
		PortalColorLightBlue,
		PortalColorLightGreen,
		PortalColorOrange,
		PortalColorPink,
		PortalColorRed,
		PortalColorWhite,
		PortalColorYellow
	);
		
	public static BlockPortalOTG getPortalBlockByColor(String color)
	{
		if(color == null)
		{
			return ModBlocks.BlockPortalOTG;
		}
		switch(color.trim().toLowerCase())
		{
			case PortalColorBeige:
				return ModBlocks.BlockPortalOTGBeige;
			case PortalColorBlack:
				return ModBlocks.BlockPortalOTGBlack;
			case PortalColorBlue:
				return ModBlocks.BlockPortalOTGBlue;
			case PortalColorCrystalBlue:
				return ModBlocks.BlockPortalOTGCrystalBlue;
			case PortalColorDarkBlue:
				return ModBlocks.BlockPortalOTGDarkBlue;
			case PortalColorDarkGreen:
				return ModBlocks.BlockPortalOTGDarkGreen;
			case PortalColorDarkRed:
				return ModBlocks.BlockPortalOTGDarkRed;
			case PortalColorEmerald:
				return ModBlocks.BlockPortalOTGEmerald;
			case PortalColorFlame:
				return ModBlocks.BlockPortalOTGFlame;				
			case PortalColorGold:
				return ModBlocks.BlockPortalOTGGold;				
			case PortalColorGreen:
				return ModBlocks.BlockPortalOTGGreen;
			case PortalColorGrey:
				return ModBlocks.BlockPortalOTGGrey;				
			case PortalColorLightBlue:
				return ModBlocks.BlockPortalOTGLightBlue;				
			case PortalColorLightGreen:
				return ModBlocks.BlockPortalOTGLightGreen;				
			case PortalColorOrange:
				return ModBlocks.BlockPortalOTGOrange;				
			case PortalColorPink:
				return ModBlocks.BlockPortalOTGPink;				
			case PortalColorRed:
				return ModBlocks.BlockPortalOTGRed;				
			case PortalColorWhite:
				return ModBlocks.BlockPortalOTGWhite;
			case PortalColorYellow:
				return ModBlocks.BlockPortalOTGYellow;				
			case PortalColorDefault:
			default:
				return ModBlocks.BlockPortalOTG;
		}
	}	
	
	public static String getPortalColorByPortalBlock(BlockPortalOTG block)
	{
		if(block.getRegistryName().getPath() == null)
		{
			return null;
		}
		
		switch(block.getRegistryName().getPath())
		{
			case ModBlocks.BlockPortalOTGBeigeName:
				return PortalColorBeige;
			case ModBlocks.BlockPortalOTGBlackName:
				return PortalColorBlack;
			case ModBlocks.BlockPortalOTGBlueName:
				return PortalColorBlue;
			case ModBlocks.BlockPortalOTGCrystalBlueName:
				return PortalColorCrystalBlue;
			case ModBlocks.BlockPortalOTGDarkBlueName:
				return PortalColorDarkBlue;
			case ModBlocks.BlockPortalOTGDarkGreenName:
				return PortalColorDarkGreen;
			case ModBlocks.BlockPortalOTGDarkRedName:
				return PortalColorDarkRed;
			case ModBlocks.BlockPortalOTGEmeraldName:
				return PortalColorEmerald;
			case ModBlocks.BlockPortalOTGFlameName:
				return PortalColorFlame;
			case ModBlocks.BlockPortalOTGGoldName:
				return PortalColorGold;
			case ModBlocks.BlockPortalOTGGreenName:
				return PortalColorGreen;
			case ModBlocks.BlockPortalOTGGreyName:
				return PortalColorGrey;
			case ModBlocks.BlockPortalOTGLightBlueName:
				return PortalColorLightBlue;
			case ModBlocks.BlockPortalOTGLightGreenName:
				return PortalColorLightGreen;
			case ModBlocks.BlockPortalOTGOrangeName:
				return PortalColorOrange;
			case ModBlocks.BlockPortalOTGPinkName:
				return PortalColorPink;
			case ModBlocks.BlockPortalOTGRedName:
				return PortalColorRed;
			case ModBlocks.BlockPortalOTGWhiteName:
				return PortalColorWhite;
			case ModBlocks.BlockPortalOTGYellowName:
				return PortalColorYellow;				
			case ModBlocks.BlockPortalOTGDefaultName:
				return PortalColorDefault;
			default:
				return null;
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
	
	public static void correctPortalColor(DimensionConfig dimConfig, List<DimensionConfig> dimensions)
	{	
        // Ensure the portal color is unique (not already in use), otherwise correct it.
    	if(!isPortalColorFree(dimConfig.Settings.PortalColor, dimensions))
    	{
    		// Change the portal material
    		dimConfig.Settings.PortalColor = PortalColors.getNextFreePortalColor(dimConfig.Settings.PortalColor, dimensions, false);
    		OTG.log(LogMarker.INFO, "Warning: Client tried to create a dimension, but portal color is already in use, changed portal color.");
    	}
	}	
	
	private static boolean isPortalColorFree(String currentColor, List<DimensionConfig> dimensions)
	{
		for (DimensionConfig dimConfig1 : dimensions)
		{
			if (dimConfig1.Settings.PortalColor.toLowerCase().equals(currentColor.toLowerCase()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static String getNextFreePortalColor(String currentColor, List<DimensionConfig> dimensions, boolean allowCurrent)
	{
		String newPortalColor = null;
			
		// If the current color is allowed (fe when clicking the portal colors button  
		// for the next color, when all colors are taken), only allow it once.
		if(allowCurrent)
		{
			boolean bFound = false;
			for (DimensionConfig dimConfig1 : dimensions)
			{
				if (dimConfig1.Settings.PortalColor.toLowerCase().equals(currentColor.toLowerCase()))
				{
					if(bFound)
					{
						// Occurs twice, don't return currentColor.
						allowCurrent = false;
					}
					bFound = true;
				}
			}
		}
		
		boolean bFound;
		newPortalColor = currentColor.toLowerCase();
		while (true)
		{
			newPortalColor = getNextPortalColor(newPortalColor);
			
			// Tried all colors, back at the currentColor
			if(newPortalColor.equals(currentColor.toLowerCase()))
			{
				if(allowCurrent)
				{
					return newPortalColor;	
				} else {
					// Tried all colors, all are in use, return default.
					return PortalColorDefault;
				}
			} else {
				bFound = false;
				for (DimensionConfig dimConfig1 : dimensions)
				{
					if (dimConfig1.Settings.PortalColor.toLowerCase().equals(newPortalColor) )
					{
						bFound = true;
						break;
					}
				}
				if (!bFound)
				{
					// Found free color
					return newPortalColor;
				}
			}
		}
	}	
}
