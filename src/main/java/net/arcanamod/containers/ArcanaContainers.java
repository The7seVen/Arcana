package net.arcanamod.containers;

import net.arcanamod.Arcana;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ArcanaContainers{
	public static final DeferredRegister<ContainerType<?>> CON = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Arcana.MODID);
	
	public static final RegistryObject<ContainerType<ResearchTableContainer>> REASERCH_TABLE = CON.register("research_table", () -> IForgeContainerType.create(ResearchTableContainer::new));
	public static final RegistryObject<ContainerType<ArcaneCraftingTableContainer>> ARCANE_CRAFTING_TABLE = CON.register("arcane_crafting_table", () -> IForgeContainerType.create(ArcaneCraftingTableContainer::new));
}