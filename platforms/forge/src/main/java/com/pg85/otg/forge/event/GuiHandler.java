package com.pg85.otg.forge.event;

import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.gui.screens.ModpackCreateWorldScreen;

import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// Only used for Modpack world creation menu atm
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT, value = Dist.CLIENT)
public class GuiHandler
{
	@SubscribeEvent
	public static void onGuiOpen(GuiOpenEvent event)
	{
		if(event.getGui() instanceof CreateWorldScreen && !(event.getGui() instanceof ModpackCreateWorldScreen))
		{
			DimensionConfig modPackConfig = DimensionConfig.fromDisk(Constants.MODPACK_CONFIG_NAME);
			if(modPackConfig != null)
			{
				CreateWorldScreen screen = (CreateWorldScreen)event.getGui();
				event.setGui(new ModpackCreateWorldScreen(null, screen.dataPacks, screen.worldGenSettingsComponent));
			}
		}
	}
}
