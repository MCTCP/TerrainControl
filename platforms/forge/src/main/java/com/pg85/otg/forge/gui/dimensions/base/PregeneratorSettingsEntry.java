package com.pg85.otg.forge.gui.dimensions.base;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionSettingsList;
import com.pg85.otg.forge.pregenerator.Pregenerator;
import com.pg85.otg.forge.world.ForgeWorldSession;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PregeneratorSettingsEntry implements IGuiListEntry
{
	private final OTGGuiDimensionSettingsList otgGuiDimensionSettingsList;
	private final OTGGuiDimensionSettingsList parent;
    private Pregenerator pregenerator = null;
    
    public PregeneratorSettingsEntry(OTGGuiDimensionSettingsList otgGuiDimensionSettingsList, OTGGuiDimensionSettingsList parent)
    {
        this.otgGuiDimensionSettingsList = otgGuiDimensionSettingsList;
		this.parent = parent;
    }
    
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
    	if(this.pregenerator == null)
    	{
			ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
			if(forgeWorld == null)
			{
				forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimension.PresetName);
			}
			pregenerator = ((ForgeWorldSession)forgeWorld.getWorldSession()).getPregenerator();
    	}

		ArrayList<String> lines = new ArrayList<String>();
		
		lines.add("");
		lines.add("Pre-generating " + (pregenerator.progressScreenWorldSizeInBlocks > 0 ? pregenerator.progressScreenWorldSizeInBlocks + "x" + pregenerator.progressScreenWorldSizeInBlocks  + " blocks" : ""));
		lines.add("Progress: " + pregenerator.preGeneratorProgress + "% (" + pregenerator.progressScreenCycle + "/" + pregenerator.progressScreenRadius + ")");
		lines.add("Chunks: " + pregenerator.preGeneratorProgressStatus);
		lines.add("Elapsed: " + pregenerator.progressScreenElapsedTime);
		lines.add("Estimated: " + pregenerator.progressScreenEstimatedTime);

		if(Minecraft.getMinecraft().isSingleplayer())
		{
			long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	        lines.add("Memory: " + Long.valueOf(bytesToMb(l)) + "/" +  Long.valueOf(bytesToMb(i)) + " MB");
		} else {
			lines.add("Memory: " + pregenerator.progressScreenServerUsedMbs + "/" +  pregenerator.progressScreenServerTotalMbs + " MB");
		}

        int linespacing = 11;
        
        for(int a = 0; a < lines.size(); a++)
        {
        	this.otgGuiDimensionSettingsList.mc.fontRenderer.drawString(
    			lines.get(a), 
    			x + 6,  			
    			y + slotHeight - this.otgGuiDimensionSettingsList.mc.fontRenderer.FONT_HEIGHT - 5 + (a * linespacing), 
    			16777215
			);
        }
    }

    private long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }
	
    public void keyTyped(char typedChar, int keyCode)
    {
    }
    
    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        return false;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public void updatePosition(int slotIndex, int x, int y, float partialTicks)
    {
    }

	@Override
	public String getLabelText() {
		return null;
	}

	@Override
	public String getDisplayText() {
		return null;
	}   	
}