package com.khorn.terraincontrol.forge.gui;

import java.util.HashMap;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeEngine;

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
	
    public static Class<? extends GuiScreen> lastGuiOpened = null;
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderGameOverLay(RenderGameOverlayEvent.Post event)
    {
    	((ForgeEngine)TerrainControl.getEngine()).getPregenerator().ShowInGameUI();
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void openGui(GuiOpenEvent event)
    {
    	if (event.getGui() instanceof GuiCreateWorld && lastGuiOpened.equals(TCGuiWorldSelection.class))
        {
    		event.setGui(new TCGuiCreateWorld(new TCGuiWorldSelection(null)));
        }
        else if (event.getGui() instanceof GuiWorldSelection)
        {
            event.setGui(new TCGuiWorldSelection(new GuiMainMenu()));
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
