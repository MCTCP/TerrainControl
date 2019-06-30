package com.pg85.otg.forge.events.client;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientConnectionEventListener
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientDisconnectionFromServerEvent(ClientDisconnectionFromServerEvent event)
    {
    	if(!Minecraft.getMinecraft().isSingleplayer()) // Don't do this for Forge SP client, it will save and unload all worlds when it shuts down the server
    	{
    		((ForgeEngine)OTG.getEngine()).getWorldLoader().onServerStopped();
    	}
    }
}
