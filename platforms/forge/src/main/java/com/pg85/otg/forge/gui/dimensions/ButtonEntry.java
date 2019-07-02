package com.pg85.otg.forge.gui.dimensions;

import com.pg85.otg.forge.gui.IGuiListEntry;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionSettingsList;
import com.pg85.otg.forge.network.client.ClientPacketManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Int;

@SideOnly(Side.CLIENT)
public class ButtonEntry implements IGuiListEntry
{
	private final OTGGuiDimensionSettingsList otgGuiDimensionSettingsList;
	private final String labelText;
    private final GuiButton btnSettingsEntry;
    private final OTGGuiDimensionSettingsList parent;

    ButtonEntry(OTGGuiDimensionSettingsList otgGuiDimensionSettingsList, OTGGuiDimensionSettingsList parent, String name)
    {
    	this.otgGuiDimensionSettingsList = otgGuiDimensionSettingsList;
		this.parent = parent;
        this.labelText = name != null ? name : "";
        this.btnSettingsEntry = new GuiButton(0, 0, 0, 120, 20, this.labelText);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        this.btnSettingsEntry.x = x + (300 / 2) - (120 / 2);
        this.btnSettingsEntry.y = y;
        this.btnSettingsEntry.displayString = this.labelText;
    	this.btnSettingsEntry.drawButton(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY, partialTicks);
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
    	if(this.btnSettingsEntry.mousePressed(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY))
    	{
    		// TODO: Make this prettier
    		if(labelText.equals("Game rules"))
    		{
    			// Open game rules menu
    			this.otgGuiDimensionSettingsList.refreshData(false, true, false);
    			this.otgGuiDimensionSettingsList.scrollBy(Int.MinValue());
    		}
    		else if(labelText.equals("Advanced settings"))
    		{
    			// Open Advanced settings menu
    			this.otgGuiDimensionSettingsList.refreshData(false, false, true);
    			this.otgGuiDimensionSettingsList.scrollBy(Int.MinValue());
    		}
    		else if(labelText.equals("Teleport"))
    		{
    			ClientPacketManager.sendTeleportPlayerPacket(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : parent.controlsScreen.selectedDimension.PresetName);
    			this.parent.mc.displayGuiScreen(null);
    		}
    		else if(labelText.equals("Back"))
    		{
    			// Return to dimension settings menu
    			this.otgGuiDimensionSettingsList.refreshData(true, false, false);
    			this.otgGuiDimensionSettingsList.scrollBy(Int.MinValue());
    		}
    		
            return true;
    	}
        
        return false;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        this.btnSettingsEntry.mouseReleased(x, y);
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