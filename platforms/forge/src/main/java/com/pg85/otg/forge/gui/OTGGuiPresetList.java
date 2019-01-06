package com.pg85.otg.forge.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.lwjgl.input.Mouse;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.util.IOHelper;

import org.lwjgl.opengl.GL11;

public class OTGGuiPresetList extends GuiScreen implements GuiYesNoCallback
{
    private GuiScreen previousMenu;
    private OTGGuiSlotPresetList presetsList;
    private OTGGuiScrollingList presetInfo;
    private int selected = -1;
    public Tuple<String, DimensionConfigGui> selectedPreset;
    private GuiButton btnContinue;
    
    private int listWidth = 150;
    private int topMargin = 37;
    private int bottomMargin = 73;
    private int btnBottomMargin = 63;
    private int leftMargin = 10;
    private int rightMargin = 10;
    private int slotHeight = 16;
    private int margin = 20;
    
    final int iContinueButton = 0;
    final int iCancelButton = 1;
    final int iNewButton = 2;
    final int iDeleteButton = 3;
    
    public OTGGuiPresetList(GuiScreen previousMenu)
    {
    	this(previousMenu, false);
    }
    
    private boolean selectingPresetForDimension;
    public OTGGuiPresetList(GuiScreen previousMenu, boolean selectingPresetForDimension)
    {    
        this.previousMenu = previousMenu;
        this.selectingPresetForDimension = selectingPresetForDimension;
    }

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
        if(ForgeEngine.presets.size() > 0)
        {
        	ArrayList<String> presets = new ArrayList<String>(ForgeEngine.presets.keySet());
        	int selectPreset = -1;
        	// Can't add a preset multiple times for the same world, make name gray and unselectable if it's already present in the world.
        	if(this.previousMenu instanceof OTGGuiDimensionList)
        	{
        		for(int i = 0; i < presets.size(); i++)
        		{
        			boolean bFound = false;
		        	for(DimensionConfig dimConfig : ((OTGGuiDimensionList)this.previousMenu).dimensions)
		        	{
		        		if(dimConfig.PresetName != null && dimConfig.PresetName.equals(presets.get(i)))
		        		{
		        			bFound = true;
		        			break;
		        		}
		        	}
		        	if(!bFound)
		        	{
		        		selectPreset = i;
		        		break;
		        	}
        		}
        	} else {
        		selectPreset = 0;
        	}
        	
            this.selected = selectPreset;
            if(selectPreset > -1)
            {
	            Entry<String, DimensionConfigGui> entry = new ArrayList<Entry<String, DimensionConfigGui>>(ForgeEngine.presets.entrySet()).get(selectPreset);
	            this.selectedPreset = new Tuple<String, DimensionConfigGui>(entry.getKey(), entry.getValue());
            } else {
            	this.selectedPreset = null;
            }
        }
    	
    	if(this.presetsList == null)
    	{
	        this.presetsList = new OTGGuiSlotPresetList(this);    	
	        this.presetInfo = new Info(null, null, null);
    	} else {
    		this.presetsList.Resize();
    	}
    	GuiButton btnNew = new GuiButton(iNewButton, OTGGuiPresetList.this.leftMargin, this.height - btnBottomMargin, listWidth, 20, "New");
    	btnNew.enabled = this.mc.world == null || this.mc.isSingleplayer(); // Don't allow creating/deleting presets for MP    	
    	this.buttonList.add(btnNew);
    	GuiButton btnDelete = new GuiButton(iDeleteButton, OTGGuiPresetList.this.leftMargin, this.height - (btnBottomMargin - 24), listWidth, 20, "Delete");
    	btnDelete.enabled = this.mc.world == null || this.mc.isSingleplayer(); // Don't allow creating/deleting presets for MP
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
                		this.mc.displayGuiScreen(new OTGGuiWorldSelection(this));
                	}
                    return;
                }
                case iNewButton:
                {
                	askDeleteSettings = false;
                	selectingPresetName = true;
    				this.mc.displayGuiScreen(new OTGGuiEnterWorldName(this, "New World"));
                    return;
                }
                case iDeleteButton:
                {
                	if(selectedPreset != null)
                	{
                		this.mc.displayGuiScreen(askDeleteSettings(this, selectedPreset.getFirst()));
                	}
                    return;
                }
            }
        }
        super.actionPerformed(button);
    }
    
	boolean askDeleteSettings = false;
	boolean selectingPresetName = true;
	
    public GuiYesNo askDeleteSettings(GuiYesNoCallback p_152129_0_, String worldName)
    {
    	selectingPresetName = false;
    	askDeleteSettings = true;    	

        String s1 = "Delete the OpenTerrainGenerator preset for '" + worldName + "'?";
        String s2 = "";
        String s3 = "Delete";
        String s4 = "Cancel";
        GuiYesNo guiyesno = new GuiYesNo(p_152129_0_, s1, s2, s3, s4, 0);
        return guiyesno;
    }

    public String newPresetName;
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
	
		            File OTGWorldsDirectory = new File(OTG.getEngine().getOTGDataFolder().getAbsolutePath() + "/" + PluginStandardValues.PresetsDirectoryName);
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
		else if(selectingPresetName)
		{
			if(ok)
			{
				((ForgeEngine)OTG.getEngine()).CreateDefaultOTGWorld(this.newPresetName);
				ForgeEngine.loadPresets();
			}
		}
				
		askDeleteSettings = false;
		selectingPresetName = false;
		
		super.confirmClicked(ok, worldId);
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
        this.presetsList.drawScreen(mouseX, mouseY, partialTicks);
        if(this.presetInfo != null)
    	{
        	this.presetInfo.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.drawCenteredString(this.fontRenderer, "Select a preset", this.width / 2, 16, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
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

    Minecraft getMinecraftInstance()
    {
        return mc;
    }

    FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }

    public void selectPresetIndex(int index)
    {
        this.selected = index;
        Entry<String, DimensionConfigGui> entry = (index >= 0 && index <= ForgeEngine.presets.size()) ? new ArrayList<Entry<String, DimensionConfigGui>>(ForgeEngine.presets.entrySet()).get(selected) : null;
        this.selectedPreset = entry != null ? new Tuple<String, DimensionConfigGui>(entry.getKey(), entry.getValue()) : null;

        updateCache();
    }

    public boolean presetIndexSelected(int index)
    {
        return index == selected;
    }

    private void updateCache()
    {
    	this.presetInfo = null;

    	this.btnContinue.enabled = this.selectedPreset != null;
        if (this.selectedPreset == null)
        {
        	this.presetInfo = new Info(null, null, null);           	
            return;
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
        	modId = OTGPlugin.MOD_ID;
        	presetWorldPackerMod = Loader.instance().activeModContainer();
        }
        
    	if(presetWorldPackerMod != null && presetWorldPackerMod.getMetadata() != null)
    	{
    		if(modId != OTGPlugin.MOD_ID)
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
	                }
	                else
	                {
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
        if(this.selectedPreset.getSecond().Dimensions.size() > 0)
        {
        	String dimsString = "";
        	for(int i = 0; i < this.selectedPreset.getSecond().Dimensions.size(); i++)
        	{
        		String dimName = this.selectedPreset.getSecond().Dimensions.get(i);
        		boolean bFound = ForgeEngine.presets.containsKey(dimName);
        		if(bFound)
        		{
        			dimsString += dimName + (i == this.selectedPreset.getSecond().Dimensions.size() - 1 ? "" : ", ");
        		} else {
        			dimsString += TextFormatting.GRAY + dimName + " (Not installed)" + TextFormatting.RESET + ", ";
        		}
        	}
        	
        	lines.add("Dimensions: " + dimsString);
        }
        lines.add("");
        lines.add((description != null ? description : this.selectedPreset.getSecond().description));
        lines.add("");

        presetInfo = new Info(lines, logoPath, logoDims);       
    }

    private class Info extends OTGGuiScrollingList
    {
        @Nullable
        private ResourceLocation logoPath;
        private Dimension logoDims;
        private List<ITextComponent> lines = null;

        public Info(List<String> lines, @Nullable ResourceLocation logoPath, Dimension logoDims)
        {
            super(
        		  OTGGuiPresetList.this.getMinecraftInstance(),
                  OTGGuiPresetList.this.width - OTGGuiPresetList.this.listWidth - OTGGuiPresetList.this.margin - OTGGuiPresetList.this.rightMargin,
                  OTGGuiPresetList.this.height,
                  OTGGuiPresetList.this.topMargin, 
                  OTGGuiPresetList.this.height - OTGGuiPresetList.this.bottomMargin,
                  OTGGuiPresetList.this.listWidth + margin,
                  60, // TODO: Find out what slotheight does exactly, doesn't seem to influence size for info, only scroll speed?
                  OTGGuiPresetList.this.width,
                  OTGGuiPresetList.this.height);
            this.lines    = resizeContent(lines);
            this.logoPath = logoPath;
            this.logoDims = logoDims;

            this.setHeaderInfo(true, getHeaderHeight());
        }

        @Override protected int getSize() { return 0; }
        @Override protected void elementClicked(int index, boolean doubleClick) { }
        @Override protected boolean isSelected(int index) { return false; }
        @Override protected void drawBackground() {}
        @Override protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) { }

        private List<ITextComponent> resizeContent(List<String> lines)
        {
            List<ITextComponent> ret = new ArrayList<ITextComponent>();
            if(lines != null)
            {
	            for (String line : lines)
	            {
	                if (line == null)
	                {
	                    ret.add(null);
	                    continue;
	                }
	
	                ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
	                int maxTextLength = this.listWidth - 8;
	                if (maxTextLength >= 0)
	                {
	                    ret.addAll(GuiUtilRenderComponents.splitText(chat, maxTextLength, OTGGuiPresetList.this.fontRenderer, false, true));
	                }
	            }
            }
            return ret;
        }

        private int getHeaderHeight()
        {
          int height = 0;
          if (logoPath != null)
          {
              double scaleX = logoDims.width / 200.0;
              double scaleY = logoDims.height / 65.0;
              double scale = 1.0;
              if (scaleX > 1 || scaleY > 1)
              {
                  scale = 1.0 / Math.max(scaleX, scaleY);
              }
              logoDims.width *= scale;
              logoDims.height *= scale;

              height += logoDims.height;
              height += 10;
          }
          height += (lines.size() * 10);
          if (height < this.bottom - this.top - 8) height = this.bottom - this.top - 8;
          return height;
        }

        @Override
        protected void drawHeader(int entryRight, int relativeY, Tessellator tess)
        {
            int top = relativeY;

            if (logoPath != null)
            {
                GlStateManager.enableBlend();
                OTGGuiPresetList.this.mc.renderEngine.bindTexture(logoPath);
                BufferBuilder wr = tess.getBuffer();
                int offset = (this.left + this.listWidth/2) - (logoDims.width / 2);
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                wr.pos(offset,                  top + logoDims.height, zLevel).tex(0, 1).endVertex();
                wr.pos(offset + logoDims.width, top + logoDims.height, zLevel).tex(1, 1).endVertex();
                wr.pos(offset + logoDims.width, top,                   zLevel).tex(1, 0).endVertex();
                wr.pos(offset,                  top,                   zLevel).tex(0, 0).endVertex();
                tess.draw();
                GlStateManager.disableBlend();
                top += logoDims.height + 10;
            }

            for (ITextComponent line : lines)
            {
                if (line != null)
                {
                    GlStateManager.enableBlend();
                    OTGGuiPresetList.this.fontRenderer.drawStringWithShadow(line.getFormattedText(), this.left + 4, top, 0xFFFFFF);
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                }
                top += 10;
            }
        }

        @Override
        protected void clickHeader(int x, int y)
        {
            int offset = y;
            if (logoPath != null)
            {
              offset -= logoDims.height + 10;
            }
            if (offset <= 0)
            {
                return;
            }

            int lineIdx = offset / 10;
            if (lineIdx >= lines.size())
            {
                return;
            }

            ITextComponent line = lines.get(lineIdx);
            if (line != null)
            {
                int k = -4;
                for (ITextComponent part : line)
            	{
                    if (!(part instanceof TextComponentString))
                    {
                        continue;
                    }
                    k += OTGGuiPresetList.this.fontRenderer.getStringWidth(((TextComponentString)part).getText());
                    if (k >= x)
                    {
                    	OTGGuiPresetList.this.handleComponentClick(part);
                        break;
                    }
                }
            }
        }
    }
    
    private class OTGGuiSlotPresetList extends OTGGuiScrollingList
    {   
        private OTGGuiPresetList parent;

        public OTGGuiSlotPresetList(OTGGuiPresetList parent)
        {
            super(
        		parent.getMinecraftInstance(), 
        		OTGGuiPresetList.this.listWidth, 
        		parent.height, 
        		OTGGuiPresetList.this.topMargin, 
        		OTGGuiPresetList.this.height - OTGGuiPresetList.this.bottomMargin,
        		OTGGuiPresetList.this.leftMargin, 
        		OTGGuiPresetList.this.slotHeight, 
        		parent.width, 
        		parent.height
    		);
            this.parent = parent;
        }

        public void Resize()
        {
            this.listWidth = OTGGuiPresetList.this.listWidth;
            this.listHeight = parent.height;
            this.top = OTGGuiPresetList.this.topMargin;
            this.bottom = OTGGuiPresetList.this.height - OTGGuiPresetList.this.bottomMargin;
            this.slotHeight = OTGGuiPresetList.this.slotHeight;
            this.left = OTGGuiPresetList.this.leftMargin;
            this.right = OTGGuiPresetList.this.listWidth + this.left;
            this.screenWidth = parent.width;
            this.screenHeight = parent.height;
        }        

		@Override
        protected int getSize()
        {
            return ForgeEngine.presets.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
        	ArrayList<String> presets = new ArrayList<String>(ForgeEngine.presets.keySet());
        	// Can't add a preset multiple times for the same world, make name gray and unselectable if it's already present in the world.
        	if(this.parent.previousMenu instanceof OTGGuiDimensionList)
        	{
	        	for(DimensionConfig dimConfig : ((OTGGuiDimensionList)this.parent.previousMenu).dimensions)
	        	{
	        		if(dimConfig.PresetName != null && dimConfig.PresetName.equals(presets.get(index)))
	        		{
	        			return;
	        		}
	        	}
        	}
        	
            this.parent.selectPresetIndex(index);
        }

        @Override
        protected boolean isSelected(int index)
        {
            return this.parent.presetIndexSelected(index);
        }

        @Override
        protected void drawBackground()
        {
            if (mc.world != null)
            {
                // No background in-game
            } else {
                this.parent.drawDefaultBackground();
            }
        }

        @Override
        protected int getContentHeight()
        {
            return (this.getSize()) * slotHeight + 1;
        }

        @Override
        protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
        {
        	ArrayList<String> presets = new ArrayList<String>(ForgeEngine.presets.keySet());
        	// Can't add a preset multiple times for the same world, make name gray and unselectable if it's already present in the world.
        	boolean bFound = false;
        	if(this.parent.previousMenu instanceof OTGGuiDimensionList)
        	{
	        	for(DimensionConfig dimConfig : ((OTGGuiDimensionList)this.parent.previousMenu).dimensions)
	        	{
	        		if(dimConfig.PresetName != null && dimConfig.PresetName.equals(presets.get(idx)))
	        		{
	        			bFound = true;
	        			break;
	        		}
	        	}
        	}
            String       name     = net.minecraft.util.StringUtils.stripControlCodes(presets.get(idx));
            FontRenderer font     = this.parent.getFontRenderer();

            font.drawString(font.trimStringToWidth(name,    listWidth - 10), this.left + 3 , top +  2, bFound ? 0x666666 : 0xFFFFFF);
        }
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
