package com.pg85.otg.forge.gui.dimensions.base;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.gui.dimensions.OTGGuiDimensionSettingsList;
import com.pg85.otg.forge.gui.dimensions.base.SettingEntry.ValueType;
import com.pg85.otg.forge.gui.presets.OTGGuiPresetList;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyEntry implements IGuiListEntry
{
	private final OTGGuiDimensionSettingsList otgGuiDimensionSettingsList;
	/** The keybinding specified for this KeyEntry */
    public final SettingEntry settingEntry;
    /** The localized key description for this KeyEntry */
    private final String keyDesc;
    private final GuiButton btnSettingEntry;
    private final GuiTextField txtSettingsEntry;
    private final GuiButton btnReset;
    private final OTGGuiDimensionSettingsList parent;

    public KeyEntry(OTGGuiDimensionSettingsList otgGuiDimensionSettingsList, SettingEntry settingEntry, OTGGuiDimensionSettingsList parent, boolean isEnabled)
    {
    	this(otgGuiDimensionSettingsList, settingEntry, parent);
    	this.btnSettingEntry.enabled = isEnabled;
    }
    
    public KeyEntry(OTGGuiDimensionSettingsList otgGuiDimensionSettingsList, SettingEntry<?> settingEntry, OTGGuiDimensionSettingsList parent)
    {
        this.otgGuiDimensionSettingsList = otgGuiDimensionSettingsList;
		this.settingEntry = settingEntry;
    	this.parent = parent;
        this.keyDesc = this.settingEntry.name;
        this.btnSettingEntry = new GuiButton(0, 0, 0, 95, 20, this.settingEntry.getValueString());
        this.btnSettingEntry.displayString = this.settingEntry.getValueString();
        this.txtSettingsEntry = new GuiTextField(0, this.otgGuiDimensionSettingsList.mc.fontRenderer, 0, 0, 90, 20);
        this.txtSettingsEntry.setMaxStringLength(Integer.MAX_VALUE);
        this.txtSettingsEntry.setText(this.settingEntry.getValueString());            
        this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
    }

    public void updatePosition(int slotIndex, int x, int y, float partialTicks)
    {
    }
    
    public String getLabelText()
    {
    	return this.settingEntry.name;
    }
    
    public String getDisplayText()
    {
    	return this.settingEntry.getValueString();
    }
    
    // Drawing
    
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
    	boolean gameIsRunning = this.otgGuiDimensionSettingsList.mc.world != null;
    	boolean newWorldOnly = this.settingEntry.newWorldOnly;
    	boolean overWorldOnly = this.settingEntry.overWorldOnly;
    	boolean isOverWorld = this.parent.controlsScreen.selectedDimensionIndex == 0;
    	boolean isNewConfig = this.parent.controlsScreen.selectedDimension.isNewConfig;
    	boolean showButtons = !(!isOverWorld && overWorldOnly) && !(gameIsRunning && !isNewConfig && newWorldOnly) && (!isOverWorld || !this.settingEntry.name.equals("Preset"));
    	
    	int marginleft = x;
        this.otgGuiDimensionSettingsList.mc.fontRenderer.drawString(this.keyDesc, marginleft + 6, y + slotHeight / 2 - this.otgGuiDimensionSettingsList.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
        this.btnReset.x = marginleft + 240;
        this.btnReset.y = y;
        this.btnReset.enabled = showButtons &&
    		(
				(
					this.settingEntry.value == null && 
					this.settingEntry.defaultValue != null
				) || (
					this.settingEntry.value != null && 
					(
						(
							this.settingEntry.valueType == ValueType.Bool && 
							this.settingEntry.value != this.settingEntry.defaultValue
						) || (
							this.settingEntry.valueType != ValueType.Bool &&
							!this.settingEntry.value.equals(this.settingEntry.defaultValue)
						)
					)
				)
			);
        this.btnReset.drawButton(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY, partialTicks);
        this.btnSettingEntry.x = marginleft + 135;
        this.btnSettingEntry.y = y;
        // Don't re-enable buttons that were disabled by other settings, like AllowCheats and BonusChest.
        this.btnSettingEntry.enabled = this.btnSettingEntry.enabled && showButtons;  
        if(!isOverWorld && overWorldOnly)
        {
        	this.btnSettingEntry.displayString = "Overworld";
        }
        
        this.txtSettingsEntry.x = marginleft + 137;
        this.txtSettingsEntry.y = y;
        this.txtSettingsEntry.setEnabled(showButtons);

        if(this.settingEntry.name.equals("Game type") || this.settingEntry.name.equals("Preset") || this.settingEntry.valueType == ValueType.Bool)
        {
        	this.btnSettingEntry.drawButton(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY, partialTicks);
        } else {
        	this.txtSettingsEntry.drawTextBox();
        }
    }
    
    // Mouse / keyboard
    
    public void keyTyped(char typedChar, int keyCode)
    {
        if(
    		this.settingEntry.valueType != ValueType.Bool && 
    		!this.settingEntry.name.equals("Game type") && // GameType is a string presented as a button 
    		!this.settingEntry.name.equals("Preset") // Preset is a string presented as a button
		)
        {
        	if(this.txtSettingsEntry.isFocused() || keyCode == 28 || keyCode == 83) // 28 + 83 is enter 
        	{
        		boolean bIsFunctionkey = false;
        		if(
					GuiScreen.isKeyComboCtrlA(keyCode) ||
					GuiScreen.isKeyComboCtrlC(keyCode) ||
					GuiScreen.isKeyComboCtrlV(keyCode) ||
					GuiScreen.isKeyComboCtrlX(keyCode) ||
					keyCode == 14 || // backspace
					keyCode == 199 ||
					keyCode == 203 || // left
					keyCode == 205 || // right
					keyCode == 207 ||
					keyCode == 211 ||
					keyCode == 28 || // 28 + 83 is enter 
					keyCode == 83
                )
        		{
        			bIsFunctionkey = true;
    			}
        		
        		if(keyCode == 28 || keyCode == 83) // 28 + 83 is enter
        		{
        			this.txtSettingsEntry.setFocused(false);
        		}
        		
        		// Key code 14 is backspace
        		if(this.settingEntry.valueType == ValueType.String)
        		{
        			if(keyCode != 28 && keyCode != 83)
        			{
        				this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
        			}
    				if(keyCode == 28 || keyCode == 83)
    				{
    	            	this.settingEntry.value = this.txtSettingsEntry.getText();
    	            	this.txtSettingsEntry.setText(this.settingEntry.getValueString());
    	            	this.otgGuiDimensionSettingsList.applySettings();
    				}
        		}
        		else if(this.settingEntry.valueType == ValueType.Int)
        		{
        			if(Character.isDigit(typedChar) || (typedChar == '-' && this.txtSettingsEntry.getCursorPosition() == 0) || bIsFunctionkey)
        			{
        				if(keyCode != 28 && keyCode != 83)
        				{
        					this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
        				}
        				if(keyCode == 28 || keyCode == 83)
        				{
        					int integer = 0;
        					try
        					{
        						integer = Integer.parseInt(txtSettingsEntry.getText().trim());
        					}
        					catch(NumberFormatException ex)
        					{
        						integer = (int)this.settingEntry.value;
        					}
        					if(integer > (int)this.settingEntry.maxValue)
        					{
        						integer = (int)this.settingEntry.maxValue;
        					}
        					if(integer < (int)this.settingEntry.minValue)
        					{
        						integer = (int)this.settingEntry.minValue;
        					}
        					
        					if(this.settingEntry.name.equals("World border radius"))
        					{
            					int radius = integer;
            					if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
            					{
	            					ForgeWorld forgeWorld = null;
            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
	            					if(forgeWorld == null)
	            					{
	            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
	            					}
	            					
	            					// ForgeWorld can be null in SP world creation menu.
	            					if(
            							Minecraft.getMinecraft().isSingleplayer() && 
            							forgeWorld != null && 
            							forgeWorld.getWorldSession() != null 
        							)
	            					{
	            						ChunkCoordinate worldBorderCenterPoint = ChunkCoordinate.fromBlockCoords(forgeWorld.getWorld().getWorldInfo().getSpawnX(),forgeWorld.getWorld().getWorldInfo().getSpawnZ());                
	            						// 0 is disabled, 1 is 1 chunk, 2 is 3 chunks, 3 is 5 chunks etc
	            						double d2 = radius == 0 ? 6.0E7D : radius == 1 ? 16 : ((radius - 1) * 2 + 1) * 16;
	            						forgeWorld.getWorld().getWorldBorder().setCenter(worldBorderCenterPoint.getBlockX() + 8, worldBorderCenterPoint.getBlockZ() + 8);
	            						forgeWorld.getWorld().getWorldBorder().setTransition(d2);			                            	
	            					}
            					}
	            				this.settingEntry.value = radius;
	            				this.txtSettingsEntry.setText(radius + "");
        					}
        					
            				if(this.settingEntry.name.equals("Pregenerator radius"))
            				{
            					int radius = integer;
            					if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
            					{
	            					ForgeWorld forgeWorld = null;
            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
	            					if(forgeWorld == null)
	            					{
	            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
	            					}
	            					// ForgeWorld can be null in SP world creation menu.
	            					if(
            							Minecraft.getMinecraft().isSingleplayer() && 
            							forgeWorld != null && 
            							forgeWorld.getWorldSession() != null
        							)
	            					{
		            					forgeWorld.getWorldSession().setPregenerationRadius(radius);
		            					radius = forgeWorld.getWorldSession().getPregenerationRadius();
		                            	this.parent.controlsScreen.selectedDimension.PregeneratorRadiusInChunks = radius;				                            	
	            					}
            					}
	            				this.settingEntry.value = radius;
	            				this.txtSettingsEntry.setText(radius + "");
            				} else {	            					
	            				this.settingEntry.value = integer;
            				}
            				this.txtSettingsEntry.setText(this.settingEntry.getValueString());
            				
            				this.otgGuiDimensionSettingsList.applySettings();	            			
        				}
        			}
        		}
        		else if(this.settingEntry.valueType == ValueType.Double)
        		{
        			if(bIsFunctionkey || Character.isDigit(typedChar) || (typedChar == '-' && this.txtSettingsEntry.getCursorPosition() == 0) || (typedChar == '.' && !this.txtSettingsEntry.getText().contains(".")))
        			{
        				if(keyCode != 28 && keyCode != 83)
        				{
        					this.txtSettingsEntry.textboxKeyTyped(typedChar, keyCode);
        				}
        				if(keyCode == 28 || keyCode == 83)
        				{
        					double db = 0;
        					try
        					{
        						db = Double.parseDouble(txtSettingsEntry.getText().trim());
        					}
        					catch(NumberFormatException ex)
        					{
        						db = (int)this.settingEntry.value;
        					}
        					if(db > (double)this.settingEntry.maxValue)
        					{
        						db = (double)this.settingEntry.maxValue;
        					}
        					if(db < (double)this.settingEntry.minValue)
        					{
        						db = (double)this.settingEntry.minValue;
        					}            					
            				this.settingEntry.value = db;
            				this.txtSettingsEntry.setText(this.settingEntry.getValueString());
            				this.otgGuiDimensionSettingsList.applySettings();
        				}
        			}
        		}
        	}
        }
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     * Also called whenever the mouse is pressed on the parent screen, to make text boxes lose focus
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
    	if(this.settingEntry.name.equals("Game type"))
    	{
        	if(this.btnSettingEntry.mousePressed(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY))
        	{
                this.otgGuiDimensionSettingsList.controlsScreen.buttonId = this.settingEntry;
            	if(((String)this.settingEntry.value).toUpperCase().equals(GameType.SURVIVAL.toString()))
            	{
	                this.settingEntry.value = "Hardcore";
    				this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            		for(IGuiListEntry entry : this.parent.getAllListEntries())
            		{
            			if(
        					entry.getLabelText().equals("Allow cheats") ||
        					entry.getLabelText().equals("Bonus chest")
    					)
        				{
            				((KeyEntry)entry).btnSettingEntry.enabled = false;
            				((KeyEntry)entry).settingEntry.value = false;
            				((KeyEntry)entry).btnSettingEntry.displayString = ((KeyEntry)entry).settingEntry.getValueString();
            			}
            		}
            	}
            	else if(((String)this.settingEntry.value).toUpperCase().equals(GameType.CREATIVE.toString()))
            	{
            		this.settingEntry.value = "Survival";
            		this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            		for(IGuiListEntry entry : this.parent.getAllListEntries())
            		{
            			if(
        					entry.getLabelText().equals("Allow cheats") ||
        					entry.getLabelText().equals("Bonus chest")
    					)
        				{
            				((KeyEntry)entry).btnSettingEntry.enabled = true;
            			}
            		}
            	}
                else if(((String)this.settingEntry.value).toUpperCase().equals("HARDCORE"))
                {
                	this.settingEntry.value = "Creative";
                	this.btnSettingEntry.displayString = this.settingEntry.getValueString();
            		for(IGuiListEntry entry : this.parent.getAllListEntries())
            		{
            			if(
        					entry.getLabelText().equals("Allow cheats") ||
        					entry.getLabelText().equals("Bonus chest")
    					)
        				{
            				((KeyEntry)entry).btnSettingEntry.enabled = true;
            			}
            		}	                	
            	}
				this.otgGuiDimensionSettingsList.applySettings();
                return true;
        	}
    	}
    	else if(this.settingEntry.name.equals("Preset"))
    	{
        	if(this.btnSettingEntry.mousePressed(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY))
        	{
                this.otgGuiDimensionSettingsList.controlsScreen.buttonId = this.settingEntry;
                
                // Show choose preset menu
                this.otgGuiDimensionSettingsList.controlsScreen.selectingPresetForDimension = true;
                this.otgGuiDimensionSettingsList.controlsScreen.selectPresetForDimensionMenu = new OTGGuiPresetList(this.otgGuiDimensionSettingsList.controlsScreen, true);
                this.otgGuiDimensionSettingsList.mc.displayGuiScreen(this.otgGuiDimensionSettingsList.controlsScreen.selectPresetForDimensionMenu);
                
                return true;
        	}
    	}
    	else if (this.settingEntry.valueType == ValueType.Bool)
        {
        	if(this.btnSettingEntry.mousePressed(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY))
        	{
                this.otgGuiDimensionSettingsList.controlsScreen.buttonId = this.settingEntry;
            	if(((boolean)this.settingEntry.value))
            	{
	                this.settingEntry.value = false;
            	} else {
	                this.settingEntry.value = true;
            	}
                this.btnSettingEntry.displayString = this.settingEntry.getValueString();
				this.otgGuiDimensionSettingsList.applySettings();
                return true;
        	}
        }
        else if (this.settingEntry.valueType != ValueType.Bool)
        {
        	if(this.txtSettingsEntry.mouseClicked(mouseX, mouseY,0))
        	{
                this.otgGuiDimensionSettingsList.controlsScreen.buttonId = this.settingEntry;                
                return true;
        	} else {
        		// If textbox for an int/double loses foxus and is empty then set default value
                if(this.txtSettingsEntry.getText().length() == 0 && (this.settingEntry.valueType == ValueType.Int || this.settingEntry.valueType == ValueType.Double))
                {
	                this.settingEntry.value = this.settingEntry.defaultValue;
					this.otgGuiDimensionSettingsList.applySettings();
                }
        	}
        }
        
        if (this.btnReset.mousePressed(this.otgGuiDimensionSettingsList.mc, mouseX, mouseY))
        {
        	this.settingEntry.value = this.settingEntry.defaultValue;       	
        	       	
        	if(this.settingEntry.name.equals("World border radius"))
    		{
				int radius = (int)this.settingEntry.value;
        		if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
        		{
					ForgeWorld forgeWorld = null;
					forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
					}
					// ForgeWorld can be null for SP world creation menu
					if(
						Minecraft.getMinecraft().isSingleplayer() && 
						forgeWorld != null && 
						forgeWorld.getWorldSession() != null
					)
					{
						ChunkCoordinate worldBorderCenterPoint = ChunkCoordinate.fromBlockCoords(forgeWorld.getWorld().getWorldInfo().getSpawnX(),forgeWorld.getWorld().getWorldInfo().getSpawnZ());                
						// 0 is disabled, 1 is 1 chunk, 2 is 3 chunks, 3 is 5 chunks etc
						double d2 = radius == 0 ? 6.0E7D : radius == 1 ? 16 : ((radius - 1) * 2 + 1) * 16;
						forgeWorld.getWorld().getWorldBorder().setCenter(worldBorderCenterPoint.getBlockX() + 8, worldBorderCenterPoint.getBlockZ() + 8);
						forgeWorld.getWorld().getWorldBorder().setTransition(d2);
					}
        		}
				this.settingEntry.value = radius;
				this.txtSettingsEntry.setText(radius + "");
    		}
        	if(this.settingEntry.name.equals("Pregenerator radius"))
    		{
				int radius = (int)this.settingEntry.value;
        		if(!this.parent.controlsScreen.selectedDimension.isNewConfig)
        		{
					ForgeWorld forgeWorld = null;
					forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(this.parent.controlsScreen.selectedDimensionIndex == 0 ? "overworld" : this.parent.controlsScreen.selectedDimension.PresetName);	
					}
					// ForgeWorld can be null for SP world creation menu
					if(
						Minecraft.getMinecraft().isSingleplayer() && 
						forgeWorld != null && 
						forgeWorld.getWorldSession() != null
					)
					{
    					forgeWorld.getWorldSession().setPregenerationRadius(radius);
    					radius = forgeWorld.getWorldSession().getPregenerationRadius();
                    	this.parent.controlsScreen.selectedDimension.PregeneratorRadiusInChunks = radius;				                            	
					}
        		}
				this.settingEntry.value = radius;
				this.txtSettingsEntry.setText(radius + "");
    		}
        	
        	if(this.settingEntry.name.equals("Game type"))
        	{
            	this.btnSettingEntry.displayString = this.settingEntry.getValueString();
        		for(IGuiListEntry entry : this.parent.getAllListEntries())
        		{
        			if(
    					entry.getLabelText().equals("Allow cheats") ||
    					entry.getLabelText().equals("Bonus chest")
					)
    				{
                    	if(((String)this.settingEntry.value).equals("Hardcore"))
                    	{
            				((KeyEntry)entry).btnSettingEntry.enabled = false;
            				((KeyEntry)entry).settingEntry.value = false;
                    	} else {
            				((KeyEntry)entry).btnSettingEntry.enabled = true;
                    	}
                    	((KeyEntry)entry).btnSettingEntry.displayString = ((KeyEntry)entry).settingEntry.getValueString();
        			}
        		}
        	} else {
        		if(this.settingEntry.valueType.equals(ValueType.Bool))
        		{
            		this.btnSettingEntry.displayString = this.settingEntry.getValueString();
        		} else {
        			this.txtSettingsEntry.setText(this.settingEntry.getValueString());
        		}
        	}
            
			this.otgGuiDimensionSettingsList.applySettings();
            return true;
        }
        
        return false;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        this.btnSettingEntry.mouseReleased(x, y);
        this.btnReset.mouseReleased(x, y);
    }
}