package com.pg85.otg.forge.network.server;

import com.pg85.otg.forge.network.CommonProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerProxy extends CommonProxy
{   
    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx)
    {
        return ctx.getServerHandler().player;
    }
}