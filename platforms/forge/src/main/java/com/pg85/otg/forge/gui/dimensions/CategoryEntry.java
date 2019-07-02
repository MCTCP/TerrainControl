package com.pg85.otg.forge.gui.dimensions;

import com.pg85.otg.forge.gui.IGuiListEntry;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionSettingsList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CategoryEntry implements IGuiListEntry
{
	private final OTGGuiDimensionSettingsList otgGuiDimensionSettingsList;
	private final String labelText;
    private final int labelWidth;

    CategoryEntry(OTGGuiDimensionSettingsList otgGuiDimensionSettingsList, String name)
    {
        this.otgGuiDimensionSettingsList = otgGuiDimensionSettingsList;
		this.labelText = name;
        this.labelWidth = this.otgGuiDimensionSettingsList.mc.fontRenderer.getStringWidth(this.labelText);
    }
    
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
    	this.otgGuiDimensionSettingsList.mc.fontRenderer.drawString(
			this.labelText, 
			x + (300 / 2) - (labelWidth / 2),  			
			y + slotHeight - this.otgGuiDimensionSettingsList.mc.fontRenderer.FONT_HEIGHT - 5, 
			16777215
		);
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
    
    public String getLabelText()
    {
    	return this.labelText;
    }
    
    public String getDisplayText()
    {
    	return this.labelText;
    }
}