package com.khorn.terraincontrol.forge.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class TCGuiEnterWorldName extends GuiScreen
{	
    private GuiScreen sender;
    private GuiTextField newWorldNameTextField;
    private GuiButton btnOk;
    
    public TCGuiEnterWorldName(GuiScreen sender, String originalValue)
    {
        this.sender = sender;
        GuiHandler.newWorldName = originalValue;
    }

    public void updateScreen()
    {
        this.newWorldNameTextField.updateCursorCounter();
    }
    
    public void initGui()
    {	    	
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        btnOk = new GuiButton(0, this.width / 2 - 102, 87, 100, 20, I18n.format("Ok", new Object[0]));
    	btnOk.enabled = false;
        this.buttonList.add(btnOk);
        this.buttonList.add(new GuiButton(1, this.width / 2 + 2, 87, 100, 20, I18n.format("gui.cancel", new Object[0])));
        this.newWorldNameTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.newWorldNameTextField.setFocused(true);
        this.newWorldNameTextField.setText(GuiHandler.newWorldName != null ? GuiHandler.newWorldName : "");
        
        bExists = false;
        for(String worldName : GuiHandler.worlds.keySet())
        {
        	if(worldName.toLowerCase().trim().equals(GuiHandler.newWorldName.toLowerCase().trim()))
        	{
        		bExists = true;
        		break;
        	}
        }
        if(bExists || GuiHandler.newWorldName.trim() == "")
        {
        	btnOk.enabled = false;
        } else {	        	
        	btnOk.enabled = true;
        }
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
        	sender.confirmClicked(button == btnOk, 0);
        	this.mc.displayGuiScreen(sender);
        }
    }

    boolean bExists = false;
    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        this.newWorldNameTextField.textboxKeyTyped(p_73869_1_, p_73869_2_);
        ((GuiButton)this.buttonList.get(0)).enabled = this.newWorldNameTextField.getText().trim().length() > 0;

        if (p_73869_2_ == 28 || p_73869_2_ == 156)
        {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
        
        GuiHandler.newWorldName = this.newWorldNameTextField.getText().trim();	        	     
        	        
        bExists = false; 
        for(String worldName : GuiHandler.worlds.keySet())
        {
        	if(worldName.toLowerCase().trim().equals(GuiHandler.newWorldName.toLowerCase().trim()))
        	{
        		bExists = true;
        		break;
        	}
        }
        
        if(bExists || GuiHandler.newWorldName.trim() == "")
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

    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("Name your new world", new Object[0]), this.width / 2, 20, 16777215);
        this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 47, 10526880);
        this.newWorldNameTextField.drawTextBox();
        
        if(bExists)
        {
        	this.drawString(this.fontRendererObj, I18n.format("Already exists", new Object[0]), this.width / 2 + 110, 67, 0xB20000);
        }
        
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
