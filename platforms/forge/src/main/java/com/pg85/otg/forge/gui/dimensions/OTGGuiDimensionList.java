package com.pg85.otg.forge.gui.dimensions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import scala.Int;

import org.lwjgl.input.Mouse;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.generator.Pregenerator;
import com.pg85.otg.forge.gui.OTGGuiEnterWorldName;
import com.pg85.otg.forge.gui.presets.OTGGuiPresetList;
import com.pg85.otg.forge.network.client.ClientPacketManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.logging.LogMarker;

public class OTGGuiDimensionList extends GuiScreen implements GuiYesNoCallback
{
	static
	{
		DimensionType.register("overworld", "OTG", 0, OTGWorldProvider.class, false);
	}
    private static boolean ShowingOpenLinkDialogue = false;
	
	SettingEntry<?> buttonId;
    public OTGGuiDimensionSettingsList dimensionSettingsList;
	
    OTGGuiPresetList selectPresetForDimensionMenu = new OTGGuiPresetList(this);
    boolean selectingPresetForDimension = false;
    boolean creatingNewDimension = false;
	
    public OTGGuiPresetList previousMenu;
    private OTGGuiSlotDimensionList dimensionsList;
    public int selectedDimensionIndex = -1;
    public String worldName;
    public final ArrayList<DimensionConfig> dimensions;
    public final ArrayList<DimensionConfig> originalDimensions;
    DimensionConfig selectedDimension;
    
    int listWidth = 100;
    int topMargin = 37;
    int bottomMargin = 73;
    private int btnBottomMargin = 63;
    int leftMargin = 10;
    private int rightMargin = 10;
    int slotHeight = 16;
    private int margin = 20;
    
    GuiButton btnContinue;
    GuiButton btnCancel;
    GuiButton btnDelete;
    private final int iContinueButton = 0;
    private final int iCancelButton = 1;
    private final int iNewButton = 2;
    private final int iDeleteButton = 3;
    
    private boolean restoreSelection = false;
	private boolean previouslySelectedMainMenu = false;
	private boolean previouslySelectedGameRulesMenu = false;
	private boolean previouslySelectedAdvancedSettingsMenu = false;
	private float lastScrollPos = 0;
	
	boolean settingsChanged = false;
    
    private int wikiBtnLeft;
    private int wikiBtnTop;
    private int wikiBtnWidth;
    private int wikiBtnHeight;
    private int wikiBtnRight;
    private int wikiBtnBottom;
    private long lastPregeneratorCheckTime = System.currentTimeMillis();   
	
    public OTGGuiDimensionList(OTGGuiPresetList previousMenu)
    {
    	ShowingOpenLinkDialogue = false;
        this.previousMenu = previousMenu;        
        this.dimensions = new ArrayList<DimensionConfig>();
        
        // If previousMenu is null then this screen was opened in-game
        if(previousMenu == null)
        {
        	// If world is null then we're ingame
        	if(Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().isSingleplayer())
        	{
        		// For SP values are applied immediately, don't clone.
        		this.dimensions.add(OTG.getDimensionsConfig().Overworld);
            	for(DimensionConfig dimConfig : OTG.getDimensionsConfig().Dimensions)
            	{
            		this.dimensions.add(dimConfig);
            	}
        	} else {
        		this.dimensions.add(OTG.getDimensionsConfig().Overworld.clone());
            	for(DimensionConfig dimConfig : OTG.getDimensionsConfig().Dimensions)
            	{
            		this.dimensions.add(dimConfig.clone());
            	}
        	}
        } else {
	        
	        // If a modpack creator has added a default config, use that, otherwise use world config.
	        DimensionsConfig defaultConfig = DimensionsConfig.getModPackConfig(previousMenu.selectedPreset.getFirst());
	        if(defaultConfig != null)
	        {
	        	this.dimensions.add(defaultConfig.Overworld.clone());
	        	for(DimensionConfig dimConfig : defaultConfig.Dimensions)
	        	{
	        		this.dimensions.add(dimConfig.clone());	        		
	        	}	        	
	        } else {
	        	// Add overworld
		        this.dimensions.add(new DimensionConfig(previousMenu.selectedPreset.getSecond()));
		                
		        // Add any dimensions from the worldconfig's dimensions list if their presets are installed
		        for(String dimName : previousMenu.selectedPreset.getSecond().dimensions)
		        {
		        	for(Entry<String, DimensionConfigGui> preset : ForgeEngine.Presets.entrySet())
		        	{
		        		if(dimName.equals(preset.getKey()))
		        		{
		        			this.dimensions.add(new DimensionConfig(preset.getValue()));
		        			break;
		        		}
		        	}
		        }
	        }
        }
        
        this.selectedDimension = this.dimensions.get(0);
        this.selectedDimensionIndex = 0;
        
        // Store original dimensions to compare differences after entering values for settings
        this.originalDimensions = new ArrayList<DimensionConfig>();
        for(DimensionConfig dimConfig : this.dimensions)
        {
        	this.originalDimensions.add(dimConfig.clone());
        }
    }

    // Only used for MP clients when refreshing the dimensions screen after receiving new data from the server
    public OTGGuiDimensionList(int previouslySelectedIndex, boolean isMainMenu, boolean isGameRulesMenu, boolean isAdvancedSettingsMenu, float lastScrollPos)
    {
        this.previousMenu = null;        
        this.dimensions = new ArrayList<DimensionConfig>();
        
        this.restoreSelection = true;
        this.previouslySelectedMainMenu = isMainMenu;
        this.previouslySelectedGameRulesMenu = isGameRulesMenu;
        this.previouslySelectedAdvancedSettingsMenu = isAdvancedSettingsMenu;
        this.lastScrollPos = lastScrollPos;
        
    	// If world is null then we're ingame
		this.dimensions.add(OTG.getDimensionsConfig().Overworld.clone());
    	for(DimensionConfig dimConfig : OTG.getDimensionsConfig().Dimensions)
    	{
    		this.dimensions.add(dimConfig.clone());
    	}
        
    	if(previouslySelectedIndex >= this.dimensions.size())
    	{
    		previouslySelectedIndex = this.dimensions.size() - 1;
    	}
    	
        this.selectedDimension = this.dimensions.get(previouslySelectedIndex);
        this.selectedDimensionIndex = previouslySelectedIndex;
        
        // Store original dimensions to compare differences after entering values for settings
        this.originalDimensions = new ArrayList<DimensionConfig>();
        for(DimensionConfig dimConfig : this.dimensions)
        {
        	this.originalDimensions.add(dimConfig.clone());
        }
	}
    
    Minecraft getMinecraftInstance()
    {
        return mc;
    }

    FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }    

	// Init / drawing
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui()
    {    
    	if(this.selectingPresetForDimension)
    	{
    		this.selectingPresetForDimension = false;
    		if(this.creatingNewDimension)
    		{
    			this.creatingNewDimension = false;    			
    			if(this.selectPresetForDimensionMenu.selectedPreset != null) // If user didn't cancel
    			{
	    			// Creating a new dim, create config with the chosen preset
	    	        // If a modpack creator has included a default config for the chosen preset in the overworld's preset, use that, otherwise use the preset's world config.
    				// If world isn't null the we're ingame
	    	        DimensionsConfig defaultConfig = DimensionsConfig.getModPackConfig(this.mc.world != null ? OTG.getDimensionsConfig().Overworld.PresetName : this.previousMenu.selectedPreset.getFirst());
	    	        if(defaultConfig != null)
	    	        {
	    	        	for(DimensionConfig dimConfig : defaultConfig.Dimensions)
	    	        	{
	    	        		if(dimConfig.PresetName.equals(this.selectPresetForDimensionMenu.selectedPreset.getFirst()))
    	        			{
	    	        			DimensionConfig newConfig = dimConfig.clone();
	    	        			// If world is not null then were ingame and we're creating a config for which a world will be created when clicking continue/apply
	    	        			newConfig.isNewConfig = this.mc.world != null;
	    	        			this.dimensions.add(newConfig);	    	        			
	    	        			break;
	    	        		}
	    	        	}
	    	        } else {
	    	        	// Add only the overworld for the chosen preset, don't add dimensions.
	    	        	DimensionConfig newConfig = new DimensionConfig(this.selectPresetForDimensionMenu.selectedPreset.getSecond());
	        			// If world is nnot null then were ingame and we're creating a config for which a world will be created when clicking continue/apply
	        			newConfig.isNewConfig = this.mc.world != null;
	    		        this.dimensions.add(newConfig);
	    	        }
	    			this.selectedDimension = this.dimensions.get(this.dimensions.size() - 1);
	    			this.dimensionsList.selectedIndex = this.dimensions.size() - 1;
	    			this.dimensionsList.lastClickTime = System.currentTimeMillis();
	    			this.selectDimensionIndex(this.dimensionsList.selectedIndex);
    	        	this.compareSettingsToOriginal();
    			}
    		} else {
	    		if(this.selectPresetForDimensionMenu.selectedPreset != null) // If user didn't cancel
	    		{
	    			this.selectedDimension.PresetName = this.selectPresetForDimensionMenu.selectedPreset.getFirst();
	    		}
    		}
    	}

        this.buttonList.add(new GuiButton(iNewButton, OTGGuiDimensionList.this.leftMargin, this.height - btnBottomMargin, listWidth, 20, "Add"));
        this.btnDelete = new GuiButton(iDeleteButton, OTGGuiDimensionList.this.leftMargin, this.height - (btnBottomMargin - 24), listWidth, 20, "Delete");
        this.buttonList.add(btnDelete);
        
        boolean isNewDim = this.selectedDimension.isNewConfig; // This dimension config was created via the ingame menu, but the dimension itself hasn't been created yet (is done when pressing continue/apply)
        boolean isLoaded = false;
        if(!isNewDim)
        {
            // TODO: Don't do this each draw ><
            // If we're ingame then find out if the dimension is loaded, getWorld only fetches loaded worlds.
            // If world isn't null then we're ingame
        	if(this.mc.isSingleplayer())
        	{            		
	            ForgeWorld forgeWorld = this.mc.world != null && this.selectedDimensionIndex != 0 ? (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(this.selectedDimension.PresetName) : null;
	            isLoaded = this.selectedDimensionIndex == 0 || forgeWorld != null;
        	} else {
        		// For MP get the loaded status from the ForgeWorld, set by a packet from the server.
	            ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.selectedDimension.PresetName);
	            isLoaded = this.selectedDimensionIndex == 0 || (forgeWorld != null && forgeWorld.isLoadedOnServer); // ForgeWorld can be null after sending a create world packet from the client
        	}
        }
		this.btnDelete.enabled = this.selectedDimensionIndex != 0 && (isNewDim || !isLoaded); // Overworld and unloaded dims can't be deleted
                
        int maxBtnWidth = 330; // Buttons show visual artifacts when they get too wide        
        int btnWidth = OTGGuiDimensionList.this.width - OTGGuiDimensionList.this.listWidth - OTGGuiDimensionList.this.margin - OTGGuiDimensionList.this.rightMargin;
        btnWidth = btnWidth > maxBtnWidth ? maxBtnWidth : btnWidth; 
        this.btnContinue = new GuiButton(iContinueButton, OTGGuiDimensionList.this.listWidth + margin, this.height - btnBottomMargin, btnWidth, 20, "Continue"); 
        this.buttonList.add(this.btnContinue);        
        this.btnCancel = new GuiButton(iCancelButton, OTGGuiDimensionList.this.listWidth + margin, this.height - (btnBottomMargin - 24), btnWidth, 20, "Cancel");
        this.buttonList.add(this.btnCancel);
         
        // If a world is loaded then this is in-game
        if(this.mc.world != null)
        {
        	boolean bFound = false;
        	for(DimensionConfig dimConfig : this.dimensions)
        	{
        		if(dimConfig.isNewConfig)
        		{
        			bFound = true;
        		}
        	}
        	if(bFound || !this.mc.isSingleplayer())
        	{
	        	this.btnContinue.displayString = "Apply";
	        	this.btnCancel.displayString = "Cancel";
	        	this.btnContinue.enabled = this.mc.isSingleplayer() ? true : this.settingsChanged;
        	} else {
	        	this.btnContinue.displayString = "";
	        	this.btnCancel.displayString = "Back";
	        	this.btnContinue.enabled = false;
        	}
    		
        	
        }
        
        if(this.dimensionSettingsList == null)
        {    	
	    	this.dimensionSettingsList = new OTGGuiDimensionSettingsList(this, OTGGuiDimensionList.this.topMargin, OTGGuiDimensionList.this.height - OTGGuiDimensionList.this.bottomMargin, OTGGuiDimensionList.this.listWidth + margin, OTGGuiDimensionList.this.width - OTGGuiDimensionList.this.listWidth - OTGGuiDimensionList.this.margin - OTGGuiDimensionList.this.rightMargin, this.mc);
	        this.dimensionsList = new OTGGuiSlotDimensionList(this, this, dimensions);
        } else {
        	this.dimensionSettingsList.resize(OTGGuiDimensionList.this.topMargin, OTGGuiDimensionList.this.height - OTGGuiDimensionList.this.bottomMargin, OTGGuiDimensionList.this.listWidth + margin, OTGGuiDimensionList.this.width - OTGGuiDimensionList.this.listWidth - OTGGuiDimensionList.this.margin - OTGGuiDimensionList.this.rightMargin);
        	this.dimensionsList.resize();
        }
        updateCache();
    }
	
    public int drawLine(String line, int offset, int shifty)
    {
        this.fontRenderer.drawString(line, offset, shifty, 0xd7edea);
        return shifty + 10;
    }    
	
    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
    	if(this.dimensionsList == null)
    	{
    		return;
    	}
    	
    	if(System.currentTimeMillis() - lastPregeneratorCheckTime > 1000l)
    	{
    		lastPregeneratorCheckTime = System.currentTimeMillis();

    		for(LocalWorld forgeWorld : OTG.getEngine().getAllWorlds())
    		{
    			if(
					(this.selectedDimensionIndex == 0 && ((ForgeWorld)forgeWorld).getName().equals("overworld")) || 
					((ForgeWorld)forgeWorld).getName().equals(this.selectedDimension.PresetName)
				)
    			{    	
    				// ForgeWorldSession can be null for MP clients that have not received the world data for a new world yet
    				if(forgeWorld.getWorldSession() != null)
    				{
	    				Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.getWorldSession()).getPregenerator();   				
	    				if(pregenerator.getPregeneratorIsRunning() && pregenerator.preGeneratorProgressStatus != "Done")
		    			{
		    				if(!this.dimensionSettingsList.showingPregeneratorStatus)
		    				{
		    					this.dimensionSettingsList.refreshData();
		    				}
		    			} else {
		    				if(this.dimensionSettingsList.showingPregeneratorStatus)
		    				{
		    					this.dimensionSettingsList.refreshData();
		    				}	    				
		    			}
    				}
    			}
    		}
    	}
    	
        this.dimensionsList.drawScreen(mouseX, mouseY, partialTicks, this.zLevel);
        this.dimensionSettingsList.drawScreen(mouseX, mouseY, partialTicks);            
        
        // If world isnt null then were ingame
        this.drawCenteredString(this.fontRenderer, this.mc.world == null ? "Create dimensions" : "Manage dimensions", this.width / 2, 16, 0xFFFFFF);
        
        this.wikiBtnWidth = this.fontRenderer.getStringWidth("Wiki");
        this.wikiBtnHeight = 6; // TODO: Measure string height        
        this.wikiBtnLeft = this.width - (this.rightMargin + this.fontRenderer.getStringWidth("Wiki")) - 2;
        this.wikiBtnRight = this.wikiBtnLeft + this.wikiBtnWidth;
        this.wikiBtnTop = 16;
        this.wikiBtnBottom = this.wikiBtnTop + this.wikiBtnHeight;
        
        this.drawString(this.fontRenderer, TextFormatting.UNDERLINE + "Wiki", wikiBtnLeft, wikiBtnTop, 0x5555FF);

        super.drawScreen(mouseX, mouseY, partialTicks);
        if(this.mc.world != null && !this.mc.isSingleplayer() && this.btnContinue.enabled != this.settingsChanged)
        {
        	this.btnContinue.enabled = this.settingsChanged;
        }
    }
    
	// Mouse/Keyboard
	
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
        	// Make sure all settings are applied
        	this.mouseClicked(0, 0, 0);
        	
            switch (button.id)
            {            
                case iContinueButton:
                {                	
                	// If world is not null then we're ingame
                	if(this.mc.world == null)
                	{
                		this.mc.displayGuiScreen(new OTGGuiEnterWorldName(this, this.dimensions.get(0).PresetName));
                	} else {
                		        
                        if(this.mc.isSingleplayer())
                        {
	                		// Apply game rules and save
	                		applyGameRules();                		
	                			                		
	                		// Create worlds for any newly created dims                			
                			for(DimensionConfig dimConfig : this.dimensions)
                			{
                				if(dimConfig.isNewConfig)
                				{
                					dimConfig.isNewConfig = false;
                			        OTG.IsNewWorldBeingCreated = true;
                        			OTGDimensionManager.createNewDimensionSP(dimConfig, this.mc.getIntegratedServer());
                        			OTG.IsNewWorldBeingCreated = false;
                				}
                			}
                			this.dimensionSettingsList.refreshData();
                			btnContinue.enabled = false;
                        } else {
                        	
                        	ArrayList<DimensionConfig> applyDimSettings = (ArrayList<DimensionConfig>) this.dimensions.clone();
	                		// Create worlds for any newly created dims
                        	ArrayList<DimensionConfig> dimensionConfigsToUpdate = new ArrayList<DimensionConfig>();
                        	boolean isOverWorldIncluded = false;
                			for(int i = 0; i < applyDimSettings.size(); i++)
                			{
                				DimensionConfig dimConfig = applyDimSettings.get(i);
                				if(dimConfig.isNewConfig)
                				{
                					dimConfig.isNewConfig = false;
                					ClientPacketManager.sendCreateDimensionPacket(dimConfig);
                        			this.dimensionSettingsList.refreshData(true, false, false);
                				} else {
                					// If the config has been edited send the changes to the server
                					for(DimensionConfig originalDimConfig : this.originalDimensions)
                					{
                						if(originalDimConfig.PresetName == null || originalDimConfig.PresetName.equals(dimConfig.PresetName))
                						{
                							if(!originalDimConfig.toYamlString().equals(dimConfig.toYamlString()))
                							{
                								if(i == 0)
                								{
                									isOverWorldIncluded = true;
                								}
                								dimensionConfigsToUpdate.add(dimConfig);
                							}
                							break;
                						}
                					}
                				}
                			}
                			if(dimensionConfigsToUpdate.size() > 0)
                			{
								// Send a packet with changes
								ClientPacketManager.sendUpdateDimensionSettingsPacket(dimensionConfigsToUpdate, isOverWorldIncluded);
                			}
                			
                			this.settingsChanged = false;
                        }                		
                		btnContinue.displayString = "";
                	}
                    return;
                }
                case iCancelButton:
                {
                	if(this.dimensionSettingsList.advancedSettingsMenu || this.dimensionSettingsList.gameRulesMenu)
                	{
                		this.dimensionSettingsList.refreshData(true, false, false);
                		this.dimensionSettingsList.scrollBy(Int.MinValue());
                	} else {
                		
                		if(this.mc.world != null) // If world is not null then we're ingame
                		{
	                		// Apply game rules and save
	                		applyGameRules();
	                		
	            			// Cancelling, remove any newly created dims, don't create worlds for them.
	                		DimensionsConfig dimsConfig = OTG.getDimensionsConfig();                	
	            			ArrayList<DimensionConfig> dims = new ArrayList<DimensionConfig>();
	            			for(DimensionConfig dimConfig : dimsConfig.Dimensions)
	            			{
	            				if(!dimConfig.isNewConfig)
	            				{
	            					dims.add(dimConfig);
	            				}
	            			}
	            			dimsConfig.Dimensions = dims;
	            			OTG.getDimensionsConfig().save();
                		}
                		this.mc.displayGuiScreen(this.previousMenu);
                	}
                    return;
                }
                case iNewButton:
                {
                	this.creatingNewDimension = true;
	                this.selectingPresetForDimension = true;
	                this.selectPresetForDimensionMenu = new OTGGuiPresetList(this, true);
	                this.mc.displayGuiScreen(this.selectPresetForDimensionMenu);           	                	
                    return;
                }
                case iDeleteButton:
                {
                    if(
                		this.dimensionsList.selectedIndex == 0 || // Can't delete overworld
                		this.dimensionsList.selectedIndex == -1
            		)
                    {
                    	return;
                    }
        			
            		// If world is null then we're not ingame
                    if(this.mc.world == null || this.mc.isSingleplayer())
                    {
	                    boolean bSuccess = true;
	        			// If world is not null then we're ingame
	        			if(!this.selectedDimension.isNewConfig && this.mc.world != null) 
	        			{
	        				bSuccess = OTGDimensionManager.DeleteDimensionServer(this.selectedDimension.PresetName, this.mc.getIntegratedServer());
	        			}
	        			if(bSuccess)
	        			{
		                	this.dimensions.remove(this.selectedDimension);
		                    if(this.dimensionsList.selectedIndex >= this.dimensionsList.getSize())
		                    {
		    	    			this.dimensionsList.selectedIndex = this.dimensionsList.getSize() - 1;
		                    }
		        			this.dimensionsList.lastClickTime = System.currentTimeMillis();
		        			this.selectDimensionIndex(this.dimensionsList.selectedIndex);
	        			}
                    } else {
                    	if(this.selectedDimension.isNewConfig)
                    	{
		                	this.dimensions.remove(this.selectedDimension);
		                    if(this.dimensionsList.selectedIndex >= this.dimensionsList.getSize())
		                    {
		    	    			this.dimensionsList.selectedIndex = this.dimensionsList.getSize() - 1;
		                    }
		        			this.dimensionsList.lastClickTime = System.currentTimeMillis();
		        			this.selectDimensionIndex(this.dimensionsList.selectedIndex);
                    	} else {
                    		ClientPacketManager.sendDeleteDimensionPacket(this.selectedDimension.PresetName);
                    	}
                    }
                    return;
                }
            }
            return;
        }
        super.actionPerformed(button);
    }    
    
	@Override
    public void confirmClicked(boolean ok, int worldId)
    {
		if(!ShowingOpenLinkDialogue && ok)
		{
            long i = (new Random()).nextLong();
            String s = this.dimensions.get(0).Seed;

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

            WorldType.parseWorldType("OTG").onGUICreateWorldPress();

            GameType gametype = this.dimensions.get(0).GameType.equals("Hardcore") ? GameType.SURVIVAL : GameType.getByName(this.dimensions.get(0).GameType.toLowerCase());
            WorldSettings worldsettings = new WorldSettings(i, gametype, true, this.dimensions.get(0).GameType.equals("Hardcore"), WorldType.parseWorldType("OTG"));
            worldsettings.setGeneratorOptions("OpenTerrainGenerator");

            if(this.dimensions.get(0).BonusChest)
            {
            	worldsettings.enableBonusChest();
            }
            if(this.dimensions.get(0).AllowCheats)
            {
            	worldsettings.enableCommands();
            }

			// Don't overwrite existing worlds, UI shouldn't allow it anyway
            if (this.mc.getSaveLoader().getWorldInfo(worldName) == null)
            {    				
        		DimensionsConfig forgeWorldConfig = new DimensionsConfig(new File(this.mc.mcDataDir.getAbsolutePath() + "\\saves\\"), this.worldName);
        		forgeWorldConfig.WorldName = this.worldName;
        		forgeWorldConfig.Overworld = this.dimensions.get(0).clone();
        		forgeWorldConfig.Dimensions = new ArrayList<DimensionConfig>();
        		for(int j = 1; j < this.dimensions.size(); j++)
        		{
        			forgeWorldConfig.Dimensions.add(this.dimensions.get(j).clone());
        		}
        		OTG.setDimensionsConfig(forgeWorldConfig);            		
        	
                ISaveFormat isaveformat = this.mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(this.worldName);
                
                OTG.getDimensionsConfig().save();
				
				OTG.IsNewWorldBeingCreated = true;
				this.mc.launchIntegratedServer(this.worldName, this.worldName, worldsettings);
		        OTG.IsNewWorldBeingCreated = false;
            }
		}
		if(ShowingOpenLinkDialogue && ok)
		{
	        try {
				this.openWebLink(new URI("http://openterraingen.wikia.com/wiki/In-game_tools_and_console_commands"));				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        Minecraft.getMinecraft().displayGuiScreen(this);
	        return;
		}
		if(ShowingOpenLinkDialogue && !ok)
		{
			Minecraft.getMinecraft().displayGuiScreen(this);
		}
		
		ShowingOpenLinkDialogue = false;
		
		super.confirmClicked(ok, worldId);
    }    
	
    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        this.dimensionsList.handleMouseInput(mouseX, mouseY);
        
        this.dimensionSettingsList.handleMouseInput();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
    	if(wikiLinkClicked(mouseX, mouseY))
    	{
	    	ShowingOpenLinkDialogue = true;
			GuiConfirmOpenLink gui = new GuiConfirmOpenLink(this, "http://openterraingen.wikia.com/wiki/In-game_tools_and_console_commands", 0, true);
			gui.disableSecurityWarning();
			mc.displayGuiScreen(gui);
			return;
    	}
    	
        if (this.buttonId != null)
        {
            this.buttonId = null;
        }
        if (mouseButton != 0 || !this.dimensionSettingsList.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private boolean wikiLinkClicked(int mouseX, int mouseY)
    {
    	return mouseX >= this.wikiBtnLeft && mouseX <= this.wikiBtnRight && mouseY >= this.wikiBtnTop && mouseY <= this.wikiBtnBottom;
	}

	/**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state != 0 || !this.dimensionSettingsList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    	if(keyCode == 1) // Escape
    	{
    		actionPerformed(btnCancel);
    	}
    	this.dimensionSettingsList.keyTyped(typedChar, keyCode);
    	if(keyCode == 28 || keyCode == 83) // 28 + 83 is enter
    	{
    		this.dimensionSettingsList.refreshData();
    	}
    } 
	    
    // Misc    

    public void compareSettingsToOriginal()
    {  
    	// Only do this for MP server (not SP or main menu)
    	if(this.mc.world != null && !this.mc.isSingleplayer())
    	{
	    	this.settingsChanged = false;
	    	for(DimensionConfig dimConfig : this.dimensions)
	    	{
	    		// PresetName is null for vanilla overworlds
	    		if(dimConfig.PresetName != null && dimConfig.isNewConfig)
	    		{
	    			this.settingsChanged = true;
	    			return;
	    		}
	    	}
	    	// dimensions and originalDimensions lists should be synchronised,
	    	// so originalDimensions should have a clone of the same index in dimensions
	    	// when that config was last applied/saved.
	    	for(int i = 0; i < this.originalDimensions.size(); i++)
	    	{
	    		DimensionConfig originalDimConfig = this.originalDimensions.get(i);
		    	DimensionConfig dimConfig = this.dimensions.get(i);
		        this.settingsChanged = !dimConfig.toYamlString().equals(originalDimConfig.toYamlString());
	    		if(this.settingsChanged)
	    		{
	    			return;
	    		}
	    	}
    	}
    }
    
	private void applyGameRules()
	{
		// If world not is null then we're ingame
		if(this.mc.world != null)
		{
			// Apply game rules to worlds
			DimensionsConfig dimsConfig = OTG.getDimensionsConfig();                			
			ArrayList<LocalWorld> worlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
			for(LocalWorld world : worlds)
			{            					
				if(((ForgeWorld)world).getWorld() != null)
				{
					OTGDimensionManager.ApplyGameRulesToWorld(((ForgeWorld)world).getWorld(), dimsConfig.getDimensionConfig(((ForgeWorld)world).getName()));
				}
			}
			// TODO: Not sending this event atm, when game rules are changed via /gamerule it is sent. 
			// /gamerule only works for the overworld though, so not sure how useful that would be anyway.
			//net.minecraftforge.event.ForgeEventFactory.onGameRuleChange(rules, p_184898_1_, server);	
		}
	}
    
    private void openWebLink(URI url)
    {
        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object)null);
            oclass.getMethod("browse", URI.class).invoke(object, url);
        }
        catch (Throwable throwable1)
        {
            Throwable throwable = throwable1.getCause();
            OTG.log(LogMarker.ERROR, "Couldn't open link: {}", (Object)(throwable == null ? "<UNKNOWN>" : throwable.getMessage()));
        }
    }  
    
    public void selectDimensionIndex(int index)
    {   	    	
    	if(index >= dimensions.size())
    	{
    		return;
    	}
        this.selectedDimensionIndex = index;
        this.selectedDimension = index >= 0 ? dimensions.get(selectedDimensionIndex) : null;
        
        updateCache();
        this.dimensionSettingsList.scrollBy(Int.MinValue());
        
        boolean isNewDim = this.selectedDimension.isNewConfig; // This dimension config was created via the ingame menu, but the dimension itself hasn't been created yet (is done when pressing continue/apply)
        boolean isLoaded = false;
        if(!isNewDim)
        {
            // TODO: Don't do this each draw ><
            // If we're ingame then find out if the dimension is loaded, getWorld only fetches loaded worlds.
            // If world isn't null then we're ingame
        	if(this.mc.isSingleplayer())
        	{            		
	            ForgeWorld forgeWorld = this.mc.world != null && this.selectedDimensionIndex != 0 ? (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(this.selectedDimension.PresetName) : null;
	            isLoaded = this.selectedDimensionIndex == 0 || forgeWorld != null;
        	} else {
        		// For MP get the loaded status from the ForgeWorld, set by a packet from the server.
	            ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.selectedDimension.PresetName);
	            isLoaded = this.selectedDimensionIndex == 0 || (forgeWorld != null && forgeWorld.isLoadedOnServer); // ForgeWorld can be null after sending a create world packet from the client
        	}
        }
		this.btnDelete.enabled = this.selectedDimensionIndex != 0 && (isNewDim || !isLoaded); // Overworld and unloaded dims can't be deleted
    }

    public boolean isDimensionIndexSelected(int index)
    {
        return index == selectedDimensionIndex;
    }
   
    private void updateCache()
    {
    	// RestoreSelection happens when the MP client UI is updated with new data from the server
    	if(this.restoreSelection)
    	{
    		this.restoreSelection = false;
    		this.dimensionsList.selectedIndex = this.selectedDimensionIndex;
    		this.dimensionSettingsList.refreshData(this.previouslySelectedMainMenu, this.previouslySelectedGameRulesMenu, this.previouslySelectedAdvancedSettingsMenu);
    		this.dimensionSettingsList.amountScrolled = this.lastScrollPos;
    	} else {
    		this.dimensionSettingsList.refreshData(true, false, false);
    	}
    }   
}
