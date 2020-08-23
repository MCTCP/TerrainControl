package com.pg85.otg.forge.gui.presets;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionList;
import com.pg85.otg.forge.gui.dimensions.base.OTGGuiScrollingList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

class OTGGuiSlotPresetList extends OTGGuiScrollingList
{   
	// TODO: Why do we need 2?
	private final OTGGuiPresetList otgGuiPresetList;
	private OTGGuiPresetList parent;

    public OTGGuiSlotPresetList(OTGGuiPresetList otgGuiPresetList, OTGGuiPresetList parent)
    {
        super(
    		parent.getMinecraftInstance(), 
    		otgGuiPresetList.listWidth, 
    		parent.height, 
    		otgGuiPresetList.topMargin, 
    		otgGuiPresetList.height - otgGuiPresetList.bottomMargin,
    		otgGuiPresetList.leftMargin, 
    		otgGuiPresetList.slotHeight, 
    		parent.width, 
    		parent.height
		);
		this.otgGuiPresetList = otgGuiPresetList;
        this.parent = parent;
    }

    public void resize()
    {
        this.listWidth = this.otgGuiPresetList.listWidth;
        this.top = this.otgGuiPresetList.topMargin;
        this.bottom = this.otgGuiPresetList.height - this.otgGuiPresetList.bottomMargin;
        this.slotHeight = this.otgGuiPresetList.slotHeight;
        this.left = this.otgGuiPresetList.leftMargin;
        this.right = this.otgGuiPresetList.listWidth + this.left;
    }        

	@Override
    protected int getSize()
    {
    	// Get all presetNames for presets that should be shown
    	List<String> presets = GuiHandler.GuiPresets
			.entrySet()
			.stream()
			.filter(a -> this.parent.selectingPresetForDimension || a.getValue().ShowInWorldCreationGUI)
			.map(Entry::getKey)
			.collect(Collectors.toList()
		);
        return presets.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        this.parent.selectPresetIndex(index);
    }

    @Override
    protected boolean isSelected(int index)
    {
        return this.parent.presetIndexSelected(index);
    }

    @Override
    protected void drawBackground()
    {
        if (this.otgGuiPresetList.mc.world != null)
        {
            // No background in-game
        } else {
            this.parent.drawDefaultBackground();
        }
    }

    @Override
    protected int getContentHeight()
    {
        return (this.getSize()) * slotHeight + 1;
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
    {
    	// Get all presetNames for presets that should be shown
    	List<String> presets = GuiHandler.GuiPresets
			.entrySet()
			.stream()
			.filter(a -> this.parent.selectingPresetForDimension || a.getValue().ShowInWorldCreationGUI)
			.map(Entry::getKey)
			.collect(Collectors.toList()
		);
    	
    	// Can't add a preset multiple times for the same world, make name gray and unselectable if it's already present in the world.
    	boolean bFound = false;
    	if(this.parent.previousMenu instanceof OTGGuiDimensionList)
    	{
        	for(DimensionConfig dimConfig : ((OTGGuiDimensionList)this.parent.previousMenu).dimensions)
        	{
        		if(
    				(dimConfig.PresetName != null && dimConfig.PresetName.equals(presets.get(idx))) ||
    				(dimConfig.PresetName == null && OTG.getDimensionsConfig().WorldName.equals(presets.get(idx)))
				)
        		{
        			bFound = true;
        			break;
        		}
        	}
    	}
        String       name     = net.minecraft.util.StringUtils.stripControlCodes(presets.get(idx));
        FontRenderer font     = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name,    listWidth - 10), this.left + 3 , top +  2, bFound ? 0x666666 : 0xFFFFFF);
    }
}