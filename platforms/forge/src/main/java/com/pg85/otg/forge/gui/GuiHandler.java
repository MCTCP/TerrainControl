package com.pg85.otg.forge.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.gui.mainmenu.OTGGuiWorldSelection;

import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler
{
	public static LinkedHashMap<String, DimensionConfigGui> GuiPresets = new LinkedHashMap<String, DimensionConfigGui>();
	public static boolean IsInMainMenu = false;

	public static void loadGuiPresets()
	{
		GuiPresets.clear();
		
		ArrayList<DimensionsConfig> modPackConfigs = OTG.getEngine().getModPackConfigManager().getAllModPackConfigs();
		
	    ArrayList<String> worldNames = new ArrayList<String>();
	    File OTGWorldsDirectory = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + File.separator + PluginStandardValues.PresetsDirectoryName);
	    if(OTGWorldsDirectory.exists() && OTGWorldsDirectory.isDirectory())
	    {
	    	for(File worldDir : OTGWorldsDirectory.listFiles())
	    	{
	    		if(worldDir.isDirectory())
	    		{
	    			for(File file : worldDir.listFiles())
	    			{
	    				if(file.getName().equals("WorldConfig.ini"))
	    				{
			    			worldNames.add(worldDir.getName());
			    			boolean shouldShow = true;
			    			for(DimensionsConfig modPackConfig : modPackConfigs)
			    			{
			    				// If the preset is an overworld for any modpackconfig, don't hide it in the GUI.
			    				if(modPackConfig.Overworld.PresetName != null && modPackConfig.Overworld.PresetName.equals(worldDir.getName()))
			    				{
			    					shouldShow = true;
			    					break;
			    				}
			    				
			    				if(shouldShow)
			    				{
				    				if(modPackConfig.Dimensions != null)
				    				{
				    					for(DimensionConfig dimConfig : modPackConfig.Dimensions)
				    					{
				    						if(dimConfig.PresetName.equals(worldDir.getName()) && !dimConfig.ShowInWorldCreationGUI)
				    						{
				    							// If a modpack config has set this preset to invisible, hide it.
				    							shouldShow = false;
				    						}
				    					}
				    				}
			    				}
			    			}
			    			WorldConfig worldConfig = WorldConfig.fromDisk(worldDir);
					        GuiPresets.put(worldDir.getName(), new DimensionConfigGui(worldDir.getName(), 0, shouldShow, worldConfig));
					        break;
	    				}
	    			}
	    		}
	    	}
		}
	}
	
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void openGui(GuiOpenEvent event)
    {
        if (event.getGui() instanceof GuiWorldSelection)
        {
        	IsInMainMenu = true;
        	if(modPackConfigAllowsOTGWorldCreationMenu())
        	{
                event.setGui(new OTGGuiWorldSelection());        		
        	}
        }
    }

	public boolean modPackConfigAllowsOTGWorldCreationMenu()
	{
		ArrayList<DimensionsConfig> defaultConfigs = OTG.getEngine().getModPackConfigManager().getAllModPackConfigs();
		
		// If there's a modpack config using a non-otg overworld, 
		// and ShowWorldCreationMenu is set to false, don't show 
		// the OTG world creation menu.
		for(DimensionsConfig dimConfig : defaultConfigs)
		{
			if(!dimConfig.ShowOTGWorldCreationMenu && dimConfig.Overworld.PresetName == null)
			{
				return false;
			}
		}
		return true;
	}
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void closeGui(GuiOpenEvent event) { }

    public void registerKeybindings() {} // TODO: Hook up O menu via here?

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}
