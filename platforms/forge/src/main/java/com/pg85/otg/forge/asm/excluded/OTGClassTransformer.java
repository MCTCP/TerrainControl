package com.pg85.otg.forge.asm.excluded;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.pg85.otg.forge.asm.OTGHooks;

import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;

public class OTGClassTransformer implements IClassTransformer
{
	static String[] classesBeingTransformed =
	{
		"net.minecraftforge.registries.GameData", // Biome registry
		"net.minecraft.world.biome.Biome", // Biome registry
		"net.minecraft.entity.EntityLivingBase", // Gravity
		"net.minecraft.entity.item.EntityMinecart", // Gravity
		"net.minecraft.entity.projectile.EntityArrow", // Gravity
		"net.minecraft.entity.item.EntityBoat", // Gravity
		"net.minecraft.entity.item.EntityFallingBlock", // Gravity
		"net.minecraft.entity.item.EntityItem", // Gravity
		"net.minecraft.entity.projectile.EntityLlamaSpit", // Gravity
		"net.minecraft.entity.projectile.EntityShulkerBullet", // Gravity
		"net.minecraft.entity.projectile.EntityThrowable", // Gravity
		"net.minecraft.entity.item.EntityTntPrimed", // Gravity
		"net.minecraft.entity.item.EntityXPOrb", // Gravity
		"net.minecraft.entity.Entity", // Gravity
		"net.minecraftforge.common.DimensionManager" // Dimensions
	};
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] classBeingTransformed)
	{
		if(name != null && transformedName != null)
		{
			boolean isObfuscated = !name.equals(transformedName);
			int index = -1;
			for(int i = 0; i < classesBeingTransformed.length; i++)
			{
				if(classesBeingTransformed[i].equals(transformedName))
				{
					index = i;
					break;
				}
			}
			return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
		}
		return classBeingTransformed;
	}

	public byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated)
	{
		//System.out.println("Transforming: " + classesBeingTransformed[index]);
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(classBeingTransformed);
			classReader.accept(classNode, 0);

			// Do the transformation
			switch(index)
			{
				case 0: // net.minecraftforge.registries.GameData.injectSnapshot
					transformInjectSnapshot(classNode, isObfuscated);
				break;
				case 1: // net.minecraft.world.biome.Biome.getIdForBiome(biome)
					transformGetIdForBiome(classNode, isObfuscated);
				break;
				case 2: // net.minecraft.entity.EntityLivingBase.travel
					transformTravel(classNode, isObfuscated);
				break;
				case 3: // net.minecraft.entity.item.EntityMinecart.onUpdate
					transformOnUpdateMineCart(classNode, isObfuscated);
				break;
				case 4: // net.minecraft.entity.projectile.EntityArrow.onUpdate
					transformOnUpdateArrow(classNode, isObfuscated);
				break;
				case 5: // net.minecraft.entity.item.EntityBoat.onUpdate
					transformOnUpdateBoat(classNode, isObfuscated);
				break;
				case 6: // net.minecraft.entity.item.EntityFallingBlock.onUpdate
					transformOnUpdateFallingBlock(classNode, isObfuscated);
				break;
				case 7: // net.minecraft.entity.item.EntityItem.onUpdate
					transformOnUpdateItem(classNode, isObfuscated);
				break;
				case 8: // net.minecraft.entity.projectile.EntityLlamaSpit.onUpdate
					transformOnUpdateLlamaSpit(classNode, isObfuscated);
				break;
				case 9: // net.minecraft.entity.projectile.EntityShulkerBullet.onUpdate
					transformOnUpdateShulkerBullet(classNode, isObfuscated);
				break;
				case 10: // net.minecraft.entity.projectile.EntityThrowable.onUpdate
					transformOnUpdateThrowable(classNode, isObfuscated);
				break;
				case 11: // net.minecraft.entity.item.EntityTntPrimed.onUpdate
					transformOnUpdateTntPrimed(classNode, isObfuscated);
				break;
				case 12: // net.minecraft.entity.item.EntityXPOrb.onUpdate
					transformOnUpdateXPOrb(classNode, isObfuscated);
				break;
				case 13: // net.minecraft.entity.Entity.updateFallState
					transformUpdateFallState(classNode, isObfuscated);
				break;
				case 14: // net.minecraftforge.common.DimensionManager.initDimension(int dim)
					transformInitDimension1(classNode, isObfuscated);
				break; 				
			}

			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(classWriter);
			return classWriter.toByteArray();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return classBeingTransformed;
	}

	// net.minecraft.world.biome.Biome.getIdForBiome(ClassNode gameDataNode, boolean isObfuscated)
	private void transformGetIdForBiome(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "a" : "getIdForBiome";
		String injectSnapShotDescriptor = isObfuscated ? "(Lanh;)I" : "(Lnet/minecraft/world/biome/Biome;)I";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode targetNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					if(instruction instanceof FieldInsnNode)
					{
						targetNode = instruction;
						break;
					}
				}

				if(targetNode == null)
				{
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}

				/*
				Replacing net.minecraft.world.biome.Biome.getIdForBiome:

				return REGISTRY.getIDForObject(biome);

				mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "getIdForBiome", "(Lnet/minecraft/world/biome/Biome;)I", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(111, l0);

				mv.visitFieldInsn(GETSTATIC, "net/minecraft/world/biome/Biome", "REGISTRY", "Lnet/minecraft/util/registry/RegistryNamespaced;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/registry/RegistryNamespaced", "getIDForObject", "(Ljava/lang/Object;)I", false);
				mv.visitInsn(IRETURN);

				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLocalVariable("biome", "Lnet/minecraft/world/biome/Biome;", null, l0, l1, 0);
				mv.visitMaxs(2, 1);
				mv.visitEnd();

				With:

				if(biome instanceof IOTGASMBiome)
				{
					return OTGHooks.getIdForBiome(Biome biome);
				}
				REGISTRY.getIDForObject(biome);

				mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "getIdForBiome", "(Lnet/minecraft/world/biome/Biome;)I", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(111, l0);

				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(INSTANCEOF, "com/pg85/otg/asm/IOTGASMBiome");
				Label l1 = new Label();
				mv.visitJumpInsn(IFEQ, l1);
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitLineNumber(113, l2);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "com/pg85/otg/asm/OTGHooks", "getIDForObject", "(Lnet/minecraft/world/biome/Biome;)I", false);
				mv.visitInsn(IRETURN);
				mv.visitLabel(l1);
				mv.visitLineNumber(115, l1);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

				mv.visitFieldInsn(GETSTATIC, "net/minecraft/world/biome/Biome", "REGISTRY", "Lnet/minecraft/util/registry/RegistryNamespaced;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/registry/RegistryNamespaced", "getIDForObject", "(Ljava/lang/Object;)I", false);
				mv.visitInsn(IRETURN);

				Label l3 = new Label();
				mv.visitLabel(l3);
				mv.visitLocalVariable("biome", "Lnet/minecraft/world/biome/Biome;", null, l0, l3, 0);
				mv.visitMaxs(2, 1);
				mv.visitEnd();
				*/

				InsnList toInsert = new InsnList();
				toInsert.add(new VarInsnNode(ALOAD, 0));
				toInsert.add(new TypeInsnNode(INSTANCEOF, "com/pg85/otg/forge/asm/excluded/IOTGASMBiome"));
				LabelNode l1 = new LabelNode();
				toInsert.add(new JumpInsnNode(IFEQ, l1));
				LabelNode l2 = new LabelNode();
				toInsert.add(l2);
				toInsert.add(new LineNumberNode(113, l2));
				toInsert.add(new VarInsnNode(ALOAD, 0));
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getIDForObject",  injectSnapShotDescriptor, false));
				toInsert.add(new InsnNode(IRETURN));
				toInsert.add(l1);
				toInsert.add(new LineNumberNode(115, l1));
				toInsert.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

				method.instructions.insertBefore(targetNode, toInsert);

				return;
			}
		}

		//for(MethodNode method : gameDataNode.methods)
		{
			//System.out.println("Biome: " + method.name + " + " + method.desc + " + " + method.signature);
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraftforge.registries.GameData.injectSnapshot()
	private void transformInjectSnapshot(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "injectSnapshot" : "injectSnapshot";
		String injectSnapShotDescriptor = isObfuscated ? "(Ljava/util/Map;ZZ)Lcom/google/common/collect/Multimap;" : "(Ljava/util/Map;ZZ)Lcom/google/common/collect/Multimap;";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode targetNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					if(instruction.getOpcode() == ALOAD)
					{
						AbstractInsnNode instruction2 = instruction.getNext();
						if(instruction2 instanceof MethodInsnNode && ((MethodInsnNode)instruction2).desc.equals("()Ljava/util/Collection;"))
						{
							AbstractInsnNode instruction3 = instruction2.getNext();
							if(instruction3 instanceof MethodInsnNode && ((MethodInsnNode)instruction3).desc.equals("()Ljava/util/stream/Stream;"))
							{
								AbstractInsnNode instruction4 = instruction3.getNext();
								if(instruction4 instanceof InvokeDynamicInsnNode && ((InvokeDynamicInsnNode)instruction4).desc.equals("()Ljava/util/function/ToIntFunction;"))
								{
									AbstractInsnNode instruction5 = instruction4.getNext();
									if(instruction5 instanceof MethodInsnNode && ((MethodInsnNode)instruction5).desc.equals("(Ljava/util/function/ToIntFunction;)Ljava/util/stream/IntStream;"))
									{
										AbstractInsnNode instruction6 = instruction5.getNext();
										if(instruction6 instanceof MethodInsnNode && ((MethodInsnNode)instruction6).desc.equals("()I"))
										{
											AbstractInsnNode instruction7 = instruction6.getNext();
											if(instruction7.getOpcode() == ISTORE)
											{
												targetNode = instruction;
												break;
											}
										}
									}
								}
							}
						}
					}
				}

				if(targetNode == null)
				{
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}

				/*
				Replacing net.minecraftforge.registries.GameData.transformInjectSnapshot:

				int count = missing.values().stream().mapToInt(Map::size).sum();

				mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/LinkedHashMap", "values", "()Ljava/util/Collection;", false);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "stream", "()Ljava/util/stream/Stream;", true);
				mv.visitInvokeDynamicInsn("applyAsInt", "()Ljava/util/function/ToIntFunction;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"), new Object[]{Type.getType("(Ljava/lang/Object;)I"), new Handle(Opcodes.H_INVOKEINTERFACE, "java/util/Map", "size", "()I"), Type.getType("(Ljava/util/Map;)I")});
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/Stream", "mapToInt", "(Ljava/util/function/ToIntFunction;)Ljava/util/stream/IntStream;", true);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/IntStream", "sum", "()I", true);

				With:

				int count = OTGHooks.countMissingRegistryEntries(missing);

				mv.visitMethodInsn(INVOKESTATIC, "com/pg85/otg/asm/OTGHooks", "countMissingRegistryEntries", "(Ljava/util/LinkedHashMap;)I", false);
				*/

				AbstractInsnNode removeNode = targetNode.getNext();
				for(int i = 0; i < 5; i++)
				{
					removeNode = removeNode.getNext();
					method.instructions.remove(removeNode.getPrevious());
				}

				InsnList toInsert = new InsnList();
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "countMissingRegistryEntries", "(Ljava/util/LinkedHashMap;)I", false));

				method.instructions.insertBefore(removeNode, toInsert);

				return;
			}
		}

		//for(MethodNode method : gameDataNode.methods)
		{
			//System.out.println("Biome: " + method.name + " + " + method.desc + " + " + method.signature);
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// Gravity settings for players
	// net.minecraft.entity.EntityLivingBase.travel(float strafe, float vertical, float forward)
	private void transformTravel(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "a" : "travel";
		String injectSnapShotDescriptor = isObfuscated ? "(FFF)V" : "(FFF)V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.08D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.08D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactor", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// Gravity settings for minecarts
	// net.minecraft.entity.item.EntityMinecart.onUpdate()
	private void transformOnUpdateMineCart(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.03999999910593033D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.03999999910593033D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorMineCart", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// Gravity settings for arrows
	// net.minecraft.entity.projectile.EntityArrow.onUpdate()
	private void transformOnUpdateArrow(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.05000000074505806D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.05000000074505806D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorArrow", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.item.EntityBoat.updateMotion()
	private void transformOnUpdateBoat(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "x" : "updateMotion";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//double d1 = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == -0.03999999910593033D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorBoat", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.item.EntityFallingBlock.onUpdate
	private void transformOnUpdateFallingBlock(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.03999999910593033D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.03999999910593033D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorFallingBlock", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}
		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.item.EntityItem.onUpdate
	private void transformOnUpdateItem(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.03999999910593033D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() ==  0.03999999910593033D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorItem", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.projectile.LlamaSpit.onUpdate
	private void transformOnUpdateLlamaSpit(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.05999999865889549D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.05999999865889549D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorLlamaSpit", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.projectile.EntityShulkerBullet.onUpdate
	private void transformOnUpdateShulkerBullet(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.05000000074505806D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.04D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorShulkerBullet", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.projectile.EntityThrowable.getGravityVelocity
	private void transformOnUpdateThrowable(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "j" : "getGravityVelocity";
		String injectSnapShotDescriptor = isObfuscated ? "()F" : "()F";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//return 0.03F;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Float && ((Float)((LdcInsnNode)instruction).cst).floatValue() == 0.03F)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorThrowable", "(Lnet/minecraft/entity/Entity;)F", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.item.EntityTntPrimed",
	private void transformOnUpdateTntPrimed(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.03999999910593033D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.03999999910593033D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorTNTPrimed", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}

	// net.minecraft.entity.item.EntityXPOrb.onUpdate
	private void transformOnUpdateXPOrb(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "B_" : "onUpdate";
		String injectSnapShotDescriptor = isObfuscated ? "()V" : "()V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode instructionToRemove = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					//this.motionY -= 0.029999999329447746D;

					if(instruction.getOpcode() == LDC && ((LdcInsnNode)instruction).cst instanceof Double && ((Double)((LdcInsnNode)instruction).cst).doubleValue() == 0.029999999329447746D)
					{
						instructionToRemove = instruction;
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getGravityFactorXPOrb", "(Lnet/minecraft/entity/Entity;)D", false));
						method.instructions.insertBefore(instructionToRemove, toInsert);
						break;
					}
				}
				if(instructionToRemove != null)
				{
					method.instructions.remove(instructionToRemove);
				} else {
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}
				return;
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}
			
	// Make sure that OTG dimensions get initialised by OTGDimensionManager.initDimension
	// net.minecraftforge.common.DimensionManager.initDimension(int dim)
	private void transformInitDimension1(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "initDimension" : "initDimension";
		String injectSnapShotDescriptor = isObfuscated ? "(I)V" : "(I)V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				AbstractInsnNode targetNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					if(instruction instanceof LineNumberNode)
					{
						if(targetNode == null)
						{
							targetNode = instruction;
						} else {
							// Inserting 5 new lines before this, so add +5 to all linenumber nodes.
							((LineNumberNode)instruction).line += 5;
						}
					}
				}

				if(targetNode == null)
				{
					throw new RuntimeException("OTG is not compatible with this version of Forge.");
				}				
				
				/*

				Inserting at start of net.minecraftforge.common.DimensionManager.initDimension(int dim):

				if(OTGHooks.InitOTGDimension(dim))
				{
					return;
				}

				mv.visitVarInsn(ILOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "com/pg85/otg/forge/asm/OTGHooks", "InitOTGDimension", "(I)Z", false);
				Label l4 = new Label();
				mv.visitJumpInsn(IFEQ, l4);
				Label l5 = new Label();
				mv.visitLabel(l5);
				mv.visitLineNumber(247, l5);
				mv.visitInsn(RETURN);
				mv.visitLabel(l4);
				mv.visitLineNumber(250, l4);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

				Inserting 5 lines of code directly below:
				
				mv.visitLineNumber(245, l3);

			 	*/
				
				InsnList toInsert = new InsnList();
				toInsert.add(new VarInsnNode(ILOAD, 0));
							
				toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "InitOTGDimension",  "(I)Z", false));
				LabelNode l4 = new LabelNode();
				toInsert.add(new JumpInsnNode(IFEQ, l4));
				LabelNode l5 = new LabelNode();
				toInsert.add(l5);
				toInsert.add(new LineNumberNode(247, l5));
				toInsert.add(new InsnNode(RETURN));
				toInsert.add(l4);
				toInsert.add(new LineNumberNode(250, l5));
				toInsert.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
				
				method.instructions.insertBefore(targetNode, toInsert);

				return;
			}
		}

		//for(MethodNode method : gameDataNode.methods)
		{
			//System.out.println("Biome: " + method.name + " + " + method.desc + " + " + method.signature);
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}	
	
	// Gravity settings for falling damage
	//protected void net.minecraft.entity.Entity.updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
	private void transformUpdateFallState(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "a" : "updateFallState";
		String injectSnapShotDescriptor = isObfuscated ? "(DZLawt;Let;)V" : "(DZLnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)V";

		for(MethodNode method : gameDataNode.methods)
		{
			if(method.name.equals(injectSnapShot) && method.desc.equals(injectSnapShotDescriptor))
			{
				boolean bFound = false;
				for(AbstractInsnNode instruction : method.instructions.toArray())
				{
					// this.fallDistance = (float)((double)this.fallDistance - y);
					// should be
					// this.fallDistance = (float)((double)this.fallDistance - (y * gravityFactor));
					// Where gravityFactor is between 0 and 1 and determines how much falling damage should be applied based on the gravity of the world.

					//mv.visitVarInsn(DLOAD, 1);

					if(instruction.getOpcode() == DLOAD)
					{
						// Only apply to the second DLOAD
						if(bFound)
						{
							// Insert new instruction
							InsnList toInsert = new InsnList();
							toInsert.add(new VarInsnNode(ALOAD, 0));
							toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OTGHooks.class), "getFallDamageFactor", "(DLnet/minecraft/entity/Entity;)D", false));
							method.instructions.insertBefore(instruction.getNext(), toInsert);
							return;
						}
						bFound = true;
					}
				}
			}
		}

		throw new RuntimeException("OTG is not compatible with this version of Forge.");
	}
}