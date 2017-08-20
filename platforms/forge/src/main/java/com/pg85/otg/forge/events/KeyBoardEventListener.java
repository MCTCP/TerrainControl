package com.pg85.otg.forge.events;

import org.lwjgl.input.Keyboard;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.gui.PregeneratorUI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyBoardEventListener
{	
	// Used for pre-generator in-game UI toggle (F3)

	KeyBinding keyBinding = null;
	boolean registered = false;			
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event)
	{
		if(Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			if(!registered)
			{
				keyBinding = new KeyBinding("OTG Pregenerator HUD toggle", Keyboard.KEY_F3, "OpenTerrainGenerator");
				ClientRegistry.registerKeyBinding(keyBinding);
				registered = true;
			}
			
			if (FMLClientHandler.instance().getClient().inGameHasFocus)
			{
				if (keyBinding.isPressed())
				{
					ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(GuiHandler.selectedWorldName);
					if(world == null)
					{
						world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(GuiHandler.selectedWorldName);
					}
					PregeneratorUI.ToggleIngameUI();
				}
			}
		}
	}
}
