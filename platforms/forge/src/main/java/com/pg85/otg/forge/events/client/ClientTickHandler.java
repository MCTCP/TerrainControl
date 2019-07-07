package com.pg85.otg.forge.events.client;

import java.util.ArrayList;

import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler
{
    private double lastSpawnedTimeIn100Ms = 0;      
	public static ArrayList<ParticleFunction<?>> ClientParticleFunctions = new ArrayList<ParticleFunction<?>>();    
    
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{	
		if(event.phase == Phase.END)
		{	
	    	double currentTimeIn100Ms = Math.ceil((double)(System.currentTimeMillis() / 100L));	    	
	    	if(currentTimeIn100Ms != lastSpawnedTimeIn100Ms) // If the server is lagging and suddenly processes multiple ticks in a row only do 1 spawning cycle.
	    	{	    		
	    		ArrayList<ParticleFunction<?>> particleFunctions = new ArrayList<ParticleFunction<?>>();
	    		synchronized(ClientParticleFunctions)
                {
                	if(ClientParticleFunctions.size() > 0)
                	{
                		particleFunctions.addAll(ClientParticleFunctions);
                	}
                }
	    		
            	lastSpawnedTimeIn100Ms = currentTimeIn100Ms;
        		for(ParticleFunction<?> particleData : particleFunctions)
            	{           			        			
                    double x = (double)particleData.x + 0.5F;
                    double y = (double)particleData.y;
                    double z = (double)particleData.z + 0.5F;
       			
    				String particleName = particleData.particleName;	                    
                    double interval = particleData.interval * 10;
                    
					double velocityY = particleData.velocityYSet ? particleData.velocityY : 0;
					double velocityX = particleData.velocityXSet ? particleData.velocityX : Math.random() * 0.2 - 0.1;
					double velocityZ = particleData.velocityZSet ? particleData.velocityZ : Math.random() * 0.2 - 0.1;
					
					if((currentTimeIn100Ms - particleData.intervalOffset) % interval == 0 || particleData.firstSpawn)// && spawnChance > rngRoll)
					{												
						if(particleData.firstSpawn)
						{
							particleData.intervalOffset = currentTimeIn100Ms;
						}
						particleData.firstSpawn = false;        						
					
        				if(particleName != null && particleName.trim().length() > 0)
        				{
       					
        					if(Minecraft.getMinecraft().inGameHasFocus)
        					{
        						EnumParticleTypes enumParticleType = EnumParticleTypes.getByName(particleName);
	    						Minecraft.getMinecraft().renderGlobal.spawnParticle(enumParticleType.getParticleID(), true, x, y, z, velocityX, velocityY, velocityZ);
	    						//OTG.log(LogMarker.INFO, "Spawned particle " + particleData.particleName + " at " + x + " " + y + " "  + z );
        					}
        				}
					}
            	}	    		
	    	}
		}
	}	
}