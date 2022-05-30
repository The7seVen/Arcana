package net.arcanamod.items.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.arcanamod.ArcanaConfig;
import net.arcanamod.aspects.AspectInfluencingRecipe;
import net.arcanamod.aspects.AspectStack;
import net.arcanamod.aspects.AspectUtils;
import net.arcanamod.aspects.ItemAspectRegistry;
import net.arcanamod.capabilities.Researcher;
import net.arcanamod.systems.research.Parent;
import net.arcanamod.util.StreamUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.arcanamod.ArcanaVariables.arcLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AlchemyRecipe implements Recipe<AlchemyInventory>, AspectInfluencingRecipe{
	
	// vanilla registry
	public static RecipeType<AlchemyRecipe> ALCHEMY = Registry.register(Registry.RECIPE_TYPE, arcLoc("alchemy"), new RecipeType<AlchemyRecipe>(){
		public String toString(){
			return arcLoc("alchemy").toString();
		}
	});

	public static List<AlchemyRecipe> RECIPES = new ArrayList<>();
	
	Ingredient in;
	ItemStack out;
	List<AspectStack> aspectsIn;
	List<Parent> requiredResearch;
	ResourceLocation id;
	
	public AlchemyRecipe(Ingredient in, ItemStack out, List<AspectStack> aspectsIn, List<Parent> requiredResearch, ResourceLocation id){
		this.in = in;
		this.out = out;
		this.aspectsIn = aspectsIn;
		this.requiredResearch = requiredResearch;
		this.id = id;
		
		if (RECIPES.stream().noneMatch(m -> m.id.toString().equals(this.id.toString())))
			RECIPES.add(this);
	}
	
	public boolean matches(AlchemyInventory inv, Level world){
		// correct item
		return in.test(inv.stack)
				// and correct research
				&& requiredResearch.stream().allMatch(parent -> parent.satisfiedBy(Researcher.getFrom(inv.getCrafter())))
				// and correct aspects
				&& aspectsIn.stream().allMatch(stack -> inv.getAspectMap().containsKey(stack.getAspect()) && inv.getAspectMap().get(stack.getAspect()).getAmount() >= stack.getAmount());
	}
	
	public ItemStack assemble(AlchemyInventory inv){
		return out.copy();
	}
	
	public boolean canCraftInDimensions(int width, int height){
		return true;
	}
	
	public ItemStack getResultItem(){
		return out;
	}
	
	public ResourceLocation getId(){
		return id;
	}
	
	public List<AspectStack> getAspects(){
		return aspectsIn;
	}
	
	public NonNullList<Ingredient> getIngredients(){
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(in);
		return list;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return ArcanaRecipes.Serializers.ALCHEMY.get();
	}
	
	public RecipeType<?> getType(){
		return ALCHEMY;
	}
	
	public void influence(List<AspectStack> in){
		in.addAll(aspectsIn.stream().map(stack -> new AspectStack(stack.getAspect(), (int)(stack.getAmount() * ArcanaConfig.ALCHEMY_ASPECT_CARRY_FRACTION.get()))).collect(Collectors.toList()));
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AlchemyRecipe>{
		
		public AlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json){
			Ingredient ingredient = Ingredient.fromJson(json.get("in"));
			ItemStack out = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "out"));
			List<AspectStack> aspects = ItemAspectRegistry.parseAspectStackList(recipeId, GsonHelper.getAsJsonArray(json, "aspects")).orElseThrow(() -> new JsonSyntaxException("Missing aspects in " + recipeId + "!"));
			List<Parent> research = StreamUtils.toStream(GsonHelper.getAsJsonArray(json, "research", null))
					.map(JsonElement::getAsString)
					.map(Parent::parse)
					.collect(Collectors.toList());
			return new AlchemyRecipe(ingredient, out, aspects, research, recipeId);
		}
		
		public AlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer){
			Ingredient ingredient = Ingredient.fromNetwork(buffer);
			ItemStack out = buffer.readItem();
			int size = buffer.readVarInt();
			List<AspectStack> aspects = new ArrayList<>(size);
			for(int i = 0; i < size; i++)
				aspects.add(new AspectStack(AspectUtils.getAspectByName(buffer.readUtf()), buffer.readFloat()));
			
			size = buffer.readVarInt();
			List<Parent> requiredResearch = new ArrayList<>(size);
			for(int i = 0; i < size; i++)
				requiredResearch.add(Parent.parse(buffer.readUtf()));
			
			return new AlchemyRecipe(ingredient, out, aspects, requiredResearch, recipeId);
		}
		
		public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe){
			recipe.in.toNetwork(buffer);
			buffer.writeItemStack(recipe.out, false);
			buffer.writeVarInt(recipe.aspectsIn.size());
			for(AspectStack stack : recipe.aspectsIn){
				buffer.writeUtf(stack.getAspect().name());
				buffer.writeFloat(stack.getAmount());
			}
			buffer.writeVarInt(recipe.requiredResearch.size());
			for(Parent research : recipe.requiredResearch)
				buffer.writeUtf(research.asString());
		}
	}
}