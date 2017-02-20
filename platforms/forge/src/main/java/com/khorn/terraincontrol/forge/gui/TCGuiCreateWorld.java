package com.khorn.terraincontrol.forge.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.io.SimpleSettingsMap;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.forge.TCPlugin;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
  
@SideOnly(Side.CLIENT)
public class TCGuiCreateWorld extends GuiScreen implements GuiYesNoCallback
{	
    private GuiScreen sender;
    
    private GuiTextField txtWorldName;
    private GuiTextField txtSeed;
    
    private GuiButton btnavailableWorld1;
    private GuiButton btnavailableWorld2;
    private GuiButton btnavailableWorld3;
    private GuiButton btnavailableWorldPrev;
    private GuiButton btnavailableWorldNext;
    private GuiButton btnavailableWorldNew;
    private GuiButton btnavailableWorldClone;
    
    private GuiButton btnavailableWorldDelete1;
    private GuiButton btnavailableWorldDelete2;
    private GuiButton btnavailableWorldDelete3;
    
    private GuiTextField txtPregenRadius;
    
    private GuiTextField txtWorldBorderRadius;
    
    private GuiButton btnPregenerationSafeMode;       

    private GuiButton btnGameMode;
    private GuiButton btnAllowCheats;
    private GuiButton btnBonusChest;
    
    private GuiButton btnCreateWorld;        
    
    private boolean bBtnCreateNewWorldClicked;
    
    private final String[] field_146327_L = new String[] {"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    private final String __OBFID = "CL_00000689";

    public TCGuiCreateWorld(GuiScreen sender)
    {
        this.sender = sender;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.txtSeed.updateCursorCounter();
        this.txtWorldBorderRadius.updateCursorCounter();
        this.txtPregenRadius.updateCursorCounter();
    }
    
    WorldConfig selectedWorldConfig = null;
            
    private void FillAvailableWorlds()
    {
    	GuiHandler.worlds.clear();
    	
        ArrayList<String> worldNames = new ArrayList<String>();        	            
        File TCWorldsDirectory = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
        {
        	for(File worldDir : TCWorldsDirectory.listFiles())
        	{
        		if(worldDir.isDirectory())
        		{
        			worldNames.add(worldDir.getName());
        			if(!GuiHandler.worlds.containsKey(worldDir.getName()))
        			{
        		        File worldConfigFile = new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        		        SettingsMap settingsMap = FileSettingsReader.read(worldDir.getName(), worldConfigFile);
        		        WorldConfig worldConfig = new WorldConfig(worldDir, settingsMap, null, null); // TODO: Make sure passing null doesn't break CustomObjects?
                        GuiHandler.worlds.put(worldDir.getName(), worldConfig);
        			}
        		}
        	}
    	}
        
        int pages = (int)Math.ceil(worldNames.size() / 3d);
        if(GuiHandler.pageNumber > pages - 1)
        {
        	GuiHandler.pageNumber = pages - 1;
        }
        if(GuiHandler.pageNumber < 0)
        {
        	GuiHandler.pageNumber = 0;
        }
        int i = 0;
        
        btnavailableWorld1.displayString = "";
        btnavailableWorld2.displayString = "";
        btnavailableWorld3.displayString = "";

        btnavailableWorldDelete1.displayString = "";
        btnavailableWorldDelete2.displayString = "";
        btnavailableWorldDelete3.displayString = "";
        
        for(String worldName : worldNames)
        {
        	i += 1;
        	if(i == (GuiHandler.pageNumber * 3) + 1)
        	{
        		btnavailableWorld1.displayString = worldName;
        		btnavailableWorldDelete1.displayString = "X";
        	}
        	if(i == (GuiHandler.pageNumber * 3) + 2)
        	{
        		btnavailableWorld2.displayString = worldName;
        		btnavailableWorldDelete2.displayString = "X";
        	}
        	if(i == (GuiHandler.pageNumber * 3) + 3)
        	{
        		btnavailableWorld3.displayString = worldName;
        		btnavailableWorldDelete3.displayString = "X";
        	}
        }
        
        btnavailableWorld1.enabled = !btnavailableWorld1.displayString.equals("") && !btnavailableWorld1.displayString.equals(GuiHandler.worldName);
       	btnavailableWorld2.enabled = !btnavailableWorld2.displayString.equals("") && !btnavailableWorld2.displayString.equals(GuiHandler.worldName);
    	btnavailableWorld3.enabled = !btnavailableWorld3.displayString.equals("") && !btnavailableWorld3.displayString.equals(GuiHandler.worldName);
    	        	
    	btnavailableWorldDelete1.enabled = !btnavailableWorld1.displayString.equals("");
    	btnavailableWorldDelete2.enabled = !btnavailableWorld2.displayString.equals("");
    	btnavailableWorldDelete3.enabled = !btnavailableWorld3.displayString.equals("");
    	
        if(worldNames.size() > 3)
        {
        	btnavailableWorldPrev.enabled = true;
        	btnavailableWorldNext.enabled = true;
        } else {
        	btnavailableWorldPrev.enabled = false;
        	btnavailableWorldNext.enabled = false;            	
        }
        
        if(GuiHandler.pageNumber == 0)
        {
        	btnavailableWorldPrev.enabled = false;	
        }
        if(GuiHandler.pageNumber == pages -1)
        {
        	btnavailableWorldNext.enabled = false;
        }                        
    }

    private void previousPage()
    {
    	GuiHandler.pageNumber -= 1;
    	if(GuiHandler.pageNumber < 0)
    	{
    		GuiHandler.pageNumber = 0;
    	}
    	FillAvailableWorlds();
    }

    private void nextPage()
    {
    	GuiHandler.pageNumber += 1;
    	FillAvailableWorlds();
    }
    
    private void updateWorldName()
    {
    	GuiHandler.worldName = txtWorldName.getText().trim();
        
        btnCreateWorld.enabled = txtWorldName.getText().length() > 0;
        
        if(GuiHandler.worldName.length() == 0)
        {
        	worldNameHelpText = "World name cannot be empty";
        } else {
        	int i = 0;
        	while(true)
        	{
        		if(i == GuiHandler.worldName.length())
        		{
        			break;
        		}
        		if(!ChatAllowedCharacters.isAllowedCharacter(GuiHandler.worldName.charAt(i)))
        		{
        			GuiHandler.worldName.replace(GuiHandler.worldName.charAt(i), '_');
        		}
        		i++;
        	}

        	GuiHandler.worldName = getCorrectWorldName(this.mc.getSaveLoader(), GuiHandler.worldName);
                        
            boolean WorldNameExists = false;
            if (this.mc.getSaveLoader().getWorldInfo(GuiHandler.worldName) != null)
            {
            	worldNameHelpText = "Existing world will be deleted!";
            } else {            	           	
            	worldNameHelpText = "New world dir will be created";
            }            
           
            ArrayList<String> worldNames = new ArrayList<String>();
        	boolean usingPreset = false;
            	        		        	
            File TCWorldsDirectory = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
            if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
            {
            	for(File worldDir : TCWorldsDirectory.listFiles())
            	{
            		if(worldDir.isDirectory())
            		{
            			if(GuiHandler.worldName.equals(worldDir.getName()))
            			{
            				selectedWorldConfig = GuiHandler.worlds.get(worldDir.getName());
            				if(GuiHandler.selectedWorldName == null || !GuiHandler.selectedWorldName.equals(GuiHandler.worldName))
            				{	
            					TerrainControl.PreGenerationSafeMode = selectedWorldConfig.PreGenerationSafeMode;
            					TerrainControl.PregenerationRadius = selectedWorldConfig.PreGenerationRadius;
            					TerrainControl.WorldBorderRadius = selectedWorldConfig.WorldBorderRadius;
            					GuiHandler.seed = selectedWorldConfig.worldSeed;
            					
            					WorldInfo worldInfo = this.mc.getSaveLoader().getWorldInfo(GuiHandler.worldName);
            					
            					if(worldInfo != null)
            					{
            						GuiHandler.seed = worldInfo.getSeed() + "";
            						GuiHandler.gameModeString = worldInfo.getGameType().getName();
            						GuiHandler.hardCore = worldInfo.isHardcoreModeEnabled();
            						GuiHandler.allowCheats = worldInfo.areCommandsAllowed();
            					} else {
            						GuiHandler.gameModeString = "survival";
            						GuiHandler.hardCore = false;
            						GuiHandler.allowCheats = false;		        	                    	            					
            					}
            					GuiHandler.bonusChest = false;
            				}
            				GuiHandler.selectedWorldName = worldDir.getName();
            				
            				usingPreset = true;
            				break;
            			}
            		}
            	}
        	}	            
            
            if(usingPreset)
            {

            	worldNameHelpText2 = "Using existing settings";
            	
            	//if(resetTextFields)
            	{
	                txtPregenRadius.setText(TerrainControl.PregenerationRadius + "");		                
	                txtWorldBorderRadius.setText(TerrainControl.WorldBorderRadius + "");;
	            	
                	if(TerrainControl.PreGenerationSafeMode)
                	{
                		this.btnPregenerationSafeMode.displayString = "Enabled";
                	} else {
                		this.btnPregenerationSafeMode.displayString = "Disabled";
                	}
	                
                    txtSeed.setText(GuiHandler.seed);	                   
            	}                	
            } else {
            	
            	GuiHandler.selectedWorldName = null;
            	selectedWorldConfig = null;
            	
            	worldNameHelpText2 = "Using default settings";
            }
			btnavailableWorldClone.enabled = GuiHandler.selectedWorldName != null;
        }
        
        FillAvailableWorlds();
        updateButtons();
    }

    String worldNameHelpText;
    String worldNameHelpText2;
    public String getCorrectWorldName(ISaveFormat anvilSaveConverter, String worldName)
    {
    	worldName = worldName.replaceAll("[\\./\"]", "_");
        String[] astring = field_146327_L;
        int i = astring.length;

        for (int j = 0; j < i; ++j)
        {
            String s1 = astring[j];

            if (worldName.equalsIgnoreCase(s1))
            {
            	worldName = "_" + worldName + "_";
            }
        }          

        return worldName;
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

	@Override
    public void confirmClicked(boolean ok, int worldId)
    {			
		if(explainingSafeMode)
		{
			this.mc.displayGuiScreen(new TCGuiCreateWorld(new TCGuiSelectCreateWorldMode()));
		}			
		else if(askDeleteSettings)
		{
			if(ok)
			{
				GuiYesNo guiyesno = askDeleteSettings(this, worldNameToDelete);
				this.mc.displayGuiScreen(guiyesno);					
				
	            File TCWorldsDirectory = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
	            if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
	            {
	            	for(File worldDir : TCWorldsDirectory.listFiles())
	            	{
	            		if(worldDir.isDirectory() && worldDir.getName().equals(worldNameToDelete))
	            		{
	            			deleteRecursive(worldDir);
	            			break;
	            		}
	            	}
	        	}
	            
                ISaveFormat isaveformat = this.mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(worldNameToDelete);
                
                if(GuiHandler.selectedWorldName != null && worldNameToDelete.trim().toLowerCase().equals(GuiHandler.selectedWorldName.trim().toLowerCase()))
                {
	                FillAvailableWorlds();
	                GuiHandler.pageNumber = 0;
                	if(GuiHandler.worlds.size() > 0)
                	{
                        ArrayList<String> worldNames = new ArrayList<String>();        	            
                        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
                        {
                        	for(File worldDir : TCWorldsDirectory.listFiles())
                        	{
                        		if(worldDir.isDirectory())
                        		{
                        			GuiHandler.selectedWorldName = worldDir.getName();
                        			break;
                        		}
                        	}
                    	}
                		selectedWorldConfig = GuiHandler.worlds.get(GuiHandler.selectedWorldName);
						
    					TerrainControl.PreGenerationSafeMode = selectedWorldConfig.PreGenerationSafeMode;
    					TerrainControl.PregenerationRadius = selectedWorldConfig.PreGenerationRadius;
    					TerrainControl.WorldBorderRadius = selectedWorldConfig.WorldBorderRadius;
    					GuiHandler.seed = selectedWorldConfig.worldSeed;
    					
    					WorldInfo worldInfo = this.mc.getSaveLoader().getWorldInfo(GuiHandler.selectedWorldName);        					
    					if(worldInfo != null)
    					{
    						GuiHandler.seed = worldInfo.getSeed() + "";
    						GuiHandler.gameModeString = worldInfo.getGameType().getName();
    						GuiHandler.hardCore = worldInfo.isHardcoreModeEnabled();
    						GuiHandler.allowCheats = worldInfo.areCommandsAllowed();
    					}
    					
                	} else {
                		GuiHandler.selectedWorldName = null;
	                	selectedWorldConfig = null;
	                	
	                	GuiHandler.seed = "";
						TerrainControl.WorldBorderRadius = 0;
						TerrainControl.PregenerationRadius = 0;
						TerrainControl.PreGenerationSafeMode = true;							
                	}
                	
					// Create new world dir?
                	GuiHandler.worldName = GuiHandler.selectedWorldName;						
					txtWorldName.setText(GuiHandler.worldName != null ?GuiHandler.worldName : "");						
                }
                
				updateWorldName();
				updateButtons();
			}
			this.mc.displayGuiScreen(new TCGuiCreateWorld(new TCGuiSelectCreateWorldMode()));
		}
		else if(askModCompatContinue)
		{
			if(ok)
			{		
				//TerrainControl.resetPregenerator();
				
				this.mc.launchIntegratedServer(GuiHandler.worldName, this.txtWorldName.getText().trim(), worldsettings);
			} else {
				this.mc.displayGuiScreen(new TCGuiCreateWorld(new TCGuiSelectCreateWorldMode()));
			}
		}
		else if(selectingWorldName)
		{
			if(ok)
			{
				if(cloning)
				{
					// Copy world settings
															
		            File sourceDir = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" +  GuiHandler.selectedWorldName);
		            if(sourceDir.exists() && sourceDir.isDirectory())
		            {
		            	for(File worldDir : sourceDir.listFiles())
		            	{
		            		if(worldDir.isDirectory() && worldDir.getName().equals(worldNameToDelete))
		            		{
		            			deleteRecursive(worldDir);
		            			break;
		            		}
		            	}
		        	} else {
		        		this.mc.displayGuiScreen(new GuiErrorScreen("Error", "Could find source directory \"" + GuiHandler.selectedWorldName + "\""));
		        		return;
		        	}
					
					File destDir = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" +  GuiHandler.newWorldName);
					try {
						FileUtils.copyDirectory(sourceDir, destDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
		        		this.mc.displayGuiScreen(new GuiErrorScreen("Error", "Could not copy directory \"" + GuiHandler.selectedWorldName + "\", files may be in use."));
		        		return;
					}
					
					GuiHandler.selectedWorldName = GuiHandler.newWorldName;
					
					updateWorldName();
					updateButtons();
					
				} else {
					// Create new world dir?
					selectedWorldConfig = null;
					GuiHandler.selectedWorldName = null;
					GuiHandler.worldName = GuiHandler.newWorldName;
					txtWorldName.setText(GuiHandler.newWorldName);
					
					GuiHandler.seed = "";
					TerrainControl.WorldBorderRadius = 0;
					TerrainControl.PregenerationRadius = 0;
					TerrainControl.PreGenerationSafeMode = true;
					
					GuiHandler.gameModeString = "survival";
					GuiHandler.hardCore = false;
					GuiHandler.allowCheats = false;
					GuiHandler.bonusChest = false;
					
					updateWorldName();
					updateButtons();
				}
			}
		}
    }
	
	public static void deleteRecursive(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	            	deleteRecursive(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	} 
			
	boolean explainingSafeMode = false;
    public GuiYesNo explainSafeMode(GuiYesNoCallback p_152129_0_)
    {
    	askDeleteSettings = false;
    	selectingWorldName = false;	    
    	askModCompatContinue = false;
    	explainingSafeMode = true;
    	    	
        String s1 = "Pre-generation is faster without safe mode but MC may run out of memory.";
        String s2 = "If so MC will crash intentionally, restart MC and enter the world to continue.";
        String s3 = "Okay";
        String s4 = "Got it";
        
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }
	
	boolean askDeleteSettings = false;
	
    public GuiYesNo askDeleteSettings(GuiYesNoCallback p_152129_0_, String worldName)
    {
    	askDeleteSettings = true;
    	selectingWorldName = false;
    	askModCompatContinue = false;
    	explainingSafeMode = false;
    	
        String s1 = "Delete the TerrainControl world settings for '" + worldName + "'?";
        String s2 = "This will also delete any world (directory) named '" + worldName + "'";
        String s3 = "Delete";
        String s4 = "Cancel";
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }
    	    	    	    
    boolean askModCompatContinue = false;
    
    public GuiYesNo askModCompatContinue(GuiYesNoCallback p_152129_0_, boolean fastcraftEnabled, boolean biomesOPlentyEnabled)
    {
    	askDeleteSettings = false;
    	selectingWorldName = false;	    	
    	askModCompatContinue = true;
    	explainingSafeMode = false;
    	
    	String bop = "Biomes o' plenty may cause crashes. ";
    	String fc = "FastCraft detected, pre-generator";
    	
        String s1 = biomesOPlentyEnabled ? bop + (fastcraftEnabled ? fc : "") : (fastcraftEnabled ? fc : "");
        String s2 = fastcraftEnabled ? "progress screen disabled. Use the launcher log instead." : "";
        String s3 = "Continue";
        String s4 = "Back";
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }
    
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        if (txtSeed.isFocused())
        {
            txtSeed.textboxKeyTyped(p_73869_1_, p_73869_2_);
            try
            {
            	GuiHandler.seed = Long.parseLong(txtSeed.getText()) + "";
            } catch(NumberFormatException ex)
            {
            	GuiHandler.seed = ""; 
            }
        }
        else if (txtPregenRadius.isFocused())
        {
            txtPregenRadius.textboxKeyTyped(p_73869_1_, p_73869_2_);
            try
            {
            	TerrainControl.PregenerationRadius = Integer.parseInt(txtPregenRadius.getText());
            } catch(NumberFormatException ex)
            {
            	TerrainControl.PregenerationRadius = 0; 
            }
        }
        else if (txtWorldBorderRadius.isFocused())
        {
            txtWorldBorderRadius.textboxKeyTyped(p_73869_1_, p_73869_2_);
            try
            {
            	TerrainControl.WorldBorderRadius = Integer.parseInt(txtWorldBorderRadius.getText());
            } catch(NumberFormatException ex)
            {
            	TerrainControl.WorldBorderRadius = 0; 
            }
        }             

        if (p_73869_2_ == 28 || p_73869_2_ == 156)
        {
            actionPerformed((GuiButton)this.buttonList.get(0));
        }
        
        updateWorldName();
    }

    /**
     * Called when the mouse is clicked.
     * @throws IOException 
     */
    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException
    {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);

        this.txtSeed.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        this.txtWorldName.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        this.txtPregenRadius.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        this.txtWorldBorderRadius.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
    }

    WorldSettings worldsettings = null;
    String worldNameToDelete = "";
	boolean selectingWorldName = false;
	boolean cloning = false;
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
        	if (button.id == 3) // Available world 1 
        	{
        		if(btnavailableWorld1.displayString.length() > 0 && !btnavailableWorld1.displayString.equalsIgnoreCase(""))
        		{
        			this.txtWorldName.setText(btnavailableWorld1.displayString);	
        		} else {
        			this.txtWorldName.setText("New World");
        		}
                this.updateWorldName();
        	}
        	if (button.id == 4) // Available world 2 
        	{
        		if(btnavailableWorld2.displayString.length() > 0 && !btnavailableWorld2.displayString.equalsIgnoreCase(""))
        		{
        			this.txtWorldName.setText(btnavailableWorld2.displayString);	
        		} else {
        			this.txtWorldName.setText("New World");
        		}
                this.updateWorldName();
        	}
        	if (button.id == 5) // Available world 3 
        	{
        		if(btnavailableWorld3.displayString.length() > 0 && !btnavailableWorld3.displayString.equalsIgnoreCase(""))
        		{
        			this.txtWorldName.setText(btnavailableWorld3.displayString);	
        		} else {
        			this.txtWorldName.setText("New World");
        		}
                this.updateWorldName();
        	}
        	
        	if (button.id == 8) // Available world delete 1
        	{
        		if(btnavailableWorld1.displayString.length() > 0 && !btnavailableWorld1.displayString.equalsIgnoreCase(""))
        		{
					GuiYesNo guiyesno = askDeleteSettings(this, btnavailableWorld1.displayString);
					worldNameToDelete = btnavailableWorld1.displayString.trim();
					this.mc.displayGuiScreen(guiyesno);
        		}
        	}
        	if (button.id == 9) // Available world delete 2
        	{
        		if(btnavailableWorld2.displayString.length() > 0 && !btnavailableWorld2.displayString.equalsIgnoreCase(""))
        		{
					GuiYesNo guiyesno = askDeleteSettings(this, btnavailableWorld2.displayString);
					worldNameToDelete = btnavailableWorld2.displayString.trim();
					this.mc.displayGuiScreen(guiyesno);            			
        		}            		
        	}
        	if (button.id == 10) // Available world delete 3
        	{
        		if(btnavailableWorld3.displayString.length() > 0 && !btnavailableWorld3.displayString.equalsIgnoreCase(""))
        		{
					GuiYesNo guiyesno = askDeleteSettings(this, btnavailableWorld3.displayString);
					worldNameToDelete = btnavailableWorld3.displayString.trim();
					this.mc.displayGuiScreen(guiyesno);
        		}	
        	}
        	
        	if (button.id == 6) // Previous 
        	{
        		previousPage();
        	}            	
        	if (button.id == 7) // Next 
        	{
        		nextPage();
        	}            	
            if (button.id == 2) // Pre-generation Safe mode
            {
            	if(this.btnPregenerationSafeMode.displayString == "Enabled")
            	{
            		this.btnPregenerationSafeMode.displayString = "Disabled";
        			TerrainControl.PreGenerationSafeMode = false;
					GuiYesNo guiyesno = explainSafeMode(this);
					this.mc.displayGuiScreen(guiyesno);
            	} else {
            		this.btnPregenerationSafeMode.displayString = "Enabled";
            		TerrainControl.PreGenerationSafeMode = true;
            	}
            }
            if (button.id == 1) // Cancel
            {
                this.mc.displayGuiScreen(this.sender);
            }
            else if (button.id == 0) // Create new world
            {
                if (this.bBtnCreateNewWorldClicked)
                {
                    return;
                }

                this.bBtnCreateNewWorldClicked = true;
                long i = (new Random()).nextLong();
                String s = this.txtSeed.getText().trim();

                if (s != null && s.length() > 0)
                {
                    try
                    {
                        long j = Long.parseLong(s);

                        if (j != 0L)
                        {
                            i = j;
                        }
                    }
                    catch (NumberFormatException numberformatexception)
                    {
                        i = (long)s.hashCode();
                    }
                }                    
                
                ISaveFormat isaveformat = this.mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(GuiHandler.worldName);
                
                WorldType.parseWorldType("TerrainControl").onGUICreateWorldPress();
                
                GameType gametype = GameType.getByName(GuiHandler.gameModeString);                    
                worldsettings = new WorldSettings(i, gametype, true, GuiHandler.hardCore, WorldType.parseWorldType("TerrainControl"));
                worldsettings.setGeneratorOptions("TerrainControl");

                if(GuiHandler.bonusChest)
                {
                	worldsettings.enableBonusChest();
                }
                if(GuiHandler.allowCheats)
                {
                	worldsettings.enableCommands();
                }
                
                int a = -1;                    		
                int b = -1; 
                                                                          
                TerrainControl.PregeneratedBorderTop = 0;
                TerrainControl.PregeneratedBorderRight = 0;
                TerrainControl.PregeneratedBorderBottom = 0;
                TerrainControl.PregeneratedBorderLeft = 0;
               
                TerrainControl.LastPregenCheckTime = 0;
                                   
            	TerrainControl.IsNewWorld = true;
                
                // Clear existing pre-generator and structurecache data
                // Do this here in the Forge layer instead of in common since this only applies to Forge atm.
                File TCWorldsDirectory = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
                if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
                {
                	for(File worldDir : TCWorldsDirectory.listFiles())
                	{
                		if(worldDir.isDirectory())
                		{
                			if(GuiHandler.worldName.equals(worldDir.getName()))
                			{
                				
                				File StructureDataDirectory = new File(worldDir.getAbsolutePath() + "/StructureData");
                                if (StructureDataDirectory.exists())
                                {
                                	deleteRecursive(StructureDataDirectory);
                                }

                                File structureDataFile = new File(worldDir.getAbsolutePath() + "/StructureData.txt");
                                if (structureDataFile.exists())
                                {
                                	deleteRecursive(structureDataFile);
                                }
                                
                                File nullChunksFile = new File(worldDir.getAbsolutePath() + "/NullChunks.txt");
                                if (nullChunksFile.exists())
                                {
                                	deleteRecursive(nullChunksFile);
                                }
                                
                                File spawnedStructuresFile = new File(worldDir.getAbsolutePath() + "/SpawnedStructures.txt");
                                if (spawnedStructuresFile.exists())
                                {
                                	deleteRecursive(spawnedStructuresFile);
                                }

                                File chunkProviderPopulatedChunksFile = new File(worldDir.getAbsolutePath() + "/ChunkProviderPopulatedChunks.txt");
                                if (chunkProviderPopulatedChunksFile.exists())
                                {
                                	deleteRecursive(chunkProviderPopulatedChunksFile);
                                }

                                File pregeneratedChunksFile = new File(worldDir.getAbsolutePath() + "/PregeneratedChunks.txt");
                                if (pregeneratedChunksFile.exists())
                                {
                                	deleteRecursive(pregeneratedChunksFile);
                                }                    				
                				
                				break;
                			}
                		}
                	}
            	}
                                    
    			boolean fastcraftEnabled = false;
    			boolean biomesOPlentyEnabled = false;
    			for (ModContainer mod : Loader.instance().getActiveModList())
    			{
    				if(mod.getName().toLowerCase().equals("fastcraft"))
    				{
    					fastcraftEnabled = true;
    				}
    				if(mod.getName().toLowerCase().equals("biomes o' plenty"))
    				{
    					biomesOPlentyEnabled = true;
    				}
    				if(fastcraftEnabled && biomesOPlentyEnabled)
    				{
    					break;
    				}
    			}
    			
    			if(TerrainControl.PregenerationRadius > 0 && fastcraftEnabled)
    			{
					GuiYesNo guiyesno = askModCompatContinue(this, fastcraftEnabled, biomesOPlentyEnabled);
					this.mc.displayGuiScreen(guiyesno);
    			}
    			else if(biomesOPlentyEnabled)
            	{
					GuiYesNo guiyesno = askModCompatContinue(this, false, biomesOPlentyEnabled);
					this.mc.displayGuiScreen(guiyesno);        				
				} else {
					TerrainControl.resetPregenerator();
					
    				this.mc.launchIntegratedServer(GuiHandler.worldName, this.txtWorldName.getText().trim(), worldsettings);
    			}
            }
            else if (button.id == 11) // Game mode
            {                	
                if (GuiHandler.gameModeString.equals("survival"))
                {
                	GuiHandler.gameModeString = "hardcore";
                	GuiHandler.hardCore = true;
                }
                else if (GuiHandler.gameModeString.equals("hardcore"))
                {
                	GuiHandler.gameModeString = "creative";
                	GuiHandler.hardCore = false;
                } else {
                	GuiHandler.gameModeString = "survival";
                	GuiHandler.hardCore = false;
                }
                updateButtons();
            }
            if(button.id == 12) // AllowCommands
            {
            	GuiHandler.allowCheats = !GuiHandler.allowCheats;
                updateButtons();
            }
            if(button.id == 13) // Bonus chest
            {
            	GuiHandler.bonusChest = !GuiHandler.bonusChest;
            	updateButtons();
            }
            if(button.id == 14) // Clone
            {   
            	if(GuiHandler.selectedWorldName != null)
            	{
            		cloning = true;
            		
        	    	askDeleteSettings = false;
        	    	selectingWorldName = true;	    
        	    	askModCompatContinue = false;
        	    	explainingSafeMode = false;
        	    	
        	        FillAvailableWorlds();
        	        TCGuiEnterWorldName guiRenameWorld = new TCGuiEnterWorldName(this, GuiHandler.selectedWorldName);
					this.mc.displayGuiScreen(guiRenameWorld);
            	}
            }
            if(button.id == 15) // New
            {
            	cloning = false;
            	
    	    	askDeleteSettings = false;
    	    	selectingWorldName = true;	    
    	    	askModCompatContinue = false;
    	    	explainingSafeMode = false;
            	
    	        FillAvailableWorlds();
    	        TCGuiEnterWorldName guiRenameWorld = new TCGuiEnterWorldName(this, "New world");
				this.mc.displayGuiScreen(guiRenameWorld);
            }
        }
    }
    
	boolean derping;
	
    private void updateButtons()
    {
    	if(GuiHandler.hardCore)
    	{
    		GuiHandler.allowCheats = false;
    		GuiHandler.bonusChest = false;
            btnAllowCheats.enabled = false;
            btnBonusChest.enabled = false;
    	} else {
            btnAllowCheats.enabled = true;
            btnBonusChest.enabled = true;        		
    	}
    	
    	btnGameMode.displayString = I18n.format("selectWorld.gameMode", new Object[0]) + " " + I18n.format("selectWorld.gameMode." + GuiHandler.gameModeString, new Object[0]);

        btnBonusChest.displayString = I18n.format("selectWorld.bonusItems", new Object[0]) + " ";

        if (GuiHandler.bonusChest)
        {
        	btnBonusChest.displayString = btnBonusChest.displayString + I18n.format("options.on", new Object[0]);
        } else {
        	btnBonusChest.displayString = btnBonusChest.displayString + I18n.format("options.off", new Object[0]);
        }

        btnAllowCheats.displayString = I18n.format("selectWorld.allowCommands", new Object[0]) + " ";

        if (GuiHandler.allowCheats)
        {
        	btnAllowCheats.displayString = btnAllowCheats.displayString + I18n.format("options.on", new Object[0]);
        } else {
        	btnAllowCheats.displayString = btnAllowCheats.displayString + I18n.format("options.off", new Object[0]);
        }
    }
    
    // Move the other controls for the time being (until the remaining buttons are fixed)
    int heightOffset = 17;
    int heightOffset1 = 43;
    int heightOffset2 = 5;
    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        
        this.buttonList.clear();
        
        // World name
        this.txtWorldName = new GuiTextField(20, this.fontRendererObj, this.width / 2 - 164, 45 + heightOffset, 160, 20); //left, top, width, height           
        this.txtWorldName.setText(GuiHandler.selectedWorldName != null ? GuiHandler.selectedWorldName : GuiHandler.worldName != null ? GuiHandler.worldName : I18n.format("selectWorld.newWorld", new Object[0]));
        this.txtWorldName.setEnabled(false);
        
        // Seed
        this.txtSeed = new GuiTextField(21, this.fontRendererObj, this.width / 2 - 164, 101 + heightOffset, 160, 20);
        this.txtSeed.setFocused(true);
        this.txtSeed.setText(GuiHandler.seed != null ? GuiHandler.seed : "");

        int btnwidth = 136;
        
        // Available worlds
        btnavailableWorld1 = new GuiButton(3, this.width / 2 + 6, 43 + heightOffset, btnwidth, 20, "");            
        btnavailableWorld2 = new GuiButton(4, this.width / 2 + 6, 43 + 24 + heightOffset, btnwidth, 20, "");
        btnavailableWorld3 = new GuiButton(5, this.width / 2 + 6, 43 + 48 + heightOffset, btnwidth, 20, "");
        btnavailableWorldPrev = new GuiButton(6, this.width / 2 + 6, 43 + 72 + heightOffset, 25, 20, "<");
        btnavailableWorldNext = new GuiButton(7, this.width / 2 + 6 + 28, 43 + 72 + heightOffset, 25, 20, ">");
        btnavailableWorldClone = new GuiButton(14, this.width / 2 + 6 + 56, 43 + 72 + heightOffset, 50, 20, "Clone");
    	btnavailableWorldClone.enabled = GuiHandler.selectedWorldName != null;
        btnavailableWorldNew = new GuiButton(15, this.width / 2 + 6 + 110, 43 + 72 + heightOffset, 50, 20, "New");
        
        // Available worlds delete btns
        btnavailableWorldDelete1 = new GuiButton(8, this.width / 2 + 6 + btnwidth + 4, 43 + heightOffset, 20, 20, "X");
        btnavailableWorldDelete2 = new GuiButton(9, this.width / 2 + 6 + btnwidth + 4, 43 + 24 + heightOffset, 20, 20, "X");
        btnavailableWorldDelete3 = new GuiButton(10, this.width / 2 + 6 + btnwidth + 4, 43 + 48 + heightOffset, 20, 20, "X");
        
        this.buttonList.add(btnavailableWorld1);
        this.buttonList.add(btnavailableWorld2);
        this.buttonList.add(btnavailableWorld3);
        this.buttonList.add(btnavailableWorldPrev);
        this.buttonList.add(btnavailableWorldNext);
        this.buttonList.add(btnavailableWorldClone);
        this.buttonList.add(btnavailableWorldNew);
        
        this.buttonList.add(btnavailableWorldDelete1);
        this.buttonList.add(btnavailableWorldDelete2);
        this.buttonList.add(btnavailableWorldDelete3);
        
        FillAvailableWorlds();
        
        // Pre-generation radius
        this.txtPregenRadius = new GuiTextField(23, this.fontRendererObj, this.width / 2 - 164, 159 + heightOffset, 50, 20);
        this.txtPregenRadius.setText(TerrainControl.PregenerationRadius + "");            
        
        // Pre-generator safe mode
        btnPregenerationSafeMode = new GuiButton(2, this.width / 2 - 164 + 125, 161 + heightOffset, 50, 20, TerrainControl.PreGenerationSafeMode ? "Enabled" : "Disabled");
        this.buttonList.add(btnPregenerationSafeMode);
        
        // World border
        this.txtWorldBorderRadius = new GuiTextField(24, this.fontRendererObj, this.width / 2 - 164 + 210, 159 + heightOffset, 50, 20);
        this.txtWorldBorderRadius.setText(TerrainControl.WorldBorderRadius + "");

        // TODO: Fix pre-generator and world borders
        this.txtPregenRadius.setVisible(false);
        btnPregenerationSafeMode.visible = false;
        this.txtWorldBorderRadius.setVisible(false);
                
        btnGameMode = new GuiButton(11, this.width / 2 - 166, 188 - heightOffset1 + heightOffset, 122, 20, I18n.format("selectWorld.gameMode", new Object[0]));
        btnAllowCheats = new GuiButton(12, this.width / 2 - 39, 188 - heightOffset1 + heightOffset, 100, 20, I18n.format("selectWorld.allowCommands", new Object[0]));
        btnBonusChest = new GuiButton(13, this.width / 2 + 66, 188 - heightOffset1 + heightOffset, 100, 20, I18n.format("selectWorld.bonusItems", new Object[0]));
        
        this.buttonList.add(btnGameMode);
        this.buttonList.add(btnAllowCheats);
        this.buttonList.add(btnBonusChest);
        
        // Create / Cancel
        btnCreateWorld = new GuiButton(0, this.width / 2 - 166, 213 - heightOffset1 + heightOffset, 164, 20, I18n.format("selectWorld.create", new Object[0]));
        this.buttonList.add(btnCreateWorld);
        this.buttonList.add(new GuiButton(1, this.width / 2 + 2, 213 - heightOffset1 + heightOffset, 164, 20, I18n.format("gui.cancel", new Object[0])));
        
        this.updateWorldName();
        this.updateButtons();
    }
    
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        
        // Create new world title
        this.drawCenteredString(this.fontRendererObj, I18n.format("Create a new TerrainControl world", new Object[0]), this.width / 2, 10 + heightOffset - heightOffset2, -1);

        // World name
        this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 164, 30 + heightOffset, -6250336);
        this.drawString(this.fontRendererObj, this.worldNameHelpText, this.width / 2 - 164, 70 + heightOffset, -6250336);
        this.txtWorldName.drawTextBox();
        
        // Available worlds
        this.drawString(this.fontRendererObj, I18n.format("World settings", new Object[0]), this.width / 2 + 9, 30 + heightOffset, -6250336);
        
        // Seed
        this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterSeed", new Object[0]), this.width / 2 - 164, 88 + heightOffset, -6250336);
        this.drawString(this.fontRendererObj, I18n.format("selectWorld.seedInfo", new Object[0]), this.width / 2 - 164, 126 + heightOffset, -6250336);
        this.txtSeed.drawTextBox();

        // TODO: Fix pre-generator and world borders
        
        // Pre-generation Radius 
        //this.drawString(this.fontRendererObj, "Pre-generation radius", this.width / 2 - 164, 145, -6250336);

        // Pre-generation safe mode
        //this.drawString(this.fontRendererObj, "Safe mode", this.width / 2 - 164 + 125, 145, -6250336);
        
        // World border Radius
        //this.drawString(this.fontRendererObj, "World border radius", this.width / 2 - 164 + 210, 145, -6250336);           
        
        //this.drawString(this.fontRendererObj, "chunks", this.width / 2 - 164 + 60, 165, -6250336);
        //this.drawString(this.fontRendererObj, "chunks", this.width / 2 - 164 + 210 + 60, 165, -6250336);
        
        //this.txtPregenRadius.drawTextBox();
        //this.txtWorldBorderRadius.drawTextBox();

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
