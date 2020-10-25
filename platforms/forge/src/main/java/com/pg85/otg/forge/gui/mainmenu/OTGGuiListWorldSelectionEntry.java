package com.pg85.otg.forge.gui.mainmenu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiOldSaveLoadConfirm;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.blocks.portal.PortalColors;
import com.pg85.otg.forge.dimensions.DimensionData;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.client.gui.GuiListExtended;;

@SideOnly(Side.CLIENT)
public class OTGGuiListWorldSelectionEntry implements GuiListExtended.IGuiListEntry
{
	// Taken from net.minecraft.client.gui.GuiListWorldSelectionEntry. Only added getSelectedWorldName().

    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    private final Minecraft client;
    private final OTGGuiWorldSelection worldSelScreen;
    private final WorldSummary worldSummary;
    private final ResourceLocation iconLocation;
    private final OTGGuiListWorldSelection containingListSel;
    private File iconFile;
    private DynamicTexture icon;
    private long lastClickTime;

    OTGGuiListWorldSelectionEntry(OTGGuiListWorldSelection listWorldSelIn, WorldSummary worldSummaryIn, ISaveFormat saveFormat)
    {
        this.containingListSel = listWorldSelIn;
        this.worldSelScreen = listWorldSelIn.getGuiWorldSelection();
        this.worldSummary = worldSummaryIn;
        this.client = Minecraft.getMinecraft();
        this.iconLocation = new ResourceLocation(PluginStandardValues.PresetsDirectoryName + File.separator + worldSummaryIn.getFileName() + File.separator + "icon");
        this.iconFile = saveFormat.getFile(worldSummaryIn.getFileName(), "icon.png");

        if (!this.iconFile.isFile())
        {
            this.iconFile = null;
        }

        this.loadServerIcon();
    }
    
	public String getSelectedWorldName()
	{
		return this.worldSummary != null ? this.worldSummary.getFileName() : null;
	}

	String lastSelectedWorldName = null;
	ArrayList<DimensionData> cachedDimData = null;
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
    	// Check if an OTG dim is present
    	if(this.lastSelectedWorldName == null || !this.getSelectedWorldName().equals(this.lastSelectedWorldName))
    	{
    		lastSelectedWorldName = this.getSelectedWorldName();
    		cachedDimData = OTGDimensionManager.GetDimensionData(new File(Minecraft.getMinecraft().gameDir + File.separator + "saves" + File.separator + this.getSelectedWorldName()));
    	}
    	boolean bFound = cachedDimData != null && cachedDimData.size() > 0;

        String s = (bFound ? TextFormatting.GOLD + "OTG " + TextFormatting.RESET : "") + this.worldSummary.getDisplayName();
        String s1 = this.worldSummary.getFileName() + " (" + DATE_FORMAT.format(new Date(this.worldSummary.getLastTimePlayed())) + ")";       
        String s2 = "";

        if (StringUtils.isEmpty(s))
        {
            s = I18n.format("selectWorld.world") + " " + (slotIndex + 1);
        }

        if (this.worldSummary.requiresConversion())
        {
            s2 = I18n.format("selectWorld.conversion") + " " + s2;
        } else {
            s2 = I18n.format("gameMode." + this.worldSummary.getEnumGameType().getName());

            if (this.worldSummary.isHardcoreModeEnabled())
            {
                s2 = TextFormatting.DARK_RED + I18n.format("gameMode.hardcore") + TextFormatting.RESET;
            }

            if (this.worldSummary.getCheatsEnabled())
            {
                s2 = s2 + ", " + I18n.format("selectWorld.cheats");
            }

            String s3 = this.worldSummary.getVersionName();
            
            if (this.worldSummary.markVersionInList())
            {
                if (this.worldSummary.askToOpenWorld())
                {
                    s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.RED + s3 + TextFormatting.RESET;
                }
                else
                {
                    s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.ITALIC + s3 + TextFormatting.RESET;
                }
            }
            else
            {
                s2 = s2 + ", " + I18n.format("selectWorld.version") + " " + s3;
            }
        }

        this.client.fontRenderer.drawString(s, x + 32 + 3, y + 1, 16777215);
        this.client.fontRenderer.drawString(s1, x + 32 + 3, y + this.client.fontRenderer.FONT_HEIGHT + 3, 8421504);
        this.client.fontRenderer.drawString(s2, x + 32 + 3, y + this.client.fontRenderer.FONT_HEIGHT + this.client.fontRenderer.FONT_HEIGHT + 3, 8421504);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : ICON_MISSING);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();

        if (this.client.gameSettings.touchscreen || isSelected)
        {
            this.client.getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int j = mouseX - x;
            int i = j < 32 ? 32 : 0;

            if (this.worldSummary.markVersionInList())
            {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, (float)i, 32, 32, 256.0F, 256.0F);

                if (this.worldSummary.askToOpenWorld())
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, (float)i, 32, 32, 256.0F, 256.0F);

                    if (j < 32)
                    {
                        this.worldSelScreen.setVersionTooltip(TextFormatting.RED + I18n.format("selectWorld.tooltip.fromNewerVersion1") + "\n" + TextFormatting.RED + I18n.format("selectWorld.tooltip.fromNewerVersion2"));
                    }
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, (float)i, 32, 32, 256.0F, 256.0F);

                    if (j < 32)
                    {
                        this.worldSelScreen.setVersionTooltip(TextFormatting.GOLD + I18n.format("selectWorld.tooltip.snapshot1") + "\n" + TextFormatting.GOLD + I18n.format("selectWorld.tooltip.snapshot2"));
                    }
                }
            }
            else
            {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, (float)i, 32, 32, 256.0F, 256.0F);
            }
        }
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        this.containingListSel.selectWorld(slotIndex);

        if (relativeX <= 32 && relativeX < 32)
        {
            this.joinWorld();
            return true;
        }
        else if (Minecraft.getSystemTime() - this.lastClickTime < 250L)
        {
            this.joinWorld();
            return true;
        }
        else
        {
            this.lastClickTime = Minecraft.getSystemTime();
            return false;
        }
    }

    void joinWorld()
    {
    	// For MP the server sends the presets
		// If world is null then we're not ingame
    	if(this.client.world == null || Minecraft.getMinecraft().isSingleplayer())
    	{
    		GuiHandler.loadGuiPresets(); // Load all WorldConfigs for the ingame UI
    	}

        DimensionsConfig dimsConfig = DimensionsConfig.loadFromFile(new File(Minecraft.getMinecraft().gameDir + File.separator + "saves" + File.separator + this.getSelectedWorldName()), OTG.getEngine().getOTGRootFolder());
        if(dimsConfig != null)
        {
        	ArrayList<String> missingPresets = new ArrayList<String>();
        	if(dimsConfig.Overworld != null && dimsConfig.Overworld.PresetName != null)
        	{
        		WorldConfig worldConfig = WorldConfig.fromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimsConfig.Overworld.PresetName));
        		if(worldConfig == null)
        		{
        			missingPresets.add(dimsConfig.Overworld.PresetName);
        		}
        	}
        	if(dimsConfig.Dimensions != null && dimsConfig.Dimensions.size() > 0)
        	{
        		for(DimensionConfig dimConfig : dimsConfig.Dimensions)
        		{
            		WorldConfig worldConfig = WorldConfig.fromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimConfig.PresetName));
            		if(worldConfig == null)
            		{
            			missingPresets.add(dimConfig.PresetName);
            		}
        		}
        	}
        	if(missingPresets.size() > 0)        		
        	{
        		String sMissingPresets = "";
        		for(int i = 0; i < missingPresets.size(); i++)
        		{
        			sMissingPresets += missingPresets.get(i) + (i < missingPresets.size() - 1 ? ", " : "");
        		}
        		this.client.displayGuiScreen(new GuiErrorScreen("Cannot load world. The following OTG presets are not installed:", sMissingPresets));
        		return;
        	}
        }        
        
        if (this.worldSummary.askToOpenWorld())
        {
            this.client.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback()
            {
                public void confirmClicked(boolean result, int id)
                {
                    if (result)
                    {
                    	OTGGuiListWorldSelectionEntry.this.loadWorld();
                    }
                    else
                    {
                    	OTGGuiListWorldSelectionEntry.this.client.displayGuiScreen(OTGGuiListWorldSelectionEntry.this.worldSelScreen);
                    }
                }
            }, I18n.format("selectWorld.versionQuestion"), I18n.format("selectWorld.versionWarning", this.worldSummary.getVersionName()), I18n.format("selectWorld.versionJoinButton"), I18n.format("gui.cancel"), 0));
        }
        else
        {
            this.loadWorld();
        }
    }

    void deleteWorld()
    {
        this.client.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback()
        {
            public void confirmClicked(boolean result, int id)
            {
                if (result)
                {
                	OTGGuiListWorldSelectionEntry.this.client.displayGuiScreen(new GuiScreenWorking());
                    ISaveFormat isaveformat = OTGGuiListWorldSelectionEntry.this.client.getSaveLoader();
                    isaveformat.flushCache();
                    isaveformat.deleteWorldDirectory(OTGGuiListWorldSelectionEntry.this.worldSummary.getFileName());
                    OTGGuiListWorldSelectionEntry.this.containingListSel.refreshList();
                }

                OTGGuiListWorldSelectionEntry.this.client.displayGuiScreen(OTGGuiListWorldSelectionEntry.this.worldSelScreen);
            }
        }, I18n.format("selectWorld.deleteQuestion"), "'" + this.worldSummary.getDisplayName() + "' " + I18n.format("selectWorld.deleteWarning"), I18n.format("selectWorld.deleteButton"), I18n.format("gui.cancel"), 0));
    }

    private void loadWorld()
    {        
        this.client.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        if (this.client.getSaveLoader().canLoadWorld(this.worldSummary.getFileName()))
        {
            tryLoadExistingWorld(net.minecraftforge.fml.client.FMLClientHandler.instance(), worldSelScreen, this.worldSummary);
        }
    }

    private void tryLoadExistingWorld(FMLClientHandler clientHandler, OTGGuiWorldSelection selectWorldGUI, WorldSummary comparator)
    {
    	// If this world has OTG overworld/dimensions then check if it has a DimensionsConfig
    	// If this is a legacy world then we'll need to create a new one.
    	ArrayList<DimensionData> dimensionDatas = OTGDimensionManager.GetDimensionData(new File(clientHandler.getSavesDir() + File.separator + this.worldSummary.getFileName()));
    	DimensionsConfig dimsConfig = DimensionsConfig.loadFromFile(new File(clientHandler.getSavesDir(), comparator.getFileName()), OTG.getEngine().getOTGRootFolder());
    	if(dimsConfig == null && dimensionDatas != null && dimensionDatas.size() > 0)
    	{
    		dimsConfig = new DimensionsConfig(new File(clientHandler.getSavesDir(), comparator.getFileName()));
    		for(DimensionData dimensionData : dimensionDatas)
    		{
    			if(dimensionData.dimensionId == 0)
    			{
    				// If this is a legacy overworld then the world name must be the same as the preset name
    				File worldConfigLocation = new File(OTG.getEngine().getWorldsDirectory(), comparator.getFileName());
    				WorldConfig worldConfig = WorldConfig.fromDisk(worldConfigLocation);
    				if(worldConfig == null)
    				{
    					OTG.log(LogMarker.ERROR, "Could not load world. Preset not found: " + worldConfigLocation);
    					return;
    				}
    				DimensionConfig overWorld = new DimensionConfig(comparator.getFileName(), 0, true, worldConfig);
    				dimsConfig.Overworld = overWorld;
    			} else {
    				// If this is a legacy dim then the dim name must be the same as the preset name
    				File worldConfigLocation = new File(OTG.getEngine().getWorldsDirectory(), dimensionData.dimensionName);
    				WorldConfig worldConfig = WorldConfig.fromDisk(worldConfigLocation);
    				if(worldConfig == null)
    				{
    					OTG.log(LogMarker.ERROR, "Could not load world. Preset not found: " + worldConfigLocation);
    					return;
    				}
    				DimensionConfig dimension = new DimensionConfig(dimensionData.dimensionName, dimensionData.dimensionId, true, worldConfig);
    				dimsConfig.Dimensions.add(dimension);
    			}
    		}
    		dimsConfig.save();
    	}
    	else if(dimsConfig == null && (dimensionDatas == null || dimensionDatas.size() == 0))
    	{
    		// This is a vanilla world without dims, save a config without overworld / dims
    		dimsConfig = new DimensionsConfig(new File(clientHandler.getSavesDir(), comparator.getFileName()));
			// Create a dummy overworld config
    		dimsConfig.Overworld = new DimensionConfig();
			// Check if there is a modpack config for vanilla worlds, 
			DimensionsConfig modPackConfig = OTG.getEngine().getModPackConfigManager().getModPackConfig(null);
			if(modPackConfig != null)
			{
				dimsConfig.ModPackConfigName = modPackConfig.ModPackConfigName;
				dimsConfig.ModPackConfigVersion = modPackConfig.ModPackConfigVersion;
				dimsConfig.Overworld = modPackConfig.Overworld.clone();
				for(DimensionConfig dimConfig : modPackConfig.Dimensions)
				{
			    	if(!OTGDimensionManager.isDimensionNameRegistered(dimConfig.PresetName))
		    		{
			    		File worldConfigFile = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + File.separator + PluginStandardValues.PresetsDirectoryName + File.separator + dimConfig.PresetName + File.separator + "WorldConfig.ini");
			    		if(worldConfigFile.exists())
			    		{
			    			DimensionConfig newConfig = dimConfig.clone();
			    	        // Ensure the portal color is unique (not already in use), otherwise correct it.
			    			PortalColors.correctPortalColor(newConfig, dimsConfig.getAllDimensions());
		                	dimsConfig.Dimensions.add(newConfig);
			    		}
		    		}
				}
			}   		
    		dimsConfig.save();
    	}

		OTG.setDimensionsConfig(dimsConfig);		 	
    	
        File dir = new File(clientHandler.getSavesDir(), comparator.getFileName());
        NBTTagCompound leveldat;
        try
        {
            leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(dir, "level.dat")));
        }
        catch (Exception e)
        {
            try
            {
                leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(dir, "level.dat_old")));
            }
            catch (Exception e1)
            {
                FMLLog.log.warn("There appears to be a problem loading the save {}, both level files are unreadable.", comparator.getFileName());
                return;
            }
        }
        NBTTagCompound fmlData = leveldat.getCompoundTag("FML");
        if (fmlData.hasKey("ModItemData"))
        {
        	clientHandler.showGuiScreen(new GuiOldSaveLoadConfirm(comparator.getFileName(), comparator.getDisplayName(), selectWorldGUI));
        }
        else
        {
            try
            {
                client.launchIntegratedServer(comparator.getFileName(), comparator.getDisplayName(), null);
            }
            catch (StartupQuery.AbortedException e)
            {
                // ignore
            }
        }
    }

    private void loadServerIcon()
    {
        boolean flag = this.iconFile != null && this.iconFile.isFile();

        if (flag)
        {
            BufferedImage bufferedimage;

            try
            {
                bufferedimage = ImageIO.read(this.iconFile);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
            }
            catch (Throwable throwable)
            {
                LOGGER.error("Invalid icon for world {}", this.worldSummary.getFileName(), throwable);
                this.iconFile = null;
                return;
            }

            if (this.icon == null)
            {
                this.icon = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                this.client.getTextureManager().loadTexture(this.iconLocation, this.icon);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), this.icon.getTextureData(), 0, bufferedimage.getWidth());
            this.icon.updateDynamicTexture();
        }
        else if (!flag)
        {
            this.client.getTextureManager().deleteTexture(this.iconLocation);
            this.icon = null;
        }
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public void updatePosition(int slotIndex, int x, int y, float partialTicks)
    {
    }
}