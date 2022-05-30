package net.arcanamod.systems.research;

import net.arcanamod.systems.research.impls.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.arcanamod.util.StreamUtils.streamAndApply;

/**
 * Represents one section of content - for example, continuous text, an image, or an inline recipe. May provide a number of pins.
 */
public abstract class EntrySection{
	
	// static stuff
	// when addon support is to be added: change this from strings to ResourceLocations so mods can register more
	private static Map<String, Function<String, EntrySection>> factories = new LinkedHashMap<>();
	private static Map<String, Function<CompoundTag, EntrySection>> deserializers = new LinkedHashMap<>();
	
	public static Function<String, EntrySection> getFactory(String type){
		return factories.get(type);
	}
	
	public static EntrySection makeSection(String type, String content){
		if(getFactory(type) != null)
			return getFactory(type).apply(content);
		else
			return null;
	}
	
	public static EntrySection deserialze(CompoundTag passData){
		String type = passData.getString("type");
		CompoundTag data = passData.getCompound("data");
		List<Requirement> requirements = streamAndApply(passData.getList("requirements", 10), CompoundTag.class, Requirement::deserialize).collect(Collectors.toList());
		if(deserializers.get(type) != null){
			EntrySection section = deserializers.get(type).apply(data);
			requirements.forEach(section::addRequirement);
			// recieving on client
			section.entry = new ResourceLocation(passData.getString("entry"));
			return section;
		}
		return null;
	}
	
	public static void init(){
		factories.put(StringSection.TYPE, StringSection::new);
		deserializers.put(StringSection.TYPE, Tag -> new StringSection(Tag.getString("text")));
		factories.put(CraftingSection.TYPE, CraftingSection::new);
		deserializers.put(CraftingSection.TYPE, Tag -> new CraftingSection(Tag.getString("recipe")));
		factories.put(SmeltingSection.TYPE, SmeltingSection::new);
		deserializers.put(SmeltingSection.TYPE, Tag -> new SmeltingSection(Tag.getString("recipe")));
		factories.put(AlchemySection.TYPE, AlchemySection::new);
		deserializers.put(AlchemySection.TYPE, Tag -> new AlchemySection(Tag.getString("recipe")));
		factories.put(ArcaneCraftingSection.TYPE, ArcaneCraftingSection::new);
		deserializers.put(ArcaneCraftingSection.TYPE, Tag -> new ArcaneCraftingSection(Tag.getString("recipe")));
		factories.put(ImageSection.TYPE, ImageSection::new);
		deserializers.put(ImageSection.TYPE, Tag -> new ImageSection(Tag.getString("image")));
		factories.put(AspectCombosSection.TYPE, __ -> new AspectCombosSection());
		deserializers.put(AspectCombosSection.TYPE, __ -> new AspectCombosSection());
	}
	
	// instance stuff
	
	protected List<Requirement> requirements = new ArrayList<>();
	protected ResourceLocation entry;
	
	public void addRequirement(Requirement requirement){
		requirements.add(requirement);
	}
	
	public List<Requirement> getRequirements(){
		return Collections.unmodifiableList(requirements);
	}
	
	public CompoundTag getPassData(){
		CompoundTag Tag = new CompoundTag();
		Tag.putString("type", getType());
		Tag.put("data", getData());
		Tag.putString("entry", getEntry().toString());
		
		ListTag list = new ListTag();
		getRequirements().forEach((requirement) -> list.add(requirement.getPassData()));
		Tag.put("requirements", list);
		
		return Tag;
	}
	
	public ResourceLocation getEntry(){
		return entry;
	}
	
	public abstract String getType();
	
	public abstract CompoundTag getData();
	
	public void addOwnRequirements(){
	}
	
	/**
	 * Returns a stream containing this entry's pins.
	 *
	 * @param index
	 * 		The index in the entry of this section.
	 * @param world
	 * 		The world the player is in.
	 * @param entry
	 * 		The entry this is in.
	 * @return This entry's pins.
	 */
	public Stream<Pin> getPins(int index, Level world, ResearchEntry entry){
		return Stream.empty();
	}
}