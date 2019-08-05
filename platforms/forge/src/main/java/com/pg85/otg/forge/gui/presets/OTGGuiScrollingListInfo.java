package com.pg85.otg.forge.gui.presets;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.pg85.otg.forge.gui.OTGGuiScrollingList;

import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;

class OTGGuiScrollingListInfo extends OTGGuiScrollingList
{
	private final OTGGuiPresetList otgGuiPresetList;
	@Nullable
    private ResourceLocation logoPath;
    private Dimension logoDims;
    private List<ITextComponent> lines = null;

    public OTGGuiScrollingListInfo(OTGGuiPresetList otgGuiPresetList, List<String> lines, @Nullable ResourceLocation logoPath, Dimension logoDims)
    {
        super(
    		  otgGuiPresetList.getMinecraftInstance(),
              otgGuiPresetList.width - otgGuiPresetList.listWidth - otgGuiPresetList.margin - otgGuiPresetList.rightMargin,
              otgGuiPresetList.height,
              otgGuiPresetList.topMargin, 
              otgGuiPresetList.height - otgGuiPresetList.bottomMargin,
              otgGuiPresetList.listWidth + otgGuiPresetList.margin,
              60, // TODO: Find out what slotheight does exactly, doesn't seem to influence size for info, only scroll speed?
              otgGuiPresetList.width,
              otgGuiPresetList.height);
		this.otgGuiPresetList = otgGuiPresetList;
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
                    ret.addAll(GuiUtilRenderComponents.splitText(chat, maxTextLength, this.otgGuiPresetList.getFontRenderer(), false, true));
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
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess, float zLevel)
    {
        int top = relativeY;

        if (logoPath != null)
        {
            GlStateManager.enableBlend();
            this.otgGuiPresetList.mc.renderEngine.bindTexture(logoPath);
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
                this.otgGuiPresetList.getFontRenderer().drawStringWithShadow(line.getFormattedText(), this.left + 4, top, 0xFFFFFF);
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
                k += this.otgGuiPresetList.getFontRenderer().getStringWidth(((TextComponentString)part).getText());
                if (k >= x)
                {
                	this.otgGuiPresetList.handleComponentClick(part);
                    break;
                }
            }
        }
    }
}