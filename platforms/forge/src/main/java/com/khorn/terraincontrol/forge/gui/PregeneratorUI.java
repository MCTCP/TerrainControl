package com.khorn.terraincontrol.forge.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import com.google.common.base.Strings;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.ForgeWorldSession;
import com.khorn.terraincontrol.forge.generator.Pregenerator;

public class PregeneratorUI
{
	private static boolean menuOpen = true;
	public static void ShowInGameUI()
	{
		if(menuOpen)
		{
			boolean isRunningAndNotDone = false;
			ArrayList<Pregenerator> pregenerators = new ArrayList<Pregenerator>();
			for(LocalWorld world : TerrainControl.getAllWorlds())
			{
				ForgeWorld forgeWorld = (ForgeWorld)world;			
				Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
				if(pregenerator.getPregeneratorIsRunning() && pregenerator.preGeneratorProgressStatus != "Done")
				{
					isRunningAndNotDone = true;
					pregenerators.add(pregenerator);
				}
			}
			
	    	Minecraft mc = Minecraft.getMinecraft();
	    	mc.gameSettings.showDebugInfo = false;
	    	
	    	if(isRunningAndNotDone)
	    	{
		    	FontRenderer fontRenderer = mc.fontRenderer;
		    	
		        GlStateManager.pushMatrix();
		        
		        List<String> list = new ArrayList<String>();       
		        
		        for(Pregenerator pregenerator : pregenerators)
		        {		        
			        list.add("Generating \"" + pregenerator.pregenerationWorld + "\" " + (pregenerator.progressScreenWorldSizeInBlocks > 0 ? "(" + pregenerator.progressScreenWorldSizeInBlocks + "x" + pregenerator.progressScreenWorldSizeInBlocks  + " blocks)" : ""));        
					list.add("Progress: " + pregenerator.preGeneratorProgress + "%");
					list.add("Chunks: " + pregenerator.preGeneratorProgressStatus);
					list.add("Elapsed: " + pregenerator.progressScreenElapsedTime);
					list.add("Estimated: " + pregenerator.progressScreenEstimatedTime);
					list.add("---");
		        }
		        
		        long i = Runtime.getRuntime().maxMemory();
		        long j = Runtime.getRuntime().totalMemory();
		        long k = Runtime.getRuntime().freeMemory();
		        long l = j - k;
		        list.add("Memory: " + Long.valueOf(BytesToMb(l)) + "/" +  Long.valueOf(BytesToMb(i)) + " MB");
		
		        for (int zi = 0; zi < list.size(); ++zi)
		        {
		            String s = (String)list.get(zi);
		
		            if (!Strings.isNullOrEmpty(s))
		            {
		                int zj = fontRenderer.FONT_HEIGHT;
		                int zk = fontRenderer.getStringWidth(s);
		                int zi1 = 2 + zj * zi;
		                Gui.drawRect(1, zi1 - 1, 2 + zk + 1, zi1 + zj - 1, -1873784752); // TODO: Make semi-transparent
		                fontRenderer.drawString(s, 2, zi1, 14737632);
		            }
		        }
		        GlStateManager.popMatrix();
	    	}
		}
	}	

    private static long BytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }
	
	public static void ToggleIngameUI()
	{ 
		boolean isRunningAndNotDone = false;
		ArrayList<Pregenerator> pregenerators = new ArrayList<Pregenerator>();
		for(LocalWorld world : TerrainControl.getAllWorlds())
		{
			ForgeWorld forgeWorld = (ForgeWorld)world;
			Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
			if(pregenerator.getPregeneratorIsRunning() && pregenerator.preGeneratorProgressStatus != "Done")
			{
				isRunningAndNotDone = true;
				pregenerators.add(pregenerator);
			}
		}
		
		if(isRunningAndNotDone || menuOpen)
		{
			if(menuOpen)
			{
				Minecraft.getMinecraft().gameSettings.showDebugInfo = false;
				menuOpen = false;
			}
			else if(!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			{			
				menuOpen = true;
			}
		}
	}
}
