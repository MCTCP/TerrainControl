package com.pg85.otg.forge.asm;

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
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import scala.tools.asm.Type;
import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;

public class OTGClassTransformer implements IClassTransformer
{
	static String[] classesBeingTransformed =
	{
		"net.minecraftforge.registries.GameData",
		"net.minecraft.world.biome.Biome"
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
		System.out.println("Transforming: " + classesBeingTransformed[index]);
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
					break;
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
				toInsert.add(new TypeInsnNode(INSTANCEOF, "com/pg85/otg/forge/generator/OTGBiome"));
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

	// net.minecraftforge.registries.GameData.transformInjectSnapshot
	private void transformInjectSnapshot(ClassNode gameDataNode, boolean isObfuscated)
	{
		String injectSnapShot = isObfuscated ? "injectSnapshot" : "injectSnapshot";
		String injectSnapShotDescriptor = isObfuscated ? "injectSnapshot" : "(Ljava/util/Map;ZZ)Lcom/google/common/collect/Multimap;";

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
					break;
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
}