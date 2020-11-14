package com.pg85.otg.forge.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// TODO: Uses a copy of the floating islands world options screen atm, replace.
@OnlyIn(Dist.CLIENT)
public class CreateOTGWorldScreen extends Screen
{
	private static final ITextComponent field_243277_a = new TranslationTextComponent("createWorld.customize.buffet.biome");
	private final Screen parent;
	private final Consumer<Biome> field_238592_b_;
	private final MutableRegistry<Biome> field_243278_p;
	private CreateOTGWorldScreen.BiomeList biomeList;
	private Biome field_238593_p_;
	private Button field_205313_u;
	
	public CreateOTGWorldScreen(Screen p_i242054_1_, DynamicRegistries p_i242054_2_, Consumer<Biome> p_i242054_3_, Biome p_i242054_4_)
	{
		super(new TranslationTextComponent("createWorld.customize.buffet.title"));
		this.parent = p_i242054_1_;
		this.field_238592_b_ = p_i242054_3_;
		this.field_238593_p_ = p_i242054_4_;
		this.field_243278_p = p_i242054_2_.func_243612_b(Registry.field_239720_u_);
	}
	
	public void func_231175_as__()
	{
		this.field_230706_i_.displayGuiScreen(this.parent);
	}
	
	protected void func_231160_c_()
	{
		this.field_230706_i_.keyboardListener.enableRepeatEvents(true);
		this.biomeList = new CreateOTGWorldScreen.BiomeList();
		this.field_230705_e_.add(this.biomeList);
		this.field_205313_u = this.func_230480_a_(
			new Button(
				this.field_230708_k_ / 2 - 155, 
				this.field_230709_l_ - 28, 
				150, 
				20, 
				DialogTexts.field_240632_c_, 
				(p_241579_1_) ->
				{
					this.field_238592_b_.accept(this.field_238593_p_);
					this.field_230706_i_.displayGuiScreen(this.parent);
				}
			)
		);
		
		this.func_230480_a_(
			new Button(
				this.field_230708_k_ / 2 + 5, 
				this.field_230709_l_ - 28, 
				150, 
				20, 
				DialogTexts.field_240633_d_, 
				(p_213015_1_) ->
				{
					this.field_230706_i_.displayGuiScreen(this.parent);
				}
			)
		);
		
		this.biomeList.func_241215_a_(
			this.biomeList.func_231039_at__().stream().filter(
				(p_241578_1_) ->
				{
					return Objects.equals(p_241578_1_.field_238599_b_, this.field_238593_p_);
				}
			).findFirst().orElse(
				(CreateOTGWorldScreen.BiomeList.BiomeEntry)null
			)
		);
	}
	
	private void func_205306_h()
	{
		this.field_205313_u.field_230693_o_ = this.biomeList.func_230958_g_() != null;
	}
	
	public void func_230430_a_(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.func_231165_f_(0);
		this.biomeList.func_230430_a_(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		func_238472_a_(p_230430_1_, this.field_230712_o_, this.field_230704_d_, this.field_230708_k_ / 2, 8, 16777215);
		func_238472_a_(p_230430_1_, this.field_230712_o_, field_243277_a, this.field_230708_k_ / 2, 28, 10526880);
		super.func_230430_a_(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}
	
	@OnlyIn(Dist.CLIENT)
	class BiomeList extends ExtendedList<CreateOTGWorldScreen.BiomeList.BiomeEntry>
	{
		private BiomeList()
		{
			super(CreateOTGWorldScreen.this.field_230706_i_, CreateOTGWorldScreen.this.field_230708_k_, CreateOTGWorldScreen.this.field_230709_l_, 40, CreateOTGWorldScreen.this.field_230709_l_ - 37, 16);
			
			CreateOTGWorldScreen.this.field_243278_p.func_239659_c_().stream().sorted(
				Comparator.comparing(
					(p_238598_0_) -> {
						return p_238598_0_.getKey().func_240901_a_().toString();
					}
				)
			).forEach(
				(p_238597_1_) -> {
					this.func_230513_b_(new CreateOTGWorldScreen.BiomeList.BiomeEntry(p_238597_1_.getValue()));
				}
			);
		}
	
		protected boolean func_230971_aw__()
		{
			return CreateOTGWorldScreen.this.func_241217_q_() == this;
		}
	
		public void func_241215_a_(@Nullable CreateOTGWorldScreen.BiomeList.BiomeEntry p_241215_1_)
		{
			super.func_241215_a_(p_241215_1_);
			if (p_241215_1_ != null)
			{
				CreateOTGWorldScreen.this.field_238593_p_ = p_241215_1_.field_238599_b_;
				NarratorChatListener.INSTANCE.say((new TranslationTextComponent("narrator.select", CreateOTGWorldScreen.this.field_243278_p.getKey(p_241215_1_.field_238599_b_))).getString());
			}
			CreateOTGWorldScreen.this.func_205306_h();
		}
		
		@OnlyIn(Dist.CLIENT)
		class BiomeEntry extends ExtendedList.AbstractListEntry<CreateOTGWorldScreen.BiomeList.BiomeEntry>
		{
			private final Biome field_238599_b_;
			private final ITextComponent field_243282_c;
			
			public BiomeEntry(Biome p_i232272_2_)
			{
				this.field_238599_b_ = p_i232272_2_;
				ResourceLocation resourcelocation = CreateOTGWorldScreen.this.field_243278_p.getKey(p_i232272_2_);
				String s = "biome." + resourcelocation.getNamespace() + "." + resourcelocation.getPath();
				if (LanguageMap.getInstance().func_230506_b_(s))
				{
					this.field_243282_c = new TranslationTextComponent(s);
				} else {
					this.field_243282_c = new StringTextComponent(resourcelocation.toString());
				}		
			}
			
			public void func_230432_a_(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				AbstractGui.func_238475_b_(p_230432_1_, CreateOTGWorldScreen.this.field_230712_o_, this.field_243282_c, p_230432_4_ + 5, p_230432_3_ + 2, 16777215);
			}
			
			public boolean func_231044_a_(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0)
				{
					BiomeList.this.func_241215_a_(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
