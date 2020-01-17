package net.kineticdevelopment.arcana.api.spells;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellEffectEarth implements ISpellEffect {

    @Override
    public String getName() {
        return "EARTH";
    }

    @Override
    public void getEffect(BlockPos block, World world, int power) {
        if(!world.getBlockState(block.add(0,1,0)).getMaterial().isReplaceable()) {
            block.add(0, 1, 0);
            world.setBlockState(block, Blocks.GRASS.getDefaultState());
        } else {
            world.setBlockState(block, Blocks.DIRT.getDefaultState());
        }
    }


    @Override
    public void getEffect(EntityLivingBase entity, int power) {
        entity.setHealth(entity.getHealth() + power);
    }

    @Override
    public int getParticleID() {
        return EnumParticleTypes.WATER_BUBBLE.getParticleID();
    }

    @Override
    public boolean isAttack() {
        return false;
    }

}
