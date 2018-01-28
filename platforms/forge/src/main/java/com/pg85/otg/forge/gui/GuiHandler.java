package com.pg85.otg.forge.gui;

import java.util.HashMap;

import com.pg85.otg.configuration.WorldConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
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
	// TODO: Remove static fields, use instance fields or pass as method parameters
	public static String newWorldName = null;
	public static String worldName;
	public static String selectedWorldName = null;
	public static String seed = null;
    public static String gameModeString = "survival";
    public static boolean hardCore = false;
    public static boolean allowCheats = false;
    public static boolean bonusChest = false;;
    public static HashMap<String,WorldConfig> worlds = new HashMap<String, WorldConfig>();
    public static int pageNumber = 0;

    public static int WorldBorderRadius = 0;
    public static int PregenerationRadius = 0;

    public static Class<? extends GuiScreen> lastGuiOpened = null;
    public static boolean askModCompatContinue = false;

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
    	if (event.getGui() instanceof GuiCreateWorld && lastGuiOpened.equals(OTGGuiWorldSelection.class))
        {
    		event.setGui(new OTGGuiCreateWorld(new OTGGuiWorldSelection(null)));
        }
        else if (event.getGui() instanceof GuiWorldSelection)
        {
            event.setGui(new OTGGuiWorldSelection(new GuiMainMenu()));
        }
        if(event.getGui() != null)
        {
        	lastGuiOpened = event.getGui().getClass();
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
