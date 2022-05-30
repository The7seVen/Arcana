package net.arcanamod.blocks.multiblocks.taint_scrubber;

import net.arcanamod.blocks.ArcanaBlocks;
import net.arcanamod.world.ServerAuraView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TaintScrubberExtensionBlock extends Block implements ITaintScrubberExtension{
	private Type type;
	
	public TaintScrubberExtensionBlock(Block.Properties properties, Type type){
		super(properties);
		this.type = type;
	}
	
	@Override
	public boolean isValidConnection(Level world, BlockPos pos){
		// Check that this is the right position.
		if(world.getBlockState(pos).getBlock() != this)
			return false;
		
		// Scrubber Base is placed below a regular Scrubber.
		if(this.type.equals(Type.SCRUBBER_MK2))
			if(world.getBlockState(pos.above(1)).getBlock().equals(ArcanaBlocks.TAINT_SCRUBBER_MK1.get()))
				return true;
		// Bore is placed below a regular Scrubber or Scrubber Base.
		if(this.type.equals(Type.BORE))
			if(world.getBlockState(pos.above(2)).getBlock().equals(ArcanaBlocks.TAINT_SCRUBBER_MK1.get()) && !world.getBlockState(pos.above(1)).getBlock().equals(Blocks.AIR)){
				return true;
			}else if(world.getBlockState(pos.above(1)).getBlock().equals(ArcanaBlocks.TAINT_SCRUBBER_MK1.get()))
				return true;
		// Sucker is placed above the regular Scrubber.
		if(this.type.equals(Type.SUCKER))
			return world.getBlockState(pos.below()).getBlock().equals(ArcanaBlocks.TAINT_SCRUBBER_MK1.get());
		return false;
	}
	
	@Override
	public void sendUpdate(Level world, BlockPos pos){
		if(type == Type.SUCKER)
			if(world.getBlockState(pos.below()).getBlock().equals(ArcanaBlocks.TAINT_SCRUBBER_MK1.get()))
				world.setBlockAndUpdate(pos.below(), world.getBlockState(pos.below()).setValue(TaintScrubberBlock.SUPPORTED, isValidConnection(world, pos)));
	}
	
	/**
	 * Runs extension action.
	 *
	 * @param world
	 * 		World
	 * @param pos
	 * 		Position of TaintScrubber
	 */
	@Override
	public void run(Level world, BlockPos pos, CompoundTag compound){
		if(this.type.equals(Type.SUCKER))
			if(!world.isClientSide && world.getGameTime() % 2 == 0){
				ServerAuraView aura = new ServerAuraView((ServerLevel) world);
				//aura.addTaintAt(pos, -Math.abs(compound.getInt("speed") + 1));
				aura.addFluxAt(pos, -1);
			}
	}
	
	@Override
	public CompoundTag getShareableData(CompoundTag compound){
		if(this.type.equals(Type.SCRUBBER_MK2)){
			if(compound.getInt("h_range") < 16)
				compound.putInt("h_range", 16);
			if(compound.getInt("v_range") < 16)
				compound.putInt("v_range", 16);
		}
		if(this.type.equals(Type.BORE))
			compound.putInt("v_range", 256);
		return compound;
	}
	
	/**
	 * For addon dev's:
	 * Arcana Pre-Defined Extensions. You can add own extension implementing ITaintScrubberExtension.
	 */
	public enum Type{
		SCRUBBER_MK2,
		SUCKER,
		BORE
	}
}
