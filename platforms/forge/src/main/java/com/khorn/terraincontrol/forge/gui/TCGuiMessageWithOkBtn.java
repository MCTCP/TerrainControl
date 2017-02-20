package com.khorn.terraincontrol.forge.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TCGuiMessageWithOkBtn extends GuiScreen
{
    private String field_146313_a;
    private String field_146312_f;
    
    GuiScreen returnToScreen;

    public TCGuiMessageWithOkBtn(String p_i46319_1_, String p_i46319_2_, GuiScreen returnToScreen)
    {
        this.field_146313_a = p_i46319_1_;
        this.field_146312_f = p_i46319_2_;
        this.returnToScreen = returnToScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        super.initGui();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 140, I18n.format("gui.cancel", new Object[0])));
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
    	this.drawDefaultBackground();
    	
        this.drawCenteredString(this.fontRendererObj, this.field_146313_a, this.width / 2, 90, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.field_146312_f, this.width / 2, 110, 16777215);
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }    

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) { }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button)
    {
        this.mc.displayGuiScreen(this.returnToScreen);
    }
}