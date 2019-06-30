package com.pg85.otg.forge.gui;

import java.io.IOException;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionList;
import com.pg85.otg.forge.gui.presets.OTGGuiPresetList;

@SideOnly(Side.CLIENT)
public class OTGGuiEnterWorldName extends GuiScreen
{	
    private GuiScreen sender;
    private GuiTextField newWorldNameTextField;
    private GuiButton btnOk;
    public String worldName;
    boolean bExists = false;
    boolean bIsIllegal = false;
    
    public OTGGuiEnterWorldName(GuiScreen sender, String originalValue)
    {
        this.sender = sender;
        worldName = originalValue;
    }
        
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
    
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen()
    {
        this.newWorldNameTextField.updateCursorCounter();
    }
    
    // Init / drawing
        
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        btnOk = new GuiButton(0, this.width / 2 - 102, 87, 100, 20, I18n.format("Ok", new Object[0]));
    	btnOk.enabled = false;
        this.buttonList.add(btnOk);
        this.buttonList.add(new GuiButton(1, this.width / 2 + 2, 87, 100, 20, I18n.format("gui.cancel", new Object[0])));
        this.newWorldNameTextField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
        this.newWorldNameTextField.setFocused(true);
        this.newWorldNameTextField.setText(worldName != null ? worldName : "");
        
        bIsIllegal = false;
        if(worldName.toLowerCase().equals("overworld"))
        {
        	bIsIllegal = true;
        }
        
        bExists = false;
        if(sender instanceof OTGGuiDimensionList)
        {
        	bExists = this.mc.getSaveLoader().getWorldInfo(worldName) != null;
        } 
        else if(sender instanceof OTGGuiPresetList)
        {
            for(Entry<String, DimensionConfigGui> world : ForgeEngine.Presets.entrySet())
            {
            	if(world.getKey().toLowerCase().trim().equals(this.worldName.toLowerCase().trim()))
            	{
            		bExists = true;
            		break;            		
            	}
            }
        }
                
        if(bIsIllegal || bExists || worldName.trim() == "")
        {
        	btnOk.enabled = false;
        } else {	        	
        	btnOk.enabled = true;
        }
    }
    
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("Name your new world", new Object[0]), this.width / 2, 20, 16777215);
        this.drawString(this.fontRenderer, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 47, 10526880);
        this.newWorldNameTextField.drawTextBox();
        
        if(bIsIllegal)
        {
        	this.drawString(this.fontRenderer, "Illegal world name", this.width / 2 + 110, 67, 0xB20000);
        }
        else if(bExists)
        {
        	this.drawString(this.fontRenderer, I18n.format("Already exists", new Object[0]), this.width / 2 + 110, 67, 0xB20000);
        }
        
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
    
    // Mouse / keyboard
        
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
        	if(sender instanceof OTGGuiDimensionList)
        	{
        		((OTGGuiDimensionList)sender).worldName = this.worldName;
        	}
        	else if(sender instanceof OTGGuiPresetList)
        	{
        		((OTGGuiPresetList)sender).newPresetName = this.worldName;
        	}
        	sender.confirmClicked(button == btnOk, 0);
        	this.mc.displayGuiScreen(sender);
        }
    }

    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        this.newWorldNameTextField.textboxKeyTyped(p_73869_1_, p_73869_2_);
        ((GuiButton)this.buttonList.get(0)).enabled = this.newWorldNameTextField.getText().trim().length() > 0;

        if (p_73869_2_ == 28 || p_73869_2_ == 156)
        {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
        
        this.worldName = this.newWorldNameTextField.getText().trim();	        	     
        	        
        bIsIllegal = false;
        if(worldName.toLowerCase().equals("overworld"))
        {
        	bIsIllegal = true;
        }
        
        bExists = false;
        if(sender instanceof OTGGuiDimensionList)
        {
        	bExists = this.mc.getSaveLoader().getWorldInfo(worldName) != null;
        } 
        else if(sender instanceof OTGGuiPresetList)
        {
            for(Entry<String, DimensionConfigGui> world : ForgeEngine.Presets.entrySet())
            {
            	if(world.getKey().toLowerCase().trim().equals(this.worldName.toLowerCase().trim()))
            	{
            		bExists = ForgeEngine.Presets.containsKey(this.worldName.trim());
            		break;            		
            	}
            }
        }
        
        if(bIsIllegal || bExists || this.worldName.trim() == "")
        {
        	btnOk.enabled = false;
        } else {	        	
        	btnOk.enabled = true;
        }
    }

    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException
    {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        this.newWorldNameTextField.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
    }    
}
