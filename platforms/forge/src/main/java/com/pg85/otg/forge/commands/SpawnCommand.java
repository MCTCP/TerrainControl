package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.util.bo3.Rotation;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class SpawnCommand extends BaseCommand
{
    SpawnCommand()
    {
        super();
        name = "spawn";
        usage = "spawn <object name>";
        description = "Spawn an object on the block you are looking at.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        Random random = new Random();

        String objectName = StringUtils.join(args, " ").trim();
        LocalWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(sender.getEntityWorld());

        if (args.isEmpty())
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "You must enter the name of the object."));
            return true;
        }

        // Search the current world, if no BO3 is found, search dims, else globalobjects.
        CustomObject spawnObject = null;
        if (forgeWorld != null)
        {
            spawnObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectName, forgeWorld.getName(), false);
        }

        if(spawnObject == null)
        {
	        for(LocalWorld localWorld : OTG.getAllWorlds())
	        {
	        	if(localWorld != forgeWorld)
	        	{
	        		spawnObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectName, localWorld.getName(), false);
	        	}
	        	if(spawnObject != null)
	        	{
	        		break;
	        	}
	        }
        }
        if(spawnObject == null)
        {
        	spawnObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectName, null, true);
        }

        if (spawnObject == null)
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "Object not found."));
            return true;
        }

        RayTraceResult trace = this.rayTrace(sender.getEntityWorld(), (EntityPlayer) sender.getCommandSenderEntity());
        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = trace.getBlockPos();
            if (spawnObject.spawnForced(forgeWorld, random, Rotation.NORTH, pos.getX(), pos.getY(), pos.getZ()))
            {
                sender.sendMessage(
                        new TextComponentString(BaseCommand.MESSAGE_COLOR + spawnObject.getName() + " was spawned."));
            } else {
                sender.sendMessage(
                        new TextComponentString(BaseCommand.ERROR_COLOR + "Object can't be spawned over there."));
            }
        } else {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "No block in sight."));
        }
        return true;
    }

    // Modified from Item.rayTrace
    protected RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn)
    {
        float f = playerIn.rotationPitch;
        float f1 = playerIn.rotationYaw;
        double d0 = playerIn.posX;
        double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
        double d2 = playerIn.posZ;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 200;
        Vec3d vec3d1 = vec3d.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
        return worldIn.rayTraceBlocks(vec3d, vec3d1, false, true, false);
    }
}