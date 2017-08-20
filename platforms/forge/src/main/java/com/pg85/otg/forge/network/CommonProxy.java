package com.pg85.otg.forge.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent e) { }

    public void init(FMLInitializationEvent e) { }

    public void postInit(FMLPostInitializationEvent e) { }
    
	public EntityPlayer getPlayerEntity(MessageContext ctx)
	{
		return ctx.getServerHandler().player;
	}
}
