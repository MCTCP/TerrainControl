package com.pg85.otg.forge.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.util.IOHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
  
@SideOnly(Side.CLIENT)
public class OTGGuiCreateWorld extends GuiScreen implements GuiYesNoCallback
{	
    GuiScreen sender;
    
    GuiTextField txtWorldName;
    GuiTextField txtSeed;
    
    GuiButton btnavailableWorld1;
    GuiButton btnavailableWorld2;
    GuiButton btnavailableWorld3;
    GuiButton btnavailableWorldPrev;
    GuiButton btnavailableWorldNext;
    GuiButton btnavailableWorldNew;
    GuiButton btnavailableWorldClone;
    
    GuiButton btnavailableWorldDelete1;
    GuiButton btnavailableWorldDelete2;
    GuiButton btnavailableWorldDelete3;          
    
    GuiTextField txtPregenRadius;    
    GuiTextField txtWorldBorderRadius;   

    GuiButton btnGameMode;
    GuiButton btnAllowCheats;
    GuiButton btnBonusChest;
    
    GuiButton btnCreateWorld;        
    
    boolean bBtnCreateNewWorldClicked;
    
    final String[] field_146327_L = new String[] {"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    public OTGGuiCreateWorld(GuiScreen sender)
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
        File TCWorldsDirectory = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
        {
        	for(File worldDir : TCWorldsDirectory.listFiles())
        	{
        		if(worldDir.isDirectory() && !worldDir.getName().toLowerCase().trim().startsWith("dim-"))
        		{
        			worldNames.add(worldDir.getName());
        			if(!GuiHandler.worlds.containsKey(worldDir.getName()))
        			{
        		        File worldConfigFile = new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        		        SettingsMap settingsMap = FileSettingsReader.read(worldDir.getName(), worldConfigFile);
        		        WorldConfig worldConfig = new WorldConfig(worldDir, settingsMap, null);
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
                        
            if (this.mc.getSaveLoader().getWorldInfo(GuiHandler.worldName) != null)
            {
            	worldNameHelpText = "Existing world will be deleted!";
            } else {            	           	
            	worldNameHelpText = "New world dir will be created";
            }            
           
        	boolean usingPreset = false;
            	        		        	
            File TCWorldsDirectory = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
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
            					GuiHandler.PregenerationRadius = selectedWorldConfig.PreGenerationRadius;
            					GuiHandler.WorldBorderRadius = selectedWorldConfig.WorldBorderRadius;            					
            					
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
            	
                txtPregenRadius.setText(GuiHandler.PregenerationRadius + "");
                txtWorldBorderRadius.setText(GuiHandler.WorldBorderRadius + "");
            	
                txtSeed.setText(GuiHandler.seed);
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
		if(askDeleteSettings)
		{
			if(ok)
			{
				GuiYesNo guiyesno = askDeleteSettings(this, worldNameToDelete);
				this.mc.displayGuiScreen(guiyesno);					
				
	            File TCWorldsDirectory = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
	            if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
	            {
	            	for(File worldDir : TCWorldsDirectory.listFiles())
	            	{
	            		if(worldDir.isDirectory() && worldDir.getName().equals(worldNameToDelete))
	            		{
            				IOHelper.deleteRecursive(worldDir);
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
                        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
                        {
                        	for(File worldDir : TCWorldsDirectory.listFiles())
                        	{
                        		if(worldDir.isDirectory() && !worldDir.getName().toLowerCase().startsWith("dim-"))
                        		{
                        			GuiHandler.selectedWorldName = worldDir.getName();
                        			break;
                        		}
                        	}
                    	}
                		selectedWorldConfig = GuiHandler.worlds.get(GuiHandler.selectedWorldName);
						                		
                		GuiHandler.PregenerationRadius = selectedWorldConfig.PreGenerationRadius;
                		GuiHandler.WorldBorderRadius = selectedWorldConfig.WorldBorderRadius;
                		
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
	                	GuiHandler.WorldBorderRadius = 0;
	                	GuiHandler.PregenerationRadius = 0;	                	
                	}
                	
					// Create new world dir?
                	GuiHandler.worldName = GuiHandler.selectedWorldName;
					txtWorldName.setText(GuiHandler.worldName != null ?GuiHandler.worldName : "");						
                }
                
				updateWorldName();
				updateButtons();
			}
			this.mc.displayGuiScreen(new OTGGuiCreateWorld(new OTGGuiWorldSelection(null)));
		}
		else if(askModCompatContinue)
		{
			if(ok)
			{
				DeleteWorldFiles(GuiHandler.worldName);
				this.mc.launchIntegratedServer(GuiHandler.worldName, this.txtWorldName.getText().trim(), worldsettings);
			} else {
				this.mc.displayGuiScreen(new OTGGuiCreateWorld(new OTGGuiWorldSelection(null)));
			}
		}
		else if(selectingWorldName)
		{
			if(ok)
			{
				if(cloning)
				{
					// Copy world settings
															
		            File sourceDir = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" +  GuiHandler.selectedWorldName);
		            if(sourceDir.exists() && sourceDir.isDirectory())
		            {
		            	for(File worldDir : sourceDir.listFiles())
		            	{
		            		if(worldDir.isDirectory() && worldDir.getName().equals(worldNameToDelete))
		            		{
		            			IOHelper.deleteRecursive(worldDir);
		            			break;
		            		}
		            	}
		        	} else {
		        		this.mc.displayGuiScreen(new GuiErrorScreen("Error", "Could find source directory \"" + GuiHandler.selectedWorldName + "\""));
		        		return;
		        	}
					
					File destDir = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" +  GuiHandler.newWorldName);
					try {
						FileUtils.copyDirectory(sourceDir, destDir);
					} catch (IOException e) {
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
					GuiHandler.WorldBorderRadius = 0;
					GuiHandler.PregenerationRadius = 0;
					
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
	
	boolean askDeleteSettings = false;	
    public GuiYesNo askDeleteSettings(GuiYesNoCallback p_152129_0_, String worldName)
    {
    	askDeleteSettings = true;
    	selectingWorldName = false;
    	askModCompatContinue = false;
    	
        String s1 = "Delete the OpenTerrainGenerator world settings for '" + worldName + "'?";
        String s2 = "This will also delete any world (directory) named '" + worldName + "'";
        String s3 = "Delete";
        String s4 = "Cancel";
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }

    boolean askModCompatContinue = false;  
    public GuiYesNo askModCompatContinue(GuiYesNoCallback p_152129_0_, boolean customMobSpawnerEnabled, boolean biomesOPlentyEnabled)
    {
    	askDeleteSettings = false;
    	selectingWorldName = false;
    	askModCompatContinue = true;
    	
        String s1 = "Warning: " + (biomesOPlentyEnabled ? " Biomes o' plenty may cause crashes." : "") + (customMobSpawnerEnabled ? " CustomMobSpawner may break OTG mob spawning." : "");
        String s2 = "It is recommended that you disable or remove " + (customMobSpawnerEnabled && biomesOPlentyEnabled ? "these mods." : "this mod.");
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
            GuiHandler.seed = txtSeed.getText();
        }
        else if (txtPregenRadius.isFocused())
        {
            txtPregenRadius.textboxKeyTyped(p_73869_1_, p_73869_2_);
            try
            {
            	GuiHandler.PregenerationRadius = Integer.parseInt(txtPregenRadius.getText());
            } catch(NumberFormatException ex)
            {
            	GuiHandler.PregenerationRadius = 0;
            }
        }
        else if (txtWorldBorderRadius.isFocused())
        {
            txtWorldBorderRadius.textboxKeyTyped(p_73869_1_, p_73869_2_);
            try
            {
            	GuiHandler.WorldBorderRadius = Integer.parseInt(txtWorldBorderRadius.getText());
            }
            catch(NumberFormatException ex)
            {
            	GuiHandler.WorldBorderRadius = 0;
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
                
                WorldType.parseWorldType("OTG").onGUICreateWorldPress();
                
                GameType gametype = GameType.getByName(GuiHandler.gameModeString);                    
                worldsettings = new WorldSettings(i, gametype, true, GuiHandler.hardCore, WorldType.parseWorldType("OTG"));
                worldsettings.setGeneratorOptions("OpenTerrainGenerator");

                if(GuiHandler.bonusChest)
                {
                	worldsettings.enableBonusChest();
                }
                if(GuiHandler.allowCheats)
                {
                	worldsettings.enableCommands();
                }                                  

                boolean biomesOPlentyEnabled = false;
    			boolean customMobSpawnerEnabled = false;
    			for (ModContainer mod : Loader.instance().getActiveModList())
    			{
    				if(mod.getName().toLowerCase().equals("biomes o' plenty"))
    				{
    					biomesOPlentyEnabled = true;
    				}
    				if(mod.getName().toLowerCase().equals("drzhark's customspawner"))
    				{
    					customMobSpawnerEnabled = true;
    				}
    				if(biomesOPlentyEnabled && customMobSpawnerEnabled)
    				{
    					break;
    				}
    			}
    			
    			if(biomesOPlentyEnabled || customMobSpawnerEnabled)
            	{
					GuiYesNo guiyesno = askModCompatContinue(this, customMobSpawnerEnabled, biomesOPlentyEnabled);
					this.mc.displayGuiScreen(guiyesno);        				
				} else {									
					DeleteWorldFiles(GuiHandler.worldName);
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
        	    	
        	        FillAvailableWorlds();
        	        OTGGuiEnterWorldName guiRenameWorld = new OTGGuiEnterWorldName(this, GuiHandler.selectedWorldName);
					this.mc.displayGuiScreen(guiRenameWorld);
            	}
            }
            if(button.id == 15) // New
            {
            	cloning = false;
            	
    	    	askDeleteSettings = false;
    	    	selectingWorldName = true;	    
    	    	askModCompatContinue = false;
            	
    	        FillAvailableWorlds();
    	        OTGGuiEnterWorldName guiRenameWorld = new OTGGuiEnterWorldName(this, "New World");
				this.mc.displayGuiScreen(guiRenameWorld);
            }
        }
    }
    
    private void DeleteWorldFiles(String worldFolderName)
    {
    	File TCWorldsDirectory = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
    	DeleteWorldFiles(TCWorldsDirectory, worldFolderName);    	     
        
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        isaveformat.flushCache();
        isaveformat.deleteWorldDirectory(worldFolderName);
    }
    
    private void DeleteWorldFiles(File TCWorldsDirectory, String WorldName)
    {
    	// TODO: This is for legacy worlds only, the files are stored in the world saves directory now. Remove this?
    	
        // Clear existing pre-generator and structurecache data
        // Do this here in the Forge layer instead of in common since this only applies to Forge atm.
   	
        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
        {
        	for(File worldDir : TCWorldsDirectory.listFiles())
        	{
        		if(worldDir.isDirectory())
        		{
        			if(WorldName.equals(worldDir.getName()))
        			{       				
        				File StructureDataDirectory = new File(worldDir.getAbsolutePath() + "/StructureData");
                        if (StructureDataDirectory.exists())
                        {
                        	IOHelper.deleteRecursive(StructureDataDirectory);
                        }

                        File dimensionsDataFile = new File(worldDir.getAbsolutePath() + "/Dimensions.txt");
                        if (dimensionsDataFile.exists())
                        {
                        	IOHelper.deleteRecursive(dimensionsDataFile);
                        }
                        
                        File structureDataFile = new File(worldDir.getAbsolutePath() + "/StructureData.txt");
                        if (structureDataFile.exists())
                        {
                        	IOHelper.deleteRecursive(structureDataFile);
                        }
                        
                        File nullChunksFile = new File(worldDir.getAbsolutePath() + "/NullChunks.txt");
                        if (nullChunksFile.exists())
                        {
                        	IOHelper.deleteRecursive(nullChunksFile);
                        }
                        
                        File spawnedStructuresFile = new File(worldDir.getAbsolutePath() + "/SpawnedStructures.txt");
                        if (spawnedStructuresFile.exists())
                        {
                        	IOHelper.deleteRecursive(spawnedStructuresFile);
                        }

                        File chunkProviderPopulatedChunksFile = new File(worldDir.getAbsolutePath() + "/ChunkProviderPopulatedChunks.txt");
                        if (chunkProviderPopulatedChunksFile.exists())
                        {
                        	IOHelper.deleteRecursive(chunkProviderPopulatedChunksFile);
                        }

                        File pregeneratedChunksFile = new File(worldDir.getAbsolutePath() + "/PregeneratedChunks.txt");
                        if (pregeneratedChunksFile.exists())
                        {
                        	IOHelper.deleteRecursive(pregeneratedChunksFile);
                        }                    				
        				
        				break;
        			}
        		}
        	}
    	}
    }

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
        
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        
        this.buttonList.clear();
        
        // World name
        this.txtWorldName = new GuiTextField(30, this.fontRenderer, this.width / 2 - 164, 45, 160, 20); //left, top, width, height        
        this.txtWorldName.setEnabled(false);
        
        // Seed
        this.txtSeed = new GuiTextField(31, this.fontRenderer, this.width / 2 - 164, 101, 160, 20);
        this.txtSeed.setFocused(true);
        this.txtSeed.setText(GuiHandler.seed != null ? GuiHandler.seed : "");

        int btnwidth = 136;
        
        // Available worlds
        btnavailableWorld1 = new GuiButton(3, this.width / 2 + 6, 43, btnwidth, 20, "");            
        btnavailableWorld2 = new GuiButton(4, this.width / 2 + 6, 43 + 24, btnwidth, 20, "");
        btnavailableWorld3 = new GuiButton(5, this.width / 2 + 6, 43 + 48, btnwidth, 20, "");
        btnavailableWorldPrev = new GuiButton(6, this.width / 2 + 6, 43 + 72, 25, 20, "<");
        btnavailableWorldNext = new GuiButton(7, this.width / 2 + 6 + 28, 43 + 72, 25, 20, ">");
        btnavailableWorldClone = new GuiButton(14, this.width / 2 + 6 + 56, 43 + 72, 50, 20, "Clone");
    	btnavailableWorldClone.enabled = GuiHandler.selectedWorldName != null;
        btnavailableWorldNew = new GuiButton(15, this.width / 2 + 6 + 110, 43 + 72, 50, 20, "New");
        
        // Available worlds delete btns
        btnavailableWorldDelete1 = new GuiButton(8, this.width / 2 + 6 + btnwidth + 4, 43, 20, 20, "X");
        btnavailableWorldDelete2 = new GuiButton(9, this.width / 2 + 6 + btnwidth + 4, 43 + 24, 20, 20, "X");
        btnavailableWorldDelete3 = new GuiButton(10, this.width / 2 + 6 + btnwidth + 4, 43 + 48, 20, 20, "X");
        
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
                               
        String worldName = "";        
    	if(GuiHandler.selectedWorldName != null)
		{
    		worldName = GuiHandler.selectedWorldName; 
		}
		else if (GuiHandler.worldName != null)
		{
			worldName = GuiHandler.worldName;
		} else {
    		if(btnavailableWorld1.displayString.length() > 0 && !btnavailableWorld1.displayString.equalsIgnoreCase(""))
    		{
    			worldName = btnavailableWorld1.displayString;
			} else {
				worldName = I18n.format("selectWorld.newWorld", new Object[0]);
			}
		}
        
        // Pre-generation radius
        this.txtPregenRadius = new GuiTextField(20, this.fontRenderer, this.width / 2 - 164, 159, 50, 20);
        this.txtPregenRadius.setText(GuiHandler.PregenerationRadius + "");
                
        // World border
        this.txtWorldBorderRadius = new GuiTextField(21, this.fontRenderer, this.width / 2 - 164 + 210, 159, 50, 20);
        this.txtWorldBorderRadius.setText(GuiHandler.WorldBorderRadius + "");
        
        btnGameMode = new GuiButton(11, this.width / 2 - 166, 188, 122, 20, I18n.format("selectWorld.gameMode", new Object[0]));
        btnAllowCheats = new GuiButton(12, this.width / 2 - 39, 188, 100, 20, I18n.format("selectWorld.allowCommands", new Object[0]));
        btnBonusChest = new GuiButton(13, this.width / 2 + 66, 188, 100, 20, I18n.format("selectWorld.bonusItems", new Object[0]));
        
        this.buttonList.add(btnGameMode);
        this.buttonList.add(btnAllowCheats);
        this.buttonList.add(btnBonusChest);
        
        // Create / Cancel
        btnCreateWorld = new GuiButton(0, this.width / 2 - 166, 213, 164, 20, I18n.format("selectWorld.create", new Object[0]));
        this.buttonList.add(btnCreateWorld);
        this.buttonList.add(new GuiButton(1, this.width / 2 + 2, 213, 164, 20, I18n.format("gui.cancel", new Object[0])));
   	
    	this.txtWorldName.setText(worldName);
   	
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
        this.drawCenteredString(this.fontRenderer, I18n.format("Create a new OpenTerrainGenerator world", new Object[0]), this.width / 2, 10, -1);

        // World name
        this.drawString(this.fontRenderer, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 164, 30, -6250336);
        this.drawString(this.fontRenderer, this.worldNameHelpText, this.width / 2 - 164, 70, -6250336);
        this.txtWorldName.drawTextBox();
        
        // Available worlds
        this.drawString(this.fontRenderer, I18n.format("World settings", new Object[0]), this.width / 2 + 9, 30, -6250336);
        
        // Seed
        this.drawString(this.fontRenderer, I18n.format("selectWorld.enterSeed", new Object[0]), this.width / 2 - 164, 88, -6250336);
        this.drawString(this.fontRenderer, I18n.format("selectWorld.seedInfo", new Object[0]), this.width / 2 - 164, 126, -6250336);
        this.txtSeed.drawTextBox();
        
        // Pre-generation Radius 
        this.drawString(this.fontRenderer, "Pre-generation radius", this.width / 2 - 164, 145, -6250336);
        
        // World border Radius
        this.drawString(this.fontRenderer, "World border radius", this.width / 2 - 164 + 210, 145, -6250336);           
        
        this.drawString(this.fontRenderer, "chunks", this.width / 2 - 164 + 60, 165, -6250336);
        this.drawString(this.fontRenderer, "chunks", this.width / 2 - 164 + 210 + 60, 165, -6250336);
        
        this.txtPregenRadius.drawTextBox();
        this.txtWorldBorderRadius.drawTextBox();

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
