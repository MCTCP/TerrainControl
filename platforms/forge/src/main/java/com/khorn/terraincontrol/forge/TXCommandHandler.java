package com.khorn.terraincontrol.forge;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.generator.TXBiome;

import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

final class TXCommandHandler implements ICommand
{
    private final List<String> aliases = Collections.singletonList("tc");

    @Override
    public String getName()
    {
        return "tc";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return "tc";
    }

    @Override
    public List<String> getAliases()
    {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString)
    {
        World mcWorld = sender.getEntityWorld();

        if (!mcWorld.isRemote) // Server side
        {
            if (argString.length == 0)
            {
                sender.sendMessage(new TextComponentString("-- TerrainControl --"));
                sender.sendMessage(new TextComponentString("Commands:"));
                sender.sendMessage(new TextComponentString("/tc worldinfo - Show author and description information for this world."));
                sender.sendMessage(new TextComponentString("/tc biome - Show biome information for any biome at the player's coordinates."));
            } else if (argString[0].equalsIgnoreCase("worldinfo"))
            {
                LocalWorld localWorld = WorldHelper.toLocalWorld(sender.getEntityWorld());
                if (localWorld != null)
                {
                    WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
                    sender.sendMessage(new TextComponentString("-- World info --"));
                    sender.sendMessage(new TextComponentString("Author: " + worldConfig.author));
                    sender.sendMessage(new TextComponentString("Description: " + worldConfig.description));
                } else
                {
                    sender.sendMessage(new TextComponentString(PluginStandardValues.PLUGIN_NAME + " is not enabled for this world."));
                }
            } else if (argString[0].equalsIgnoreCase("biome"))
            {
                final BlockPos blockPos = sender.getPosition();
                final World world = sender.getEntityWorld();
                final Biome biome = world.getBiome(blockPos);

                final int generationId = ((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(biome);
                final int savedId = biome instanceof TXBiome ? ((TXBiome) biome).id.getSavedId() : generationId;
                final boolean isVirtual = generationId > 256;

                final StringBuilder builder = new StringBuilder();
                builder.append(TextFormatting.DARK_GREEN + "Current Biome Data...").append("\n");
                builder.append(getKeyValueMessage("ID", "" + generationId)).append("\n");
                builder.append(getKeyValueMessage("Saved ID", "" + savedId)).append("\n");
                builder.append(getKeyValueMessage("Registry Key", biome.getRegistryName().toString())).append("\n");
                builder.append(getKeyValueMessage("IsVirtual", "" + (generationId != savedId))).append("\n");
                if (isVirtual)
                {
                    builder.append(getKeyValueMessage("  Biome Base", "" + Biome.getBiome(savedId).getRegistryName().toString())).append("\n");
                }
                builder.append(getKeyValueMessage("Temperature", "" + biome.getTemperature(blockPos)));

                sender.sendMessage(new TextComponentString(builder.toString()));
            } else if (argString[0].equalsIgnoreCase("print"))
            {
                ForgeWorld toPrint = null;
                boolean specifiedWorld = false;

                if (argString.length > 1) {
                    toPrint = (ForgeWorld) TerrainControl.getWorld(argString[1]);
                    specifiedWorld = true;
                }

                ForgeRegistry<Biome> registry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;

                if (toPrint == null) {
                    if (specifiedWorld) {
                        TerrainControl.log(LogMarker.INFO, "Did not find a TC config for '{}'. Printing biome registry. Format is Id-RegistryKey"
                                + ".", argString[1]);

                    } else {
                        TerrainControl.log(LogMarker.INFO, "Printing biome registry. Format is Id-RegistryKey.");
                    }
                    boolean virtualHeader = false;

                    for (final Biome biome : registry) {
                        final int generationId = registry.getID(biome);
                        final int savedId = biome instanceof TXBiome ? ((TXBiome) biome).id.getSavedId() : generationId;

                        if (generationId != savedId) {
                            if (!virtualHeader) {
                                virtualHeader = true;
                                TerrainControl.log(LogMarker.INFO, "Printing Virtual biomes. Format is GenId-GenRegistryKey-SaveId-SaveRegistryKey"
                                        + ".");
                            }
                            TerrainControl.log(LogMarker.INFO, "  {}-{}-{}-{}", generationId, biome.getRegistryName(), savedId, registry.getValue
                                    (savedId).getRegistryName());
                        } else {
                            TerrainControl.log(LogMarker.INFO, "  {}-{}", generationId, biome.getRegistryName());
                        }
                    }
                } else {
                    TerrainControl.log(LogMarker.INFO, "Printing specific biomes for '{}'. Format is Id-RegistryKey.", toPrint.getName());
                    boolean virtualHeader = false;

                    for (final LocalBiome tcBiome : toPrint.biomeNames.values().stream().sorted(Comparator.comparing(localBiome -> localBiome.getIds().getGenerationId())).collect(Collectors.toList())) {
                        final Biome biome = ((ForgeBiome) tcBiome).getHandle();

                        final int generationId = registry.getID(biome);
                        final int savedId = biome instanceof TXBiome ? ((TXBiome) biome).id.getSavedId() : generationId;

                        if (generationId != savedId) {
                            if (!virtualHeader) {
                                virtualHeader = true;
                                TerrainControl.log(LogMarker.INFO, "Printing specific Virtual biomes for '{}'. Format is "
                                        + "GenId-GenRegistryKey-SaveId-SaveRegistryKey.", toPrint.getName());
                            }
                            TerrainControl.log(LogMarker.INFO, "  {}-{}-{}-{}", generationId, biome.getRegistryName(), savedId, registry.getValue
                                    (savedId).getRegistryName());
                        } else {
                            TerrainControl.log(LogMarker.INFO, "  {}-{}", generationId, biome.getRegistryName());
                        }
                    }
                }

                sender.sendMessage(new TextComponentString("Check console for biome printout."));
            } else
            {
                sender.sendMessage(new TextComponentString("Unknown command. Type /tc for a list of commands."));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canUseCommand(2, this.getName());
    }

    @Override
    public boolean isUsernameIndex(String[] var1, int var2)
    {
        return false;
    }

    @Override
    public int compareTo(ICommand that)
    {
        return this.getName().compareTo(that.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return Collections.emptyList();
    }

    private static String getKeyValueMessage(String key, String value) {
        return "  " + key + ": " + TextFormatting.GRAY + value;
    }
}
