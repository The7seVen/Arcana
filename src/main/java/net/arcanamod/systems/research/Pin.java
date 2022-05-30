package net.arcanamod.systems.research;

import net.arcanamod.systems.research.impls.AbstractCraftingSection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;

/**
 * A quick reference to a specific page in a research book, which may point to an item's recipe.
 */
public class Pin{
	
	@Nullable Item result;
	ResearchEntry entry;
	int stage;
	Icon icon;
	
	public Pin(@Nullable Item result, ResearchEntry entry, int stage, Icon icon){
		this.result = result;
		this.entry = entry;
		this.stage = stage;
		this.icon = icon;
	}
	
	// Grabs the icon and item from the entry.
	// If a recipe isn't being pointed to, uses the icon of the entry its in.
	public Pin(ResearchEntry entry, int stage, ServerLevel world){
		// Check if the section is a recipe.
		if(entry.sections().size() > stage){
			this.stage = stage;
			EntrySection section = entry.sections().get(stage);
			if(section instanceof AbstractCraftingSection && world.getRecipeManager().byKey(((AbstractCraftingSection)section).getRecipe()).isPresent()){
				Recipe<?> recipe = world.getRecipeManager().byKey(((AbstractCraftingSection)section).getRecipe()).get();
				this.icon = new Icon(recipe.getResultItem());
				this.result = recipe.getResultItem().getItem();
			}else
				this.icon = entry.icons().get(0);
		}else
			this.stage = 0;
		this.entry = entry;
	}
	
	public ResearchEntry getEntry(){
		return entry;
	}
	
	public int getStage(){
		return stage;
	}
	
	@Nullable
	public Item getResult(){
		return result;
	}
	
	public Icon getIcon(){
		return icon;
	}
}