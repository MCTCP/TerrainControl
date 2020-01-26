package com.pg85.otg.forge.gui.presets;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Mouse;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.gui.OTGGuiEnterWorldName;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionList;
import com.pg85.otg.forge.gui.dimensions.base.OTGGuiScrollingList;
import com.pg85.otg.forge.gui.mainmenu.OTGGuiWorldSelection;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.util.IOHelper;
import com.pg85.otg.logging.LogMarker;

public class OTGGuiPresetList extends GuiScreen implements GuiYesNoCallback
{
    private static boolean ShowingOpenLinkDialogue = false;
    
    GuiScreen previousMenu;
    private OTGGuiSlotPresetList presetsList;
    private OTGGuiScrollingList presetInfo;
    private int selected = -1;
    public Tuple<String, DimensionConfigGui> selectedPreset;
    private GuiButton btnDelete;
    private GuiButton btnContinue;
    
    int listWidth = 150;
    int topMargin = 37;
    int bottomMargin = 73;
    private int btnBottomMargin = 63;
    int leftMargin = 10;
    int rightMargin = 10;
    int slotHeight = 16;
    int margin = 20;
    
    private final int iContinueButton = 0;
    private final int iCancelButton = 1;
    private final int iNewButton = 2;
    private final int iCloneButton = 4;
    private final int iDeleteButton = 3;
    
	private boolean askDeleteSettings = false;
	private boolean selectingNewPresetName = true;
	private boolean selectingClonePresetName = true;
    public String newPresetName;

    private int wikiBtnLeft;
    private int wikiBtnTop;
    private int wikiBtnWidth;
    private int wikiBtnHeight;
    private int wikiBtnRight;
    private int wikiBtnBottom;
    private boolean selectingPresetForDimension;
    
    public OTGGuiPresetList(GuiScreen previousMenu)
    {
    	this(previousMenu, false);
    }
    
    public OTGGuiPresetList(GuiScreen previousMenu, boolean selectingPresetForDimension)
    {    
    	ShowingOpenLinkDialogue = false;
        this.previousMenu = previousMenu;
        this.selectingPresetForDimension = selectingPresetForDimension;
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    Minecraft getMinecraftInstance()
    {
        return mc;
    }

    FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }
    
    void selectPresetIndex(int index)
    {
    	if(index >= ForgeEngine.Presets.size())
    	{
    		return;
    	}
        this.selected = index;
        Entry<String, DimensionConfigGui> entry = index >= 0 ? new ArrayList<Entry<String, DimensionConfigGui>>(ForgeEngine.Presets.entrySet()).get(selected) : null;
        this.selectedPreset = entry != null ? new Tuple<String, DimensionConfigGui>(entry.getKey(), entry.getValue()) : null;
    	
        updateCache();
    }

    boolean presetIndexSelected(int index)
    {
        return index == selected;
    }

    private void updateCache()
    {
    	this.presetInfo = null;

    	this.btnContinue.enabled = this.selectedPreset != null;
        if (this.selectedPreset == null)
        {
        	this.presetInfo = new OTGGuiScrollingListInfo(this, null, null, null);           	
            return;
        }
        
    	ArrayList<String> presets = new ArrayList<String>(ForgeEngine.Presets.keySet());
    	// When using the O menu ingame, can't delete a preset that's currently in use. 
    	if(this.previousMenu instanceof OTGGuiDimensionList)
    	{
    		boolean bFound = false;
        	for(DimensionConfig dimConfig : ((OTGGuiDimensionList)this.previousMenu).dimensions)
        	{
        		if(
    				(dimConfig.PresetName != null && dimConfig.PresetName.equals(presets.get(this.selected))) ||
    				(dimConfig.PresetName == null && OTG.getDimensionsConfig().WorldName.equals(presets.get(this.selected)))
				)
        		{
        			bFound = true;
        			break;
        		}
        	}
			this.btnContinue.enabled = !bFound;
			this.btnDelete.enabled = !bFound;
    	} else {
        	this.btnContinue.enabled = true;
        	this.btnDelete.enabled = true;
    	}

        // Use OTG's logo as the default logo if there is no worldpacker jar for this world.
        String modId = this.selectedPreset.getSecond().worldPackerModName;
        ModContainer presetWorldPackerMod = null;
        ResourceLocation logoPath = null;
        String description = null;
        String version = null;
        String credits = null;
        String url = null;
        Dimension logoDims = new Dimension(0, 0);
        List<String> lines = new ArrayList<String>();

        if(modId != null)
        {
	        for (ModContainer mod : Loader.instance().getModList())
	        {
	            if (mod.getModId().equals(modId))
	            {
	            	presetWorldPackerMod = mod;
	            	break;
	            }
	        }
        }
        
        if(presetWorldPackerMod == null) // Default to OTG for mod image
        {
        	modId = PluginStandardValues.MOD_ID;
        	presetWorldPackerMod = Loader.instance().activeModContainer();
        }
        
    	if(presetWorldPackerMod != null && presetWorldPackerMod.getMetadata() != null)
    	{
    		if(modId != PluginStandardValues.MOD_ID)
    		{
	    		description = presetWorldPackerMod.getMetadata().description;
	    		version = presetWorldPackerMod.getMetadata().version;
	    		credits = presetWorldPackerMod.getMetadata().credits;
	    		url = presetWorldPackerMod.getMetadata().url;
    		}
    		
    		if(!presetWorldPackerMod.getMetadata().logoFile.isEmpty())
    		{
	            TextureManager tm = mc.getTextureManager();
	            IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(modId);
	            try
	            {
	                BufferedImage logo = null;
	                if (pack != null)
	                {
	                    logo = pack.getPackImage();
	                } else {
	                    InputStream logoResource = getClass().getResourceAsStream(presetWorldPackerMod.getMetadata().logoFile);
	                    if (logoResource != null)
	                        logo = ImageIO.read(logoResource);
	                }
	                if (logo != null)
	                {
	                    logoPath = tm.getDynamicTextureLocation("modlogo", new DynamicTexture(logo));
	                    logoDims = new Dimension(logo.getWidth(), logo.getHeight());
	                }
	            }
	            catch (IOException e) { }
    		}
    	}

        lines.add("Name: " + this.selectedPreset.getFirst());
        lines.add("Version: " + (version != null ? version : "Unknown"));
        lines.add("Credits: " + (credits != null ? credits : this.selectedPreset.getSecond().author));
        if(url != null)
        {
        	lines.add("URL: " + url);
        }
        if(this.selectedPreset.getSecond().dimensions.size() > 0)
        {
        	String dimsString = "";
        	for(int i = 0; i < this.selectedPreset.getSecond().dimensions.size(); i++)
        	{
        		String dimName = this.selectedPreset.getSecond().dimensions.get(i);
        		boolean bFound = ForgeEngine.Presets.containsKey(dimName);
        		if(bFound)
        		{
        			dimsString += dimName + (i == this.selectedPreset.getSecond().dimensions.size() - 1 ? "" : ", ");
        		} else {
        			dimsString += TextFormatting.GRAY + dimName + " (Not installed)" + TextFormatting.RESET + ", ";
        		}
        	}
        	
        	lines.add("Dimensions: " + dimsString);
        }
        lines.add("");
        lines.add((description != null ? description : this.selectedPreset.getSecond().description));
        lines.add("");

        presetInfo = new OTGGuiScrollingListInfo(this, lines, logoPath, logoDims);       
    }
    
    private GuiYesNo askDeleteSettings(GuiYesNoCallback p_152129_0_, String worldName)
    {
    	selectingNewPresetName = false;
    	selectingClonePresetName = false;
    	askDeleteSettings = true;    	

        String s1 = "Delete the OpenTerrainGenerator preset for '" + worldName + "'?";
        String s2 = "";
        String s3 = "Delete";
        String s4 = "Cancel";
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }
    
    // Init / drawing
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui()
    {    
    	// For MP the server sends the presets
    	if(this.mc.world == null || Minecraft.getMinecraft().isSingleplayer())
    	{    	
    		ForgeEngine.loadPresets(); // Worldpacker may not have be done unpacking presets on app start, so fetch world configs here (also done when joining world via the world selection screen)
    	}
        if(ForgeEngine.Presets.size() > 0)
        {
        	ArrayList<String> presets = new ArrayList<String>(ForgeEngine.Presets.keySet());
        	int selectPreset = 0;
        	
            this.selected = selectPreset;
            if(selectPreset > -1)
            {
	            Entry<String, DimensionConfigGui> entry = new ArrayList<Entry<String, DimensionConfigGui>>(ForgeEngine.Presets.entrySet()).get(selectPreset);
	            this.selectedPreset = new Tuple<String, DimensionConfigGui>(entry.getKey(), entry.getValue());
            } else {
            	this.selectedPreset = null;
            }
        }
    	
    	if(this.presetsList == null)
    	{
	        this.presetsList = new OTGGuiSlotPresetList(this, this);    	
	        this.presetInfo = new OTGGuiScrollingListInfo(this, null, null, null);
    	} else {
    		this.presetsList.resize();
    	}
    	GuiButton btnNew = new GuiButton(iNewButton, OTGGuiPresetList.this.leftMargin, this.height - btnBottomMargin, (listWidth - 3) / 2 + 1, 20, "New");
    	btnNew.enabled = this.mc.world == null || this.mc.isSingleplayer(); // Don't allow creating/cloning/deleting presets for MP    	
    	this.buttonList.add(btnNew);
    	
    	GuiButton btnClone = new GuiButton(iCloneButton, OTGGuiPresetList.this.leftMargin + (listWidth - 3) / 2 + 3 + 2, this.height - btnBottomMargin, (listWidth - 3) / 2, 20, "Clone");
    	btnClone.enabled = this.mc.world == null || this.mc.isSingleplayer(); // Don't allow creating/deleting presets for MP    	
    	this.buttonList.add(btnClone);
    	
    	btnDelete = new GuiButton(iDeleteButton, OTGGuiPresetList.this.leftMargin, this.height - (btnBottomMargin - 24), listWidth, 20, "Delete");
    	btnDelete.enabled = this.mc.world == null || this.mc.isSingleplayer(); // Don't allow creating/cloning/deleting presets for MP
        this.buttonList.add(btnDelete);

        int maxBtnWidth = 330; // Buttons show visual artifacts when they get too wide        
        int btnWidth = OTGGuiPresetList.this.width - OTGGuiPresetList.this.listWidth - OTGGuiPresetList.this.margin - OTGGuiPresetList.this.rightMargin;
        btnWidth = btnWidth > maxBtnWidth ? maxBtnWidth : btnWidth; 
        
        btnContinue = new GuiButton(iContinueButton, OTGGuiPresetList.this.listWidth + margin, this.height - btnBottomMargin, btnWidth, 20, "Continue");
        this.buttonList.add(btnContinue);
        this.buttonList.add(new GuiButton(iCancelButton, OTGGuiPresetList.this.listWidth + margin, this.height - (btnBottomMargin - 24), btnWidth, 20, "Cancel"));

        updateCache();
    }
           
    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.presetsList.drawScreen(mouseX, mouseY, partialTicks, this.zLevel);
        if(this.presetInfo != null)
    	{
        	this.presetInfo.drawScreen(mouseX, mouseY, partialTicks, this.zLevel);
        }

        this.drawCenteredString(this.fontRenderer, "Select a preset", this.width / 2, 16, 0xFFFFFF);

        this.wikiBtnWidth = this.fontRenderer.getStringWidth("Wiki");
        this.wikiBtnHeight = 6; // TODO: Measure string height        
        this.wikiBtnLeft = this.width - (this.rightMargin + this.fontRenderer.getStringWidth("Wiki")) - 2;
        this.wikiBtnRight = this.wikiBtnLeft + this.wikiBtnWidth;
        this.wikiBtnTop = 16;
        this.wikiBtnBottom = this.wikiBtnTop + this.wikiBtnHeight;
        
        this.drawString(this.fontRenderer, TextFormatting.UNDERLINE + "Wiki", wikiBtnLeft, wikiBtnTop, 0x5555FF);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public int drawLine(String line, int offset, int shifty)
    {
        this.fontRenderer.drawString(line, offset, shifty, 0xd7edea);
        return shifty + 10;
    }

    // Mouse / keyboard
    
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {            
                case iContinueButton:
                {
                	if(previousMenu instanceof OTGGuiDimensionList)
                	{
                		this.mc.displayGuiScreen(this.previousMenu);	
                	} else {
                		if(this.selectingPresetForDimension)
                		{
                			this.selectingPresetForDimension = false;
                			this.mc.displayGuiScreen(this.previousMenu);
                		} else {
                			this.mc.displayGuiScreen(new OTGGuiDimensionList(this));
                		}
                	}
                    return;
                }
                case iCancelButton:
                {
                	this.selectedPreset = null;
                	if(this.selectingPresetForDimension)
                	{
                		this.selectingPresetForDimension = false;
                		this.mc.displayGuiScreen(this.previousMenu);
                	} else {
                		this.mc.displayGuiScreen(new OTGGuiWorldSelection());
                	}
                    return;
                }
                case iNewButton:
                {
                	askDeleteSettings = false;
                	selectingClonePresetName = false;
                	selectingNewPresetName = true;
    				this.mc.displayGuiScreen(new OTGGuiEnterWorldName(this, "New World"));
                    return;
                }
                case iCloneButton:
                {
                	if(this.selectedPreset != null)
                	{
	                	askDeleteSettings = false;
	                	selectingNewPresetName = false;
	                	selectingClonePresetName = true;
	    				this.mc.displayGuiScreen(new OTGGuiEnterWorldName(this, selectedPreset.getFirst()));
	                    return;
                	}
                }                
                case iDeleteButton:
                {
                	if(this.selectedPreset != null)
                	{
                		this.mc.displayGuiScreen(askDeleteSettings(this, selectedPreset.getFirst()));
                	}
                    return;
                }
            }
        }
        super.actionPerformed(button);
    }
    
	@Override
    public void confirmClicked(boolean ok, int worldId)
    {
		if(askDeleteSettings)
		{
			if(ok)
			{
        		// If world is null then we're not ingame
		    	if(this.mc.world == null || Minecraft.getMinecraft().isSingleplayer())
		    	{
					String presetNameToDelete = selectedPreset.getFirst();
					
					GuiYesNo guiyesno = askDeleteSettings(this, presetNameToDelete);
					this.mc.displayGuiScreen(guiyesno);
	
		            File OTGWorldsDirectory = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + File.separator + PluginStandardValues.PresetsDirectoryName);
		            if(OTGWorldsDirectory.exists() && OTGWorldsDirectory.isDirectory())
		            {
		            	for(File worldDir : OTGWorldsDirectory.listFiles())
		            	{
		            		if(worldDir.isDirectory() && worldDir.getName().equals(presetNameToDelete))
		            		{
	            				IOHelper.deleteRecursive(worldDir);
		            			break;
		            		}
		            	}
		        	}
	
	                ForgeEngine.loadPresets();
	
	                if(this.presetsList.getSize() == 0)
	                {
		    			this.presetsList.selectedIndex = -1;
	                }
	                else if(this.presetsList.selectedIndex > this.presetsList.getSize() - 1)
	                {
		    			this.presetsList.selectedIndex = this.presetsList.getSize() - 1;
	                }
	    			this.presetsList.lastClickTime = System.currentTimeMillis();
	    			this.selectPresetIndex(this.presetsList.selectedIndex);
		    	}
			}
			this.mc.displayGuiScreen(this);
		}
		else if(selectingNewPresetName)
		{
			if(ok)
			{
				((ForgeEngine)OTG.getEngine()).getWorldLoader().createDefaultOTGWorld(this.newPresetName);
				ForgeEngine.loadPresets();
			}
		}
		else if(selectingClonePresetName)
		{
			if(ok)
			{
				// Clone preset
				// Copy world settings

				String presetNameToClone = selectedPreset.getFirst();
				
	            File sourceDir = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + "/worlds/" +  presetNameToClone);
				File destDir = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + "/worlds/" +  this.newPresetName);
				try {
					FileUtils.copyDirectory(sourceDir, destDir);
				} catch (IOException e) {
					e.printStackTrace();
	        		this.mc.displayGuiScreen(new GuiErrorScreen("Error", "Could not copy directory \"" + presetNameToClone + "\", files may be in use."));
	        		
	        		askDeleteSettings = false;
	        		selectingNewPresetName = false;
	        		selectingClonePresetName = false;
	        		ShowingOpenLinkDialogue = false;
	        		        		
	        		return;
				}
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
	        
			askDeleteSettings = false;
			selectingNewPresetName = false;
			selectingClonePresetName = false;
			ShowingOpenLinkDialogue = false;
			 
			return;
		}
		if(ShowingOpenLinkDialogue && !ok)
		{
			Minecraft.getMinecraft().displayGuiScreen(this);
		}
				
		askDeleteSettings = false;
		selectingNewPresetName = false;
		selectingClonePresetName = false;
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
        if (this.presetInfo != null)
        {
            this.presetInfo.handleMouseInput(mouseX, mouseY);
        }
        this.presetsList.handleMouseInput(mouseX, mouseY);
    }
    
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
    	super.mouseClicked(mouseX, mouseY, mouseButton);
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
    
    private boolean wikiLinkClicked(int mouseX, int mouseY)
    {
    	return mouseX >= this.wikiBtnLeft && mouseX <= this.wikiBtnRight && mouseY >= this.wikiBtnTop && mouseY <= this.wikiBtnBottom;
	}
}
