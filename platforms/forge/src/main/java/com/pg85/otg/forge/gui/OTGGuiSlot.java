package com.pg85.otg.forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class OTGGuiSlot
{
    protected final Minecraft mc;
    public int width;
    public int height;
    /** The top of the slot container. Affects the overlays and scrolling. */
    public int top;
    /** The bottom of the slot container. Affects the overlays and scrolling. */
    public int bottom;
    public int right;
    public int left;
    /** The height of a slot. */
    public final int slotHeight;
    /** The buttonID of the button used to scroll up */
    private int scrollUpButtonID;
    /** The buttonID of the button used to scroll down */
    private int scrollDownButtonID;
    protected int mouseX;
    protected int mouseY;
    protected boolean centerListVertically = true;
    /** Where the mouse was in the window when you first clicked to scroll */
    protected int initialClickY = -2;
    /**
     * What to multiply the amount you moved your mouse by (used for slowing down scrolling when over the items and not
     * on the scroll bar)
     */
    protected float scrollMultiplier;
    /** How far down this slot has been scrolled */
    protected float amountScrolled;
    /** The element in the list that was selected */
    protected int selectedElement = -1;
    /** The time when this button was last clicked. */
    protected long lastClicked;
    protected boolean visible = true;
    /** Set to true if a selected element in this gui will show an outline box */
    protected boolean showSelectionBox = true;
    protected boolean hasListHeader;
    public int headerPadding;
    private boolean enabled = true;

    public OTGGuiSlot(Minecraft mcIn, int left, int width, int height, int topIn, int bottomIn, int slotHeightIn)
    {
        this.mc = mcIn;
        this.width = width;
        this.height = height;
        this.top = topIn;
        this.bottom = bottomIn;
        this.slotHeight = slotHeightIn;
        this.left = left;
        this.right = left + width;
    }

    public void setShowSelectionBox(boolean showSelectionBoxIn)
    {
        this.showSelectionBox = showSelectionBoxIn;
    }

    /**
     * Sets hasListHeader and headerHeight. Params: hasListHeader, headerHeight. If hasListHeader is false headerHeight
     * is set to 0.
     */
    protected void setHasListHeader(boolean hasListHeaderIn, int headerPaddingIn)
    {
        this.hasListHeader = hasListHeaderIn;
        this.headerPadding = headerPaddingIn;

        if (!hasListHeaderIn)
        {
            this.headerPadding = 0;
        }
    }

    protected abstract int getSize();

    /**
     * The element in the slot that was clicked, boolean for whether it was double clicked or not
     */
    protected abstract void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY);

    /**
     * Returns true if the element passed in is currently selected
     */
    protected abstract boolean isSelected(int slotIndex);

    /**
     * Return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.headerPadding;
    }

    protected abstract void drawBackground();

    protected void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks)
    {
    }

    protected abstract void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks);

    /**
     * Handles drawing a list's header row.
     */
    protected void drawListHeader(int insideLeft, int insideTop, Tessellator tessellatorIn)
    {
    }

    protected void clickedHeader(int p_148132_1_, int p_148132_2_)
    {
    }

    protected void renderDecorations(int mouseXIn, int mouseYIn)
    {
    }

    public int getSlotIndexFromScreenCoords(int posX, int posY)
    {
        int i = this.left + this.width / 2 - this.getListWidth() / 2;
        int j = this.left + this.width / 2 + this.getListWidth() / 2;
        int k = posY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        return posX < this.getScrollBarX() && posX >= i && posX <= j && l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
    }

    /**
     * Registers the IDs that can be used for the scrollbar's up/down buttons.
     */
    public void registerScrollButtons(int scrollUpButtonIDIn, int scrollDownButtonIDIn)
    {
        this.scrollUpButtonID = scrollUpButtonIDIn;
        this.scrollDownButtonID = scrollDownButtonIDIn;
    }

    /**
     * Stop the thing from scrolling out of bounds
     */
    protected void bindAmountScrolled()
    {
        this.amountScrolled = MathHelper.clamp(this.amountScrolled, 0.0F, (float)this.getMaxScroll());
    }

    public int getMaxScroll()
    {
        return Math.max(0, this.getContentHeight() - (this.bottom - this.top - 4));
    }

    /**
     * Returns the amountScrolled field as an integer.
     */
    public int getAmountScrolled()
    {
        return (int)this.amountScrolled;
    }
    
    public float getAmountScrolledFloat()
    {
        return this.amountScrolled;
    }

    public boolean isMouseYWithinSlotBounds(int p_148141_1_)
    {
        return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= this.left && this.mouseX <= this.right;
    }

    /**
     * Scrolls the slot by the given amount. A positive value scrolls down, and a negative value scrolls up.
     */
    public void scrollBy(int amount)
    {
        this.amountScrolled += (float)amount;
        this.bindAmountScrolled();
        this.initialClickY = -2;
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpButtonID)
            {
                this.amountScrolled -= (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
            else if (button.id == this.scrollDownButtonID)
            {
                this.amountScrolled += (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
        }
    }

    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;

            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();          
            
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();            
            
            ScaledResolution res = new ScaledResolution(mc);
            double scaleW = mc.displayWidth / res.getScaledWidth_double();
            double scaleH = mc.displayHeight / res.getScaledHeight_double();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            
            GL11.glScissor((int)(this.left * scaleW), (int)(mc.displayHeight -  (this.bottom * scaleH)), (int)(this.width * scaleW), (int)((this.height - top) * scaleH));
                        
            if (mc.world != null)
            {
            	GuiUtils.drawGradientRect(0, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
            } else {
            	int startY = 0;
            	int endY = this.top;
            	// Draw dark dirt background
                GlStateManager.disableLighting();
                GlStateManager.disableFog();
                mc.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                final float scale = 32.0F;
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)this.left, (double)endY, 0.0D
            		).tex(0.0D, (double)((float)endY / scale)
        			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                bufferbuilder.pos((double)(this.left + this.width), (double)endY, 0.0D
            		).tex((double)((float)this.width / scale), (double)((float)endY / scale)
        			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                bufferbuilder.pos((double)(this.left + this.width), (double)startY, 0.0D
            		).tex((double)((float)this.width / scale), (double)((float)startY / scale)
        			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                bufferbuilder.pos((double)this.left, (double)startY, 0.0D
            		).tex(0.0D, (double)((float)startY / scale)
        			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
                tessellator.draw();
            }
            
            // Draw slots
            
            // Inside container dirt background, only draw when this screen is not used in-game
            if(mc.world == null)
            {
            	this.drawContainerBackground(tessellator);
            }
            int left = this.left;
            int top = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader)
            {
                this.drawListHeader(left, top, tessellator);
            }

            this.drawSelectionBox(left, top, mouseXIn, mouseYIn, partialTicks);
            
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();

            // Draw scroll bar
            
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.renderDecorations(mouseXIn, mouseYIn);

            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            
            //           
        }
    }
    
    public void handleMouseInput()
    {
        if (this.isMouseYWithinSlotBounds(this.mouseY))
        {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom)
            {
                int i = (this.width - this.getListWidth()) / 2;
                int j = (this.width + this.getListWidth()) / 2;
                int k = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
                int l = k / this.slotHeight;

                if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0)
                {
                    this.elementClicked(l, false, this.mouseX, this.mouseY);
                    this.selectedElement = l;
                }
                else if (this.mouseX >= i && this.mouseX <= j && k < 0)
                {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int)this.amountScrolled - 4);
                }
            }

            if (Mouse.isButtonDown(0) && this.getEnabled())
            {
                if (this.initialClickY == -1)
                {
                    boolean flag1 = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom)
                    {
                        int j2 = (this.width - this.getListWidth()) / 2;
                        int k2 = (this.width + this.getListWidth()) / 2;
                        int l2 = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
                        int i1 = l2 / this.slotHeight;

                        if (i1 < this.getSize() && this.mouseX >= j2 && this.mouseX <= k2 && i1 >= 0 && l2 >= 0)
                        {
                            boolean flag = i1 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(i1, flag, this.mouseX, this.mouseY);
                            this.selectedElement = i1;
                            this.lastClicked = Minecraft.getSystemTime();
                        }
                        else if (this.mouseX >= j2 && this.mouseX <= k2 && l2 < 0)
                        {
                            this.clickedHeader(this.mouseX - j2, this.mouseY - this.top + (int)this.amountScrolled - 4);
                            flag1 = false;
                        }

                        int i3 = this.getScrollBarX();
                        int j1 = i3 + 6;

                        if (this.mouseX >= i3 && this.mouseX <= j1)
                        {
                            this.scrollMultiplier = -1.0F;
                            int k1 = this.getMaxScroll();

                            if (k1 < 1)
                            {
                                k1 = 1;
                            }

                            int l1 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
                            l1 = MathHelper.clamp(l1, 32, this.bottom - this.top - 8);
                            this.scrollMultiplier /= (float)(this.bottom - this.top - l1) / (float)k1;
                        }
                        else
                        {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (flag1)
                        {
                            this.initialClickY = this.mouseY;
                        }
                        else
                        {
                            this.initialClickY = -2;
                        }
                    }
                    else
                    {
                        this.initialClickY = -2;
                    }
                }
                else if (this.initialClickY >= 0)
                {
                    this.amountScrolled -= (float)(this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            }
            else
            {
                this.initialClickY = -1;
            }

            int i2 = Mouse.getEventDWheel();

            if (i2 != 0)
            {
                if (i2 > 0)
                {
                    i2 = -1;
                }
                else if (i2 < 0)
                {
                    i2 = 1;
                }

                this.amountScrolled += (float)(i2 * this.slotHeight / 2);
            }
        }
    }

    public void setEnabled(boolean enabledIn)
    {
        this.enabled = enabledIn;
    }

    public boolean getEnabled()
    {
        return this.enabled;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return this.width;
    }

    /**
     * Draws the selection box around the selected slot element.
     */
    protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks)
    {
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            int xPos = insideTop + j * this.slotHeight + this.headerPadding;
            int yPos = this.slotHeight - 4;

            if (xPos > this.bottom || xPos + yPos < this.top)
            {
                this.updateItemPos(j, insideLeft, xPos, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(j))
            {
                int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i1, (double)(xPos + yPos + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(xPos + yPos + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(xPos - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i1, (double)(xPos - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(xPos + yPos + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(xPos + yPos + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(xPos - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(xPos - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(j, insideLeft, xPos, yPos, mouseXIn, mouseYIn, partialTicks);
        }
    }

    protected int getScrollBarX()
    {    	
    	return this.left + this.width - 6;//this.width / 2 + 124;
    }

    /**
     * Overlays the background to hide scrolled items
     */
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        
        ScaledResolution res = new ScaledResolution(mc);
        double scaleW = mc.displayWidth / res.getScaledWidth_double();
        double scaleH = mc.displayHeight / res.getScaledHeight_double();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left      * scaleW), (int)(mc.displayHeight - (bottom * scaleH)),
                       (int)(this.width * scaleW), (int)(this.height * scaleH));
        
        if (mc.world != null)
        {
        	GuiUtils.drawGradientRect(0, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
        } else {
        	// Draw dark dirt background
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            mc.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            final float scale = 32.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.left, (double)endY, 0.0D
        		).tex(0.0D, (double)((float)endY / scale)
    			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            bufferbuilder.pos((double)(this.left + this.width), (double)endY, 0.0D
        		).tex((double)((float)this.width / scale), (double)((float)endY / scale)
    			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            bufferbuilder.pos((double)(this.left + this.width), (double)startY, 0.0D
        		).tex((double)((float)this.width / scale), (double)((float)startY / scale)
    			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            bufferbuilder.pos((double)this.left, (double)startY, 0.0D
        		).tex(0.0D, (double)((float)startY / scale)
    			).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            tessellator.draw();
        }
    }

    /**
     * Sets the left and right bounds of the slot. Param is the left bound, right is calculated as left + width.
     */
    public void setSlotXBoundsFromLeft(int leftIn)
    {
        this.left = leftIn;
        this.right = leftIn + this.width;
    }

    public int getSlotHeight()
    {
        return this.slotHeight;
    }

    protected void drawContainerBackground(Tessellator tessellator)
    {
        BufferBuilder buffer = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        
        buffer.pos(
        		(double)this.left,  
        		(double)this.bottom, 
        		0.0D
    		).tex(
				(double)((float)this.left  / f), 
				(double)((float)(this.bottom + (int)this.amountScrolled) / f)
			).color(32, 32, 32, 255).endVertex();
        
        buffer.pos(
        		(double)this.right, 
        		(double)this.bottom, 
        		0.0D
    		).tex(
				(double)((float)this.right / f), 
				(double)((float)(this.bottom + (int)this.amountScrolled) / f)
			).color(32, 32, 32, 255).endVertex();
        
        buffer.pos(
        		(double)this.right, 
        		(double)this.top,    
        		0.0D
    		).tex(
				(double)((float)this.right / f), 
				(double)((float)(this.top    + (int)this.amountScrolled) / f)
			).color(32, 32, 32, 255).endVertex();
        
        buffer.pos(
        		(double)this.left,  
        		(double)this.top,    
        		0.0D
    		).tex(
				(double)((float)this.left  / f), 
				(double)((float)(this.top    + (int)this.amountScrolled) / f)
			).color(32, 32, 32, 255).endVertex();
        
        tessellator.draw();
    }
}