package com.pg85.otg.forge.commands;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.world.ForgeWorld;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TPCommand extends BaseCommand
{
    TPCommand()
    {
        name = "tp";
        usage = "tp <biome/dimension name or id> [-p player]";
        description = "Attempt to teleport to the target biome or dimension (Max range: 1000 chunks).";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        if (args.isEmpty())
		{
            return false;
		}

        EntityPlayerMP player = null;
        if (args.contains("-p")) {
            String name = args.get(args.size()-1);
            player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
            if (player == null)
            {
                sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not find player " + name));
                return true;
            }
        } else if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentTranslation(
                    ERROR_COLOR + "Must be a player to send this command without player argument " + name));
            return true;
        }

        if (player == null)
        {
            player = (EntityPlayerMP) sender.getCommandSenderEntity();
        }

        BlockPos pos = player.getPosition();
        int playerX = pos.getX();
        int playerZ = pos.getZ();
        LocalWorld world = this.getWorld(sender, "");

        if (world == null)
        {
            sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not find world for sender"));
            return true;
        }

        StringBuilder biomeOrDimensionName = new StringBuilder();
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-p"))
                // If there's a -p flag, then the final arg is a player name
                break;
            biomeOrDimensionName.append(arg).append(" ");
        }

        if (biomeOrDimensionName.toString().trim().length() <= 0) {
            return false;
        }
        biomeOrDimensionName = new StringBuilder(biomeOrDimensionName.toString().trim());
        sender.sendMessage(
                new TextComponentTranslation(MESSAGE_COLOR + "Searching for destination biome or dimension \"" + VALUE_COLOR + biomeOrDimensionName + MESSAGE_COLOR + "\"."));

        // Check dimension names
        for (int i = -1; i < Long.SIZE << 4; i++)
        {
            if (DimensionManager.isDimensionRegistered(i))
            {
                DimensionType dimensionType = DimensionManager.getProviderType(i);
                if (dimensionType.getName().toLowerCase().trim().equals(biomeOrDimensionName.toString().toLowerCase()))
                {
                    sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Dimension found, teleport commenced"));
                    OTGTeleporter.changeDimension(i, player, false, true);
                    return true;
                }
            }
        }

        int biomeId = -1;
        try {
            biomeId = Integer.parseInt(biomeOrDimensionName.toString().replace(" ", ""));
        } catch (NumberFormatException ex) {
            // Do nothing
        }

        Biome targetMCBiome = null;
        if (biomeId == -1)
        {
            ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByNameOrNull(biomeOrDimensionName.toString());
            if (targetBiome != null)
            {
                targetMCBiome = targetBiome.biomeBase;
            }
        } else {
            ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByOTGIdOrNull(biomeId);
            if(targetBiome != null)
            {
                targetMCBiome = targetBiome.biomeBase;
            }
        }

        if(targetMCBiome != null)
        {
            ArrayList<Biome> biomes = new ArrayList<Biome>();
            biomes.add(targetMCBiome);
            BlockPos biomePos = ((ForgeWorld)world).getWorld().getBiomeProvider().findBiomePosition(playerX, playerZ, 8000, biomes, new Random());
            if(biomePos != null)
            {
                sender.sendMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "Teleporting to \"" + VALUE_COLOR + biomeOrDimensionName + MESSAGE_COLOR + "\"."));
                player.setPositionAndUpdate(biomePos.getX(), world.getHighestBlockYAt(biomePos.getX(), biomePos.getZ(), true, true, false, false, false, null), biomePos.getZ());
                return true;
            }
        }
        sender.sendMessage(
                new TextComponentTranslation(ERROR_COLOR + "Could not find biome \"" + biomeOrDimensionName + "\"."));
        return true;
    }
}