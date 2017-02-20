package com.khorn.terraincontrol.forge.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler
{		
	// TODO: Remove these static fields and use instance fields or pass as method parameters where possible.
	public static boolean selecting;
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
	
    public Class lastGuiOpened = null;
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void openGui(GuiOpenEvent event)
    {
        if (!(event.getGui() instanceof TCGuiWorldCreationLoadingScreen) && (TerrainControl.PreGeneratorIsRunning || TerrainControl.startingPreGeneration))
        {
        	event.setGui(new TCGuiWorldCreationLoadingScreen());
        }
        else if (event.getGui() instanceof GuiCreateWorld && lastGuiOpened.equals(TCGuiWorldSelection.class) && !selecting)
        {
            event.setGui(new TCGuiSelectCreateWorldMode());
        }
        else if (event.getGui() instanceof GuiWorldSelection)
        {
            event.setGui(new TCGuiWorldSelection(new GuiMainMenu()));
        }
        selecting = false;
        lastGuiOpened = event.getGui().getClass();
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void closeGui(GuiOpenEvent event)
    {

    }
    
    public void registerKeybindings() {}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
