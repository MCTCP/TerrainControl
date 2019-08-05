package com.pg85.otg.forge.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IGuiListEntry
{
	String getLabelText();
	String getDisplayText();
	
    void updatePosition(int slotIndex, int x, int y, float partialTicks);

    void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks);

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);

    void keyTyped(char typedChar, int keyCode);
    
    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
}