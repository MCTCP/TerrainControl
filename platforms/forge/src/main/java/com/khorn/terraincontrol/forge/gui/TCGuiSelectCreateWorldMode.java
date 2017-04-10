package com.khorn.terraincontrol.forge.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TCGuiSelectCreateWorldMode extends GuiScreen
{	
	public TCGuiSelectCreateWorldMode() {}
	 
    @Override
    public void drawScreen(int x, int y, float f)
    {
        this.drawDefaultBackground();
        super.drawScreen(x, y, f);
    }
    
    GuiButton terrainControl;
    GuiButton vanilla;
    GuiButton cancel;
    
    @Override
    public void initGui()
    {
    	super.initGui();
        this.buttonList.add(this.terrainControl = new GuiButton(0, this.width / 2 - 100, this.height / 2 - 32, "OpenTerrainGenerator"));
        this.buttonList.add(this.vanilla = new GuiButton(1, this.width / 2 - 100, this.height / 2 - 4, "Vanilla"));
        this.buttonList.add(this.cancel = new GuiButton(2, this.width / 2 - 100, this.height / 2 + 24, "Cancel"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	super.actionPerformed(button);
    	if(button.enabled)
    	{
            if (button == this.cancel) // Cancel
            {
                this.mc.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()));
            }
            if (button == this.terrainControl)
            {
                //Main.packetHandler.sendToServer(...);
                this.mc.displayGuiScreen(new TCGuiCreateWorld(this));
                if (this.mc.currentScreen == null)
                {
                    this.mc.setIngameFocus();
                }
            }
            if (button == this.vanilla)
            {
            	//GuiHandler.selecting = true;
                //Main.packetHandler.sendToServer(...);
                this.mc.displayGuiScreen(new GuiCreateWorld(this));
                if (this.mc.currentScreen == null)
                {                	
                    this.mc.setIngameFocus();
                }
            }
    	}
    }
}
