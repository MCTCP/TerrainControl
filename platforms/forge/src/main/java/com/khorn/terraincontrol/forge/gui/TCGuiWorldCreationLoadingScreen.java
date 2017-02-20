package com.khorn.terraincontrol.forge.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.TCPlugin;

@SideOnly(Side.CLIENT)
public class TCGuiWorldCreationLoadingScreen extends GuiScreen
{	   		    
    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        
        int yOffset = 0;
        this.drawCenteredString(this.fontRendererObj, "Generating world '" + TerrainControl.PregenerationWorld + "' " + (TerrainControl.PreGeneratorProgress6 > 0 ? " (" + TerrainControl.PreGeneratorProgress6 + "x" + TerrainControl.PreGeneratorProgress6  + " blocks)" : ""), this.width / 2, yOffset + 30, -1);
        
        this.drawCenteredString(this.fontRendererObj, TerrainControl.PreGeneratorProgress2, this.width / 2, yOffset + 55, -1);
        
        this.drawCenteredString(this.fontRendererObj, TerrainControl.PreGeneratorProgress1, this.width / 2, yOffset + 80, -1);
        this.drawCenteredString(this.fontRendererObj, TerrainControl.PreGeneratorProgress3, this.width / 2, yOffset + 95, -1);
        this.drawCenteredString(this.fontRendererObj, TerrainControl.PreGeneratorProgress4, this.width / 2, yOffset + 110, -1);
        this.drawCenteredString(this.fontRendererObj, TerrainControl.PreGeneratorProgress5, this.width / 2, yOffset + 125, -1);
        
        int yOffset2 = yOffset + 15;
        
        if(TerrainControl.PreGenerationSafeMode)
        {        
	        this.drawCenteredString(this.fontRendererObj, "Disabling safe mode can greatly increase pre-generation speed", this.width / 2, yOffset2 + 140, -1);
	        this.drawCenteredString(this.fontRendererObj, "but increases memory usage and may require server reboots during", this.width / 2, yOffset2 + 155, -1);
	        this.drawCenteredString(this.fontRendererObj, "pre-generation. This is recommended only for installations with", this.width / 2, yOffset2 + 170, -1);
	        this.drawCenteredString(this.fontRendererObj, "more than 2GB memory. Pre-generation will automatically resume", this.width / 2, yOffset2 + 185, -1);
	        this.drawCenteredString(this.fontRendererObj, "when entering the world after a server reboot.", this.width / 2, yOffset2 + 200, -1);            
        } else {
        	this.drawCenteredString(this.fontRendererObj, "Safe mode disabled, reboots may be required.", this.width / 2, yOffset2 + 140, -1);
	        this.drawCenteredString(this.fontRendererObj, "Pre-generation will automatically resume", this.width / 2, yOffset2 + 155, -1);
	        this.drawCenteredString(this.fontRendererObj, "when entering the world after a server reboot.", this.width / 2, yOffset2 + 170, -1);
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        
        if(TerrainControl.PreGeneratorProgress1.equals("Done"))
        {
        	mc.displayGuiScreen((GuiScreen)null);
        }
    }
    
    // Make sure escape doesn't do anything
    @Override
    protected void keyTyped(char p_73869_1_, int p_73869_2_) { }
}