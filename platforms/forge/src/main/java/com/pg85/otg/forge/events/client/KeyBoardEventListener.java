package com.pg85.otg.forge.events.client;

import org.lwjgl.input.Keyboard;

import com.pg85.otg.forge.gui.OTGGuiDimensionList;
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
	// Used for pre-generator in-game UI toggle (F3) and OTG in-game menu (O)

	KeyBinding pregeneratorUIKeyBinding = null;
	KeyBinding otgInGameUIKeyBinding = null;
	boolean registered = false;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event)
	{
		if(!registered)
		{
			pregeneratorUIKeyBinding = new KeyBinding("OTG Pregenerator HUD toggle", Keyboard.KEY_F3, "OpenTerrainGenerator");
			ClientRegistry.registerKeyBinding(pregeneratorUIKeyBinding);
			otgInGameUIKeyBinding = new KeyBinding("OTG Pregenerator HUD toggle", Keyboard.KEY_O, "OpenTerrainGenerator");
			registered = true;
		}

		if (FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			if (pregeneratorUIKeyBinding.isPressed())
			{
				if(Minecraft.getMinecraft().isSingleplayer())
				{
					PregeneratorUI.ToggleIngameUI();
				}
			}
			if (otgInGameUIKeyBinding.isPressed())
			{
				Minecraft.getMinecraft().displayGuiScreen(new OTGGuiDimensionList(null));
			}				
		}
	}
}
