package net.arcanamod.aspects;

import com.google.gson.*;
import com.mojang.realmsclient.util.JsonUtils;
import net.arcanamod.aspects.handlers.AspectHandler;
import net.arcanamod.aspects.handlers.AspectHolder;
import net.arcanamod.items.CrystalItem;
import net.arcanamod.systems.taint.Taint;
import net.arcanamod.util.Pair;
import net.arcanamod.util.StreamUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Associates items with aspects. Every item is associated with a set of aspect stacks, and item stack may be given extra
 * aspects.
 * <br/>
 * Items and item tags are associated with sets of aspects in JSON files. Additionally, stack functions, defined in code,
 * can add or remove aspects to an item stack, based on NBT, damage levels, or anything else.
 */
public class ItemAspectRegistry extends SimpleJsonResourceReloadListener {
	
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static boolean processing = false;
	
	private static Map<Item, List<AspectStack>> itemAssociations = new HashMap<>();
	private static Map<TagKey<Item>, List<AspectStack>> itemTagAssociations = new HashMap<>();
	private static Collection<BiConsumer<ItemStack, List<AspectStack>>> stackFunctions = new ArrayList<>();
	
	private static Map<Item, List<AspectStack>> itemAspects = new HashMap<>();
	
	private RecipeManager recipes;
	private static Set<Item> generating = new HashSet<>();
	
	public ItemAspectRegistry(RecipeManager recipes){
		super(GSON, "arcana/aspect_maps");
		this.recipes = recipes;
	}
	
	public static List<AspectStack> get(Item item){
		return item != Items.AIR && itemAspects.get(item) != null ? new ArrayList<>(itemAspects.get(item)) : new ArrayList<>();
	}
	
	public static List<AspectStack> get(ItemStack stack){
		List<AspectStack> itemAspects = get(stack.getItem());
		for(BiConsumer<ItemStack, List<AspectStack>> function : stackFunctions)
			function.accept(stack, itemAspects);
		return squish(itemAspects);
	}
	
	protected void apply(@Nonnull Map<ResourceLocation, JsonElement> objects, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler){
		// remove existing data
		processing = true;
		itemAssociations.clear();
		itemTagAssociations.clear();
		stackFunctions.clear();
		itemAspects.clear();
		
		// add new data
		addStackFunctions();
		objects.forEach(ItemAspectRegistry::applyJson);
		
		// compute aspect assignment
		// for every item defined, give them aspects
		itemAssociations.forEach((key, value) -> {
			itemAspects.merge(key, value, (left, right) -> {
				List<AspectStack> news = new ArrayList<>(left);
				news.addAll(right);
				return news;
			});
			// Idk...
			itemTagAssociations.forEach((tag_key, tag_value) -> {
				if(key.getDefaultInstance().is(tag_key)) {
					itemAspects.merge(key, tag_value, (left, right) -> {
						List<AspectStack> news = new ArrayList<>(left);
						news.addAll(right);
						return news;
					});
				}
			});
		});
		
		// for every item tag, give them aspects
//		itemTagAssociations.forEach((key, value) -> {
//			for(Item item : key.getAllElements())
//				itemAspects.merge(item, value, (left, right) -> {
//					List<AspectStack> news = new ArrayList<>(left);
//					news.addAll(right);
//					return news;
//				});
//		});
		
		// for every item not already given aspects in this way, give according to recipes
		for(Recipe<?> recipe : recipes.getRecipes()){
			Item item = recipe.getResultItem().getItem();
			if(!itemAspects.containsKey(item))
				itemAspects.put(item, getGenerating(item));
			// generating avoids getting stuck in recursive loops.
			generating.clear();
		}
		
		// squish lists: [{1x plant}, {1x plant}, {1x machine}] -> [{2x plant}, {1x machine}]
		Collection<Item> itemMirror = new ArrayList<>(itemAspects.keySet());
		for(Item item : itemMirror)
			if(itemAspects.containsKey(item)){
				List<AspectStack> squished = squish(itemAspects.get(item));
				squished.removeIf(AspectStack::isEmpty);
				itemAspects.put(item, squished);
			}
		LOGGER.info("Assigned aspects to {} items", itemAspects.size());
		processing = false;
	}
	
	private static List<AspectStack> squish(List<AspectStack> unSquished){
		return StreamUtils.partialReduce(unSquished, AspectStack::getAspect, (left, right) -> new AspectStack(left.getAspect(), left.getAmount() + right.getAmount()));
	}
	
	private List<AspectStack> getFromRecipes(Item item){
		generating.add(item);
		List<AspectStack> ret;
		List<List<AspectStack>> allGenerated = new ArrayList<>();
		// consider every recipe that produces this item
		for(Recipe<?> recipe : recipes.getRecipes())
			if(recipe.getResultItem().getItem() == item){
				List<AspectStack> generated = new ArrayList<>();
				for(Ingredient ingredient : recipe.getIngredients()){
					if(ingredient.getItems().length > 0){
						ItemStack first = ingredient.getItems()[0];
						if(!generating.contains(first.getItem())){
							List<AspectStack> ingredients = getGenerating(first);
							for(AspectStack stack : ingredients)
								generated.add(new AspectStack(stack.getAspect(), Math.max(stack.getAmount() / recipe.getResultItem().getCount(), 1)));
						}
					}
				}
				if(recipe instanceof AspectInfluencingRecipe)
					((AspectInfluencingRecipe)recipe).influence(generated);
				allGenerated.add(generated);
			}
		ret = allGenerated.stream()
				// pair of aspects:total num of aspects
				.map(stacks -> Pair.of(stacks, stacks.stream().mapToDouble(AspectStack::getAmount).sum()))
				// filter combos with 0 aspects
				.filter(pair -> pair.getSecond() > 0)
				// sort by least total aspects
				.min(Comparator.comparingDouble(Pair::getSecond))
				// get aspects
				.map(Pair::getFirst).orElse(new ArrayList<>());
		return ret;
	}
	
	private List<AspectStack> getGenerating(Item item){
		if(itemAspects.containsKey(item))
			return itemAspects.get(item);
		else
			return getFromRecipes(item);
	}
	
	private List<AspectStack> getGenerating(ItemStack stack){
		if(itemAspects.containsKey(stack.getItem()))
			return get(stack.getItem());
		else
			return getFromRecipes(stack.getItem());
	}
	
	public static boolean isProcessing(){
		return processing;
	}
	
	private static void applyJson(ResourceLocation location, JsonElement e){
		// just go through the keys and map them to tags or items
		// then put them in the relevant maps
		// error if they don't map
		if(e.isJsonObject()){
			JsonObject object = e.getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : object.entrySet()){
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				if(key.startsWith("#")){
					ResourceLocation itemTagLoc = new ResourceLocation(key.substring(1));
					TagKey<Item> itemTag = ItemTags.create(itemTagLoc);
					if(itemTag != null)
						parseAspectStackList(location, value).ifPresent(stacks -> itemTagAssociations.put(itemTag, stacks));
					else
						LOGGER.error("Invalid item tag \"" + key.substring(1) + "\" found in file " + location + "!");
				}else{
					ResourceLocation itemLoc = new ResourceLocation(key);
					Item item = ForgeRegistries.ITEMS.getValue(itemLoc);
					if(item != null)
						parseAspectStackList(location, value).ifPresent(stacks -> itemAssociations.put(item, stacks));
					else
						LOGGER.error("Invalid item tag \"" + key.substring(1) + "\" found in file " + location + "!");
				}
			}
		}
	}
	
	public static Optional<List<AspectStack>> parseAspectStackList(ResourceLocation file, JsonElement listElement){
		if(listElement.isJsonArray()){
			JsonArray array = listElement.getAsJsonArray();
			List<AspectStack> ret = new ArrayList<>();
			for(JsonElement element : array){
				if(element.isJsonObject()){
					JsonObject object = element.getAsJsonObject();
					String aspectName = object.get("aspect").getAsString();
					int amount = JsonUtils.getIntOr("amount", object, 1);
					Aspect aspect = AspectUtils.getAspectByName(aspectName);
					if(aspect != null)
						ret.add(new AspectStack(aspect, amount));
					else
						LOGGER.error("Invalid aspect " + aspectName + " referenced in file " + file);
				}else
					LOGGER.error("Invalid aspect stack found in " + file + " - not an object!");
			}
			return Optional.of(ret);
		}else
			LOGGER.error("Invalid aspect stack list found in " + file + " - not a list!");
		return Optional.empty();
	}
	
	protected void addStackFunctions(){
		stackFunctions.add((item, stacks) -> {
			// Add 5x magic to enchanted items
			if(EnchantmentHelper.getEnchantments(item).size() > 0)
				stacks.add(new AspectStack(Aspects.MANA, 5));
		});
		stackFunctions.add((item, stacks) -> {
			// Assign crystals their aspects
			// Could be done with data but I don't care
			if(item.getItem() instanceof CrystalItem)
				stacks.add(new AspectStack(((CrystalItem)item.getItem()).aspect, 1));
		});
		stackFunctions.add((item, stacks) -> {
			// Give tainted blocks taint
			if(item.getItem() instanceof BlockItem && Taint.isTainted(((BlockItem)item.getItem()).getBlock()))
				stacks.add(new AspectStack(Aspects.TAINT, 3));
		});
		stackFunctions.add((item, stacks) -> {
			// Give any non-IOverrideAspects aspect handler item their aspects
			if (!(item.getItem() instanceof IOverrideAspects)) {
				AspectHandler.getOptional(item).ifPresent(handler -> {
					List<AspectStack> list = new ArrayList<>();
					for (AspectHolder holder : handler.getHolders()) {
						AspectStack stack = holder.getStack();
						if (!stack.isEmpty())
							list.add(stack);
					}
					stacks.addAll(list);
				});
			}
		});
		stackFunctions.add((item, stacks) -> {
			// Apply IOverrideAspects
			if(item.getItem() instanceof IOverrideAspects){
				List<AspectStack> overrides = ((IOverrideAspects)item.getItem()).getAspectStacks(item);
				if(!(overrides.get(0).isEmpty())){
					stacks.clear();
					stacks.addAll(overrides);
				}
			}
		});
	}
}