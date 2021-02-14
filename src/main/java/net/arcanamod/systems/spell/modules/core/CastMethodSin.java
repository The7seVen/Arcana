package net.arcanamod.systems.spell.modules.core;

import net.arcanamod.aspects.Aspect;
import net.arcanamod.systems.spell.SpellState;
import net.arcanamod.systems.spell.modules.SpellModule;
import net.minecraft.nbt.CompoundNBT;

public class CastMethodSin extends SpellModule {
	public Aspect aspect;

	@Override
	public String getName() {
		return "cast_method_sin";
	}

	@Override
	public int getInputAmount() {
		return 1;
	}

	@Override
	public boolean canConnect(SpellModule connectingModule) {
		return connectingModule instanceof CastMethod;
	}

	@Override
	public CompoundNBT toNBT(){
		CompoundNBT compound = new CompoundNBT();
		compound.putString("aspect", aspect.toResourceLocation().toString());
		compound.putInt("x", x);
		compound.putInt("y", y);
		return compound;
	}

	@Override
	public boolean mouseUp(SpellState spellState, int x, int y) {


		return false;
	}
}
