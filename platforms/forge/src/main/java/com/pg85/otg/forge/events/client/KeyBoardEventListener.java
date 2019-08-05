package com.pg85.otg.forge.events.client;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionList;
import com.pg85.otg.forge.network.client.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyBoardEventListener
{
	// Used for OTG in-game menu (O)

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event)
	{
		if (FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			if (ClientProxy.OtgInGameUIKeyBinding.isPressed())
			{
		    	if(
					// The GUI screen requires the dimensions configuration to be available, so it shouldn't be opened
					// with an unset configuration.
                    // This is the case when connecting to servers without the mod installed, for example.
					OTG.getDimensionsConfig() != null &&
					
					// Only open the menu if players are opped
    				Minecraft.getMinecraft().player.canUseCommand(2, "") &&
					
					// Only open the menu for overworld or OTG dimensions
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
