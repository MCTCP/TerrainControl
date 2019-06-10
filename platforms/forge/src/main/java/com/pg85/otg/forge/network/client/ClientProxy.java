package com.pg85.otg.forge.network.client;

import org.lwjgl.input.Keyboard;

import com.pg85.otg.forge.network.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends CommonProxy
{
	public static KeyBinding pregeneratorUIKeyBinding = null;
	public static KeyBinding otgInGameUIKeyBinding = null;
	
	@Override
	public void init(FMLInitializationEvent e)
	{
		otgInGameUIKeyBinding = new KeyBinding("key.otghud.desc", Keyboard.KEY_O, "key.otg.category");
		pregeneratorUIKeyBinding = new KeyBinding("key.pregenerator.desc", Keyboard.KEY_F3, "key.otg.category");
		ClientRegistry.registerKeyBinding(otgInGameUIKeyBinding);
		ClientRegistry.registerKeyBinding(pregeneratorUIKeyBinding);
	}
	
	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx)
	{
		return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
	}
}