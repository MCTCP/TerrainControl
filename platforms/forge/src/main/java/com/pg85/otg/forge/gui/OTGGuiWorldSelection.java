package com.pg85.otg.forge.gui;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pg85.otg.OTG;
import com.pg85.otg.forge.util.IOHelper;
import com.pg85.otg.logging.LogMarker;

@SideOnly(Side.CLIENT)
public class OTGGuiWorldSelection extends GuiScreen implements GuiYesNoCallback
{
	// Taken from net.minecraft.client.gui.GuiWorldSelection. Changed confirmClicked and disabled rename button
	
    /** The screen to return to when this closes (always Main Menu). */
    protected GuiScreen prevScreen;
    protected String title = "Select world";
    /** Tooltip displayed a world whose version is different from this client's */
    private String worldVersTooltip;
    //private GuiButton renameButton;
    //private GuiButton copyButton;
    private OTGGuiListWorldSelection selectionList;
	
    public OTGGuiWorldSelection(GuiScreen screenIn)
    {    	
    	this.prevScreen = screenIn;
    }
	
	@Override
    public void confirmClicked(boolean ok, int worldId)
    {
		// Delete existing TC settings
					
		OTGGuiListWorldSelectionEntry selectedWorld = this.selectionList.getSelectedWorld();
		
		String worldName = selectedWorld.getSelectedWorldName();
		
		// Delete world
		super.confirmClicked(ok, worldId);

    	// TODO: This is for legacy worlds only, the files are stored in the world saves directory now. Remove this?
		
		// Check for world settings for this world
        File TCWorldsDirectory = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds");
        if(TCWorldsDirectory.exists() && TCWorldsDirectory.isDirectory())
        {
        	for(File worldDir : TCWorldsDirectory.listFiles())
        	{
        		if(worldDir.isDirectory() && worldDir.getName().equals(worldName))
        		{
    				// Only delete temp world files, not WorldBiomes and WorldObjects
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
    			}
    			break;
    		}
    	}            
		this.mc.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()));
    }
    
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
	@Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            OTGGuiListWorldSelectionEntry guilistworldselectionentry = this.selectionList.getSelectedWorld();

            if (button.id == 2)
            {
                if (guilistworldselectionentry != null)
                {
                    guilistworldselectionentry.deleteWorld();
                }
            }
            else if (button.id == 1)
            {
                if (guilistworldselectionentry != null)
                {
                    guilistworldselectionentry.joinWorld();
                }
            }
            else if (button.id == 3)
            {
            	GuiHandler.useVanillaScreen = false;
                this.mc.displayGuiScreen(new GuiCreateWorld(this));
            }
            else if (button.id == 4)
            {
            	GuiHandler.useVanillaScreen = true;
    	        DimensionType.OVERWORLD.clazz = WorldProviderSurface.class;
    	        DimensionType.OVERWORLD.suffix = "overworld";
            	this.mc.displayGuiScreen(new GuiCreateWorld(this));
            	
                //if (guilistworldselectionentry != null)
                //{
                    //guilistworldselectionentry.editWorld();
                //}
            }
            else if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.prevScreen);
            }
            //else if (button.id == 5 && guilistworldselectionentry != null)
            //{
                //guilistworldselectionentry.recreateWorld();
            //}
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.title = I18n.format("selectWorld.title", new Object[0]);
        this.selectionList = new OTGGuiListWorldSelection(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
        this.postInit();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.selectionList.handleMouseInput();
    }

    GuiButton selectButton;
    GuiButton deleteButton;
    
    public void postInit()
    {
    	int margin = 4;
    	int marginFromBottom = this.height - 54;
    	int buttonHeight = 20;
    	int uiWidth = 357;
    	int marginFromLeft = Math.round((this.width - uiWidth) / 2f);
    	int row1Width = 3;
    	int row1ButtonWidth = (int) Math.floor((uiWidth - ((row1Width - 1) * margin)) / (float)row1Width);
    	int row1LeftOver = (uiWidth - ((row1ButtonWidth * row1Width) + (margin * (row1Width - 1))));
    	int row2Width = 2;
    	int row2ButtonWidth = (int)Math.floor((uiWidth - ((row2Width - 1) * margin)) / (float)row2Width);
    	int row2LeftOver = (uiWidth - ((row2ButtonWidth * row2Width) + (margin * (row2Width - 1))));
    	
        selectButton = this.addButton(new GuiButton(1, marginFromLeft, marginFromBottom, row1ButtonWidth, buttonHeight, I18n.format("selectWorld.select", new Object[0])));
		GuiButton createButton = this.addButton(new GuiButton(4, marginFromLeft + row1ButtonWidth + margin, marginFromBottom, row1ButtonWidth, buttonHeight, I18n.format("selectWorld.create", new Object[0])));
		GuiButton otgButton = this.addButton(new GuiButton(3, marginFromLeft + row1ButtonWidth + margin + row1ButtonWidth + margin, marginFromBottom, row1ButtonWidth + row1LeftOver, buttonHeight, "Create OTG World"));
        //this.renameButton = this.addButton(new GuiButton(4, this.width / 2 - 154, this.height - 28, 72, 20, I18n.format("selectWorld.edit", new Object[0])));
        deleteButton = this.addButton(new GuiButton(2, marginFromLeft, marginFromBottom + buttonHeight + margin, row2ButtonWidth, buttonHeight, I18n.format("selectWorld.delete", new Object[0])));
        //this.copyButton = this.addButton(new GuiButton(5, this.width / 2 + 4, this.height - 28, 72, 20, I18n.format("selectWorld.recreate", new Object[0])));
		//this.addButton(new GuiButton(0, this.width / 2 + 82, this.height - 28, 72, 20, I18n.format("gui.cancel", new Object[0])));
		GuiButton cancelButton = this.addButton(new GuiButton(0, marginFromLeft + row2ButtonWidth + margin, marginFromBottom + buttonHeight + margin, row2ButtonWidth + row2LeftOver, buttonHeight, I18n.format("gui.cancel", new Object[0])));
        selectButton.enabled = false;
        deleteButton.enabled = false;
        //this.renameButton.enabled = false;
        //this.copyButton.enabled = false;
    }
	
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.worldVersTooltip = null;
        this.selectionList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.worldVersTooltip != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.worldVersTooltip)), mouseX, mouseY);
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectionList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.selectionList.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Called back by selectionList when we call its drawScreen method, from ours.
     */
    public void setVersionTooltip(String p_184861_1_)
    {
        this.worldVersTooltip = p_184861_1_;
    }

    public void selectWorld(@Nullable OTGGuiListWorldSelectionEntry entry)
    {
        boolean flag = entry != null;
        this.selectButton.enabled = flag;
        this.deleteButton.enabled = flag;
        //this.renameButton.enabled = flag;
        //this.copyButton.enabled = flag;
    }
}