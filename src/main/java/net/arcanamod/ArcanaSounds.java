package net.arcanamod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.ObjectHolderRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings({"ConstantConditions", "DanglingJavadoc"})
/**
 *	Sound class that contains:
 * 	- Impl (SoundEvent registration)
 * 	- SoundTypes
 * 	- Play SoundEvent functions
 */
public class ArcanaSounds {

	// SoundTypes
	public static ForgeSoundType JAR = new ForgeSoundType(0.6F, 1.0F, () -> Impl.jar_break, () -> Impl.jar_step, () -> Impl.jar_place, () -> Impl.jar_break, () -> Impl.jar_step);
	public static ForgeSoundType TAINT = new ForgeSoundType(0.6F, 1.4F, () -> Impl.taint_break, () -> Impl.taint_step, () -> Impl.taint_place, () -> Impl.taint_break, () -> Impl.taint_step);
	public static ForgeSoundType TAINT_STONE = new ForgeSoundType(0.6F, 1.4F, () -> Impl.taint_stone_break, () -> Impl.taint_stone_step, () -> Impl.taint_stone_place, () -> Impl.taint_stone_break, () -> Impl.taint_stone_break);
	public static ForgeSoundType CRYSTAL = new ForgeSoundType(0.6F, 1.0F, () -> Impl.crystal_break, () -> Impl.crystal_place, () -> Impl.crystal_place, () -> Impl.crystal_break, () -> Impl.crystal_place);

	// SoundEvents
	public static void playPhialshelfSlideSound(Player player){
		playSound(player, ArcanaSounds.Impl.phialshelf_slide, SoundSource.BLOCKS,0.4f,1.2f);
	}

	@SuppressWarnings("ConstantConditions")
	public static void playPhialCorkpopSound(Player player){
		playSound(player, ArcanaSounds.Impl.phial_corkpop, SoundSource.BLOCKS,0.4f,1.4f);
	}

	public static void playSpellCastSound(Player player) {
		playSound(player, ArcanaSounds.Impl.spell_cast, SoundSource.PLAYERS,0.4f,1.0f);
	}

	public static void playSoundOnce(Player player, SoundEvent evt, SoundSource category, float v, float p) {
		if (new Random().nextInt(400) == 0)
			playSound(player, evt, category, v, p);
	}

	public static void playSound(Player player, SoundEvent evt, SoundSource category, float v, float p) {
		if (!player.level.isClientSide()) return;
		if (evt.equals(null)) return;
		player.level.playSound(null, player, evt, category,v,p);
	}

	//Impl
	@ObjectHolder(Arcana.MODID)
	@ParametersAreNonnullByDefault
	@SuppressWarnings({"null", "DanglingJavadoc"})
	/**
	 * Implementation of SoundEvents.
	 * Every SoundEvent in this class will be registered.
	 * You get SoundName from sounds.json
	 */
	public static class Impl {

		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.FIELD)
		private static @interface SoundName {
			String value();
		}

		// Jar/Phial Sounds

		@SoundName("jar_break")
		public static final SoundEvent jar_break = null;

		@SoundName("jar_step")
		public static final SoundEvent jar_step = null;

		@SoundName("jar_place")
		public static final SoundEvent jar_place = null;

		@SoundName("phialshelf_slide")
		public static final SoundEvent phialshelf_slide = null;

		@SoundName("phial_corkpop")
		public static final SoundEvent phial_corkpop = null;

		// Tainted Block Sounds

		@SoundName("taint_break")
		public static final SoundEvent taint_break = null;
		@SoundName("taint_step")
		public static final SoundEvent taint_step = null;
		@SoundName("taint_place")
		public static final SoundEvent taint_place = null;

		@SoundName("taint_stone_break")
		public static final SoundEvent taint_stone_break = null;
		@SoundName("taint_stone_step")
		public static final SoundEvent taint_stone_step = null;
		@SoundName("taint_stone_place")
		public static final SoundEvent taint_stone_place = null;

		// Block Sounds

		@SoundName("arcana_taint_portal_extended")
		public static final SoundEvent arcana_taint_portal_extended = null;

		@SoundName("crystal_break")
		public static final SoundEvent crystal_break = null;

		@SoundName("crystal_place")
		public static final SoundEvent crystal_place = null;

		@SoundName("crystal_break_negative")
		public static final SoundEvent crystal_break_negative = null;

		@SoundName("crystal_place_negative")
		public static final SoundEvent crystal_place_negative = null;

		//Music Discs
		@SoundName("music_arcana_theme")
		public static final SoundEvent music_arcana_theme = null;

		@SoundName("music_arcana_green_sleeves")
		public static final SoundEvent music_arcana_green_sleeves = null;

		// Ambience

		@SoundName("arcanainfusionpart1d")
		public static final SoundEvent arcanainfusionpart1d = null;

		@SoundName("arcanainfusionpart4de")
		public static final SoundEvent arcanainfusionpart4de = null;

		// Spell Cast

		@SoundName("spell_cast")
		public static final SoundEvent spell_cast = null;

		public static void init() {}

		// Entities

		@SoundName("arcana_mana_creeper")
		public static final SoundEvent arcana_mana_creeper = null;

		// Nodes

		@SoundName("arcananodes")
		public static final SoundEvent arcananodes = null;

		@SoundName("arcananodesnegative")
		public static final SoundEvent arcananodesnegative = null;

		@SoundName("arcana_hunger_node")
		public static final SoundEvent arcana_hunger_node = null;

		static {
			for (Field f : ArcanaSounds.Impl.class.getDeclaredFields()) {
				if (f.isAnnotationPresent(SoundName.class)) {
					ForgeRegistries.SOUND_EVENTS.register(new SoundEvent(new ResourceLocation(Arcana.MODID, f.getAnnotation(SoundName.class).value())).setRegistryName(f.getName().toLowerCase(Locale.ROOT)));
				}
			}

			ObjectHolderRegistry.applyObjectHolders();
		}
	}
}