package com.pg85.otg.forge.gui.dimensions.base;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionList;
import com.pg85.otg.forge.world.ForgeWorld;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;

public class OTGGuiSlotDimensionList extends OTGGuiScrollingList
{   
	private final OTGGuiDimensionList otgGuiDimensionList;
	private OTGGuiDimensionList parent;
    private ArrayList<DimensionConfig> dimensions;

    public OTGGuiSlotDimensionList(OTGGuiDimensionList otgGuiDimensionList, OTGGuiDimensionList parent, ArrayList<DimensionConfig> dimensions)
    {
        super(
    		parent.getMinecraftInstance(), 
    		otgGuiDimensionList.listWidth, 
    		parent.height, 
    		otgGuiDimensionList.topMargin, 
    		otgGuiDimensionList.height - otgGuiDimensionList.bottomMargin,
    		otgGuiDimensionList.leftMargin, 
    		otgGuiDimensionList.slotHeight, 
    		parent.width, 
    		parent.height
		);
		this.otgGuiDimensionList = otgGuiDimensionList;
        this.parent = parent;
        this.dimensions = dimensions;
    }

    public void resize()
    {
        this.listWidth = this.otgGuiDimensionList.listWidth;
        this.top = this.otgGuiDimensionList.topMargin;
        this.bottom = this.otgGuiDimensionList.height - this.otgGuiDimensionList.bottomMargin;
        this.slotHeight = this.otgGuiDimensionList.slotHeight;
        this.left = this.otgGuiDimensionList.leftMargin;
        this.right = this.otgGuiDimensionList.listWidth + this.left;
    }
    
    @Override
	public int getSize()
    {
        return dimensions.size();
    }

    @Override
    protected boolean isSelected(int index)
    {
        return this.parent.isDimensionIndexSelected(index);
    }
    
    @Override
    protected int getContentHeight()
    {
        return (this.getSize()) * slotHeight + 1;
    }

    // Mouse / keyboard
    
    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
    	// Make sure all settings are applied
    	this.parent.dimensionSettingsList.mouseClicked(0, 0, 0);
        this.parent.selectDimensionIndex(index);
    }
    
    // Drawing

    @Override
    protected void drawBackground()
    {
        if (this.otgGuiDimensionList.mc.world != null)
        {
            // No background in-game
        } else {
            this.parent.drawDefaultBackground();
        }
    }
    
    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
    {
    	DimensionConfig dimConfig = dimensions.get(idx);
        String name = net.minecraft.util.StringUtils.stripControlCodes(dimConfig.PresetName != null && dimConfig.PresetName.length() > 0 ? dimConfig.PresetName : idx == 0 ? "Overworld" : "Dimension " + idx);
        FontRenderer font = this.parent.getFontRenderer();

        boolean isNewDim = dimConfig.isNewConfig; // This dimension config was created via the ingame menu, but the dimension itself hasn't been created yet (is done when pressing continue/apply)
        boolean isLoaded = false;
        boolean isBeingCreatedOnServer = false;
        if(!isNewDim)
        {
            // TODO: Don't do this each draw ><
            // If we're ingame then find out if the dimension is loaded, getWorld only fetches loaded worlds.
            // If world isn't null then we're ingame
        	if(this.parent.mc.world == null || this.parent.mc.isSingleplayer())
        	{            		
	            isLoaded = idx == 0 || (this.parent.mc.world != null && idx != 0 ? ((ForgeEngine)OTG.getEngine()).getWorld(dimensions.get(idx).PresetName) : null) != null;
        	} else {
        		// For MP get the loaded status from the ForgeWorld, set by a packet from the server.
	            ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(dimensions.get(idx).PresetName);
	            if(forgeWorld == null)
	            {
	            	forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(dimensions.get(idx).PresetName);
	            }
	            isBeingCreatedOnServer = idx != 0 && forgeWorld == null;
	            isLoaded = idx == 0 || (forgeWorld != null && forgeWorld.isLoadedOnServer); // ForgeWorld can be null after sending a create world packet from the client
        	}
        }
        font.drawString(font.trimStringToWidth(name + (isBeingCreatedOnServer ? TextFormatting.GRAY + " (creating)" + TextFormatting.RESET : (this.parent.mc.world != null ? (isLoaded ? TextFormatting.GREEN + " (loaded)" + TextFormatting.RESET : isNewDim ?  TextFormatting.GOLD + " (new)" + TextFormatting.RESET : TextFormatting.DARK_GREEN + " (unloaded)" + TextFormatting.RESET) : "")), listWidth - 10), this.left + 3 , top +  2, 0xFFFFFF);
    }
}