package net.arcanamod.blocks.tiles;

import com.google.common.collect.Sets;
import com.mojang.datafixers.types.Type;
import net.arcanamod.Arcana;
import net.arcanamod.blocks.ArcanaBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ArcanaTiles{
	
	public static final DeferredRegister<TileEntityType<?>> TES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Arcana.MODID);
	
	public static final RegistryObject<TileEntityType<NodeTileEntity>> NODE_TE = TES.register("node", () -> new TileEntityType<>(NodeTileEntity::new, Sets.newHashSet(ArcanaBlocks.NORMAL_NODE.get()), null));
}