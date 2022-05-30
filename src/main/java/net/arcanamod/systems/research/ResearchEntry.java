package net.arcanamod.systems.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.arcanamod.util.StreamUtils.streamAndApply;

/**
 * Represents a node in the research tree. Stores an ordered list of entry sections representing its content.
 */
public class ResearchEntry{
	
	private ResourceLocation key;
	private List<EntrySection> sections;
	private List<String> meta;
	private List<Parent> parents;
	private List<Icon> icons;
	private ResearchCategory category;
	
	private String name, desc;
	
	private int x, y;
	
	public ResearchEntry(ResourceLocation key, List<EntrySection> sections, List<Icon> icons, List<String> meta, List<Parent> parents, ResearchCategory category, String name, String desc, int x, int y){
		this.key = key;
		this.sections = sections;
		this.icons = icons;
		this.meta = meta;
		this.parents = parents;
		this.category = category;
		this.name = name;
		this.desc = desc;
		this.x = x;
		this.y = y;
	}
	
	public List<EntrySection> sections(){
		return Collections.unmodifiableList(sections);
	}
	
	public List<Icon> icons(){
		return icons;
	}
	
	public List<String> meta(){
		return meta;
	}
	
	public List<Parent> parents(){
		return parents;
	}
	
	public ResearchCategory category(){
		return category;
	}
	
	public ResourceLocation key(){
		return key;
	}
	
	public String name(){
		return name;
	}
	
	public String description(){
		return desc;
	}
	
	public int x(){
		return x;
	}
	
	public int y(){
		return y;
	}
	
	public CompoundTag serialize(ResourceLocation tag){
		CompoundTag Tag = new CompoundTag();
		// key
		Tag.putString("id", tag.toString());
		// name, desc
		Tag.putString("name", name());
		Tag.putString("desc", description());
		// x, y
		Tag.putInt("x", x());
		Tag.putInt("y", y());
		// sections
		ListTag list = new ListTag();
		sections().forEach((section) -> list.add(section.getPassData()));
		Tag.put("sections", list);
		// icons
		ListTag icons = new ListTag();
		icons().forEach((icon) -> icons.add(StringTag.valueOf(icon.toString())));
		Tag.put("icons", icons);
		// parents
		ListTag parents = new ListTag();
		parents().forEach((parent) -> parents.add(StringTag.valueOf(parent.asString())));
		Tag.put("parents", parents);
		// meta
		ListTag meta = new ListTag();
		meta().forEach((met) -> meta.add(StringTag.valueOf(met)));
		Tag.put("meta", meta);
		return Tag;
	}
	
	public static ResearchEntry deserialize(CompoundTag Tag, ResearchCategory in){
		ResourceLocation key = new ResourceLocation(Tag.getString("id"));
		String name = Tag.getString("name");
		String desc = Tag.getString("desc");
		int x = Tag.getInt("x");
		int y = Tag.getInt("y");
		List<EntrySection> sections = streamAndApply(Tag.getList("sections", 10), CompoundTag.class, EntrySection::deserialze).collect(Collectors.toList());
		List<Parent> betterParents = streamAndApply(Tag.getList("parents", 8), StringTag.class, StringTag::getAsString).map(Parent::parse).collect(Collectors.toList());
		List<Icon> icons = streamAndApply(Tag.getList("icons", 8), StringTag.class, StringTag::getAsString).map(Icon::fromString).collect(Collectors.toList());
		List<String> meta = streamAndApply(Tag.getList("meta", 8), StringTag.class, StringTag::getAsString).collect(Collectors.toList());
		return new ResearchEntry(key, sections, icons, meta, betterParents, in, name, desc, x, y);
	}
	
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(!(o instanceof ResearchEntry))
			return false;
		ResearchEntry entry = (ResearchEntry)o;
		return key.equals(entry.key);
	}
	
	public int hashCode(){
		return Objects.hash(key);
	}
	
	/**
	 * Returns a stream containing all of the pins of contained sections.
	 *
	 * @param world
	 * 		The world the player is in.
	 * @return A stream containing the pins of contained sections.
	 */
	public Stream<Pin> getAllPins(Level world){
		return sections().stream().flatMap(section -> section.getPins(sections.indexOf(section), world, this));
	}
}