package com.pg85.otg.forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler
{	
    public static boolean useVanillaScreen = false;

    //public static boolean askModCompatContinue = false;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderGameOverLay(RenderGameOverlayEvent.Post event)
    {
    	if(Minecraft.getMinecraft().isIntegratedServerRunning())
    	{
    		PregeneratorUI.ShowInGameUI();
    	}
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void openGui(GuiOpenEvent event)
    {
        if (event.getGui() instanceof GuiWorldSelection)
        {
            event.setGui(new OTGGuiWorldSelection(new GuiMainMenu()));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void closeGui(GuiOpenEvent event) { }

    public void registerKeybindings() {}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}
