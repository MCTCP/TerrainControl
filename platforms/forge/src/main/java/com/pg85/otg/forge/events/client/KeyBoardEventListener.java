package com.pg85.otg.forge.events.client;

import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.gui.OTGGuiDimensionList;
import com.pg85.otg.forge.gui.PregeneratorUI;
import com.pg85.otg.forge.network.client.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyBoardEventListener
{
	// Used for pre-generator in-game UI toggle (F3) and OTG in-game menu (O)

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event)
	{
		if (FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			if (ClientProxy.pregeneratorUIKeyBinding.isPressed())
			{
				if(Minecraft.getMinecraft().isSingleplayer())
				{
					PregeneratorUI.ToggleIngameUI();
				}
			}
			if (ClientProxy.otgInGameUIKeyBinding.isPressed())
			{
				// Only open the menu for overworld or OTG dimensions
		    	if(
					Minecraft.getMinecraft().world != null && 
					Minecraft.getMinecraft().world.provider != null && 
					(
    					Minecraft.getMinecraft().world.provider.getDimension() == 0 ||
    					Minecraft.getMinecraft().world.provider instanceof OTGWorldProvider
					)
	    		)
	        	{
	        		Minecraft.getMinecraft().displayGuiScreen(new OTGGuiDimensionList(null));
	        	}
			}				
		}
	}
}
